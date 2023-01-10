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
    private final List<Element> children;

    public Panel(PanelParams params) {
        this.params = params;
        children = new ArrayList<>();
    }

    @Override
    public Vector2f render(float scale, Vector2f loc) {
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

        for (Element e : children) {
            Vector2f v = e.render(scale, loc);

            switch (params.mode) {
                case VERTICAL:
                    glTranslatef(0f, -params.listPad - v.y, 0f);
                    break;
                case HORIZONTAL:
                    glTranslatef(params.listPad + v.x, 0f, 0f);
                    break;
            }

//            glPushMatrix();
//
//            switch (params.mode) {
//                case VERTICAL:
//                    glTranslatef(0f, offset, 0f);
//                    e.render();
//                    offset += e.getHeight();
//                    break;
//                case HORIZONTAL:
//                    glTranslatef(offset, 0f, 0f);
//                    e.render();
//                    offset += e.getWidth();
//                    break;
//            }
//
//            glPopMatrix();
        }

        glDisable(GL_SCISSOR_TEST);

        Vector2f dim = new Vector2f(params.x, params.y);
        return dim;
    }

    @Override
    public void processInputEvents(List<InputEventAPI> events) {
        for (Element e : children) e.processInputEvents(events);
    }

    public void addChild(Element e) {
        children.add(e);
    }

    public List<Element> getChildren() {
        return children;
    }

    public static final class PanelParams {
        public float x = 100f;
        public float y = 100f;
        public float edgePad = 4f;
        public float listPad = 2f;
        public Color color = Color.WHITE;
        public ListMode mode = ListMode.VERTICAL;
    }

    @Override
    public float getWidth() {
        return params.x;
    }

    @Override
    public float getHeight() {
        return params.y;
    }
}
