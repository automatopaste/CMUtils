package cmu.drones.systems;

import cmu.misc.CombatUI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Handles spawning, launching and reserve cooldown
 */
public class ForgeTracker extends BaseEveryFrameCombatPlugin {

    public static final String LAUNCH_DELAY_STAT_KEY = "PSE_launchDelayStatKey";
    public static final String REGEN_DELAY_STAT_KEY = "PSE_regenDelayStatKey";

    private final ForgeSpec spec; // spec object with basic values needed by all drone systems
    private final ShipAPI mothership; // host ship with drone system

    private final List<ShipAPI> deployed = new ArrayList<>(); // list of currently deployed drones

    private final IntervalUtil launchDelay; // tracks delay between spawning new drones
    private final DroneSystem droneSystem;
    private int reserveCount; // tracks number of drones stored in current ship
    private float forgeProgress; // cooldown tracker

    public ForgeTracker(ForgeSpec spec, ShipAPI mothership, DroneSystem droneSystem) {
        this.spec = spec;
        this.mothership = mothership;

        launchDelay = new IntervalUtil(spec.getLaunchDelay(), spec.getLaunchDelay());
        this.droneSystem = droneSystem;
        launchDelay.forceIntervalElapsed();

        reserveCount = spec.getMaxReserveCount();
        forgeProgress = spec.getForgeCooldown();
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        CombatEngineAPI engine = Global.getCombatEngine();

        if (mothership == null || !mothership.isAlive() || mothership.isHulk()) {
            engine.removePlugin(this);
            return;
        }

        //stat modifications
        float regenDelayStatMod = mothership.getMutableStats().getDynamic().getMod(REGEN_DELAY_STAT_KEY).computeEffective(1f);
        float launchDelayStatMod = mothership.getMutableStats().getDynamic().getMod(LAUNCH_DELAY_STAT_KEY).computeEffective(1f);
        // cooldown period
        float forgeCooldown = spec.getForgeCooldown() * regenDelayStatMod;

        boolean[] arr = new boolean[spec.getMaxDeployedDrones()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = i < deployed.size();
        }
        CombatUI.drawDroneSystemUI(
                mothership,
                arr,
                Math.max(deployed.size() - spec.getMaxDeployedDrones(), 0),
                "deployed",
                "forge cooldown",
                forgeProgress / forgeCooldown,
                reserveCount,
                spec.getMaxReserveCount(),
                droneSystem.getActiveDroneOrder(),
                droneSystem.getNumDroneOrders(),
                droneSystem.getActiveDroneOrderTitle(),
                droneSystem.getIconForActiveState()
        );

        if (engine.isPaused()) return;

        List<ShipAPI> toRemove = new ArrayList<>();
        for (ShipAPI drone : deployed) {
            if (!engine.isEntityInPlay(drone) || !drone.isAlive() || drone.isHulk()) {
                engine.removeEntity(drone);
                toRemove.add(drone);
            } else if (drone.isFinishedLanding()) {
                reserveCount++;
                engine.removeEntity(drone);
                toRemove.add(drone);
            }
        }
        deployed.removeAll(toRemove);

        if (forgeProgress > 0f) {
            if (reserveCount < spec.getMaxReserveCount()) {
                forgeProgress -= amount;
            }
        } else {
            if (reserveCount < spec.getMaxReserveCount()) {
                reserveCount++;

                forgeProgress = forgeCooldown;
            } else {
                forgeProgress = 0f;
            }
        }

        //check if new drone can be spawned
        if (deployed.size() < spec.getMaxDeployedDrones() && spec.canDeploy() && reserveCount > 0) {
            if (launchDelay.getElapsed() >= launchDelay.getIntervalDuration()) {
                launchDelay.setElapsed(0);
                spawnDrone(engine);

                //subtract from reserve drone count on launch
                reserveCount--;
            }

            launchDelay.advance(amount / launchDelayStatMod);
        }
    }

    public void spawnDrone(CombatEngineAPI engine) {
        CombatFleetManagerAPI manager = engine.getFleetManager(mothership.getOwner());
        boolean suppress = manager.isSuppressDeploymentMessages();
        manager.setSuppressDeploymentMessages(true);

        Vector3f launch = getLaunchLocation();

        FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.FIGHTER_WING, spec.getDroneVariant());
        ShipAPI drone = manager.spawnFleetMember(member, new Vector2f(launch.x, launch.y), launch.z, 0f);

        drone.setAnimatedLaunch();

        Vector2f vel = new Vector2f(mothership.getVelocity());
        Vector2f n = new Vector2f(spec.getLaunchSpeed(), 0f);
        VectorUtils.rotate(n, launch.z);
        drone.getVelocity().set(Vector2f.add(vel, n, vel));

        drone.setShipAI(spec.initNewDroneAIPlugin(drone, mothership));

        drone.setOwner(mothership.getOwner());

        manager.setSuppressDeploymentMessages(suppress);

        droneSystem.droneSpawnCallback(drone, this, droneSystem);

        deployed.add(drone);
    }

    public Vector3f getLaunchLocation() {
        Vector2f loc = null;
        float facing = 0f;

        List<WeaponSlotAPI> weapons = mothership.getHullSpec().getAllWeaponSlotsCopy();
        if (!weapons.isEmpty()) {
            //these aren't actually bays, but since launch bays have no way of getting their location system mounts are used
            List<WeaponSlotAPI> bays = new ArrayList<>();
            for (WeaponSlotAPI weapon : weapons) {
                if (weapon.isSystemSlot()) {
                    bays.add(weapon);
                }
            }

            if (!bays.isEmpty()) {
                //pick random entry in bay list
                Random index = new Random();
                WeaponSlotAPI w = bays.get(index.nextInt(bays.size()));
                loc = w.computePosition(mothership);
                facing = w.getAngle();
            }
        }

        if (loc == null) {
            loc = mothership.getLocation();
            facing = mothership.getFacing();
        }

        return new Vector3f(loc.x, loc.y, facing);
    }

    public ShipAPI getMothership() {
        return mothership;
    }

    public List<ShipAPI> getDeployed() {
        return deployed;
    }

    public int getReserveCount() {
        return reserveCount;
    }

    public ForgeSpec getSpec() {
        return spec;
    }
}
