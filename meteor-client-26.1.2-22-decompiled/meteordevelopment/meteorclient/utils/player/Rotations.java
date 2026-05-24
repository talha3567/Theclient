package meteordevelopment.meteorclient.utils.player;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.SendMovementPacketsEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.meteorclient.utils.entity.Target;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class Rotations {
    private static final Pool<Rotation> rotationPool = new Pool<Rotation>(Rotation::new);
    private static final List<Rotation> rotations = new ArrayList<Rotation>();
    public static float serverYaw;
    public static float serverPitch;
    public static int rotationTimer;
    private static float preYaw;
    private static float prePitch;
    private static int i;
    private static Rotation lastRotation;
    private static int lastRotationTimer;
    private static boolean sentLastRotation;
    public static boolean rotating;

    private Rotations() {
    }

    @PreInit
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(Rotations.class);
    }

    public static void rotate(double yaw, double pitch, int priority, boolean clientSide, Runnable callback) {
        int i;
        Rotation rotation = rotationPool.get();
        rotation.set(yaw, pitch, priority, clientSide, callback);
        for (i = 0; i < rotations.size() && priority <= Rotations.rotations.get((int)i).priority; ++i) {
        }
        rotations.add(i, rotation);
    }

    public static void rotate(double yaw, double pitch, int priority, Runnable callback) {
        Rotations.rotate(yaw, pitch, priority, false, callback);
    }

    public static void rotate(double yaw, double pitch, Runnable callback) {
        Rotations.rotate(yaw, pitch, 0, callback);
    }

    public static void rotate(double yaw, double pitch, int priority) {
        Rotations.rotate(yaw, pitch, priority, null);
    }

    public static void rotate(double yaw, double pitch) {
        Rotations.rotate(yaw, pitch, 0, null);
    }

    private static void resetLastRotation() {
        if (lastRotation != null) {
            rotationPool.free(lastRotation);
            lastRotation = null;
            lastRotationTimer = 0;
        }
    }

    @EventHandler
    private static void onSendMovementPacketsPre(SendMovementPacketsEvent.Pre event) {
        if (MeteorClient.mc.getCameraEntity() != MeteorClient.mc.player) {
            return;
        }
        sentLastRotation = false;
        if (!rotations.isEmpty()) {
            rotating = true;
            Rotations.resetLastRotation();
            Rotation rotation = rotations.get(i);
            Rotations.setupMovementPacketRotation(rotation);
            if (rotations.size() > 1) {
                rotationPool.free(rotation);
            }
            ++i;
        } else if (lastRotation != null) {
            if (lastRotationTimer >= Config.get().rotationHoldTicks.get()) {
                Rotations.resetLastRotation();
                rotating = false;
            } else {
                Rotations.setupMovementPacketRotation(lastRotation);
                sentLastRotation = true;
                ++lastRotationTimer;
            }
        }
    }

    private static void setupMovementPacketRotation(Rotation rotation) {
        Rotations.setClientRotation(rotation);
        Rotations.setCamRotation(rotation.yaw, rotation.pitch);
    }

    private static void setClientRotation(Rotation rotation) {
        preYaw = MeteorClient.mc.player.getYRot();
        prePitch = MeteorClient.mc.player.getXRot();
        MeteorClient.mc.player.setYRot((float)rotation.yaw);
        MeteorClient.mc.player.setXRot((float)rotation.pitch);
    }

    @EventHandler
    private static void onSendMovementPacketsPost(SendMovementPacketsEvent.Post event) {
        if (!rotations.isEmpty()) {
            if (MeteorClient.mc.getCameraEntity() == MeteorClient.mc.player) {
                rotations.get(i - 1).runCallback();
                if (rotations.size() == 1) {
                    lastRotation = rotations.get(i - 1);
                }
                Rotations.resetPreRotation();
            }
            while (i < rotations.size()) {
                Rotation rotation = rotations.get(i);
                Rotations.setCamRotation(rotation.yaw, rotation.pitch);
                if (rotation.clientSide) {
                    Rotations.setClientRotation(rotation);
                }
                rotation.sendPacket();
                if (rotation.clientSide) {
                    Rotations.resetPreRotation();
                }
                if (i == rotations.size() - 1) {
                    lastRotation = rotation;
                } else {
                    rotationPool.free(rotation);
                }
                ++i;
            }
            rotations.clear();
            i = 0;
        } else if (sentLastRotation) {
            Rotations.resetPreRotation();
        }
    }

    private static void resetPreRotation() {
        MeteorClient.mc.player.setYRot(preYaw);
        MeteorClient.mc.player.setXRot(prePitch);
    }

    @EventHandler
    private static void onTick(TickEvent.Pre event) {
        ++rotationTimer;
    }

    public static double getYaw(Entity entity) {
        return MeteorClient.mc.player.getYRot() + Mth.wrapDegrees((float)((float)Math.toDegrees(Math.atan2(entity.getZ() - MeteorClient.mc.player.getZ(), entity.getX() - MeteorClient.mc.player.getX())) - 90.0f - MeteorClient.mc.player.getYRot()));
    }

    public static double getYaw(Vec3 pos) {
        return MeteorClient.mc.player.getYRot() + Mth.wrapDegrees((float)((float)Math.toDegrees(Math.atan2(pos.z() - MeteorClient.mc.player.getZ(), pos.x() - MeteorClient.mc.player.getX())) - 90.0f - MeteorClient.mc.player.getYRot()));
    }

    public static double getPitch(Vec3 pos) {
        double diffX = pos.x() - MeteorClient.mc.player.getX();
        double diffY = pos.y() - (MeteorClient.mc.player.getY() + (double)MeteorClient.mc.player.getEyeHeight(MeteorClient.mc.player.getPose()));
        double diffZ = pos.z() - MeteorClient.mc.player.getZ();
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        return MeteorClient.mc.player.getXRot() + Mth.wrapDegrees((float)((float)(-Math.toDegrees(Math.atan2(diffY, diffXZ))) - MeteorClient.mc.player.getXRot()));
    }

    public static double getPitch(Entity entity, Target target) {
        double y = switch (target) {
            default -> throw new MatchException(null, null);
            case Target.Head -> entity.getEyeY();
            case Target.Body -> entity.getY() + (double)(entity.getBbHeight() / 2.0f);
            case Target.Feet -> entity.getY();
        };
        double diffX = entity.getX() - MeteorClient.mc.player.getX();
        double diffY = y - (MeteorClient.mc.player.getY() + (double)MeteorClient.mc.player.getEyeHeight(MeteorClient.mc.player.getPose()));
        double diffZ = entity.getZ() - MeteorClient.mc.player.getZ();
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        return MeteorClient.mc.player.getXRot() + Mth.wrapDegrees((float)((float)(-Math.toDegrees(Math.atan2(diffY, diffXZ))) - MeteorClient.mc.player.getXRot()));
    }

    public static double getPitch(Entity entity) {
        return Rotations.getPitch(entity, Target.Body);
    }

    public static double getYaw(BlockPos pos) {
        return MeteorClient.mc.player.getYRot() + Mth.wrapDegrees((float)((float)Math.toDegrees(Math.atan2((double)pos.getZ() + 0.5 - MeteorClient.mc.player.getZ(), (double)pos.getX() + 0.5 - MeteorClient.mc.player.getX())) - 90.0f - MeteorClient.mc.player.getYRot()));
    }

    public static double getPitch(BlockPos pos) {
        double diffX = (double)pos.getX() + 0.5 - MeteorClient.mc.player.getX();
        double diffY = (double)pos.getY() + 0.5 - (MeteorClient.mc.player.getY() + (double)MeteorClient.mc.player.getEyeHeight(MeteorClient.mc.player.getPose()));
        double diffZ = (double)pos.getZ() + 0.5 - MeteorClient.mc.player.getZ();
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        return MeteorClient.mc.player.getXRot() + Mth.wrapDegrees((float)((float)(-Math.toDegrees(Math.atan2(diffY, diffXZ))) - MeteorClient.mc.player.getXRot()));
    }

    public static void setCamRotation(double yaw, double pitch) {
        serverYaw = (float)yaw;
        serverPitch = (float)pitch;
        rotationTimer = 0;
    }

    static {
        i = 0;
        rotating = false;
    }

    private static class Rotation {
        public double yaw;
        public double pitch;
        public int priority;
        public boolean clientSide;
        public Runnable callback;

        private Rotation() {
        }

        public void set(double yaw, double pitch, int priority, boolean clientSide, Runnable callback) {
            this.yaw = yaw;
            this.pitch = pitch;
            this.priority = priority;
            this.clientSide = clientSide;
            this.callback = callback;
        }

        public void sendPacket() {
            MeteorClient.mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.Rot((float)this.yaw, (float)this.pitch, MeteorClient.mc.player.onGround(), MeteorClient.mc.player.horizontalCollision));
            this.runCallback();
        }

        public void runCallback() {
            if (this.callback != null) {
                this.callback.run();
            }
        }
    }
}
