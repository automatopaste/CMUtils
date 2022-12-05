package cmu.plugins.renderers;

import cmu.shaders.BaseParticleRenderer;
import cmu.shaders.ShaderProgram;
import cmu.shaders.particles.BaseParticle;
import cmu.shaders.particles.PolygonParticle;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.ViewportAPI;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector2f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glUniformMatrix4;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

/**
 * work in progress
 */
public class PolygonParticleRenderer extends BaseParticleRenderer {
    private FloatBuffer projectionBuffer;

    private FloatBuffer vertexBuffer;
    private FloatBuffer modelViewBuffer;
    private FloatBuffer colourBuffer;
    private IntBuffer indexBuffer;

    @Override
    protected int[] initBuffers() {
        projectionBuffer = BufferUtils.createFloatBuffer(16);

        int verticesVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, verticesVBO);
        glEnableVertexAttribArray(0);
        int size = 2;
        glVertexAttribPointer(0, size, GL_FLOAT, false, size * Float.SIZE / Byte.SIZE, 0);

        // Create buffer for model view matrices
        int modelViewsVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, modelViewsVBO);
        int start = 1;
        for (int i = 0; i < 4; i++) {
            glVertexAttribPointer(start, 4, GL_FLOAT, false, 4 * 4 * 4, i * 4 * 4);
            glVertexAttribDivisor(start, 0);
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
        int numIndices = 0;
        int numVertices = 0;

        for (BaseParticle p : toDraw) {
            PolygonParticle polygon = (PolygonParticle) p;

            numIndices += polygon.poly * 3;
            numVertices += polygon.poly + 1;
        }

        vertexBuffer = BufferUtils.createFloatBuffer(numVertices);
        indexBuffer = BufferUtils.createIntBuffer(numIndices);

        for (BaseParticle p : toDraw) {
            PolygonParticle polygon = (PolygonParticle) p;

            for (int i = 0; i < polygon.poly; i++) {

            }
        }
    }

    @Override
    protected void draw(CombatEngineLayers layer, ViewportAPI viewport) {

    }

    @Override
    protected ShaderProgram initShaderProgram() {
        return null;
    }
}
