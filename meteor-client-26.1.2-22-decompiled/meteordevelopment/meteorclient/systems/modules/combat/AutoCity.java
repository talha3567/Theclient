package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

public class AutoCity
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<Double> targetRange;
    private final Setting<Double> breakRange;
    private final Setting<SwitchMode> switchMode;
    private final Setting<Boolean> support;
    private final Setting<Double> placeRange;
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> chatInfo;
    private final Setting<Boolean> swingHand;
    private final Setting<Boolean> renderBlock;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private Player target;
    private BlockPos targetPos;
    private FindItemResult pick;
    private float progress;

    public AutoCity() {
        super(Categories.Combat, "auto-city", "Automatically mine blocks next to someone's feet.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.targetRange = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("target-range")).description("The radius in which players get targeted.")).defaultValue(5.5).min(0.0).sliderMax(7.0).build());
        this.breakRange = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("break-range")).description("How close a block must be to you to be considered.")).defaultValue(4.5).min(0.0).sliderMax(6.0).build());
        this.switchMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("switch-mode")).description("How to switch to a pickaxe.")).defaultValue(SwitchMode.Normal)).build());
        this.support = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("support")).description("If there is no block below a city block it will place one before mining.")).defaultValue(true)).build());
        this.placeRange = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("place-range")).description("How far away to try and place a block.")).defaultValue(4.5).min(0.0).sliderMax(6.0).visible(this.support::get)).build());
        this.rotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Automatically rotates you towards the city block.")).defaultValue(true)).build());
        this.chatInfo = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("chat-info")).description("Whether the module should send messages in chat.")).defaultValue(true)).build());
        this.swingHand = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("swing-hand")).description("Whether to render your hand swinging.")).defaultValue(false)).build());
        this.renderBlock = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-block")).description("Whether to render the block being broken.")).defaultValue(true)).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).visible(this.renderBlock::get)).build());
        this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The side color of the rendering.")).defaultValue(new SettingColor(225, 0, 0, 75)).visible(() -> this.renderBlock.get() != false && this.shapeMode.get().sides())).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The line color of the rendering.")).defaultValue(new SettingColor(225, 0, 0, 255)).visible(() -> this.renderBlock.get() != false && this.shapeMode.get().lines())).build());
    }

    @Override
    public void onActivate() {
        BlockPos supportPos;
        this.target = TargetUtils.getPlayerTarget(this.targetRange.get(), SortPriority.ClosestAngle);
        if (TargetUtils.isBadTarget(this.target, this.targetRange.get())) {
            if (this.chatInfo.get().booleanValue()) {
                this.error("Couldn't find a target, disabling.", new Object[0]);
            }
            this.toggle();
            return;
        }
        this.targetPos = EntityUtils.getCityBlock(this.target);
        if (this.targetPos == null || PlayerUtils.squaredDistanceTo(this.targetPos) > Math.pow(this.breakRange.get(), 2.0)) {
            if (this.chatInfo.get().booleanValue()) {
                this.error("Couldn't find a good block, disabling.", new Object[0]);
            }
            this.toggle();
            return;
        }
        if (this.support.get().booleanValue() && PlayerUtils.squaredDistanceTo(supportPos = this.targetPos.below()) <= Math.pow(this.placeRange.get(), 2.0)) {
            BlockUtils.place(supportPos, InvUtils.findInHotbar(Items.OBSIDIAN), this.rotate.get(), 0, true);
        }
        this.pick = InvUtils.find(itemStack -> itemStack.getItem() == Items.DIAMOND_PICKAXE || itemStack.getItem() == Items.NETHERITE_PICKAXE);
        if (!this.pick.isHotbar()) {
            this.error("No pickaxe found... disabling.", new Object[0]);
            this.toggle();
            return;
        }
        this.progress = 0.0f;
        this.mine(false);
    }

    @Override
    public void onDeactivate() {
        this.target = null;
        this.targetPos = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (TargetUtils.isBadTarget(this.target, this.targetRange.get())) {
            this.toggle();
            return;
        }
        if (PlayerUtils.squaredDistanceTo(this.targetPos) > Math.pow(this.breakRange.get(), 2.0)) {
            if (this.chatInfo.get().booleanValue()) {
                this.error("Couldn't find a target, disabling.", new Object[0]);
            }
            this.toggle();
            return;
        }
        if (this.progress < 1.0f) {
            this.pick = InvUtils.find(itemStack -> itemStack.getItem() == Items.DIAMOND_PICKAXE || itemStack.getItem() == Items.NETHERITE_PICKAXE);
            if (!this.pick.isHotbar()) {
                this.error("No pickaxe found... disabling.", new Object[0]);
                this.toggle();
                return;
            }
            this.progress = (float)((double)this.progress + BlockUtils.getBreakDelta(this.pick.slot(), this.mc.level.getBlockState(this.targetPos)));
            if (this.progress < 1.0f) {
                return;
            }
        }
        this.mine(true);
        this.toggle();
    }

    public void mine(boolean done) {
        InvUtils.swap(this.pick.slot(), this.switchMode.get() == SwitchMode.Silent);
        if (this.rotate.get().booleanValue()) {
            Rotations.rotate(Rotations.getYaw(this.targetPos), Rotations.getPitch(this.targetPos));
        }
        Direction direction = BlockUtils.getDirection(this.targetPos);
        if (!done) {
            this.mc.gameMode.startPrediction(this.mc.level, sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.targetPos, direction, sequence));
        }
        this.mc.gameMode.startPrediction(this.mc.level, sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.targetPos, direction, sequence));
        if (this.swingHand.get().booleanValue()) {
            this.mc.player.swing(InteractionHand.MAIN_HAND);
        } else {
            this.mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        }
        if (this.switchMode.get() == SwitchMode.Silent) {
            InvUtils.swapBack();
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (this.targetPos == null || !this.renderBlock.get().booleanValue()) {
            return;
        }
        event.renderer.box(this.targetPos, (Color)this.sideColor.get(), (Color)this.lineColor.get(), this.shapeMode.get(), 0);
    }

    @Override
    public String getInfoString() {
        return EntityUtils.getName((Entity)this.target);
    }

    public static enum SwitchMode {
        Normal,
        Silent;

    }
}
