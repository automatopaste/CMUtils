package cmu.gui;

import com.fs.starfarer.api.input.InputEventAPI;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.lazywizard.lazylib.opengl.ColorUtils.glColor;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glVertex2f;

public class ListPanel implements Element {

    public enum ListMode {
        VERTICAL,
        HORIZONTAL,
    }

    private final ListPanelParams params;
    private final PanelMaker panelMaker;
    private final List<Element> children;
    private float width;
    private float height;

    public ListPanel(ListPanelParams params, PanelMaker panelMaker) {
        this.params = params;
        this.panelMaker = panelMaker;
        children = new ArrayList<>();

        panelMaker.make(this);

        width = params.x;
        height = params.y;
    }

    @Override
    public Vector2f update(float scale, Vector2f loc, List<InputEventAPI> events) {
        if (params.update) {
            children.clear();
            panelMaker.make(this);
        }

        float w = 0f, h = 0f;
        Vector2f l2 = new Vector2f(loc);
        l2.x += params.edgePad;
        l2.y -= params.edgePad;

        for (Element e : children) {
            Vector2f v = e.update(scale, l2, events);

            float shift;
            switch (params.mode) {
                case VERTICAL:
                    shift = -params.listPad - v.y;
                    l2.y += shift;
                    h -= shift;
                    w = Math.max(v.x, w);
                    break;
                case HORIZONTAL:
                    shift = params.listPad + v.x;
                    l2.x += shift;
                    w += shift;
                    h = Math.max(v.y, h);
                    break;
            }
        }

        if (params.conformToListSize) {
            if (params.mode == ListMode.HORIZONTAL) {
                width = w + params.edgePad;
            }
            if (params.mode == ListMode.VERTICAL) {
                height = h + params.edgePad;
            }
        }

        return new Vector2f(w, h);
    }

    @Override
    public Vector2f render(float scale, Vector2f loc, List<InputEventAPI> events) {
        glLineWidth(scale);

        if (!params.noDeco) {
            glBegin(GL_LINE_LOOP);
            glColor(params.color);

            glVertex2f(0f, 0f);
            glVertex2f(width, 0f);
            glVertex2f(width, -height);
            glVertex2f(0f, -height);

            glEnd();
        }

        glEnable(GL_SCISSOR_TEST);
        glScissor((int) (loc.x + params.edgePad), (int) (loc.y - height + params.edgePad), (int) (width - (2f * params.edgePad)), (int) (height - params.edgePad));

        glPushMatrix();

        glTranslatef(params.edgePad, -params.edgePad, 0f);

        Vector2f l2 = new Vector2f(loc);
        l2.x += params.edgePad;
        l2.y -= params.edgePad;

        for (Element e : children) {
            Vector2f v = e.render(scale, l2, events);

            float shift;
            switch (params.mode) {
                case VERTICAL:
                    shift = -params.listPad - v.y;
                    glTranslatef(0f, shift, 0f);
                    l2.y += shift;
                    break;
                case HORIZONTAL:
                    shift = params.listPad + v.x;
                    glTranslatef(shift, 0f, 0f);
                    l2.x += shift;
                    break;
            }
        }

        glPopMatrix();

        glDisable(GL_SCISSOR_TEST);

        return new Vector2f(width, height);
    }

    public void addChild(Element e) {
        children.add(e);
    }

    public List<Element> getChildren() {
        return children;
    }

    public static final class ListPanelParams {
        public boolean update = false;
        public float x = 100f;
        public float y = 100f;
        public float edgePad = 1f;
        public float listPad = 2f;
        public Color color = Color.WHITE;
        public ListMode mode = ListMode.VERTICAL;
        public boolean conformToListSize = false;
        public boolean noDeco = false;
    }

    @Override
    public float getWidth() {
        return params.x;
    }

    @Override
    public float getHeight() {
        return params.y;
    }

    public interface PanelMaker {
        /**
         * If params are set to update, clears children and calls every frame
         * @param listPanel The panel
         */
        void make(ListPanel listPanel);
    }
}
