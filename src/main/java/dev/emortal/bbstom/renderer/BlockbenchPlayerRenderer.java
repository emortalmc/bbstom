package dev.emortal.bbstom.renderer;

import dev.emortal.bbstom.BBAnimation;
import dev.emortal.bbstom.BBModel;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class BlockbenchPlayerRenderer extends PlayerAnimationRenderer {
    private String bodyUUID;
    private String leftArmUUID;
    private String rightArmUUID;
    private String leftLegUUID;
    private String rightLegUUID;
    private String headUUID;

    private @Nullable Task renderTask;

    private PlayerSkin skin;
    public BlockbenchPlayerRenderer(BBModel model, BBAnimation animation, Instance instance, Pos position, PlayerSkin skin) {
        super(model, animation, instance, position, skin);
        this.skin = skin;
    }

    public void setSkin(PlayerSkin skin) {
        this.skin = skin;
    }

    public PlayerSkin getSkin() {
        return skin;
    }

    @Override
    public void setModel(BBModel model) {
        super.setModel(model);
        this.bodyUUID = model.getElementUUID("Body");
        this.leftArmUUID = model.getElementUUID("Left Arm");
        this.rightArmUUID = model.getElementUUID("Right Arm");
        this.leftLegUUID = model.getElementUUID("Left Leg");
        this.rightLegUUID = model.getElementUUID("Right Leg");
        this.headUUID = model.getElementUUID("Head");
    }

    @Override
    public void render() {
        renderTask = MinecraftServer.getSchedulerManager().submitTask(new Supplier<>() {
            final float tickTime = 1f / ServerFlag.SERVER_TICKS_PER_SECOND;

            @Override
            public TaskSchedule get() {
                if (isPaused()) return TaskSchedule.park();

                Entity torso = getTorso();
                Entity head = getHead();
                Entity leftArm = getLeftArm();
                Entity rightArm = getRightArm();
                Entity leftLeg = getLeftLeg();
                Entity rightLeg = getRightLeg();
                Pos pos = getPosition();
                float time = getCurrentTime();
                BBAnimation animation = getAnimation();
                BBModel model = getModel();

                Quaternionf yawQuat = new Quaternionf().rotateY((float) Math.toRadians(-pos.yaw() + 180));

                renderEntity(yawQuat, model, animation, torso, bodyUUID, time, pos, PlayerDisplayPart.TORSO);
                renderEntity(yawQuat, model, animation, head, headUUID, time, pos, PlayerDisplayPart.HEAD);
                renderEntity(yawQuat, model, animation, leftArm, leftArmUUID, time, pos, PlayerDisplayPart.LEFT_ARM);
                renderEntity(yawQuat, model, animation, rightArm, rightArmUUID, time, pos, PlayerDisplayPart.RIGHT_ARM);
                renderEntity(yawQuat, model, animation, leftLeg, leftLegUUID, time, pos, PlayerDisplayPart.LEFT_LEG);
                renderEntity(yawQuat, model, animation, rightLeg, rightLegUUID, time, pos, PlayerDisplayPart.RIGHT_LEG);

                setCurrentTime(time + tickTime * getAnimationSpeed());
                if (time > animation.length()) {
                    if (animation.loop().equals("loop")) {
                        setCurrentTime(time % animation.length());
                    }
                }

                return TaskSchedule.tick(1);
            }
        });
    }

    private void renderEntity(Quaternionf yawQuat, BBModel model, BBAnimation animation, Entity entity, String uuid, float time, Pos pos, PlayerDisplayPart part) {
        Vector3f vec = model.getPosition(animation, time, uuid).div(16);
        yawQuat.transform(vec);
        Quaternionf quat = model.getRotation(animation, time, uuid).premul(yawQuat);
        Vector3f offset = quat.transform(part.getOffset(), new Vector3f());
        entity.teleport(new Pos(vec.x + offset.x, vec.y + offset.y, vec.z + offset.z).add(pos));
        entity.editEntityMeta(AbstractDisplayMeta.class, meta -> {
            meta.setTransformationInterpolationDuration(1);
            meta.setTransformationInterpolationStartDelta(0);
            meta.setLeftRotation(BBModel.quatToFloats(quat));
        });
    }

    @Override
    public void setPaused(boolean paused) {
        super.setPaused(paused);
        if (!paused) {
            if (renderTask != null) renderTask.unpark();
        }
    }
}
