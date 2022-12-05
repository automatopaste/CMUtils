package cmu.shaders.particles;

import org.lwjgl.util.vector.Vector2f;

/**
 * Scripting mechanism to allow fully customisable particle behaviour
 * Could develop this into a GPU-side compute shader for extreme swag (skill issue)
 */
public abstract class ComputeFunction {
    public abstract void advance(float delta, BaseParticle data);

    public static class DefaultComputeFunction extends ComputeFunction {
        @Override
        public void advance(float delta, BaseParticle data) {
            data.age += delta;

            Vector2f.add(data.velocity, (Vector2f) new Vector2f(data.acceleration).scale(delta), data.velocity);
            Vector2f.add(data.location, (Vector2f) new Vector2f(data.velocity).scale(delta), data.location);

            data.angularVelocity += data.angularAcceleration * delta;
            data.angle += data.angularVelocity * delta;

            data.alpha = data.age / data.lifetime;

            float ratio = data.age / data.lifetime;
            data.size.x = (data.sizeFinal.x - data.sizeInitial.x) * ratio + data.sizeInitial.x;
            data.size.y = (data.sizeFinal.y - data.sizeInitial.y) * ratio + data.sizeInitial.y;
        }
    }

    public static class SmoothAlphaComputeFunction extends DefaultComputeFunction {

        @Override
        public void advance(float delta, BaseParticle data) {
            super.advance(delta, data);

            float f = (2f * data.alpha - 1);
            data.alpha = 1f - (f * f * f * f);
        }
    }

    public static class SquaredFalloffAlphaFunction extends ComputeFunction {
        @Override
        public void advance(float delta, BaseParticle data) {
            data.age += delta;

            float a = data.age / data.lifetime;
            data.alpha = 1f - (a * a);

            Vector2f.add(data.velocity, (Vector2f) new Vector2f(data.acceleration).scale(delta), data.velocity);
            Vector2f.add(data.location, (Vector2f) new Vector2f(data.velocity).scale(delta), data.location);

            data.angularVelocity += data.angularAcceleration * delta;
            data.angle += data.angularVelocity * delta;

            data.alpha = data.age / data.lifetime;

            float ratio = data.age / data.lifetime;
            data.size.x = (data.sizeFinal.x - data.sizeInitial.x) * ratio + data.sizeInitial.x;
            data.size.y = (data.sizeFinal.y - data.sizeInitial.y) * ratio + data.sizeInitial.y;
        }
    }

    public static class ConstantAlphaFunction extends ComputeFunction {
        protected final float alpha;

        public ConstantAlphaFunction(float alpha) {
            this.alpha = alpha;
        }

        @Override
        public void advance(float delta, BaseParticle data) {
            data.age += delta;

            data.alpha = alpha;

            Vector2f.add(data.velocity, (Vector2f) new Vector2f(data.acceleration).scale(delta), data.velocity);
            Vector2f.add(data.location, (Vector2f) new Vector2f(data.velocity).scale(delta), data.location);

            data.angularVelocity += data.angularAcceleration * delta;
            data.angle += data.angularVelocity * delta;

            data.alpha = data.age / data.lifetime;

            float ratio = data.age / data.lifetime;
            data.size.x = (data.sizeFinal.x - data.sizeInitial.x) * ratio + data.sizeInitial.x;
            data.size.y = (data.sizeFinal.y - data.sizeInitial.y) * ratio + data.sizeInitial.y;
        }
    }
}