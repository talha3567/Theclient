package meteordevelopment.meteorclient.systems.modules.player;

import java.util.List;
import java.util.Set;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.events.entity.player.InteractEntityEvent;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;

public class NoInteract
extends Module {
    private final SettingGroup sgBlocks;
    private final SettingGroup sgEntities;
    private final Setting<List<Block>> blockMine;
    private final Setting<ListMode> blockMineMode;
    private final Setting<List<Block>> blockInteract;
    private final Setting<ListMode> blockInteractMode;
    private final Setting<HandMode> blockInteractHand;
    private final Setting<Set<EntityType<?>>> entityHit;
    private final Setting<ListMode> entityHitMode;
    private final Setting<Set<EntityType<?>>> entityInteract;
    private final Setting<ListMode> entityInteractMode;
    private final Setting<HandMode> entityInteractHand;
    private final Setting<InteractMode> friends;
    private final Setting<InteractMode> babies;
    private final Setting<InteractMode> nametagged;

    public NoInteract() {
        super(Categories.Player, "no-interact", "Blocks interactions with certain types of inputs.");
        this.sgBlocks = this.settings.createGroup("Blocks");
        this.sgEntities = this.settings.createGroup("Entities");
        this.blockMine = this.sgBlocks.add(((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("block-mine")).description("Cancels block mining.")).build());
        this.blockMineMode = this.sgBlocks.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("block-mine-mode")).description("List mode to use for block mine.")).defaultValue(ListMode.BlackList)).build());
        this.blockInteract = this.sgBlocks.add(((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("block-interact")).description("Cancels block interaction.")).build());
        this.blockInteractMode = this.sgBlocks.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("block-interact-mode")).description("List mode to use for block interact.")).defaultValue(ListMode.BlackList)).build());
        this.blockInteractHand = this.sgBlocks.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("block-interact-hand")).description("Cancels block interaction if performed by this hand.")).defaultValue(HandMode.None)).build());
        this.entityHit = this.sgEntities.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entity-hit")).description("Cancel entity hitting.")).onlyAttackable().build());
        this.entityHitMode = this.sgEntities.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("entity-hit-mode")).description("List mode to use for entity hit.")).defaultValue(ListMode.BlackList)).build());
        this.entityInteract = this.sgEntities.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entity-interact")).description("Cancel entity interaction.")).onlyAttackable().build());
        this.entityInteractMode = this.sgEntities.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("entity-interact-mode")).description("List mode to use for entity interact.")).defaultValue(ListMode.BlackList)).build());
        this.entityInteractHand = this.sgEntities.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("entity-interact-hand")).description("Cancels entity interaction if performed by this hand.")).defaultValue(HandMode.None)).build());
        this.friends = this.sgEntities.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("friends")).description("Friends cancel mode.")).defaultValue(InteractMode.None)).build());
        this.babies = this.sgEntities.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("babies")).description("Baby entity cancel mode.")).defaultValue(InteractMode.None)).build());
        this.nametagged = this.sgEntities.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("nametagged")).description("Nametagged entity cancel mode.")).defaultValue(InteractMode.None)).build());
    }

    @EventHandler(priority=100)
    private void onStartBreakingBlockEvent(StartBreakingBlockEvent event) {
        if (!this.shouldAttackBlock(event.blockPos)) {
            event.cancel();
        }
    }

    @EventHandler
    private void onInteractBlock(InteractBlockEvent event) {
        if (!this.shouldInteractBlock(event.result, event.hand)) {
            event.cancel();
        }
    }

    @EventHandler(priority=100)
    private void onAttackEntity(AttackEntityEvent event) {
        if (!this.shouldAttackEntity(event.entity)) {
            event.cancel();
        }
    }

    @EventHandler
    private void onInteractEntity(InteractEntityEvent event) {
        if (!this.shouldInteractEntity(event.entity, event.hand)) {
            event.cancel();
        }
    }

    private boolean shouldAttackBlock(BlockPos blockPos) {
        boolean blockInList = this.blockMine.get().contains(this.mc.level.getBlockState(blockPos).getBlock());
        if (this.blockMineMode.get() == ListMode.WhiteList) {
            return blockInList;
        }
        return !blockInList;
    }

    private boolean shouldInteractBlock(BlockHitResult hitResult, InteractionHand hand) {
        if (this.blockInteractHand.get() == HandMode.Both || this.blockInteractHand.get() == HandMode.Mainhand && hand == InteractionHand.MAIN_HAND || this.blockInteractHand.get() == HandMode.Offhand && hand == InteractionHand.OFF_HAND) {
            return false;
        }
        boolean blockInList = this.blockInteract.get().contains(this.mc.level.getBlockState(hitResult.getBlockPos()).getBlock());
        if (this.blockInteractMode.get() == ListMode.WhiteList) {
            return blockInList;
        }
        return !blockInList;
    }

    private boolean shouldAttackEntity(Entity entity) {
        Animal animal;
        if ((this.friends.get() == InteractMode.Both || this.friends.get() == InteractMode.Hit) && entity instanceof Player) {
            Player player = (Player)entity;
            if (!Friends.get().shouldAttack(player)) {
                return false;
            }
        }
        if ((this.babies.get() == InteractMode.Both || this.babies.get() == InteractMode.Hit) && entity instanceof Animal && (animal = (Animal)entity).isBaby()) {
            return false;
        }
        if ((this.nametagged.get() == InteractMode.Both || this.nametagged.get() == InteractMode.Hit) && entity.hasCustomName()) {
            return false;
        }
        if (this.entityHitMode.get() == ListMode.BlackList && this.entityHit.get().contains(entity.getType())) {
            return false;
        }
        return this.entityHitMode.get() != ListMode.WhiteList || this.entityHit.get().contains(entity.getType());
    }

    private boolean shouldInteractEntity(Entity entity, InteractionHand hand) {
        Animal animal;
        if (this.entityInteractHand.get() == HandMode.Both || this.entityInteractHand.get() == HandMode.Mainhand && hand == InteractionHand.MAIN_HAND || this.entityInteractHand.get() == HandMode.Offhand && hand == InteractionHand.OFF_HAND) {
            return false;
        }
        if ((this.friends.get() == InteractMode.Both || this.friends.get() == InteractMode.Interact) && entity instanceof Player) {
            Player player = (Player)entity;
            if (!Friends.get().shouldAttack(player)) {
                return false;
            }
        }
        if ((this.babies.get() == InteractMode.Both || this.babies.get() == InteractMode.Interact) && entity instanceof Animal && (animal = (Animal)entity).isBaby()) {
            return false;
        }
        if ((this.nametagged.get() == InteractMode.Both || this.nametagged.get() == InteractMode.Interact) && entity.hasCustomName()) {
            return false;
        }
        if (this.entityInteractMode.get() == ListMode.BlackList && this.entityInteract.get().contains(entity.getType())) {
            return false;
        }
        return this.entityInteractMode.get() != ListMode.WhiteList || this.entityInteract.get().contains(entity.getType());
    }

    public static enum ListMode {
        WhiteList,
        BlackList;

    }

    public static enum HandMode {
        Mainhand,
        Offhand,
        Both,
        None;

    }

    public static enum InteractMode {
        Hit,
        Interact,
        Both,
        None;

    }
}
