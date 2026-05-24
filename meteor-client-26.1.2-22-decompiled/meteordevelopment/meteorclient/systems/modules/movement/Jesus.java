package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.entity.player.CanWalkOnFluidEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.CollisionShapeEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.ServerboundMovePlayerPacketAccessor;
import meteordevelopment.meteorclient.mixininterface.IVec3;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Flight;
import meteordevelopment.meteorclient.systems.modules.movement.speed.modes.Strafe;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import org.joml.Vector2d;

public class Jesus
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgWater;
    private final SettingGroup sgLava;
    private final Setting<Boolean> powderSnow;
    private final Setting<Boolean> ncpBypass;
    private final Setting<Boolean> slowDown;
    private final Setting<Mode> waterMode;
    private final Setting<Boolean> dipIfBurning;
    private final Setting<Boolean> dipOnSneakWater;
    private final Setting<Boolean> dipOnFallWater;
    private final Setting<Integer> dipFallHeightWater;
    private final Setting<Mode> lavaMode;
    private final Setting<Boolean> dipIfFireResistant;
    private final Setting<Boolean> dipOnSneakLava;
    private final Setting<Boolean> dipOnFallLava;
    private final Setting<Integer> dipFallHeightLava;
    private int ascending;
    private int swimmingTicks;
    private boolean prePathManagerWalkOnWater;
    private boolean prePathManagerWalkOnLava;
    public boolean isInBubbleColumn;

    public Jesus() {
        super(Categories.Movement, "jesus", "Walk on liquids and powder snow like Jesus.");
        this.sgGeneral = this.settings.createGroup("General");
        this.sgWater = this.settings.createGroup("Water");
        this.sgLava = this.settings.createGroup("Lava");
        this.powderSnow = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("powder-snow")).description("Walk on powder snow.")).defaultValue(true)).build());
        this.ncpBypass = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ncp-bypass")).description("Whether to apply a bypass for NCP.")).defaultValue(false)).build());
        this.slowDown = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("slow-down")).description("Further movement option to try bypassing NCP")).defaultValue(false)).visible(this.ncpBypass::get)).build());
        this.waterMode = this.sgWater.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("How to treat the water.")).defaultValue(Mode.Solid)).build());
        this.dipIfBurning = this.sgWater.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("dip-if-burning")).description("Lets you go into the water when you are burning.")).defaultValue(true)).visible(() -> this.waterMode.get() == Mode.Solid)).build());
        this.dipOnSneakWater = this.sgWater.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("dip-on-sneak")).description("Lets you go into the water when your sneak key is held.")).defaultValue(true)).visible(() -> this.waterMode.get() == Mode.Solid)).build());
        this.dipOnFallWater = this.sgWater.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("dip-on-fall")).description("Lets you go into the water when you fall over a certain height.")).defaultValue(true)).visible(() -> this.waterMode.get() == Mode.Solid)).build());
        this.dipFallHeightWater = this.sgWater.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("dip-fall-height")).description("The fall height at which you will go into the water.")).defaultValue(4)).range(1, 255).sliderRange(3, 20).visible(() -> this.waterMode.get() == Mode.Solid && this.dipOnFallWater.get() != false)).build());
        this.lavaMode = this.sgLava.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("How to treat the lava.")).defaultValue(Mode.Solid)).build());
        this.dipIfFireResistant = this.sgLava.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("dip-if-resistant")).description("Lets you go into the lava if you have Fire Resistance effect.")).defaultValue(true)).visible(() -> this.lavaMode.get() == Mode.Solid)).build());
        this.dipOnSneakLava = this.sgLava.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("dip-on-sneak")).description("Lets you go into the lava when your sneak key is held.")).defaultValue(true)).visible(() -> this.lavaMode.get() == Mode.Solid)).build());
        this.dipOnFallLava = this.sgLava.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("dip-on-fall")).description("Lets you go into the lava when you fall over a certain height.")).defaultValue(true)).visible(() -> this.lavaMode.get() == Mode.Solid)).build());
        this.dipFallHeightLava = this.sgLava.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("dip-fall-height")).description("The fall height at which you will go into the lava.")).defaultValue(4)).range(1, 255).sliderRange(3, 20).visible(() -> this.lavaMode.get() == Mode.Solid && this.dipOnFallLava.get() != false)).build());
        this.ascending = 10;
        this.swimmingTicks = 0;
        this.isInBubbleColumn = false;
    }

    @Override
    public void onActivate() {
        this.prePathManagerWalkOnWater = PathManagers.get().getSettings().getWalkOnWater().get();
        this.prePathManagerWalkOnLava = PathManagers.get().getSettings().getWalkOnLava().get();
        PathManagers.get().getSettings().getWalkOnWater().set(this.waterMode.get() == Mode.Solid);
        PathManagers.get().getSettings().getWalkOnLava().set(this.lavaMode.get() == Mode.Solid);
    }

    @Override
    public void onDeactivate() {
        PathManagers.get().getSettings().getWalkOnWater().set(this.prePathManagerWalkOnWater);
        PathManagers.get().getSettings().getWalkOnLava().set(this.prePathManagerWalkOnLava);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        LocalPlayer movingEntity;
        boolean bubbleColumn = this.isInBubbleColumn;
        this.isInBubbleColumn = false;
        if (this.mc.player.isVisuallySwimming()) {
            return;
        }
        if (this.mc.player.isInWater() && !this.waterShouldBeSolid()) {
            return;
        }
        if (this.mc.player.isInLava() && !this.lavaShouldBeSolid()) {
            return;
        }
        Object object = movingEntity = this.mc.player.isPassenger() ? this.mc.player.getVehicle() : this.mc.player;
        if (bubbleColumn) {
            if (this.mc.options.keyJump.isDown() && movingEntity.getDeltaMovement().y() < 0.11) {
                ((IVec3)movingEntity.getDeltaMovement()).meteor$setY(0.11);
            }
            return;
        }
        if (movingEntity.isInWater() || movingEntity.isInLava()) {
            ((IVec3)movingEntity.getDeltaMovement()).meteor$setY(0.11);
            this.ascending = 0;
            return;
        }
        BlockState blockBelowState = this.mc.level.getBlockState(movingEntity.blockPosition().below());
        boolean waterLogged = (Boolean)blockBelowState.getValueOrElse((Property)BlockStateProperties.WATERLOGGED, (Comparable)Boolean.valueOf(false));
        if (this.ascending == 0) {
            ((IVec3)movingEntity.getDeltaMovement()).meteor$setY(0.11);
        } else if (this.ascending == 1 && (blockBelowState.getBlock() == Blocks.WATER || blockBelowState.getBlock() == Blocks.LAVA || waterLogged)) {
            ((IVec3)movingEntity.getDeltaMovement()).meteor$setY(0.0);
        }
        ++this.ascending;
    }

    @EventHandler
    private void onCanWalkOnFluid(CanWalkOnFluidEvent event) {
        if (this.mc.player != null && this.mc.player.isSwimming()) {
            return;
        }
        if ((event.fluidState.getType() == Fluids.WATER || event.fluidState.getType() == Fluids.FLOWING_WATER) && this.waterShouldBeSolid()) {
            event.walkOnFluid = true;
        } else if ((event.fluidState.getType() == Fluids.LAVA || event.fluidState.getType() == Fluids.FLOWING_LAVA) && this.lavaShouldBeSolid()) {
            event.walkOnFluid = true;
        }
    }

    @EventHandler
    private void onFluidCollisionShape(CollisionShapeEvent event) {
        if (event.state.getFluidState().isEmpty()) {
            return;
        }
        if ((event.state.getBlock() == Blocks.WATER || event.state.getFluidState().getType() == Fluids.WATER) && !this.mc.player.isInWater() && this.waterShouldBeSolid() && (double)event.pos.getY() <= this.mc.player.getY() - 1.0) {
            event.shape = Shapes.block();
        } else if (event.state.getBlock() == Blocks.LAVA && !this.mc.player.isInLava() && this.lavaShouldBeSolid() && (this.isLavaDangerous() || (double)event.pos.getY() <= this.mc.player.getY() - 1.0)) {
            event.shape = Shapes.block();
        }
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        boolean shouldWork;
        Packet<?> packet = event.packet;
        if (!(packet instanceof ServerboundMovePlayerPacket)) {
            return;
        }
        ServerboundMovePlayerPacket packet2 = (ServerboundMovePlayerPacket)packet;
        if (this.mc.player.isInWater() && !this.waterShouldBeSolid()) {
            return;
        }
        if (this.mc.player.isInLava() && !this.lavaShouldBeSolid()) {
            return;
        }
        if (!this.ncpBypass.get().booleanValue()) {
            return;
        }
        Tuple<Boolean, Boolean> overLiquid = this.isOverLiquid();
        boolean bl = shouldWork = (Boolean)overLiquid.getA() != false && this.waterShouldBeSolid() || (Boolean)overLiquid.getB() != false && this.lavaShouldBeSolid();
        if (this.mc.player.isInWater() || this.mc.player.isInLava() || this.mc.player.fallDistance > 3.0 || !shouldWork) {
            return;
        }
        ((ServerboundMovePlayerPacketAccessor)packet2).meteor$setOnGround(false);
        if (!this.mc.player.onGround() || !packet2.hasPosition()) {
            return;
        }
        ((ServerboundMovePlayerPacketAccessor)packet2).meteor$setY(packet2.getY(0.0) - (0.02 + 1.0E-4 * (double)this.swimmingTicks));
    }

    @EventHandler
    private void onMoveEvent(PlayerMoveEvent event) {
        boolean lava;
        if (!this.ncpBypass.get().booleanValue()) {
            return;
        }
        Tuple<Boolean, Boolean> overLiquid = this.isOverLiquid();
        boolean water = (Boolean)overLiquid.getA() != false && this.waterShouldBeSolid();
        boolean bl = lava = (Boolean)overLiquid.getB() != false && this.lavaShouldBeSolid();
        if (!water && !lava) {
            this.swimmingTicks = 0;
            return;
        }
        if (++this.swimmingTicks < 15) {
            if (this.mc.player.onGround()) {
                Vector2d vel = Strafe.transformStrafe(PlayerUtils.isMoving() ? 0.2873 : 0.0);
                ((IVec3)event.movement).meteor$setXZ(vel.x, vel.y);
            }
            return;
        }
        this.swimmingTicks = 0;
        if (this.slowDown.get().booleanValue()) {
            ((IVec3)event.movement).meteor$setXZ(0.0, 0.0);
        }
        ((IVec3)this.mc.player.getDeltaMovement()).meteor$setY(0.08);
    }

    private boolean waterShouldBeSolid() {
        if (EntityUtils.getGameMode((Player)this.mc.player) == GameType.SPECTATOR || this.mc.player.getAbilities().flying) {
            return false;
        }
        if (this.mc.player.getVehicle() != null && this.mc.player.getVehicle() instanceof AbstractBoat) {
            return false;
        }
        if (Modules.get().get(Flight.class).isActive()) {
            return false;
        }
        if (this.dipIfBurning.get().booleanValue() && this.mc.player.isOnFire()) {
            return false;
        }
        if (this.dipOnSneakWater.get().booleanValue() && this.mc.options.keyShift.isDown()) {
            return false;
        }
        if (this.dipOnFallWater.get().booleanValue() && this.mc.player.fallDistance > (double)this.dipFallHeightWater.get().intValue()) {
            return false;
        }
        return this.waterMode.get() == Mode.Solid;
    }

    private boolean lavaShouldBeSolid() {
        if (EntityUtils.getGameMode((Player)this.mc.player) == GameType.SPECTATOR || this.mc.player.getAbilities().flying) {
            return false;
        }
        if (this.mc.player.getVehicle() != null && this.mc.player.getVehicle() instanceof Strider) {
            return false;
        }
        if (this.isLavaDangerous() && this.lavaMode.get() == Mode.Solid) {
            return true;
        }
        if (this.dipOnSneakLava.get().booleanValue() && this.mc.options.keyShift.isDown()) {
            return false;
        }
        if (this.dipOnFallLava.get().booleanValue() && this.mc.player.fallDistance > (double)this.dipFallHeightLava.get().intValue()) {
            return false;
        }
        return this.lavaMode.get() == Mode.Solid;
    }

    private boolean isLavaDangerous() {
        if (!this.dipIfFireResistant.get().booleanValue()) {
            return true;
        }
        return !this.mc.player.hasEffect(MobEffects.FIRE_RESISTANCE) || (double)this.mc.player.getEffect(MobEffects.FIRE_RESISTANCE).getDuration() <= 300.0 * this.mc.player.getAttributeValue(Attributes.BURNING_TIME);
    }

    private Tuple<Boolean, Boolean> isOverLiquid() {
        AABB box = this.mc.player.isPassenger() ? this.mc.player.getBoundingBox().minmax(this.mc.player.getVehicle().getBoundingBox()) : this.mc.player.getBoundingBox();
        BlockState[] states = (BlockState[])this.mc.level.getBlockStatesIfLoaded(box.move(0.0, -0.01, 0.0)).toArray(BlockState[]::new);
        boolean water = false;
        boolean lava = false;
        boolean foundSolid = false;
        for (BlockState state : states) {
            if (state.getBlock() == Blocks.WATER || state.getFluidState().getType() == Fluids.WATER) {
                water = true;
                continue;
            }
            if (state.getBlock() == Blocks.LAVA) {
                lava = true;
                continue;
            }
            if (state.isAir()) continue;
            foundSolid = true;
            break;
        }
        return new Tuple((Object)(water && !foundSolid ? 1 : 0), (Object)(lava && !foundSolid ? 1 : 0));
    }

    public boolean canWalkOnPowderSnow() {
        return this.isActive() && this.powderSnow.get() != false;
    }

    public static enum Mode {
        Solid,
        Ignore;

    }
}
