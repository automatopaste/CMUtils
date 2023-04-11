package cmu.gui;

import com.fs.starfarer.api.input.InputEventAPI;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;

public class Text implements Element {

    private Execute<String> execute;
    private final LazyFont.DrawableString draw;
    private final TextParams params;

    public Text(Execute<String> execute, LazyFont.DrawableString draw, TextParams params) {
        this.execute = execute;
        this.draw = draw;
        this.params = params;
    }

    @Override
    public Vector2f update(float scale, Vector2f loc, List<InputEventAPI> events) {
        draw.setText(execute.get());
        draw.setAlignment(params.align);
        draw.setMaxHeight(params.maxHeight);
        draw.setMaxWidth(params.maxWidth);

        return new Vector2f(draw.getWidth(), draw.getHeight());
    }

    @Override
    public Vector2f render(float scale, Vector2f loc, List<InputEventAPI> events) {
        draw.setText(execute.get());
        draw.setBaseColor(params.color);
        draw.setAlignment(params.align);

        switch (params.align) {
            case LEFT:
            case RIGHT:
                float x = loc.x - (int) loc.x;
                float y = loc.y - (int) loc.y;
                draw.draw(-x, -y);
                break;
            case CENTER:
                float width = draw.getWidth();
                float height = draw.getHeight();
                draw.draw(Math.round(-width * 0.5f), Math.round(height * 0.5f));
                break;
        }

        return new Vector2f(draw.getWidth(), draw.getHeight());
    }

    public static final class TextParams {
        public Color color = Color.WHITE;
        public LazyFont.TextAlignment align = LazyFont.TextAlignment.LEFT;
        public float maxWidth = Float.MAX_VALUE;
        public float maxHeight = Float.MAX_VALUE;
    }

    @Override
    public float getWidth() {
        return draw.getWidth();
    }

    @Override
    public float getHeight() {
        return draw.getHeight();
    }

    public TextParams getParams() {
        return params;
    }

    public void setExecute(Execute<String> execute) {
        this.execute = execute;
    }

    public void setColor(Color c) {
        params.color = c;
    }
}
