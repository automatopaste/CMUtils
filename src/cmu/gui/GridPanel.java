package cmu.gui;

import com.fs.starfarer.api.input.InputEventAPI;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;

public class GridPanel implements Element {
    @Override
    public Vector2f update(float scale, Vector2f loc, List<InputEventAPI> events) {
        return null;
    }

    @Override
    public Vector2f render(float scale, Vector2f loc, List<InputEventAPI> events) {
        return null;
    }

    @Override
    public float getWidth() {
        return 0;
    }

    @Override
    public float getHeight() {
        return 0;
    }

    public static class GridParams {
        public boolean update = false;
        public float x = 100f;
        public float y = 100f;
        public float edgePad = 1f;
        public float gridPad = 2f;
        public Color color = Color.WHITE;
        public boolean conformToListSize = false;
    }
}
