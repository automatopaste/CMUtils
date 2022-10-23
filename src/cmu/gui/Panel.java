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
    public void render() {
//        glBegin(GL_LINE_STRIP);
//        glLineWidth(1f);
//        glColor(params.color);
//
//        glVertex2f(0f, 0f);
//        glVertex2f(params.x, 0f);
//        glVertex2f(params.x, params.y);
//        glVertex2f(0f, params.y);
//
//        glEnd();

        float offset = 0f;
        for (Element e : children) {
            e.render();

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
    }

    @Override
    public void processInputEvents() {

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
