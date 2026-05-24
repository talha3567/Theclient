package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.Set;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.TridentItem;

public class Hitboxes
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgWeapon;
    private final Setting<Set<EntityType<?>>> entities;
    private final Setting<Double> value;
    private final Setting<Boolean> ignoreFriends;
    private final Setting<Boolean> onlyOnWeapon;
    private final Setting<Boolean> sword;
    private final Setting<Boolean> axe;
    private final Setting<Boolean> pickaxe;
    private final Setting<Boolean> shovel;
    private final Setting<Boolean> hoe;
    private final Setting<Boolean> mace;
    private final Setting<Boolean> spear;
    private final Setting<Boolean> trident;

    public Hitboxes() {
        super(Categories.Combat, "hitboxes", "Expands an entity's hitboxes.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgWeapon = this.settings.createGroup("Weapon Options");
        this.entities = this.sgGeneral.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entities")).description("Which entities to target.")).defaultValue(EntityType.PLAYER).build());
        this.value = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("expand")).description("How much to expand the hitbox of the entity.")).defaultValue(0.5).build());
        this.ignoreFriends = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-friends")).description("Doesn't expand the hitboxes of friends.")).defaultValue(true)).build());
        this.onlyOnWeapon = this.sgWeapon.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-on-weapon")).description("Only modifies hitbox when holding a weapon in hand.")).defaultValue(false)).build());
        this.sword = this.sgWeapon.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sword")).description("Enable when holding a sword.")).defaultValue(true)).visible(this.onlyOnWeapon::get)).build());
        this.axe = this.sgWeapon.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("axe")).description("Enable when holding an axe.")).defaultValue(true)).visible(this.onlyOnWeapon::get)).build());
        this.pickaxe = this.sgWeapon.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pickaxe")).description("Enable when holding a pickaxe.")).defaultValue(true)).visible(this.onlyOnWeapon::get)).build());
        this.shovel = this.sgWeapon.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("shovel")).description("Enable when holding a shovel.")).defaultValue(true)).visible(this.onlyOnWeapon::get)).build());
        this.hoe = this.sgWeapon.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("hoe")).description("Enable when holding a hoe.")).defaultValue(true)).visible(this.onlyOnWeapon::get)).build());
        this.mace = this.sgWeapon.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("mace")).description("Enable when holding a mace.")).defaultValue(true)).visible(this.onlyOnWeapon::get)).build());
        this.spear = this.sgWeapon.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("spear")).description("Enable when holding a spear.")).defaultValue(true)).visible(this.onlyOnWeapon::get)).build());
        this.trident = this.sgWeapon.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("trident")).description("Enable when holding a trident.")).defaultValue(true)).visible(this.onlyOnWeapon::get)).build());
    }

    public double getEntityValue(Entity entity) {
        block5: {
            block4: {
                if (!this.isActive() || !this.testWeapon()) break block4;
                if (!this.ignoreFriends.get().booleanValue() || !(entity instanceof Player)) break block5;
                Player playerEntity = (Player)entity;
                if (!Friends.get().isFriend(playerEntity)) break block5;
            }
            return 0.0;
        }
        if (this.entities.get().contains(entity.getType())) {
            return this.value.get();
        }
        return 0.0;
    }

    private boolean testWeapon() {
        if (!this.onlyOnWeapon.get().booleanValue()) {
            return true;
        }
        return InvUtils.testInMainHand(itemStack -> {
            if (this.sword.get().booleanValue() && itemStack.is(ItemTags.SWORDS)) {
                return true;
            }
            if (this.axe.get().booleanValue() && itemStack.is(ItemTags.AXES)) {
                return true;
            }
            if (this.pickaxe.get().booleanValue() && itemStack.is(ItemTags.PICKAXES)) {
                return true;
            }
            if (this.shovel.get().booleanValue() && itemStack.is(ItemTags.SHOVELS)) {
                return true;
            }
            if (this.hoe.get().booleanValue() && itemStack.is(ItemTags.HOES)) {
                return true;
            }
            if (this.mace.get().booleanValue() && itemStack.getItem() instanceof MaceItem) {
                return true;
            }
            if (this.spear.get().booleanValue() && itemStack.is(ItemTags.SPEARS)) {
                return true;
            }
            return this.trident.get() != false && itemStack.getItem() instanceof TridentItem;
        });
    }
}
