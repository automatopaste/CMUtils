package cmu.plugins.renderers;

import cmu.shaders.BaseRenderPlugin;
import cmu.shaders.ShaderProgram;
import cmu.shaders.particles.SegmentedPath;
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
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class SegmentRenderer extends BaseRenderPlugin {
    private static final String VERT_PATH = "data/shaders/lightning.vert";
    private static final String FRAG_PATH = "data/shaders/lightning.frag";

    private FloatBuffer projectionBuffer;

    private FloatBuffer verticesBuffer;
    private FloatBuffer modelViewBuffer;

    private final List<SegmentedPath> toDraw;

    public SegmentRenderer() {
        toDraw = new ArrayList<>();
    }

    public void addToFrame(SegmentedPath segmentedPath) {
        toDraw.add(segmentedPath);
    }

    public void clear() {
        toDraw.clear();
    }

    @Override
    protected int[] initBuffers() {
        projectionBuffer = BufferUtils.createFloatBuffer(16);

        int index;

        int verticesVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, verticesVBO);
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
        for (SegmentedPath path : toDraw) {

            numMatrices += (path.numSegments + 1) * 2;
            Matrix4f[] transforms = path.getVertexTransforms(view);
            pathVertexTransforms.add(transforms);
        }
        this.numElements = numMatrices;

        // vertices
        verticesBuffer = BufferUtils.createFloatBuffer(numMatrices * 2);

        // model view
        modelViewBuffer = BufferUtils.createFloatBuffer(numMatrices * 16);

        for (int j = 0; j < toDraw.size(); j++) {
            final float[] vertices = new float[] {
                    0f, 0f,
                    0f, 1f,
                    1f, 0f,
                    1f, 1f
            };

            Matrix4f[] transforms = pathVertexTransforms.get(j);

            int index = 0;
            for (Matrix4f m : transforms) {
                m.store(modelViewBuffer);

                index++;
                if (index == 4) index = 0;

                int index2 = index * 2;
                new Vector2f(vertices[index2], vertices[index2 + 1]).store(verticesBuffer);
            }
        }

        verticesBuffer.flip();
        modelViewBuffer.flip();

        glBindBuffer(GL_ARRAY_BUFFER, buffers[0]);
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_DYNAMIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, buffers[1]);
        glBufferData(GL_ARRAY_BUFFER, modelViewBuffer, GL_DYNAMIC_DRAW);
    }

    @Override
    protected void draw(CombatEngineLayers layer, ViewportAPI viewport) {
        glEnable(GL_BLEND);

        glBlendFunc(GL_SRC_ALPHA, GL_ONE);
        glBlendEquation(blendEquation);

        int first = 0;
        for (SegmentedPath path : toDraw) {
            int num = (path.numSegments + 1) * 2;
            glDrawArrays(GL_TRIANGLE_STRIP, first, num);
            first += num;
        }

        verticesBuffer.clear();
        modelViewBuffer.clear();
    }

    @Override
    public void cleanup() {
        super.cleanup();

        if (verticesBuffer != null) verticesBuffer.clear();
        if (modelViewBuffer != null) modelViewBuffer.clear();
        toDraw.clear();
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
