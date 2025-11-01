package dev.emortal.bbstom.curve;

/**
 * Represents a 2D cubic Bezier curve defined by four control points.
 *
 * @author Michael Carleton
 */
public record CubicBezier2f(float x1, float y1, float cx1, float cy1, float cx2, float cy2, float x2, float y2) {

    public float getXAtParameter(final float t) {
        final float t1 = 1.0f - t;
        return x1 * t1 * t1 * t1 + 3 * cx1 * t * t1 * t1 + 3 * cx2 * t * t * t1 + x2 * t * t * t;
    }

    public float getYAtParameter(final float t) {
        final float t1 = 1.0f - t;
        return y1 * t1 * t1 * t1 + 3 * cy1 * t * t1 * t1 + 3 * cy2 * t * t * t1 + y2 * t * t * t;
    }

    public float solve(float t) {
        float l = 0;
        float u = 1;
        float s = (u + l) * 0.5f;

        float x = getXAtParameter(s);
        float y = getYAtParameter(s);
        while (Math.abs(t - x) > 0.001f) { // configure accuracy here
            if (t > x) {
                l = s;
            } else {
                u = s;
            }
            s = (u + l) * 0.5f;

            x = getXAtParameter(s);
            y = getYAtParameter(s);
        }
        return y;
    }

}