package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.entity.player.ClipAtLedgeEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SafeWalk
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<Integer> fallDistance;
    private final Setting<Boolean> sneak;
    private final Setting<Boolean> safeSneak;
    private final Setting<Boolean> sneakSprint;
    private final Setting<Double> edgeDistance;
    private final Setting<Boolean> renderEdgeDistance;
    private final Setting<Boolean> renderPlayerBox;

    public SafeWalk() {
        super(Categories.Movement, "safe-walk", "Prevents you from walking off blocks.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.fallDistance = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("minimum-fall-distance")).description("The minimum number of blocks you are expected to fall before the module activates.")).defaultValue(1)).min(1).build());
        this.sneak = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sneak")).description("Sneak when approaching edge of block.")).defaultValue(false)).build());
        this.safeSneak = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("safe-sneak")).description("Prevent you from falling if sneak doesn't trigger correctly.")).defaultValue(true)).visible(this.sneak::get)).build());
        this.sneakSprint = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sneak-on-sprint")).description("Sneak even when sprinting at the block edge.")).defaultValue(true)).visible(this.sneak::get)).build());
        this.edgeDistance = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("edge-distance")).description("Distance offset before reaching an edge.")).defaultValue(0.3).sliderRange(0.0, 0.3).decimalPlaces(2).visible(this.sneak::get)).build());
        this.renderEdgeDistance = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render")).description("Render edge distance helper.")).defaultValue(false)).visible(this.sneak::get)).build());
        this.renderPlayerBox = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-player-box")).description("Render player box helper.")).defaultValue(false)).visible(() -> this.sneak.get() != false && this.renderEdgeDistance.get() != false)).build());
    }

    @EventHandler
    private void onClipAtLedge(ClipAtLedgeEvent event) {
        if (this.fallDistance.get() > 1) {
            BlockHitResult raycastResult;
            int surface = this.mc.level.getChunkAt(this.mc.player.blockPosition()).getOrCreateHeightmapUnprimed(Heightmap.Types.MOTION_BLOCKING).getFirstAvailable(this.mc.player.getBlockX() & 0xF, this.mc.player.getBlockZ() & 0xF);
            if (this.mc.player.getBlockY() >= surface ? this.mc.player.getBlockY() - surface < this.fallDistance.get() : (raycastResult = this.mc.level.clip(new ClipContext(this.mc.player.position(), new Vec3(this.mc.player.getX(), (double)this.mc.level.getMinY(), this.mc.player.getZ()), ClipContext.Block.COLLIDER, ClipContext.Fluid.WATER, (Entity)this.mc.player))).getType() != HitResult.Type.MISS && (int)(this.mc.player.getY() - (double)raycastResult.getBlockPos().above().getY()) < this.fallDistance.get()) {
                return;
            }
        }
        if (this.sneak.get().booleanValue()) {
            boolean closeToEdge = false;
            boolean isSprinting = this.sneakSprint.get() == false && this.mc.options.keySprint.isDown();
            AABB playerBox = this.mc.player.getBoundingBox();
            AABB adjustedBox = this.getAdjustedPlayerBox(playerBox);
            if (this.mc.level.noCollision((Entity)this.mc.player, adjustedBox) && this.mc.player.onGround()) {
                closeToEdge = true;
            }
            if (!isSprinting) {
                if (closeToEdge) {
                    this.mc.player.input.keyPresses = new Input(this.mc.player.input.keyPresses.forward(), this.mc.player.input.keyPresses.backward(), this.mc.player.input.keyPresses.left(), this.mc.player.input.keyPresses.right(), this.mc.player.input.keyPresses.jump(), true, this.mc.player.input.keyPresses.sprint());
                } else if (this.safeSneak.get().booleanValue()) {
                    event.setClip(true);
                }
            }
        } else if (!this.mc.player.isShiftKeyDown()) {
            event.setClip(true);
        }
    }

    private AABB getAdjustedPlayerBox(AABB playerBox) {
        return playerBox.expandTowards(0.0, (double)(-this.mc.player.maxUpStep()), 0.0).inflate(-this.edgeDistance.get().doubleValue(), 0.0, -this.edgeDistance.get().doubleValue());
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.sneak.get().booleanValue() && this.renderEdgeDistance.get().booleanValue()) {
            AABB playerBox = this.mc.player.getBoundingBox();
            AABB adjustedBox = this.getAdjustedPlayerBox(playerBox);
            event.renderer.box(adjustedBox, Color.BLUE, Color.RED, ShapeMode.Lines, 0);
            if (this.renderPlayerBox.get().booleanValue()) {
                event.renderer.box(playerBox, Color.BLUE, Color.GREEN, ShapeMode.Lines, 0);
            }
        }
    }
}
