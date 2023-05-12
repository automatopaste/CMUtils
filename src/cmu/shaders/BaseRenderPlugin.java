package cmu.shaders;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.EnumSet;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.GL_FUNC_ADD;
import static org.lwjgl.opengl.GL14.glBlendEquation;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;

public abstract class BaseRenderPlugin extends BaseCombatLayeredRenderingPlugin {
    private int vao;
    private ShaderProgram program;
    protected int blendEquation;
    protected int numElements;
    protected CombatEngineLayers layer;

    private int[] bufferVBOs;

    // Used for generic rendering of quads (two triangles using indexed vertices)
    protected static final FloatBuffer VERTICES_BUFFER;
    protected static final IntBuffer INDICES_BUFFER;
    static {
        // Create buffer for vertices
        final float[] vertices = new float[] {
                0f, 0f,
                0f, 1f,
                1f, 0f,
                1f, 1f
        };
        VERTICES_BUFFER = BufferUtils.createFloatBuffer(vertices.length);
        VERTICES_BUFFER.put(vertices).flip();

        final int[] indices = new int[] {
                0, 2, 3,
                0, 1, 3
        };
        INDICES_BUFFER = BufferUtils.createIntBuffer(indices.length);
        INDICES_BUFFER.put(indices).flip();
    }

    public BaseRenderPlugin() {
        layer = CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER;
    }

    protected abstract int[] initBuffers();

    protected abstract void populateUniforms(int glProgramID, CombatEngineLayers layer, ViewportAPI viewport);

    protected abstract void updateBuffers(int[] buffers, CombatEngineLayers layer, ViewportAPI viewport);

    /**
     * Define GL funcs etc.
     */
    protected abstract void draw(CombatEngineLayers layer, ViewportAPI viewport);

    protected abstract ShaderProgram initShaderProgram();

    @Override
    public void init(CombatEntityAPI entity) {
        program = initShaderProgram();

        // Create the VAO and bind to it
        vao = glGenVertexArrays();

        glBindVertexArray(vao);
        bufferVBOs = initBuffers();
        glBindVertexArray(0);
    }

    @Override
    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        if (layer != this.layer) return;

        glBindVertexArray(vao);
        program.bind();

        //uniforms
        populateUniforms(program.getProgramID(), layer, viewport);

        updateBuffers(bufferVBOs, layer, viewport);

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        draw(layer, viewport);

        program.unbind();
        glBindVertexArray(0);

        glBlendEquation(GL_FUNC_ADD);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void cleanup() {
        for (int buffer : bufferVBOs) {
            glDeleteBuffers(buffer);
        }

        glDeleteVertexArrays(vao);

        program.dispose();
    }

    public static Matrix4f getViewMatrix(ViewportAPI viewport) {
        float viewMult = viewport.getViewMult();
        Matrix4f matrix = new Matrix4f();

        matrix.setIdentity();

        //view
        matrix.translate(new Vector3f(viewport.getVisibleWidth() / (2f * viewMult), viewport.getVisibleHeight() / (2f * viewMult), 0f));
        matrix.scale(new Vector3f(1f / viewport.getViewMult(), 1f / viewport.getViewMult(), 1f));
        matrix.translate(new Vector3f(-viewport.getCenter().x, -viewport.getCenter().y, 0f));

        return matrix;
    }

    public static Matrix4f orthogonal(float right, float top) {
        Matrix4f matrix = new Matrix4f();

        float left = 0f;
        float bottom = 0f;
        float zNear = -100f;
        float zFar = 100f;

        matrix.m00 = 2f / (right - left);

        matrix.m11 = 2f / (top - bottom);
        matrix.m22 = 2f / (zNear - zFar);

        matrix.m30 = -(right + left) / (right - left);
        matrix.m31 = -(top + bottom) / (top - bottom);
        matrix.m32 = -(zFar + zNear) / (zFar - zNear);

        matrix.m33 = 1f;

        return matrix;
    }

    @Override
    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return EnumSet.of(layer);
    }

    public void setLayer(CombatEngineLayers layer) {
        this.layer = layer;
    }

    public void setBlendEquation(int blendEquation) {
        this.blendEquation = blendEquation;
    }

    @Override
    public float getRenderRadius() {
        return Float.MAX_VALUE;
    }
}
