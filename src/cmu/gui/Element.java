package cmu.gui;

public interface Element {

    /**
     * Conduct a render operation
     */
    void render();

    void processInputEvents();

    float getWidth();

    float getHeight();
}
