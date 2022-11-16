package cmu.drones.ai;

import cmu.CMUtils;
import cmu.drones.systems.DroneSystem;
import cmu.drones.systems.SystemData;
import cmu.misc.MiscUtils;
import cmu.plugins.GUIDebug;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;
import java.util.Map;

public class DroneAIUtils {

    public static void move(ShipAPI drone, ShipAPI mothership, Vector2f movementTargetLocation) {
        //The bones of the movement AI are below, all it needs is a target vector location to move to

        //account for ship velocity
        Vector2f.add(movementTargetLocation, (Vector2f) new Vector2f(mothership.getVelocity()).scale(Global.getCombatEngine().getElapsedInLastFrame()), movementTargetLocation);

        //GET USEFUL VALUES
        float angleFromDroneToTargetLocation = VectorUtils.getAngle(drone.getLocation(), movementTargetLocation); //ABSOLUTE 360 ANGLE

        float droneVelocityAngle = VectorUtils.getFacing(drone.getVelocity()); //ABSOLUTE 360 ANGLE

        float rotationFromFacingToLocationAngle = MathUtils.getShortestRotation(drone.getFacing(), angleFromDroneToTargetLocation); //ROTATION ANGLE
        float rotationFromVelocityToLocationAngle = MathUtils.getShortestRotation(droneVelocityAngle, angleFromDroneToTargetLocation); //ROTATION ANGLE

        float distanceToTargetLocation = MathUtils.getDistance(drone.getLocation(), movementTargetLocation); //DISTANCE

        //damping scaling based on ship speed (function y = -x + 2 where x is 0->1)
        //float damping = (-drone.getLaunchingShip().getVelocity().length() / drone.getLaunchingShip().getMaxSpeedWithoutBoost()) + 2f;

        //FIND DISTANCE THAT CAN BE DECELERATED FROM CURRENT SPEED TO ZERO s = v^2 / 2a
        float speedSquared = drone.getVelocity().lengthSquared();
        float decelerationDistance = speedSquared / (2 * drone.getDeceleration());

        //DO LARGE MOVEMENT IF OVER DISTANCE THRESHOLD
        if (distanceToTargetLocation >= decelerationDistance) {
            rotationFromFacingToLocationAngle = Math.round(rotationFromFacingToLocationAngle);

            //COURSE CORRECTION
            drone.getVelocity().set(VectorUtils.rotate(drone.getVelocity(), rotationFromVelocityToLocationAngle * 0.5f));

            //accelerate forwards or backwards
            if (90f > rotationFromFacingToLocationAngle && rotationFromFacingToLocationAngle > -90f
            ) { //between 90 and -90 is an acute angle therefore in front
                drone.giveCommand(ShipCommand.ACCELERATE, null, 0);
            } else if ((180f >= rotationFromFacingToLocationAngle && rotationFromFacingToLocationAngle > 90f) || (-90f > rotationFromFacingToLocationAngle && rotationFromFacingToLocationAngle >= -180f)
            ) { //falls between 90 to 180 or -90 to -180, which should be obtuse and thus relatively behind
                drone.giveCommand(ShipCommand.ACCELERATE_BACKWARDS, null, 0);
            }

            //strafe left or right
            if (180f > rotationFromFacingToLocationAngle && rotationFromFacingToLocationAngle > 0f) { //between 0 and 180 (i.e. left)
                drone.giveCommand(ShipCommand.STRAFE_LEFT, null, 0);
            } else if (0f > rotationFromFacingToLocationAngle && rotationFromFacingToLocationAngle > -180f) { //between 0 and -180 (i.e. right)
                drone.giveCommand(ShipCommand.STRAFE_RIGHT, null, 0);
            }
        } else {
            //COURSE CORRECTION
            drone.getVelocity().set(VectorUtils.rotate(drone.getVelocity(), rotationFromVelocityToLocationAngle));
        }

        //DECELERATE IF IN THRESHOLD DISTANCE OF TARGET
        if (distanceToTargetLocation <= decelerationDistance) {
            drone.giveCommand(ShipCommand.DECELERATE, null, 0);

            float frac = distanceToTargetLocation / decelerationDistance;
            frac = (float) Math.sqrt(frac);
            //drone.getVelocity().set((Vector2f) drone.getVelocity().scale(frac));

            if (frac <= 0.25f) {
                drone.getVelocity().set(mothership.getVelocity());
            } else {
                drone.getVelocity().set((Vector2f) drone.getVelocity().scale(frac));
            }
        }
    }

    /**
     * Perfect movement algorithm using a PD controller damping system. Integral is not necessary because inherent
     * offset is zero
     * @param dest target location
     * @param drone drone that is moving
     * @param control controller object with defined values
     */
    public static void move2(Vector2f dest, ShipAPI drone, PDControl control) {
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
        public abstract float getKpX();
        public abstract float getKdX();
        public abstract float getKpY();
        public abstract float getKdY();
        public float lx = 0f;
        public float ly = 0f;
    }

    public static void snapToLocation(ShipAPI drone, Vector2f target) {
        drone.getLocation().set(target);
    }

    public static void rotateToTarget(ShipAPI mothership, ShipAPI drone, Vector2f targetedLocation) {
        CombatEngineAPI engine = Global.getCombatEngine();
        //float droneFacing = drone.getFacing();

        //FIND ANGLE THAT CAN BE DECELERATED FROM CURRENT ANGULAR VELOCITY TO ZERO theta = v^2 / 2 (not actually used atm)
        //float decelerationAngle = (float) (Math.pow(drone.getAngularVelocity(), 2) / (2 * drone.getTurnDeceleration()));


        //point at target, if that doesn't exist then point in direction of mothership facing
        //float rotationAngleDelta;
        if (targetedLocation != null) {
            //GET ABSOLUTE ANGLE FROM DRONE TO TARGETED LOCATION
            Vector2f droneToTargetedLocDir = VectorUtils.getDirectionalVector(drone.getLocation(), targetedLocation);
            float droneAngleToTargetedLoc = VectorUtils.getFacing(droneToTargetedLocDir); //ABSOLUTE 360 ANGLE

            rotateToFacing(drone, droneAngleToTargetedLoc, engine);
        } else {
            rotateToFacing(drone, mothership.getFacing(), engine);
        }
    }

    public static void rotateToFacing(ShipAPI drone, float absoluteFacingTargetAngle, CombatEngineAPI engine) {
        float droneFacing = drone.getFacing();
        float angvel = drone.getAngularVelocity();
        float rotationAngleDelta = MathUtils.getShortestRotation(droneFacing, absoluteFacingTargetAngle);

        //FIND ANGLE THAT CAN BE DECELERATED FROM CURRENT ANGULAR VELOCITY TO ZERO theta = v^2 / 2
        float decelerationAngleAbs = (angvel * angvel) / (2 * drone.getTurnDeceleration());

        float accel = 0f;
        if (rotationAngleDelta < 0f) {
            if (-decelerationAngleAbs < rotationAngleDelta) {
                accel += drone.getTurnDeceleration() * engine.getElapsedInLastFrame();
            } else {
                accel -= drone.getTurnAcceleration() * engine.getElapsedInLastFrame();
            }
        } else if (rotationAngleDelta > 0f) {
            if (decelerationAngleAbs > rotationAngleDelta) {
                accel -= drone.getTurnDeceleration() * engine.getElapsedInLastFrame();
            } else {
                accel += drone.getTurnAcceleration() * engine.getElapsedInLastFrame();
            }
        }

        angvel += accel;

        MathUtils.clamp(angvel, -drone.getMaxTurnRate(), drone.getMaxTurnRate());

        drone.setAngularVelocity(angvel);
    }

    public static void rotateToFacingJerky(ShipAPI drone, float targetAngle) {
        float delta = MathUtils.getShortestRotation(drone.getFacing(), targetAngle);
        drone.setFacing(drone.getFacing() + delta * 0.1f);
    }

    public static void attemptToLand(ShipAPI mothership, ShipAPI drone, float amount, IntervalUtil delayBeforeLandingTracker, CombatEngineAPI engine) {
        delayBeforeLandingTracker.advance(amount);
        boolean isPlayerShip = mothership.equals(engine.getPlayerShip());

        if (drone.isLanding()) {
            delayBeforeLandingTracker.setElapsed(0);
            if (isPlayerShip) {
                engine.maintainStatusForPlayerShip("PSE_STATUS_KEY_DRONE_LANDING_STATE", "graphics/icons/hullsys/drone_pd_high.png", "LANDING STATUS", "LANDING... ", false);
            }
        } else {
            float round = Math.round((delayBeforeLandingTracker.getIntervalDuration() - delayBeforeLandingTracker.getElapsed()) * 100) / 100f;
            if (isPlayerShip) {
                engine.maintainStatusForPlayerShip("PSE_STATUS_KEY_DRONE_LANDING_STATE", "graphics/icons/hullsys/drone_pd_high.png", "LANDING STATUS", "LANDING IN " + round, false);
            }
        }

        if (delayBeforeLandingTracker.intervalElapsed()) {
            drone.beginLandingAnimation(mothership);
        }
    }

    public static void attemptToLandAsExtra(ShipAPI mothership, ShipAPI drone) {
        if (!drone.isLanding() && MathUtils.getDistance(drone, mothership) < mothership.getCollisionRadius()) {
            drone.beginLandingAnimation(mothership);
        }
    }

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

                float distance = MathUtils.getDistance(missile, drone);
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
                            if (MathUtils.getDistance(enemyShip, drone) > MathUtils.getDistance(ally, drone)) {
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

                    float distance = MathUtils.getDistance(enemyShip, drone);
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

                float distance = MathUtils.getDistance(enemyShip, drone);
                if (distance < tracker) {
                    tracker = distance;
                    droneTargetFighter = enemyShip;
                }
            }
        }

        //PRIORITISE TARGET, SET LOCATION
        CombatEntityAPI target;
        if (droneTargetMissile != null) {
            target = droneTargetMissile;
        } else if (droneTargetFighter != null) {
            target = droneTargetFighter;
        } else target = droneTargetShip;
        return target;
    }

    public static boolean areFriendliesBlockingArc(ShipAPI drone, CombatEntityAPI target, float focusWeaponRange) {
        for (ShipAPI ally : AIUtils.getNearbyAllies(drone, focusWeaponRange)) {
            if (ally.getCollisionClass() == CollisionClass.FIGHTER || ally.getCollisionClass() == CollisionClass.NONE) {
                continue;
            }

            float distance = MathUtils.getDistance(ally, drone);
            if (MathUtils.getDistance(target, drone) < distance) {
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
        //engine.removeEntity(drone);
        //engine.spawnExplosion(drone.getLocation(), drone.getVelocity(), DRONE_EXPLOSION_COLOUR, drone.getMass(), 1.5f);
        engine.applyDamage(
                drone,
                drone.getLocation(),
                10000f,
                DamageType.HIGH_EXPLOSIVE,
                0f,
                true,
                false,
                null,
                false
        );
    }
}
