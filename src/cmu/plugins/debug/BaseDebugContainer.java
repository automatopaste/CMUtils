package cmu.plugins.debug;

import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public interface BaseDebugContainer {
    boolean isExpired();

    void advance(float amount);

    float render(Vector2f loc, LazyFont.DrawableString toDraw, Color color, float width);
}
