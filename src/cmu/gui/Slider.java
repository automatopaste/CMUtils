package cmu.gui;

import com.fs.starfarer.api.input.InputEventAPI;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class Slider implements Element {

    private final SliderParams params;
    private float pos = 0.5f;
    final float sPad = 4f;

    public Slider(SliderParams params) {
        this.params = params;
    }

    @Override
    public Vector2f update(float scale, Vector2f loc, List<InputEventAPI> events) {
        return new Vector2f(params.x, params.y);
    }

    @Override
    public Vector2f render(float scale, Vector2f loc, List<InputEventAPI> events) {
        float pad = 8f;
        if (scale > 1f) pad *= scale;

        float x1 = pad;
        float x2 = params.x - pad;
        float y1 = -params.y + pad;
        float y2 = -pad;

        glBegin(GL_LINE_LOOP);
        glVertex2f(x1, y1);
        glVertex2f(x1, y2);
        glVertex2f(x2, y2);
        glVertex2f(x2, y1);
        glEnd();

        float tPad = sPad * scale;
        float p = pos * params.x;
        float px1 = p - tPad;
        float px2 = p + tPad;

        glBegin(GL_TRIANGLE_STRIP);
        glVertex2f(px1, 0f);
        glVertex2f(px1, -params.y);
        glVertex2f(px2, 0f);
        glVertex2f(px2, -params.y);
        glEnd();

        return new Vector2f(params.x, params.y);
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
