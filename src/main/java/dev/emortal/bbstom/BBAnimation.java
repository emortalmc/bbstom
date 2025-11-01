package dev.emortal.bbstom;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

@SuppressWarnings("unused")
public record BBAnimation(String uuid, String name, String loop, boolean override, float length, String anim_time_update,
                          String blend_weight, String start_delay, String loop_delay,
                          Map<String, BBAnimator> animators) {
    public @Nullable BBAnimator getAnimatorByUUID(String uuid) {
        return animators.get(uuid);
    }

    public @Nullable BBAnimator getAnimatorByName(String name) {
        for (Map.Entry<String, BBAnimator> entry : animators.entrySet()) {
            if (entry.getValue().name().equals(name)) return entry.getValue();
        }
        return null;
    }
}