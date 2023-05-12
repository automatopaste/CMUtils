package cmu.plugins.renderers;

import cmu.shaders.BaseParticleRenderer;
import cmu.shaders.ShaderProgram;
import cmu.shaders.particles.BaseParticle;
import cmu.shaders.particles.PolygonParticle;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.ViewportAPI;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.awt.*;
import java.io.IOException;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.glBlendEquation;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glUniformMatrix4;
import static org.lwjgl.opengl.GL31.glDrawArraysInstanced;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class PolygonParticleRenderer extends BaseParticleRenderer {

    private static final String VERT_PATH = "data/shaders/polygon.vert";
    private static final String GEOM_PATH = "data/shaders/polygon.geom";
    private static final String FRAG_PATH = "data/shaders/polygon.frag";

    private FloatBuffer projectionBuffer;

    private FloatBuffer modelViewBuffer;
    private FloatBuffer edgeColorBuffer;
    private FloatBuffer fillColorBuffer;
    private FloatBuffer dataBuffer;
    private static final int DATA_BUFFER_SIZE = 2;

    @Override
    protected int[] initBuffers() {
        projectionBuffer = BufferUtils.createFloatBuffer(16);

        // Create buffer for model view matrices
        int modelViewsVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, modelViewsVBO);
        int start = 0;
        for (int i = 0; i < 4; i++) {
            glVertexAttribPointer(start, 4, GL_FLOAT, false, 4 * 4 * 4, i * 4 * 4);
            glVertexAttribDivisor(start, 1);
            glEnableVertexAttribArray(start);
            start++;
        }

        // Create buffer for colours
        int edgeColorsVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, edgeColorsVBO);
        glVertexAttribPointer(4, 4, GL_FLOAT, false, 4 * Float.SIZE / Byte.SIZE, 0);
        glVertexAttribDivisor(4, 1);
        glEnableVertexAttribArray(4);

        // Create buffer for colours
        int fillColorsVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, fillColorsVBO);
        glVertexAttribPointer(5, 4, GL_FLOAT, false, 4 * Float.SIZE / Byte.SIZE, 0);
        glVertexAttribDivisor(5, 1);
        glEnableVertexAttribArray(5);

        int dataVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, dataVBO);
        glVertexAttribPointer(6, DATA_BUFFER_SIZE, GL_FLOAT, false, DATA_BUFFER_SIZE * Float.SIZE / Byte.SIZE, 0);
        glVertexAttribDivisor(6, 1);
        glEnableVertexAttribArray(6);

        return new int[] {
                modelViewsVBO,
                edgeColorsVBO,
                fillColorsVBO,
                dataVBO
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
        // colour
        edgeColorBuffer = BufferUtils.createFloatBuffer(4 * numElements);
        fillColorBuffer = BufferUtils.createFloatBuffer(4 * numElements);
        // data
        dataBuffer = BufferUtils.createFloatBuffer(DATA_BUFFER_SIZE * numElements);

        Matrix4f view = getViewMatrix(viewport);

        for (BaseParticle particle : toDraw) {
            PolygonParticle poly = (PolygonParticle) particle;
            Matrix4f modelView = particle.getModel(view);
            modelView.store(modelViewBuffer);

            Color c1 = poly.edgeColor;
            Vector4f v1 = new Vector4f(
                    c1.getRed() / 255f,
                    c1.getGreen() / 255f,
                    c1.getBlue() / 255f,
                    c1.getAlpha() / 255f
            );
            v1.w *= poly.alphaMult * poly.alpha;
            v1.store(edgeColorBuffer);

            Color c2 = particle.color;
            Vector4f v2 = new Vector4f(
                    c2.getRed() / 255f,
                    c2.getGreen() / 255f,
                    c2.getBlue() / 255f,
                    c2.getAlpha() / 255f
            );
            v2.w *= poly.alphaMult * poly.alpha;
            v2.store(fillColorBuffer);

            Vector2f data = new Vector2f(poly.poly, poly.edgeWidth);
            data.store(dataBuffer);
        }

        modelViewBuffer.flip();
        edgeColorBuffer.flip();
        fillColorBuffer.flip();
        dataBuffer.flip();

        glBindBuffer(GL_ARRAY_BUFFER, buffers[0]);
        glBufferData(GL_ARRAY_BUFFER, modelViewBuffer, GL_DYNAMIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, buffers[1]);
        glBufferData(GL_ARRAY_BUFFER, edgeColorBuffer, GL_DYNAMIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, buffers[2]);
        glBufferData(GL_ARRAY_BUFFER, fillColorBuffer, GL_DYNAMIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, buffers[3]);
        glBufferData(GL_ARRAY_BUFFER, dataBuffer, GL_DYNAMIC_DRAW);
    }

    @Override
    protected void draw(CombatEngineLayers layer, ViewportAPI viewport) {
        glEnable(GL_BLEND);

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glBlendEquation(blendEquation);

        glDrawArraysInstanced(GL_POINTS, 0, numElements, numElements);

        modelViewBuffer.clear();
        edgeColorBuffer.clear();
        fillColorBuffer.clear();
        dataBuffer.clear();
    }

    @Override
    public void addParticle(Vector2f loc, BaseParticle.ParticleParams params) {
        PolygonParticle polygonParticle = new PolygonParticle(loc, (PolygonParticle.PolygonParams) params);
        addParticle(polygonParticle);
    }

    @Override
    protected ShaderProgram initShaderProgram() {
        ShaderProgram program = new ShaderProgram();

        String vert, geom, frag;
        try {
            vert = Global.getSettings().loadText(VERT_PATH);
            geom = Global.getSettings().loadText(GEOM_PATH);
            frag = Global.getSettings().loadText(FRAG_PATH);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        program.createVertexShader(vert);
        program.createGeometryShader(geom);
        program.createFragmentShader(frag);
        program.link();

        return program;
    }
}
