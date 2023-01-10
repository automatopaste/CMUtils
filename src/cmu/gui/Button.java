package cmu.gui;

import com.fs.starfarer.api.input.InputEventAPI;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class Button implements Element {

    private final ButtonParams params;
    private final Text text;
    private boolean isArmed = false;
    private boolean isPressed = false;

    public Button(ButtonParams params, Text text) {
        this.params = params;
        this.text = text;
    }

    @Override
    public Vector2f render(float scale, Vector2f loc) {
        float pad = 1f;
        if (scale > 1f) pad *= scale;

        float x1 = pad;
        float x2 = params.width - pad;
        float y1 = -params.height + pad;
        float y2 = -pad;

        glBegin(GL_LINE_LOOP);
        glVertex2f(x1, y1);
        glVertex2f(x1, y2);
        glVertex2f(x2, y2);
        glVertex2f(x2, y1);
        glEnd();

        if (text.getParams().align == LazyFont.TextAlignment.CENTER) {
            glPushMatrix();
            glTranslatef((int) (params.width * 0.5f), (int) (-params.height * 0.5f), 0f);

            text.render(scale, loc);

            glPopMatrix();
        } else {
            text.render(scale, loc);
        }

        return new Vector2f(params.width, params.height);
    }

    @Override
    public void processInputEvents(List<InputEventAPI> events) {

    }

    @Override
    public float getWidth() {
        return params.width;
    }

    @Override
    public float getHeight() {
        return params.height;
    }

    public static class ButtonParams {
        public float width = 80f;
        public float height = 40f;
        public String text = "TEXT";
        public Color color = Color.WHITE;
    }
}
