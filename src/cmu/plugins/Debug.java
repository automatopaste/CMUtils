package cmu.plugins;

import cmu.CMUtils;
import cmu.misc.PathGeneratorUtil;
import cmu.plugins.renderers.SegmentRenderer;
import cmu.shaders.particles.SegmentedParticle;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class Debug extends BaseEveryFrameCombatPlugin {
//    private final IntervalUtil interval = new IntervalUtil(1f, 1f);
//    private PathGeneratorUtil pathGeneratorUtil;

    @Override
    public void init(CombatEngineAPI engine) {
//        SegmentRenderer segmentRenderer = (SegmentRenderer) CMUtils.initBuiltinRenderer(CMUtils.BuiltinRenderers.SEGMENT);
//        pathGeneratorUtil = new PathGeneratorUtil(segmentRenderer);
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
//        if (Global.getCombatEngine().isPaused()) return;
//        pathGeneratorUtil.update();
//
//        interval.advance(amount);
//        if (interval.intervalElapsed()) {
//            SegmentedParticle.SegmentedParticleParams params = new SegmentedParticle.SegmentedParticleParams();
//            params.lifetime = 1f;
//            params.width = 50f;
//
//            pathGeneratorUtil.createPath(params, new Vector2f(200f, 0f), new Vector2f(1000f, 0f), 10);
//        }
    }
}
