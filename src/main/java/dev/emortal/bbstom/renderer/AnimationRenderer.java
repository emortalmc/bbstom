package dev.emortal.bbstom.renderer;

import dev.emortal.bbstom.BBAnimation;
import dev.emortal.bbstom.BBModel;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;

@SuppressWarnings("unused")
public abstract class AnimationRenderer {
    private float currentTime = 0f;
    private float animationSpeed = 1f;
    private boolean paused = false;

    private BBModel model;
    private BBAnimation animation;
    private Instance instance;
    private Pos position;

    public AnimationRenderer(BBModel model, BBAnimation animation, Instance instance, Pos position) {
        this.model = model;
        this.animation = animation;
        this.instance = instance;
        this.position = position;

        setModel(model);
        setInstance(instance);
    }

    public float getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(float currentTime) {
        this.currentTime = currentTime;
    }

    public float getAnimationSpeed() {
        return animationSpeed;
    }

    public void setAnimationSpeed(float animationSpeed) {
        this.animationSpeed = animationSpeed;
    }

    public Pos getPosition() {
        return position;
    }

    public void setPosition(Pos position) {
        this.position = position;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public BBModel getModel() {
        return model;
    }

    public void setModel(BBModel model) {
        this.model = model;
    }

    public BBAnimation getAnimation() {
        return animation;
    }

    public void setAnimation(BBAnimation animation) {
        this.animation = animation;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    abstract void render();
}
