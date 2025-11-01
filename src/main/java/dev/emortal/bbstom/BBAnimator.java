package dev.emortal.bbstom;

import com.alibaba.fastjson2.annotation.JSONCreator;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("unused")
public record BBAnimator(String name, String type, BBKeyframe[] rotationKeyframes,
                         BBKeyframe[] positionKeyframes, BBKeyframe[] scaleKeyframes) {
    @JSONCreator
    public BBAnimator(String name, String type, BBKeyframe[] keyframes/*, boolean rotation_global, boolean quaternion_interpolation*/) {
        List<BBKeyframe> rot = new ArrayList<>();
        List<BBKeyframe> pos = new ArrayList<>();
        List<BBKeyframe> scale = new ArrayList<>();
        if (keyframes != null) {
            Arrays.sort(keyframes, Comparator.comparingDouble(BBKeyframe::time));

            for (BBKeyframe keyframe : keyframes) {
                if (keyframe == null) continue;

                switch (keyframe.channel()) {
                    case "rotation" -> rot.add(keyframe);
                    case "position" -> pos.add(keyframe);
                    case "scale" -> scale.add(keyframe);
                }
            }
        }

        this(name, type, rot.toArray(new BBKeyframe[0]), pos.toArray(new BBKeyframe[0]), scale.toArray(new BBKeyframe[0]));
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

    public Vector3f getScale(float time) {
        return getValue(time, scaleKeyframes);
    }
}