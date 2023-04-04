package cmu.gui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class CMUKitUI {

    public static void render(Element root, Vector2f loc, List<InputEventAPI> events) {
        openGLForMisc();

        glLineWidth(1f);

        glPushMatrix();
        glTranslatef(loc.x, loc.y, 0f);

        root.update(Global.getSettings().getScreenScaleMult(), loc, events);

        root.render(Global.getSettings().getScreenScaleMult(), loc, events);

        glPopMatrix();

        closeGLForMisc();
    }

    /**
     * GL11 to start, when you want render text of Lazyfont.
     */
    public static void openGL11ForText() {
        glPushAttrib(GL_ENABLE_BIT);
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
        glOrtho(0.0, Display.getWidth(), 0.0, Display.getHeight(), -1.0, 1.0);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * GL11 to close, when you want render text of Lazyfont.
     */
    public static void closeGL11ForText() {
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glPopMatrix();
        glPopAttrib();
    }

    public static void openGLForMisc() {
        final int w = (int) (Display.getWidth() * Display.getPixelScaleFactor());
        final int h = (int) (Display.getHeight() * Display.getPixelScaleFactor());

        glPushAttrib(GL_ALL_ATTRIB_BITS);
        glViewport(0, 0, w, h);
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, w, 0, h, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glTranslatef(0.01f, 0.01f, 0);
    }

    public static void closeGLForMisc() {
        glDisable(GL_BLEND);
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glPopAttrib();
    }
}
