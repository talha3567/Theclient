package meteordevelopment.meteorclient.systems.modules.world;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.Target;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class EndermanLook
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Mode> lookMode;
    private final Setting<Boolean> stun;

    public EndermanLook() {
        super(Categories.World, "enderman-look", "Either looks at all Endermen or prevents you from looking at Endermen.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.lookMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("look-mode")).description("How this module behaves.")).defaultValue(Mode.Away)).build());
        this.stun = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("stun-hostiles")).description("Automatically stares at hostile endermen to stun them in place.")).defaultValue(true)).visible(() -> this.lookMode.get() == Mode.Away)).build());
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.player.getItemBySlot(EquipmentSlot.HEAD).is((Object)Blocks.CARVED_PUMPKIN.asItem()) || this.mc.player.getAbilities().instabuild) {
            return;
        }
        for (Entity entity : this.mc.level.entitiesForRendering()) {
            EnderMan enderman;
            if (!(entity instanceof EnderMan) || !(enderman = (EnderMan)entity).isAlive() || !this.mc.player.hasLineOfSight((Entity)enderman)) continue;
            switch (this.lookMode.get().ordinal()) {
                case 1: {
                    if (enderman.isAngry() && this.stun.get().booleanValue()) {
                        Rotations.rotate(Rotations.getYaw((Entity)enderman), Rotations.getPitch((Entity)enderman, Target.Head), -75, null);
                        break;
                    }
                    if (!this.angleCheck(enderman)) break;
                    Rotations.rotate(this.mc.player.getYRot(), 90.0, -75, null);
                    break;
                }
                case 0: {
                    if (enderman.isAngry()) break;
                    Rotations.rotate(Rotations.getYaw((Entity)enderman), Rotations.getPitch((Entity)enderman, Target.Head), -75, null);
                }
            }
        }
    }

    private boolean angleCheck(EnderMan entity) {
        Vec3 vec3d = this.mc.player.getViewVector(1.0f).normalize();
        Vec3 vec3d2 = new Vec3(entity.getX() - this.mc.player.getX(), entity.getEyeY() - this.mc.player.getEyeY(), entity.getZ() - this.mc.player.getZ());
        double d = vec3d2.length();
        double e = vec3d.dot(vec3d2 = vec3d2.normalize());
        return e > 1.0 - 0.025 / d;
    }

    public static enum Mode {
        At,
        Away;

    }
}
