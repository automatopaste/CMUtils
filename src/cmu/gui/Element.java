package cmu.gui;

import com.fs.starfarer.api.input.InputEventAPI;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public interface Element {

    /**
     * Updates the GUI element before rendering
     * @param scale screen scale mult
     * @param loc absolute screen coordinates for scissor test
     * @param events input events
     * @return dimensions of rendered object
     */
    Vector2f update(float scale, Vector2f loc, List<InputEventAPI> events);

    /**
     * Conduct a render operation
     * @param scale screen scale mult
     * @param loc absolute screen coordinates for scissor test
     * @param events input events
     * @return dimensions of rendered object
     */
    Vector2f render(float scale, Vector2f loc, List<InputEventAPI> events);

    float getWidth();

    float getHeight();
}
