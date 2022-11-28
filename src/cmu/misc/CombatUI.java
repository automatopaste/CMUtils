package cmu.misc;

import cmu.plugins.SubsystemCombatManager;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import com.fs.starfarer.ui.V;
import org.lazywizard.lazylib.ui.FontException;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lazywizard.lazylib.ui.LazyFont.DrawableString;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author Dark.Revenant, Tartiflette, LazyWizard, Snrasha, tomatopaste
 * because jesus christ why are most of these private methods in magiclib
 *
 */

public class CombatUI {
    //Color of the HUD when the ship is alive or the hud
    public static final Color GREENCOLOR;
    //Color of the HUD when the ship is not alive.
    public static final Color BLUCOLOR;
    //Color of the HUD for the red colour.
    public static final Color REDCOLOR;
    private static DrawableString TODRAW14;

    private static final Vector2f PERCENTBARVEC1 = new Vector2f(21f, 0f); // Just 21 pixel of width of difference.
    private static final Vector2f PERCENTBARVEC2 = new Vector2f(50f, 58f);

    private static float UIscaling = Global.getSettings().getScreenScaleMult();

    private static final String[] alphabet = new String[] {
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J"
    };

    private static final Vector2f DRONE_UI_WIDGET_OFFSET = new Vector2f(Global.getSettings().getFloat("cmu_widgetOffsetX"), Global.getSettings().getFloat("cmu_widgetOffsetY"));

    static {
        GREENCOLOR = Global.getSettings().getColor("textFriendColor");
        BLUCOLOR = Global.getSettings().getColor("textNeutralColor");
        REDCOLOR = Global.getSettings().getColor("textEnemyColor");

        try {
            LazyFont fontdraw = LazyFont.loadFont("graphics/fonts/victor14.fnt");
            TODRAW14 = fontdraw.createText();

            if (UIscaling > 1f) { //mf
                TODRAW14.setFontSize(14f * UIscaling);
            }

        } catch (FontException ignored) {
        }
    }

    ///////////////////////////////////
    //                               //
    //         SUBSYSTEM GUI         //
    //                               //
    ///////////////////////////////////

    /**
     *
     * @author tomatopaste
     * @param ship Player ship
     * @param fill Value 0 to 1, how full the bar is from left to right
     * @param name Name of subsystem
     * @param infoText Info string opportunity
     * @param stateText Subsystem activity status
     * @param hotkey Hotkey string of key used to activate subsystem
     * @param flavourText A brief description of what the subsystem does
     */
    public static Vector2f drawSubsystemStatus(
            ShipAPI ship,
            float fill,
            String name,
            String infoText,
            String stateText,
            String hotkey,
            String flavourText,
            boolean showInfoText,
            int guiBarCount,
            Vector2f inputLoc,
            Vector2f rootLoc
    ) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (!ship.equals(engine.getPlayerShip()) || engine.isUIShowingDialog()) return null;
        if (engine.getCombatUI() == null || engine.getCombatUI().isShowingCommandUI() || !engine.isUIShowingHUD()) return null;

        Color colour = (ship.isAlive()) ? GREENCOLOR : BLUCOLOR;

        final float barHeight = 13f * UIscaling;
        final int bars = (showInfoText) ? guiBarCount + 1 : guiBarCount;

        Vector2f loc = new Vector2f(inputLoc);

        openGL11ForText();

        TODRAW14.setMaxWidth(6969);

        TODRAW14.setText(name);
        TODRAW14.setBaseColor(Color.BLACK);
        TODRAW14.draw(loc.x + 1, loc.y - 1);
        TODRAW14.setBaseColor(colour);
        TODRAW14.draw(loc);

        Vector2f hotkeyTextLoc = new Vector2f(loc);
        hotkeyTextLoc.y -= barHeight * UIscaling;
        hotkeyTextLoc.x += 20f * UIscaling;

        TODRAW14.setText("HOTKEY: " + hotkey);
        float hotkeyTextWidth = TODRAW14.getWidth();
        if (showInfoText) {
            TODRAW14.setBaseColor(Color.BLACK);
            TODRAW14.draw(hotkeyTextLoc.x + 1, hotkeyTextLoc.y - 1);
            TODRAW14.setBaseColor(colour);
            TODRAW14.draw(hotkeyTextLoc);
        }

        Vector2f flavourTextLoc = new Vector2f(hotkeyTextLoc);
        flavourTextLoc.x += hotkeyTextWidth + 20f * UIscaling;

        TODRAW14.setText("BRIEF: " + flavourText);
        if (showInfoText) {
            TODRAW14.setBaseColor(Color.BLACK);
            TODRAW14.draw(flavourTextLoc.x + 1, flavourTextLoc.y - 1);
            TODRAW14.setBaseColor(colour);
            TODRAW14.draw(flavourTextLoc);
        }

        Vector2f boxLoc = new Vector2f(loc);
        boxLoc.x += 200f * UIscaling;

        final float boxHeight = 9f * UIscaling;
        final float boxEndWidth = 45f * UIscaling;

        float boxWidth = boxEndWidth * fill;

        Vector2f stateLoc = new Vector2f(boxLoc);
        TODRAW14.setText(stateText);
        stateLoc.x -= TODRAW14.getWidth() + (4f * UIscaling);
        TODRAW14.setBaseColor(Color.BLACK);
        TODRAW14.draw(stateLoc.x + 1, stateLoc.y - 1);
        TODRAW14.setBaseColor(colour);
        TODRAW14.draw(stateLoc);

        Vector2f infoLoc = new Vector2f(boxLoc);
        infoLoc.x += boxEndWidth + (5f * UIscaling);
        TODRAW14.setText(infoText);
        TODRAW14.setBaseColor(Color.BLACK);
        TODRAW14.draw(infoLoc.x + 1, infoLoc.y - 1);
        TODRAW14.setBaseColor(colour);
        TODRAW14.draw(infoLoc);

        closeGL11ForText();

        final int width = (int) (Display.getWidth() * Display.getPixelScaleFactor());
        final int height = (int) (Display.getHeight() * Display.getPixelScaleFactor());

        glPushAttrib(GL_ALL_ATTRIB_BITS);
        glViewport(0, 0, width, height);
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, width, 0, height, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glTranslatef(0.01f, 0.01f, 0);

        Vector2f nodeLoc = new Vector2f(loc);
        nodeLoc.y -= 4f * UIscaling;
        nodeLoc.x -= 2f * UIscaling;

        Vector2f titleLoc = getSubsystemTitleLoc(ship);
        boolean isHigh = loc.y > titleLoc.y;

        glLineWidth(UIscaling);
        glBegin(GL_LINE_STRIP);
        glColor4f(colour.getRed() / 255f, colour.getGreen() / 255f, colour.getBlue() / 255f, 1f - engine.getCombatUI().getCommandUIOpacity());
        glVertex2f(nodeLoc.x, nodeLoc.y);

        nodeLoc.y += (isHigh) ? -6f * UIscaling : 6f * UIscaling;
        nodeLoc.x -= 6f * UIscaling;
        glVertex2f(nodeLoc.x, nodeLoc.y);

        boolean isTitleHigh = rootLoc.y > titleLoc.y - 16f;
        nodeLoc.y = getSubsystemTitleLoc(ship).y;
        nodeLoc.y -= 16f;
        nodeLoc.y -= (isTitleHigh) ? -6f * UIscaling : 6f * UIscaling;
        glVertex2f(nodeLoc.x, nodeLoc.y);

        glEnd();

        Vector2f boxRenderLoc = new Vector2f(boxLoc);
        boxRenderLoc.y -= 3f * UIscaling;

        //drop shadow
        Vector2f shadowLoc = new Vector2f(boxRenderLoc);
        shadowLoc.x += UIscaling;
        shadowLoc.y -= UIscaling;
        glBegin(GL_TRIANGLE_STRIP);
        glColor4f(0f, 0f, 0f, 1f - engine.getCombatUI().getCommandUIOpacity());
        glVertex2f(shadowLoc.x, shadowLoc.y);
        glVertex2f(shadowLoc.x + boxWidth, shadowLoc.y);
        glVertex2f(shadowLoc.x, shadowLoc.y - boxHeight);
        glVertex2f(shadowLoc.x + boxWidth, shadowLoc.y - boxHeight);
        glEnd();

        glBegin(GL_TRIANGLE_STRIP);
        glColor4f(colour.getRed() / 255f, colour.getGreen() / 255f, colour.getBlue() / 255f, 1f - engine.getCombatUI().getCommandUIOpacity());
        glVertex2f(boxRenderLoc.x, boxRenderLoc.y);
        glVertex2f(boxRenderLoc.x + boxWidth, boxRenderLoc.y);
        glVertex2f(boxRenderLoc.x, boxRenderLoc.y - boxHeight);
        glVertex2f(boxRenderLoc.x + boxWidth, boxRenderLoc.y - boxHeight);
        glEnd();

        Vector2f boxEndBarLoc = new Vector2f(boxRenderLoc);
        boxEndBarLoc.x += boxEndWidth;

        glLineWidth(UIscaling);
        glBegin(GL_LINES);
        glColor4f(0f, 0f, 0f, 1f - engine.getCombatUI().getCommandUIOpacity());
        glVertex2f(boxEndBarLoc.x + 1, boxEndBarLoc.y - 1);
        glVertex2f(boxEndBarLoc.x + 1, boxEndBarLoc.y - boxHeight - 1);
        glEnd();

        glLineWidth(UIscaling);
        glBegin(GL_LINES);
        glColor4f(colour.getRed() / 255f, colour.getGreen() / 255f, colour.getBlue() / 255f, 1f - engine.getCombatUI().getCommandUIOpacity());
        glVertex2f(boxEndBarLoc.x, boxEndBarLoc.y);
        glVertex2f(boxEndBarLoc.x, boxEndBarLoc.y - boxHeight);
        glEnd();

        glDisable(GL_BLEND);
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glPopAttrib();

        loc.y -= bars * barHeight;
        return loc;
    }

    public static void renderAuxiliaryStatusBar(ShipAPI ship, float indent, float fillStartX, float fillLength, float fillLevel, String text1, String text2, Vector2f inputLoc) {
        CombatEngineAPI engine = Global.getCombatEngine();

        Color colour = (ship.isAlive()) ? GREENCOLOR : BLUCOLOR;

        inputLoc.x += indent * UIscaling;

        Vector2f boxLoc = new Vector2f(inputLoc);
        boxLoc.x += fillStartX * UIscaling;

        final float boxHeight = 9f * UIscaling;
        final float boxEndWidth = fillLength * UIscaling;

        float boxWidth = boxEndWidth * fillLevel;

        openGL11ForText();

        TODRAW14.setMaxWidth(6969);

        TODRAW14.setText(text1);
        TODRAW14.setBaseColor(Color.BLACK);
        TODRAW14.draw(inputLoc.x + 1, inputLoc.y - 1);
        TODRAW14.setBaseColor(colour);
        TODRAW14.draw(inputLoc);

        Vector2f text2Pos = new Vector2f(boxLoc);
        text2Pos.x += boxEndWidth + (4f * UIscaling);
        TODRAW14.setText(text2);
        TODRAW14.setBaseColor(Color.BLACK);
        TODRAW14.draw(text2Pos.x + 1, text2Pos.y - 1);
        TODRAW14.setBaseColor(colour);
        TODRAW14.draw(text2Pos);

        closeGL11ForText();

        final int width = (int) (Display.getWidth() * Display.getPixelScaleFactor());
        final int height = (int) (Display.getHeight() * Display.getPixelScaleFactor());

        glPushAttrib(GL_ALL_ATTRIB_BITS);
        glViewport(0, 0, width, height);
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, width, 0, height, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glTranslatef(0.01f, 0.01f, 0);

        Vector2f nodeLoc = new Vector2f(inputLoc);
        nodeLoc.x -= 2f * UIscaling;
        nodeLoc.y -= 11f * UIscaling;

        glLineWidth(UIscaling);
        glBegin(GL_LINE_STRIP);
        glColor4f(colour.getRed() / 255f, colour.getGreen() / 255f, colour.getBlue() / 255f, 1f - engine.getCombatUI().getCommandUIOpacity());
        glVertex2f(nodeLoc.x, nodeLoc.y);

        nodeLoc.x -= 6f * UIscaling;
        nodeLoc.y += 6f * UIscaling;
        glVertex2f(nodeLoc.x, nodeLoc.y);

        nodeLoc.y += 4f * UIscaling; //7
        glVertex2f(nodeLoc.x, nodeLoc.y);

        glEnd();

        Vector2f boxRenderLoc = new Vector2f(boxLoc);
        boxRenderLoc.y -= 3f * UIscaling;

        //drop shadow
        Vector2f shadowLoc = new Vector2f(boxRenderLoc);
        shadowLoc.x += UIscaling;
        shadowLoc.y -= UIscaling;
        glBegin(GL_TRIANGLE_STRIP);
        glColor4f(0f, 0f, 0f, 1f - engine.getCombatUI().getCommandUIOpacity());
        glVertex2f(shadowLoc.x, shadowLoc.y);
        glVertex2f(shadowLoc.x + boxWidth, shadowLoc.y);
        glVertex2f(shadowLoc.x, shadowLoc.y - boxHeight);
        glVertex2f(shadowLoc.x + boxWidth, shadowLoc.y - boxHeight);
        glEnd();

        glBegin(GL_TRIANGLE_STRIP);
        glColor4f(colour.getRed() / 255f, colour.getGreen() / 255f, colour.getBlue() / 255f, 1f - engine.getCombatUI().getCommandUIOpacity());
        glVertex2f(boxRenderLoc.x, boxRenderLoc.y);
        glVertex2f(boxRenderLoc.x + boxWidth, boxRenderLoc.y);
        glVertex2f(boxRenderLoc.x, boxRenderLoc.y - boxHeight);
        glVertex2f(boxRenderLoc.x + boxWidth, boxRenderLoc.y - boxHeight);
        glEnd();

        Vector2f boxEndBarLoc = new Vector2f(boxRenderLoc);
        boxEndBarLoc.x += boxEndWidth;

        glLineWidth(UIscaling);
        glBegin(GL_LINES);
        glColor4f(0f, 0f, 0f, 1f - engine.getCombatUI().getCommandUIOpacity());
        glVertex2f(boxEndBarLoc.x + 1, boxEndBarLoc.y - 1);
        glVertex2f(boxEndBarLoc.x + 1, boxEndBarLoc.y - boxHeight - 1);
        glEnd();

        glLineWidth(UIscaling);
        glBegin(GL_LINES);
        glColor4f(colour.getRed() / 255f, colour.getGreen() / 255f, colour.getBlue() / 255f, 1f - engine.getCombatUI().getCommandUIOpacity());
        glVertex2f(boxEndBarLoc.x, boxEndBarLoc.y);
        glVertex2f(boxEndBarLoc.x, boxEndBarLoc.y - boxHeight);
        glEnd();

        glDisable(GL_BLEND);
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glPopAttrib();
    }

    public static Vector2f getSubsystemsRootLocation(ShipAPI ship, int numBars, float barHeight) {
        Vector2f loc = new Vector2f(529f, 74f);
        Vector2f.add(loc, getUIElementOffset(ship, ship.getVariant()), loc);

        float height = numBars * barHeight * UIscaling;

        int numWeapons = getNumWeapons(ship);
        float maxWeaponHeight = numWeapons * (13f * UIscaling) + 30f;
        if (numWeapons == 0) maxWeaponHeight -= 5f * UIscaling;

        final float minOffset = 10f * UIscaling;
        float weaponOffset = maxWeaponHeight + minOffset;

        loc.y = weaponOffset + height;

        loc.x *= UIscaling;

        return loc;
    }

    private static int getNumWeapons(ShipAPI ship) {
        WeaponGroupAPI groups = ship.getSelectedGroupAPI();
        List<WeaponAPI> weapons = (groups == null) ? null : groups.getWeaponsCopy();
        return (weapons == null) ? 0 : weapons.size();
    }

    private static Vector2f getSubsystemTitleLoc(ShipAPI ship) {
        Vector2f loc = new Vector2f(529f, 72f);
        Vector2f.add(loc, getUIElementOffset(ship, ship.getVariant()), loc);
        loc.scale(UIscaling);

        return loc;
    }

    public static void drawSubsystemsTitle(ShipAPI ship, boolean showInfo, Vector2f rootLoc) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (!ship.equals(engine.getPlayerShip()) || engine.isUIShowingDialog()) return;
        if (engine.getCombatUI() == null || engine.getCombatUI().isShowingCommandUI() || !engine.isUIShowingHUD()) return;

        Color colour = (ship.isAlive()) ? GREENCOLOR : BLUCOLOR;

        float barHeight = 13f * UIscaling;
        Vector2f loc = getSubsystemTitleLoc(ship);

        openGL11ForText();

        String info = "TOGGLE INFO WITH \"" + SubsystemCombatManager.INFO_TOGGLE_KEY + "\"";

        Vector2f titleTextLoc = new Vector2f(loc);
        Vector2f infoTextLoc = new Vector2f(rootLoc);

        TODRAW14.setText("SUBSYSTEMS");
        titleTextLoc.x -= TODRAW14.getWidth() + (14f * UIscaling);

        TODRAW14.setBaseColor(Color.BLACK);
        TODRAW14.draw(titleTextLoc.x + 1f, titleTextLoc.y - 1f);
        TODRAW14.setBaseColor(colour);
        TODRAW14.draw(titleTextLoc);

        if (showInfo) {
            infoTextLoc.y += barHeight * UIscaling;
            infoTextLoc.x -= 4f * UIscaling;

            TODRAW14.setText(info);
            TODRAW14.setBaseColor(Color.BLACK);
            TODRAW14.draw(infoTextLoc.x + 1f, infoTextLoc.y - 1f);
            TODRAW14.setBaseColor(colour);
            TODRAW14.draw(infoTextLoc);
        }

        closeGL11ForText();

        openGLForMisc();

        glLineWidth(UIscaling);
        glBegin(GL_LINE_STRIP);
        glColor4f(colour.getRed() / 255f, colour.getGreen() / 255f, colour.getBlue() / 255f, 1f - engine.getCombatUI().getCommandUIOpacity());

        Vector2f sysBarNode = new Vector2f(loc);

        final float length = 354f * UIscaling; //354
        sysBarNode.x -= length;
        sysBarNode.y += 4f * UIscaling;
        glVertex2f(sysBarNode.x, sysBarNode.y);

        //sysBarNode.x += length - (16f * UIscaling);
        sysBarNode.x += length - (113f * UIscaling);
        glVertex2f(sysBarNode.x, sysBarNode.y);

        sysBarNode.x += 20f * UIscaling;
        sysBarNode.y -= 20f * UIscaling;
        glVertex2f(sysBarNode.x, sysBarNode.y);

        sysBarNode.x += (85f - 6f * UIscaling);
        glVertex2f(sysBarNode.x, sysBarNode.y);

        boolean isTitleHigh = rootLoc.y > loc.y - 16f;
        sysBarNode.y += (isTitleHigh) ? 6f * UIscaling : -6f * UIscaling;
        sysBarNode.x += 6f * UIscaling;
        glVertex2f(sysBarNode.x, sysBarNode.y);

        glEnd();

        closeGLForMisc();
    }

    ///////////////////////////////////
    //                               //
    //          STATUS BAR           //
    //                               //
    ///////////////////////////////////

    /**
     * Draw a third status bar above the Flux and Hull ones on the User Interface.
     * With a text of the left and the number on the right.
     *
     * @param ship Player ship.
     *
     * @param fill Filling level of the bar. 0 to 1
     *
     * @param innerColor Color of the bar. If null, the vanilla green UI colour will be used.
     *
     * @param borderColor Color of the border. If null, the vanilla green UI colour will be used.
     *
     * @param secondfill Wider filling like the soft/hard-flux. 0 to 1.
     *
     * @param text The text written to the left, automatically cut if too large.
     *
     * @param rearText The text displayed on the right.
     */
    public static void drawSecondUnlimitedInterfaceStatusBar(ShipAPI ship, float fill, Color innerColor, Color borderColor, float secondfill, String text, String rearText) {
        if (ship != Global.getCombatEngine().getPlayerShip()) {
            return;
        }

        if (Global.getCombatEngine().getCombatUI() == null || Global.getCombatEngine().getCombatUI().isShowingCommandUI() || !Global.getCombatEngine().isUIShowingHUD()) {
            return;
        }

        addSecondUnlimitedInterfaceStatusBar(ship, fill, innerColor, borderColor, secondfill);
        if (TODRAW14 != null) {
            addInterfaceStatusText(ship, text);
            addInterfaceStatusNumber(ship, rearText);
        }
    }

    public static void drawDroneSystemUI(ShipAPI ship, boolean[] tiles, int extra, String text1, String text2, float cooldown, int reserve, int reserveMax, int activeState, int numStates, String state, SpriteAPI icon) {
        if (ship != Global.getCombatEngine().getPlayerShip()) {
            return;
        }

        if (Global.getCombatEngine().getCombatUI() == null || Global.getCombatEngine().getCombatUI().isShowingCommandUI() || !Global.getCombatEngine().isUIShowingHUD()) {
            return;
        }

        final Vector2f tileDim = new Vector2f(100f, 20f);
        final Vector2f statusDim = new Vector2f(100f, 12f);
        final Vector2f reserveDim = new Vector2f(12f, 12f);
        final Vector2f stateRender = new Vector2f(22f, 22f);
        final Vector2f iconDim = new Vector2f(32f, 32f);

        final Vector2f edgePad = new Vector2f(-16f - reserveDim.x - (2f * UIscaling), 16f);
        edgePad.scale(UIscaling);
        Vector2f start = Vector2f.add(new Vector2f(Global.getSettings().getScreenWidth() * UIscaling, 0f), edgePad, new Vector2f());
        Vector2f.add(start, DRONE_UI_WIDGET_OFFSET, start);

        Color colour = (ship.isAlive()) ? GREENCOLOR : BLUCOLOR;

        Vector2f decoSize = new Vector2f(tileDim.x, tileDim.y + statusDim.y);

        decoRender(Color.BLACK, start, new Vector2f(UIscaling, -UIscaling), decoSize);
        decoRender(colour, start, new Vector2f(0f, 0f), decoSize);

        Vector2f reserveStart = new Vector2f(start);

        final Vector2f decoPad = new Vector2f(-4f, 4f);
        decoPad.scale(UIscaling);
        Vector2f.add(start, decoPad, start);

        tileRender(Color.BLACK, start, new Vector2f(UIscaling, -UIscaling), tileDim, tiles, extra, text1);
        float hPad = tileRender(colour, start, new Vector2f(0f, 0f), tileDim, tiles, extra, text1);

        final float pad = UIscaling * 4f;
        start.y += tileDim.y + pad;

        boolean full = reserve >= reserveMax && cooldown > 0.95f;
        statusRender(Color.BLACK, start, new Vector2f(UIscaling, -UIscaling), statusDim, text2, cooldown, hPad, full);
        statusRender(colour, start, new Vector2f(0f, 0f), statusDim, text2, cooldown, hPad, full);

        reserveRender(Color.BLACK, reserveStart, new Vector2f(UIscaling, -UIscaling), reserveDim, reserve, reserveMax);
        reserveRender(colour, reserveStart, new Vector2f(0f, 0f), reserveDim, reserve, reserveMax);

        start.y += statusDim.y + pad;

        stateRender(Color.BLACK, start, new Vector2f(UIscaling, -UIscaling), stateRender, state, numStates, activeState);
        stateRender(colour, start, new Vector2f(0f, 0f), stateRender, state, numStates, activeState);

        start.x += UIscaling;
        start.y += iconDim.y + pad;

        iconRender(Color.BLACK, icon, start, new Vector2f(UIscaling, -UIscaling), iconDim);
        iconRender(colour, icon, start, new Vector2f(0f, 0f), iconDim);
    }

    private static void iconRender(Color colour, SpriteAPI sprite, Vector2f start, Vector2f offset, Vector2f size) {
        if (sprite == null) return;

        Vector2f dim = new Vector2f(size);
        dim.scale(UIscaling);
        offset.scale(UIscaling);

        Vector2f node = Vector2f.add(start, offset, new Vector2f());

        sprite.setSize(size.x, size.y);
        sprite.setColor(colour);

        openGLForMisc();
        sprite.render(node.x, node.y - (size.y * 0.25f));
        closeGLForMisc();
    }

    private static void stateRender(Color colour, Vector2f start, Vector2f offset, Vector2f size, String text, int num, int active) {
        Vector2f dim = new Vector2f(size);
        dim.scale(UIscaling);
        offset.scale(UIscaling);

        Vector2f node = Vector2f.add(start, offset, new Vector2f());

        float w = num * size.x;

        openGLForMisc();

        float x1 = 0.05f * size.x;
        float x2 = 0.3f * size.x;
        float x3 = 0.7f * size.x;
        float x4 = 0.95f * size.x;

        float y1 = 0.1f * size.x + node.y;
        float y2 = 0.5f * size.y + node.y;
        float y3 = 0.9f * size.y + node.y;

        float x = node.x - w;
        for (int i = 0; i < num; i++) {
            Color c = i == active ? colour : colour.darker().darker();

            glColor4f(
                    c.getRed() / 255f,
                    c.getGreen() / 255f,
                    c.getBlue() / 255f,
                    1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity()
            );

            glBegin(GL_TRIANGLE_FAN);

            glVertex2f(x + x1, y2);
            glVertex2f(x + x2, y1);
            glVertex2f(x + x3, y1);
            glVertex2f(x + x4, y2);
            glVertex2f(x + x3, y3);
            glVertex2f(x + x2, y3);

            glEnd();

            x += size.x;
        }

        float x5 = node.x - w + (0.25f * dim.x);
        float x6 = node.x;
        float y5 = node.y + dim.y + (2f * UIscaling);
        float y6 = y5 + UIscaling;

        glColor4f(
                colour.getRed() / 255f,
                colour.getGreen() / 255f,
                colour.getBlue() / 255f,
                1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity()
        );

        glBegin(GL_TRIANGLE_STRIP);
        glVertex2f(x5, y5);
        glVertex2f(x5, y6);
        glVertex2f(x6, y5);
        glVertex2f(x6, y6);
        glEnd();

        float x7 = (active * size.x) + (0.5f * size.x) + node.x - w;
        float x8 = x7 + (dim.x * 0.25f);
        float x9 = x7 - (dim.x * 0.25f);
        float y7 = y5 + (6f * UIscaling);

        glBegin(GL_TRIANGLES);
        glVertex2f(x8, y6);
        glVertex2f(x7, y7);
        glVertex2f(x9, y6);
        glEnd();

        closeGLForMisc();

        openGL11ForText();

        x = node.x - w;
        for (int i = 0; i < num; i++) {
            TODRAW14.setText(i < 11 ? alphabet[i] : "NIL");

            float tx = Math.round((0.5f * dim.x) - (0.5f * TODRAW14.getWidth()));
            float ty = Math.round((0.5f * dim.y) + (0.5f * TODRAW14.getHeight()));

            TODRAW14.setBaseColor(Color.BLACK);
            TODRAW14.draw(tx + x, ty + node.y);
            x += size.x;
        }

        TODRAW14.setText(text);
        TODRAW14.setBaseColor(colour);
        TODRAW14.draw(node.x - TODRAW14.getWidth(), node.y + dim.y + (10f * UIscaling) + TODRAW14.getHeight());

        closeGL11ForText();
    }

    private static void reserveRender(Color colour, Vector2f start, Vector2f offset, Vector2f size, int num, int max) {
        Vector2f dim = new Vector2f(size);
        dim.scale(UIscaling);
        offset.scale(UIscaling);

        Vector2f node = Vector2f.add(start, offset, new Vector2f());

        float x1 = node.x + (dim.x * 0.2f);
        float x2 = node.x + (dim.x * 0.8f);
        float y1 = dim.y * 0.2f;
        float y2 = dim.y * 0.8f;

        float y = node.y;

        openGLForMisc();

        for (int i = 0; i < max; i++) {
            glBegin(GL_TRIANGLE_STRIP);

            Color c = i >= num ? colour.darker().darker() : colour;

            glColor4f(
                    c.getRed() / 255f,
                    c.getGreen() / 255f,
                    c.getBlue() / 255f,
                    1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity()
            );

            glVertex2f(x1, y + y1);
            glVertex2f(x1, y + y2);
            glVertex2f(x2, y + y1);
            glVertex2f(x2, y + y2);

            y += dim.x;
            glEnd();
        }

        float x3 = node.x - UIscaling;
        float x4 = node.x + dim.x + UIscaling;
        float y3 = dim.x * max + node.y;
        float y4 = y3 + UIscaling;

        glBegin(GL_TRIANGLE_STRIP);
        glVertex2f(x3, y3);
        glVertex2f(x3, y4);
        glVertex2f(x4, y3);
        glVertex2f(x4, y4);
        glEnd();

        closeGLForMisc();

        int overflow = Math.max(num - max, 0);

        TODRAW14.setText("+" + overflow);
        TODRAW14.setBaseColor(colour);
        float x5 = node.x + (2f * UIscaling);
        float y5 = y + TODRAW14.getHeight() + UIscaling;

        openGL11ForText();
        TODRAW14.draw(x5, y5);
        closeGL11ForText();
    }

    private static void decoRender(Color colour, Vector2f start, Vector2f offset, Vector2f size) {
        Vector2f dim = new Vector2f(size);
        dim.scale(UIscaling);
        offset.scale(UIscaling);

        Vector2f node = Vector2f.add(start, offset, new Vector2f());

        final Vector2f decoPad = new Vector2f(-4f, 4f);
        decoPad.scale(UIscaling);

        openGLForMisc();

        // edge deco

        glBegin(GL_TRIANGLE_STRIP);

        glColor4f(
                colour.getRed() / 255f,
                colour.getGreen() / 255f,
                colour.getBlue() / 255f,
                1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity()
        );

        float px = 0.5f * decoPad.x;
        float py = 0.5f * decoPad.y;

        glVertex2f(node.x + decoPad.x - (dim.x * 1.2f), node.y);
        glVertex2f(node.x + decoPad.x - (dim.x * 1.2f), node.y + py);
        glVertex2f(node.x + px, node.y);
        glVertex2f(node.x + px, node.y + py);
        glVertex2f(node.x, node.y + py);
        glVertex2f(node.x + px, node.y + (decoPad.y + (1.2f * dim.y)));
        glVertex2f(node.x, node.y + (decoPad.y + (1.2f * dim.y)));

        glEnd();

        closeGLForMisc();
    }

    private static void statusRender(Color colour, Vector2f start, Vector2f offset, Vector2f size, String text, float fill, float hPad, boolean full) {
        Vector2f dim = new Vector2f(size);
        dim.scale(UIscaling);
        offset.scale(UIscaling);

        dim.x += hPad;

        Vector2f node = Vector2f.add(start, offset, new Vector2f());

        openGL11ForText();

        final float textPad = 10f * UIscaling;

        TODRAW14.setText(text);
        TODRAW14.setBaseColor(colour);

        float ty = node.y + (dim.y * 0.5f) + (int) (TODRAW14.getHeight() * 0.5f) + 1f;

        TODRAW14.draw(node.x - TODRAW14.getWidth() - dim.x - textPad, ty);

        closeGL11ForText();

        openGLForMisc();

        glColor4f(
                colour.getRed() / 255f,
                colour.getGreen() / 255f,
                colour.getBlue() / 255f,
                1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity()
        );

        float y1 = node.y + (dim.y * 0.1f);
        float y2 = node.y + (dim.y * 0.9f);
        float x1 = node.x - (dim.x * fill);
        float x2 = node.x;

        glBegin(GL_TRIANGLE_STRIP);
        glVertex2f(x2, y1);
        glVertex2f(x2, y2);
        glVertex2f(x1, y1);
        glVertex2f(x1, y2);
        glEnd();

        float y5 = node.y + (dim.y * 0.1f);
        float y6 = y5 + UIscaling;
        float y7 = node.y + (dim.y * 0.9f);
        float y8 = y7 - UIscaling;
        float x5 = node.x - dim.x;
        float x6 = x5 + (4f * UIscaling);

        glBegin(GL_TRIANGLE_STRIP);
        glVertex2f(x5, y5);
        glVertex2f(x5, y6);
        glVertex2f(x6, y5);
        glVertex2f(x6, y6);
        glEnd();
        glBegin(GL_TRIANGLE_STRIP);
        glVertex2f(x5, y7);
        glVertex2f(x5, y8);
        glVertex2f(x6, y7);
        glVertex2f(x6, y8);
        glEnd();

        closeGLForMisc();

        if (full) {
            openGL11ForText();

            TODRAW14.setText("reserve full");

            TODRAW14.setBaseColor(Color.BLACK);
            TODRAW14.draw(node.x - (int) (0.5f * dim.x) - (int) (0.5f * TODRAW14.getWidth()), ty);

            closeGL11ForText();
        }
    }

    private static float tileRender(Color colour, Vector2f start, Vector2f offset, Vector2f size, boolean[] tiles, int extra, String text) {
        Vector2f dim = new Vector2f(size);
        dim.scale(UIscaling);
        offset.scale(UIscaling);

        Vector2f node = Vector2f.add(start, offset, new Vector2f());

        TODRAW14.setText("+" + extra);
        float pad = TODRAW14.getWidth();

        openGLForMisc();

        // chevron tiles

        float y1 = node.y + (0.9f * dim.y);
        float y2 = node.y + (0.4f * dim.y);
        float y3 = node.y + (0.1f * dim.y);

        float interval = dim.x / tiles.length;
        float x1 = interval * 0.1f;
        float x2 = interval * 0.5f;
        float x3 = interval * 0.9f;

        float xp = node.x - pad - dim.x;
        for (boolean tile : tiles) {
            glBegin(GL_TRIANGLE_STRIP);

            Color c = tile ? colour : colour.darker().darker();

            glColor4f(
                    c.getRed() / 255f,
                    c.getGreen() / 255f,
                    c.getBlue() / 255f,
                    1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity()
            );

            glVertex2f(xp + x1, y3);
            glVertex2f(xp + x2, y2);
            glVertex2f(xp + x2, y1);
            glVertex2f(xp + x3, y3);

            glEnd();

            xp += interval;
        }

        closeGLForMisc();

        // Text rendering

        openGL11ForText();

        TODRAW14.setBaseColor(colour);

        // extra text
        // TODRAW14 already set
        float x4 = node.x - TODRAW14.getWidth();
        float y4 = TODRAW14.getHeight() + node.y;

        TODRAW14.draw(x4, y4);

        final float textPad = 10f * UIscaling;

        TODRAW14.setText(text);
        float x5 = node.x - dim.x - pad - textPad - TODRAW14.getWidth();
        float y5 = node.y + (dim.y * 0.5f) + (int) (TODRAW14.getHeight() * 0.5f) + 1f;

        TODRAW14.draw(x5, y5);

        closeGL11ForText();

        return pad;
    }

    ///// UTILS /////

    /**
     * Get the UI Element Offset for the Third bar. (Depends of the group
     * layout, or if the player has some wing)
     *
     * @param ship The player ship.
     * @param variant The variant of the ship.
     * @return The offset who depends of weapon and wing.
     */
    private static Vector2f getInterfaceOffsetFromStatusBars(ShipAPI ship, ShipVariantAPI variant) {
        return getUIElementOffset(ship, variant);
    }

    /**
     * Get the UI Element Offset.
     * (Depends on the weapon groups and wings present)
     *
     * @param ship The player ship.
     * @param variant The variant of the ship.
     * @return the offset.
     */
    private static Vector2f getUIElementOffset(ShipAPI ship, ShipVariantAPI variant) {
        int numEntries = 0;
        final List<WeaponGroupSpec> weaponGroups = variant.getWeaponGroups();
        final List<WeaponAPI> usableWeapons = ship.getUsableWeapons();
        for (WeaponGroupSpec group : weaponGroups) {
            final Set<String> uniqueWeapons = new HashSet<>(group.getSlots().size());
            for (String slot : group.getSlots()) {
                boolean isUsable = false;
                for (WeaponAPI weapon : usableWeapons) {
                    if (weapon.getSlot().getId().contentEquals(slot)) {
                        isUsable = true;
                        break;
                    }
                }
                if (!isUsable) {
                    continue;
                }
                String id = Global.getSettings().getWeaponSpec(variant.getWeaponId(slot)).getWeaponName();
                if (id != null) {
                    uniqueWeapons.add(id);
                }
            }
            numEntries += uniqueWeapons.size();
        }
        if (variant.getFittedWings().isEmpty()) {
            if (numEntries < 2) {
                return CombatUI.PERCENTBARVEC1;
            }
            return new Vector2f(30f + ((numEntries - 2) * 13f), 18f + ((numEntries - 2) * 26f));
        } else {
            if (numEntries < 2) {
                return CombatUI.PERCENTBARVEC2;
            }
            return new Vector2f(59f + ((numEntries - 2) * 13f), 76f + ((numEntries - 2) * 26f));
        }
    }


    /**
     * Draws a small UI bar above the flux bar. The HUD colour change to blue
     * when the ship is not alive. Bug: When you left the battle, the hud
     * keep for qew second, no solution found. Bug: Also for other
     * normal drawBox, when paused, they switch brutally of "colour".
     *
     * @param ship Ship concerned (the element will only be drawn if that ship
     * is the player ship)
     * @param fill Filling level
     * @param innerColor Color of the bar. If null, use the vanilla HUD colour.
     * @param borderColor Color of the border. If null, use the vanilla HUD
     * colour.
     * @param secondfill Like the hardflux of the fluxbar. 0 per default.
     */
    private static void addSecondUnlimitedInterfaceStatusBar(ShipAPI ship, float fill, Color innerColor, Color borderColor, float secondfill) {
        float boxWidth = 79;
        float boxHeight = 7;
        if (UIscaling > 1f) {
            boxWidth *= UIscaling;
            boxHeight *= UIscaling;
        }
        final Vector2f element = getInterfaceOffsetFromStatusBars(ship, ship.getVariant());
        final Vector2f boxLoc = Vector2f.add(new Vector2f(224f, 120f), element, null);
        final Vector2f shadowLoc = Vector2f.add(new Vector2f(225f, 119f), element, null);
        if (UIscaling > 1f) {
            boxLoc.scale(UIscaling);
            shadowLoc.scale(UIscaling);
        }

        // Used to properly interpolate between colours
        float alpha = 1;
        if (Global.getCombatEngine().isUIShowingDialog()) {
            alpha = 0.28f;
        }

        Color innerCol = innerColor == null ? GREENCOLOR : innerColor;
        Color borderCol = borderColor == null ? GREENCOLOR : borderColor;
        if (!ship.isAlive()) {
            innerCol = BLUCOLOR;
            borderCol = BLUCOLOR;
        }
        float hardfill = secondfill < 0 ? 0 : secondfill;
        hardfill = hardfill > 1 ? 1 : hardfill;
        int pixelHardfill = (int) (boxWidth * hardfill);
        pixelHardfill = pixelHardfill <= 3 ? -pixelHardfill : -3;

        int hfboxWidth = (int) (boxWidth * hardfill);
        int fboxWidth = (int) (boxWidth * fill);

        OpenGLBar(ship, alpha, borderCol, innerCol, fboxWidth, hfboxWidth, boxHeight, boxWidth, pixelHardfill, shadowLoc, boxLoc);
    }

    /**
     * Draw the text with the font victor14.
     * @param ship The player ship
     * @param text The text to write.
     */
    private static void addInterfaceStatusText(ShipAPI ship, String text) {
        if (ship != Global.getCombatEngine().getPlayerShip()) {
            return;
        }
        if (Global.getCombatEngine().getCombatUI().isShowingCommandUI() || !Global.getCombatEngine().isUIShowingHUD()) {
            return;
        }
        Color borderCol = GREENCOLOR;
        if (!ship.isAlive()) {
            borderCol = BLUCOLOR;
        }
        float alpha = 1;
        if (Global.getCombatEngine().isUIShowingDialog()) {
            alpha = 0.28f;
        }
        Color shadowcolour = new Color(Color.BLACK.getRed() / 255f, Color.BLACK.getGreen() / 255f, Color.BLACK.getBlue() / 255f,
                1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity());
        Color colour = new Color(borderCol.getRed() / 255f, borderCol.getGreen() / 255f, borderCol.getBlue() / 255f,
                alpha * (borderCol.getAlpha() / 255f)
                        * (1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity()));

        final Vector2f boxLoc = Vector2f.add(new Vector2f(176f, 131f),
                getInterfaceOffsetFromStatusBars(ship, ship.getVariant()), null);
        final Vector2f shadowLoc = Vector2f.add(new Vector2f(177f, 130f),
                getInterfaceOffsetFromStatusBars(ship, ship.getVariant()), null);

        openGL11ForText();
        if (UIscaling > 1f) {
            TODRAW14.setFontSize(14*UIscaling);

            boxLoc.scale(UIscaling);
            shadowLoc.scale(UIscaling);
        }
        TODRAW14.setText(text);
        TODRAW14.setMaxWidth(46*UIscaling);
        TODRAW14.setMaxHeight(14*UIscaling);
        TODRAW14.setBaseColor(shadowcolour);
        TODRAW14.draw(shadowLoc);
        TODRAW14.setBaseColor(colour);
        TODRAW14.draw(boxLoc);
        closeGL11ForText();

    }

    /**
     * Draw number at the right of the percent bar.
     * @param ship The player ship died or alive.
     * @param rearText The number NOT displayed, Not bounded per the method to 0 at 999
     * 999.
     */
    private static void addInterfaceStatusNumber(ShipAPI ship, String rearText) {
        if (ship != Global.getCombatEngine().getPlayerShip()) {
            return;
        }
        if (Global.getCombatEngine().getCombatUI().isShowingCommandUI() || !Global.getCombatEngine().isUIShowingHUD()) {
            return;
        }

        Color borderCol = GREENCOLOR;
        if (!ship.isAlive()) {
            borderCol = BLUCOLOR;
        }
        float alpha = 1;
        if (Global.getCombatEngine().isUIShowingDialog()) {
            alpha = 0.28f;
        }
        Color shadowColour = new Color(Color.BLACK.getRed() / 255f, Color.BLACK.getGreen() / 255f, Color.BLACK.getBlue() / 255f,
                1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity());
        Color colour = new Color(borderCol.getRed() / 255f, borderCol.getGreen() / 255f, borderCol.getBlue() / 255f,
                alpha * (borderCol.getAlpha() / 255f)
                        * (1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity()));

        final Vector2f boxLoc = Vector2f.add(new Vector2f(355f, 131f),
                getInterfaceOffsetFromStatusBars(ship, ship.getVariant()), null);
        final Vector2f shadowLoc = Vector2f.add(new Vector2f(356f, 130f),
                getInterfaceOffsetFromStatusBars(ship, ship.getVariant()), null);
        if (UIscaling > 1f) {
            boxLoc.scale(UIscaling);
            shadowLoc.scale(UIscaling);
        }

        openGL11ForText();
        TODRAW14.setText(rearText);
        float width = TODRAW14.getWidth() - 1;
        TODRAW14.setBaseColor(shadowColour);
        TODRAW14.draw(shadowLoc.x - width, shadowLoc.y);
        TODRAW14.setBaseColor(colour);
        TODRAW14.draw(boxLoc.x - width, boxLoc.y);
        closeGL11ForText();
    }

    private static void OpenGLBar(ShipAPI ship, float alpha, Color borderCol, Color innerCol, int fboxWidth, int hfboxWidth, float boxHeight, float boxWidth, int pixelHardfill, Vector2f shadowLoc, Vector2f boxLoc) {
        final int width = (int) (Display.getWidth() * Display.getPixelScaleFactor());
        final int height = (int) (Display.getHeight() * Display.getPixelScaleFactor());

        // Set OpenGL flags
        glPushAttrib(GL_ALL_ATTRIB_BITS);
        glViewport(0, 0, width, height);
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, width, 0, height, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glTranslatef(0.01f, 0.01f, 0);

        if (ship.isAlive()) {
            // Render the drop shadow
            if (fboxWidth != 0) {
                glBegin(GL_TRIANGLE_STRIP);
                glColor4f(Color.BLACK.getRed() / 255f, Color.BLACK.getGreen() / 255f, Color.BLACK.getBlue() / 255f,
                        1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity());
                glVertex2f(shadowLoc.x - 1, shadowLoc.y);
                glVertex2f(shadowLoc.x + fboxWidth, shadowLoc.y);
                glVertex2f(shadowLoc.x - 1, shadowLoc.y + boxHeight + 1);
                glVertex2f(shadowLoc.x + fboxWidth, shadowLoc.y + boxHeight + 1);
                glEnd();
            }
        }

        // Render the drop shadow of border.
        glBegin(GL_LINES);
        glColor4f(Color.BLACK.getRed() / 255f, Color.BLACK.getGreen() / 255f, Color.BLACK.getBlue() / 255f,
                1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity());
        glVertex2f(shadowLoc.x + hfboxWidth + pixelHardfill, shadowLoc.y - 1);
        glVertex2f(shadowLoc.x + 3 + hfboxWidth + pixelHardfill, shadowLoc.y - 1);
        glVertex2f(shadowLoc.x + hfboxWidth + pixelHardfill, shadowLoc.y + boxHeight);
        glVertex2f(shadowLoc.x + 3 + hfboxWidth + pixelHardfill, shadowLoc.y + boxHeight);
        glVertex2f(shadowLoc.x + boxWidth, shadowLoc.y);
        glVertex2f(shadowLoc.x + boxWidth, shadowLoc.y + boxHeight);

        // Render the border transparency fix
        glColor4f(Color.BLACK.getRed() / 255f, Color.BLACK.getGreen() / 255f, Color.BLACK.getBlue() / 255f,
                1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity());
        glVertex2f(boxLoc.x + hfboxWidth + pixelHardfill, boxLoc.y - 1);
        glVertex2f(boxLoc.x + 3 + hfboxWidth + pixelHardfill, boxLoc.y - 1);
        glVertex2f(boxLoc.x + hfboxWidth + pixelHardfill, boxLoc.y + boxHeight);
        glVertex2f(boxLoc.x + 3 + hfboxWidth + pixelHardfill, boxLoc.y + boxHeight);
        glVertex2f(boxLoc.x + boxWidth, boxLoc.y);
        glVertex2f(boxLoc.x + boxWidth, boxLoc.y + boxHeight);

        // Render the border
        glColor4f(borderCol.getRed() / 255f, borderCol.getGreen() / 255f, borderCol.getBlue() / 255f,
                alpha * (1 - Global.getCombatEngine().getCombatUI().getCommandUIOpacity()));
        glVertex2f(boxLoc.x + hfboxWidth + pixelHardfill, boxLoc.y - 1);
        glVertex2f(boxLoc.x + 3 + hfboxWidth + pixelHardfill, boxLoc.y - 1);
        glVertex2f(boxLoc.x + hfboxWidth + pixelHardfill, boxLoc.y + boxHeight);
        glVertex2f(boxLoc.x + 3 + hfboxWidth + pixelHardfill, boxLoc.y + boxHeight);
        glVertex2f(boxLoc.x + boxWidth, boxLoc.y);
        glVertex2f(boxLoc.x + boxWidth, boxLoc.y + boxHeight);
        glEnd();

        // Render the fill element
        if (ship.isAlive()) {
            glBegin(GL_TRIANGLE_STRIP);
            glColor4f(innerCol.getRed() / 255f, innerCol.getGreen() / 255f, innerCol.getBlue() / 255f,
                    alpha * (innerCol.getAlpha() / 255f)
                            * (1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity()));
            glVertex2f(boxLoc.x, boxLoc.y);
            glVertex2f(boxLoc.x + fboxWidth, boxLoc.y);
            glVertex2f(boxLoc.x, boxLoc.y + boxHeight);
            glVertex2f(boxLoc.x + fboxWidth, boxLoc.y + boxHeight);
            glEnd();
        }
        glDisable(GL_BLEND);
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glPopAttrib();

    }

    /**
     * GL11 to start, when you want render text of Lazyfont.
     */
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

    /**
     * GL11 to close, when you want render text of Lazyfont.
     */
    private static void closeGL11ForText() {
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glPopMatrix();
        glPopAttrib();
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
}