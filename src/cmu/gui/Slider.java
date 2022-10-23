package cmu.gui;

import com.fs.starfarer.api.input.InputEventAPI;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class Slider implements Element {

    private final SliderParams params;
    private float pos = 0.5f;

    public Slider(SliderParams params) {
        this.params = params;
    }

    @Override
    public void render() {
        float width = pos * params.x;
        final float pad = 2f;

        glBegin(GL_TRIANGLE_STRIP);
        glVertex2f(0f, 0f);
        glVertex2f(0f, params.y);
        glVertex2f(width - pad, params.y);
        glVertex2f(width - pad, 0f);
        glEnd();

        glLineWidth(1f);
        glBegin(GL_LINES);
        glVertex2f(width, 0f);
        glVertex2f(width, params.y);
        glEnd();

        glBegin(GL_TRIANGLE_STRIP);
        glVertex2f(width + pad, 0f);
        glVertex2f(width + pad, params.y);
        glVertex2f(params.x, params.y);
        glVertex2f(params.x, 0f);
        glEnd();
    }

    @Override
    public void processInputEvents() {

    }

    @Override
    public float getWidth() {
        return 0;
    }

    @Override
    public float getHeight() {
        return 0;
    }

    public static final class SliderParams {
        public float x = 100f;
        public float y = 15f;
    }
}
