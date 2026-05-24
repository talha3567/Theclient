package meteordevelopment.meteorclient.systems.modules.movement;

import java.util.List;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.BlockBehaviourAccessor;
import meteordevelopment.meteorclient.mixin.DirectionAccessor;
import meteordevelopment.meteorclient.mixininterface.IVec3;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Vector3dSetting;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public class AutoWasp
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> horizontalSpeed;
    private final Setting<Double> verticalSpeed;
    private final Setting<Boolean> avoidLanding;
    private final Setting<Boolean> predictMovement;
    private final Setting<Boolean> onlyFriends;
    private final Setting<Action> action;
    private final Setting<Vector3d> offset;
    public Player target;
    private int jumpTimer;
    private boolean incrementJumpTimer;

    public AutoWasp() {
        super(Categories.Movement, "auto-wasp", "Wasps for you. Unable to traverse around blocks, assumes a clear straight line to the target.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.horizontalSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("horizontal-speed")).description("Horizontal elytra speed.")).defaultValue(2.0).build());
        this.verticalSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("vertical-speed")).description("Vertical elytra speed.")).defaultValue(3.0).build());
        this.avoidLanding = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("avoid-landing")).description("Will try to avoid landing if your target is on the ground.")).defaultValue(true)).build());
        this.predictMovement = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("predict-movement")).description("Tries to predict the targets position according to their movement.")).defaultValue(true)).build());
        this.onlyFriends = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-friends")).description("Will only follow friends.")).defaultValue(false)).build());
        this.action = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("action-on-target-loss")).description("What to do if you lose the target.")).defaultValue(Action.TOGGLE)).build());
        this.offset = this.sgGeneral.add(((Vector3dSetting.Builder)((Vector3dSetting.Builder)new Vector3dSetting.Builder().name("offset")).description("How many blocks offset to wasp at from the target.")).defaultValue(0.0, 0.0, 0.0).build());
        this.jumpTimer = 0;
        this.incrementJumpTimer = false;
    }

    @Override
    public void onActivate() {
        if (this.target == null || this.target.isRemoved()) {
            this.target = (Player)TargetUtils.get(entity -> {
                Player player;
                block5: {
                    block4: {
                        if (!(entity instanceof Player)) break block4;
                        player = (Player)entity;
                        if (entity != this.mc.player) break block5;
                    }
                    return false;
                }
                if (player.isDeadOrDying() || player.getHealth() <= 0.0f) {
                    return false;
                }
                return this.onlyFriends.get() == false || Friends.get().get(player) != null;
            }, SortPriority.LowestDistance);
            if (this.target == null) {
                this.error("No valid targets.", new Object[0]);
                this.toggle();
                return;
            }
            this.info(this.target.getName().getString() + " set as target.", new Object[0]);
        }
        this.jumpTimer = 0;
        this.incrementJumpTimer = false;
    }

    @Override
    public void onDeactivate() {
        this.target = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.target.isRemoved()) {
            this.warning("Lost target!", new Object[0]);
            switch (this.action.get().ordinal()) {
                case 1: {
                    this.onActivate();
                    break;
                }
                case 0: {
                    this.toggle();
                    break;
                }
                case 2: {
                    this.mc.player.connection.handleDisconnect(new ClientboundDisconnectPacket((Component)Component.literal((String)"%s[%sAuto Wasp%s] Lost target.".formatted(ChatFormatting.GRAY, ChatFormatting.BLUE, ChatFormatting.GRAY))));
                }
            }
            if (!this.isActive()) {
                return;
            }
        }
        if (!this.mc.player.getItemBySlot(EquipmentSlot.CHEST).has(DataComponents.GLIDER)) {
            return;
        }
        if (this.incrementJumpTimer) {
            ++this.jumpTimer;
        }
        if (!this.mc.player.isFallFlying()) {
            if (!this.incrementJumpTimer) {
                this.incrementJumpTimer = true;
            }
            if (this.mc.player.onGround() && this.incrementJumpTimer) {
                this.mc.player.jumpFromGround();
                return;
            }
            if (this.jumpTimer >= 4) {
                this.jumpTimer = 0;
                this.mc.player.setJumping(false);
                this.mc.player.setSprinting(true);
                this.mc.getConnection().send((Packet)new ServerboundPlayerCommandPacket((Entity)this.mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
            }
        } else {
            this.incrementJumpTimer = false;
            this.jumpTimer = 0;
        }
    }

    @EventHandler
    private void onMove(PlayerMoveEvent event) {
        double yDist;
        if (!this.mc.player.getItemBySlot(EquipmentSlot.CHEST).has(DataComponents.GLIDER)) {
            return;
        }
        if (!this.mc.player.isFallFlying()) {
            return;
        }
        double xVel = 0.0;
        double yVel = 0.0;
        double zVel = 0.0;
        Vec3 targetPos = this.target.position().add(this.offset.get().x, this.offset.get().y, this.offset.get().z);
        if (this.predictMovement.get().booleanValue()) {
            targetPos.add(Entity.collideBoundingBox((Entity)this.target, (Vec3)this.target.getDeltaMovement(), (AABB)this.target.getBoundingBox(), (Level)this.mc.level, (List)this.mc.level.getEntityCollisions((Entity)this.target, this.target.getBoundingBox().expandTowards(this.target.getDeltaMovement()))));
        }
        if (this.avoidLanding.get().booleanValue()) {
            double d = this.target.getBoundingBox().getXsize() / 2.0;
            for (Direction dir : DirectionAccessor.meteor$getHorizontal()) {
                BlockPos pos = BlockPos.containing((Position)targetPos.relative(dir, d).relative(dir.getClockWise(), d)).below();
                if (!((BlockBehaviourAccessor)this.mc.level.getBlockState(pos).getBlock()).meteor$isHasCollision() || !(Math.abs(targetPos.y() - (double)(pos.getY() + 1)) <= 0.25)) continue;
                targetPos = new Vec3(targetPos.x, (double)pos.getY() + 1.25, targetPos.z);
                break;
            }
        }
        double xDist = targetPos.x() - this.mc.player.getX();
        double zDist = targetPos.z() - this.mc.player.getZ();
        double absX = Math.abs(xDist);
        double absZ = Math.abs(zDist);
        double diag = 0.0;
        if (absX > (double)1.0E-5f && absZ > (double)1.0E-5f) {
            diag = 1.0 / Math.sqrt(absX * absX + absZ * absZ);
        }
        if (absX > (double)1.0E-5f) {
            xVel = absX < this.horizontalSpeed.get() ? xDist : this.horizontalSpeed.get() * Math.signum(xDist);
            if (diag != 0.0) {
                xVel *= absX * diag;
            }
        }
        if (absZ > (double)1.0E-5f) {
            zVel = absZ < this.horizontalSpeed.get() ? zDist : this.horizontalSpeed.get() * Math.signum(zDist);
            if (diag != 0.0) {
                zVel *= absZ * diag;
            }
        }
        if (Math.abs(yDist = targetPos.y() - this.mc.player.getY()) > (double)1.0E-5f) {
            yVel = Math.abs(yDist) < this.verticalSpeed.get() ? yDist : this.verticalSpeed.get() * Math.signum(yDist);
        }
        ((IVec3)event.movement).meteor$set(xVel, yVel, zVel);
    }

    public static enum Action {
        TOGGLE,
        CHOOSE_NEW_TARGET,
        DISCONNECT;


        public String toString() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> "Toggle module";
                case 1 -> "Choose new target";
                case 2 -> "Disconnect";
            };
        }
    }
}
