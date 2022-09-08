package cmu.plugins;

import cmu.CMUtils;
import cmu.misc.PathGeneratorUtil;
import cmu.plugins.renderers.SegmentRenderer;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.opengl.GL14;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class RenderDebug extends BaseEveryFrameCombatPlugin {
    private final IntervalUtil interval = new IntervalUtil(0.05f, 0.05f);
    private PathGeneratorUtil pathGeneratorUtil;

    @Override
    public void init(CombatEngineAPI engine) {
        SegmentRenderer segmentRenderer = (SegmentRenderer) CMUtils.initBuiltinRenderer(CMUtils.BuiltinRenderers.SEGMENT);
        segmentRenderer.setBlendEquation(GL14.GL_FUNC_ADD);
        pathGeneratorUtil = new PathGeneratorUtil(segmentRenderer);
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (Global.getCombatEngine().isPaused()) return;

        pathGeneratorUtil.advance(amount);

        interval.advance(amount);
        if (interval.intervalElapsed()) {
            PathGeneratorUtil.StaticPathData params = new PathGeneratorUtil.StaticPathData();
            params.lifetime = 1f;
            params.width = 20f;
            params.range = 200f;
            //params.shuffleInterval = new IntervalUtil(0.1f, 0.1f);
            params.start = new Vector2f(0f, 0f);
            params.end = VectorUtils.rotate(new Vector2f(500f, 0f), (float) (Math.random() * 360f));
            params.numSegments = 5;

            pathGeneratorUtil.addPathStatic(params);
        }
    }
}
