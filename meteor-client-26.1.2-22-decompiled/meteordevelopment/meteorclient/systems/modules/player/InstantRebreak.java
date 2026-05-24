package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;

public class InstantRebreak
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<Integer> tickDelay;
    private final Setting<Boolean> pick;
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    public final BlockPos.MutableBlockPos blockPos;
    private int ticks;
    private Direction direction;

    public InstantRebreak() {
        super(Categories.Player, "instant-rebreak", "Instantly re-breaks blocks in the same position.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.tickDelay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("delay")).description("The delay between break attempts.")).defaultValue(0)).min(0).sliderMax(20).build());
        this.pick = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-pick")).description("Only tries to mine the block if you are holding a pickaxe.")).defaultValue(true)).build());
        this.rotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Faces the block being mined server side.")).defaultValue(true)).build());
        this.render = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render")).description("Renders an overlay on the block being broken.")).defaultValue(true)).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The color of the sides of the blocks being rendered.")).defaultValue(new SettingColor(204, 0, 0, 10)).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The color of the lines of the blocks being rendered.")).defaultValue(new SettingColor(204, 0, 0, 255)).build());
        this.blockPos = new BlockPos.MutableBlockPos(0, Integer.MIN_VALUE, 0);
    }

    @Override
    public void onActivate() {
        this.ticks = 0;
        this.blockPos.set(0, -1, 0);
    }

    @EventHandler
    private void onStartBreakingBlock(StartBreakingBlockEvent event) {
        this.direction = event.direction;
        this.blockPos.set((Vec3i)event.blockPos);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.ticks >= this.tickDelay.get()) {
            this.ticks = 0;
            if (this.shouldMine()) {
                if (this.rotate.get().booleanValue()) {
                    Rotations.rotate(Rotations.getYaw((BlockPos)this.blockPos), Rotations.getPitch((BlockPos)this.blockPos), this::sendPacket);
                } else {
                    this.sendPacket();
                }
                this.mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            }
        } else {
            ++this.ticks;
        }
    }

    public void sendPacket() {
        this.mc.gameMode.startPrediction(this.mc.level, sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, (BlockPos)this.blockPos, this.direction == null ? Direction.UP : this.direction, sequence));
    }

    public boolean shouldMine() {
        if (this.mc.level.isOutsideBuildHeight((BlockPos)this.blockPos) || !BlockUtils.canBreak((BlockPos)this.blockPos)) {
            return false;
        }
        return this.pick.get() == false || this.mc.player.getMainHandItem().is(ItemTags.PICKAXES);
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!this.render.get().booleanValue() || !this.shouldMine()) {
            return;
        }
        event.renderer.box((BlockPos)this.blockPos, (Color)this.sideColor.get(), (Color)this.lineColor.get(), this.shapeMode.get(), 0);
    }
}
