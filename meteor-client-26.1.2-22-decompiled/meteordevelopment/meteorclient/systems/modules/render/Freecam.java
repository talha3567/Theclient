package meteordevelopment.meteorclient.systems.modules.render;

import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.meteor.KeyInputEvent;
import meteordevelopment.meteorclient.events.meteor.MouseClickEvent;
import meteordevelopment.meteorclient.events.meteor.MouseScrollEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.ChunkOcclusionEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.GUIMove;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class Freecam
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgPathing;
    private final Setting<Double> speed;
    private final Setting<Double> speedScrollSensitivity;
    private final Setting<Boolean> staySneaking;
    private final Setting<Boolean> toggleOnDamage;
    private final Setting<Boolean> toggleOnDeath;
    private final Setting<Boolean> toggleOnLog;
    private final Setting<Boolean> reloadChunks;
    private final Setting<Boolean> renderHands;
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> staticView;
    private final Setting<Boolean> baritoneClick;
    private final Setting<Boolean> requireDoubleClick;
    public final Vector3d pos;
    public final Vector3d prevPos;
    private CameraType perspective;
    private double speedValue;
    public float yaw;
    public float pitch;
    public float lastYaw;
    public float lastPitch;
    private double fovScale;
    private boolean bobView;
    private boolean forward;
    private boolean backward;
    private boolean right;
    private boolean left;
    private boolean up;
    private boolean down;
    private boolean isSneaking;
    private long clickTs;

    public Freecam() {
        super(Categories.Render, "freecam", "Allows the camera to move away from the player.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgPathing = this.settings.createGroup("Pathing");
        this.speed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("speed")).description("Your speed while in freecam.")).onChanged(aDouble -> {
            this.speedValue = aDouble;
        })).defaultValue(1.0).min(0.0).build());
        this.speedScrollSensitivity = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("speed-scroll-sensitivity")).description("Allows you to change speed value using scroll wheel. 0 to disable.")).defaultValue(0.0).min(0.0).sliderMax(2.0).build());
        this.staySneaking = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("stay-sneaking")).description("If you are sneaking when you enter freecam, whether your player should remain sneaking.")).defaultValue(true)).build());
        this.toggleOnDamage = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("toggle-on-damage")).description("Disables freecam when you take damage.")).defaultValue(false)).build());
        this.toggleOnDeath = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("toggle-on-death")).description("Disables freecam when you die.")).defaultValue(false)).build());
        this.toggleOnLog = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("toggle-on-log")).description("Disables freecam when you disconnect from a server.")).defaultValue(true)).build());
        this.reloadChunks = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("reload-chunks")).description("Disables cave culling.")).defaultValue(true)).build());
        this.renderHands = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-hands")).description("Whether or not to render your hands in freecam.")).defaultValue(true)).build());
        this.rotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Rotates to the block or entity you are looking at.")).defaultValue(false)).build());
        this.staticView = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("static")).description("Disables settings that move the view.")).defaultValue(true)).build());
        this.baritoneClick = this.sgPathing.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("click-to-path")).description("Sets a pathfinding goal to any block/entity you click at.")).defaultValue(false)).build());
        this.requireDoubleClick = this.sgPathing.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("double-click")).description("Require two clicks to start pathing.")).defaultValue(false)).build());
        this.pos = new Vector3d();
        this.prevPos = new Vector3d();
        this.clickTs = 0L;
    }

    @Override
    public void onActivate() {
        this.fovScale = (Double)this.mc.options.fovEffectScale().get();
        this.bobView = (Boolean)this.mc.options.bobView().get();
        if (this.staticView.get().booleanValue()) {
            this.mc.options.fovEffectScale().set((Object)0.0);
            this.mc.options.bobView().set((Object)false);
        }
        this.yaw = this.mc.player.getYRot();
        this.pitch = this.mc.player.getXRot();
        this.perspective = this.mc.options.getCameraType();
        this.speedValue = this.speed.get();
        Utils.set(this.pos, this.mc.gameRenderer.getMainCamera().position());
        Utils.set(this.prevPos, this.mc.gameRenderer.getMainCamera().position());
        if (this.mc.options.getCameraType() == CameraType.THIRD_PERSON_FRONT) {
            this.yaw += 180.0f;
            this.pitch *= -1.0f;
        }
        this.lastYaw = this.yaw;
        this.lastPitch = this.pitch;
        this.isSneaking = this.mc.options.keyShift.isDown();
        this.forward = Input.isPressed(this.mc.options.keyUp);
        this.backward = Input.isPressed(this.mc.options.keyDown);
        this.right = Input.isPressed(this.mc.options.keyRight);
        this.left = Input.isPressed(this.mc.options.keyLeft);
        this.up = Input.isPressed(this.mc.options.keyJump);
        this.down = Input.isPressed(this.mc.options.keyShift);
        this.unpress();
        if (this.reloadChunks.get().booleanValue()) {
            this.mc.levelRenderer.allChanged();
        }
    }

    @Override
    public void onDeactivate() {
        if (this.reloadChunks.get().booleanValue()) {
            this.mc.execute(() -> ((LevelRenderer)this.mc.levelRenderer).allChanged());
        }
        this.mc.options.setCameraType(this.perspective);
        if (this.staticView.get().booleanValue()) {
            this.mc.options.fovEffectScale().set((Object)this.fovScale);
            this.mc.options.bobView().set((Object)this.bobView);
        }
        this.isSneaking = false;
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        this.unpress();
        this.prevPos.set((Vector3dc)this.pos);
        this.lastYaw = this.yaw;
        this.lastPitch = this.pitch;
    }

    private void unpress() {
        this.mc.options.keyUp.setDown(false);
        this.mc.options.keyDown.setDown(false);
        this.mc.options.keyRight.setDown(false);
        this.mc.options.keyLeft.setDown(false);
        this.mc.options.keyJump.setDown(false);
        this.mc.options.keyShift.setDown(false);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.getCameraEntity().isInWall()) {
            this.mc.getCameraEntity().noPhysics = true;
        }
        if (!this.perspective.isFirstPerson()) {
            this.mc.options.setCameraType(CameraType.FIRST_PERSON);
        }
        Vec3 forward = Vec3.directionFromRotation((float)0.0f, (float)this.yaw);
        Vec3 right = Vec3.directionFromRotation((float)0.0f, (float)(this.yaw + 90.0f));
        double velX = 0.0;
        double velY = 0.0;
        double velZ = 0.0;
        if (this.rotate.get().booleanValue()) {
            HitResult hitResult = this.mc.hitResult;
            if (hitResult instanceof EntityHitResult) {
                EntityHitResult ehr = (EntityHitResult)hitResult;
                crossHairPos = ehr.getEntity().blockPosition();
                Rotations.rotate(Rotations.getYaw(crossHairPos), Rotations.getPitch(crossHairPos), 0, null);
            } else {
                Vec3 crossHairPosition = this.mc.hitResult.getLocation();
                crossHairPos = ((BlockHitResult)this.mc.hitResult).getBlockPos();
                if (!this.mc.level.getBlockState(crossHairPos).isAir()) {
                    Rotations.rotate(Rotations.getYaw(crossHairPosition), Rotations.getPitch(crossHairPosition), 0, null);
                }
            }
        }
        double s = 0.5;
        if (Input.isPressed(this.mc.options.keySprint)) {
            s = 1.0;
        }
        boolean a = false;
        if (this.forward) {
            velX += forward.x * s * this.speedValue;
            velZ += forward.z * s * this.speedValue;
            a = true;
        }
        if (this.backward) {
            velX -= forward.x * s * this.speedValue;
            velZ -= forward.z * s * this.speedValue;
            a = true;
        }
        boolean b = false;
        if (this.right) {
            velX += right.x * s * this.speedValue;
            velZ += right.z * s * this.speedValue;
            b = true;
        }
        if (this.left) {
            velX -= right.x * s * this.speedValue;
            velZ -= right.z * s * this.speedValue;
            b = true;
        }
        if (a && b) {
            double diagonal = 1.0 / Math.sqrt(2.0);
            velX *= diagonal;
            velZ *= diagonal;
        }
        if (this.up) {
            velY += s * this.speedValue;
        }
        if (this.down) {
            velY -= s * this.speedValue;
        }
        this.prevPos.set((Vector3dc)this.pos);
        this.pos.set(this.pos.x + velX, this.pos.y + velY, this.pos.z + velZ);
    }

    @EventHandler(priority=100)
    public void onKey(KeyInputEvent event) {
        if (Input.isKeyPressed(292)) {
            return;
        }
        if (this.checkGuiMove()) {
            return;
        }
        if (this.onInput(event.key(), event.action)) {
            event.cancel();
        }
    }

    @Nullable
    private BlockPos rayCastEntity(Vec3 posVec, Vec3 max, short maxDist) {
        EntityHitResult res = ProjectileUtil.getEntityHitResult((Entity)this.mc.player, (Vec3)posVec, (Vec3)max, (AABB)AABB.encapsulatingFullBlocks((BlockPos)BlockPos.containing((double)posVec.x, (double)posVec.y, (double)posVec.z), (BlockPos)BlockPos.containing((double)max.x, (double)max.y, (double)max.z)), entity -> true, (double)maxDist);
        if (res == null) {
            return null;
        }
        Vec3 vec = res.getLocation();
        return BlockPos.containing((double)vec.x, (double)vec.y, (double)vec.z);
    }

    @Nullable
    private BlockPos rayCastBlock(Vec3 posVec, Vec3 max) {
        ClipContext ctx = new ClipContext(posVec, max, ClipContext.Block.VISUAL, ClipContext.Fluid.SOURCE_ONLY, CollisionContext.empty());
        BlockHitResult res = this.mc.level.clip(ctx);
        if (res.getType() == HitResult.Type.MISS) {
            return null;
        }
        return res.getBlockPos().offset(res.getDirection().getUnitVec3i());
    }

    private void setGoal() {
        short maxDist;
        Vec3 lookVec;
        Vec3 max;
        long prevClick = this.clickTs;
        this.clickTs = System.currentTimeMillis();
        if (this.requireDoubleClick.get().booleanValue() && this.clickTs - prevClick > 500L) {
            return;
        }
        Camera cam = this.mc.gameRenderer.getMainCamera();
        Vec3 posVec = cam.position();
        BlockPos pos = this.rayCastEntity(posVec, max = posVec.add((lookVec = Vec3.directionFromRotation((float)cam.xRot(), (float)cam.yRot())).scale((double)(maxDist = 256))), maxDist);
        if (pos == null) {
            pos = this.rayCastBlock(posVec, max);
        }
        if (pos == null) {
            return;
        }
        PathManagers.get().moveTo(pos);
    }

    @EventHandler(priority=100)
    private void onMouseClick(MouseClickEvent event) {
        if (this.checkGuiMove()) {
            return;
        }
        if (this.baritoneClick.get().booleanValue() && event.action == KeyAction.Press && this.mc.options.keyAttack.matchesMouse(event.click)) {
            this.setGoal();
        }
        if (this.onInput(event.button(), event.action)) {
            event.cancel();
        }
    }

    private boolean onInput(int key, KeyAction action) {
        if (Input.getKey(this.mc.options.keyUp) == key) {
            this.forward = action != KeyAction.Release;
            this.mc.options.keyUp.setDown(false);
        } else if (Input.getKey(this.mc.options.keyDown) == key) {
            this.backward = action != KeyAction.Release;
            this.mc.options.keyDown.setDown(false);
        } else if (Input.getKey(this.mc.options.keyRight) == key) {
            this.right = action != KeyAction.Release;
            this.mc.options.keyRight.setDown(false);
        } else if (Input.getKey(this.mc.options.keyLeft) == key) {
            this.left = action != KeyAction.Release;
            this.mc.options.keyLeft.setDown(false);
        } else if (Input.getKey(this.mc.options.keyJump) == key) {
            this.up = action != KeyAction.Release;
            this.mc.options.keyJump.setDown(false);
        } else if (Input.getKey(this.mc.options.keyShift) == key) {
            this.down = action != KeyAction.Release;
            this.mc.options.keyShift.setDown(false);
        } else {
            return false;
        }
        return true;
    }

    @EventHandler(priority=-100)
    private void onMouseScroll(MouseScrollEvent event) {
        if (this.speedScrollSensitivity.get() > 0.0 && this.mc.screen == null) {
            this.speedValue += event.value * 0.25 * (this.speedScrollSensitivity.get() * this.speedValue);
            if (this.speedValue < 0.1) {
                this.speedValue = 0.1;
            }
            event.cancel();
        }
    }

    @EventHandler
    private void onChunkOcclusion(ChunkOcclusionEvent event) {
        event.cancel();
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (!this.toggleOnLog.get().booleanValue()) {
            return;
        }
        this.toggle();
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        Packet<?> packet = event.packet;
        if (packet instanceof ClientboundPlayerCombatKillPacket) {
            ClientboundPlayerCombatKillPacket packet2 = (ClientboundPlayerCombatKillPacket)packet;
            Entity entity = this.mc.level.getEntity(packet2.playerId());
            if (entity == this.mc.player && this.toggleOnDeath.get().booleanValue()) {
                this.toggle();
                this.info("Toggled off because you died.", new Object[0]);
            }
        } else {
            packet = event.packet;
            if (packet instanceof ClientboundSetHealthPacket) {
                ClientboundSetHealthPacket packet3 = (ClientboundSetHealthPacket)packet;
                if (this.mc.player.getHealth() - packet3.getHealth() > 0.0f && this.toggleOnDamage.get().booleanValue()) {
                    this.toggle();
                    this.info("Toggled off because you took damage.", new Object[0]);
                }
            } else if (event.packet instanceof ClientboundRespawnPacket && this.isActive()) {
                this.toggle();
                this.info("Toggled off because you changed dimensions.", new Object[0]);
            }
        }
    }

    private boolean checkGuiMove() {
        GUIMove guiMove = Modules.get().get(GUIMove.class);
        if (this.mc.screen != null && !guiMove.isActive()) {
            return true;
        }
        return this.mc.screen != null && guiMove.isActive() && guiMove.skip();
    }

    public void changeLookDirection(double deltaX, double deltaY) {
        this.lastYaw = this.yaw;
        this.lastPitch = this.pitch;
        this.yaw += (float)deltaX;
        this.pitch += (float)deltaY;
        this.pitch = Mth.clamp((float)this.pitch, (float)-90.0f, (float)90.0f);
    }

    public boolean renderHands() {
        return !this.isActive() || this.renderHands.get() != false;
    }

    public boolean staySneaking() {
        return this.isActive() && !this.mc.player.getAbilities().flying && this.staySneaking.get() != false && this.isSneaking;
    }

    public double getX(float tickDelta) {
        return Mth.lerp((double)tickDelta, (double)this.prevPos.x, (double)this.pos.x);
    }

    public double getY(float tickDelta) {
        return Mth.lerp((double)tickDelta, (double)this.prevPos.y, (double)this.pos.y);
    }

    public double getZ(float tickDelta) {
        return Mth.lerp((double)tickDelta, (double)this.prevPos.z, (double)this.pos.z);
    }

    public double getYaw(float tickDelta) {
        return Mth.lerp((float)tickDelta, (float)this.lastYaw, (float)this.yaw);
    }

    public double getPitch(float tickDelta) {
        return Mth.lerp((float)tickDelta, (float)this.lastPitch, (float)this.pitch);
    }
}
