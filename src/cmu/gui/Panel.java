package cmu.gui;

import com.fs.starfarer.api.input.InputEventAPI;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.lazywizard.lazylib.opengl.ColorUtils.glColor;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glVertex2f;

public class Panel implements Element {

    public enum ListMode {
        VERTICAL,
        HORIZONTAL,
    }

    private final PanelParams params;
    private final PanelMaker panelMaker;
    private final List<Element> children;

    public Panel(PanelParams params, PanelMaker panelMaker) {
        this.params = params;
        this.panelMaker = panelMaker;
        children = new ArrayList<>();

        panelMaker.make(this);
    }

    @Override
    public Vector2f render(float scale, Vector2f loc, List<InputEventAPI> events) {
        if (params.update) {
            children.clear();
            panelMaker.make(this);
        }

        glBegin(GL_LINE_LOOP);
        glLineWidth(scale);
        glColor(params.color);

        glVertex2f(0f, 0f);
        glVertex2f(params.x, 0f);
        glVertex2f(params.x, -params.y);
        glVertex2f(0f, -params.y);

        glEnd();

        glEnable(GL_SCISSOR_TEST);
        glScissor((int) (loc.x + params.edgePad), (int) (loc.y - params.y + params.edgePad), (int) (params.x - (2f * params.edgePad)), (int) (params.y - params.edgePad));

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

        if (params.conformToListSize) {
            switch (params.mode) {
                case VERTICAL:
                    params.y = (loc.y - l2.y) + params.edgePad;
                    break;
                case HORIZONTAL:
                    params.x = (loc.x - l2.x) + params.edgePad;
                    break;
            }
        }

        glDisable(GL_SCISSOR_TEST);

        return new Vector2f(params.x, params.y);
    }

    public void addChild(Element e) {
        children.add(e);
    }

    public List<Element> getChildren() {
        return children;
    }

    public static final class PanelParams {
        public boolean update = false;
        public float x = 100f;
        public float y = 100f;
        public float edgePad = 4f;
        public float listPad = 2f;
        public Color color = Color.WHITE;
        public ListMode mode = ListMode.VERTICAL;
        public boolean conformToListSize = false;
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
         * @param panel The panel
         */
        void make(Panel panel);
    }
}
