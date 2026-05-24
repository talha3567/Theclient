package meteordevelopment.meteorclient.systems.modules.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.BreakIndicators;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PacketMine
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<Integer> delay;
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> autoSwitch;
    private final Setting<Boolean> notOnUse;
    private final Setting<Boolean> obscureBreakingProgress;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> readySideColor;
    private final Setting<SettingColor> readyLineColor;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Pool<MyBlock> blockPool;
    public final List<MyBlock> blocks;
    private boolean swapped;
    private boolean shouldUpdateSlot;

    public PacketMine() {
        super(Categories.World, "packet-mine", "Sends packets to mine blocks without the mining animation.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.delay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("delay")).description("Delay between mining blocks in ticks.")).defaultValue(1)).min(0).build());
        this.rotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Sends rotation packets to the server when mining.")).defaultValue(true)).build());
        this.autoSwitch = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-switch")).description("Automatically switches to the best tool when the block is ready to be mined instantly.")).defaultValue(false)).build());
        this.notOnUse = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("not-on-use")).description("Won't auto switch if you're using an item.")).defaultValue(true)).visible(this.autoSwitch::get)).build());
        this.obscureBreakingProgress = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("obscure-breaking-progress")).description("Spams abort breaking packets to obscure the block mining progress from other players. Does not hide it perfectly.")).defaultValue(false)).build());
        this.render = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render")).description("Whether or not to render the block being mined.")).defaultValue(true)).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.readySideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("ready-side-color")).description("The color of the sides of the blocks that can be broken.")).defaultValue(new SettingColor(0, 204, 0, 10)).build());
        this.readyLineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("ready-line-color")).description("The color of the lines of the blocks that can be broken.")).defaultValue(new SettingColor(0, 204, 0, 255)).build());
        this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The color of the sides of the blocks being rendered.")).defaultValue(new SettingColor(204, 0, 0, 10)).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The color of the lines of the blocks being rendered.")).defaultValue(new SettingColor(204, 0, 0, 255)).build());
        this.blockPool = new Pool<MyBlock>(() -> new MyBlock(this));
        this.blocks = new ArrayList<MyBlock>();
    }

    @Override
    public void onActivate() {
        this.swapped = false;
    }

    @Override
    public void onDeactivate() {
        this.blockPool.freeAll(this.blocks);
        this.blocks.clear();
        if (this.shouldUpdateSlot) {
            this.mc.player.connection.send((Packet)new ServerboundSetCarriedItemPacket(this.mc.player.getInventory().getSelectedSlot()));
            this.shouldUpdateSlot = false;
        }
    }

    @EventHandler
    private void onStartBreakingBlock(StartBreakingBlockEvent event) {
        if (!BlockUtils.canBreak(event.blockPos)) {
            return;
        }
        event.cancel();
        this.swapped = false;
        if (!this.isMiningBlock(event.blockPos)) {
            this.blocks.add(this.blockPool.get().set(event));
        }
    }

    public boolean isMiningBlock(BlockPos pos) {
        for (MyBlock block : this.blocks) {
            if (!block.blockPos.equals((Object)pos)) continue;
            return true;
        }
        return false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        this.blocks.removeIf(MyBlock::shouldRemove);
        if (this.shouldUpdateSlot) {
            this.mc.player.connection.send((Packet)new ServerboundSetCarriedItemPacket(this.mc.player.getInventory().getSelectedSlot()));
            this.shouldUpdateSlot = false;
            this.swapped = false;
        }
        if (!this.blocks.isEmpty()) {
            MyBlock block = this.blocks.getFirst();
            block.mine();
            if (!(!block.isReady() || this.swapped || !this.autoSwitch.get().booleanValue() || this.mc.player.isUsingItem() && this.notOnUse.get().booleanValue())) {
                FindItemResult slot = InvUtils.findFastestTool(block.blockState);
                if (!slot.found() || this.mc.player.getInventory().getSelectedSlot() == slot.slot()) {
                    return;
                }
                this.mc.player.connection.send((Packet)new ServerboundSetCarriedItemPacket(slot.slot()));
                this.swapped = true;
                this.shouldUpdateSlot = true;
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!this.render.get().booleanValue()) {
            return;
        }
        for (MyBlock block : this.blocks) {
            if (Modules.get().get(BreakIndicators.class).isActive() && Modules.get().get(BreakIndicators.class).packetMine.get().booleanValue() && block.mining) continue;
            block.render(event);
        }
    }

    public class MyBlock {
        public BlockPos blockPos;
        public BlockState blockState;
        public Block block;
        public Direction direction;
        public int timer;
        public int startTime;
        public boolean mining;
        final /* synthetic */ PacketMine this$0;

        public MyBlock(PacketMine this$0) {
            PacketMine packetMine = this$0;
            Objects.requireNonNull(packetMine);
            this.this$0 = packetMine;
        }

        public MyBlock set(StartBreakingBlockEvent event) {
            this.blockPos = event.blockPos;
            this.direction = event.direction;
            this.blockState = ((PacketMine)this.this$0).mc.level.getBlockState(this.blockPos);
            this.block = this.blockState.getBlock();
            this.timer = this.this$0.delay.get();
            this.mining = false;
            return this;
        }

        public boolean shouldRemove() {
            boolean broken = ((PacketMine)this.this$0).mc.level.getBlockState(this.blockPos).getBlock() != this.block;
            boolean timeout = this.progress() > 2.0 && ((PacketMine)this.this$0).mc.player.tickCount - this.startTime > 50;
            boolean distance = Utils.distance(((PacketMine)this.this$0).mc.player.getEyePosition().x, ((PacketMine)this.this$0).mc.player.getEyePosition().y, ((PacketMine)this.this$0).mc.player.getEyePosition().z, this.blockPos.getX() + this.direction.getStepX(), this.blockPos.getY() + this.direction.getStepY(), this.blockPos.getZ() + this.direction.getStepZ()) > ((PacketMine)this.this$0).mc.player.blockInteractionRange();
            return broken || timeout || distance;
        }

        public boolean isReady() {
            return this.progress() >= 1.0;
        }

        public double progress() {
            if (!this.mining) {
                return 0.0;
            }
            FindItemResult fir = InvUtils.findFastestTool(this.blockState);
            return BlockUtils.getBreakDelta(fir.found() ? fir.slot() : ((PacketMine)this.this$0).mc.player.getInventory().getSelectedSlot(), this.blockState) * (double)(((PacketMine)this.this$0).mc.player.tickCount - this.startTime + 1);
        }

        public void mine() {
            if (this.this$0.rotate.get().booleanValue()) {
                Rotations.rotate(Rotations.getYaw(this.blockPos), Rotations.getPitch(this.blockPos), 50, this::sendMinePackets);
            } else {
                this.sendMinePackets();
            }
        }

        private void sendMinePackets() {
            if (this.timer <= 0) {
                if (!this.mining) {
                    ((PacketMine)this.this$0).mc.gameMode.startPrediction(((PacketMine)this.this$0).mc.level, sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.blockPos, this.direction, sequence));
                    ((PacketMine)this.this$0).mc.gameMode.startPrediction(((PacketMine)this.this$0).mc.level, sequence -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.blockPos, this.direction, sequence));
                    this.mining = true;
                    this.startTime = ((PacketMine)this.this$0).mc.player.tickCount;
                }
            } else {
                --this.timer;
            }
            if (this.mining && this.this$0.obscureBreakingProgress.get().booleanValue()) {
                this.this$0.mc.getConnection().send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.blockPos, this.direction));
            }
        }

        public void render(Render3DEvent event) {
            VoxelShape shape = ((PacketMine)this.this$0).mc.level.getBlockState(this.blockPos).getShape((BlockGetter)((PacketMine)this.this$0).mc.level, this.blockPos);
            double x1 = this.blockPos.getX();
            double y1 = this.blockPos.getY();
            double z1 = this.blockPos.getZ();
            double x2 = this.blockPos.getX() + 1;
            double y2 = this.blockPos.getY() + 1;
            double z2 = this.blockPos.getZ() + 1;
            if (!shape.isEmpty()) {
                x1 = (double)this.blockPos.getX() + shape.min(Direction.Axis.X);
                y1 = (double)this.blockPos.getY() + shape.min(Direction.Axis.Y);
                z1 = (double)this.blockPos.getZ() + shape.min(Direction.Axis.Z);
                x2 = (double)this.blockPos.getX() + shape.max(Direction.Axis.X);
                y2 = (double)this.blockPos.getY() + shape.max(Direction.Axis.Y);
                z2 = (double)this.blockPos.getZ() + shape.max(Direction.Axis.Z);
            }
            if (this.isReady()) {
                event.renderer.box(x1, y1, z1, x2, y2, z2, this.this$0.readySideColor.get(), this.this$0.readyLineColor.get(), this.this$0.shapeMode.get(), 0);
            } else {
                event.renderer.box(x1, y1, z1, x2, y2, z2, this.this$0.sideColor.get(), this.this$0.lineColor.get(), this.this$0.shapeMode.get(), 0);
            }
        }
    }
}
