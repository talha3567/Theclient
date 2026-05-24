package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.BedBlock;

public class ReverseStep
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> fallSpeed;
    private final Setting<Double> fallDistance;
    private final Setting<Boolean> vehicles;

    public ReverseStep() {
        super(Categories.Movement, "reverse-step", "Allows you to fall down blocks at a greater speed.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.fallSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("fall-speed")).description("How fast to fall in blocks per second.")).defaultValue(3.0).min(0.0).build());
        this.fallDistance = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("fall-distance")).description("The maximum fall distance this setting will activate at.")).defaultValue(3.0).min(0.0).build());
        this.vehicles = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("vehicles")).description("Whether or not reverse step should affect vehicles.")).defaultValue(false)).build());
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        Entity vehicle = this.mc.player.getVehicle();
        if (vehicle != null && this.vehicles.get().booleanValue()) {
            if (this.canSnap(vehicle)) {
                ((IVec3)vehicle.getDeltaMovement()).meteor$setY(-this.fallSpeed.get().doubleValue());
            }
        } else {
            if (this.mc.player.isSuppressingSlidingDownLadder() || this.mc.player.zza == 0.0f && this.mc.player.xxa == 0.0f) {
                return;
            }
            if (!this.isOnBed() && this.canSnap((Entity)this.mc.player)) {
                ((IVec3)this.mc.player.getDeltaMovement()).meteor$setY(-this.fallSpeed.get().doubleValue());
            }
        }
    }

    private boolean canSnap(Entity entity) {
        if (!entity.onGround() || entity.isUnderWater() || entity.isInLava() || this.mc.options.keyJump.isDown() || entity.noPhysics) {
            return false;
        }
        return !this.mc.level.noCollision(entity.getBoundingBox().move(0.0, (double)((float)(-(this.fallDistance.get() + 0.01))), 0.0));
    }

    private boolean isOnBed() {
        BlockPos.MutableBlockPos blockPos = this.mc.player.blockPosition().mutable();
        if (this.check(blockPos, 0, 0)) {
            return true;
        }
        double xa = this.mc.player.getX() - (double)blockPos.getX();
        double za = this.mc.player.getZ() - (double)blockPos.getZ();
        if (xa >= 0.0 && xa <= 0.3 && this.check(blockPos, -1, 0)) {
            return true;
        }
        if (xa >= 0.7 && this.check(blockPos, 1, 0)) {
            return true;
        }
        if (za >= 0.0 && za <= 0.3 && this.check(blockPos, 0, -1)) {
            return true;
        }
        if (za >= 0.7 && this.check(blockPos, 0, 1)) {
            return true;
        }
        if (xa >= 0.0 && xa <= 0.3 && za >= 0.0 && za <= 0.3 && this.check(blockPos, -1, -1)) {
            return true;
        }
        if (xa >= 0.0 && xa <= 0.3 && za >= 0.7 && this.check(blockPos, -1, 1)) {
            return true;
        }
        if (xa >= 0.7 && za >= 0.0 && za <= 0.3 && this.check(blockPos, 1, -1)) {
            return true;
        }
        return xa >= 0.7 && za >= 0.7 && this.check(blockPos, 1, 1);
    }

    private boolean check(BlockPos.MutableBlockPos blockPos, int x, int z) {
        blockPos.move(x, 0, z);
        boolean is = this.mc.level.getBlockState((BlockPos)blockPos).getBlock() instanceof BedBlock;
        blockPos.move(-x, 0, -z);
        return is;
    }
}
