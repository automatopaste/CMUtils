package cmu.plugins;

import cmu.CMUtils;
import cmu.misc.PathGeneratorUtil;
import cmu.plugins.renderers.BattlespaceSpriteParticleRenderer;
import cmu.plugins.renderers.SegmentRenderer;
import cmu.shaders.particles.BaseParticle;
import cmu.shaders.particles.ComputeFunction;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.opengl.GL14;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class RenderDebug extends BaseEveryFrameCombatPlugin {
    private final IntervalUtil interval = new IntervalUtil(0.1f, 0.1f);
    private PathGeneratorUtil pathGeneratorUtil;

    private BattlespaceSpriteParticleRenderer spriteParticleRenderer;

    @Override
    public void init(CombatEngineAPI engine) {
        SegmentRenderer segmentRenderer = (SegmentRenderer) CMUtils.initBuiltinRenderer(CMUtils.BuiltinRenderers.SEGMENT);
        segmentRenderer.setBlendEquation(GL14.GL_FUNC_ADD);
        pathGeneratorUtil = new PathGeneratorUtil(segmentRenderer);

        spriteParticleRenderer = (BattlespaceSpriteParticleRenderer) CMUtils.initBuiltinParticleRenderer(CMUtils.BuiltinParticleRenderers.SPRITE, CombatEngineLayers.BELOW_SHIPS_LAYER);
        spriteParticleRenderer.setBlendEquation(GL14.GL_FUNC_ADD);
        spriteParticleRenderer.setSprite(Global.getSettings().getSprite("sexy_back", "sexy_back"));
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (Global.getCombatEngine().isPaused()) return;

//        pathGeneratorUtil.advance(amount);

        //interval.advance(amount);
        if (interval.intervalElapsed()) {
//            PathGeneratorUtil.StaticPathData params = new PathGeneratorUtil.StaticPathData();
//            params.lifetime = 1f;
//            params.width = 20f;
//            params.range = 200f;
//            //params.shuffleInterval = new IntervalUtil(0.1f, 0.1f);
//            params.start = new Vector2f(0f, 0f);
//            params.end = VectorUtils.rotate(new Vector2f(500f, 0f), (float) (Math.random() * 360f));
//            params.numSegments = 5;
//
//            pathGeneratorUtil.addPathStatic(params);

            BaseParticle.ParticleParams params = new BaseParticle.ParticleParams();
            for (ShipAPI ship : Global.getCombatEngine().getShips()) {
                params.angle = (float) (360f * Math.random());
                params.vel = VectorUtils.rotate(new Vector2f(300f, 0f), ship.getFacing() + 180f);
                params.sizeInit = new Vector2f(0f, 0f);
                params.sizeFinal = new Vector2f(200f, 200f);
                params.computeFunction = new ComputeFunction.ConstantAlphaFunction(1f);
                params.lifetime = 2f;
                spriteParticleRenderer.addParticle(new Vector2f(ship.getLocation()), params);
            }
        }
    }
}
