package cmu.plugins;

import cmu.subsystems.BaseSubsystem;
import cmu.misc.CombatUI;
import cmu.misc.SpecLoadingUtils;
import cmu.misc.SubsystemManager;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

import java.util.*;

public class SubsystemCombatManager extends BaseEveryFrameCombatPlugin {
    public static final String DATA_KEY = "cmu_SubsystemCombatManager";
    public static final String INFO_TOGGLE_KEY = Global.getSettings().getString("cmu_SubsystemToggleKey");

    private Map<ShipAPI, List<Class<? extends BaseSubsystem>>> subsystemHullmodQueue = new HashMap<>();

    public static boolean showInfoText = true;

    private final Map<ShipAPI, List<BaseSubsystem>> subsystems;
    private static final Map<String, List<Class<? extends BaseSubsystem>>> subsystemsByHullId = new HashMap<>();

    public SubsystemCombatManager() {
        subsystems = new HashMap<>();
    }

    @Override
    public void init(CombatEngineAPI engine) {
        engine.getCustomData().put(DATA_KEY, this);

        showInfoText = true;

        subsystems.clear();

        subsystemHullmodQueue = SubsystemManager.getSubsystemQueue();
        SubsystemManager.getSubsystemQueue().clear();
    }

    private boolean isHotkeyDownLast = false;

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (!engine.isPaused()) {
            List<ShipAPI> ships = engine.getShips();
            for (ShipAPI ship : ships) {
                if (!ship.isAlive()) continue;

                List<BaseSubsystem> subsystemsOnShip = subsystems.get(ship);
                if (subsystemsOnShip == null) subsystemsOnShip = new ArrayList<>();

                List<Class<? extends BaseSubsystem>> toAdd = new ArrayList<>();

                List<Class<? extends BaseSubsystem>> subsystemByHullId = subsystemsByHullId.get(ship.getHullSpec().getBaseHullId());
                if (subsystemByHullId != null) {
                    outer:
                    for (Class<? extends BaseSubsystem> c : subsystemByHullId) {
                        for (BaseSubsystem s : subsystemsOnShip) if (s.getClass().equals(c)) continue outer;

                        toAdd.add(c);
                    }
                }

                List<Class<? extends BaseSubsystem>> hullmodQueue = subsystemHullmodQueue.get(ship);
                if (hullmodQueue != null) {
                    toAdd.addAll(hullmodQueue);
                    subsystemHullmodQueue.put(ship, null);
                }

                int index = 0;
                for (Class<? extends BaseSubsystem> t : toAdd) {
                    try {
                        BaseSubsystem subsystem = t.newInstance();

                        subsystemsOnShip.add(subsystem);
                        subsystem.init(ship);
                        subsystem.setIndex(index);
                        index++;
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

                subsystems.put(ship, subsystemsOnShip);
            }

            List<ShipAPI> rem = new LinkedList<>();
            for (ShipAPI ship : subsystems.keySet()) {
                if (!engine.isEntityInPlay(ship)) {
                    rem.add(ship);
                    continue;
                }

                int index = 0;
                for (BaseSubsystem subsystem : subsystems.get(ship)) {
                    subsystem.setIndex(index);
                    index++;

                    subsystem.advance(amount);
                    if (engine.getPlayerShip().equals(ship)) {
                        if (ship.getShipAI() != null) {
                            subsystem.aiUpdate(amount);
                        }
                    } else {
                        subsystem.aiUpdate(amount);
                    }
                }
            }

            for (ShipAPI ship : rem) subsystems.remove(ship);
        }

        boolean isHotkeyDown = Keyboard.isKeyDown(Keyboard.getKeyIndex(INFO_TOGGLE_KEY));
        if (isHotkeyDown && !isHotkeyDownLast) showInfoText = !showInfoText;
        isHotkeyDownLast = isHotkeyDown;

        ShipAPI player = engine.getPlayerShip();
        List<BaseSubsystem> s;
        if (player != null) {
            s = subsystems.get(player);
            if (s == null || s.isEmpty()) return;

            int numBars = 0;
            for (BaseSubsystem sub : s) {
                numBars += sub.getNumGuiBars();
                if (showInfoText) numBars++;
            }
            Vector2f rootLoc = CombatUI.getSubsystemsRootLocation(player, numBars, 13f * Global.getSettings().getScreenScaleMult());

            Vector2f inputLoc = new Vector2f(rootLoc);
            for (BaseSubsystem sub : s) {
                inputLoc = sub.guiRender(inputLoc, rootLoc);
            }

            CombatUI.drawSubsystemsTitle(engine.getPlayerShip(), showInfoText, rootLoc);

            List<String> defaultHotkeys = new ArrayList<>(SpecLoadingUtils.getSubsystemHotkeyPriority());
            while (defaultHotkeys.size() < s.size()) {
                defaultHotkeys.add(defaultHotkeys.get(5));
            }
            //Collections.reverse(defaultHotkeys);

            for (BaseSubsystem sub : s) sub.setDefaultHotkey(defaultHotkeys.get(sub.getIndex()));
        }
    }

    public Map<ShipAPI, List<BaseSubsystem>> getSubsystems() {
        return subsystems;
    }

    public List<BaseSubsystem> getSubsystemsOnShip(ShipAPI ship) {
        return subsystems.get(ship);
    }

    public static Map<String, List<Class<? extends BaseSubsystem>>> getSubsystemsByHullId() {
        return subsystemsByHullId;
    }
}
