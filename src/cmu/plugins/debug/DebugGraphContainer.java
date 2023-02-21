package cmu.plugins.debug;

import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static org.lazywizard.lazylib.opengl.ColorUtils.glColor;
import static org.lwjgl.opengl.GL11.*;

public class DebugGraphContainer implements BaseDebugContainer {
    private final float[] data;
    private int index = 0;
    private final String label;
    private final int capacity;
    private final float height;
    private boolean autoFormat;

    private float max;
    private float min;
    private boolean expired;

    public DebugGraphContainer(String label, float max, float min, int capacity, float height) {
        this.label = label;
        this.max = max;
        this.min = min;
        this.capacity = capacity;
        this.height = height;

        data = new float[capacity];
        expired = false;
        autoFormat = false;
    }

    public DebugGraphContainer(String label, int capacity, float height) {
        this(label, 0f, 0f, capacity, height);
        autoFormat = true;
    }

    public void increment(float next) {
        synchronized (data) {
            data[index] = next;

            index++;
            index %= data.length;
        }
    }

    @Override
    public void advance(float amount) {

    }

    @Override
    public boolean isExpired() {
        return expired;
    }

    public void expire() {
        expired = true;
    }

    @Override
    public float render(Vector2f input, LazyFont.DrawableString toDraw, Color color, float width) {
        Vector2f loc = new Vector2f(input);
        float graphWidth = width - 50f;
        float graphHeight = height;

        toDraw.setBaseColor(color);
        toDraw.setText(label);
        toDraw.draw(loc);

        final float yPad = toDraw.getHeight() + 1f;
        loc.y -= yPad;
        graphHeight += yPad;

        loc.x += 15f;

        // background
        Vector2f b = new Vector2f(loc);

        glBegin(GL_LINE_STRIP);
        glLineWidth(1f);
        glColor(color.darker());

        glVertex2f(b.x, b.y);

        b.y -= graphHeight;
        glVertex2f(b.x, b.y);

        b.x += graphWidth;
        glVertex2f(b.x, b.y);

        glEnd();

        float[] offsets = new float[data.length];
        synchronized (data) {
            int gap = data.length - index;
            System.arraycopy(data, index, offsets, 0, gap);
            System.arraycopy(data, 0, offsets, gap, index);
        }

        if (autoFormat) {
            max = Float.MIN_VALUE;
            min = Float.MAX_VALUE;

            for (float f : offsets) {
                max = Math.max(max, f);
                min = Math.min(min, f);
            }
        }

        // text
        toDraw.setBaseColor(color.darker());
        Vector2f textLoc = new Vector2f(loc);

        toDraw.setText(String.format("%.3f", max).substring(0, 5));
        textLoc.x = loc.x - (toDraw.getWidth() + 4f);
        toDraw.draw(textLoc);

        toDraw.setText(String.format("%.3f", min).substring(0, 5));
        textLoc.y -= graphHeight - toDraw.getHeight();
        textLoc.x = loc.x - (toDraw.getWidth() + 4f);
        toDraw.draw(textLoc);

        // graph
        glBegin(GL_LINE_STRIP);
        glLineWidth(1f);
        glColor(color);

        float increment = graphWidth / capacity;

        Vector2f l = new Vector2f(loc);
        l.y -= graphHeight;

        float y = l.y;

        for (float f : offsets) {
            float d = max - min;
            float h = (f - min) / d;
            h *= graphHeight;
            l.y = y + h;

            glVertex2f(l.x, l.y);

            l.x += increment;
        }

        glEnd();

        graphHeight += toDraw.getHeight();

        return graphHeight;
    }
}
