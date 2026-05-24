package meteordevelopment.meteorclient.systems.modules.world;

import java.util.Set;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.HorizontalDirection;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class Flamethrower
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> distance;
    private final Setting<Boolean> antiBreak;
    private final Setting<Boolean> putOutFire;
    private final Setting<Boolean> targetBabies;
    private final Setting<Integer> tickInterval;
    private final Setting<Boolean> rotate;
    private final Setting<Set<EntityType<?>>> entities;
    private Entity entity;
    private int ticks;
    private InteractionHand hand;

    public Flamethrower() {
        super(Categories.World, "flamethrower", "Ignites every alive piece of food.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.distance = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("distance")).description("The maximum distance the animal has to be to be roasted.")).min(0.0).defaultValue(5.0).build());
        this.antiBreak = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("anti-break")).description("Prevents flint and steel from being broken.")).defaultValue(false)).build());
        this.putOutFire = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("put-out-fire")).description("Tries to put out the fire when animal is low health, so the items don't burn.")).defaultValue(true)).build());
        this.targetBabies = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("target-babies")).description("If checked babies will also be killed.")).defaultValue(false)).build());
        this.tickInterval = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("tick-interval")).defaultValue(5)).build());
        this.rotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Automatically faces towards the animal roasted.")).defaultValue(true)).build());
        this.entities = this.sgGeneral.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entities")).description("Entities to cook.")).defaultValue(EntityType.PIG, EntityType.COW, EntityType.SHEEP, EntityType.CHICKEN, EntityType.RABBIT).build());
        this.ticks = 0;
    }

    @Override
    public void onDeactivate() {
        this.entity = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        this.entity = null;
        ++this.ticks;
        for (Entity entity : this.mc.level.entitiesForRendering()) {
            LivingEntity livingEntity;
            if (!this.entities.get().contains(entity.getType()) || !PlayerUtils.isWithin(entity, (double)this.distance.get()) || entity == this.mc.player || !entity.isAlive() || entity.isInPowderSnow || entity.isInWaterOrRain() || entity.fireImmune() || !this.targetBabies.get().booleanValue() && entity instanceof LivingEntity && (livingEntity = (LivingEntity)entity).isBaby()) continue;
            FindItemResult item = InvUtils.findInHotbar(itemStack -> !(!itemStack.is((Object)Items.FLINT_AND_STEEL) && !itemStack.is((Object)Items.FIRE_CHARGE) || itemStack.isDamageableItem() && this.antiBreak.get() != false && itemStack.getDamageValue() >= itemStack.getMaxDamage() - 1));
            if (!InvUtils.swap(item.slot(), true)) {
                return;
            }
            this.hand = item.getHand();
            this.entity = entity;
            if (this.rotate.get().booleanValue()) {
                Rotations.rotate(Rotations.getYaw(entity.blockPosition()), Rotations.getPitch(entity.blockPosition()), -100, this::interact);
            } else {
                this.interact();
            }
            return;
        }
    }

    private void interact() {
        LivingEntity animal;
        HorizontalDirection[] horizontalDirectionArray;
        Block block = this.mc.level.getBlockState(this.entity.blockPosition()).getBlock();
        Block bottom = this.mc.level.getBlockState(this.entity.blockPosition().below()).getBlock();
        if (block == Blocks.WATER || bottom == Blocks.WATER || bottom == Blocks.DIRT_PATH) {
            return;
        }
        if (block == Blocks.GRASS_BLOCK) {
            this.mc.gameMode.startDestroyBlock(this.entity.blockPosition(), Direction.DOWN);
        }
        if (this.putOutFire.get().booleanValue() && (horizontalDirectionArray = this.entity) instanceof LivingEntity && (animal = (LivingEntity)horizontalDirectionArray).getHealth() < 2.0f) {
            this.mc.gameMode.startDestroyBlock(this.entity.blockPosition(), Direction.DOWN);
            for (HorizontalDirection direction : HorizontalDirection.values()) {
                this.mc.gameMode.startDestroyBlock(this.entity.blockPosition().offset(direction.offsetX, 0, direction.offsetZ), Direction.DOWN);
            }
        } else if (this.ticks >= this.tickInterval.get() && !this.entity.isOnFire()) {
            this.mc.gameMode.useItemOn(this.mc.player, this.hand, new BlockHitResult(this.entity.position().subtract(new Vec3(0.0, 1.0, 0.0)), Direction.UP, this.entity.blockPosition().below(), false));
            this.ticks = 0;
        }
        InvUtils.swapBack();
    }
}
