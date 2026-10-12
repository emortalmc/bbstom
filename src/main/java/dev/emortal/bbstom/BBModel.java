package dev.emortal.bbstom;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONCreator;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class BBModel {
    /** How a model's display geometry is baked. */
    public enum BakeMode {
        /** Geometry baked at its bind-pose world position. Only the change from bind is applied. */
        BIND_POSE,
        /** Geometry centred on its own origin (element centre / group origin). Full animated position is applied. */
        LOCAL_ORIGIN
    }

    private final BBMeta meta;
    private final String name;
    private final BBGroup[] groups;
    private final BBAnimation[] animations;
    private final BBElement[] elements;

    private final Map<String, String> parentMap = new HashMap<>();
    private final Map<String, BBGroup> groupMap = new HashMap<>();
    private final Map<String, BBElement> elementMap = new HashMap<>();

    @JSONCreator
    public BBModel(
            BBMeta meta,
            String name,
            BBGroup[] groups,
            BBAnimation[] animations,
            JSONObject[] outliner,
            BBElement[] elements) {
        this.meta = meta;
        this.name = name;
        this.groups = groups;
        this.animations = animations;
        this.elements = elements;

        for (BBGroup group : groups) {
            groupMap.put(group.uuid(), group);
            groupMap.put(group.name(), group);
        }
        for (BBElement element : elements) {
            elementMap.put(element.uuid(), element);
            elementMap.put(element.name(), element);
        }

        for (JSONObject outlinerr : outliner) {
            registerChildren(null, outlinerr);
        }
    }

    private void registerChildren(String parentUUID, JSONObject child) {
        String uuid = child.getString("uuid");
        JSONArray childrenObj = child.getJSONArray("children");

        parentMap.put(uuid, parentUUID);

        for (Object o : childrenObj) {
            if (o instanceof JSONObject obj) {
                registerChildren(uuid, obj);
                continue;
            }
            if (o instanceof String string) {
                parentMap.put(string, uuid);
            }
        }
    }

    public @Nullable String getParent(String uuid) {
        return parentMap.get(uuid);
    }

    public String getName() {
        return name;
    }

    public BBMeta getMeta() {
        return meta;
    }

    public BBGroup[] getGroups() {
        return groups;
    }

    public BBAnimation[] getAnimations() {
        return animations;
    }

    public BBElement[] getElements() {
        return elements;
    }

    public @Nullable BBAnimation getAnimationByName(String name) {
        for (BBAnimation animation : animations) {
            if (animation.name().equals(name)) {
                return animation;
            }
        }
        return null;
    }

    public @Nullable String getElementUUID(String elementName) {
        BBElement element = elementMap.get(elementName);
        if (element == null) return null;
        return element.uuid();
    }

    public @Nullable BBElement getElementByUUID(String uuid) {
        return elementMap.get(uuid);
    }

    public @Nullable BBGroup getGroupByUUID(String uuid) {
        return groupMap.get(uuid);
    }

    public @Nullable BBGroup getGroupByName(String name) {
        return groupMap.get(name);
    }

    /**
     * Local Euler rotation (degrees) of a node: bind rotation + animation rotation.
     * Blockbench adds the two as Euler angles, then builds a ZYX rotation from the sum.
     */
    private Vector3f localEuler(String uuid, BBAnimation animation, float time) {
        Vector3f rot = new Vector3f();
        BBGroup group = getGroupByUUID(uuid);
        if (group != null) rot.add(group.rotation());

        BBAnimator animator = animation.getAnimatorByUUID(uuid);
        if (animator != null) rot.add(animator.getRotation(time));
        return rot;
    }

    /** World rotation of the node: root * ... * parent * node. */
    public Quaternionf getRotation(BBAnimation animation, float time, String uuid) {
        Quaternionf quat = new Quaternionf();
        for (String id = uuid; id != null; id = getParent(id)) {
            quat.premul(BBModel.quatFromRot(localEuler(id, animation, time)));
        }
        return quat;
    }

    /** Animated model-space position of the element's centre, or the group's origin. */
    public Vector3f getPosition(BBAnimation animation, float time, String uuid) {
        Vector3f point = bindPoint(uuid);

        // Apply each node's transform innermost -> outermost:
        // rotate about the node's pivot, then translate by its animation position.
        for (String id = uuid; id != null; id = getParent(id)) {
            BBGroup group = getGroupByUUID(id);
            BBAnimator animator = animation.getAnimatorByUUID(id);

            if (group != null) {
                Vector3f pivot = group.origin();
                Quaternionf rot = BBModel.quatFromRot(localEuler(id, animation, time));
                point.sub(pivot).rotate(rot).add(pivot);
            }
            if (animator != null) point.add(animator.getPosition(time));
        }
        return point;
    }

    private Vector3f bindPoint(String uuid) {
        BBElement element = getElementByUUID(uuid);
        if (element != null) return new Vector3f(element.position());
        BBGroup group = getGroupByUUID(uuid);
        return group != null ? new Vector3f(group.origin()) : new Vector3f();
    }

    /**
     * Model-space matrix of a node (root * ... * parent * node), with rotations
     * applied about each group's origin. A null animation gives the bind pose.
     */
    private Matrix4f worldMatrix(@Nullable BBAnimation animation, float time, String uuid) {
        Matrix4f world = new Matrix4f();
        for (String id = uuid; id != null; id = getParent(id)) {
            BBGroup group = getGroupByUUID(id);
            BBAnimator animator = animation == null ? null : animation.getAnimatorByUUID(id);

            Matrix4f node = new Matrix4f();
            if (animator != null) node.translate(animator.getPosition(time));
            if (group != null) {
                Vector3f rot = new Vector3f(group.rotation());
                if (animator != null) rot.add(animator.getRotation(time));
                Vector3f o = group.origin();
                node.translate(o).rotate(quatFromRot(rot)).translate(-o.x, -o.y, -o.z);
            }
            world.mulLocal(node); // node * world
        }
        return world;
    }

    /**
     * Transform to apply to a display entity for the given node, in model units (pixels).
     * BIND_POSE: geometry already sits at its bind position, so only the change from bind is returned.
     * LOCAL_ORIGIN: geometry is centred on its own origin, so the full animated placement is returned.
     */
    public Matrix4f getTransform(BBAnimation animation, float time, String uuid, BakeMode mode) {
        return getTransform(animation, time, uuid, mode, new Vector3f());
    }

    /**
     * @param entityOrigin BIND_POSE only: the bbmodel-space point that sits at the display entity's
     *                     origin when no transform is applied (0,0,0 if the baked model is placed so the
     *                     bbmodel origin is at the entity position). A display rotates about its own
     *                     origin, so every pivot has to be expressed relative to this point.
     */
    public Matrix4f getTransform(BBAnimation animation, float time, String uuid, BakeMode mode, Vector3f entityOrigin) {
        return getTransform(animation, time, uuid, mode, entityOrigin, new Quaternionf());
    }

    /**
     * @param frame rotation from the bbmodel frame to the frame the display entities are baked/rendered in
     *              (identity if they match; rotationY(PI) if the baked model is turned 180 degrees about Y).
     *              The whole transform is conjugated by it, so rotation AND translation are mapped consistently,
     *              unlike negating individual Euler angles which leaves the translation unflipped.
     */
    public Matrix4f getTransform(BBAnimation animation, float time, String uuid, BakeMode mode,
                                 Vector3f entityOrigin, Quaternionf frame) {
        Matrix4f animated = worldMatrix(animation, time, uuid);
        Matrix4f result = switch (mode) {
            case BIND_POSE -> {
                // delta in bbmodel space, re-expressed in the entity's local space: T(-c) * delta * T(c)
                Matrix4f delta = animated.mul(worldMatrix(null, 0, uuid).invert());
                yield new Matrix4f()
                        .translate(-entityOrigin.x, -entityOrigin.y, -entityOrigin.z)
                        .mul(delta)
                        .translate(entityOrigin);
            }
            case LOCAL_ORIGIN -> animated.translate(bindPoint(uuid));
        };
        // F * M * F^-1
        return new Matrix4f().rotate(frame).mul(result).rotate(new Quaternionf(frame).conjugate());
    }

    public record BBMeta(String format_version, String model_format, boolean box_uv) {
    }
    public record BBGroup(String name, String uuid, Vector3f origin, Vector3f rotation) {
        @JSONCreator
        public BBGroup(String name, String uuid, float[] origin, float[] rotation) {
            this(name, uuid, new Vector3f(origin), new Vector3f(rotation));
        }
    }
    public record BBElement(String name, String uuid, Vector3f from, Vector3f to, Vector3f position, Vector3f scale) {
        @JSONCreator
        public BBElement(String name, String uuid, float[] from, float[] to) {
            Vector3f fromVec = from == null ? new Vector3f() : new Vector3f(from);
            Vector3f toVec = to == null ? new Vector3f() : new Vector3f(to);

            this(
                    name,
                    uuid,
                    fromVec,
                    toVec,
                    toVec.lerp(fromVec, 0.5f, new Vector3f()),
                    toVec.sub(fromVec, new Vector3f())
            );
        }
    }

    public static BBModel fromBytes(byte[] bytes) {
        return JSON.parseObject(bytes, BBModel.class);
    }

    public byte[] toBytes() {
        return JSON.toJSONBytes(this);
    }

    public static Quaternionf quatFromRot(Vector3f rotVec) {
        return new Quaternionf(0, 0, 0, 1).rotateZYX(
                (float) Math.toRadians(rotVec.z()),
                (float) Math.toRadians(rotVec.y()),
                (float) Math.toRadians(rotVec.x())
        );
    }

    public static float[] quatToFloats(Quaternionf rotation) {
        return new float[] { rotation.x(), rotation.y(), rotation.z(), rotation.w() };
    }

}