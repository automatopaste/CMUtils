package cmu.drones.systems;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;

public interface DroneSystem {

    ForgeTracker initDroneSystem(ShipAPI mothership);

    ForgeTracker getForgeTracker();

    void cycleDroneOrders();

    int getIndexForDrone(ShipAPI drone);

    int getNumDroneOrders();

    int getActiveDroneOrder();

    String getActiveDroneOrderTitle();

    SpriteAPI getIconForActiveState();

    void droneSpawnCallback(ShipAPI drone, ForgeTracker forgeTracker, DroneSystem droneSystem);

    SpriteAPI getSpatialUIGraphic();
}
