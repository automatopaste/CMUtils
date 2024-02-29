package cmu.misc;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BoundsAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.ui.FontException;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.Iterator;

public class MiscUtils {
    private static final float UIScaling = Global.getSettings().getScreenScaleMult();
    public static final Color GREENCOLOR;
    public static final Color BLUCOLOR;
    private static LazyFont.DrawableString TODRAW14;
    static {
        GREENCOLOR = Global.getSettings().getColor("textFriendColor");
        BLUCOLOR = Global.getSettings().getColor("textNeutralColor");

        try {
            LazyFont fontdraw = LazyFont.loadFont("graphics/fonts/victor14.fnt");
            TODRAW14 = fontdraw.createText();
        } catch (FontException ignored) {
        }
    }

    private static final Vector2f PERCENTBARVEC1 = new Vector2f(21f, 0f); // Just 21 pixel of width of difference.
    private static final Vector2f PERCENTBARVEC2 = new Vector2f(50f, 58f);

    /**
     * @param entity entity to measure from, used to determine whether use more precise ship bounds
     * @param center vertex of angle
     * @param centerAngle the absolute angle at which the arc center faces
     * @param arcDeviation the distance to either side of the center angle, total arc length is arcDeviation * 2
     * @return is entity in arc
     */
    public static boolean isEntityInArc(CombatEntityAPI entity, Vector2f center, float centerAngle, float arcDeviation) {
        if (entity instanceof ShipAPI) {
            Vector2f point = getNearestPointOnShipBounds((ShipAPI) entity, center);
            return Misc.isInArc(centerAngle, arcDeviation * 2f, center, point);
        } else {
            return Misc.isInArc(centerAngle, arcDeviation * 2f, center, getNearestPointOnCollisionRadius(entity,  center));
        }
    }

    /**
     * @param a entity from
     * @param b entity to
     * @return vector exactly equal to distance and direction from a to b
     */
    public static Vector2f getVectorFromAToB(CombatEntityAPI a, CombatEntityAPI b) {
        return Vector2f.sub(b.getLocation(), a.getLocation(), new Vector2f());
    }

    /**
     * @param maxRange maximum distance
     * @param minRange minimum distance
     * @param center location of center
     * @return random point in a donut shape defined by parameters
     */
    public static Vector2f getRandomVectorInCircleRange(float maxRange, float minRange, Vector2f center) {
        float dist = (minRange + ((float) Math.random() * (maxRange - minRange)));
        Vector2f loc = new Vector2f(0f, dist);
        VectorUtils.rotate(loc, (float) Math.random() * 360f);
        Vector2f.add(loc, center, loc);
        return loc;
    }

    /**
     * @param maxRange maximum distance
     * @param minRange minimum distance
     * @param center location of center
     * @param mult multiplies the maximum distance from the minimum distance by a constant, useful for time varying
     * @return random point in a donut shape defined by parameters
     */
    public static Vector2f getRandomVectorInCircleRangeWithDistanceMult(float maxRange, float minRange, Vector2f center, float mult) {
        float dist = (minRange + (mult * (maxRange - minRange)));
        Vector2f loc = new Vector2f(0f, dist);
        VectorUtils.rotate(loc, (float) Math.random() * 360f);
        Vector2f.add(loc, center, loc);
        return loc;
    }

    public static Vector2f getNearestPointOnCollisionRadius(CombatEntityAPI entity, Vector2f point) {
        return MathUtils.getPointOnCircumference(
                entity.getLocation(),
                entity.getCollisionRadius(),
                VectorUtils.getAngle(entity.getLocation(), point)
        );
    }

    public static Vector2f getNearestPointOnRadius(Vector2f center, float radius, Vector2f point) {
        return MathUtils.getPointOnCircumference(
                center,
                radius,
                VectorUtils.getAngle(center, point)
        );
    }

    public static Vector2f getNearestPointOnShipBounds(ShipAPI ship, Vector2f point) {
        BoundsAPI bounds = ship.getExactBounds();
        if (bounds == null) {
            return getNearestPointOnCollisionRadius(ship, point);
        } else {
            Vector2f closest = ship.getLocation();
            float distSquared = 0f;
            for (BoundsAPI.SegmentAPI segment : bounds.getSegments()) {
                Vector2f tmpcp = MathUtils.getNearestPointOnLine(point, segment.getP1(), segment.getP2());
                float distSquaredTemp = MathUtils.getDistanceSquared(tmpcp, point);
                if (distSquaredTemp < distSquared) {
                    distSquared = distSquaredTemp;
                    closest = tmpcp;
                }
            }
            return closest;
        }
    }

    /**
     * @param value value from 0 to 1
     * @param ratio value from 0 to 1, when value equals ratio, returned alpha will be at the maximum value
     * @param minAlpha value from 0 to 1, minimum alpha value
     * @param maxAlpha value from 0 to 1, maximum alpha value
     * @return value from 0 to 1, alpha multiplier
     */
    public static float getSmoothAlpha(float value, float ratio, float minAlpha, float maxAlpha) {
        float alpha;
        if (value > 1 - ratio) { //second
            //alpha = (1f - value) / ratio;
            alpha = (((minAlpha - maxAlpha) / (1f - ratio)) * value) + maxAlpha - (((minAlpha - maxAlpha) / (1f - ratio)) * ratio);
        } else { //first
            //alpha = value / (1f - ratio);
            alpha = (((maxAlpha - minAlpha) / (ratio)) * value) + minAlpha;
        }

        MathUtils.clamp(alpha, minAlpha, maxAlpha); //just in case

        return alpha;
    }

    /**
     * @param value value from 0 to 1
     * @param ratio value from 0 to 1, when value equals ratio, returned alpha will be at the maximum value
     * @param minAlpha value from 0 to 1, minimum alpha value
     * @param maxAlpha value from 0 to 1, maximum alpha value
     * @return value from 0 to 1, alpha multiplier
     */
    public static float getSqrtAlpha(float value, float ratio, float minAlpha, float maxAlpha) {
        float alpha;
        if (value < 1 - ratio) {
            alpha = (float) ((maxAlpha - minAlpha) * (Math.sqrt(value / ratio))) + minAlpha;
        } else {
            alpha = (float) ((maxAlpha - minAlpha) * (Math.sqrt((-value + 1) / (-ratio + 1)))) + minAlpha;
        }

        MathUtils.clamp(alpha, minAlpha, maxAlpha); //just in case

        return alpha;
    }

    /**
     * @param value value from 0 to 1
     * @param time value for complete rotation
     * @param minAlpha value from 0 to 1, minimum alpha value
     * @param maxAlpha value from 0 to 1, maximum alpha value
     * @return value from 0 to 1, alpha multiplier
     */
    public static float getSinAlpha(float value, float time, float minAlpha, float maxAlpha) {
        float alpha = (float) (((maxAlpha - minAlpha) / 2f) * FastTrig.sin((Math.PI * 2f * value) / (time)) + ((maxAlpha - minAlpha) / 2f) + minAlpha);

        MathUtils.clamp(alpha, minAlpha, maxAlpha); //just in case

        return alpha;
    }
}
