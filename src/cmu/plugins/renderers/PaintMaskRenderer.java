package cmu.plugins.renderers;

import cmu.ModPlugin;
import cmu.shaders.BaseRenderPlugin;
import cmu.shaders.ShaderProgram;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.ViewportAPI;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class PaintMaskRenderer extends BaseRenderPlugin {

    private static final String VERT_PATH = "data/shaders/implosion.vert";
    private static final String FRAG_PATH = "data/shaders/implosion.frag";

    private final int textureID;

    private FloatBuffer projectionBuffer;

    private FloatBuffer modelViewBuffer;
    private FloatBuffer colourBuffer;

    public PaintMaskRenderer() {
        textureID = ModPlugin.PARAGON.getTextureId();
    }

    @Override
    protected int[] initBuffers() {
        projectionBuffer = BufferUtils.createFloatBuffer(16);

        int verticesVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, verticesVBO);
        glBufferData(GL_ARRAY_BUFFER, VERTICES_BUFFER, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        final int size = 2;
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

        int imageLocation = glGetUniformLocation(glProgramID, "image0");
        glUniform1i(imageLocation, 0);
    }

    @Override
    protected void updateBuffers(int[] buffers, CombatEngineLayers layer, ViewportAPI viewport) {

    }

    @Override
    protected void draw(CombatEngineLayers layer, ViewportAPI viewport) {

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
