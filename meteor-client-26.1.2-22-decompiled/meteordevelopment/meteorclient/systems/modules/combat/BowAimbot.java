package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.Set;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class BowAimbot
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> range;
    private final Setting<Set<EntityType<?>>> entities;
    private final Setting<SortPriority> priority;
    private final Setting<Boolean> babies;
    private final Setting<Boolean> nametagged;
    private final Setting<Boolean> pauseOnCombat;
    private boolean wasPathing;
    private Entity target;

    public BowAimbot() {
        super(Categories.Combat, "bow-aimbot", "Automatically aims your bow for you.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.range = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("range")).description("The maximum range the entity can be to aim at it.")).defaultValue(20.0).range(0.0, 100.0).sliderMax(100.0).build());
        this.entities = this.sgGeneral.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entities")).description("Entities to attack.")).onlyAttackable().build());
        this.priority = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("priority")).description("What type of entities to target.")).defaultValue(SortPriority.LowestHealth)).build());
        this.babies = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("babies")).description("Whether or not to attack baby variants of the entity.")).defaultValue(true)).build());
        this.nametagged = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("nametagged")).description("Whether or not to attack mobs with a name tag.")).defaultValue(false)).build());
        this.pauseOnCombat = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-on-combat")).description("Freezes Baritone temporarily until you released the bow.")).defaultValue(false)).build());
    }

    @Override
    public void onDeactivate() {
        this.target = null;
        this.wasPathing = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!PlayerUtils.isAlive() || !this.itemInHand()) {
            return;
        }
        if (!this.mc.player.getAbilities().instabuild && !InvUtils.find(itemStack -> itemStack.getItem() instanceof ArrowItem).found()) {
            return;
        }
        this.target = TargetUtils.get(entity -> {
            if (entity == this.mc.player) return false;
            if (entity == this.mc.getCameraEntity()) {
                return false;
            }
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity)entity;
                if (livingEntity.isDeadOrDying()) return false;
            }
            if (!entity.isAlive()) {
                return false;
            }
            if (!PlayerUtils.isWithin(entity, (double)this.range.get())) {
                return false;
            }
            if (!this.entities.get().contains(entity.getType())) {
                return false;
            }
            if (!this.nametagged.get().booleanValue() && entity.hasCustomName()) {
                return false;
            }
            if (!PlayerUtils.canSeeEntity(entity)) {
                return false;
            }
            if (entity instanceof Player) {
                Player player = (Player)entity;
                if (player.isCreative()) {
                    return false;
                }
                if (!Friends.get().shouldAttack(player)) {
                    return false;
                }
            }
            if (!(entity instanceof Animal)) return true;
            Animal animal = (Animal)entity;
            if (this.babies.get() != false) return true;
            if (animal.isBaby()) return false;
            return true;
        }, this.priority.get());
        if (this.target == null) {
            if (this.wasPathing) {
                PathManagers.get().resume();
                this.wasPathing = false;
            }
            return;
        }
        if (this.mc.options.keyUse.isDown() && this.itemInHand()) {
            if (this.pauseOnCombat.get().booleanValue() && PathManagers.get().isPathing() && !this.wasPathing) {
                PathManagers.get().pause();
                this.wasPathing = true;
            }
            this.aim();
        }
    }

    private boolean itemInHand() {
        return InvUtils.testInMainHand(Items.BOW, Items.CROSSBOW);
    }

    private void aim() {
        float velocity = BowItem.getPowerForTime((int)this.mc.player.getTicksUsingItem());
        Vec3 pos = this.target.position();
        double relativeX = pos.x - this.mc.player.getX();
        double relativeY = pos.y + (double)(this.target.getBbHeight() / 2.0f) - this.mc.player.getEyeY();
        float velocitySq = velocity * velocity;
        float g = 0.006f;
        double relativeZ = pos.z - this.mc.player.getZ();
        double hDistance = Math.sqrt(relativeX * relativeX + relativeZ * relativeZ);
        double hDistanceSq = hDistance * hDistance;
        float pitch = (float)(-Math.toDegrees(Math.atan(((double)velocitySq - Math.sqrt((double)(velocitySq * velocitySq) - (double)g * ((double)g * hDistanceSq + 2.0 * relativeY * (double)velocitySq))) / ((double)g * hDistance))));
        if (Float.isNaN(pitch)) {
            Rotations.rotate(Rotations.getYaw(this.target), Rotations.getPitch(this.target));
        } else {
            Rotations.rotate(Rotations.getYaw(new Vec3(pos.x, pos.y, pos.z)), pitch);
        }
    }

    @Override
    public String getInfoString() {
        return EntityUtils.getName(this.target);
    }
}
