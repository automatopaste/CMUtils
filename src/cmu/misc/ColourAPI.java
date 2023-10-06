package cmu.misc;

import org.lwjgl.util.vector.Vector4f;

import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class ColourAPI {

    public float r;
    public float g;
    public float b;
    public float a;

    public ColourAPI(int r, int g, int b, int a) {
        this.r = r * 0.00392156863f;
        this.g = g * 0.00392156863f;
        this.b = b * 0.00392156863f;
        this.a = a * 0.00392156863f;
    }

    public ColourAPI(float r, float g, float b, float a) {
        this.r = (byte) (r * 0xFF);
        this.g = (byte) (g * 0xFF);
        this.b = (byte) (b * 0xFF);
        this.a = (byte) (a * 0xFF);
    }

    public Color get() {
        return new Color(r, g, b, a);
    }

    public void store(FloatBuffer buf) {
        buf.put(r);
        buf.put(g);
        buf.put(b);
        buf.put(a);
    }

    public Vector4f asVec4() {
        return new Vector4f(r, g, b, a);
    }

    public ColourAPI multiply(ColourAPI c) {
        r *= c.r;
        g *= c.g;
        b *= c.b;
        a *= c.a;
        return this;
    }

    public ColourAPI add(ColourAPI c) {
        r += c.r;
        g += c.g;
        b += c.b;
        a += c.a;
        return this;
    }

    public ColourAPI clamp() {
        r = Math.min(1f, Math.max(0f, r));
        g = Math.min(1f, Math.max(0f, g));
        b = Math.min(1f, Math.max(0f, b));
        a = Math.min(1f, Math.max(0f, a));
        return this;
    }
}
