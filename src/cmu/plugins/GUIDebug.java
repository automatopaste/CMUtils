package cmu.plugins;

import cmu.CMUtils;
import cmu.plugins.debug.BaseDebugContainer;
import cmu.plugins.debug.DebugTextContainer;
import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.ui.FontException;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lazywizard.lazylib.opengl.ColorUtils.glColor;
import static org.lwjgl.opengl.GL11.*;

public class GUIDebug extends BaseEveryFrameCombatPlugin {

    private LazyFont.DrawableString TODRAW14;
    public Color GREENCOLOR;

    private Vector2f size;
    private static final Vector2f PADDING = new Vector2f(-10f, -175f);
    private float width = 300f;
    private static final float W_MAX = 600f;
    private static final float W_MIN = 200f;
    private static final float W_INC = 25f;
    private static final float V_PAD = 2f;
    private static final float P_PAD = 4f;
    private static final float T_PAD = 20f;

    private final Map<Class<?>, Map<String, BaseDebugContainer>> debugData;

    private boolean active = false;

//    private DebugGraphContainer graphContainer;
//    private ShipAPI ship;
//    private final IntervalUtil interval = new IntervalUtil(1f / 60f, 1f / 60f);

    public GUIDebug() {
        debugData = new HashMap<>();
    }

    @Override
    public void init(CombatEngineAPI engine) {
        size = new Vector2f(Global.getSettings().getScreenWidth(), Global.getSettings().getScreenHeight());

        GREENCOLOR = Global.getSettings().getColor("textFriendColor");

        try {
            LazyFont fontdraw = LazyFont.loadFont("graphics/fonts/victor14.fnt");
            TODRAW14 = fontdraw.createText();
        } catch (FontException ignored) {
        }

        CMUtils.setGuiDebug(this);
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
//        if (graphContainer != null) {
//            putContainer(GUIDebug.class, "debug", graphContainer);
//
//            if (!Global.getCombatEngine().isPaused()) {
//                interval.advance(amount);
//                if (interval.intervalElapsed()) {
//                    graphContainer.increment(ship.getVelocity().length());
//                }
//            }
//        } else {
//            List<ShipAPI> ships = Global.getCombatEngine().getShips();
//            if (!ships.isEmpty()) {
//                ship = ships.get(0);
//                graphContainer = new DebugGraphContainer("SHIP VELOCITY", ship.getMaxSpeedWithoutBoost() + ship.getMutableStats().getZeroFluxSpeedBoost().getModifiedValue(), 60, 100f);
//            }
//        }

        List<Class<?>> classes = new ArrayList<>();

        for (Class<?> clazz : debugData.keySet()) {
            Map<String, BaseDebugContainer> category = debugData.get(clazz);

            List<String> keys = new ArrayList<>();
            for (String key : category.keySet()) {
                BaseDebugContainer container = category.get(key);
                container.advance(amount);
                if (container.isExpired()) keys.add(key);
            }
            for (String s : keys) category.remove(s);

            if (category.isEmpty()) classes.add(clazz);
        }

        for (Class<?> clazz : classes) debugData.remove(clazz);
    }

    @Override
    public void renderInUICoords(ViewportAPI viewport) {
        if (Global.getCurrentState() != GameState.COMBAT) return;

        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine.isUIShowingDialog()) return;
        if (engine.getCombatUI() == null || engine.getCombatUI().isShowingCommandUI() || !engine.isUIShowingHUD()) return;

        if (!active) return;

        Vector2f loc = Vector2f.add(size, PADDING, new Vector2f());
        loc.x -= width;

        render(new Vector2f(loc.x + 1f, loc.y - 1f), Color.BLACK);
        render(loc, GREENCOLOR);
    }

    private void render(Vector2f loc, Color color) {
        TODRAW14.setBaseColor(color);
        TODRAW14.setText("COMBAT MISC UTILS DEBUG");
        TODRAW14.draw(loc.x - 10f, loc.y + 20f);
        float w = TODRAW14.getWidth();
        TODRAW14.setText("TOGGLE WITH '\\'");
        TODRAW14.draw(loc.x - 10f + (w - TODRAW14.getWidth()), loc.y + 32f);
        TODRAW14.setMaxWidth(600f);

        TODRAW14.setMaxWidth(width);
        float textHeight = 0f;
        if (debugData.isEmpty()) {
            TODRAW14.setBaseColor(color.darker());
            TODRAW14.setText("NO DATA");
            TODRAW14.draw(loc.x, loc.y);
        } else {
            synchronized (debugData) {
                for (Class<?> clazz : debugData.keySet()) {
                    Map<String, BaseDebugContainer> category = debugData.get(clazz);

                    TODRAW14.setText(clazz.getName());
                    TODRAW14.setBaseColor(color.darker());
                    TODRAW14.draw(loc);

                    float h = loc.y;

                    loc.x += T_PAD;
                    loc.y -= TODRAW14.getHeight() + V_PAD;

                    for (String s : category.keySet()) {
                        BaseDebugContainer container = category.get(s);

                        float height = container.render(loc, TODRAW14, color, width);

                        loc.y -= V_PAD + height;
                    }

                    loc.x -= T_PAD;
                    loc.y -= P_PAD;

                    textHeight += h - loc.y;
                }
            }
        }

        loc = Vector2f.add(size, PADDING, new Vector2f());
        loc.x -= width;
        loc.y += P_PAD;
        loc.x -= P_PAD + 6f;

        glBegin(GL_LINE_STRIP);
        glLineWidth(1f);
        glColor(color);

        loc.y -= textHeight + 10f;
        glVertex2f(loc.x, loc.y);

        loc.y += textHeight;
        glVertex2f(loc.x, loc.y);

        loc.x += 10f;
        loc.y += 10f;
        glVertex2f(loc.x, loc.y);

        loc.x += width;
        glVertex2f(loc.x, loc.y);

        glEnd();
    }

    public void putText(Class<?> clazz, String key, String text) {
        DebugTextContainer container = new DebugTextContainer(text);

        Map<String, BaseDebugContainer> category = debugData.get(clazz);
        if (category == null) category = new HashMap<>();

        category.put(key, container);
        debugData.put(clazz, category);
    }

    public void putContainer(Class<?> clazz, String key, BaseDebugContainer container) {
        Map<String, BaseDebugContainer> category = debugData.get(clazz);
        if (category == null) category = new HashMap<>();

        category.put(key, container);
        debugData.put(clazz, category);
    }

    public BaseDebugContainer getDebugContainer(Class<?> clazz, String key) {
        return debugData.get(clazz).get(key);
    }

    @Override
    public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {
        for (InputEventAPI event : events) {
            if (event.isKeyboardEvent() && event.getEventChar() == '[') width = MathUtils.clamp(width + W_INC, W_MIN, W_MAX);
            if (event.isKeyboardEvent() && event.getEventChar() == ']') width = MathUtils.clamp(width - W_INC, W_MIN, W_MAX);
            if (event.isKeyboardEvent() && event.getEventChar() == '\\') active = !active;
        }
    }

    public void setWidth(float width) {
        this.width = MathUtils.clamp(width, W_MIN, W_MAX);
    }

    public float getWidth() {
        return width;
    }
}
