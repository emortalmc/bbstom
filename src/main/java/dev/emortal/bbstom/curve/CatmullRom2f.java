package dev.emortal.bbstom.curve;

/**
 * Represents a 2D cubic Bezier curve defined by four control points.
 *
 * @author Michael Carleton
 */
public record CatmullRom2f(float x1, float y1, float cx1, float cy1, float cx2, float cy2, float x2, float y2) {

    // https://hawkesy.blogspot.com/2010/05/catmull-rom-spline-curve-implementation.html
    public float q(float p0, float p1, float p2, float p3, float t) {
        return 0.5f * ((2 * p1) +
                (p2 - p0) * t +
                (2*p0 - 5*p1 + 4*p2 - p3) * t * t +
                (3*p1 -p0 - 3 * p2 + p3) * t * t * t);
    }


    public float getXAtParameter(final float t) {
        return q(x1, cx1, cx2, x2, t);
    }

    public float getYAtParameter(final float t) {
        return q(y1, cy1, cy2, y2, t);
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