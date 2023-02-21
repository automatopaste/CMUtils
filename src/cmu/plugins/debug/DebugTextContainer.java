package cmu.plugins.debug;

import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class DebugTextContainer implements BaseDebugContainer {

    private final String text;
    private final float lifetime;
    private float timer;

    public DebugTextContainer(String text) {
        this.text = text;
        lifetime = 1f;
        timer = lifetime;
    }

    @Override
    public void advance(float amount) {
        timer -= amount;
    }

    @Override
    public float render(Vector2f loc, LazyFont.DrawableString toDraw, Color color, float width) {
        Color c = new Color(
                Math.min((int) (color.getRed() * getAgeRatio()), 255),
                Math.min((int) (color.getGreen() * getAgeRatio()), 255),
                Math.min((int) (color.getBlue() * getAgeRatio()), 255),
                Math.min((int) ((timer / lifetime) * 255f), 255)
        );
        toDraw.setBaseColor(c);
        toDraw.setText(text);
        toDraw.setMaxWidth(width);

        toDraw.draw(loc);

        return toDraw.getHeight();
    }

    private float getAgeRatio() {
        return timer / lifetime;
    }

    @Override
    public boolean isExpired() {
        return timer <= 0f;
    }
}
