package cmu.plugins.renderers;

import cmu.shaders.BaseParticleRenderer;
import cmu.shaders.particles.BaseParticle;
import cmu.shaders.ShaderProgram;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.ViewportAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import java.awt.*;
import java.io.IOException;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class ImplosionParticleRenderer extends BaseParticleRenderer {
    private static final String VERT_PATH = "data/shaders/implosion.vert";
    private static final String FRAG_PATH = "data/shaders/implosion.frag";

    private FloatBuffer projectionBuffer;

    private FloatBuffer modelViewBuffer;
    private FloatBuffer colourBuffer;

    @Override
    protected int[] initBuffers() {
        projectionBuffer = BufferUtils.createFloatBuffer(16);

        int verticesVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, verticesVBO);
        glBufferData(GL_ARRAY_BUFFER, VERTICES_BUFFER, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        int size = 2;
        glVertexAttribPointer(0, size, GL_FLOAT, false, size * Float.SIZE / Byte.SIZE, 0);

        // Create buffer for model view matrices
        int modelViewsVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, modelViewsVBO);
        int start = 1;
        for (int i = 0; i < 4; i++) {
            glVertexAttribPointer(start, 4, GL_FLOAT, false, 4 * 4 * 4, i * 4 * 4);
            glVertexAttribDivisor(start, 1);
            glEnableVertexAttribArray(start);
            start++;
        }

        // Create buffer for colours
        int coloursVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, coloursVBO);
        glVertexAttribPointer(5, 4, GL_FLOAT, false, 4 * Float.SIZE / Byte.SIZE, 0);
        glVertexAttribDivisor(5, 1);
        glEnableVertexAttribArray(5);

        return new int[] {
                verticesVBO,
                modelViewsVBO,
                coloursVBO
        };
    }

    @Override
    protected void populateUniforms(int glProgramID, CombatEngineLayers layer, ViewportAPI viewport) {
        projectionBuffer.clear();
        orthogonal(viewport.getVisibleWidth() / viewport.getViewMult(), viewport.getVisibleHeight() / viewport.getViewMult()).store(projectionBuffer);
        projectionBuffer.flip();
        int loc = glGetUniformLocation(glProgramID, "projection");
        glUniformMatrix4(loc, false, projectionBuffer);
    }

    @Override
    protected void updateBuffers(int[] buffers, CombatEngineLayers layer, ViewportAPI viewport) {
        int numElements = toDraw.size();

        // modelview
        modelViewBuffer = BufferUtils.createFloatBuffer(16 * numElements);
        //colour
        colourBuffer = BufferUtils.createFloatBuffer(4 * numElements);

        Matrix4f view = getViewMatrix(viewport);

        for (BaseParticle particle : toDraw) {
            Matrix4f modelView = particle.getModel(view);
            modelView.store(modelViewBuffer);

            Color color = particle.color;
            Vector4f colour = new Vector4f(
                    color.getRed() / 255f,
                    color.getGreen() / 255f,
                    color.getBlue() / 255f,
                    MathUtils.clamp(particle.age / particle.lifetime, 0f, 1f)
            );

            colour.store(colourBuffer);
        }

        modelViewBuffer.flip();
        colourBuffer.flip();

        glBindBuffer(GL_ARRAY_BUFFER, buffers[1]);
        glBufferData(GL_ARRAY_BUFFER, modelViewBuffer, GL_DYNAMIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, buffers[2]);
        glBufferData(GL_ARRAY_BUFFER, colourBuffer, GL_DYNAMIC_DRAW);
    }

    @Override
    protected void draw(CombatEngineLayers layer, ViewportAPI viewport) {
        glEnable(GL_BLEND);

        glBlendFunc(GL_SRC_ALPHA, GL_ONE);
        glBlendEquation(blendEquation);

        glDrawElementsInstanced(GL_TRIANGLES, INDICES_BUFFER, numElements);

        modelViewBuffer.clear();
        colourBuffer.clear();
    }

    @Override
    public void cleanup() {
        super.cleanup();

        if (modelViewBuffer != null) modelViewBuffer.clear();
        if (colourBuffer != null) colourBuffer.clear();
    }

    @Override
    protected ShaderProgram initShaderProgram() {
        ShaderProgram program = new ShaderProgram();

        String vert, frag;
        try {
            vert = Global.getSettings().loadText(VERT_PATH);
            frag = Global.getSettings().loadText(FRAG_PATH);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        program.createVertexShader(vert);
        program.createFragmentShader(frag);
        program.link();

        return program;
    }
}
