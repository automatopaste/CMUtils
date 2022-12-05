package cmu.shaders.particles;

import com.fs.starfarer.api.combat.CombatEngineLayers;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class PolygonParticle extends BaseParticle {

    public final int poly;
    public CombatEngineLayers layer;
    public Color edgeColor;
    public float alphaMult;

    public PolygonParticle(Vector2f location, PolygonParams params) {
        super(location, params);

        this.poly = params.poly;
        this.layer = params.layer;
        this.edgeColor = params.edgeColor;
        this.alphaMult = params.alphaMult;
    }

    public static class PolygonParams extends ParticleParams {
        public final int poly;
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
}
