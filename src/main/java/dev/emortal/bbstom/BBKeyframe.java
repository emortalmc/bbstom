package dev.emortal.bbstom;

import com.alibaba.fastjson2.annotation.JSONCreator;
import dev.emortal.bbstom.curve.CatmullRom2f;
import dev.emortal.bbstom.curve.CubicBezier2f;
import org.joml.Vector3f;

@SuppressWarnings("unused")
public record BBKeyframe(String uuid, String channel, float time, BBDataPoint[] data_points, Interpolation interpolation,
                         Vector3f bezier_left_time, Vector3f bezier_right_time, Vector3f bezier_left_value,
                         Vector3f bezier_right_value) {
    @JSONCreator
    public BBKeyframe(String uuid, String channel, float time, BBDataPoint[] data_points, String interpolation,
                      float[] bezier_left_time, float[] bezier_right_time,
                      float[] bezier_left_value, float[] bezier_right_value) {
        this(
                uuid,
                channel,
                time,
                data_points,
                Interpolation.valueOf(interpolation.toUpperCase()),
                bezier_left_time == null ? new Vector3f() : new Vector3f(bezier_left_time),
                bezier_right_time == null ? new Vector3f() : new Vector3f(bezier_right_time),
                bezier_left_value == null ? new Vector3f() : new Vector3f(bezier_left_value),
                bezier_right_value == null ? new Vector3f() : new Vector3f(bezier_right_value)
        );
    }

    public Vector3f interpolate(BBKeyframe k0, BBKeyframe k2, BBKeyframe k3, float alpha) {
        BBKeyframe k1 = this;

        Vector3f p1 = k1.data_points[0].vector(alpha);

        float length = k2.time - k1.time;
        if (length < 0.001f) return p1;

        Vector3f p2 = k2.data_points[0].vector(alpha);

        float relTime = (alpha - k1.time) / length;

        switch (Interpolation.getInterpolation(k1.interpolation, k2.interpolation)) {
            case Interpolation.LINEAR -> {
                return p1.lerp(p2, relTime);
            }
            case Interpolation.CATMULLROM -> {
                // if p0 (beforePlus) or p3 (afterPlus) not exist, use:
                // p0 = 2 * p1 - p2;
                // p3 = 2 * p2 - p1;
                Vector3f p0 = k0 == null ? p1.mul(2, new Vector3f()).sub(p2) : k0.data_points[0].vector(alpha);
                Vector3f p3 = k3 == null ? p2.mul(2, new Vector3f()).sub(p1) : k3.data_points[0].vector(alpha);
                float t0 = k0 == null ? 2 * k1.time - k2.time : k0.time;
                float t3 = k3 == null ? 2 * k2.time - k1.time : k3.time;

                Vector3f newVec = new Vector3f();
                for (int i = 0; i < 3; i++) {
                    newVec.setComponent(i, new CatmullRom2f(
                            t0, p0.get(i),
                            k1.time, p1.get(i),
                            k2.time, p2.get(i),
                            t3, p3.get(i)
                    ).solve(alpha));
                }
                return newVec;
            }
            case Interpolation.BEZIER -> {
                Vector3f newVec = new Vector3f();
                for (int i = 0; i < 3; i++) {
                    newVec.setComponent(i, new CubicBezier2f(
                            k1.time, p1.get(i),
                            k1.time + k1.bezier_right_time.get(i), p1.get(i) + k1.bezier_right_value.get(i),
                            k2.time + k2.bezier_left_time.get(i), p2.get(i) + k2.bezier_left_value.get(i),
                            k2.time, p2.get(i)
                    ).solve(alpha));
                }
                return newVec;
            }
            case Interpolation.STEP -> {
                return p1;
            }

            default -> throw new IllegalArgumentException("Unknown interpolation: " + interpolation);
        }
    }

    public enum Interpolation {
        // ordered by priority
        STEP,
        CATMULLROM,
        BEZIER,
        LINEAR;

        public static Interpolation getInterpolation(Interpolation before, Interpolation after) {
            if (after == Interpolation.STEP) return before;
            return before.ordinal() > after.ordinal() ? after : before; // return the one with the greatest priority
        }
    }
}