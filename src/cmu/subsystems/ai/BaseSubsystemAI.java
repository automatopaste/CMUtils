package cmu.subsystems.ai;

public interface BaseSubsystemAI {
    /**
     * Returns the desired subsystem AI type.
     * @return
     */
    BaseSubsystemAI getAI();

    /**
     * Initialise the AI script. Will be called regardless of ship being in player control initially.
     */
    void aiInit();

    /**
     * Used to update any system AI decisions. Called after subsystem logic is advanced. Will not be called under player
     * control.
     * @param amount frame delta
     */
    void aiUpdate(float amount);
}
