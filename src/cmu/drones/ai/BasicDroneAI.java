package cmu.drones.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lwjgl.util.vector.Vector2f;

public abstract class BasicDroneAI implements ShipAIPlugin {

    protected final ShipAPI drone;
    protected ShipAPI mothership;

    private final IntervalUtil alternateHostSearchLimit = new IntervalUtil(1f, 1f);
    private final IntervalUtil delayBeforeLanding = new IntervalUtil(getDelayBeforeLanding(), getDelayBeforeLanding());
    private boolean landing = false;
    private final DroneAIUtils.PDControl control = new DroneAIUtils.PDControl() {
        @Override
        public float getKpX() {
            return 10f;
        }
        @Override
        public float getKdX() {
            return 3f;
        }
        @Override
        public float getKpY() {
            return 6f;
        }
        @Override
        public float getKdY() {
            return 1.5f;
        }
    };

    public BasicDroneAI(ShipAPI drone, ShipAPI mothership) {
        this.drone = drone;
        this.mothership = mothership;

        drone.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.DRONE_MOTHERSHIP);
    }

    /**
     * Handle movement, rotation, landing and host mothership relocation
     * @param amount frame delta
     */
    @Override
    public void advance(float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();

        if (engine.isPaused() || drone == null) return;

        if (mothership == null || !engine.isEntityInPlay(mothership) || !mothership.isAlive()) {
            mothership = DroneAIUtils.getAlternateHost(drone, getSystemID(), getHostSearchRange());

            alternateHostSearchLimit.advance(amount);
            if (alternateHostSearchLimit.intervalElapsed()) {
                delete(engine);
            }
        } else {
            alternateHostSearchLimit.setElapsed(0f);

            Vector2f destLocation = getDestLocation(amount);
            if (destLocation == null) destLocation = new Vector2f(mothership.getLocation());
//            DroneAIUtils.move(drone, mothership, destLocation);
            DroneAIUtils.move2(destLocation, drone, control);

            float destFacing = getDestFacing(amount);
            DroneAIUtils.rotateToFacing(drone, destFacing, engine);

            if (isLanding() || isRecalling()) { // system wants this drone to land
                if (!landing) {
                    delayBeforeLanding.advance(amount);

                    if (delayBeforeLanding.intervalElapsed()) {
                        landing = true;
                        drone.beginLandingAnimation(mothership);
                    }
                } // drone deletion after finished landing handled in DroneSystem class
            } else {
                if (landing) drone.abortLanding();

                delayBeforeLanding.setElapsed(0f);
                landing = false;
            }
        }
    }

    protected void delete(CombatEngineAPI engine) {
        DroneAIUtils.deleteDrone(drone, engine);
    }

    protected abstract Vector2f getDestLocation(float amount);

    protected abstract float getDestFacing(float amount);

    protected abstract String getSystemID();

    protected abstract float getHostSearchRange();

    protected abstract boolean isLanding();

    protected abstract boolean isRecalling();

    protected abstract float getDelayBeforeLanding();

    @Override
    public void setDoNotFireDelay(float amount) {

    }

    @Override
    public void forceCircumstanceEvaluation() {

    }

    @Override
    public boolean needsRefit() {
        return false;
    }

    @Override
    public ShipwideAIFlags getAIFlags() {
        ShipwideAIFlags flags = new ShipwideAIFlags();
        flags.setFlag(ShipwideAIFlags.AIFlags.DRONE_MOTHERSHIP);
        return flags;
    }

    @Override
    public void cancelCurrentManeuver() {

    }

    @Override
    public ShipAIConfig getConfig() {
        return null;
    }
}