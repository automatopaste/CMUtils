package cmu;

import cmu.plugins.renderers.SegmentRenderer;
import cmu.shaders.BaseParticleRenderer;
import cmu.plugins.renderers.BattlespaceSpriteParticleRenderer;
import cmu.plugins.renderers.ImplosionParticleRenderer;
import cmu.shaders.particles.BaseParticle;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;

import java.util.ArrayList;
import java.util.List;

public class CMUtils {
    private static final int GLOBAL_MAX = Global.getSettings().getInt("cmu_GlobalMaxParticles");
    private static final int GLOBAL_MIN = Global.getSettings().getInt("cmu_GlobalMinParticles");
    private static int GLOBAL_CURRENT = 1;

    private static final boolean FORCE_UPDATE = Global.getSettings().getBoolean("cmu_ForceRendererParticleCapUpdate");

    private static final List<BaseParticleRenderer> activeRenderers = new ArrayList<>();

    public enum BuiltinRenderers {
        SPRITE,
        IMPLOSION,
        SEGMENT
    }

    private CMUtils() {

    }

    private static void updateRendererCaps() {
        GLOBAL_CURRENT = Math.max(GLOBAL_MAX / activeRenderers.size(), GLOBAL_MIN);

        if (FORCE_UPDATE) {
            for (BaseParticleRenderer renderer : activeRenderers) {
                renderer.updateMaxCount(GLOBAL_CURRENT);
            }
        }
    }

    public static void removeRenderer(BaseParticleRenderer baseParticleRenderer) {
        activeRenderers.remove(baseParticleRenderer);
        baseParticleRenderer.cleanup();
    }

    public static BaseParticleRenderer initBuiltinRenderer(BuiltinRenderers builtinRenderers) {
        BaseParticleRenderer baseParticleRenderer = null;
        switch (builtinRenderers) {
            case SPRITE:
                baseParticleRenderer = new BattlespaceSpriteParticleRenderer();
                break;
            case IMPLOSION:
                baseParticleRenderer = new ImplosionParticleRenderer();
                break;
            case SEGMENT:
                baseParticleRenderer = new SegmentRenderer();
                break;
        }

        activeRenderers.add(baseParticleRenderer);
        updateRendererCaps();

        Global.getCombatEngine().addLayeredRenderingPlugin(baseParticleRenderer);

        return baseParticleRenderer;
    }

    public static BaseParticleRenderer initCustomParticleRenderer(CombatEngineAPI engine, BaseParticleRenderer renderer) {
        engine.addLayeredRenderingPlugin(renderer);
        activeRenderers.add(renderer);

        return renderer;
    }

    /**
     * Distributes global max, renderers guaranteed minimum
     * @return recommended max for the renderer (possible to override)
     */
    public static int getMaxParticles() {
        return GLOBAL_CURRENT;
    }
}
