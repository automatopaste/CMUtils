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
    private final Queue<Float> data;
    private final String label;
    private final int capacity;
    private final float height;
    private final boolean autoFormat;

    private float max;
    private boolean expired;

    public DebugGraphContainer(String label, float max, int capacity, float height) {
        this.label = label;
        this.max = max;
        this.capacity = capacity;
        this.height = height;

        data = new CircularQueue<>(120);
        expired = false;
        autoFormat = false;
    }

    public DebugGraphContainer(String label, int capacity, float height) {
        this.label = label;
        this.max = 0f;
        this.capacity = capacity;
        this.height = height;

        autoFormat = true;
        data = new CircularQueue<>(120);
        expired = false;
    }

    public void increment(float next) {
        synchronized (data) {
            data.add(next);
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

        loc.x += 5f;

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

        List<Float> offsets;
        synchronized (data) {
            offsets = new ArrayList<>(data);
        }

        if (autoFormat) {
            max = Float.MIN_VALUE;

            for (float f : offsets) {
                if (f > max) max = f;
            }
        }

        // text
        toDraw.setBaseColor(color.darker());
        String s = String.format("%.3f", max) + "";
        toDraw.setText(s.substring(0, 5));
        Vector2f textLoc = new Vector2f(loc);
        textLoc.x -= toDraw.getWidth() - 2f;
        toDraw.draw(textLoc);

        // graph
        glBegin(GL_LINE_STRIP);
        glLineWidth(1f);
        glColor(color);

        float increment = graphWidth / capacity;
        increment *= 0.5f;

        Vector2f l = new Vector2f(loc);
        l.y -= graphHeight;

        float y = l.y;

        for (float f : offsets) {
            float h = f / max;
            h *= graphHeight;
            l.y = y + h;

            glVertex2f(l.x, l.y);

            l.x += increment;
        }

        glEnd();

        graphHeight += toDraw.getHeight();

        return graphHeight;
    }

    public static class CircularQueue<E> extends LinkedList<E> {
        private final int capacity;

        public CircularQueue(int capacity){
            this.capacity = capacity;
        }

        @Override
        public boolean add(E e) {
            if (size() >= capacity) removeFirst();
            return super.add(e);
        }
    }
}
