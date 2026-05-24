package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.DirectionAccessor;
import meteordevelopment.meteorclient.mixin.LevelRendererAccessor;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ModuleListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
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
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class Surround
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgToggles;
    private final SettingGroup sgRender;
    private final Setting<List<Block>> blocks;
    private final Setting<Integer> delay;
    private final Setting<Integer> blocksPerTick;
    private final Setting<Center> center;
    private final Setting<Boolean> doubleHeight;
    private final Setting<Boolean> onlyOnGround;
    private final Setting<Boolean> airPlace;
    private final Setting<Boolean> toggleModules;
    private final Setting<Boolean> toggleBack;
    private final Setting<List<Module>> modules;
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> protect;
    private final Setting<Boolean> toggleOnYChange;
    private final Setting<Boolean> toggleOnComplete;
    private final Setting<Boolean> toggleOnDeath;
    private final Setting<Boolean> swing;
    private final Setting<Boolean> render;
    private final Setting<Boolean> renderBelow;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> safeSideColor;
    private final Setting<SettingColor> safeLineColor;
    private final Setting<SettingColor> normalSideColor;
    private final Setting<SettingColor> normalLineColor;
    private final Setting<SettingColor> unsafeSideColor;
    private final Setting<SettingColor> unsafeLineColor;
    public ArrayList<Module> toActivate;
    private int timer;

    public Surround() {
        super(Categories.Combat, "surround", "Surrounds you in blocks to prevent massive crystal damage.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgToggles = this.settings.createGroup("Toggles");
        this.sgRender = this.settings.createGroup("Render");
        this.blocks = this.sgGeneral.add(((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("blocks")).description("What blocks to use for surround.")).defaultValue(Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.NETHERITE_BLOCK).filter(this::blockFilter).build());
        this.delay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("delay")).description("Delay, in ticks, between block placements.")).min(0).defaultValue(0)).build());
        this.blocksPerTick = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("blocks-per-tick")).description("How many blocks to place in one tick.")).defaultValue(1)).min(1).build());
        this.center = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("center")).description("Teleports you to the center of the block.")).defaultValue(Center.Incomplete)).build());
        this.doubleHeight = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("double-height")).description("Places obsidian on top of the original surround blocks to prevent people from face-placing you.")).defaultValue(false)).build());
        this.onlyOnGround = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-on-ground")).description("Works only when you are standing on blocks.")).defaultValue(true)).build());
        this.airPlace = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("air-place")).description("Allows Surround to place blocks in the air.")).defaultValue(true)).build());
        this.toggleModules = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("toggle-modules")).description("Turn off other modules when surround is activated.")).defaultValue(false)).build());
        this.toggleBack = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("toggle-back-on")).description("Turn the other modules back on when surround is deactivated.")).defaultValue(false)).visible(this.toggleModules::get)).build());
        this.modules = this.sgGeneral.add(((ModuleListSetting.Builder)((ModuleListSetting.Builder)((ModuleListSetting.Builder)new ModuleListSetting.Builder().name("modules")).description("Which modules to disable on activation.")).visible(this.toggleModules::get)).build());
        this.rotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Automatically faces towards the obsidian being placed.")).defaultValue(true)).build());
        this.protect = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("protect")).description("Attempts to break crystals around surround positions to prevent surround break.")).defaultValue(true)).build());
        this.toggleOnYChange = this.sgToggles.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("toggle-on-y-change")).description("Automatically disables when your y level changes (step, jumping, etc).")).defaultValue(true)).build());
        this.toggleOnComplete = this.sgToggles.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("toggle-on-complete")).description("Toggles off when all blocks are placed.")).defaultValue(false)).build());
        this.toggleOnDeath = this.sgToggles.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("toggle-on-death")).description("Toggles off when you die.")).defaultValue(true)).build());
        this.swing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("swing")).description("Render your hand swinging when placing surround blocks.")).defaultValue(true)).build());
        this.render = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render")).description("Renders a block overlay where the obsidian will be placed.")).defaultValue(true)).build());
        this.renderBelow = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("below")).description("Renders the block below you.")).defaultValue(false)).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.safeSideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("safe-side-color")).description("The side color for safe blocks.")).defaultValue(new SettingColor(13, 255, 0, 0)).visible(() -> this.render.get() != false && this.shapeMode.get() != ShapeMode.Lines)).build());
        this.safeLineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("safe-line-color")).description("The line color for safe blocks.")).defaultValue(new SettingColor(13, 255, 0, 0)).visible(() -> this.render.get() != false && this.shapeMode.get() != ShapeMode.Sides)).build());
        this.normalSideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("normal-side-color")).description("The side color for normal blocks.")).defaultValue(new SettingColor(0, 255, 238, 12)).visible(() -> this.render.get() != false && this.shapeMode.get() != ShapeMode.Lines)).build());
        this.normalLineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("normal-line-color")).description("The line color for normal blocks.")).defaultValue(new SettingColor(0, 255, 238, 100)).visible(() -> this.render.get() != false && this.shapeMode.get() != ShapeMode.Sides)).build());
        this.unsafeSideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("unsafe-side-color")).description("The side color for unsafe blocks.")).defaultValue(new SettingColor(204, 0, 0, 12)).visible(() -> this.render.get() != false && this.shapeMode.get() != ShapeMode.Lines)).build());
        this.unsafeLineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("unsafe-line-color")).description("The line color for unsafe blocks.")).defaultValue(new SettingColor(204, 0, 0, 100)).visible(() -> this.render.get() != false && this.shapeMode.get() != ShapeMode.Sides)).build());
        this.toActivate = new ArrayList();
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!this.render.get().booleanValue()) {
            return;
        }
        BlockPos playerPos = this.mc.player.blockPosition();
        if (this.renderBelow.get().booleanValue()) {
            this.draw(playerPos.below(), event, 0);
        }
        for (Direction direction : DirectionAccessor.meteor$getHorizontal()) {
            BlockPos renderPos = playerPos.relative(direction);
            this.draw(renderPos, event, this.doubleHeight.get() != false ? 2 : 0);
            if (!this.doubleHeight.get().booleanValue()) continue;
            this.draw(renderPos.above(), event, 4);
        }
    }

    private void draw(BlockPos renderPos, Render3DEvent event, int exclude) {
        Color sideColor = this.getSideColor(renderPos);
        Color lineColor = this.getLineColor(renderPos);
        event.renderer.box(renderPos, sideColor, lineColor, this.shapeMode.get(), exclude);
    }

    @Override
    public void onActivate() {
        if (this.center.get() == Center.OnActivate) {
            PlayerUtils.centerPlayer();
        }
        this.timer = this.delay.get();
        if (this.toggleModules.get().booleanValue() && !this.modules.get().isEmpty() && this.mc.level != null && this.mc.player != null) {
            for (Module module : this.modules.get()) {
                if (!module.isActive()) continue;
                module.toggle();
                this.toActivate.add(module);
            }
        }
    }

    @Override
    public void onDeactivate() {
        if (this.toggleBack.get().booleanValue() && !this.toActivate.isEmpty() && this.mc.level != null && this.mc.player != null) {
            for (Module module : this.toActivate) {
                module.enable();
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        BlockPos placePos;
        if (this.timer++ < this.delay.get()) {
            return;
        }
        if (this.toggleOnYChange.get().booleanValue() && this.mc.player.yo != this.mc.player.getY()) {
            this.toggle();
            return;
        }
        if (this.onlyOnGround.get().booleanValue() && !this.mc.player.onGround()) {
            return;
        }
        FindItemResult block = InvUtils.findInHotbar(itemStack -> this.blocks.get().contains(Block.byItem((Item)itemStack.getItem())));
        if (!block.found()) {
            return;
        }
        if (this.center.get() == Center.Always) {
            PlayerUtils.centerPlayer();
        }
        int placedCount = 0;
        boolean complete = true;
        BlockPos playerPos = this.mc.player.blockPosition();
        for (Direction direction : DirectionAccessor.meteor$getHorizontal()) {
            placePos = playerPos.relative(direction);
            if (!this.airPlace.get().booleanValue() && this.isAirPlace(placePos) && this.mc.level.getBlockState(placePos).canBeReplaced()) {
                if (this.place(placePos.below(), block) && ++placedCount >= this.blocksPerTick.get()) break;
                if (this.mc.level.getBlockState(placePos.below()).canBeReplaced()) {
                    complete = false;
                }
            }
            if (this.place(placePos, block) && ++placedCount >= this.blocksPerTick.get()) break;
            if (!this.mc.level.getBlockState(placePos).canBeReplaced()) continue;
            complete = false;
        }
        if (this.doubleHeight.get().booleanValue() && complete) {
            Direction direction;
            Direction[] directionArray = DirectionAccessor.meteor$getHorizontal();
            int n = directionArray.length;
            for (int i = 0; !(i >= n || this.place(placePos = playerPos.relative(direction = directionArray[i]).above(), block) && ++placedCount >= this.blocksPerTick.get()); ++i) {
                if (!this.mc.level.getBlockState(placePos).canBeReplaced()) continue;
                complete = false;
            }
        }
        this.timer = 0;
        if (complete && this.toggleOnComplete.get().booleanValue()) {
            this.toggle();
            return;
        }
        if (!complete && this.center.get() == Center.Incomplete) {
            PlayerUtils.centerPlayer();
        }
    }

    private boolean place(BlockPos placePos, FindItemResult block) {
        boolean isThreat;
        boolean placed = BlockUtils.place(placePos, block, this.rotate.get(), 100, this.swing.get(), true);
        boolean beingMined = false;
        for (BlockDestructionProgress value : ((LevelRendererAccessor)this.mc.levelRenderer).meteor$getDestroyingBlocks().values()) {
            if (!value.getPos().equals((Object)placePos)) continue;
            beingMined = true;
            break;
        }
        boolean bl = isThreat = this.mc.level.getBlockState(placePos).canBeReplaced() || beingMined;
        if (this.protect.get().booleanValue() && !placed && isThreat) {
            AABB box = new AABB((double)(placePos.getX() - 1), (double)(placePos.getY() - 1), (double)(placePos.getZ() - 1), (double)(placePos.getX() + 1), (double)(placePos.getY() + 1), (double)(placePos.getZ() + 1));
            Predicate<Entity> entityPredicate = entity -> entity instanceof EndCrystal && DamageUtils.crystalDamage((LivingEntity)this.mc.player, entity.position()) < PlayerUtils.getTotalHealth();
            for (Entity crystal : this.mc.level.getEntities((Entity)null, box, entityPredicate)) {
                if (this.rotate.get().booleanValue()) {
                    Rotations.rotate(Rotations.getPitch(crystal), Rotations.getYaw(crystal), () -> this.mc.player.connection.send((Packet)new ServerboundAttackPacket(crystal.getId())));
                } else {
                    this.mc.player.connection.send((Packet)new ServerboundAttackPacket(crystal.getId()));
                }
                this.mc.getConnection().send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            }
        }
        return placed;
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        ClientboundPlayerCombatKillPacket packet;
        Entity entity;
        Packet<?> packet2 = event.packet;
        if (packet2 instanceof ClientboundPlayerCombatKillPacket && (entity = this.mc.level.getEntity((packet = (ClientboundPlayerCombatKillPacket)packet2).playerId())) == this.mc.player && this.toggleOnDeath.get().booleanValue()) {
            this.toggle();
            this.info("Toggled off because you died.", new Object[0]);
        }
    }

    private BlockType getBlockType(BlockPos pos) {
        BlockState blockState = this.mc.level.getBlockState(pos);
        if (blockState.getBlock().defaultDestroyTime() < 0.0f) {
            return BlockType.Safe;
        }
        if (blockState.getBlock().getExplosionResistance() >= 600.0f) {
            return BlockType.Normal;
        }
        return BlockType.Unsafe;
    }

    private Color getSideColor(BlockPos pos) {
        return switch (this.getBlockType(pos).ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> this.safeSideColor.get();
            case 1 -> this.normalSideColor.get();
            case 2 -> this.unsafeSideColor.get();
        };
    }

    private Color getLineColor(BlockPos pos) {
        return switch (this.getBlockType(pos).ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> this.safeLineColor.get();
            case 1 -> this.normalLineColor.get();
            case 2 -> this.unsafeLineColor.get();
        };
    }

    private boolean isAirPlace(BlockPos blockPos) {
        for (Direction direction : Direction.values()) {
            if (this.mc.level.getBlockState(blockPos.relative(direction)).canBeReplaced()) continue;
            return false;
        }
        return true;
    }

    private boolean blockFilter(Block block) {
        return block.getExplosionResistance() >= 600.0f && block.defaultDestroyTime() >= 0.0f && block != Blocks.REINFORCED_DEEPSLATE;
    }

    public static enum Center {
        Never,
        OnActivate,
        Incomplete,
        Always;

    }

    public static enum BlockType {
        Safe,
        Normal,
        Unsafe;

    }
}
