package cmu;

import cmu.plugins.GUIDebug;
import cmu.plugins.renderers.SegmentRenderer;
import cmu.shaders.BaseParticleRenderer;
import cmu.plugins.renderers.BattlespaceSpriteParticleRenderer;
import cmu.plugins.renderers.ImplosionParticleRenderer;
import cmu.shaders.BaseRenderPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;

import java.util.ArrayList;
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
        IMPLOSION
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

    public static BaseParticleRenderer initBuiltinParticleRenderer(BuiltinParticleRenderers builtinParticleRenderers) {
        BaseParticleRenderer baseParticleRenderer = null;
        switch (builtinParticleRenderers) {
            case SPRITE:
                baseParticleRenderer = new BattlespaceSpriteParticleRenderer();
                break;
            case IMPLOSION:
                baseParticleRenderer = new ImplosionParticleRenderer();
                break;
        }

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
}
