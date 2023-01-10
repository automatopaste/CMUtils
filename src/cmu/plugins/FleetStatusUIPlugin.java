package cmu.plugins;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import org.lazywizard.lazylib.ui.FontException;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;
import sun.plugin2.gluegen.runtime.CPU;

import java.awt.*;
import java.util.*;
import java.util.List;

import static org.lazywizard.lazylib.opengl.ColorUtils.glColor;
import static org.lwjgl.opengl.GL11.*;

public class FleetStatusUIPlugin extends BaseEveryFrameCombatPlugin {

    public static final Color GREEN = Global.getSettings().getColor("textFriendColor");
    public static final Color BLUE = Global.getSettings().getColor("textNeutralColor");

    private static final float SCALING = Global.getSettings().getScreenScaleMult();

    private static LazyFont.DrawableString TODRAW14;

    private float maxWidth = 800f;
    private float maxHeight = 300f;

    private final Map<String, SpriteData> sprites = new HashMap<>();

    private final boolean[] states = new boolean[] {true, true, true, true};

    @Override
    public void init(CombatEngineAPI engine) {
        if (TODRAW14 == null) {
            try {
                LazyFont fontdraw = LazyFont.loadFont("graphics/fonts/victor14.fnt");
                TODRAW14 = fontdraw.createText();

                if (SCALING > 1f) {
                    TODRAW14.setFontSize(14f * SCALING);
                }

            } catch (FontException ignored) {
            }
        }

        sprites.clear();
    }

    public static class SizeComparator implements Comparator<ShipAPI> {

        private static final Map<ShipAPI.HullSize, Integer> sizes = new HashMap<>();
        static {
            sizes.put(ShipAPI.HullSize.FRIGATE, 3);
            sizes.put(ShipAPI.HullSize.DESTROYER, 2);
            sizes.put(ShipAPI.HullSize.CRUISER, 1);
            sizes.put(ShipAPI.HullSize.CAPITAL_SHIP, 0);
        }

        @Override
        public int compare(ShipAPI o1, ShipAPI o2) {
            int s1 = sizes.get(o1.getHullSize());
            int s2 = sizes.get(o2.getHullSize());

            if (s1 == s2) {
                int d2 = (int) o1.getHullSpec().getSuppliesPerMonth();
                int d1 = (int) o2.getHullSpec().getSuppliesPerMonth();

                return Integer.compare(d1, d2);
            }

            return Integer.compare(s1, s2);
        }
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        CombatEngineAPI engine = Global.getCombatEngine();

        if (Global.getCurrentState() == GameState.TITLE) return;
        if (engine.isUIShowingDialog() || !engine.isUIShowingHUD()) return;

        List<ShipAPI> ships = new ArrayList<>();
        for (ShipAPI ship : engine.getShips()) {
            if (ship.getOwner() == 0 && ship.isAlive() && ship.getHullSize() != ShipAPI.HullSize.FIGHTER && !ship.equals(engine.getPlayerShip())) {
                switch (ship.getHullSize()) {
                    case FRIGATE:
                        if (states[0]) ships.add(ship);
                        break;
                    case DESTROYER:
                        if (states[1]) ships.add(ship);
                        break;
                    case CRUISER:
                        if (states[2]) ships.add(ship);
                        break;
                    case CAPITAL_SHIP:
                        if (states[3]) ships.add(ship);
                        break;
                }
            }
        }

        Collections.sort(ships, new SizeComparator());

        Vector2f origin = new Vector2f(Global.getSettings().getScreenWidth(), Global.getSettings().getScreenHeight());
        origin.x -= 160f;
        origin.y -= 10f;

        final float size = 12f * SCALING;
        final float offset = 18f * SCALING;
        float ox1 = origin.x;
        float ox2 = ox1 + size;
        float oy1 = origin.y - offset;
        float oy2 = oy1 - size;

        for (InputEventAPI event : events) {
            if (event.isMouseDownEvent() && !event.isConsumed()) {
                for (int i = 0; i < states.length; i++) {
                    boolean b = states[i];

                    float y = i * (size + SCALING);

                    if (event.getX() > ox1 && event.getX() < ox2 && event.getY() < oy1 - y && event.getY() > oy2 - y) {
                        states[i] = !b;

                        event.consume();
                        break;
                    }
                }
            }
        }

        float maxX = origin.x;
        float minX = origin.x - maxWidth;

        final float pad = 1f * SCALING;

        origin.x += 1f;
        renderDeco(new Vector2f(origin.x + 1f, origin.y - 1f), Color.BLACK);
        renderDeco(origin, GREEN);

        renderIcons(new Vector2f(origin.x + (2f * SCALING), origin.y - (18f * SCALING)), size, states);

        Vector2f dim = new Vector2f(120f, 40f);
        origin.x -= dim.x;

        for (ShipAPI ship : ships) {
            renderStatusMinimal(new Vector2f(origin.x + 1f, origin.y - 1f), dim, ship, Color.BLACK);
            renderStatusMinimal(origin, dim, ship, GREEN);

            origin.x -= dim.x + pad;

            if (origin.x < minX) {
                origin.x = maxX - dim.x;
                origin.y -= dim.y;
            }
        }
    }

    private void renderDeco(Vector2f origin, Color color) {
        final float w = 1f * SCALING;

        float x1 = origin.x;
        float x2 = origin.x + (48f * SCALING);

        float y1 = origin.y - (14f * SCALING);
        float y2 = y1 - w;

        openGLForMisc();
        glBegin(GL_TRIANGLE_STRIP);
        glColor(color);
        glVertex2f(x1, y1);
        glVertex2f(x1, y2);
        glVertex2f(x2, y1);
        glVertex2f(x2, y2);
        glEnd();
        closeGLForMisc();

        openGL11ForText();
        TODRAW14.setBaseColor(color);
        TODRAW14.setText("STATUS");
        TODRAW14.draw(origin.x + (4f * SCALING), origin.y);
        closeGL11ForText();
    }

    private void renderIcons(Vector2f origin, float size, boolean[] states) {
        float x = origin.x;
        float y = origin.y;

        for (int i = 0; i < 4; i++) {
            openGLForMisc();
            glColor(states[i] ? GREEN : GREEN.darker().darker().darker());
            glBegin(GL_TRIANGLE_STRIP);
            glVertex2f(x, y);
            glVertex2f(x + size, y);
            glVertex2f(x, y - size);
            glVertex2f(x + size, y - size);
            glEnd();
            closeGLForMisc();

            String s;
            switch (i) {
                case 0:
                    s = "F";
                    break;
                case 1:
                    s = "D";
                    break;
                case 2:
                    s = "C";
                    break;
                case 3:
                default:
                    s = "B";
                    break;
            }

            TODRAW14.setText(s);
            TODRAW14.setBaseColor(Color.BLACK);

            openGL11ForText();

            TODRAW14.draw(x + (2f * SCALING), y + (2f * SCALING));

            closeGL11ForText();

            y -= size + SCALING;
        }
    }

    private void renderStatusMinimal(Vector2f origin, Vector2f dim, ShipAPI ship, Color color) {
        openGLForMisc();

        glColor(color);

        final float length = 6f * SCALING;
        final float width = 2f * SCALING;

        Vector2f innerDim = new Vector2f(dim);
        innerDim.x -= width + width;
        innerDim.y -= width + width;

        float x1 = origin.x;
        float x2 = x1 + width;
        float x3 = x2 + length;
        float x4 = x3 + width;

        float y1 = origin.y;
        float y2 = y1 - width;
        float y3 = y2 - length;
        float y4 = y3 - width;

        glBegin(GL_TRIANGLE_STRIP);
        glVertex2f(x1, y4);
        glVertex2f(x1, y3);
        glVertex2f(x2, y3);
        glVertex2f(x1, y2);
        glVertex2f(x2, y1);
        glVertex2f(x2, y2);
        glVertex2f(x3, y1);
        glVertex2f(x3, y2);
        glVertex2f(x4, y1);
        glEnd();

        float x5 = origin.x + dim.x;
        float x6 = x5 - width;
        float x7 = x6 - length;
        float x8 = x7 - width;

        float y5 = origin.y - dim.y;
        float y6 = y5 + width;
        float y7 = y6 + length;
        float y8 = y7 + width;

        glBegin(GL_TRIANGLE_STRIP);
        glVertex2f(x5, y8);
        glVertex2f(x5, y7);
        glVertex2f(x6, y7);
        glVertex2f(x5, y6);
        glVertex2f(x6, y6);
        glVertex2f(x6, y5);
        glVertex2f(x7, y6);
        glVertex2f(x7, y5);
        glVertex2f(x8, y5);
        glEnd();

        closeGLForMisc();

        openGL11ForText();

        Vector2f node = new Vector2f(origin);
        node.x += width + (SCALING);
        float nx = node.x;
        float ny = node.y;

        TODRAW14.setText(ship.getName());
        TODRAW14.setMaxWidth(dim.x);
        TODRAW14.setMaxHeight(14f);
        TODRAW14.setBaseColor(color);
        TODRAW14.draw(node.x, node.y);

        final float yp = 8f;

        node.y -= yp;

        float ty1 = node.y;
        TODRAW14.setText("HULL");
        TODRAW14.draw(node);

        node.y -= yp;
        float tw = TODRAW14.getWidth();

        float ty2 = node.y;
        TODRAW14.setText("FLUX");
        TODRAW14.draw(node);

        node.y -= yp;

        float ty3 = node.y;
        TODRAW14.setText("CR");
        TODRAW14.draw(node);

        closeGL11ForText();

        openGLForMisc();

        final float wPad = 35f * SCALING;
        final float wMult = 0.6f;
        final float wMultI = 1f - wMult;

        float innerWidth = (innerDim.x * wMult) - tw - (6f * SCALING);
        renderBar(new Vector2f(node.x + wPad, ty1 - 4f), new Vector2f(innerWidth, yp - 2f), ship.getHullLevel(), color);
        renderBar(new Vector2f(node.x + wPad, ty2 - 4f), new Vector2f(innerWidth, yp - 2f), ship.getFluxLevel(), color);
        renderBar(new Vector2f(node.x + wPad, ty3 - 4f), new Vector2f(innerWidth, yp - 2f), ship.getCurrentCR(), color);

        closeGLForMisc();

        SpriteData sprite = sprites.get(ship.getHullSpec().getBaseHullId());
        if (sprite == null) {
            SpriteAPI s = Global.getSettings().getSprite(ship.getHullSpec().getSpriteName());
            sprite = new SpriteData(s.getWidth() > s.getHeight(), s.getHeight() / s.getWidth(), s);
            sprites.put(ship.getHullSpec().getBaseHullId(), sprite);
        }

        if (!color.equals(Color.BLACK)) {
            Color c = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * 0.6f));
            renderShip(new Vector2f(nx + innerDim.x * wMult, ny - width), new Vector2f(innerDim.x * wMultI, innerDim.y), sprite, c, ship.getFacing());
        }
    }

    private void renderBar(Vector2f origin, Vector2f dim, float fill, Color color) {
        float x1 = origin.x;
        float x2 = x1 + SCALING;
        float x4 = x1 + dim.x;
        float x3 = x4 - SCALING;

        float xf = ((x4 - x1) * fill) + x1;

        float y1 = origin.y;
        float y2 = y1 - dim.y;

        glColor(color);

//        glBegin(GL_TRIANGLE_STRIP);
//        glVertex2f(x1, y1);
//        glVertex2f(x1, y2);
//        glVertex2f(x2, y1);
//        glVertex2f(x2, y2);
//        glEnd();

        glBegin(GL_TRIANGLE_STRIP);
        glVertex2f(x3, y1);
        glVertex2f(x3, y2);
        glVertex2f(x4, y1);
        glVertex2f(x4, y2);
        glEnd();

        glBegin(GL_TRIANGLE_STRIP);
        glVertex2f(x1, y1);
        glVertex2f(xf, y1);
        glVertex2f(x1, y2);
        glVertex2f(xf, y2);
        glEnd();
    }

    private void renderShip(Vector2f origin, Vector2f dim, SpriteData sprite, Color color, float angle) {
        glColor(color);

        float x = origin.x + (dim.x * 0.5f);
        float y = origin.y - (dim.y * 0.5f);

        float dx;
        float dy;
        if (sprite.isWidth) {
            dx = dim.x;
            dy = dx * sprite.ratio;
        } else {
            dy = dim.y;
            dx = dy / sprite.ratio;
        }

        openGLForMisc();

        SpriteAPI s = sprite.sprite;
        s.setWidth(dx);
        s.setHeight(dy);
        s.setAngle(angle - 90f);
        s.setColor(color);
        s.renderAtCenter(x, y);

//        glBegin(GL_TRIANGLE_STRIP);
//        glVertex2f(origin.x, origin.y);
//        glVertex2f(origin.x + dim.x, origin.y);
//        glVertex2f(origin.x, origin.y - dim.y);
//        glVertex2f(origin.x + dim.x, origin.y - dim.y);
//        glEnd();

        closeGLForMisc();
    }

    public static class SpriteData {
        public final boolean isWidth;
        public final float ratio;
        public final SpriteAPI sprite;

        public SpriteData(boolean isWidth, float ratio, SpriteAPI sprite) {
            this.isWidth = isWidth;
            this.ratio = ratio;
            this.sprite = sprite;
        }
    }

    private static void openGLForMisc() {
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

    private static void closeGLForMisc() {
        glDisable(GL_BLEND);
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glPopAttrib();
    }

    private static void openGL11ForText() {
        glPushAttrib(GL_ENABLE_BIT);
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
        glOrtho(0.0, Display.getWidth(), 0.0, Display.getHeight(), -1.0, 1.0);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    private static void closeGL11ForText() {
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glPopMatrix();
        glPopAttrib();
    }
}
