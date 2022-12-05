package cmu.drones.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import org.lwjgl.input.Keyboard;

import java.util.List;

public abstract class DroneShipsystem extends BaseShipSystemScript implements DroneSystem {

    private ForgeTracker forgeTracker;
    private boolean tracker = false;
    private boolean once = true;
    private SpriteAPI spatialUIGraphic;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (once) {
            ShipAPI mothership = (ShipAPI) stats.getEntity();
            forgeTracker = initDroneSystem(mothership);
            Global.getCombatEngine().addPlugin(forgeTracker);

            SystemData.putDroneSystem(this, mothership, Global.getCombatEngine());
            once = false;
        }

        boolean activate = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_USE_SYSTEM")));
        if (activate && !tracker) cycleDroneOrders();

        tracker = activate;

        spatialUIGraphic = Global.getSettings().getSprite("ui", "spatial");
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {

    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (forgeTracker == null) return null;

        int reserve = forgeTracker.getReserveCount();

        if (reserve < forgeTracker.getSpec().getMaxReserveCount()) {
            return "FORGING";
        } else if (reserve > forgeTracker.getSpec().getMaxReserveCount()) {
            return "RESERVE OVERFULL";
        } else {
            return "RESERVE FULL";
        }
    }

    @Override
    public int getIndexForDrone(ShipAPI drone) {
        List<ShipAPI> deployed = getForgeTracker().getDeployed();
        for (int i = 0; i < deployed.size(); i++) {
            if (drone == deployed.get(i)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public ForgeTracker getForgeTracker() {
        return forgeTracker;
    }

    @Override
    public void droneSpawnCallback(ShipAPI drone, ForgeTracker forgeTracker, DroneSystem droneSystem) {

    }

    @Override
    public SpriteAPI getSpatialUIGraphic() {
        return spatialUIGraphic;
    }
}
