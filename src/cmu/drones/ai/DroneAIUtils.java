package cmu.drones.ai;

import cmu.CMUtils;
import cmu.drones.systems.DroneSystem;
import cmu.drones.systems.SystemData;
import cmu.misc.MiscUtils;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import org.jetbrains.annotations.Nullable;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class DroneAIUtils {

    /**
     * Movement algorithm using a PD controller damping system. Integral is not necessary because inherent
     * offset is zero
     * @param dest target location
     * @param drone drone that is moving
     * @param control controller object with defined values
     */
    public static void move(Vector2f dest, ShipAPI drone, PDControl control) {
        Vector2f d = Vector2f.sub(dest, drone.getLocation(), new Vector2f());
        VectorUtils.rotate(d, 90f - drone.getFacing());

        float ex = d.x;
        float rex = (ex - control.lx) / Global.getCombatEngine().getElapsedInLastFrame();
        float ax = control.getKpX() * ex + control.getKdX() * rex;
        if (ax > 0f) drone.giveCommand(ShipCommand.STRAFE_RIGHT, null, 0);
        else drone.giveCommand(ShipCommand.STRAFE_LEFT, null, 0);
        control.lx = ex;

        float ey = d.y;
        float rey = (ey - control.ly) / Global.getCombatEngine().getElapsedInLastFrame();
        float ay = control.getKpY() * ey + control.getKdY() * rey;
        if (ay > 0f) drone.giveCommand(ShipCommand.ACCELERATE, null, 0);
        else drone.giveCommand(ShipCommand.ACCELERATE_BACKWARDS, null, 0);
        control.ly = ey;
    }

    /**
     * Values should be tweaked based on the acceleration stats of the controlled drone.
     */
    public abstract static class PDControl {
        public abstract float getKp();
        public abstract float getKd();

        public abstract float getRp();

        public abstract float getRd();

        public static final float STRAFE_RATIO = 0.5f;

        public float lx = 0f;
        public float ly = 0f;
        public float lr = 0f;

        public float getKpX() {
            return getKp();
        }

        public float getKdX() {
            return getKd();
        }

        public float getKpY() {
            return getKp() * STRAFE_RATIO;
        }

        public float getKdY() {
            return getKd() * STRAFE_RATIO;
        }
    }

    public static void rotate(float target, ShipAPI drone, PDControl control) {
        float er = target - drone.getFacing();
        if (er > 180f) er -= 360f;
        else if (er < -180f) er += 360f;

        float rer = (er - control.lr) / Global.getCombatEngine().getElapsedInLastFrame();
        float ar = control.getRp() * er + control.getRd() * rer;
        if (ar > 0f) drone.giveCommand(ShipCommand.TURN_LEFT, null, 0);
        else if (ar < 0f) drone.giveCommand(ShipCommand.TURN_RIGHT, null, 0);
        control.lr = er;

        //CMUtils.getGuiDebug().putText(DroneAIUtils.class, "rotation", er + "");
    }

    @Nullable
    public static CombatEntityAPI getEnemyTarget(ShipAPI mothership, ShipAPI drone, float weaponRange, boolean ignoreMissiles, boolean ignoreFighters, boolean ignoreShips, float targetingArcDeviation) {
        //GET NEARBY OBJECTS TO SHOOT AT priority missiles > fighters > ships
        Vector2f d = Vector2f.sub(drone.getLocation(), mothership.getLocation(), new Vector2f());
        float facing = VectorUtils.getFacing(d);

        MissileAPI droneTargetMissile = null;
        if (!ignoreMissiles) {
            //get missile close to mothership
            List<MissileAPI> enemyMissiles = AIUtils.getNearbyEnemyMissiles(mothership, weaponRange);
            float tracker = Float.MAX_VALUE;
            for (MissileAPI missile : enemyMissiles) {
                if (MiscUtils.isEntityInArc(missile, drone.getLocation(), facing, targetingArcDeviation)) {
                    continue;
                }

                float distance = MathUtils.getDistanceSquared(missile, drone);
                if (distance < tracker) {
                    tracker = distance;
                    droneTargetMissile = missile;
                }
            }
        }

        ShipAPI droneTargetShip = null;
        if (!ignoreShips) {
            if (mothership.getShipTarget() != null) {
                droneTargetShip = mothership.getShipTarget();
            } else {
                //get non-fighter ship close to mothership
                List<ShipAPI> enemyShips = AIUtils.getNearbyEnemies(drone, weaponRange);
                float tracker = Float.MAX_VALUE;
                for (ShipAPI enemyShip : enemyShips) {
                    if (enemyShip.isFighter()) {
                        continue;
                    }

                    //check if there is a friendly ship in the way
                    boolean areFriendliesInFiringArc = false;
                    float relAngle = VectorUtils.getFacing(Vector2f.sub(enemyShip.getLocation(), drone.getLocation(), new Vector2f()));
                    for (ShipAPI ally : AIUtils.getNearbyAllies(drone, weaponRange)) {
                        if (MiscUtils.isEntityInArc(ally, drone.getLocation(), relAngle, 20f)) {
                            if (MathUtils.getDistanceSquared(enemyShip, drone) > MathUtils.getDistanceSquared(ally, drone)) {
                                areFriendliesInFiringArc = true;
                                break;
                            }
                        }
                    }
                    if (areFriendliesInFiringArc) {
                        continue;
                    }

                    //can only match similar facing to host ship for balancing
                    if (!MiscUtils.isEntityInArc(enemyShip, drone.getLocation(), facing, targetingArcDeviation)) {
                        continue;
                    }

                    float distance = MathUtils.getDistanceSquared(enemyShip, drone);
                    if (distance < tracker) {
                        tracker = distance;
                        droneTargetShip = enemyShip;
                    }
                }
            }
        }

        ShipAPI droneTargetFighter = null;
        if (!ignoreFighters) {
            //get fighter close to drone
            List<ShipAPI> enemyShips = AIUtils.getNearbyEnemies(drone, weaponRange);
            float tracker = Float.MAX_VALUE;
            for (ShipAPI enemyShip : enemyShips) {
                if (!enemyShip.isFighter()) {
                    continue;
                }

                if (!MiscUtils.isEntityInArc(enemyShip, drone.getLocation(), facing, targetingArcDeviation)) {
                    continue;
                }

                float distance = MathUtils.getDistanceSquared(enemyShip, drone);
                if (distance < tracker) {
                    tracker = distance;
                    droneTargetFighter = enemyShip;
                }
            }
        }

        //PRIORITISE TARGET, SET LOCATION
        if (droneTargetMissile != null) {
            return droneTargetMissile;
        } else if (droneTargetFighter != null) {
            return droneTargetFighter;
        } else return droneTargetShip;
    }

    public static boolean areFriendliesBlockingArc(ShipAPI drone, CombatEntityAPI target, float focusWeaponRange) {
        for (ShipAPI ally : AIUtils.getNearbyAllies(drone, focusWeaponRange)) {
            if (ally.getCollisionClass() == CollisionClass.FIGHTER || ally.getCollisionClass() == CollisionClass.NONE) {
                continue;
            }

            float d2 = MathUtils.getDistanceSquared(ally, drone);
            if (MathUtils.getDistanceSquared(target, drone) < d2) {
                continue;
            }

            if (CollisionUtils.getCollisionPoint(drone.getLocation(), target.getLocation(), ally) != null) {
                return true;
            }
        }
        return false;
    }

    public static ShipAPI getAlternateHost(ShipAPI drone, String systemID, float range) {
        CombatEngineAPI engine = Global.getCombatEngine();
        List<ShipAPI> allies = AIUtils.getNearbyAllies(drone, range);
        if (allies.isEmpty()) {
            return null;
        }

        float dist = Float.MAX_VALUE;
        ShipAPI host = null;

        Map<ShipAPI, DroneSystem> compatible = SystemData.getSystemInstances(systemID, engine);

        for (ShipAPI ship : compatible.keySet()) {
            float temp = MathUtils.getDistanceSquared(drone, ship);
            if (temp < dist) {
                dist = temp;
                host = ship;
            }
        }

        return host;
    }

    public static void deleteDrone(ShipAPI drone, CombatEngineAPI engine) {
        engine.spawnExplosion(drone.getLocation(), drone.getVelocity(), Color.WHITE, drone.getMass(), 1.5f);
        engine.removeEntity(drone);
//        engine.applyDamage(
//                drone,
//                drone.getLocation(),
//                10000f,
//                DamageType.HIGH_EXPLOSIVE,
//                0f,
//                true,
//                false,
//                null,
//                false
//        );
    }
}
