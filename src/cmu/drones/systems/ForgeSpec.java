package cmu.drones.systems;

import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;

public interface ForgeSpec {

    int getMaxDeployedDrones();

    float getForgeCooldown();

    float getLaunchDelay();

    float getLaunchSpeed();

    String getDroneVariant();

    int getMaxReserveCount();

    boolean canDeploy();

    ShipAIPlugin initNewDroneAIPlugin(ShipAPI drone, ShipAPI mothership);
}
