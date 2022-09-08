package cmu.shaders;

import cmu.CMUtils;
import cmu.shaders.particles.BaseParticle;
import org.lwjgl.opengl.GL14;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BaseParticleRenderer extends BaseRenderPlugin {
    protected BaseParticle[] particles;
    protected final List<BaseParticle> toDraw;

    public BaseParticleRenderer() {
        int max = CMUtils.getMaxParticles();
        particles = new BaseParticle[max];
        toDraw = new ArrayList<>();

        blendEquation = GL14.GL_FUNC_ADD;
    }

    public void addParticle(BaseParticle baseParticle) {
        add(baseParticle);
    }

    public void addParticle(Vector2f loc, BaseParticle.ParticleParams params) {
        BaseParticle particle = new BaseParticle(loc, params);
        add(particle);
    }

    private void add(BaseParticle data) {
        for (int i = 0; i < particles.length; i++) {
            BaseParticle p = particles[i];
            if (p == null || p.age > p.lifetime) {
                particles[i] = data;
                return;
            }
        }
    }

    @Override
    public void advance(float amount) {
        toDraw.clear();

        for (BaseParticle particle : particles) {
            if (particle != null) {
                if (particle.age <= particle.lifetime) {
                    particle.advance(amount);

                    toDraw.add(particle);
                }
            }
        }
        numElements = toDraw.size();
    }

    @Override
    public void cleanup() {
        super.cleanup();

        VERTICES_BUFFER.clear();
        INDICES_BUFFER.clear();
    }

    public void updateMaxCount(int max) {
        particles = Arrays.copyOf(particles, max);
    }
}
