package cmu.plugins;

import cmu.gui.*;
import cmu.gui.Button;
import cmu.gui.Panel;
import cmu.misc.CombatUI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.ui.P;
import org.lazywizard.lazylib.ui.FontException;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CMUCombatPlugin extends BaseEveryFrameCombatPlugin {

    private static LazyFont.DrawableString TODRAW14;
    private static LazyFont.DrawableString TODRAW24;

    @Override
    public void init(CombatEngineAPI engine) {
        if (TODRAW14 == null) {
            try {
                LazyFont fontdraw = LazyFont.loadFont("graphics/fonts/victor14.fnt");
                TODRAW14 = fontdraw.createText();
                if (Global.getSettings().getScreenScaleMult() > 1f) TODRAW14.setFontSize(14f * Global.getSettings().getScreenScaleMult());
            } catch (FontException ignored) {
            }
        }
        if (TODRAW24 == null) {
            try {
                LazyFont fontdraw = LazyFont.loadFont("graphics/fonts/orbitron24aa.fnt");
                TODRAW24 = fontdraw.createText();
                if (Global.getSettings().getScreenScaleMult() > 1f) TODRAW24.setFontSize(24f * Global.getSettings().getScreenScaleMult());
            } catch (FontException ignored) {
            }
        }
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        CombatUI.hasRendered = false;

        if (TODRAW14 == null) return;

        Panel.PanelParams panelParams = new Panel.PanelParams();
        panelParams.x = 280f;
        panelParams.y = 120f;
        Panel panel = new Panel(panelParams);

        Text.TextParams textParams = new Text.TextParams();
        Text text = new Text(new Execute<String>() {
            @Override
            public String get() {
                return "JOIN MULTIPLAYER GAME";
            }
        }, TODRAW24, textParams);
        panel.addChild(text);

        Text.TextParams textParams2 = new Text.TextParams();
        Text text2 = new Text(new Execute<String>() {
            @Override
            public String get() {
                return "CLIENT ADDRESS: 192.168.86.117";
            }
        }, TODRAW14, textParams2);
        panel.addChild(text2);

        Text.TextParams textParams3 = new Text.TextParams();
        Text text3 = new Text(new Execute<String>() {
            @Override
            public String get() {
                return "CLIENT PORT: 20303";
            }
        }, TODRAW14, textParams3);
        panel.addChild(text3);

        Slider.SliderParams sliderParams = new Slider.SliderParams();
        sliderParams.x = 160f;
        sliderParams.y = 20f;
        Slider slider = new Slider(sliderParams);
//        panel.addChild(slider);

        Text.TextParams buttonTextParams = new Text.TextParams();
        buttonTextParams.align = LazyFont.TextAlignment.CENTER;
        Text buttonText = new Text(new Execute<String>() {
            @Override
            public String get() {
                return "CONNECT";
            }
        }, TODRAW14, buttonTextParams);

        Button.ButtonParams buttonParams = new Button.ButtonParams();
        buttonParams.width = 120f;
        buttonParams.height = 24f;
        Button button = new Button(buttonParams, buttonText);
        panel.addChild(button);

        Text.TextParams infoParams = new Text.TextParams();
        infoParams.color = Color.GREEN;
        Text infoText = new Text(new Execute<String>() {
            @Override
            public String get() {
                return "ENTER TEXT THAT MAY GO OFFSCREEN";
            }
        }, TODRAW14, infoParams);
//        panel.addChild(infoText);

        panel.processInputEvents(events);

        CMUKitUI.render(panel, new Vector2f(500f, 500f));
    }
}
