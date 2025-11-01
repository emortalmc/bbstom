package dev.emortal.bbstom.renderer;

import org.joml.Vector3f;

public enum PlayerDisplayPart {
    HEAD(0, "animated_java:blueprint/player_display/head", new Vector3f(0, -4.0f/16.0f, 0)),
    RIGHT_ARM(-1024, "animated_java:blueprint/player_display/right_arm", new Vector3f(-2f/16f, 6f/16f, 0)),
    LEFT_ARM(-2048, "animated_java:blueprint/player_display/left_arm", new Vector3f(2f/16f, 6f/16f, 0)),
    TORSO(-3072, "animated_java:blueprint/player_display/torso", new Vector3f(0, 6f/16f, 0)),
    RIGHT_LEG(-4096, "animated_java:blueprint/player_display/right_leg", new Vector3f(0, 6f/16f, 0)),
    LEFT_LEG(-5120, "animated_java:blueprint/player_display/left_leg", new Vector3f(0, 6f/16f, 0));

    private final double yTranslation;
    private final String customModelData;
    private final Vector3f offset;
    PlayerDisplayPart(double yTranslation, String customModelData, Vector3f offset) {
        this.yTranslation = yTranslation;
        this.customModelData = customModelData;
        this.offset = offset;
    }

    public double getYTranslation() {
        return yTranslation;
    }

    public String getCustomModelData() {
        return customModelData;
    }

    public Vector3f getOffset() {
        return offset;
    }
}
