package cmu.shaders.particles;

import com.fs.starfarer.api.combat.CombatEngineLayers;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.awt.*;

public class PolygonParticle extends BaseParticle {

    public final int poly;
    public float edgeWidth;
    public CombatEngineLayers layer;
    public Color edgeColor;
    public float alphaMult;

    public PolygonParticle(Vector2f location, PolygonParams params) {
        super(location, params);

        this.poly = params.poly;
        this.edgeWidth = params.edgeWidth;
        this.layer = params.layer;
        this.edgeColor = params.edgeColor;
        this.alphaMult = params.alphaMult;
    }

    public static class PolygonParams extends ParticleParams {
        public final int poly;
        public float edgeWidth = 0.1f;
        public CombatEngineLayers layer;
        public Color edgeColor;
        public float alphaMult;

        public PolygonParams(int poly, CombatEngineLayers layer, Color edgeColor, float alphaMult) {
            this.poly = poly;
            this.layer = layer;
            this.edgeColor = edgeColor;
            this.alphaMult = alphaMult;
        }
    }

    @Override
    public Matrix4f getModel(Matrix4f in) {
        Matrix4f matrix = new Matrix4f(in);

        matrix.translate(new Vector3f(location.x, location.y, 0f));
        matrix.rotate((float) Math.toRadians(angle), new Vector3f(0f, 0f, 1f));

//        Vector2f offset = new Vector2f(size.x * 0.5f, size.y * 0.5f);

//        matrix.translate(new Vector3f(-offset.x, -offset.y, 0f));
        matrix.scale(new Vector3f(size.x, size.x, 1f));

        return matrix;
    }
}
