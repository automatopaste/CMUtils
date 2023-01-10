package cmu.gui;

import com.fs.starfarer.api.input.InputEventAPI;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public interface Element {

    /**
     * Conduct a render operation
     * @return dimensions of rendered object
     * @param scale screen scale mult
     * @param loc absolute screen coordinates for scissor test
     */
    Vector2f render(float scale, Vector2f loc, List<InputEventAPI> events);

    float getWidth();

    float getHeight();
}
