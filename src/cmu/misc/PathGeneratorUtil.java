package cmu.misc;

import cmu.plugins.renderers.SegmentRenderer;
import cmu.shaders.particles.SegmentedPath;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class PathGeneratorUtil {
    private final SegmentRenderer renderer;
    private final List<PathData> paths;

    private static final Random RANDOM = new Random(6969);

    public PathGeneratorUtil(SegmentRenderer renderer) {
        this.renderer = renderer;
        paths = new ArrayList<>();
    }

    public void advance(float amount) {
        renderer.clear();

        for (Iterator<PathData> iterator = paths.iterator(); iterator.hasNext();) {
            PathData path = iterator.next();
            path.function.advance(path, amount);

            if (path.age > path.lifetime) {
                iterator.remove();
            } else {
                renderer.addToFrame(path.path);
            }
        }
    }

    public void addPathStatic(StaticPathData data) {
        data.path = new SegmentedPath(data.start, data.width);

        Vector2f interval = Vector2f.sub(data.end, data.start, new Vector2f());
        VectorUtils.clampLength(interval, interval.length() / data.numSegments);

        List<Vector2f> segments = new ArrayList<>();
        for (int i = 0; i < data.numSegments; i++) {
            float y = i == data.numSegments - 1 ? 0f : (RANDOM.nextFloat() * data.range * 2f) - data.range;
            Vector2f offset = new Vector2f(0f, y);
            Vector2f.add(offset, interval, offset);
            segments.add(offset);
        }

        float angle = VectorUtils.getFacing(interval);
        for (Vector2f segment : segments) {
            data.path.addSegment(segment, angle);
        }

        paths.add(data);
    }

    public abstract static class PathData {
        public IntervalUtil shuffleInterval = new IntervalUtil(999f, 999f);
        public SegmentedPath path;
        public float width = 40f;
        public float range = 50f;
        public float lifetime = 1f;
        public float age = 0f;
        public Vector2f start = new Vector2f(0f, 0f);
        public int numSegments;
        public PathComputeFunction function;
    }

    public static class StaticPathData extends PathData {
        public Vector2f end = new Vector2f(200f, 0f);

        public StaticPathData() {
            function = new StaticComputeFunction();
            numSegments = 10;
        }
    }

    public static class EntityTrackingPathData extends PathData {
        public CombatEntityAPI anchor;

        public EntityTrackingPathData() {
            numSegments = 10;
        }
    }

    public static class EntityBrushPathData extends PathData {

        public EntityBrushPathData() {
            numSegments = 1;
        }
    }

    public static abstract class PathComputeFunction {
        public abstract void advance(PathData data, float amount);
    }

    public static class StaticComputeFunction extends PathComputeFunction {
        @Override
        public void advance(PathData pathData, float amount) {
            StaticPathData data = (StaticPathData) pathData;

            data.shuffleInterval.advance(amount);
            if (data.shuffleInterval.intervalElapsed()) {
                data.path.clearSegments();

                Vector2f interval = Vector2f.sub(data.end, data.start, new Vector2f());
                VectorUtils.clampLength(interval, interval.length() / data.numSegments);

                List<Vector2f> segments = new ArrayList<>();
                for (int i = 0; i < data.numSegments; i++) {
                    float y = i == data.numSegments - 1 ? 0f : (RANDOM.nextFloat() * data.range * 2f) - data.range;
                    Vector2f offset = new Vector2f(0f, y);
                    Vector2f.add(offset, interval, offset);
                    segments.add(offset);
                }

                float angle = VectorUtils.getFacing(interval);
                for (Vector2f segment : segments) {
                    VectorUtils.rotate(segment, angle);
                    data.path.addSegment(segment, angle);
                }
            }

            data.age += amount;
        }
    }
}
