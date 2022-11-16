package cmu.drones.systems;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import java.util.HashMap;
import java.util.Map;

public class SystemData {

    private SystemData() {}

    private static final String DATA_KEY = "DroneSystemDataKey";

    public static Map<String, Map<ShipAPI, DroneSystem>> getDroneSystems(CombatEngineAPI engine) {
        Object data = engine.getCustomData().get(DATA_KEY);
        Map<String, Map<ShipAPI, DroneSystem>> systems = data != null ? (Map<String, Map<ShipAPI, DroneSystem>>) data : new HashMap<String, Map<ShipAPI, DroneSystem>>();

        engine.getCustomData().put(DATA_KEY, systems);

        return systems;
    }

    public static void putDroneSystem(DroneSystem droneSystem, ShipAPI mothership, CombatEngineAPI engine) {
        String id = mothership.getHullSpec().getShipSystemId();

        Map<String, Map<ShipAPI, DroneSystem>> systems = getDroneSystems(engine);
        Map<ShipAPI, DroneSystem> systemInstances = systems.get(id);

        if (systemInstances == null) {
            systemInstances = new HashMap<>();
            systems.put(id, systemInstances);
        }

        systemInstances.put(mothership, droneSystem);
    }

    public static DroneSystem getDroneSystem(ShipAPI mothership, CombatEngineAPI engine) {
        String id = mothership.getHullSpec().getShipSystemId();

        Map<String, Map<ShipAPI, DroneSystem>> systems = getDroneSystems(engine);
        Map<ShipAPI, DroneSystem> systemInstances = systems.get(id);

        if (systemInstances == null) {
            systemInstances = new HashMap<>();
            systems.put(id, systemInstances);
        }

        return systemInstances.get(mothership);
    }

    public static Map<ShipAPI, DroneSystem> getSystemInstances(String id, CombatEngineAPI engine) {
        Map<String, Map<ShipAPI, DroneSystem>> systems = getDroneSystems(engine);

        Map<ShipAPI, DroneSystem> systemInstances = systems.get(id);
        if (systemInstances == null) {
            systemInstances = new HashMap<>();
            systems.put(id, systemInstances);
        }

        return systemInstances;
    }
}
