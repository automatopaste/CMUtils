package cmu.gui;

import org.lazywizard.lazylib.ui.LazyFont;

import java.awt.*;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glEnable;

public class Text implements Element {

    private final StringExecute execute;
    private final LazyFont.DrawableString draw;
    private final TextParams params;

    public Text(StringExecute execute, LazyFont.DrawableString draw, TextParams params) {
        this.execute = execute;
        this.draw = draw;
        this.params = params;
    }

    @Override
    public void render() {
        draw.setFontSize(12f);
        draw.setText(execute.get());
        draw.setBaseColor(params.color);
        draw.draw(0f, 0f);
    }

    @Override
    public void processInputEvents() {

    }

    public static final class TextParams {
        public float size;
        public Color color;
    }

    @Override
    public float getWidth() {
        return draw.getWidth();
    }

    @Override
    public float getHeight() {
        return draw.getHeight();
    }
}
