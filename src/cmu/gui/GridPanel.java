package cmu.gui;

import com.fs.starfarer.api.input.InputEventAPI;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.lazywizard.lazylib.opengl.ColorUtils.glColor;
import static org.lwjgl.opengl.GL11.*;

public class GridPanel implements Element {

    private final GridParams params;
    private final PanelMaker panelMaker;
    private Element[][] children;

    public GridPanel(GridParams gridParams, PanelMaker panelMaker) {
        this.params = gridParams;
        this.panelMaker = panelMaker;
    }

    @Override
    public Vector2f update(float scale, Vector2f loc, List<InputEventAPI> events) {
        if (params.update) {
            panelMaker.make(this);
        }

        if (children == null || children.length == 0) return new Vector2f(0f, 0f);

        Vector2f l2 = new Vector2f(loc);
//        l2.x += params.edgePad;
//        l2.y -= params.edgePad;

        float dx = params.x / children[0].length;
        float dy = params.y / children.length;

        float x0 = l2.x;
        for (Element[] row : children) {
            for (Element element : row) {
                if (element != null) {
                    element.update(scale, l2, events);
                }

                l2.x += dx;
            }

            l2.y -= dy;
            l2.x = x0;
        }

        return new Vector2f(params.x, params.y);
    }

    @Override
    public Vector2f render(float scale, Vector2f loc, List<InputEventAPI> events) {
        glLineWidth(scale);

        if (params.background) {
            glColor(new Color(0f, 0f, 0f, 0.5f));

            glEnable(GL_BLEND);
            glBegin(GL_TRIANGLE_STRIP);
            glVertex2f(0f, 0f);
            glVertex2f(params.x, 0f);
            glVertex2f(0f, -params.y);
            glVertex2f(params.x, -params.y);
            glEnd();
            glDisable(GL_BLEND);
        }

        if (!params.noDeco) {
            glBegin(GL_LINE_LOOP);
            glColor(params.color);

            glVertex2f(0f, 0f);
            glVertex2f(params.x, 0f);
            glVertex2f(params.x, -params.y);
            glVertex2f(0f, -params.y);

            glEnd();
        }

        if (children == null || children.length == 0) {
            return new Vector2f(params.x, params.y);
        }

        glEnable(GL_SCISSOR_TEST);
        glScissor((int) (loc.x + params.edgePad), (int) (loc.y - params.y + params.edgePad), (int) (params.x - (2f * params.edgePad)), (int) (params.y - params.edgePad));

        glPushMatrix();

        glTranslatef((int) params.edgePad, (int) -params.edgePad, 0f);

        Vector2f l2 = new Vector2f(loc);
        l2.x += params.edgePad;
        l2.y -= params.edgePad;

        float dx = params.x / children[0].length;
        float dy = params.y / children.length;

        float x0 = l2.x;
        for (Element[] row : children) {
            glPushMatrix();

            for (Element element : row) {
                if (element != null) {
                    element.render(scale, l2, events);
                }

                float s = dx + 0f; // pad
                glTranslatef(s, 0f, 0f);
                l2.x += s;
            }

            glPopMatrix();

            float s = dy + 0f; // pad
            glTranslatef(0f, -s, 0f);
            l2.y -= s;
            l2.x = x0;
        }

        glPopMatrix();

        glDisable(GL_SCISSOR_TEST);

        return new Vector2f(params.x, params.y);
    }

    @Override
    public float getWidth() {
        return params.x;
    }

    @Override
    public float getHeight() {
        return params.y;
    }

    public Element[][] getChildren() {
        return children;
    }

    public void setChildren(Element[][] children) {
        this.children = children;
    }

    public static class GridParams {
        public boolean update = false;
        public float x = 100f;
        public float y = 100f;
        public float edgePad = 1f;
        public float gridPad = 2f;
        public Color color = Color.WHITE;
        public boolean noDeco = false;
        public boolean background = true;
    }

    public interface PanelMaker {
        /**
         * If params are set to update, clears children and calls every frame
         * @param gridPanel The panel
         */
        void make(GridPanel gridPanel);
    }
}
