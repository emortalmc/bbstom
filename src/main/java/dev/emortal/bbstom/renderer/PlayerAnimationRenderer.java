package dev.emortal.bbstom.renderer;

import dev.emortal.bbstom.BBAnimation;
import dev.emortal.bbstom.BBModel;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.player.ResolvableProfile;

import java.util.List;

@SuppressWarnings("unused")
public abstract class PlayerAnimationRenderer extends AnimationRenderer {

    private Entity head;
    private Entity torso;
    private Entity leftArm;
    private Entity rightArm;
    private Entity leftLeg;
    private Entity rightLeg;

    private PlayerSkin skin;

    public PlayerAnimationRenderer(BBModel model, BBAnimation animation, Instance instance, Pos position, PlayerSkin skin) {
        this.skin = skin;
        super(model, animation, instance, position);
    }

    public PlayerSkin getSkin() {
        return skin;
    }

    public void setSkin(PlayerSkin skin) {
        this.skin = skin;
    }

    public Entity getTorso() {
        return torso;
    }

    public Entity getHead() {
        return head;
    }

    public Entity getLeftArm() {
        return leftArm;
    }

    public Entity getRightArm() {
        return rightArm;
    }

    public Entity getLeftLeg() {
        return leftLeg;
    }

    public Entity getRightLeg() {
        return rightLeg;
    }

    @Override
    public void setInstance(Instance instance) {
        super.setInstance(instance);
        if (this.head != null) this.head.remove();
        if (this.torso != null) this.torso.remove();
        if (this.leftArm != null) this.leftArm.remove();
        if (this.rightArm != null) this.rightArm.remove();
        if (this.leftLeg != null) this.leftLeg.remove();
        if (this.rightLeg != null) this.rightLeg.remove();
        this.head = spawnEntity(instance, Pos.ZERO, PlayerDisplayPart.HEAD, skin);
        this.torso = spawnEntity(instance, Pos.ZERO, PlayerDisplayPart.TORSO, skin);
        this.leftArm = spawnEntity(instance, Pos.ZERO, PlayerDisplayPart.LEFT_ARM, skin);
        this.rightArm = spawnEntity(instance, Pos.ZERO, PlayerDisplayPart.RIGHT_ARM, skin);
        this.leftLeg = spawnEntity(instance, Pos.ZERO, PlayerDisplayPart.LEFT_LEG, skin);
        this.rightLeg = spawnEntity(instance, Pos.ZERO, PlayerDisplayPart.RIGHT_LEG, skin);
    }

    public static Entity spawnEntity(Instance instance, Point position, PlayerDisplayPart part, PlayerSkin skin) {
        Entity entity = new Entity(EntityType.ITEM_DISPLAY);
        entity.setNoGravity(true);

        entity.editEntityMeta(ItemDisplayMeta.class, meta -> {
            meta.setWidth(2);
            meta.setHeight(2);

            meta.setPosRotInterpolationDuration(1);
            meta.setDisplayContext(ItemDisplayMeta.DisplayContext.THIRDPERSON_RIGHT_HAND);
            meta.setItemStack(ItemStack.builder(Material.PLAYER_HEAD)
                    .set(DataComponents.PROFILE, new ResolvableProfile(skin))
                    .itemModel(part.getCustomModelData())
                    .customModelData(List.of(), List.of(), List.of("default"), List.of()).build());
            meta.setScale(new Vec(1));
            meta.setTranslation(new Vec(0, part.getYTranslation(), 0));
        });

        entity.setInstance(instance, position);

        return entity;
    }

}
