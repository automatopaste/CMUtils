package cmu.gui;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.input.InputEventType;
import com.sun.corba.se.spi.orbutil.fsm.Input;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.Iterator;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class Button implements Element {

    private final ButtonParams params;
    private final Text text;
    private final ButtonCallback callback;
    private boolean isArmed = false;
    private boolean isPressed = false;
    private boolean isClick = false;

    public Button(ButtonParams params, Text text, ButtonCallback callback) {
        this.params = params;
        this.text = text;
        this.callback = callback;
    }

    @Override
    public Vector2f update(float scale, Vector2f loc, List<InputEventAPI> events) {
        processInputEvents(events, loc);

        return new Vector2f(params.width, params.height);
    }

    @Override
    public Vector2f render(float scale, Vector2f loc, List<InputEventAPI> events) {
        float pad = 1f;
        if (scale > 1f) pad *= scale;

        float x1 = pad;
        float x2 = params.width - pad;
        float y1 = -params.height + pad;
        float y2 = -pad;

        if (isArmed) {
            glBegin(GL_LINE_LOOP);
            glVertex2f(x1 + 2f, y1 + 2f);
            glVertex2f(x1 + 2f, y2 - 2f);
            glVertex2f(x2 - 2f, y2 - 2f);
            glVertex2f(x2 - 2f, y1 + 2f);
            glEnd();
        }
        if (isPressed) {
            glBegin(GL_LINE_LOOP);
            glVertex2f(x1, y1);
            glVertex2f(x1, y2);
            glVertex2f(x2, y2);
            glVertex2f(x2, y1);
            glEnd();
        } else {
            glBegin(GL_LINE_LOOP);
            glVertex2f(x1 + 1f, y1 + 1f);
            glVertex2f(x1 + 1f, y2 - 1f);
            glVertex2f(x2 - 1f, y2 - 1f);
            glVertex2f(x2 - 1f, y1 + 1f);
            glEnd();
        }

        glEnable(GL_SCISSOR_TEST);
        glScissor((int) (loc.x + (3f * pad)), (int) (loc.y - params.height + pad), (int) (params.width - (4f * pad)), (int) (params.height - pad));

        glPushMatrix();
        if (text.getParams().align == LazyFont.TextAlignment.CENTER) {
            glTranslatef((int) (params.width * 0.5f), (int) (-params.height * 0.5f), 0f);
            text.render(scale, loc, events);
        } else {
            glTranslatef(Math.round(pad * 4f), Math.round(-pad * 3f), 0f);
            text.render(scale, loc, events);
        }
        glPopMatrix();

        glDisable(GL_SCISSOR_TEST);

        return new Vector2f(params.width, params.height);
    }

    public void processInputEvents(List<InputEventAPI> events, Vector2f loc) {
        float mx = Mouse.getX();
        float my = Mouse.getY();
        float x1 = loc.x;
        float y1 = loc.y;
        float x2 = loc.x + params.width;
        float y2 = loc.y - params.height;

        if (mx > x1 && mx < x2 && my < y1 && my > y2) {
            for (InputEventAPI event : events) {
                if (event.isMouseEvent()) {
                    if (event.isMouseDownEvent() && !event.isConsumed()) {
                        isClick = true;
                        event.consume();
                    } else if (event.isMouseUpEvent() && !event.isConsumed()) {
                        isClick = false;
                        isPressed = false;
                        callback.onClick();
                        event.consume();
                    }
                }
            }

            isArmed = !isClick;

            if (Mouse.isButtonDown(0)) {
                isPressed = true;
                isArmed = false;
            }
        } else {
            isArmed = false;
        }
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

    public interface ButtonCallback {
        void onClick();
    }
}
