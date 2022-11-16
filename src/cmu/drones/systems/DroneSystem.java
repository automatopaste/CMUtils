package cmu.drones.systems;

import com.fs.starfarer.api.combat.ShipAPI;

public interface DroneSystem {

    ForgeTracker initDroneSystem(ShipAPI mothership);

    ForgeTracker getForgeTracker();

    void cycleDroneOrders();

    int getIndexForDrone(ShipAPI drone);
}
