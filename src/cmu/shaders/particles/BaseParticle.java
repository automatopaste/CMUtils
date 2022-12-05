package cmu.shaders.particles;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.awt.*;

public class BaseParticle {

    public float age;
    public final float lifetime;
    public final Vector2f location;
    public final Vector2f velocity;
    public final Vector2f acceleration;
    public float angle;
    public float angularVelocity;
    public final float angularAcceleration;
    public final Vector2f sizeInitial;
    public final Vector2f sizeFinal;
    public final Vector2f size;
    public final Color color;
    public float alpha;

    private final ComputeFunction computeFunction;

    public BaseParticle(
            Vector2f location, ParticleParams params
    ) {
        age = 0f;
        this.lifetime = params.lifetime;
        this.location = location;
        this.velocity = params.vel;
        this.acceleration = params.acc;
        this.angle = params.angle;
        this.angularVelocity = params.angVel;
        this.angularAcceleration = params.angVel;
        this.sizeInitial = new Vector2f(params.sizeInit);
        this.sizeFinal = new Vector2f(params.sizeFinal);
        this.size = new Vector2f(sizeInitial);
        this.color = params.color;
        this.alpha = 0f;
        this.computeFunction = params.computeFunction;
    }

    public void advance(float delta) {
        computeFunction.advance(delta, this);
    }

    public Matrix4f getModel(Matrix4f in) {
        Matrix4f matrix = new Matrix4f(in);

        matrix.translate(new Vector3f(location.x, location.y, 0f));
        matrix.rotate((float) Math.toRadians(angle - 90f), new Vector3f(0f, 0f, 1f));

        Vector2f offset = new Vector2f(size.x * 0.5f, size.y * 0.5f);

        matrix.translate(new Vector3f(-offset.x, -offset.y, 0f));
        matrix.scale(new Vector3f(size.x, size.y, 1f));

        return matrix;
    }

    public static class ParticleParams {
        public Vector2f vel = new Vector2f(0f, 0f);
        public Vector2f acc = new Vector2f(0f, 0f);
        public float angle = 0f;
        public float angVel = 0f;
        public float angAcc = 0f;
        public Vector2f sizeInit = new Vector2f(10f, 10f);
        public Vector2f sizeFinal = new Vector2f(10f, 10f);
        public Color color = Color.WHITE;
        public float lifetime = 1f;
        public ComputeFunction computeFunction = new ComputeFunction.DefaultComputeFunction();
    }
}
