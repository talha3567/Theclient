package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.events.entity.player.InteractItemEvent;
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
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ArmorStandItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class AirPlace
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRange;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Setting<Boolean> customRange;
    private final Setting<Double> range;
    private HitResult hitResult;

    public AirPlace() {
        super(Categories.Player, "air-place", "Places a block where your crosshair is pointing at.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRange = this.settings.createGroup("Range");
        this.render = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render")).description("Renders a block overlay where the obsidian will be placed.")).defaultValue(true)).build());
        this.shapeMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.sideColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The color of the sides of the blocks being rendered.")).defaultValue(new SettingColor(204, 0, 0, 10)).build());
        this.lineColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The color of the lines of the blocks being rendered.")).defaultValue(new SettingColor(204, 0, 0, 255)).build());
        this.customRange = this.sgRange.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("custom-range")).description("Use custom range for air place.")).defaultValue(false)).build());
        this.range = this.sgRange.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("range")).description("Custom range to place at.")).visible(this.customRange::get)).defaultValue(5.0).min(0.0).sliderMax(6.0).build());
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!InvUtils.testInHands(this::placeable)) {
            return;
        }
        if (this.mc.hitResult != null && this.mc.hitResult.getType() != HitResult.Type.MISS) {
            return;
        }
        double r = this.customRange.get() != false ? this.range.get().doubleValue() : this.mc.player.blockInteractionRange();
        this.hitResult = this.mc.getCameraEntity().pick(r, 0.0f, false);
    }

    @EventHandler
    private void onInteractItem(InteractItemEvent event) {
        BlockHitResult bhr;
        block6: {
            block5: {
                HitResult hitResult = this.hitResult;
                if (!(hitResult instanceof BlockHitResult)) break block5;
                bhr = (BlockHitResult)hitResult;
                if (this.placeable(this.mc.player.getItemInHand(event.hand))) break block6;
            }
            return;
        }
        Block toPlace = Blocks.OBSIDIAN;
        Item i = this.mc.player.getItemInHand(event.hand).getItem();
        if (i instanceof BlockItem) {
            BlockItem blockItem = (BlockItem)i;
            toPlace = blockItem.getBlock();
        }
        if (!BlockUtils.canPlaceBlock(bhr.getBlockPos(), i instanceof ArmorStandItem || i instanceof BlockItem, toPlace)) {
            return;
        }
        Vec3 hitPos = Vec3.atCenterOf((Vec3i)bhr.getBlockPos());
        BlockHitResult b = new BlockHitResult(hitPos, this.mc.player.getMotionDirection().getOpposite(), bhr.getBlockPos(), false);
        BlockUtils.interact(b, event.hand, true);
        event.toReturn = InteractionResult.SUCCESS;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        BlockHitResult bhr;
        block3: {
            block2: {
                HitResult hitResult = this.hitResult;
                if (!(hitResult instanceof BlockHitResult)) break block2;
                bhr = (BlockHitResult)hitResult;
                if ((this.mc.hitResult == null || this.mc.hitResult.getType() == HitResult.Type.MISS) && this.mc.level.getBlockState(bhr.getBlockPos()).canBeReplaced() && InvUtils.testInHands(this::placeable) && this.render.get().booleanValue()) break block3;
            }
            return;
        }
        event.renderer.box(bhr.getBlockPos(), (Color)this.sideColor.get(), (Color)this.lineColor.get(), this.shapeMode.get(), 0);
    }

    private boolean placeable(ItemStack stack) {
        Item i = stack.getItem();
        return i instanceof BlockItem || i instanceof SpawnEggItem || i instanceof FireworkRocketItem || i instanceof ArmorStandItem;
    }
}
