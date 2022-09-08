package cmu.plugins;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;

public class LightningPathGenerator extends BaseEveryFrameCombatPlugin {
//    private final LightningSegmentRenderer renderer;
//    private final List<LightningPathData> paths;
//
//    private final Random random = new Random(6969);
//
//    public LightningPathGenerator() {
//        renderer = (LightningSegmentRenderer) CMUtils.initBuiltinRenderer(CMUtils.BuiltinRenderers.LIGHTNING_SEGMENTED);
//        paths = new ArrayList<>();
//    }
//
//    @Override
//    public void advance(float amount, List<InputEventAPI> events) {
//        if (Global.getCombatEngine().isPaused()) return;
//
//        int index = 1;
//        Iterator<LightningPathData> iterator = paths.iterator();
//        while (iterator.hasNext()) {
//            LightningPathData path = iterator.next();
//
//            path.age += amount;
//
//            if (path.numSegments >= path.segmentIntervals.length) {
//                iterator.remove();
//            }
//        }
//    }
//
//    public void addPath(Vector2f from, Vector2f to, SegmentedParticle.SegmentedParticleParams params) {
//        params.from = from;
//        params.to = to;
//
//        LightningPathData path = new LightningPathData(params);
//        paths.add(path);
//
//        // generate interval values
//        float[] segmentIntervals = new float[path.numSegments];
//        float interval = 1f / path.numSegments;
//        float cumulative = 0f;
//        for (int i = 0; i < path.numSegments; i++) {
//            if (i == segmentIntervals.length - 1) {
//                segmentIntervals[i] = 1f - cumulative;
//                break;
//            }
//
//            float dev = interval * path.segmentMaxDeviation;
//            float amount = interval + ((random.nextFloat() * dev * 2f) - dev);
//            segmentIntervals[i] = amount;
//            cumulative += amount;
//        }
//        path.segmentIntervals = segmentIntervals;
//
//        // generate vertices
//        cumulative = 0f;
//        Vector2f[] vertices = new Vector2f[path.numSegments * 2 + 2];
//        float[] offsets = new float[path.numSegments + 1];
//        for (int i = 0; i < path.numSegments; i++) {
//            int index = i * 2;
//            vertices[index] = new Vector2f(cumulative, 0f);
//            vertices[index + 1] = new Vector2f(cumulative, 1f);
//            cumulative += path.segmentIntervals[i];
//
//            offsets[i] = (random.nextFloat() * path.segmentMaxDeviation * 2f) - path.segmentMaxDeviation;
//        }
//        vertices[path.numSegments * 2] = new Vector2f(1f, 0f);
//        vertices[path.numSegments * 2 + 1] = new Vector2f(1f, 1f);
//        offsets[path.numSegments] = 0f;
//        path.vertices = vertices;
//        path.offsets = offsets;
//
//        // spawn particle
//        SegmentedParticle.SegmentedParticleParams segmentedParticleParams = new SegmentedParticle.SegmentedParticleParams();
//        segmentedParticleParams.from = from;
//        segmentedParticleParams.to = to;
//        SegmentedParticle segmentedParticle = new SegmentedParticle(segmentedParticleParams);
//
//        renderer.addParticle(segmentedParticle);
//    }
//
//    private static class LightningPathData {
//        public Vector2f[] vertices;
//        public float[] segmentIntervals;
//        public float[] offsets;
//        public float age = 0f;
//
//        public Color color;
//        public float width;
//        public float lifetime;
//        public int numSegments;
//        public float segmentOffsetArcRange;
//        public Vector2f from;
//        public Vector2f to;
//        public float segmentMaxDeviation;
//        public float angle;
//
//        public LightningPathData(SegmentedParticle.SegmentedParticleParams params) {
//            color = params.color;
//            width = params.width;
//            lifetime = params.lifetime;
//            numSegments = params.numSegments;
//            segmentOffsetArcRange = params.segmentOffsetArcRange;
//            from = params.from;
//            to = params.to;
//            segmentMaxDeviation = params.segmentMaxDeviation;
//            angle = VectorUtils.getFacing(Vector2f.sub(to, from, new Vector2f()));
//        }
//    }
//
//    public static class LightningSegmentParticleComputeFunction extends ComputeFunction {
//        private final float end;
//
//        public LightningSegmentParticleComputeFunction(float end) {
//            this.end = end;
//        }
//
//        @Override
//        public void advance(float delta, BaseParticle data) {
//            data.age += delta;
//
//            Vector2f.add(data.velocity, (Vector2f) new Vector2f(data.acceleration).scale(delta), data.velocity);
//            Vector2f.add(data.location, (Vector2f) new Vector2f(data.velocity).scale(delta), data.location);
//
//            data.angularVelocity += data.angularAcceleration * delta;
//            data.angle += data.angularVelocity * delta;
//
//            data.alpha = Math.min(data.age / end, 1f);
//
//            float ratio = data.age / data.lifetime;
//            data.size.x = (data.sizeFinal.x - data.sizeInitial.x) * ratio + data.sizeInitial.x;
//            data.size.y = (data.sizeFinal.y - data.sizeInitial.y) * ratio + data.sizeInitial.y;
//        }
//    }
}
