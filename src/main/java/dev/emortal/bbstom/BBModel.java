package dev.emortal.bbstom;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONCreator;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class BBModel {
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
        return elementMap.get(elementName).uuid();
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

    public Quaternionf getRotation(BBAnimation animation, float time, String uuid) {
        return getRotation(new Quaternionf(), animation, time, uuid);
    }

    private Quaternionf getRotation(Quaternionf quat, BBAnimation animation, float time, String uuid) {
        if (uuid == null) return quat; // no more parents

        BBAnimator animator = animation.getAnimatorByUUID(uuid);
        if (animator != null) quat.premul(BBModel.quatFromRot(animator.getRotation(time)));

        return getRotation(quat, animation, time, getParent(uuid));
    }

    public Vector3f getPosition(BBAnimation animation, float time, String uuid) {
        Vector3f objectPos = new Vector3f();
        BBElement element = getElementByUUID(uuid);
        if (element != null) {
            objectPos = element.position();
        } else {
            BBGroup group = getGroupByUUID(uuid);
            if (group != null) objectPos = group.origin();
        }

        return getPosition(new Vector3f(), new Vector3f(), objectPos, animation, time, uuid);
    }

    private Vector3f getPosition(Vector3f pos, Vector3f rotOffset, Vector3f objectPos, BBAnimation animation, float time, String uuid) {
        if (uuid == null) {
            return pos.add(rotOffset).add(objectPos);
        }

        BBGroup group = getGroupByUUID(uuid);
        Vector3f origin;
        if (group != null) origin = group.origin().sub(objectPos, new Vector3f());
        else origin = new Vector3f();

        BBAnimator animator = animation.getAnimatorByUUID(uuid);
        if (animator != null) {
            Vector3f animPos = animator.getPosition(time);
            Vector3f rot = animator.getRotation(time);
            Quaternionf parentRot = getRotation(new Quaternionf(), animation, time, getParent(uuid));
            Quaternionf quat = BBModel.quatFromRot(rot);
            pos.add(parentRot.transform(animPos));

            rotOffset.sub(origin);
            quat.transform(rotOffset);
            rotOffset.add(origin);
        }

        return getPosition(pos, rotOffset, objectPos, animation, time, getParent(uuid));
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
