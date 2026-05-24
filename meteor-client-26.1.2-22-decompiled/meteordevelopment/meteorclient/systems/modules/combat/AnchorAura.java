package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.CrystalAura;
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AnchorAura
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgPlace;
    private final SettingGroup sgBreak;
    private final SettingGroup sgPause;
    private final SettingGroup sgRender;
    private final Setting<Double> targetRange;
    private final Setting<SortPriority> targetPriority;
    private final Setting<Double> minDamage;
    private final Setting<Double> maxSelfDamage;
    private final Setting<Boolean> antiSuicide;
    private final Setting<Boolean> swapBack;
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> place;
    private final Setting<Integer> placeDelay;
    private final Setting<Double> placeRange;
    private final Setting<Double> placeWallsRange;
    private final Setting<Boolean> airPlace;
    private final Setting<Integer> chargeDelay;
    private final Setting<Integer> breakDelay;
    private final Setting<Double> breakRange;
    private final Setting<Double> breakWallsRange;
    private final Setting<Boolean> pauseOnUse;
    private final Setting<Boolean> pauseOnMine;
    private final Setting<Boolean> pauseOnCA;
    private final Setting<Boolean> swing;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private double bestPlaceDamage;
    private final BlockPos.MutableBlockPos bestPlacePos;
    private double bestBreakDamage;
    private final BlockPos.MutableBlockPos bestBreakPos;
    private BlockPos renderBlockPos;
    private int placeDelayLeft;
    private int chargeDelayLeft;
    private int breakDelayLeft;
    private Player target;

    public AnchorAura() {
        super(Categories.Combat, "anchor-aura", "Automatically places and breaks Respawn Anchors to harm entities.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgPlace = this.settings.createGroup("Place");
        this.sgBreak = this.settings.createGroup("Break");
        this.sgPause = this.settings.createGroup("Pause");
        this.sgRender = this.settings.createGroup("Render");
        this.targetRange = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("target-range")).description("Range in which to target players.")).defaultValue(10.0).min(0.0).sliderMax(16.0).build());
        this.targetPriority = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("target-priority")).description("How to select the player to target.")).defaultValue(SortPriority.LowestHealth)).build());
        this.minDamage = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("min-damage")).description("The minimum damage to inflict on your target.")).defaultValue(7.0).min(0.0).sliderMax(36.0).build());
        this.maxSelfDamage = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("max-self-damage")).description("The maximum damage to inflict on yourself.")).defaultValue(7.0).min(0.0).sliderMax(36.0).build());
        this.antiSuicide = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("anti-suicide")).description("Will not place and break anchors if they will kill you.")).defaultValue(true)).build());
        this.swapBack = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("swap-back")).description("Switches to your previous slot after using anchors.")).defaultValue(true)).build());
        this.rotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Rotates server-side towards the anchors being placed/broken.")).defaultValue(true)).build());
        this.place = this.sgPlace.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("place")).description("Allows Anchor Aura to place anchors.")).defaultValue(true)).build());
        this.placeDelay = this.sgPlace.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("place-delay")).description("The tick delay between placing anchors.")).defaultValue(5)).range(0, 10).visible(this.place::get)).build());
        this.placeRange = this.sgPlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("place-range")).description("The range at which anchors can be placed.")).defaultValue(4.0).range(0.0, 6.0).visible(this.place::get)).build());
        this.placeWallsRange = this.sgPlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("walls-range")).description("Range in which to place anchors when behind blocks.")).defaultValue(4.0).range(0.0, 6.0).visible(this.place::get)).build());
        this.airPlace = this.sgPlace.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("air-place")).description("Allows Anchor Aura to place anchors in the air.")).defaultValue(true)).visible(this.place::get)).build());
        this.chargeDelay = this.sgBreak.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("charge-delay")).description("The tick delay it takes to charge anchors.")).defaultValue(1)).range(0, 10).build());
        this.breakDelay = this.sgBreak.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("break-delay")).description("The tick delay it takes to break anchors.")).defaultValue(1)).range(0, 10).build());
        this.breakRange = this.sgBreak.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("break-range")).description("Range in which to break anchors.")).defaultValue(4.5).min(0.0).sliderMax(6.0).build());
        this.breakWallsRange = this.sgBreak.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("walls-range")).description("Range in which to break anchors when behind blocks.")).defaultValue(4.5).min(0.0).sliderMax(6.0).build());
        this.pauseOnUse = this.sgPause.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-on-use")).description("Pauses while using an item.")).defaultValue(true)).build());
        this.pauseOnMine = this.sgPause.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-on-mine")).description("Pauses while mining blocks.")).defaultValue(true)).build());
        this.pauseOnCA = this.sgPause.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-on-CA")).description("Pauses while Crystal Aura is placing.")).defaultValue(true)).build());
        this.swing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("swing")).description("Whether to swing your hand client-side.")).defaultValue(true)).build());
        this.render = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render")).description("Renders the block where it is placing an anchor.")).defaultValue(true)).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).visible(this.render::get)).build());
        this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The side color for positions to be placed.")).defaultValue(new SettingColor(15, 255, 211, 41)).visible(() -> this.render.get() != false && this.shapeMode.get().sides())).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The line color for positions to be placed.")).defaultValue(new SettingColor(15, 255, 211)).visible(() -> this.render.get() != false && this.shapeMode.get().lines())).build());
        this.bestPlacePos = new BlockPos.MutableBlockPos();
        this.bestBreakPos = new BlockPos.MutableBlockPos();
    }

    @Override
    public void onActivate() {
        this.renderBlockPos = null;
        this.placeDelayLeft = this.placeDelay.get();
        this.chargeDelayLeft = 0;
        this.breakDelayLeft = 0;
        this.target = null;
    }

    @Override
    public void onDeactivate() {
        this.renderBlockPos = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.level.dimension() == Level.NETHER) {
            this.error("You can't blow up respawn anchors in this dimension, disabling.", new Object[0]);
            this.toggle();
            return;
        }
        if (this.shouldPause()) {
            this.renderBlockPos = null;
            return;
        }
        if (TargetUtils.isBadTarget(this.target, this.targetRange.get())) {
            this.renderBlockPos = null;
            this.target = TargetUtils.getPlayerTarget(this.targetRange.get(), this.targetPriority.get());
            if (TargetUtils.isBadTarget(this.target, this.targetRange.get())) {
                return;
            }
        }
        this.doAnchorAura();
    }

    private void doAnchorAura() {
        this.bestPlaceDamage = 0.0;
        this.bestBreakDamage = 0.0;
        int iteratorRange = (int)Math.ceil(Math.max(this.placeRange.get(), this.breakRange.get()));
        BlockIterator.register(iteratorRange, iteratorRange, (blockPos, blockState) -> {
            double wallsRange;
            boolean isPlacing = blockState.getBlock() != Blocks.RESPAWN_ANCHOR;
            double baseRange = isPlacing ? this.placeRange.get() : this.breakRange.get();
            if (this.isOutOfRange((BlockPos)blockPos, baseRange, wallsRange = (isPlacing ? this.placeWallsRange.get() : this.breakWallsRange.get()).doubleValue())) {
                return;
            }
            if (isPlacing) {
                if (!BlockUtils.canPlace(blockPos)) {
                    return;
                }
                if (!this.airPlace.get().booleanValue() && this.isAirPlace((BlockPos)blockPos)) {
                    return;
                }
            }
            float bestDamage = isPlacing ? (float)this.bestPlaceDamage : (float)this.bestBreakDamage;
            float selfDamage = DamageUtils.anchorDamage((LivingEntity)this.mc.player, blockPos.getCenter());
            float targetDamage = DamageUtils.anchorDamage((LivingEntity)this.target, blockPos.getCenter());
            if ((double)targetDamage >= this.minDamage.get() && targetDamage > bestDamage && (!this.antiSuicide.get().booleanValue() || (double)selfDamage <= this.maxSelfDamage.get()) && (!this.antiSuicide.get().booleanValue() || PlayerUtils.getTotalHealth() - selfDamage > 0.0f)) {
                if (isPlacing) {
                    this.bestPlaceDamage = targetDamage;
                    this.bestPlacePos.set((Vec3i)blockPos);
                } else {
                    this.bestBreakDamage = targetDamage;
                    this.bestBreakPos.set((Vec3i)blockPos);
                }
            }
        });
        BlockIterator.after(() -> {
            this.renderBlockPos = null;
            FindItemResult anchor = InvUtils.findInHotbar(Items.RESPAWN_ANCHOR);
            FindItemResult glowStone = InvUtils.findInHotbar(Items.GLOWSTONE);
            if (this.bestBreakDamage > 0.0) {
                this.doBreak(glowStone);
            } else if (this.bestPlaceDamage > 0.0 && this.place.get().booleanValue() && anchor.found() && glowStone.found()) {
                this.doPlace(anchor);
            }
        });
    }

    private void doPlace(FindItemResult anchor) {
        this.renderBlockPos = this.bestPlacePos;
        if (this.placeDelayLeft++ < this.placeDelay.get()) {
            return;
        }
        BlockUtils.place((BlockPos)this.bestPlacePos, anchor, this.rotate.get(), 50, this.swing.get(), false, this.swapBack.get());
        this.placeDelayLeft = 0;
    }

    private void doBreak(FindItemResult glowStone) {
        this.renderBlockPos = this.bestBreakPos;
        if (this.rotate.get().booleanValue()) {
            Rotations.rotate(Rotations.getYaw((BlockPos)this.bestBreakPos), Rotations.getPitch((BlockPos)this.bestBreakPos), 40, () -> this.doInteract(glowStone));
        } else {
            this.doInteract(glowStone);
        }
    }

    private void doInteract(FindItemResult glowStone) {
        BlockState blockState = this.mc.level.getBlockState((BlockPos)this.bestBreakPos);
        if (blockState.getBlock() != Blocks.RESPAWN_ANCHOR) {
            return;
        }
        Vec3 center = this.bestBreakPos.getCenter();
        int charges = (Integer)blockState.getValue((Property)BlockStateProperties.RESPAWN_ANCHOR_CHARGES);
        if (charges == 0 && this.chargeDelayLeft++ >= this.chargeDelay.get()) {
            if (!glowStone.found()) {
                return;
            }
            InvUtils.swap(glowStone.slot(), this.swapBack.get());
            BlockUtils.interact(new BlockHitResult(center, BlockUtils.getDirection((BlockPos)this.bestBreakPos), (BlockPos)this.bestBreakPos, true), InteractionHand.MAIN_HAND, this.swing.get());
            this.chargeDelayLeft = 0;
            ++charges;
        }
        if (charges > 0 && this.breakDelayLeft++ >= this.breakDelay.get()) {
            FindItemResult fir = InvUtils.findInHotbar(item -> !item.getItem().equals(Items.GLOWSTONE));
            if (!fir.found()) {
                return;
            }
            InvUtils.swap(fir.slot(), this.swapBack.get());
            BlockUtils.interact(new BlockHitResult(center, BlockUtils.getDirection((BlockPos)this.bestBreakPos), (BlockPos)this.bestBreakPos, true), InteractionHand.MAIN_HAND, this.swing.get());
            this.breakDelayLeft = 0;
            this.mc.level.setBlock((BlockPos)this.bestBreakPos, this.mc.level.getFluidState((BlockPos)this.bestBreakPos).createLegacyBlock(), 0);
        }
        if (this.swapBack.get().booleanValue()) {
            InvUtils.swapBack();
        }
    }

    private boolean isOutOfRange(BlockPos blockPos, double baseRange, double wallsRange) {
        Vec3 pos = blockPos.getCenter();
        if (!PlayerUtils.isWithin(pos, baseRange)) {
            return true;
        }
        ClipContext clipContext = new ClipContext(this.mc.player.getEyePosition(), pos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this.mc.player);
        BlockHitResult result = this.mc.level.clip(clipContext);
        if (result == null || !result.getBlockPos().equals((Object)blockPos)) {
            return !PlayerUtils.isWithin(pos, wallsRange);
        }
        return false;
    }

    private boolean isAirPlace(BlockPos blockPos) {
        for (Direction direction : Direction.values()) {
            if (this.mc.level.getBlockState(blockPos.relative(direction)).canBeReplaced()) continue;
            return false;
        }
        return true;
    }

    private boolean shouldPause() {
        if (this.pauseOnUse.get().booleanValue() && this.mc.player.isUsingItem()) {
            return true;
        }
        if (this.pauseOnMine.get().booleanValue() && this.mc.gameMode.isDestroying()) {
            return true;
        }
        CrystalAura CA = Modules.get().get(CrystalAura.class);
        return this.pauseOnCA.get() != false && CA.isActive() && CA.kaTimer > 0;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!this.render.get().booleanValue() || this.renderBlockPos == null) {
            return;
        }
        event.renderer.box(this.renderBlockPos, (Color)this.sideColor.get(), (Color)this.lineColor.get(), this.shapeMode.get(), 0);
    }

    @Override
    public String getInfoString() {
        return EntityUtils.getName((Entity)this.target);
    }
}
