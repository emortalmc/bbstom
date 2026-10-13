package dev.emortal.bbstom;

import com.alibaba.fastjson2.annotation.JSONCreator;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("unused")
public record BBAnimator(String name, String type, BBKeyframe[] rotationKeyframes,
                         BBKeyframe[] positionKeyframes, BBKeyframe[] scaleKeyframes, boolean quaternion_interpolation) {
    public BBAnimator(String name, String type, BBKeyframe[] rotationKeyframes,
                      BBKeyframe[] positionKeyframes, BBKeyframe[] scaleKeyframes) {
        this(name, type, rotationKeyframes, positionKeyframes, scaleKeyframes, false);
    }

    public BBAnimator(String name, String type, BBKeyframe[] keyframes) {
        this(name, type, keyframes, false);
    }

    @JSONCreator
    public BBAnimator(String name, String type, BBKeyframe[] keyframes, boolean quaternion_interpolation) {
        List<BBKeyframe> rot = new ArrayList<>();
        List<BBKeyframe> pos = new ArrayList<>();
        List<BBKeyframe> scale = new ArrayList<>();
        if (keyframes != null) {
            Arrays.sort(keyframes, Comparator.nullsLast(Comparator.comparingDouble(BBKeyframe::time)));

            for (BBKeyframe keyframe : keyframes) {
                if (keyframe == null) continue;

                switch (keyframe.channel()) {
                    case "rotation" -> rot.add(keyframe);
                    case "position" -> pos.add(keyframe);
                    case "scale" -> scale.add(keyframe);
                }
            }
        }

        this(name, type, rot.toArray(new BBKeyframe[0]), pos.toArray(new BBKeyframe[0]), scale.toArray(new BBKeyframe[0]), quaternion_interpolation);
    }

    public Vector3f getValue(float time, BBKeyframe[] keyframes) {
        if (keyframes == null || keyframes.length == 0) return new Vector3f(0, 0, 0);
        if (keyframes.length == 1) return keyframes[0].data_points()[0].vector(time);

        BBKeyframe beforePlus = null;
        BBKeyframe before = keyframes[0];
        BBKeyframe after = null;
        BBKeyframe afterPlus = null;

        for (int i = 0; i < keyframes.length; i++) {
            BBKeyframe keyframe = keyframes[i];
            if (keyframe.time() > time) {
                after = keyframe;
                afterPlus = (i + 1) >= keyframes.length ? null : keyframes[i + 1];
                break;
            }
            beforePlus = before;
            before = keyframe;
        }

        if (after == null) after = keyframes[keyframes.length - 1];

        return before.interpolate(beforePlus, after, afterPlus, time);
    }

    public Vector3f getPosition(float time) {
        return getValue(time, positionKeyframes);
    }

    public Vector3f getRotation(float time) {
        return getValue(time, rotationKeyframes);
    }

    /**
     * Gets the rotation with quaternions instead of eulers to avoid gimbal lock
     */
    public Quaternionf getRotationQuaternion(float time, Vector3f bindRotation) {
        if (!quaternion_interpolation || rotationKeyframes == null || rotationKeyframes.length == 0) {
            return BBModel.quatFromRot(getRotation(time).add(bindRotation));
        }

        BBKeyframe before = rotationKeyframes[0];
        if (time <= before.time()) {
            return BBModel.quatFromRot(before.data_points()[0].vector(time).add(bindRotation));
        }
        for (int i = 1; i < rotationKeyframes.length; i++) {
            BBKeyframe after = rotationKeyframes[i];
            if (time == after.time()) {
                return BBModel.quatFromRot(after.data_points()[0].vector(time).add(bindRotation));
            }
            if (time < after.time()) {
                Quaternionf start = BBModel.quatFromRot(before.data_points()[before.data_points().length - 1].vector(time).add(bindRotation));
                if (before.interpolation() == BBKeyframe.Interpolation.STEP) return start;
                Quaternionf end = BBModel.quatFromRot(after.data_points()[0].vector(time).add(bindRotation));
                float fraction = (time - before.time()) / (after.time() - before.time());
                return start.slerp(end, fraction).normalize();
            }
            before = after;
        }
        return BBModel.quatFromRot(before.data_points()[before.data_points().length - 1].vector(time).add(bindRotation));
    }

    public Vector3f getScale(float time) {
        return getValue(time, scaleKeyframes);
    }
}