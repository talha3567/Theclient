package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.simulator.ProjectileEntitySimulator;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.arrow.SpectralArrow;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class ArrowDodge
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgMovement;
    private final Setting<MoveType> moveType;
    private final Setting<Double> moveSpeed;
    private final Setting<Double> distanceCheck;
    private final Setting<Boolean> groundCheck;
    private final Setting<Boolean> allProjectiles;
    private final Setting<Boolean> ignoreOwn;
    public final Setting<Integer> simulationSteps;
    private final List<Vec3> possibleMoveDirections;
    private final ProjectileEntitySimulator simulator;
    private final Pool<Vector3d> vec3s;
    private final List<Vector3d> points;

    public ArrowDodge() {
        super(Categories.Combat, "arrow-dodge", "Tries to dodge arrows coming at you.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgMovement = this.settings.createGroup("Movement");
        this.moveType = this.sgMovement.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("move-type")).description("The way you are moved by this module.")).defaultValue(MoveType.Velocity)).build());
        this.moveSpeed = this.sgMovement.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("move-speed")).description("How fast should you be when dodging arrow.")).defaultValue(1.0).min(0.01).sliderRange(0.01, 5.0).build());
        this.distanceCheck = this.sgMovement.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("distance-check")).description("How far should an arrow be from the player to be considered not hitting.")).defaultValue(1.0).min(0.01).sliderRange(0.01, 5.0).build());
        this.groundCheck = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ground-check")).description("Tries to prevent you from falling to your death.")).defaultValue(true)).build());
        this.allProjectiles = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("all-projectiles")).description("Dodge all projectiles, not only arrows.")).defaultValue(false)).build());
        this.ignoreOwn = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-own")).description("Ignore your own projectiles.")).defaultValue(true)).build());
        this.simulationSteps = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("simulation-steps")).description("How many steps to simulate projectiles. Zero for no limit.")).defaultValue(500)).sliderMax(5000).build());
        this.possibleMoveDirections = Arrays.asList(new Vec3(1.0, 0.0, 1.0), new Vec3(0.0, 0.0, 1.0), new Vec3(-1.0, 0.0, 1.0), new Vec3(1.0, 0.0, 0.0), new Vec3(-1.0, 0.0, 0.0), new Vec3(1.0, 0.0, -1.0), new Vec3(0.0, 0.0, -1.0), new Vec3(-1.0, 0.0, -1.0));
        this.simulator = new ProjectileEntitySimulator();
        this.vec3s = new Pool<Vector3d>(Vector3d::new);
        this.points = new ArrayList<Vector3d>();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        this.vec3s.freeAll(this.points);
        this.points.clear();
        block0: for (Entity e : this.mc.level.entitiesForRendering()) {
            Entity owner;
            if (!(e instanceof Projectile)) continue;
            Projectile projectile = (Projectile)e;
            if (!this.allProjectiles.get().booleanValue() && !(projectile instanceof Arrow) && !(projectile instanceof SpectralArrow) || this.ignoreOwn.get().booleanValue() && (owner = projectile.getOwner()) != null && owner.getUUID().equals(this.mc.player.getUUID()) || !this.simulator.set((Entity)projectile)) continue;
            for (int i = 0; i < (this.simulationSteps.get() > 0 ? this.simulationSteps.get() : Integer.MAX_VALUE); ++i) {
                this.points.add(this.vec3s.get().set((Vector3dc)this.simulator.pos));
                if (this.simulator.tick().shouldStop) continue block0;
            }
        }
        if (this.isValid(Vec3.ZERO, false)) {
            return;
        }
        double speed = this.moveSpeed.get();
        for (int i = 0; i < 500; ++i) {
            boolean didMove = false;
            Collections.shuffle(this.possibleMoveDirections);
            for (Vec3 direction : this.possibleMoveDirections) {
                Vec3 velocity = direction.scale(speed);
                if (!this.isValid(velocity, true)) continue;
                this.move(velocity);
                didMove = true;
                break;
            }
            if (didMove) break;
            speed += this.moveSpeed.get().doubleValue();
        }
    }

    private void move(Vec3 vel) {
        this.move(vel.x, vel.y, vel.z);
    }

    private void move(double velX, double velY, double velZ) {
        switch (this.moveType.get().ordinal()) {
            case 0: {
                this.mc.player.setDeltaMovement(velX, velY, velZ);
                break;
            }
            case 1: {
                Vec3 newPos = this.mc.player.position().add(velX, velY, velZ);
                this.mc.player.connection.send((Packet)new ServerboundMovePlayerPacket.Pos(newPos.x, newPos.y, newPos.z, false, this.mc.player.horizontalCollision));
                this.mc.player.connection.send((Packet)new ServerboundMovePlayerPacket.Pos(newPos.x, newPos.y - 0.01, newPos.z, true, this.mc.player.horizontalCollision));
            }
        }
    }

    private boolean isValid(Vec3 velocity, boolean checkGround) {
        Vec3 playerPos = this.mc.player.position().add(velocity);
        Vec3 headPos = playerPos.add(0.0, 1.0, 0.0);
        for (Vector3d pos : this.points) {
            Vec3 projectilePos = new Vec3(pos.x, pos.y, pos.z);
            if (projectilePos.closerThan((Position)playerPos, this.distanceCheck.get().doubleValue())) {
                return false;
            }
            if (!projectilePos.closerThan((Position)headPos, this.distanceCheck.get().doubleValue())) continue;
            return false;
        }
        if (checkGround) {
            BlockPos blockPos = this.mc.player.blockPosition().offset((Vec3i)BlockPos.containing((double)velocity.x, (double)velocity.y, (double)velocity.z));
            if (!this.mc.level.getBlockState(blockPos).getCollisionShape((BlockGetter)this.mc.level, blockPos).isEmpty()) {
                return false;
            }
            if (!this.mc.level.getBlockState(blockPos.above()).getCollisionShape((BlockGetter)this.mc.level, blockPos.above()).isEmpty()) {
                return false;
            }
            if (this.groundCheck.get().booleanValue()) {
                return !this.mc.level.getBlockState(blockPos.below()).getCollisionShape((BlockGetter)this.mc.level, blockPos.below()).isEmpty();
            }
        }
        return true;
    }

    public static enum MoveType {
        Velocity,
        Packet;

    }
}
