package cmu;

import cmu.plugins.GUIDebug;
import cmu.plugins.renderers.PolygonParticleRenderer;
import cmu.plugins.renderers.SegmentRenderer;
import cmu.shaders.BaseParticleRenderer;
import cmu.plugins.renderers.BattlespaceSpriteParticleRenderer;
import cmu.plugins.renderers.ImplosionParticleRenderer;
import cmu.shaders.BaseRenderPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.ShipAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CMUtils {
    private static final int GLOBAL_MAX = Global.getSettings().getInt("cmu_GlobalMaxParticles");
    private static final int GLOBAL_MIN = Global.getSettings().getInt("cmu_GlobalMinParticles");
    private static int GLOBAL_CURRENT = 1;

    private static final boolean FORCE_UPDATE = Global.getSettings().getBoolean("cmu_ForceRendererParticleCapUpdate");

    private static final List<BaseParticleRenderer> ACTIVE_PARTICLE_RENDERERS = new ArrayList<>();
    private static final List<BaseRenderPlugin> ACTIVE_RENDERERS = new ArrayList<>();

    private static GUIDebug GUI_DEBUG_PLUGIN;

    public enum BuiltinParticleRenderers {
        SPRITE,
        IMPLOSION,
        POLYGON
    }

    public enum BuiltinRenderers {
        SEGMENT
    }

    private CMUtils() {

    }

    private static void updateRendererCaps() {
        GLOBAL_CURRENT = Math.max(GLOBAL_MAX / ACTIVE_PARTICLE_RENDERERS.size(), GLOBAL_MIN);

        if (FORCE_UPDATE) {
            for (BaseParticleRenderer renderer : ACTIVE_PARTICLE_RENDERERS) {
                renderer.updateMaxCount(GLOBAL_CURRENT);
            }
        }
    }

    public static void removeRenderer(BaseParticleRenderer baseParticleRenderer) {
        ACTIVE_PARTICLE_RENDERERS.remove(baseParticleRenderer);
        baseParticleRenderer.cleanup();
    }

    public static BaseParticleRenderer initBuiltinParticleRenderer(BuiltinParticleRenderers builtinParticleRenderers, CombatEngineLayers layer) {
        BaseParticleRenderer baseParticleRenderer = null;
        switch (builtinParticleRenderers) {
            case SPRITE:
                baseParticleRenderer = new BattlespaceSpriteParticleRenderer();
                break;
            case IMPLOSION:
                baseParticleRenderer = new ImplosionParticleRenderer();
                break;
            case POLYGON:
                baseParticleRenderer = new PolygonParticleRenderer();
                break;
        }

        baseParticleRenderer.setLayer(layer);

        ACTIVE_PARTICLE_RENDERERS.add(baseParticleRenderer);
        updateRendererCaps();

        Global.getCombatEngine().addLayeredRenderingPlugin(baseParticleRenderer);

        return baseParticleRenderer;
    }

    public static BaseRenderPlugin initBuiltinRenderer(BuiltinRenderers builtinRenderers) {
        BaseRenderPlugin baseRenderPlugin = null;
        switch (builtinRenderers) {
            case SEGMENT:
                baseRenderPlugin = new SegmentRenderer();
                break;
        }

        ACTIVE_RENDERERS.add(baseRenderPlugin);

        Global.getCombatEngine().addLayeredRenderingPlugin(baseRenderPlugin);

        return baseRenderPlugin;
    }

    public static BaseParticleRenderer initCustomParticleRenderer(CombatEngineAPI engine, BaseParticleRenderer renderer) {
        engine.addLayeredRenderingPlugin(renderer);
        ACTIVE_PARTICLE_RENDERERS.add(renderer);

        return renderer;
    }

    /**
     * Distributes global max, renderers guaranteed minimum
     * @return recommended max for the renderer (possible to override)
     */
    public static int getMaxParticles() {
        return GLOBAL_CURRENT;
    }

    public static void setGuiDebug(GUIDebug plugin) {
        if (GUI_DEBUG_PLUGIN != null) Global.getCombatEngine().removePlugin(GUI_DEBUG_PLUGIN);
        GUI_DEBUG_PLUGIN = plugin;
    }

    public static GUIDebug getGuiDebug() {
        return GUI_DEBUG_PLUGIN;
    }

    public enum ShipSearchFighters {
        FIGHTERS,
        NOT_FIGHTERS,
        DONT_CARE
    }

    public enum ShipSearchOwner {
        FRIENDLY,
        ENEMY,
        DONT_CARE
    }

    public static List<ShipAPI> getShipsInRange(Vector2f loc, float range, int owner, ShipSearchFighters searchFighters, ShipSearchOwner searchOwner) {
        Iterator<Object> iter = Global.getCombatEngine().getShipGrid().getCheckIterator(loc, range * 2f, range * 2f);

        List<ShipAPI> out = new ArrayList<>();

        while (iter.hasNext()) {
            ShipAPI ship = (ShipAPI) iter.next();

            if (ship.isHulk() || !ship.isAlive()) continue;

            switch (searchOwner) {
                case ENEMY:
                    if (owner == ship.getOwner()) continue;
                    break;
                case FRIENDLY:
                    if (owner != ship.getOwner()) continue;
                    break;
                case DONT_CARE:
                    break;
            }

            switch (searchFighters) {
                case FIGHTERS:
                    if (!ship.isFighter()) continue;
                    break;
                case NOT_FIGHTERS:
                    if (ship.isFighter()) continue;
                    break;
                case DONT_CARE:
                    break;
            }

            if (MathUtils.getDistanceSquared(loc, ship.getLocation()) <= range * range) {
                out.add(ship);
            }
        }

        return out;
    }
}
