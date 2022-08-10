package cmu.misc;

import cmu.plugins.renderers.SegmentRenderer;
import cmu.shaders.particles.SegmentedParticle;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PathGeneratorUtil {
    private final SegmentRenderer renderer;
    private final List<SegmentedParticle> paths;

    private final Random random = new Random(6969);

    public PathGeneratorUtil(SegmentRenderer renderer) {
        this.renderer = renderer;
        paths = new ArrayList<>();
    }

    public void update() {

    }

    public void createPath(SegmentedParticle.SegmentedParticleParams segmentedParticleParams, Vector2f start, Vector2f end, int numSegments) {
        SegmentedParticle path = new SegmentedParticle(start, segmentedParticleParams);
        paths.add(path);
        renderer.addParticle(path);

        float interval = Vector2f.sub(end, start, new Vector2f()).length() / numSegments;
        for (int i = 0; i < numSegments; i++) {
            path.addSegment(new Vector2f(interval, 0f));
        }
    }
}
