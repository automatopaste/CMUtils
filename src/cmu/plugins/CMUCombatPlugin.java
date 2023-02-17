package cmu.plugins;

import cmu.misc.CombatUI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.input.InputEventAPI;

import java.util.List;

public class CMUCombatPlugin extends BaseEveryFrameCombatPlugin {

//    DebugGraphContainer dataGraph;

    @Override
    public void init(CombatEngineAPI engine) {
//        dataGraph = new DebugGraphContainer("test", 1200, 80f);
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        CombatUI.hasRendered = false;

//        if (!Global.getCombatEngine().isPaused()) dataGraph.increment(Global.getCombatEngine().getPlayerShip().getVelocity().x);
//        CMUtils.getGuiDebug().putContainer(CMUCombatPlugin.class, "ye", dataGraph);
    }
}
