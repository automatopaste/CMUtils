package cmu.plugins.renderers;

import cmu.shaders.BaseParticleRenderer;
import cmu.shaders.ShaderProgram;
import cmu.shaders.particles.BaseParticle;
import cmu.shaders.particles.SegmentedParticle;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.ViewportAPI;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.glBlendEquation;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glUniformMatrix4;
import static org.lwjgl.opengl.GL31.glDrawArraysInstanced;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class SegmentRenderer extends BaseParticleRenderer {
    private static final String VERT_PATH = "data/shaders/lightning.vert";
    private static final String FRAG_PATH = "data/shaders/lightning.frag";

    private FloatBuffer projectionBuffer;

    private FloatBuffer modelViewBuffer;

    @Override
    protected int[] initBuffers() {
        projectionBuffer = BufferUtils.createFloatBuffer(16);

        int index;

        // Vertices buffer
        final float[] v = new float[] {
                0f, 0f,
                1f, 0f,
                1f, 1f,
                0f, 0f,
                0f, 1f,
                1f, 1f
        };
        FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(v.length);
        verticesBuffer.put(v).flip();

        int verticesVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, verticesVBO);
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        final int size = 2;
        glVertexAttribPointer(0, size, GL_FLOAT, false, size * Float.SIZE / Byte.SIZE, 0);

        // Create buffer for model view matrices
        int modelViewVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, modelViewVBO);
        index = 1;
        for (int i = 0; i < 4; i++) {
            glVertexAttribPointer(index, 4, GL_FLOAT, false, 4 * 4 * 4, i * 4 * 4);
            glVertexAttribDivisor(index, 0);
            glEnableVertexAttribArray(index);
            index++;
        }

        return new int[] {
                verticesVBO,
                modelViewVBO
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
        Matrix4f view = getViewMatrix(Global.getCombatEngine().getViewport());
        List<Matrix4f[]> pathVertexTransforms = new ArrayList<>();
        int numMatrices = 0;
        int numElements = 0;
        for (BaseParticle particle : toDraw) {
            SegmentedParticle segmentedParticle = (SegmentedParticle) particle;

            numElements += segmentedParticle.getSegments().size();

            Matrix4f[] transforms = segmentedParticle.getVertexTransforms(view);
            numMatrices += (transforms.length * 2) - 4;
            pathVertexTransforms.add(transforms);
        }
        this.numElements = numElements;

        // model view
        modelViewBuffer = BufferUtils.createFloatBuffer(numMatrices * 16);

        for (int j = 0; j < toDraw.size(); j++) {
            Matrix4f[] transforms = pathVertexTransforms.get(j);

            for (int i = 0; i < transforms.length - 2; i += 2) {
                transforms[i].store(modelViewBuffer);
                transforms[i + 1].store(modelViewBuffer);
                transforms[i + 2].store(modelViewBuffer);
                transforms[i + 3].store(modelViewBuffer);
            }
        }

        modelViewBuffer.flip();

        glBindBuffer(GL_ARRAY_BUFFER, buffers[1]);
        glBufferData(GL_ARRAY_BUFFER, modelViewBuffer, GL_DYNAMIC_DRAW);
    }

    @Override
    protected void draw(CombatEngineLayers layer, ViewportAPI viewport) {
        glEnable(GL_BLEND);

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glBlendEquation(blendEquation);

        //glDrawElementsInstanced(GL_TRIANGLES, INDICES_BUFFER, numElements);
        glDrawArraysInstanced(GL_TRIANGLES, 0, 6, numElements);

        modelViewBuffer.clear();
    }

    @Override
    public void cleanup() {
        super.cleanup();

        if (modelViewBuffer != null) modelViewBuffer.clear();
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
