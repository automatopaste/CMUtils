package cmu.plugins;

import cmu.gui.Element;
import cmu.gui.Execute;
import cmu.gui.ListPanel;
import cmu.gui.Text;
import cmu.misc.Clock;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import org.lazywizard.lazylib.ui.FontException;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class CMUOverlayPlugin extends BaseEveryFrameCombatPlugin {
    public static final int FPS = 60;

    private SpriteAPI tl;
    private SpriteAPI l;
    private SpriteAPI bl;
    private SpriteAPI b;
    private SpriteAPI br;
    private SpriteAPI r;
    private SpriteAPI tr;
    private SpriteAPI t;

    private LazyFont.DrawableString toDraw14;

    private boolean enabled = false;

    private final List<Element> elements;
    private final Clock clock;

    public CMUOverlayPlugin() {
        elements = new ArrayList<>();

        clock = new Clock(FPS);
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (enabled) run();
    }

    @Override
    public void init(CombatEngineAPI engine) {
        try {
            LazyFont fontdraw = LazyFont.loadFont("graphics/fonts/victor14.fnt");
            toDraw14 = fontdraw.createText();
        } catch (FontException ignored) {
        }

        tl = Global.getSettings().getSprite("ui", "panel00_top_left");
        l = Global.getSettings().getSprite("ui", "panel00_left");
        bl = Global.getSettings().getSprite("ui", "panel00_bot_left");
        b = Global.getSettings().getSprite("ui", "panel00_bot");
        br = Global.getSettings().getSprite("ui", "panel00_bot_right");
        r = Global.getSettings().getSprite("ui", "panel00_right");
        tr = Global.getSettings().getSprite("ui", "panel00_top_right");
        t = Global.getSettings().getSprite("ui", "panel00_top");

        ListPanel.ListPanelParams params = new ListPanel.ListPanelParams();
        params.color = Color.GREEN;
        ListPanel listPanel = new ListPanel(params, null);
        elements.add(listPanel);

        Text.TextParams tp = new Text.TextParams();
        tp.color = Color.RED;
        Text text = new Text(new Execute<String>() {
            @Override
            public String get() {
                return "SOME TEXT";
            }
        }, toDraw14, tp);
        listPanel.addChild(text);
    }

    private void run() {
        while (Keyboard.next()) Keyboard.poll();
        while (Mouse.next()) Mouse.poll();

        try {
            while (enabled) {
                Display.update();
                clock.sleepUntilTick();

                if (Display.isCloseRequested()) System.exit(0);

                processInput();
                render(Global.getCombatEngine().getViewport());

                Display.sync(FPS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void processInput() {
        while (Keyboard.next()) {
            boolean down = Keyboard.getEventKeyState();

            if (Keyboard.getEventKey() == Keyboard.KEY_MINUS && down) {
                enabled = false;
                return;
            }
        }
    }

    @Override
    public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {
        for (InputEventAPI event : events) if (event.getEventChar() == '-' && event.isKeyboardEvent()) enabled = true;
    }

    private void render(ViewportAPI viewport) {
        glClear(GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(0f, 0f, 0f, 1f);

        // viewport
        glPushAttrib(GL_ALL_ATTRIB_BITS);

        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glViewport(0, 0, (int) viewport.getVisibleWidth(), (int) viewport.getVisibleHeight());
        glOrtho(0.0, viewport.getVisibleWidth(), 0.0, viewport.getVisibleHeight(), -1.0, 1.0);

        Vector2f center = new Vector2f(0.5f * viewport.getVisibleWidth(), 0.5f * viewport.getVisibleHeight());
        Vector2f dim = new Vector2f(900f, 600f);

        // mv transform
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
        glTranslatef(center.x, center.y, 0f);
        glTranslatef(-dim.x * 0.5f, dim.y * 0.5f, 0f);

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        tl.render(-32f, 0f);

        t.setSize(dim.x, 32f);
        t.render(0f, 0f);

        tr.render(dim.x, 0f);

        l.setSize(32f, dim.y);
        l.setTexHeight(dim.y / 32f);
        l.render(-32f, -dim.y);

        bl.render(-32f, -dim.y - 32f);

        b.setSize(dim.x, 32f);
        b.render(0f, -dim.y - 32f);

        br.render(dim.x, -dim.y - 32f);

        r.setSize(32f, dim.y);
        r.setTexHeight(dim.y / 32f);
        r.render(dim.x, -dim.y);

        for (Element e : elements) {
//            e.render(Global.getSettings().getScreenScaleMult(), new Vector2f());
        }

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);

        // mv transform
        glPopMatrix();

        // viewport
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();

        glPopAttrib();
    }
}
