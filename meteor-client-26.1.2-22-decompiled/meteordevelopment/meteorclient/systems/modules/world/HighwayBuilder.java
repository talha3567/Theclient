package meteordevelopment.meteorclient.systems.modules.world;

import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.ShulkerBoxMenuAccessor;
import meteordevelopment.meteorclient.mixininterface.IVec3;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.systems.modules.movement.Velocity;
import meteordevelopment.meteorclient.systems.modules.movement.speed.Speed;
import meteordevelopment.meteorclient.systems.modules.player.AutoEat;
import meteordevelopment.meteorclient.systems.modules.player.AutoGap;
import meteordevelopment.meteorclient.systems.modules.player.AutoTool;
import meteordevelopment.meteorclient.systems.modules.player.InstantRebreak;
import meteordevelopment.meteorclient.systems.modules.player.SpeedMine;
import meteordevelopment.meteorclient.systems.modules.world.NoGhostBlocks;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.misc.HorizontalDirection;
import meteordevelopment.meteorclient.utils.misc.MBlockPos;
import meteordevelopment.meteorclient.utils.player.CustomPlayerInput;
import meteordevelopment.meteorclient.utils.player.EChestMemory;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.meteorclient.utils.world.Dir;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.client.player.ClientInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.joml.Vector3d;

public class HighwayBuilder
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgDigging;
    private final SettingGroup sgPaving;
    private final SettingGroup sgInventory;
    private final SettingGroup sgRenderDigging;
    private final SettingGroup sgRenderPaving;
    private final Setting<Integer> width;
    private final Setting<Integer> height;
    private final Setting<Floor> floor;
    private final Setting<Boolean> railings;
    private final Setting<Boolean> cornerBlock;
    private final Setting<Boolean> mineAboveRailings;
    private final Setting<Rotation> rotation;
    private final Setting<Boolean> disconnectOnToggle;
    private final Setting<Boolean> pauseOnLag;
    private final Setting<Boolean> destroyCrystalTraps;
    private final Setting<Boolean> doubleMine;
    private final Setting<Boolean> fastBreak;
    private final Setting<Boolean> dontBreakTools;
    private final Setting<Integer> breakDurability;
    private final Setting<Integer> savePickaxes;
    private final Setting<Integer> breakDelay;
    private final Setting<Integer> blocksPerTick;
    public final Setting<List<Block>> blocksToPlace;
    private final Setting<Double> placeRange;
    private final Setting<Integer> placeDelay;
    private final Setting<Integer> placementsPerTick;
    private final Setting<List<Item>> trashItems;
    private final Setting<Integer> inventoryDelay;
    private final Setting<Boolean> ejectUselessShulkers;
    private final Setting<Boolean> searchEnderChest;
    private final Setting<Boolean> searchShulkers;
    private final Setting<Integer> minEmpty;
    private final Setting<Boolean> mineEnderChests;
    private final Setting<BlockadeType> blockadeType;
    public final Setting<Integer> saveEchests;
    private final Setting<Boolean> rebreakEchests;
    private final Setting<Integer> rebreakTimer;
    private final Setting<Boolean> renderMine;
    private final Setting<ShapeMode> renderMineShape;
    private final Setting<SettingColor> renderMineSideColor;
    private final Setting<SettingColor> renderMineLineColor;
    private final Setting<Boolean> renderPlace;
    private final Setting<ShapeMode> renderPlaceShape;
    private final Setting<SettingColor> renderPlaceSideColor;
    private final Setting<SettingColor> renderPlaceLineColor;
    private HorizontalDirection dir;
    private HorizontalDirection leftDir;
    private HorizontalDirection rightDir;
    private ClientInput prevInput;
    private CustomPlayerInput input;
    private State state;
    private State lastState;
    private IBlockPosProvider blockPosProvider;
    public Vec3 start;
    public int blocksBroken;
    public int blocksPlaced;
    private final MBlockPos lastBreakingPos;
    private boolean displayInfo;
    private boolean warned;
    private boolean suspended;
    private boolean inventory;
    private int placeTimer;
    private int breakTimer;
    private int count;
    private int containerId;
    private final RestockTask restockTask;
    private final ArrayList<EndCrystal> ignoreCrystals;
    public boolean drawingBow;
    public DoubleMineBlock normalMining;
    public DoubleMineBlock packetMining;
    private final MBlockPos posRender2;
    private final MBlockPos posRender3;

    public HighwayBuilder() {
        super(Categories.World, "highway-builder", "Automatically builds highways.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgDigging = this.settings.createGroup("Digging");
        this.sgPaving = this.settings.createGroup("Paving");
        this.sgInventory = this.settings.createGroup("Inventory");
        this.sgRenderDigging = this.settings.createGroup("Render Digging");
        this.sgRenderPaving = this.settings.createGroup("Render Paving");
        this.width = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("width")).description("Width of the highway.")).defaultValue(4)).range(1, 5).sliderRange(1, 5).build());
        this.height = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("height")).description("Height of the highway.")).defaultValue(3)).range(2, 5).sliderRange(2, 5).build());
        this.floor = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("floor")).description("What floor placement mode to use.")).defaultValue(Floor.Replace)).build());
        this.railings = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("railings")).description("Builds railings next to the highway.")).defaultValue(true)).build());
        this.cornerBlock = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("corner-support-block")).description("Places a support block underneath the railings, to prevent air placing.")).defaultValue(true)).visible(this.railings::get)).build());
        this.mineAboveRailings = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("mine-above-railings")).description("Mines blocks above railings.")).defaultValue(true)).build());
        this.rotation = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("rotation")).description("Mode of rotation.")).defaultValue(Rotation.Both)).build());
        this.disconnectOnToggle = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("disconnect-on-toggle")).description("Automatically disconnects when the module is turned off, for example for not having enough blocks.")).defaultValue(false)).build());
        this.pauseOnLag = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-on-lag")).description("Pauses the current process while the server stops responding.")).defaultValue(true)).build());
        this.destroyCrystalTraps = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("destroy-crystal-traps")).description("Use a bow to defuse crystal traps safely from a distance. An infinity bow is recommended.")).defaultValue(true)).build());
        this.doubleMine = this.sgDigging.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("double-mine")).description("Whether to double mine blocks when applicable (normal mine and packet mine simultaneously).")).defaultValue(true)).build());
        this.fastBreak = this.sgDigging.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("fast-break")).description("Whether to finish breaking blocks faster than normal while double mining.")).defaultValue(true)).visible(this.doubleMine::get)).build());
        this.dontBreakTools = this.sgDigging.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("dont-break-tools")).description("Don't break tools.")).defaultValue(false)).build());
        this.breakDurability = this.sgDigging.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("durability-percentage")).description("The durability percentage at which to stop using a tool.")).defaultValue(2)).range(1, 100).sliderRange(1, 100).visible(this.dontBreakTools::get)).build());
        this.savePickaxes = this.sgDigging.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("save-pickaxes")).description("How many pickaxes to ensure are saved. Hitting this number in your inventory will trigger a restock or the module toggling off.")).defaultValue(1)).range(0, 36).sliderRange(0, 36).visible(() -> this.dontBreakTools.get() == false)).build());
        this.breakDelay = this.sgDigging.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("break-delay")).description("The delay between breaking blocks.")).defaultValue(0)).min(0).build());
        this.blocksPerTick = this.sgDigging.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("blocks-per-tick")).description("The maximum amount of blocks that can be mined in a tick. Only applies to blocks instantly breakable.")).defaultValue(1)).range(1, 100).sliderRange(1, 25).build());
        this.blocksToPlace = this.sgPaving.add(((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("blocks-to-place")).description("Blocks it is allowed to place.")).defaultValue(Blocks.OBSIDIAN).filter(block -> Block.isShapeFullBlock((VoxelShape)block.defaultBlockState().getCollisionShape((BlockGetter)EmptyBlockGetter.INSTANCE, BlockPos.ZERO))).build());
        this.placeRange = this.sgPaving.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("place-range")).description("The maximum distance at which you can place blocks.")).defaultValue(4.5).sliderMax(5.5).build());
        this.placeDelay = this.sgPaving.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("place-delay")).description("The delay between placing blocks.")).defaultValue(0)).min(0).build());
        this.placementsPerTick = this.sgPaving.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("placements-per-tick")).description("The maximum amount of blocks that can be placed in a tick.")).defaultValue(1)).min(1).build());
        this.trashItems = this.sgInventory.add(((ItemListSetting.Builder)((ItemListSetting.Builder)new ItemListSetting.Builder().name("trash-items")).description("Items that are considered trash and can be thrown out.")).defaultValue(Items.NETHERRACK, Items.QUARTZ, Items.GOLD_NUGGET, Items.GOLDEN_SWORD, Items.GLOWSTONE_DUST, Items.GLOWSTONE, Items.BLACKSTONE, Items.BASALT, Items.GHAST_TEAR, Items.SOUL_SAND, Items.SOUL_SOIL, Items.ROTTEN_FLESH, Items.MAGMA_BLOCK).build());
        this.inventoryDelay = this.sgInventory.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("inventory-delay")).description("Delay in ticks on inventory interactions.")).defaultValue(3)).min(0).build());
        this.ejectUselessShulkers = this.sgInventory.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("eject-useless-shulkers")).description("Whether you should eject useless shulkers. Warning - will throw out any shulkers that don't contain blocks to place, pickaxes, or food. Be careful with your kits.")).defaultValue(true)).build());
        this.searchEnderChest = this.sgInventory.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("search-ender-chest")).description("Searches your ender chest to find items to use. Be careful with this one, especially if you let it search through shulkers.")).defaultValue(false)).build());
        this.searchShulkers = this.sgInventory.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("search-shulkers")).description("Searches through shulkers to find items to use.")).defaultValue(true)).build());
        this.minEmpty = this.sgInventory.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("minimum-empty-slots")).description("The minimum amount of empty slots you want left after mining obsidian.")).defaultValue(3)).sliderRange(0, 9).min(0).build());
        this.mineEnderChests = this.sgInventory.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("mine-ender-chests")).description("Mines ender chests for obsidian.")).defaultValue(true)).build());
        this.blockadeType = this.sgInventory.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("echest-blockade-type")).description("What blockade type to use (the structure placed when mining echests).")).defaultValue(BlockadeType.Full)).visible(this.mineEnderChests::get)).build());
        this.saveEchests = this.sgInventory.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("save-ender-chests")).description("How many ender chests to ensure are saved. Hitting this number in your inventory will trigger a restock or the module toggling off.")).defaultValue(2)).range(0, 64).sliderRange(0, 64).visible(this.mineEnderChests::get)).build());
        this.rebreakEchests = this.sgInventory.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("instantly-rebreak-echests")).description("Whether or not to use the instant rebreak exploit to break echests.")).defaultValue(false)).visible(this.mineEnderChests::get)).build());
        this.rebreakTimer = this.sgInventory.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("rebreak-delay")).description("Delay between rebreak attempts.")).defaultValue(0)).sliderMax(20).visible(() -> this.mineEnderChests.get() != false && this.rebreakEchests.get() != false)).build());
        this.renderMine = this.sgRenderDigging.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-blocks-to-mine")).description("Render blocks to be mined.")).defaultValue(true)).build());
        this.renderMineShape = this.sgRenderDigging.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("blocks-to-mine-shape-mode")).description("How the blocks to be mined are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.renderMineSideColor = this.sgRenderDigging.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("blocks-to-mine-side-color")).description("Color of blocks to be mined.")).defaultValue(new SettingColor(225, 25, 25, 25)).build());
        this.renderMineLineColor = this.sgRenderDigging.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("blocks-to-mine-line-color")).description("Color of blocks to be mined.")).defaultValue(new SettingColor(225, 25, 25)).build());
        this.renderPlace = this.sgRenderPaving.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-blocks-to-place")).description("Render blocks to be placed.")).defaultValue(true)).build());
        this.renderPlaceShape = this.sgRenderPaving.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("blocks-to-place-shape-mode")).description("How the blocks to be placed are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.renderPlaceSideColor = this.sgRenderPaving.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("blocks-to-place-side-color")).description("Color of blocks to be placed.")).defaultValue(new SettingColor(25, 25, 225, 25)).build());
        this.renderPlaceLineColor = this.sgRenderPaving.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("blocks-to-place-line-color")).description("Color of blocks to be placed.")).defaultValue(new SettingColor(25, 25, 225)).build());
        this.lastBreakingPos = new MBlockPos();
        this.suspended = true;
        this.inventory = true;
        this.restockTask = new RestockTask(this, this);
        this.ignoreCrystals = new ArrayList();
        this.posRender2 = new MBlockPos();
        this.posRender3 = new MBlockPos();
        this.runInMainMenu = true;
    }

    @Override
    public void onActivate() {
        if (!Utils.canUpdate()) {
            return;
        }
        this.updateVariables();
        this.dir = HorizontalDirection.get(this.mc.player.getYRot());
        this.leftDir = this.dir.rotateLeftSkipOne();
        this.rightDir = this.leftDir.opposite();
        this.blockPosProvider = this.dir.diagonal ? new DiagonalBlockPosProvider(this) : new StraightBlockPosProvider(this);
        this.state = State.Forward;
        this.setState(State.Center);
        this.lastBreakingPos.set(0, 0, 0);
        this.start = this.mc.player.position();
        this.blocksPlaced = 0;
        this.blocksBroken = 0;
        this.displayInfo = true;
        this.suspended = false;
        this.restockTask.complete();
        if (this.blocksPerTick.get() > 1 && this.rotation.get().mine) {
            this.warning("With rotations enabled, you can break at most 1 block per tick.", new Object[0]);
        }
        if (this.placementsPerTick.get() > 1 && this.rotation.get().place) {
            this.warning("With rotations enabled, you can place at most 1 block per tick.", new Object[0]);
        }
        if (Modules.get().get(InstantRebreak.class).isActive()) {
            this.warning("It's recommended to disable the Instant Rebreak module and instead use the 'instantly-rebreak-echests' setting to avoid errors.", new Object[0]);
        }
        if (Modules.get().get(Speed.class).isActive() && this.dir.diagonal) {
            this.warning("It's recommended to disable the Speed module to avoid misalignment on diagonals.", new Object[0]);
        }
        if (!Modules.get().get(Velocity.class).isActive()) {
            this.warning("It's recommended to enable the Velocity module to avoid misalignment (entity pushing, liquid movement).", new Object[0]);
        }
        if (!this.warned && Modules.get().get(NoGhostBlocks.class).isActive()) {
            this.info("The No Ghost Blocks module is useful to prevent desyncs on laggy servers. However, it will also slow Highway Builder down, and comes with the risks of incorrect statistics and packet kicks.", new Object[0]);
            this.warned = true;
        }
    }

    @Override
    public void onDeactivate() {
        if (!Utils.canUpdate()) {
            return;
        }
        this.mc.player.input = this.prevInput;
        this.mc.player.setYRot(this.dir.yaw);
        this.mc.options.keyUse.setDown(false);
        if (this.displayInfo) {
            this.info("Distance: (highlight)%.0f", PlayerUtils.distanceTo(this.start));
            this.info("Blocks broken: (highlight)%d", this.blocksBroken);
            this.info("Blocks placed: (highlight)%d", this.blocksPlaced);
        }
    }

    @Override
    public void error(String message, Object ... args) {
        super.error(message, args);
        this.toggle();
        if (this.disconnectOnToggle.get().booleanValue()) {
            this.disconnect(message, args);
        }
    }

    private void errorEarly(String message, Object ... args) {
        super.error(message, args);
        this.displayInfo = false;
        this.toggle();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.dir == null) {
            this.onActivate();
            return;
        }
        if (this.suspended) {
            if (this.inventory && Utils.canUpdate()) {
                this.updateVariables();
                this.suspended = false;
            } else {
                return;
            }
        }
        if (this.width.get() < 3 && this.dir.diagonal) {
            this.errorEarly("Diagonal highways with width less than 3 are not supported.", new Object[0]);
            return;
        }
        if (Modules.get().get(AutoEat.class).eating || Modules.get().get(AutoGap.class).isEating() || Modules.get().get(KillAura.class).attacking) {
            this.input.stop();
            return;
        }
        if (this.pauseOnLag.get().booleanValue() && TickRate.INSTANCE.getTimeSinceLastTick() >= 1.5f) {
            this.input.stop();
            return;
        }
        this.count = 0;
        if (this.mc.player.getY() < this.start.y - 0.5) {
            this.setState(State.ReLevel);
        }
        this.tickDoubleMine();
        this.state.tick(this);
        if (this.breakTimer > 0) {
            --this.breakTimer;
        }
        if (this.placeTimer > 0) {
            --this.placeTimer;
        }
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        Packet<?> packet = event.packet;
        if (packet instanceof ClientboundContainerSetContentPacket) {
            ClientboundContainerSetContentPacket p = (ClientboundContainerSetContentPacket)packet;
            if (p.containerId() == 0 && this.suspended) {
                this.inventory = true;
            } else {
                this.containerId = p.containerId();
            }
        }
    }

    @EventHandler
    private void onGameLeave(GameLeftEvent event) {
        this.suspended = true;
        this.inventory = false;
    }

    @EventHandler
    private void onRender2d(Render2DEvent event) {
        if (this.suspended || !this.renderMine.get().booleanValue()) {
            return;
        }
        if (this.normalMining != null) {
            this.normalMining.renderLetter();
        }
        if (this.packetMining != null) {
            this.packetMining.renderLetter();
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (this.suspended || this.blockPosProvider == null) {
            return;
        }
        if (this.renderMine.get().booleanValue()) {
            this.render(event, this.blockPosProvider.getFront(), mBlockPos -> this.canMine((MBlockPos)mBlockPos, true), true);
            if (this.floor.get() == Floor.Replace) {
                this.render(event, this.blockPosProvider.getFloor(), mBlockPos -> this.canMine((MBlockPos)mBlockPos, false), true);
            }
            if (this.railings.get().booleanValue()) {
                this.render(event, this.blockPosProvider.getRailings(0), mBlockPos -> this.canMine((MBlockPos)mBlockPos, false), true);
            }
            if (this.mineAboveRailings.get().booleanValue()) {
                this.render(event, this.blockPosProvider.getRailings(1), mBlockPos -> this.canMine((MBlockPos)mBlockPos, true), true);
            }
            if (this.state == State.MineEChestBlockade) {
                this.render(event, this.blockPosProvider.getBlockade(true, this.blockadeType.get()), mBlockPos -> this.canMine((MBlockPos)mBlockPos, true), true);
            }
        }
        if (this.renderPlace.get().booleanValue()) {
            this.render(event, this.blockPosProvider.getLiquids(), mBlockPos -> this.canPlace((MBlockPos)mBlockPos, true), false);
            if (this.railings.get().booleanValue()) {
                this.render(event, this.blockPosProvider.getRailings(0), mBlockPos -> this.canPlace((MBlockPos)mBlockPos, false), false);
                if (this.cornerBlock.get().booleanValue()) {
                    this.render(event, this.blockPosProvider.getRailings(-1), mBlockPos -> {
                        boolean valid = false;
                        for (MBlockPos pos : this.blockPosProvider.getRailings(0)) {
                            if (this.blocksToPlace.get().contains(pos.getState().getBlock()) || !pos.add(0, -1, 0).equals(mBlockPos)) continue;
                            valid = true;
                            break;
                        }
                        return valid && this.canPlace((MBlockPos)mBlockPos, false);
                    }, false);
                }
            }
            this.render(event, this.blockPosProvider.getFloor(), mBlockPos -> this.canPlace((MBlockPos)mBlockPos, false), false);
            if (this.state == State.PlaceEChestBlockade) {
                this.render(event, this.blockPosProvider.getBlockade(false, this.blockadeType.get()), mBlockPos -> this.canPlace((MBlockPos)mBlockPos, false), false);
            }
        }
    }

    private void render(Render3DEvent event, MBPIterator it, Predicate<MBlockPos> predicate, boolean mine) {
        Color sideColor = mine ? (Color)this.renderMineSideColor.get() : (Color)this.renderPlaceSideColor.get();
        Color lineColor = mine ? (Color)this.renderMineLineColor.get() : (Color)this.renderPlaceLineColor.get();
        ShapeMode shapeMode = mine ? this.renderMineShape.get() : this.renderPlaceShape.get();
        for (MBlockPos pos : it) {
            this.posRender2.set(pos);
            if (!predicate.test(this.posRender2)) continue;
            int excludeDir = 0;
            for (Direction side : Direction.values()) {
                this.posRender3.set(this.posRender2).add(side.getStepX(), side.getStepY(), side.getStepZ());
                it.save();
                for (MBlockPos p : it) {
                    if (!p.equals(this.posRender3) || !predicate.test(p)) continue;
                    excludeDir |= Dir.get(side);
                }
                it.restore();
            }
            event.renderer.box(this.posRender2.getBlockPos(), sideColor, lineColor, shapeMode, excludeDir);
        }
    }

    private void updateVariables() {
        this.prevInput = this.mc.player.input;
        this.input = new CustomPlayerInput();
        this.mc.player.input = this.input;
        this.containerId = 0;
        this.count = 0;
        this.breakTimer = 0;
        this.placeTimer = 0;
        this.ignoreCrystals.clear();
        this.normalMining = null;
        this.packetMining = null;
    }

    private void setState(State state) {
        this.setState(state, this.state);
    }

    private void setState(State state, State lastState) {
        this.lastState = lastState;
        this.state = state;
        this.input.stop();
        state.start(this);
    }

    private int getWidthLeft() {
        return switch (this.width.get()) {
            case 4, 5 -> 2;
            case 2, 3 -> 1;
            default -> 0;
        };
    }

    private int getWidthRight() {
        return switch (this.width.get()) {
            case 5 -> 2;
            case 3, 4 -> 1;
            default -> 0;
        };
    }

    private boolean canMine(MBlockPos pos, boolean mineBlocksToPlace) {
        BlockState state = pos.getState();
        return BlockUtils.canBreak(pos.getBlockPos(), state) && (mineBlocksToPlace || !this.blocksToPlace.get().contains(state.getBlock()));
    }

    private boolean canPlace(MBlockPos pos, boolean liquids) {
        if (pos.getBlockPos().distToCenterSqr((Position)this.mc.player.getEyePosition()) > this.placeRange.get() * this.placeRange.get()) {
            return false;
        }
        return liquids ? !pos.getState().getFluidState().isEmpty() : BlockUtils.canPlace(pos.getBlockPos());
    }

    private void disconnect(String message, Object ... args) {
        MutableComponent text = Component.literal((String)(String.format("%s[%s%s%s] %s", ChatFormatting.GRAY, ChatFormatting.BLUE, this.title, ChatFormatting.GRAY, ChatFormatting.RED) + String.format(message, args))).append("\n");
        text.append((Component)this.getStatsText());
        this.mc.getConnection().getConnection().disconnect((Component)text);
    }

    public MutableComponent getStatsText() {
        MutableComponent text = Component.literal((String)String.format("%sDistance: %s%.0f\n", ChatFormatting.GRAY, ChatFormatting.WHITE, this.mc.player == null ? 0.0 : PlayerUtils.distanceTo(this.start)));
        text.append(String.format("%sBlocks broken: %s%d\n", ChatFormatting.GRAY, ChatFormatting.WHITE, this.blocksBroken));
        text.append(String.format("%sBlocks placed: %s%d", ChatFormatting.GRAY, ChatFormatting.WHITE, this.blocksPlaced));
        return text;
    }

    private void tickDoubleMine() {
        if (this.normalMining != null) {
            if (this.normalMining.shouldRemove()) {
                this.mc.getConnection().send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.normalMining.blockPos, this.normalMining.direction));
                this.normalMining = null;
                DoubleMineBlock.rateLimited = true;
            } else if (this.mc.level.getBlockState(this.normalMining.blockPos).getBlock() != this.normalMining.block) {
                this.normalMining = null;
                ++this.blocksBroken;
                ++this.count;
                DoubleMineBlock.rateLimited = false;
            } else if (this.normalMining.isReady()) {
                this.normalMining.stopDestroying();
            }
            this.mc.player.swing(InteractionHand.MAIN_HAND);
        }
        if (this.packetMining != null) {
            if (this.packetMining.shouldRemove()) {
                this.packetMining = null;
            } else if (this.mc.level.getBlockState(this.packetMining.blockPos).getBlock() != this.packetMining.block) {
                this.packetMining = null;
                ++this.blocksBroken;
                ++this.count;
            }
        }
    }

    public static enum Floor {
        Replace,
        PlaceMissing;

    }

    public static enum Rotation {
        None(false, false),
        Mine(true, false),
        Place(false, true),
        Both(true, true);

        public final boolean mine;
        public final boolean place;

        private Rotation(boolean mine, boolean place) {
            this.mine = mine;
            this.place = place;
        }
    }

    public static enum BlockadeType {
        Full(6),
        Partial(4),
        Shulker(3);

        public final int columns;

        private BlockadeType(int columns) {
            this.columns = columns;
        }
    }

    private class RestockTask {
        public boolean materials;
        public boolean pickaxes;
        public boolean food;
        private final HighwayBuilder b;
        final /* synthetic */ HighwayBuilder this$0;

        public RestockTask(HighwayBuilder highwayBuilder, HighwayBuilder b) {
            HighwayBuilder highwayBuilder2 = highwayBuilder;
            Objects.requireNonNull(highwayBuilder2);
            this.this$0 = highwayBuilder2;
            this.b = b;
        }

        public void setMaterials() {
            this.setTask(0);
        }

        public void setPickaxes() {
            this.setTask(1);
        }

        public void setFood() {
            this.setTask(2);
        }

        private void setTask(@Range(from=0L, to=2L) int value) {
            this.complete();
            switch (value) {
                case 0: {
                    this.materials = true;
                    break;
                }
                case 1: {
                    this.pickaxes = true;
                    break;
                }
                case 2: {
                    this.food = true;
                }
            }
            this.this$0.setState(State.Restock);
            this.b.info("Starting new restock task for " + this.item(), new Object[0]);
        }

        public void complete() {
            this.materials = false;
            this.pickaxes = false;
            this.food = false;
        }

        public boolean tasksInactive() {
            return !this.materials && !this.pickaxes && !this.food;
        }

        public String item() {
            if (this.materials) {
                return "building materials";
            }
            if (this.pickaxes) {
                return "pickaxes";
            }
            if (this.food) {
                return "food";
            }
            return "unknown";
        }
    }

    private class DiagonalBlockPosProvider
    implements IBlockPosProvider {
        private final MBlockPos pos;
        private final MBlockPos pos2;
        final /* synthetic */ HighwayBuilder this$0;

        private DiagonalBlockPosProvider(HighwayBuilder highwayBuilder) {
            HighwayBuilder highwayBuilder2 = highwayBuilder;
            Objects.requireNonNull(highwayBuilder2);
            this.this$0 = highwayBuilder2;
            this.pos = new MBlockPos();
            this.pos2 = new MBlockPos();
        }

        @Override
        public MBPIterator getFront() {
            this.pos.coerceBlockLevel((Entity)((HighwayBuilder)this.this$0).mc.player).offset(this.this$0.dir.rotateLeft()).offset(this.this$0.leftDir, this.this$0.getWidthLeft() - 1);
            return new MBPIterator(this){
                private int i;
                private int w;
                private int y;
                private int pi;
                private int pw;
                private int py;
                final /* synthetic */ DiagonalBlockPosProvider this$1;
                {
                    DiagonalBlockPosProvider diagonalBlockPosProvider = this$1;
                    Objects.requireNonNull(diagonalBlockPosProvider);
                    this.this$1 = diagonalBlockPosProvider;
                }

                @Override
                public boolean hasNext() {
                    return this.i < 2 && this.w < this.this$1.this$0.width.get() && this.y < this.this$1.this$0.height.get();
                }

                @Override
                public MBlockPos next() {
                    this.this$1.pos2.set(this.this$1.pos).offset(this.this$1.this$0.rightDir, this.w).add(0, this.y++, 0);
                    if (this.y >= this.this$1.this$0.height.get()) {
                        this.y = 0;
                        ++this.w;
                        if (this.w >= (this.i == 0 ? this.this$1.this$0.width.get() - 1 : this.this$1.this$0.width.get())) {
                            this.w = 0;
                            ++this.i;
                            this.this$1.pos.coerceBlockLevel((Entity)((HighwayBuilder)this.this$1.this$0).mc.player).offset(this.this$1.this$0.dir).offset(this.this$1.this$0.leftDir, this.this$1.this$0.getWidthLeft());
                        }
                    }
                    return this.this$1.pos2;
                }

                private void initPos() {
                    if (this.i == 0) {
                        this.this$1.pos.coerceBlockLevel((Entity)((HighwayBuilder)this.this$1.this$0).mc.player).offset(this.this$1.this$0.dir.rotateLeft()).offset(this.this$1.this$0.leftDir, this.this$1.this$0.getWidthLeft() - 1);
                    } else {
                        this.this$1.pos.coerceBlockLevel((Entity)((HighwayBuilder)this.this$1.this$0).mc.player).offset(this.this$1.this$0.dir).offset(this.this$1.this$0.leftDir, this.this$1.this$0.getWidthLeft());
                    }
                }

                @Override
                public void save() {
                    this.pi = this.i;
                    this.pw = this.w;
                    this.py = this.y;
                    this.y = 0;
                    this.w = 0;
                    this.i = 0;
                    this.initPos();
                }

                @Override
                public void restore() {
                    this.i = this.pi;
                    this.w = this.pw;
                    this.y = this.py;
                    this.initPos();
                }
            };
        }

        @Override
        public MBPIterator getFloor() {
            this.pos.coerceBlockLevel((Entity)((HighwayBuilder)this.this$0).mc.player).add(0, -1, 0).offset(this.this$0.dir.rotateLeft()).offset(this.this$0.leftDir, this.this$0.getWidthLeft() - 1);
            return new MBPIterator(this){
                private int i;
                private int w;
                private int pi;
                private int pw;
                final /* synthetic */ DiagonalBlockPosProvider this$1;
                {
                    DiagonalBlockPosProvider diagonalBlockPosProvider = this$1;
                    Objects.requireNonNull(diagonalBlockPosProvider);
                    this.this$1 = diagonalBlockPosProvider;
                }

                @Override
                public boolean hasNext() {
                    return this.i < 2 && this.w < this.this$1.this$0.width.get();
                }

                @Override
                public MBlockPos next() {
                    this.this$1.pos2.set(this.this$1.pos).offset(this.this$1.this$0.rightDir, this.w++);
                    if (this.w >= (this.i == 0 ? this.this$1.this$0.width.get() - 1 : this.this$1.this$0.width.get())) {
                        this.w = 0;
                        ++this.i;
                        this.this$1.pos.coerceBlockLevel((Entity)((HighwayBuilder)this.this$1.this$0).mc.player).add(0, -1, 0).offset(this.this$1.this$0.dir).offset(this.this$1.this$0.leftDir, this.this$1.this$0.getWidthLeft());
                    }
                    return this.this$1.pos2;
                }

                private void initPos() {
                    if (this.i == 0) {
                        this.this$1.pos.coerceBlockLevel((Entity)((HighwayBuilder)this.this$1.this$0).mc.player).add(0, -1, 0).offset(this.this$1.this$0.dir.rotateLeft()).offset(this.this$1.this$0.leftDir, this.this$1.this$0.getWidthLeft() - 1);
                    } else {
                        this.this$1.pos.coerceBlockLevel((Entity)((HighwayBuilder)this.this$1.this$0).mc.player).add(0, -1, 0).offset(this.this$1.this$0.dir).offset(this.this$1.this$0.leftDir, this.this$1.this$0.getWidthLeft());
                    }
                }

                @Override
                public void save() {
                    this.pi = this.i;
                    this.pw = this.w;
                    this.w = 0;
                    this.i = 0;
                    this.initPos();
                }

                @Override
                public void restore() {
                    this.i = this.pi;
                    this.w = this.pw;
                    this.initPos();
                }
            };
        }

        @Override
        public MBPIterator getRailings(final int state) {
            this.pos.coerceBlockLevel((Entity)((HighwayBuilder)this.this$0).mc.player).offset(this.this$0.dir.rotateLeft()).offset(this.this$0.leftDir, this.this$0.getWidthLeft());
            return new MBPIterator(){
                private int i;
                private int y;
                private int pi;
                private int py;
                final /* synthetic */ DiagonalBlockPosProvider this$1;
                {
                    DiagonalBlockPosProvider diagonalBlockPosProvider = this$1;
                    Objects.requireNonNull(diagonalBlockPosProvider);
                    this.this$1 = diagonalBlockPosProvider;
                    this.y = state;
                }

                @Override
                public boolean hasNext() {
                    return this.i < 2 && this.y < (state == 1 ? this.this$1.this$0.height.get() : state + 1);
                }

                @Override
                public MBlockPos next() {
                    this.this$1.pos2.set(this.this$1.pos).add(0, this.y++, 0);
                    if (this.y >= (state == 1 ? this.this$1.this$0.height.get() : state + 1)) {
                        this.y = state;
                        ++this.i;
                        this.this$1.pos.coerceBlockLevel((Entity)((HighwayBuilder)this.this$1.this$0).mc.player).offset(this.this$1.this$0.dir.rotateRight()).offset(this.this$1.this$0.rightDir, this.this$1.this$0.getWidthRight());
                    }
                    return this.this$1.pos2;
                }

                private void initPos() {
                    if (this.i == 0) {
                        this.this$1.pos.coerceBlockLevel((Entity)((HighwayBuilder)this.this$1.this$0).mc.player).offset(this.this$1.this$0.dir.rotateLeft()).offset(this.this$1.this$0.leftDir, this.this$1.this$0.getWidthLeft());
                    } else {
                        this.this$1.pos.coerceBlockLevel((Entity)((HighwayBuilder)this.this$1.this$0).mc.player).offset(this.this$1.this$0.dir.rotateRight()).offset(this.this$1.this$0.rightDir, this.this$1.this$0.getWidthRight());
                    }
                }

                @Override
                public void save() {
                    this.pi = this.i;
                    this.py = this.y;
                    this.i = 0;
                    this.y = state;
                    this.initPos();
                }

                @Override
                public void restore() {
                    this.i = this.pi;
                    this.y = this.py;
                    this.initPos();
                }
            };
        }

        @Override
        public MBPIterator getLiquids() {
            final boolean m = this.this$0.mineAboveRailings.get();
            this.pos.coerceBlockLevel((Entity)((HighwayBuilder)this.this$0).mc.player).offset(this.this$0.dir).offset(this.this$0.dir.rotateLeft()).offset(this.this$0.leftDir, this.this$0.getWidthLeft());
            return new MBPIterator(){
                private int i;
                private int w;
                private int y;
                private int pi;
                private int pw;
                private int py;
                final /* synthetic */ DiagonalBlockPosProvider this$1;
                {
                    DiagonalBlockPosProvider diagonalBlockPosProvider = this$1;
                    Objects.requireNonNull(diagonalBlockPosProvider);
                    this.this$1 = diagonalBlockPosProvider;
                }

                private int getWidth() {
                    return this.this$1.this$0.width.get() + (this.i == 0 ? 1 : 0) + (m && this.i == 1 ? 2 : 0);
                }

                @Override
                public boolean hasNext() {
                    if (m && this.i == 1 && this.y == this.this$1.this$0.height.get() && this.w == this.getWidth() - 1) {
                        return false;
                    }
                    return this.i < 2 && this.w < this.getWidth() && this.y < this.this$1.this$0.height.get() + 1;
                }

                private void updateW() {
                    ++this.w;
                    if (this.w >= this.getWidth()) {
                        this.w = 0;
                        ++this.i;
                        this.this$1.pos.coerceBlockLevel((Entity)((HighwayBuilder)this.this$1.this$0).mc.player).offset(this.this$1.this$0.dir, 2).offset(this.this$1.this$0.leftDir, this.this$1.this$0.getWidthLeft() + (m ? 1 : 0));
                    }
                }

                @Override
                public MBlockPos next() {
                    if (this.i == (m ? 1 : 0) && this.y == this.this$1.this$0.height.get() && (this.w == 0 || this.w == this.getWidth() - 1)) {
                        this.y = 0;
                        this.updateW();
                    }
                    this.this$1.pos2.set(this.this$1.pos).offset(this.this$1.this$0.rightDir, this.w).add(0, this.y++, 0);
                    if (this.y >= this.this$1.this$0.height.get() + 1) {
                        this.y = 0;
                        this.updateW();
                    }
                    return this.this$1.pos2;
                }

                private void initPos() {
                    if (this.i == 0) {
                        this.this$1.pos.coerceBlockLevel((Entity)((HighwayBuilder)this.this$1.this$0).mc.player).offset(this.this$1.this$0.dir).offset(this.this$1.this$0.dir.rotateLeft()).offset(this.this$1.this$0.leftDir, this.this$1.this$0.getWidthLeft());
                    } else {
                        this.this$1.pos.coerceBlockLevel((Entity)((HighwayBuilder)this.this$1.this$0).mc.player).offset(this.this$1.this$0.dir, 2).offset(this.this$1.this$0.leftDir, this.this$1.this$0.getWidthLeft() + (m ? 1 : 0));
                    }
                }

                @Override
                public void save() {
                    this.pi = this.i;
                    this.pw = this.w;
                    this.py = this.y;
                    this.y = 0;
                    this.w = 0;
                    this.i = 0;
                    this.initPos();
                }

                @Override
                public void restore() {
                    this.i = this.pi;
                    this.w = this.pw;
                    this.y = this.py;
                    this.initPos();
                }
            };
        }

        @Override
        public MBPIterator getBlockade(final boolean mine, final BlockadeType blockadeType) {
            return new MBPIterator(){
                private int i;
                private int y;
                private int pi;
                private int py;
                final /* synthetic */ DiagonalBlockPosProvider this$1;
                {
                    DiagonalBlockPosProvider diagonalBlockPosProvider = this$1;
                    Objects.requireNonNull(diagonalBlockPosProvider);
                    this.this$1 = diagonalBlockPosProvider;
                    this.i = mine ? -1 : 0;
                }

                private MBlockPos get(int i) {
                    HorizontalDirection dir2 = this.this$1.this$0.dir.rotateLeft().rotateLeftSkipOne();
                    this.this$1.pos.coerceBlockLevel((Entity)((HighwayBuilder)this.this$1.this$0).mc.player).offset(dir2);
                    return switch (i) {
                        case -1 -> this.this$1.pos;
                        case 0 -> this.this$1.pos.offset(dir2);
                        case 1 -> this.this$1.pos.offset(dir2.rotateLeftSkipOne());
                        case 2 -> this.this$1.pos.offset(dir2.rotateLeftSkipOne().opposite());
                        case 3 -> this.this$1.pos.offset(dir2.opposite(), 2);
                        case 4 -> this.this$1.pos.offset(dir2.opposite()).offset(dir2.rotateLeftSkipOne());
                        case 5 -> this.this$1.pos.offset(dir2.opposite()).offset(dir2.rotateLeftSkipOne().opposite());
                        default -> throw new IllegalStateException("Unexpected value: " + i);
                    };
                }

                @Override
                public boolean hasNext() {
                    return this.i < blockadeType.columns && this.y < 2;
                }

                @Override
                public MBlockPos next() {
                    MBlockPos pos = this.get(this.i).add(0, this.y, 0);
                    ++this.y;
                    if (this.y > 1) {
                        this.y = 0;
                        ++this.i;
                    }
                    return pos;
                }

                @Override
                public void save() {
                    this.pi = this.i;
                    this.py = this.y;
                    this.y = 0;
                    this.i = 0;
                }

                @Override
                public void restore() {
                    this.i = this.pi;
                    this.y = this.py;
                }

                @Override
                public int placementsPerTick(HighwayBuilder b) {
                    return 1;
                }
            };
        }
    }

    private class StraightBlockPosProvider
    implements IBlockPosProvider {
        private final MBlockPos pos;
        private final MBlockPos pos2;
        final /* synthetic */ HighwayBuilder this$0;

        private StraightBlockPosProvider(HighwayBuilder highwayBuilder) {
            HighwayBuilder highwayBuilder2 = highwayBuilder;
            Objects.requireNonNull(highwayBuilder2);
            this.this$0 = highwayBuilder2;
            this.pos = new MBlockPos();
            this.pos2 = new MBlockPos();
        }

        @Override
        public MBPIterator getFront() {
            this.pos.coerceBlockLevel((Entity)((HighwayBuilder)this.this$0).mc.player).offset(this.this$0.dir).offset(this.this$0.leftDir, this.this$0.getWidthLeft());
            return new MBPIterator(this){
                private int w;
                private int y;
                private int pw;
                private int py;
                final /* synthetic */ StraightBlockPosProvider this$1;
                {
                    StraightBlockPosProvider straightBlockPosProvider = this$1;
                    Objects.requireNonNull(straightBlockPosProvider);
                    this.this$1 = straightBlockPosProvider;
                }

                @Override
                public boolean hasNext() {
                    return this.w < this.this$1.this$0.width.get() && this.y < this.this$1.this$0.height.get();
                }

                @Override
                public MBlockPos next() {
                    this.this$1.pos2.set(this.this$1.pos).offset(this.this$1.this$0.rightDir, this.w).add(0, this.y, 0);
                    ++this.w;
                    if (this.w >= this.this$1.this$0.width.get()) {
                        this.w = 0;
                        ++this.y;
                    }
                    return this.this$1.pos2;
                }

                @Override
                public void save() {
                    this.pw = this.w;
                    this.py = this.y;
                    this.y = 0;
                    this.w = 0;
                }

                @Override
                public void restore() {
                    this.w = this.pw;
                    this.y = this.py;
                }
            };
        }

        @Override
        public MBPIterator getFloor() {
            this.pos.coerceBlockLevel((Entity)((HighwayBuilder)this.this$0).mc.player).offset(this.this$0.dir).offset(this.this$0.leftDir, this.this$0.getWidthLeft()).add(0, -1, 0);
            return new MBPIterator(this){
                private int w;
                private int pw;
                final /* synthetic */ StraightBlockPosProvider this$1;
                {
                    StraightBlockPosProvider straightBlockPosProvider = this$1;
                    Objects.requireNonNull(straightBlockPosProvider);
                    this.this$1 = straightBlockPosProvider;
                }

                @Override
                public boolean hasNext() {
                    return this.w < this.this$1.this$0.width.get();
                }

                @Override
                public MBlockPos next() {
                    return this.this$1.pos2.set(this.this$1.pos).offset(this.this$1.this$0.rightDir, this.w++);
                }

                @Override
                public void save() {
                    this.pw = this.w;
                    this.w = 0;
                }

                @Override
                public void restore() {
                    this.w = this.pw;
                }
            };
        }

        @Override
        public MBPIterator getRailings(final int state) {
            this.pos.coerceBlockLevel((Entity)((HighwayBuilder)this.this$0).mc.player).offset(this.this$0.dir);
            return new MBPIterator(){
                private int i;
                private int y;
                private int pi;
                private int py;
                final /* synthetic */ StraightBlockPosProvider this$1;
                {
                    StraightBlockPosProvider straightBlockPosProvider = this$1;
                    Objects.requireNonNull(straightBlockPosProvider);
                    this.this$1 = straightBlockPosProvider;
                    this.y = state;
                }

                @Override
                public boolean hasNext() {
                    return this.i < 2 && this.y < (state == 1 ? this.this$1.this$0.height.get() : state + 1);
                }

                @Override
                public MBlockPos next() {
                    if (this.i == 0) {
                        this.this$1.pos2.set(this.this$1.pos).offset(this.this$1.this$0.leftDir, this.this$1.this$0.getWidthLeft() + 1).add(0, this.y, 0);
                    } else {
                        this.this$1.pos2.set(this.this$1.pos).offset(this.this$1.this$0.rightDir, this.this$1.this$0.getWidthRight() + 1).add(0, this.y, 0);
                    }
                    ++this.y;
                    if (this.y >= (state == 1 ? this.this$1.this$0.height.get() : state + 1)) {
                        this.y = state;
                        ++this.i;
                    }
                    return this.this$1.pos2;
                }

                @Override
                public void save() {
                    this.pi = this.i;
                    this.py = this.y;
                    this.i = 0;
                    this.y = state;
                }

                @Override
                public void restore() {
                    this.i = this.pi;
                    this.y = this.py;
                }
            };
        }

        @Override
        public MBPIterator getLiquids() {
            this.pos.coerceBlockLevel((Entity)((HighwayBuilder)this.this$0).mc.player).offset(this.this$0.dir, 2).offset(this.this$0.leftDir, this.this$0.getWidthLeft() + (this.this$0.mineAboveRailings.get() != false ? 2 : 1));
            return new MBPIterator(this){
                private int w;
                private int y;
                private int pw;
                private int py;
                final /* synthetic */ StraightBlockPosProvider this$1;
                {
                    StraightBlockPosProvider straightBlockPosProvider = this$1;
                    Objects.requireNonNull(straightBlockPosProvider);
                    this.this$1 = straightBlockPosProvider;
                }

                private int getWidth() {
                    return this.this$1.this$0.width.get() + (this.this$1.this$0.mineAboveRailings.get() != false ? 2 : 0);
                }

                @Override
                public boolean hasNext() {
                    return this.w < this.getWidth() + 2 && this.y < this.this$1.this$0.height.get() + 1;
                }

                @Override
                public MBlockPos next() {
                    this.this$1.pos2.set(this.this$1.pos).offset(this.this$1.this$0.rightDir, this.w).add(0, this.y, 0);
                    ++this.w;
                    if (this.w >= this.getWidth() + 2) {
                        this.w = 0;
                        ++this.y;
                    }
                    return this.this$1.pos2;
                }

                @Override
                public void save() {
                    this.pw = this.w;
                    this.py = this.y;
                    this.y = 0;
                    this.w = 0;
                }

                @Override
                public void restore() {
                    this.w = this.pw;
                    this.y = this.py;
                }
            };
        }

        @Override
        public MBPIterator getBlockade(final boolean mine, final BlockadeType blockadeType) {
            return new MBPIterator(){
                private int i;
                private int y;
                private int pi;
                private int py;
                final /* synthetic */ StraightBlockPosProvider this$1;
                {
                    StraightBlockPosProvider straightBlockPosProvider = this$1;
                    Objects.requireNonNull(straightBlockPosProvider);
                    this.this$1 = straightBlockPosProvider;
                    this.i = mine ? -1 : 0;
                }

                private MBlockPos get(int i) {
                    this.this$1.pos.coerceBlockLevel((Entity)((HighwayBuilder)this.this$1.this$0).mc.player).offset(this.this$1.this$0.dir.opposite());
                    return switch (i) {
                        case -1 -> this.this$1.pos;
                        case 0 -> this.this$1.pos.offset(this.this$1.this$0.dir.opposite());
                        case 1 -> this.this$1.pos.offset(this.this$1.this$0.leftDir);
                        case 2 -> this.this$1.pos.offset(this.this$1.this$0.rightDir);
                        case 3 -> this.this$1.pos.offset(this.this$1.this$0.dir, 2);
                        case 4 -> this.this$1.pos.offset(this.this$1.this$0.dir).offset(this.this$1.this$0.leftDir);
                        case 5 -> this.this$1.pos.offset(this.this$1.this$0.dir).offset(this.this$1.this$0.rightDir);
                        default -> throw new IllegalStateException("Unexpected value: " + i);
                    };
                }

                @Override
                public boolean hasNext() {
                    return this.i < blockadeType.columns && this.y < 2;
                }

                @Override
                public MBlockPos next() {
                    if (this.this$1.this$0.width.get() == 1 && this.this$1.this$0.railings.get().booleanValue() && this.i > 0 && this.y == 0) {
                        ++this.y;
                    }
                    MBlockPos pos = this.get(this.i).add(0, this.y, 0);
                    ++this.y;
                    if (this.y > 1) {
                        this.y = 0;
                        ++this.i;
                    }
                    return pos;
                }

                @Override
                public void save() {
                    this.pi = this.i;
                    this.py = this.y;
                    this.y = 0;
                    this.i = 0;
                }

                @Override
                public void restore() {
                    this.i = this.pi;
                    this.y = this.py;
                }

                @Override
                public int placementsPerTick(HighwayBuilder b) {
                    return 1;
                }
            };
        }
    }

    private static interface IBlockPosProvider {
        public MBPIterator getFront();

        public MBPIterator getFloor();

        public MBPIterator getRailings(int var1);

        public MBPIterator getLiquids();

        public MBPIterator getBlockade(boolean var1, BlockadeType var2);
    }

    private static enum State {
        Center{

            @Override
            protected void start(HighwayBuilder b) {
                if (((HighwayBuilder)b).mc.player.position().closerThan((Position)Vec3.atBottomCenterOf((Vec3i)((HighwayBuilder)b).mc.player.blockPosition()), 0.1)) {
                    this.stop(b);
                }
            }

            @Override
            protected void tick(HighwayBuilder b) {
                boolean isZ;
                double x = Math.abs(((HighwayBuilder)b).mc.player.getX() - (double)((int)((HighwayBuilder)b).mc.player.getX())) - 0.5;
                double z = Math.abs(((HighwayBuilder)b).mc.player.getZ() - (double)((int)((HighwayBuilder)b).mc.player.getZ())) - 0.5;
                boolean isX = Math.abs(x) <= 0.1;
                boolean bl = isZ = Math.abs(z) <= 0.1;
                if (isX && isZ) {
                    this.stop(b);
                } else {
                    ((HighwayBuilder)b).mc.player.setYRot(0.0f);
                    if (!isZ) {
                        b.input.forward(z < 0.0);
                        b.input.backward(z > 0.0);
                        if (((HighwayBuilder)b).mc.player.getZ() < 0.0) {
                            boolean forward = b.input.keyPresses.forward();
                            b.input.forward(b.input.keyPresses.backward());
                            b.input.backward(forward);
                        }
                    }
                    if (!isX) {
                        b.input.right(x > 0.0);
                        b.input.left(x < 0.0);
                        if (((HighwayBuilder)b).mc.player.getX() < 0.0) {
                            boolean right = b.input.keyPresses.right();
                            b.input.right(b.input.keyPresses.left());
                            b.input.left(right);
                        }
                    }
                    b.input.sneak(true);
                }
            }

            private void stop(HighwayBuilder b) {
                b.input.stop();
                ((HighwayBuilder)b).mc.player.setDeltaMovement(0.0, 0.0, 0.0);
                ((HighwayBuilder)b).mc.player.setPos((double)((int)((HighwayBuilder)b).mc.player.getX()) + (((HighwayBuilder)b).mc.player.getX() < 0.0 ? -0.5 : 0.5), ((HighwayBuilder)b).mc.player.getY(), (double)((int)((HighwayBuilder)b).mc.player.getZ()) + (((HighwayBuilder)b).mc.player.getZ() < 0.0 ? -0.5 : 0.5));
                b.setState(b.lastState);
            }
        }
        ,
        Forward{

            @Override
            protected void start(HighwayBuilder b) {
                this.checkTasks(b);
                if (b.state == Forward) {
                    ((HighwayBuilder)b).mc.player.setYRot(b.dir.yaw);
                }
            }

            @Override
            protected void tick(HighwayBuilder b) {
                this.checkTasks(b);
                if (b.state == Forward) {
                    b.input.forward(true);
                }
            }

            private void checkTasks(HighwayBuilder b) {
                if (b.destroyCrystalTraps.get().booleanValue() && this.isCrystalTrap(b)) {
                    b.setState(DefuseCrystalTraps);
                } else if (this.needsToPlace(b, b.blockPosProvider.getLiquids(), true)) {
                    b.setState(FillLiquids);
                } else if (this.needsToMine(b, b.blockPosProvider.getFront(), true)) {
                    b.setState(MineFront);
                } else if (b.floor.get() == Floor.Replace && this.needsToMine(b, b.blockPosProvider.getFloor(), false)) {
                    b.setState(MineFloor);
                } else if (b.railings.get().booleanValue() && this.needsToMine(b, b.blockPosProvider.getRailings(0), false)) {
                    b.setState(MineRailings);
                } else if (b.mineAboveRailings.get().booleanValue() && this.needsToMine(b, b.blockPosProvider.getRailings(1), true)) {
                    b.setState(MineAboveRailings);
                } else if (b.railings.get().booleanValue() && this.needsToPlace(b, b.blockPosProvider.getRailings(0), false)) {
                    if (b.cornerBlock.get().booleanValue() && this.needsToPlace(b, b.blockPosProvider.getRailings(-1), false)) {
                        b.setState(PlaceCornerBlock);
                    } else {
                        b.setState(PlaceRailings);
                    }
                } else if (this.needsToPlace(b, b.blockPosProvider.getFloor(), false)) {
                    b.setState(PlaceFloor);
                }
            }

            private boolean needsToMine(HighwayBuilder b, MBPIterator it, boolean mineBlocksToPlace) {
                for (MBlockPos pos : it) {
                    if (!b.canMine(pos, mineBlocksToPlace)) continue;
                    return true;
                }
                return false;
            }

            private boolean needsToPlace(HighwayBuilder b, MBPIterator it, boolean liquids) {
                for (MBlockPos pos : it) {
                    if (!b.canPlace(pos, liquids)) continue;
                    return true;
                }
                return false;
            }

            private boolean isCrystalTrap(HighwayBuilder b) {
                for (Entity entity : ((HighwayBuilder)b).mc.level.entitiesForRendering()) {
                    EndCrystal endCrystal;
                    if (!(entity instanceof EndCrystal) || PlayerUtils.isWithin((Entity)(endCrystal = (EndCrystal)entity), 12.0) || !PlayerUtils.isWithin((Entity)endCrystal, 24.0) || b.ignoreCrystals.contains(endCrystal)) continue;
                    Vec3 vec1 = new Vec3(0.0, 0.0, 0.0);
                    Vec3 vec2 = new Vec3(0.0, 0.0, 0.0);
                    ((IVec3)vec1).meteor$set(((HighwayBuilder)b).mc.player.getX(), ((HighwayBuilder)b).mc.player.getY() + (double)((HighwayBuilder)b).mc.player.getEyeHeight(), ((HighwayBuilder)b).mc.player.getZ());
                    ((IVec3)vec2).meteor$set(entity.getX(), entity.getY() + 0.5, entity.getZ());
                    return ((HighwayBuilder)b).mc.level.clip(new ClipContext(vec1, vec2, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)((HighwayBuilder)b).mc.player)).getType() == HitResult.Type.MISS;
                }
                return false;
            }
        }
        ,
        ReLevel{
            private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            private BlockPos startPos;
            private int timer = 30;

            @Override
            protected void start(HighwayBuilder b) {
                this.startPos = BlockPos.containing((Position)b.start);
            }

            @Override
            protected void tick(HighwayBuilder b) {
                Vec3 vec = ((HighwayBuilder)b).mc.player.position().add(((HighwayBuilder)b).mc.player.getDeltaMovement()).add(0.0, -0.75, 0.0);
                this.pos.set((double)((HighwayBuilder)b).mc.player.getBlockX(), vec.y, (double)((HighwayBuilder)b).mc.player.getBlockZ());
                if (this.pos.getY() >= ((HighwayBuilder)b).mc.player.blockPosition().getY()) {
                    this.pos.setY(((HighwayBuilder)b).mc.player.blockPosition().getY() - 1);
                }
                if (this.pos.getY() >= this.startPos.getY()) {
                    this.pos.setY(this.startPos.getY() - 1);
                }
                if (((HighwayBuilder)b).mc.player.getY() > b.start.y - 0.5 && !((HighwayBuilder)b).mc.level.getBlockState((BlockPos)this.pos).canBeReplaced()) {
                    b.input.jump(false);
                    if (this.timer > 0) {
                        --this.timer;
                    } else {
                        b.setState(Forward);
                        this.timer = 30;
                    }
                    return;
                }
                if (b.placeTimer > 0) {
                    return;
                }
                if (this.timer < 30) {
                    this.timer = 30;
                }
                b.input.jump(true);
                int slot = -1;
                if (this.pos.getY() == this.startPos.below().getY()) {
                    slot = this.findAndMoveToHotbar(b, itemStack -> {
                        Item patt0$temp = itemStack.getItem();
                        if (!(patt0$temp instanceof BlockItem)) return false;
                        BlockItem blockItem = (BlockItem)patt0$temp;
                        if (!b.blocksToPlace.get().contains(blockItem.getBlock())) return false;
                        return true;
                    });
                }
                if (slot == -1 && (slot = this.findAcceptablePlacementBlock(b)) == -1) {
                    return;
                }
                if (BlockUtils.place(this.pos.immutable(), InteractionHand.MAIN_HAND, slot, b.rotation.get().place, 100, true, true, false)) {
                    if (b.renderPlace.get().booleanValue()) {
                        RenderUtils.renderTickingBlock(this.pos.immutable(), b.renderPlaceSideColor.get(), b.renderPlaceLineColor.get(), b.renderPlaceShape.get(), 0, 5, true, false);
                    }
                    b.placeTimer = b.placeDelay.get();
                }
            }

            private int findAcceptablePlacementBlock(HighwayBuilder b) {
                int slot = this.findAndMoveToHotbar(b, itemStack -> {
                    if (!(itemStack.getItem() instanceof BlockItem)) {
                        return false;
                    }
                    return b.trashItems.get().contains(itemStack.getItem());
                });
                if (slot == -1) {
                    slot = this.findAndMoveToHotbar(b, itemStack -> {
                        Item patt0$temp = itemStack.getItem();
                        if (!(patt0$temp instanceof BlockItem)) {
                            return false;
                        }
                        BlockItem bi = (BlockItem)patt0$temp;
                        return b.blocksToPlace.get().contains(bi.getBlock());
                    });
                }
                return slot != -1 ? slot : this.findAndMoveToHotbar(b, itemStack -> {
                    Item patt0$temp = itemStack.getItem();
                    if (!(patt0$temp instanceof BlockItem)) {
                        return false;
                    }
                    BlockItem bi = (BlockItem)patt0$temp;
                    if (Utils.isShulker((Item)bi)) {
                        return false;
                    }
                    Block block = bi.getBlock();
                    if (!Block.isShapeFullBlock((VoxelShape)block.defaultBlockState().getCollisionShape((BlockGetter)((HighwayBuilder)b).mc.level, (BlockPos)this.pos))) {
                        return false;
                    }
                    return !(block instanceof FallingBlock) || !FallingBlock.isFree((BlockState)((HighwayBuilder)b).mc.level.getBlockState((BlockPos)this.pos));
                });
            }
        }
        ,
        FillLiquids{

            @Override
            protected void tick(HighwayBuilder b) {
                int slot = this.findBlocksToPlacePrioritizeTrash(b);
                if (slot == -1) {
                    return;
                }
                this.place(b, new MBPIteratorFilter(b.blockPosProvider.getLiquids(), pos -> !pos.getState().getFluidState().isEmpty()), slot, Forward);
            }
        }
        ,
        MineFront{

            @Override
            protected void start(HighwayBuilder b) {
                this.mine(b, b.blockPosProvider.getFront(), true, Forward, this);
            }

            @Override
            protected void tick(HighwayBuilder b) {
                this.mine(b, b.blockPosProvider.getFront(), true, Forward, this);
            }
        }
        ,
        MineFloor{

            @Override
            protected void start(HighwayBuilder b) {
                this.mine(b, b.blockPosProvider.getFloor(), false, Forward, this);
            }

            @Override
            protected void tick(HighwayBuilder b) {
                this.mine(b, b.blockPosProvider.getFloor(), false, Forward, this);
            }
        }
        ,
        MineRailings{

            @Override
            protected void start(HighwayBuilder b) {
                this.mine(b, b.blockPosProvider.getRailings(0), false, Forward, this);
            }

            @Override
            protected void tick(HighwayBuilder b) {
                this.mine(b, b.blockPosProvider.getRailings(0), false, Forward, this);
            }
        }
        ,
        MineAboveRailings{

            @Override
            protected void start(HighwayBuilder b) {
                this.mine(b, b.blockPosProvider.getRailings(1), true, Forward, this);
            }

            @Override
            protected void tick(HighwayBuilder b) {
                this.mine(b, b.blockPosProvider.getRailings(1), true, Forward, this);
            }
        }
        ,
        PlaceCornerBlock{

            @Override
            protected void start(HighwayBuilder b) {
                int slot = this.findBlocksToPlacePrioritizeTrash(b);
                if (slot == -1) {
                    return;
                }
                this.place(b, b.blockPosProvider.getRailings(-1), slot, Forward);
            }

            @Override
            protected void tick(HighwayBuilder b) {
                int slot = this.findBlocksToPlacePrioritizeTrash(b);
                if (slot == -1) {
                    return;
                }
                this.place(b, b.blockPosProvider.getRailings(-1), slot, Forward);
            }
        }
        ,
        PlaceRailings{

            @Override
            protected void start(HighwayBuilder b) {
                int slot = this.findBlocksToPlace(b);
                if (slot == -1) {
                    return;
                }
                this.place(b, b.blockPosProvider.getRailings(0), slot, Forward);
            }

            @Override
            protected void tick(HighwayBuilder b) {
                int slot = this.findBlocksToPlace(b);
                if (slot == -1) {
                    return;
                }
                this.place(b, b.blockPosProvider.getRailings(0), slot, Forward);
            }
        }
        ,
        PlaceFloor{

            @Override
            protected void start(HighwayBuilder b) {
                int slot = this.findBlocksToPlace(b);
                if (slot == -1) {
                    return;
                }
                this.place(b, b.blockPosProvider.getFloor(), slot, Forward);
            }

            @Override
            protected void tick(HighwayBuilder b) {
                int slot = this.findBlocksToPlace(b);
                if (slot == -1) {
                    return;
                }
                this.place(b, b.blockPosProvider.getFloor(), slot, Forward);
            }
        }
        ,
        ThrowOutTrash{
            private int skipSlot;
            private boolean timerEnabled;
            private boolean firstTick;
            private boolean threwItems;
            private int timer;
            private static final ItemStack[] ITEMS = new ItemStack[27];

            @Override
            protected void start(HighwayBuilder b) {
                int biggestCount = 0;
                for (int i = 0; i < ((HighwayBuilder)b).mc.player.getInventory().getNonEquipmentItems().size(); ++i) {
                    ItemStack itemStack = ((HighwayBuilder)b).mc.player.getInventory().getItem(i);
                    if (!(itemStack.getItem() instanceof BlockItem) || !b.trashItems.get().contains(itemStack.getItem()) || itemStack.getCount() <= biggestCount) continue;
                    biggestCount = itemStack.getCount();
                    this.skipSlot = i;
                    if (biggestCount >= 64) break;
                }
                if (biggestCount == 0) {
                    this.skipSlot = -1;
                }
                this.timerEnabled = false;
                this.firstTick = true;
                this.threwItems = false;
            }

            @Override
            protected void tick(HighwayBuilder b) {
                if (this.timerEnabled) {
                    if (this.timer > 0) {
                        --this.timer;
                    } else {
                        b.setState(b.lastState);
                    }
                    return;
                }
                ((HighwayBuilder)b).mc.player.setYRot(b.dir.opposite().yaw);
                ((HighwayBuilder)b).mc.player.setXRot(-25.0f);
                if (this.firstTick) {
                    this.firstTick = false;
                    return;
                }
                if (!((HighwayBuilder)b).mc.player.containerMenu.getCarried().isEmpty()) {
                    InvUtils.dropHand();
                    return;
                }
                for (int i = 0; i < ((HighwayBuilder)b).mc.player.getInventory().getNonEquipmentItems().size(); ++i) {
                    if (i == this.skipSlot) continue;
                    ItemStack itemStack = ((HighwayBuilder)b).mc.player.getInventory().getItem(i);
                    if (b.trashItems.get().contains(itemStack.getItem())) {
                        InvUtils.drop().slot(i);
                        this.threwItems = true;
                        return;
                    }
                    if (!b.ejectUselessShulkers.get().booleanValue() || !Utils.isShulker(itemStack.getItem())) continue;
                    Utils.getItemsInContainerItem(itemStack, ITEMS);
                    boolean eject = true;
                    for (ItemStack stack : ITEMS) {
                        Item item = stack.getItem();
                        if (item instanceof BlockItem) {
                            BlockItem bi = (BlockItem)item;
                            if (b.blocksToPlace.get().contains(bi.getBlock()) || b.blocksToPlace.get().contains(Blocks.OBSIDIAN) && bi == Items.ENDER_CHEST) {
                                eject = false;
                                break;
                            }
                        }
                        if (stack.is(ItemTags.PICKAXES)) {
                            eject = false;
                            break;
                        }
                        if (!Utils.isFood(stack) || Modules.get().get(AutoEat.class).blacklist.get().contains(stack.getItem())) continue;
                        eject = false;
                        break;
                    }
                    if (!eject) continue;
                    InvUtils.drop().slot(i);
                    this.threwItems = true;
                    return;
                }
                this.timerEnabled = true;
                this.timer = this.threwItems ? 10 : 1;
            }
        }
        ,
        PlaceEChestBlockade{

            @Override
            protected void tick(HighwayBuilder b) {
                int slot = this.findBlocksToPlacePrioritizeTrash(b);
                if (slot == -1) {
                    return;
                }
                this.place(b, b.blockPosProvider.getBlockade(false, b.blockadeType.get()), slot, MineEnderChests);
            }
        }
        ,
        MineEChestBlockade{

            @Override
            protected void tick(HighwayBuilder b) {
                this.mine(b, b.blockPosProvider.getBlockade(true, b.blockadeType.get()), true, Center, Forward);
            }
        }
        ,
        MineEnderChests{
            private static final MBlockPos pos = new MBlockPos();
            private int minimumObsidian;
            private boolean first;
            private boolean primed;
            private boolean stopTimerEnabled;
            private int stopTimer;
            private int moveTimer;
            private int rebreakTimer;
            private int timeout;

            @Override
            protected void start(HighwayBuilder b) {
                if (b.lastState != Center && b.lastState != ThrowOutTrash && b.lastState != PlaceEChestBlockade) {
                    b.setState(Center);
                    return;
                }
                if (b.lastState == Center) {
                    b.setState(ThrowOutTrash);
                    return;
                }
                if (b.lastState == ThrowOutTrash) {
                    b.setState(PlaceEChestBlockade);
                    return;
                }
                int emptySlots = 0;
                for (int i = 0; i < ((HighwayBuilder)b).mc.player.getInventory().getNonEquipmentItems().size(); ++i) {
                    if (!((HighwayBuilder)b).mc.player.getInventory().getItem(i).isEmpty()) continue;
                    ++emptySlots;
                }
                if (emptySlots == 0) {
                    b.error("No empty slots.", new Object[0]);
                    return;
                }
                int minimumSlots = Math.max(emptySlots - b.minEmpty.get(), 1);
                this.minimumObsidian = minimumSlots * 64;
                this.first = true;
                this.timeout = 0;
                this.moveTimer = 0;
                this.stopTimerEnabled = false;
                this.primed = false;
            }

            @Override
            protected void tick(HighwayBuilder b) {
                if (this.stopTimerEnabled) {
                    if (this.stopTimer > 0) {
                        --this.stopTimer;
                    } else {
                        b.setState(MineEChestBlockade);
                    }
                    return;
                }
                HorizontalDirection dir = b.dir.diagonal ? b.dir.rotateLeft().rotateLeftSkipOne() : b.dir.opposite();
                pos.set((Entity)((HighwayBuilder)b).mc.player).offset(dir);
                if (this.moveTimer > 0) {
                    ((HighwayBuilder)b).mc.player.setYRot(dir.yaw);
                    b.input.forward(this.moveTimer > 2);
                    --this.moveTimer;
                    return;
                }
                int obsidianCount = 0;
                for (Entity entity : ((HighwayBuilder)b).mc.level.getEntities((Entity)((HighwayBuilder)b).mc.player, new AABB((double)15.pos.x, (double)15.pos.y, (double)15.pos.z, (double)(15.pos.x + 1), (double)(15.pos.y + 2), (double)(15.pos.z + 1)))) {
                    ItemEntity itemEntity;
                    if (!(entity instanceof ItemEntity) || (itemEntity = (ItemEntity)entity).getItem().getItem() != Items.OBSIDIAN) continue;
                    obsidianCount += itemEntity.getItem().getCount();
                }
                for (int i = 0; i < ((HighwayBuilder)b).mc.player.getInventory().getNonEquipmentItems().size(); ++i) {
                    ItemStack itemStack2 = ((HighwayBuilder)b).mc.player.getInventory().getItem(i);
                    if (itemStack2.getItem() != Items.OBSIDIAN) continue;
                    obsidianCount += itemStack2.getCount();
                }
                if (obsidianCount >= this.minimumObsidian) {
                    this.stopTimerEnabled = true;
                    this.stopTimer = 12;
                    return;
                }
                BlockPos bp = pos.getBlockPos();
                BlockState blockState = ((HighwayBuilder)b).mc.level.getBlockState(bp);
                if (blockState.getBlock() == Blocks.ENDER_CHEST) {
                    Screen screen = ((HighwayBuilder)b).mc.screen;
                    if (screen instanceof ContainerScreen) {
                        ContainerScreen screen2 = (ContainerScreen)screen;
                        if (((ChestMenu)screen2.getMenu()).containerId != b.containerId) {
                            return;
                        }
                        ((HighwayBuilder)b).mc.screen.onClose();
                    }
                    if (!EChestMemory.isKnown()) {
                        if (b.rotation.get().place) {
                            Rotations.rotate(Rotations.getYaw(bp), Rotations.getPitch(bp), () -> ((HighwayBuilder)b).mc.gameMode.useItemOn(((HighwayBuilder)b).mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf((Vec3i)bp), Direction.UP, bp, false)));
                        } else {
                            ((HighwayBuilder)b).mc.gameMode.useItemOn(((HighwayBuilder)b).mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf((Vec3i)bp), Direction.UP, bp, false));
                        }
                        return;
                    }
                    if (this.first) {
                        this.moveTimer = 8;
                        this.first = false;
                        return;
                    }
                    int slot = this.findAndMoveBestToolToHotbar(b, blockState, true);
                    if (slot == -1) {
                        b.error("Cannot find pickaxe without silk touch to mine ender chests.", new Object[0]);
                        return;
                    }
                    InvUtils.swap(slot, false);
                    if (b.rebreakEchests.get().booleanValue() && this.primed) {
                        ++this.timeout;
                        if (this.timeout > 60) {
                            this.primed = false;
                            this.timeout = 0;
                            return;
                        }
                        if (this.rebreakTimer > 0) {
                            --this.rebreakTimer;
                            return;
                        }
                        this.rebreakTimer = b.rebreakTimer.get();
                        if (b.rotation.get().mine) {
                            Rotations.rotate(Rotations.getYaw(bp), Rotations.getPitch(bp), () -> ((HighwayBuilder)b).mc.gameMode.startPrediction(((HighwayBuilder)b).mc.level, sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, bp, BlockUtils.getDirection(bp), sequence)));
                        } else {
                            ((HighwayBuilder)b).mc.gameMode.startPrediction(((HighwayBuilder)b).mc.level, sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, bp, BlockUtils.getDirection(bp), sequence));
                        }
                    } else if (b.rotation.get().mine) {
                        Rotations.rotate(Rotations.getYaw(bp), Rotations.getPitch(bp), () -> BlockUtils.breakBlock(bp, true));
                    } else {
                        BlockUtils.breakBlock(bp, true);
                    }
                } else {
                    int slot = this.findAndMoveToHotbar(b, itemStack -> itemStack.getItem() == Items.ENDER_CHEST);
                    if (slot == -1 || this.countItem(b, stack -> stack.getItem().equals(Items.ENDER_CHEST)) <= b.saveEchests.get()) {
                        this.stopTimerEnabled = true;
                        this.stopTimer = 12;
                        return;
                    }
                    if (this.countItem(b, stack -> stack.is(ItemTags.PICKAXES)) <= b.savePickaxes.get() && (b.searchEnderChest.get().booleanValue() || b.searchShulkers.get().booleanValue())) {
                        b.restockTask.setPickaxes();
                    }
                    if (!this.first) {
                        this.primed = true;
                    }
                    BlockUtils.place(bp, InteractionHand.MAIN_HAND, slot, b.rotation.get().place, 0, true, true, false);
                    this.timeout = 0;
                }
            }
        }
        ,
        Restock{
            private static final MBlockPos pos = new MBlockPos();
            private static final ItemStack[] ITEMS = new ItemStack[27];
            private int minimumSlots;
            private int stopTimer;
            private int delayTimer;
            private boolean breakContainer;
            private boolean indicateStopping;
            private Predicate<ItemStack> shulkerPredicate;
            private int slot = -1;

            @Override
            protected void start(HighwayBuilder b) {
                this.slot = -1;
                if (this.shulkerPredicate == null) {
                    this.setShulkerPredicate(b);
                }
                if (b.restockTask.tasksInactive()) {
                    b.setState(Forward);
                    return;
                }
                if (b.lastState != Center && b.lastState != ThrowOutTrash && b.lastState != PlaceShulkerBlockade && b.lastState != this) {
                    b.setState(Center);
                    return;
                }
                if (b.lastState == Center) {
                    b.setState(ThrowOutTrash);
                    return;
                }
                if (this.slot == -1 && b.searchShulkers.get().booleanValue()) {
                    this.slot = this.findAndMoveToHotbar(b, this.shulkerPredicate);
                    if (this.slot != -1 && b.lastState != PlaceShulkerBlockade) {
                        b.setState(PlaceShulkerBlockade);
                    }
                }
                if (this.slot == -1 && b.searchEnderChest.get().booleanValue() && this.countItem(b, stack -> stack.getItem().equals(Items.ENDER_CHEST)) > 0) {
                    boolean stop = EChestMemory.isKnown();
                    if (EChestMemory.isKnown()) {
                        for (ItemStack stack2 : EChestMemory.ITEMS) {
                            Item item;
                            if (b.restockTask.materials && (item = stack2.getItem()) instanceof BlockItem) {
                                BlockItem bi = (BlockItem)item;
                                if (b.blocksToPlace.get().contains(bi.getBlock()) || b.blocksToPlace.get().contains(Blocks.OBSIDIAN) && bi == Items.ENDER_CHEST) {
                                    stop = false;
                                    break;
                                }
                            }
                            if (b.restockTask.pickaxes && stack2.is(ItemTags.PICKAXES)) {
                                stop = false;
                                break;
                            }
                            if (b.restockTask.food && Utils.isFood(stack2) && !Modules.get().get(AutoEat.class).blacklist.get().contains(stack2.getItem())) {
                                stop = false;
                                break;
                            }
                            if (!b.searchShulkers.get().booleanValue() || !this.shulkerPredicate.test(stack2)) continue;
                            stop = false;
                            break;
                        }
                    }
                    if (!stop) {
                        this.slot = this.findAndMoveToHotbar(b, itemStack -> itemStack.getItem() == Items.ENDER_CHEST);
                    }
                }
                if (this.slot == -1) {
                    boolean restockOccurred;
                    boolean bl = restockOccurred = b.restockTask.materials && (this.hasItem(b, stack -> {
                        Item patt0$temp = stack.getItem();
                        if (!(patt0$temp instanceof BlockItem)) return false;
                        BlockItem bi = (BlockItem)patt0$temp;
                        if (!b.blocksToPlace.get().contains(bi.getBlock())) return false;
                        return true;
                    }) || b.blocksToPlace.get().contains(Blocks.OBSIDIAN) && this.countItem(b, itemStack -> itemStack.getItem() == Items.ENDER_CHEST) > b.saveEchests.get()) || b.restockTask.pickaxes && this.countItem(b, itemStack -> itemStack.is(ItemTags.PICKAXES)) > b.savePickaxes.get() || b.restockTask.food && this.hasItem(b, itemStack -> Utils.isFood(itemStack) && !Modules.get().get(AutoEat.class).blacklist.get().contains(itemStack.getItem()));
                    if (restockOccurred) {
                        b.setState(ThrowOutTrash, Forward);
                    } else {
                        b.error("Unable to perform restock for '" + b.restockTask.item() + "'.", new Object[0]);
                    }
                    return;
                }
                int restockSlots = -b.minEmpty.get().intValue();
                for (int i = 0; i < ((HighwayBuilder)b).mc.player.getInventory().getNonEquipmentItems().size(); ++i) {
                    if (!((HighwayBuilder)b).mc.player.getInventory().getItem(i).isEmpty()) continue;
                    ++restockSlots;
                }
                if (restockSlots <= 0) {
                    b.error("No empty slots for restocking items.", new Object[0]);
                    return;
                }
                this.minimumSlots = b.restockTask.materials ? restockSlots : 1;
                HorizontalDirection dir = b.dir.diagonal ? b.dir.rotateLeft().rotateLeftSkipOne() : b.dir.opposite();
                pos.set((Entity)((HighwayBuilder)b).mc.player).offset(dir);
                this.breakContainer = ((HighwayBuilder)b).mc.level.getBlockState(pos.getBlockPos()).getBlock() == Blocks.ENDER_CHEST;
                this.indicateStopping = false;
                this.delayTimer = b.inventoryDelay.get();
            }

            @Override
            protected void tick(HighwayBuilder b) {
                if (this.slot == -1) {
                    b.error("Invalid restocking action.", new Object[0]);
                    return;
                }
                if (this.indicateStopping && !this.breakContainer) {
                    if (this.stopTimer > 0) {
                        --this.stopTimer;
                    } else if (b.lastState == PlaceShulkerBlockade) {
                        b.setState(MineShulkerBlockade);
                    } else {
                        b.setState(ThrowOutTrash, Forward);
                    }
                    return;
                }
                if (b.restockTask.tasksInactive()) {
                    b.setState(Forward);
                    return;
                }
                if (this.delayTimer > 0) {
                    --this.delayTimer;
                    return;
                }
                int slotsPulled = 0;
                if (b.restockTask.materials) {
                    slotsPulled += this.countSlots(b, itemStack -> {
                        Item patt0$temp = itemStack.getItem();
                        if (!(patt0$temp instanceof BlockItem)) return false;
                        BlockItem bi = (BlockItem)patt0$temp;
                        if (!b.blocksToPlace.get().contains(bi.getBlock())) return false;
                        return true;
                    });
                    if (b.blocksToPlace.get().contains(Blocks.OBSIDIAN)) {
                        slotsPulled += (this.countItem(b, itemStack -> itemStack.getItem() == Items.ENDER_CHEST) - b.saveEchests.get()) * 8 / 64;
                    }
                }
                if (b.restockTask.pickaxes) {
                    slotsPulled += this.countSlots(b, itemStack -> itemStack.is(ItemTags.PICKAXES)) - b.savePickaxes.get();
                }
                if (b.restockTask.food) {
                    slotsPulled += this.countSlots(b, itemStack -> Utils.isFood(itemStack) && !Modules.get().get(AutoEat.class).blacklist.get().contains(itemStack.getItem()));
                }
                if (slotsPulled >= this.minimumSlots && !this.indicateStopping) {
                    this.indicateStopping = true;
                    this.breakContainer = true;
                    this.stopTimer = 12;
                    if (((HighwayBuilder)b).mc.screen != null) {
                        ((HighwayBuilder)b).mc.screen.onClose();
                    }
                    return;
                }
                BlockPos blockPos = pos.getBlockPos();
                BlockState blockState = ((HighwayBuilder)b).mc.level.getBlockState(blockPos);
                Block block = blockState.getBlock();
                Objects.requireNonNull(block);
                Block block2 = block;
                int n = 0;
                switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ShulkerBoxBlock.class, EnderChestBlock.class, AirBlock.class}, (Block)block2, n)) {
                    case 0: {
                        Screen screen = ((HighwayBuilder)b).mc.screen;
                        if (screen instanceof ShulkerBoxScreen) {
                            ShulkerBoxScreen screen2 = (ShulkerBoxScreen)screen;
                            if (((ShulkerBoxMenu)screen2.getMenu()).containerId != b.containerId) {
                                return;
                            }
                            Container inv = ((ShulkerBoxMenuAccessor)screen2.getMenu()).meteor$getContainer();
                            if (this.restockItems(b, inv)) {
                                this.delayTimer = b.inventoryDelay.get();
                                return;
                            }
                            ((HighwayBuilder)b).mc.screen.onClose();
                            this.breakContainer = true;
                            break;
                        }
                        if (!b.searchShulkers.get().booleanValue()) {
                            this.breakContainer = true;
                        }
                        this.handleContainerBlock(b, blockPos);
                        break;
                    }
                    case 1: {
                        Screen inv = ((HighwayBuilder)b).mc.screen;
                        if (inv instanceof ContainerScreen) {
                            int moveTo;
                            ContainerScreen screen = (ContainerScreen)inv;
                            if (((ChestMenu)screen.getMenu()).containerId != b.containerId) {
                                return;
                            }
                            inv = ((ChestMenu)screen.getMenu()).getContainer();
                            if (this.restockItems(b, (Container)inv)) {
                                this.delayTimer = b.inventoryDelay.get();
                                return;
                            }
                            if (b.searchShulkers.get().booleanValue() && (moveTo = InvUtils.findEmpty().slot()) != -1) {
                                for (int i = 0; i < inv.getContainerSize(); ++i) {
                                    if (!this.shulkerPredicate.test(inv.getItem(i))) continue;
                                    InvUtils.move().fromId(i).to(moveTo);
                                    this.delayTimer = b.inventoryDelay.get();
                                    break;
                                }
                            }
                            ((HighwayBuilder)b).mc.screen.onClose();
                            this.breakContainer = true;
                            break;
                        }
                        if (!b.searchEnderChest.get().booleanValue()) {
                            this.breakContainer = true;
                        }
                        this.handleContainerBlock(b, blockPos);
                        break;
                    }
                    case 2: {
                        if (this.breakContainer) {
                            this.breakContainer = false;
                            if (this.indicateStopping) {
                                b.restockTask.complete();
                            } else {
                                this.start(b);
                            }
                            return;
                        }
                        BlockUtils.place(blockPos, InteractionHand.MAIN_HAND, this.slot, b.rotation.get().place, 0, true, true, false);
                        break;
                    }
                    default: {
                        b.error("Invalid block at container restocking position?", new Object[0]);
                    }
                }
            }

            private boolean restockItems(HighwayBuilder b, Container inv) {
                if (b.restockTask.materials) {
                    if (this.grabFromInventory(inv, itemStack -> {
                        Item patt0$temp = itemStack.getItem();
                        if (!(patt0$temp instanceof BlockItem)) return false;
                        BlockItem bi = (BlockItem)patt0$temp;
                        if (!b.blocksToPlace.get().contains(bi.getBlock())) return false;
                        return true;
                    })) {
                        return true;
                    }
                    if (b.blocksToPlace.get().contains(Blocks.OBSIDIAN) && this.grabFromInventory(inv, itemStack -> itemStack.getItem() == Items.ENDER_CHEST)) {
                        return true;
                    }
                }
                if (b.restockTask.pickaxes && this.grabFromInventory(inv, itemStack -> itemStack.is(ItemTags.PICKAXES))) {
                    return true;
                }
                if (b.restockTask.food) {
                    return this.grabFromInventory(inv, itemStack -> Utils.isFood(itemStack) && !Modules.get().get(AutoEat.class).blacklist.get().contains(itemStack.getItem()));
                }
                return false;
            }

            private boolean grabFromInventory(Container inv, Predicate<ItemStack> filterItem) {
                for (int i = 0; i < inv.getContainerSize(); ++i) {
                    if (!filterItem.test(inv.getItem(i))) continue;
                    InvUtils.shiftClick().slotId(i);
                    return true;
                }
                return false;
            }

            private void setShulkerPredicate(HighwayBuilder b) {
                this.shulkerPredicate = itemStack -> {
                    if (!Utils.isShulker(itemStack.getItem())) {
                        return false;
                    }
                    Utils.getItemsInContainerItem(itemStack, ITEMS);
                    for (ItemStack stack : ITEMS) {
                        Item patt0$temp;
                        if (b.restockTask.materials && (patt0$temp = stack.getItem()) instanceof BlockItem) {
                            BlockItem bi = (BlockItem)patt0$temp;
                            if (b.blocksToPlace.get().contains(bi.getBlock()) || b.blocksToPlace.get().contains(Blocks.OBSIDIAN) && bi == Items.ENDER_CHEST) {
                                return true;
                            }
                        }
                        if (b.restockTask.pickaxes && stack.is(ItemTags.PICKAXES)) {
                            return true;
                        }
                        if (!b.restockTask.food || !Utils.isFood(stack) || Modules.get().get(AutoEat.class).blacklist.get().contains(stack.getItem())) continue;
                        return true;
                    }
                    return false;
                };
            }

            private void handleContainerBlock(HighwayBuilder b, BlockPos bp) {
                if (this.breakContainer) {
                    BlockState state = ((HighwayBuilder)b).mc.level.getBlockState(bp);
                    int toolSlot = this.findAndMoveBestToolToHotbar(b, state, false);
                    InvUtils.swap(toolSlot, false);
                    if (b.rotation.get().mine) {
                        Rotations.rotate(Rotations.getYaw(bp), Rotations.getPitch(bp), () -> BlockUtils.breakBlock(bp, true));
                    } else {
                        BlockUtils.breakBlock(bp, true);
                    }
                } else {
                    if (b.rotation.get().place) {
                        Rotations.rotate(Rotations.getYaw(bp), Rotations.getPitch(bp), () -> ((HighwayBuilder)b).mc.gameMode.useItemOn(((HighwayBuilder)b).mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf((Vec3i)bp), Direction.UP, bp, false)));
                    } else {
                        ((HighwayBuilder)b).mc.gameMode.useItemOn(((HighwayBuilder)b).mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf((Vec3i)bp), Direction.UP, bp, false));
                    }
                    this.delayTimer = b.inventoryDelay.get();
                }
            }

            private int countSlots(HighwayBuilder b, Predicate<ItemStack> predicate) {
                int count = 0;
                for (int i = 0; i < ((HighwayBuilder)b).mc.player.getInventory().getNonEquipmentItems().size(); ++i) {
                    ItemStack stack = ((HighwayBuilder)b).mc.player.getInventory().getItem(i);
                    if (!predicate.test(stack)) continue;
                    ++count;
                }
                return count;
            }
        }
        ,
        PlaceShulkerBlockade{

            @Override
            protected void tick(HighwayBuilder b) {
                int slot = this.findBlocksToPlacePrioritizeTrash(b);
                if (slot == -1) {
                    return;
                }
                this.place(b, b.blockPosProvider.getBlockade(false, BlockadeType.Shulker), slot, Restock);
            }
        }
        ,
        MineShulkerBlockade{
            private boolean stopTimerEnabled;
            private int stopTimer;

            @Override
            protected void start(HighwayBuilder b) {
                this.stopTimerEnabled = false;
                if (b.lastState == this) {
                    this.stopTimerEnabled = true;
                    this.stopTimer = 12;
                }
            }

            @Override
            protected void tick(HighwayBuilder b) {
                if (!this.stopTimerEnabled) {
                    this.mine(b, b.blockPosProvider.getBlockade(true, b.blockadeType.get()), true, this, this);
                } else {
                    --this.stopTimer;
                    if (this.stopTimer <= 0) {
                        b.setState(ThrowOutTrash, Forward);
                    }
                }
            }
        }
        ,
        DefuseCrystalTraps{
            private int cooldown;
            private int shots;
            private EndCrystal target;

            @Override
            protected void start(HighwayBuilder b) {
                if (!InvUtils.find(Items.BOW).found() || !InvUtils.find(itemStack -> itemStack.getItem() instanceof ArrowItem).found() && !((HighwayBuilder)b).mc.player.getAbilities().instabuild) {
                    b.destroyCrystalTraps.set(false);
                    b.warning("No bow found to destroy crystal traps with. Toggling the setting off.", new Object[0]);
                    b.setState(Forward);
                }
                this.cooldown = 0;
                this.shots = 0;
                this.target = null;
            }

            @Override
            protected void tick(HighwayBuilder b) {
                if (this.cooldown > 0) {
                    --this.cooldown;
                    return;
                }
                if (!InvUtils.testInMainHand(Items.BOW)) {
                    int slot = this.findAndMoveToHotbar(b, itemStack -> itemStack.getItem() instanceof BowItem);
                    if (slot == -1) {
                        b.destroyCrystalTraps.set(false);
                        b.warning("No bow found to destroy crystal traps with. Toggling the setting off.", new Object[0]);
                        b.setState(Forward);
                        ((HighwayBuilder)b).mc.gameMode.releaseUsingItem((Player)((HighwayBuilder)b).mc.player);
                        b.drawingBow = false;
                        return;
                    }
                    InvUtils.swap(slot, false);
                }
                EndCrystal potentialTarget = (EndCrystal)TargetUtils.get(entity -> {
                    if (!(entity instanceof EndCrystal)) {
                        return false;
                    }
                    EndCrystal endCrystal = (EndCrystal)entity;
                    if (PlayerUtils.isWithin((Entity)endCrystal, 12.0) || !PlayerUtils.isWithin((Entity)endCrystal, 24.0)) {
                        return false;
                    }
                    if (b.ignoreCrystals.contains(endCrystal)) {
                        return false;
                    }
                    Vec3 vec1 = new Vec3(0.0, 0.0, 0.0);
                    Vec3 vec2 = new Vec3(0.0, 0.0, 0.0);
                    ((IVec3)vec1).meteor$set(((HighwayBuilder)b).mc.player.getX(), ((HighwayBuilder)b).mc.player.getY() + (double)((HighwayBuilder)b).mc.player.getEyeHeight(), ((HighwayBuilder)b).mc.player.getZ());
                    ((IVec3)vec2).meteor$set(entity.getX(), entity.getY() + 0.5, entity.getZ());
                    return ((HighwayBuilder)b).mc.level.clip(new ClipContext(vec1, vec2, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)((HighwayBuilder)b).mc.player)).getType() == HitResult.Type.MISS;
                }, SortPriority.LowestDistance);
                if (this.target == null || this.target.isRemoved()) {
                    if (potentialTarget == null) {
                        b.setState(Forward);
                        ((HighwayBuilder)b).mc.gameMode.releaseUsingItem((Player)((HighwayBuilder)b).mc.player);
                        b.drawingBow = false;
                        return;
                    }
                    this.target = potentialTarget;
                    this.shots = 0;
                }
                if (this.shots >= 3) {
                    b.ignoreCrystals.add(this.target);
                    b.warning("Detected potential hangup on a crystal. Adding it to ignore list and continuing forward.", new Object[0]);
                    b.setState(Forward);
                    ((HighwayBuilder)b).mc.gameMode.releaseUsingItem((Player)((HighwayBuilder)b).mc.player);
                    b.drawingBow = false;
                    return;
                }
                ((HighwayBuilder)b).mc.player.setYRot((float)Rotations.getYaw((Entity)this.target));
                float pitch = this.aim(b, (Entity)this.target);
                if (Float.isNaN(pitch)) {
                    ((HighwayBuilder)b).mc.player.setXRot((float)Rotations.getPitch((Entity)this.target));
                } else {
                    ((HighwayBuilder)b).mc.player.setXRot(pitch);
                }
                if (BowItem.getPowerForTime((int)(((HighwayBuilder)b).mc.player.getTicksUsingItem() - 3)) >= 1.0f) {
                    ((HighwayBuilder)b).mc.gameMode.releaseUsingItem((Player)((HighwayBuilder)b).mc.player);
                    b.drawingBow = false;
                    this.cooldown = 20;
                    ++this.shots;
                } else {
                    b.drawingBow = true;
                    ((HighwayBuilder)b).mc.gameMode.useItem((Player)((HighwayBuilder)b).mc.player, InteractionHand.MAIN_HAND);
                }
            }

            private float aim(HighwayBuilder b, Entity target) {
                float velocity = BowItem.getPowerForTime((int)((HighwayBuilder)b).mc.player.getTicksUsingItem());
                Vec3 pos = target.position();
                double relativeX = pos.x - ((HighwayBuilder)b).mc.player.getX();
                double relativeY = pos.y + 0.5 - ((HighwayBuilder)b).mc.player.getEyeY();
                double relativeZ = pos.z - ((HighwayBuilder)b).mc.player.getZ();
                double hDistance = Math.sqrt(relativeX * relativeX + relativeZ * relativeZ);
                double hDistanceSq = hDistance * hDistance;
                float g = 0.006f;
                float velocitySq = velocity * velocity;
                return (float)(-Math.toDegrees(Math.atan(((double)velocitySq - Math.sqrt((double)(velocitySq * velocitySq) - (double)g * ((double)g * hDistanceSq + 2.0 * relativeY * (double)velocitySq))) / ((double)g * hDistance))));
            }
        };


        protected void start(HighwayBuilder b) {
        }

        protected abstract void tick(HighwayBuilder var1);

        protected void mine(HighwayBuilder b, MBPIterator it, boolean mineBlocksToPlace, State nextState, State lastState) {
            boolean breaking = false;
            boolean finishedBreaking = false;
            if (b.doubleMine.get().booleanValue()) {
                int slot;
                ArrayDeque<BlockPos> toDoubleMine = new ArrayDeque<BlockPos>();
                it.save();
                it.forEach(pos -> {
                    if (!(!BlockUtils.canBreak(pos.getBlockPos(), pos.getState()) || !mineBlocksToPlace && b.blocksToPlace.get().contains(pos.getState().getBlock()) || BlockUtils.canInstaBreak(pos.getBlockPos()) || Modules.get().get(SpeedMine.class).instamine() && !((double)pos.getState().getDestroyProgress((Player)((HighwayBuilder)b).mc.player, (BlockGetter)((HighwayBuilder)b).mc.level, pos.getBlockPos()) <= 0.5) || b.normalMining != null && pos.getBlockPos().equals((Object)b.normalMining.blockPos) || b.packetMining != null && pos.getBlockPos().equals((Object)b.packetMining.blockPos))) {
                        toDoubleMine.add((BlockPos)pos.getBlockPos().mutable());
                    }
                });
                it.restore();
                if (!toDoubleMine.isEmpty()) {
                    slot = this.findAndMoveBestToolToHotbar(b, ((HighwayBuilder)b).mc.level.getBlockState((BlockPos)toDoubleMine.peek()), false);
                    if (slot == -1) {
                        return;
                    }
                    InvUtils.swap(slot, false);
                    this.doubleMine(b, toDoubleMine);
                }
                if (b.normalMining != null || b.packetMining != null) {
                    slot = this.findAndMoveBestToolToHotbar(b, b.normalMining != null ? b.normalMining.blockState : b.packetMining.blockState, false);
                    if (slot == -1) {
                        return;
                    }
                    InvUtils.swap(slot, false);
                    return;
                }
            }
            for (MBlockPos pos2 : it) {
                boolean multiBreak;
                if (b.count >= b.blocksPerTick.get()) {
                    return;
                }
                if (b.breakTimer > 0) {
                    return;
                }
                BlockState state = pos2.getState();
                if (state.isAir() || !mineBlocksToPlace && b.blocksToPlace.get().contains(state.getBlock())) continue;
                int slot = this.findAndMoveBestToolToHotbar(b, state, false);
                if (slot == -1) {
                    return;
                }
                InvUtils.swap(slot, false);
                BlockPos mcPos = pos2.getBlockPos();
                boolean bl = multiBreak = b.blocksPerTick.get() > 1 && BlockUtils.canInstaBreak(mcPos) && !b.rotation.get().mine;
                if (BlockUtils.canBreak(mcPos)) {
                    if (b.rotation.get().mine) {
                        Rotations.rotate(Rotations.getYaw(mcPos), Rotations.getPitch(mcPos), () -> BlockUtils.breakBlock(mcPos, true));
                    } else {
                        BlockUtils.breakBlock(mcPos, true);
                    }
                    breaking = true;
                    b.breakTimer = b.breakDelay.get();
                    if (!b.lastBreakingPos.equals(pos2)) {
                        b.lastBreakingPos.set(pos2);
                        ++b.blocksBroken;
                    }
                    ++b.count;
                    if (!multiBreak) break;
                }
                if (it.hasNext() || !BlockUtils.canInstaBreak(mcPos)) continue;
                finishedBreaking = true;
            }
            if (finishedBreaking || !breaking) {
                b.setState(nextState, lastState);
            }
        }

        private void doubleMine(HighwayBuilder b, ArrayDeque<BlockPos> blocks) {
            DoubleMineBlock block;
            if (b.breakTimer > 0) {
                return;
            }
            if (b.normalMining == null) {
                block = new DoubleMineBlock(b, blocks.pop());
                b.normalMining = block.startDestroying();
                b.breakTimer = b.breakDelay.get();
                if (b.breakTimer > 0) {
                    return;
                }
            }
            if (DoubleMineBlock.rateLimited) {
                return;
            }
            if (b.packetMining == null && !blocks.isEmpty() && (block = new DoubleMineBlock(b, blocks.pop())) != null) {
                b.packetMining = b.normalMining.packetMine();
                b.normalMining = block.startDestroying();
                b.breakTimer = b.breakDelay.get();
            }
        }

        protected void place(HighwayBuilder b, MBPIterator it, int slot, State nextState) {
            boolean placed = false;
            boolean finishedPlacing = false;
            for (MBlockPos pos : it) {
                if (b.count >= it.placementsPerTick(b)) {
                    return;
                }
                if (b.placeTimer > 0) {
                    return;
                }
                if (pos.getBlockPos().distToCenterSqr((Position)((HighwayBuilder)b).mc.player.getEyePosition()) > b.placeRange.get() * b.placeRange.get()) continue;
                if (BlockUtils.place(pos.getBlockPos(), InteractionHand.MAIN_HAND, slot, b.rotation.get().place, 0, true, true, true)) {
                    placed = true;
                    ++b.blocksPlaced;
                    b.placeTimer = b.placeDelay.get();
                    ++b.count;
                    if (b.placementsPerTick.get() == 1) break;
                }
                if (it.hasNext()) continue;
                finishedPlacing = true;
            }
            if (finishedPlacing || !placed) {
                b.setState(nextState);
            }
        }

        private int findSlot(HighwayBuilder b, Predicate<ItemStack> predicate, boolean hotbar) {
            for (int i = hotbar ? 0 : 9; i < (hotbar ? 9 : ((HighwayBuilder)b).mc.player.getInventory().getNonEquipmentItems().size()); ++i) {
                if (!predicate.test(((HighwayBuilder)b).mc.player.getInventory().getItem(i))) continue;
                return i;
            }
            return -1;
        }

        protected int findHotbarSlot(HighwayBuilder b, boolean replaceTools) {
            int thrashSlot = -1;
            int slotsWithBlocks = 0;
            int slotWithLeastBlocks = -1;
            int slotWithLeastBlocksCount = Integer.MAX_VALUE;
            for (int i = 0; i < 9; ++i) {
                Item item;
                ItemStack itemStack = ((HighwayBuilder)b).mc.player.getInventory().getItem(i);
                if (itemStack.isEmpty()) {
                    return i;
                }
                if (replaceTools && AutoTool.isTool(itemStack)) {
                    return i;
                }
                if (b.trashItems.get().contains(itemStack.getItem())) {
                    thrashSlot = i;
                }
                if (!((item = itemStack.getItem()) instanceof BlockItem)) continue;
                BlockItem blockItem = (BlockItem)item;
                if (!b.blocksToPlace.get().contains(blockItem.getBlock()) && (!b.blocksToPlace.get().contains(Blocks.OBSIDIAN) || blockItem != Items.ENDER_CHEST)) continue;
                ++slotsWithBlocks;
                if (itemStack.getCount() >= slotWithLeastBlocksCount) continue;
                slotWithLeastBlocksCount = itemStack.getCount();
                slotWithLeastBlocks = i;
            }
            if (thrashSlot != -1) {
                return thrashSlot;
            }
            if (slotsWithBlocks > 0) {
                return slotWithLeastBlocks;
            }
            b.error("No empty space in hotbar.", new Object[0]);
            return -1;
        }

        protected boolean hasItem(HighwayBuilder b, Predicate<ItemStack> predicate) {
            for (int i = 0; i < ((HighwayBuilder)b).mc.player.getInventory().getNonEquipmentItems().size(); ++i) {
                if (!predicate.test(((HighwayBuilder)b).mc.player.getInventory().getItem(i))) continue;
                return true;
            }
            return false;
        }

        protected int countItem(HighwayBuilder b, Predicate<ItemStack> predicate) {
            int count = 0;
            for (int i = 0; i < ((HighwayBuilder)b).mc.player.getInventory().getNonEquipmentItems().size(); ++i) {
                ItemStack stack = ((HighwayBuilder)b).mc.player.getInventory().getItem(i);
                if (!predicate.test(stack)) continue;
                count += stack.getCount();
            }
            return count;
        }

        protected int findAndMoveToHotbar(HighwayBuilder b, Predicate<ItemStack> predicate) {
            int slot = this.findSlot(b, predicate, true);
            if (slot != -1) {
                return slot;
            }
            int hotbarSlot = this.findHotbarSlot(b, false);
            if (hotbarSlot == -1) {
                return -1;
            }
            slot = this.findSlot(b, predicate, false);
            if (slot == -1) {
                return -1;
            }
            InvUtils.move().from(slot).toHotbar(hotbarSlot);
            InvUtils.dropHand();
            return hotbarSlot;
        }

        protected int findAndMoveBestToolToHotbar(HighwayBuilder b, BlockState blockState, boolean noSilkTouch) {
            int count;
            if (((HighwayBuilder)b).mc.player.isCreative()) {
                return ((HighwayBuilder)b).mc.player.getInventory().getSelectedSlot();
            }
            double bestScore = -1.0;
            int bestSlot = -1;
            for (int i = 0; i < ((HighwayBuilder)b).mc.player.getInventory().getNonEquipmentItems().size(); ++i) {
                double score = AutoTool.getScore(((HighwayBuilder)b).mc.player.getInventory().getItem(i), blockState, false, false, AutoTool.EnchantPreference.None, itemStack -> {
                    if (noSilkTouch && Utils.hasEnchantment(itemStack, (ResourceKey<Enchantment>)Enchantments.SILK_TOUCH)) {
                        return false;
                    }
                    return b.dontBreakTools.get() == false || itemStack.getMaxDamage() - itemStack.getDamageValue() > itemStack.getMaxDamage() * (b.breakDurability.get() / 100);
                });
                if (!(score > bestScore)) continue;
                bestScore = score;
                bestSlot = i;
            }
            if (bestSlot == -1) {
                return ((HighwayBuilder)b).mc.player.getInventory().getSelectedSlot();
            }
            ItemStack bestStack = ((HighwayBuilder)b).mc.player.getInventory().getItem(bestSlot);
            if (bestStack.is(ItemTags.PICKAXES) && (count = this.countItem(b, stack -> stack.is(ItemTags.PICKAXES))) <= b.savePickaxes.get() && (!b.restockTask.pickaxes || bestStack.getMaxDamage() - bestStack.getDamageValue() <= bestStack.getMaxDamage() * (b.breakDurability.get() / 100))) {
                if (!b.restockTask.pickaxes && (b.searchEnderChest.get().booleanValue() || b.searchShulkers.get().booleanValue())) {
                    b.restockTask.setPickaxes();
                } else {
                    b.error("Found less than the minimum amount of pickaxes required: " + count + "/" + (b.savePickaxes.get() + 1), new Object[0]);
                }
                return -1;
            }
            if (bestSlot < 9) {
                return bestSlot;
            }
            int hotbarSlot = this.findHotbarSlot(b, true);
            if (hotbarSlot == -1) {
                return -1;
            }
            InvUtils.move().from(bestSlot).toHotbar(hotbarSlot);
            InvUtils.dropHand();
            return hotbarSlot;
        }

        protected int findBlocksToPlace(HighwayBuilder b) {
            int slot = this.findAndMoveToHotbar(b, itemStack -> {
                Item patt0$temp = itemStack.getItem();
                if (!(patt0$temp instanceof BlockItem)) return false;
                BlockItem blockItem = (BlockItem)patt0$temp;
                if (!b.blocksToPlace.get().contains(blockItem.getBlock())) return false;
                return true;
            });
            if (slot == -1) {
                if (b.mineEnderChests.get().booleanValue() && b.blocksToPlace.get().contains(Blocks.OBSIDIAN) && this.countItem(b, stack -> stack.getItem().equals(Items.ENDER_CHEST)) > b.saveEchests.get()) {
                    b.setState(MineEnderChests);
                } else if (b.searchEnderChest.get().booleanValue() || b.searchShulkers.get().booleanValue()) {
                    b.restockTask.setMaterials();
                } else {
                    b.error("Out of blocks to place.", new Object[0]);
                }
                return -1;
            }
            return slot;
        }

        protected int findBlocksToPlacePrioritizeTrash(HighwayBuilder b) {
            int slot = this.findAndMoveToHotbar(b, itemStack -> {
                if (!(itemStack.getItem() instanceof BlockItem)) {
                    return false;
                }
                return b.trashItems.get().contains(itemStack.getItem());
            });
            return slot != -1 ? slot : this.findBlocksToPlace(b);
        }
    }

    public static class DoubleMineBlock {
        public static boolean rateLimited = false;
        public final BlockPos blockPos;
        public final BlockState blockState;
        private final Block block;
        private final Direction direction;
        private final HighwayBuilder b;
        private final Vector3d vec3 = new Vector3d(0.0);
        private int normalStartTime;
        private int packetStartTime;
        private boolean packet;

        public DoubleMineBlock(HighwayBuilder b, BlockPos pos) {
            this.b = b;
            this.blockPos = pos;
            this.blockState = ((HighwayBuilder)b).mc.level.getBlockState(this.blockPos);
            this.block = this.blockState.getBlock();
            this.direction = BlockUtils.getDirection(pos);
            this.packet = false;
        }

        public DoubleMineBlock startDestroying() {
            ((HighwayBuilder)this.b).mc.gameMode.startPrediction(((HighwayBuilder)this.b).mc.level, sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.blockPos, this.direction, sequence));
            this.normalStartTime = ((HighwayBuilder)this.b).mc.player.tickCount;
            return this;
        }

        public DoubleMineBlock stopDestroying() {
            ((HighwayBuilder)this.b).mc.gameMode.startPrediction(((HighwayBuilder)this.b).mc.level, sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.blockPos, this.direction, sequence));
            return this;
        }

        public DoubleMineBlock packetMine() {
            this.packetStartTime = ((HighwayBuilder)this.b).mc.player.tickCount;
            this.packet = true;
            return this.stopDestroying();
        }

        public boolean isReady() {
            return this.progress() >= (this.b.fastBreak.get() != false ? 0.7 : 1.0);
        }

        public boolean shouldRemove() {
            boolean distance;
            boolean bl = distance = !this.packet && Utils.distance(((HighwayBuilder)this.b).mc.player.getEyePosition().x, ((HighwayBuilder)this.b).mc.player.getEyePosition().y, ((HighwayBuilder)this.b).mc.player.getEyePosition().z, this.blockPos.getX() + this.direction.getStepX(), this.blockPos.getY() + this.direction.getStepY(), this.blockPos.getZ() + this.direction.getStepZ()) > ((HighwayBuilder)this.b).mc.player.blockInteractionRange();
            boolean timeout = this.progress() > 2.0 && ((HighwayBuilder)this.b).mc.player.tickCount - (this.packet ? this.packetStartTime : this.normalStartTime) > 60;
            return distance || timeout;
        }

        public double progress() {
            int slot = ((HighwayBuilder)this.b).mc.player.getInventory().getSelectedSlot();
            return BlockUtils.getBreakDelta(slot, this.blockState) * (double)(((HighwayBuilder)this.b).mc.player.tickCount - (this.packet ? this.packetStartTime : this.normalStartTime) + 1);
        }

        public void renderLetter() {
            this.vec3.set((double)this.blockPos.getX() + 0.5, (double)this.blockPos.getY() + 0.5, (double)this.blockPos.getZ() + 0.5);
            if (!NametagUtils.to2D(this.vec3, 2.0)) {
                return;
            }
            NametagUtils.begin(this.vec3);
            TextRenderer.get().begin(1.0, false, true);
            String letter = this.packet ? "P" : "N";
            double w = TextRenderer.get().getWidth(letter) / 2.0;
            TextRenderer.get().render(letter, -w, 0.0, Color.WHITE, true);
            TextRenderer.get().end();
            NametagUtils.end();
        }
    }

    private static interface MBPIterator
    extends Iterator<MBlockPos>,
    Iterable<MBlockPos> {
        public void save();

        public void restore();

        @Override
        @NotNull
        default public Iterator<MBlockPos> iterator() {
            return this;
        }

        default public int placementsPerTick(HighwayBuilder b) {
            return b.placementsPerTick.get();
        }
    }

    private static class MBPIteratorFilter
    implements MBPIterator {
        private final MBPIterator it;
        private final Predicate<MBlockPos> predicate;
        private MBlockPos pos;
        private boolean isOld = true;
        private boolean pisOld = true;

        public MBPIteratorFilter(MBPIterator it, Predicate<MBlockPos> predicate) {
            this.it = it;
            this.predicate = predicate;
        }

        @Override
        public void save() {
            this.it.save();
            this.pisOld = this.isOld;
            this.isOld = true;
        }

        @Override
        public void restore() {
            this.it.restore();
            this.isOld = this.pisOld;
        }

        @Override
        public boolean hasNext() {
            if (this.isOld) {
                this.isOld = false;
                this.pos = null;
                while (this.it.hasNext()) {
                    this.pos = (MBlockPos)this.it.next();
                    if (this.predicate.test(this.pos)) {
                        return true;
                    }
                    this.pos = null;
                }
            }
            return this.pos != null && this.predicate.test(this.pos);
        }

        @Override
        public MBlockPos next() {
            this.isOld = true;
            return this.pos;
        }
    }
}
