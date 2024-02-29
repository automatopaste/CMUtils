package cmu.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.input.InputEventAPI;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.util.*;
import java.util.List;

public class DRACULAFLOW extends BaseEveryFrameCombatPlugin {

    public static List<Poem> POEMS;

    private final float interval = 20f;
    private float i = 0f;
    private DRACULAFLOW.Poem currentPoem = null;

    private boolean active = false;
    private final float poemInterval = 2.5f;
    private float p = 0f;

    private final Random random = new Random();

    @Override
    public void init(CombatEngineAPI engine) {
        POEMS = new ArrayList<>();

        try {
            JSONObject obj = Global.getSettings().loadJSON("data/dracula/flow.json");
            JSONArray array = obj.getJSONArray("dracula_flow");
            for (int i = 0; i < array.length(); i++) {
                JSONArray poemArray = array.getJSONArray(i);

                List<String> lines = new ArrayList<>();
                for (int j = 0; j < poemArray.length(); j++) {
                    String s = poemArray.getString(j);
                    lines.add(s);
                }

                Poem poem = new Poem(lines);
                POEMS.add(poem);
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        CombatEngineAPI engine = Global.getCombatEngine();
        ShipAPI ship = engine.getPlayerShip();
        if (ship == null) return;

        float heat = 1f + (2f * ship.getFluxLevel());

        if (active) {
            p += amount;
            if (p >= poemInterval) {
                String s = currentPoem.next();

                Global.getCombatEngine().addFloatingText(ship.getLocation(), s, 20f, new Color(255, 255, 255, 255), ship, 3f, 3f);

                if (currentPoem.index >= currentPoem.lines.size()) {
                    active = false;
                }

                p = 0f;
            }

            i = 0f;
        } else {
            i += amount * heat;
            if (i >= interval) {
                int z = random.nextInt(DRACULAFLOW.POEMS.size());
                currentPoem = new DRACULAFLOW.Poem(DRACULAFLOW.POEMS.get(z).lines);
                active = true;
                i = 0f;
            }

            p = 0f;
        }
    }

    public static final class Poem {
        List<String> lines;
        int index = 0;

        public Poem(List<String> lines) {
            this.lines = lines;
        }

        String next() {
            String s = lines.get(index);
            index++;
            return s;
        }
    }
}
