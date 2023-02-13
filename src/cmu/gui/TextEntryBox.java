package cmu.gui;

import com.fs.starfarer.api.input.InputEventAPI;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.security.Key;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.lwjgl.opengl.GL11.*;

public class TextEntryBox implements Element {

    private final TextEntryBoxParams params;
    private StringBuilder string = new StringBuilder();
    private final Text text;
    private boolean selected = false;
    private boolean backOld = true;

    public TextEntryBox(TextEntryBoxParams params, LazyFont.DrawableString draw, Text.TextParams textParams) {
        this.params = params;
        this.text = new Text(new Execute<String>() {
            @Override
            public String get() {
                return string + "_";
            }
        }, draw, textParams);
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

        if (selected) {
            glBegin(GL_LINE_LOOP);
            glVertex2f(x1 + 1f, y1 + 1f);
            glVertex2f(x1 + 1f, y2 - 1f);
            glVertex2f(x2 - 1f, y2 - 1f);
            glVertex2f(x2 - 1f, y1 + 1f);
            glEnd();
            glBegin(GL_LINE_LOOP);
            glVertex2f(x1 + 2f, y1 + 2f);
            glVertex2f(x1 + 2f, y2 - 2f);
            glVertex2f(x2 - 2f, y2 - 2f);
            glVertex2f(x2 - 2f, y1 + 2f);
            glEnd();
        } else {
            glBegin(GL_LINE_LOOP);
            glVertex2f(x1 + 1f, y1 + 1f);
            glVertex2f(x1 + 1f, y2 - 1f);
            glVertex2f(x2 - 1f, y2 - 1f);
            glVertex2f(x2 - 1f, y1 + 1f);
            glEnd();
        }

        glPushMatrix();
        glTranslatef(5f, -4f, 0f);

        if (text.getParams().align == LazyFont.TextAlignment.CENTER) {
            glPushMatrix();
            glTranslatef((int) (params.width * 0.5f), (int) (-params.height * 0.5f), 0f);
        }

        text.render(scale, loc, events);

        glPopMatrix();

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
                    if (event.isMouseDownEvent()) {
                        selected = true;
                        event.consume();
                    }
                }
            }
        } else {
            for (InputEventAPI event : events) {
                if (event.isMouseEvent()) {
                    if (event.isMouseDownEvent() && selected) {
                        selected = false;
                    }
                }
            }
        }

        if (selected) {
            Pattern letter = Pattern.compile("[a-zA-z]");
            Pattern digit = Pattern.compile("\\d");
            Pattern special = Pattern.compile ("[ :.\\[\\]~-]");

            if (Keyboard.isKeyDown(Keyboard.KEY_BACK) && backOld) {
                if (string.length() > 0) string.deleteCharAt(string.length() - 1);
                backOld = false;
            } else {
                if (string.length() < params.maxChars) {
                    for (InputEventAPI event : events) {
                        if (event.isKeyboardEvent() && !event.isConsumed()) {
                            String c = event.getEventChar() + "";

                            Matcher hasLetter = letter.matcher(c);
                            Matcher hasDigit = digit.matcher(c);
                            Matcher hasSpecial = special.matcher(c);

                            if (hasLetter.find() || hasDigit.find() || hasSpecial.find()) {
                                string.append(c);
                                event.consume();
                            }
                        }
                    }
                }

                if (!Keyboard.isKeyDown(Keyboard.KEY_BACK)) backOld = true;
            }
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

    public String getString() {
        return string.toString();
    }

    public void setString(String s) {
        string = new StringBuilder(s);
    }

    public static final class TextEntryBoxParams {
        public Color color = Color.WHITE;
        public float width = 80f;
        public float height = 40f;
        public String text = "TEXT";
        public int maxChars = 255;
    }
}
