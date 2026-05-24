package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.CrystalAura;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.Target;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;

public class KillAura
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgTargeting;
    private final SettingGroup sgTiming;
    private final Setting<AttackItems> attackWhenHolding;
    private final Setting<List<Item>> weapons;
    private final Setting<RotationMode> rotation;
    private final Setting<Boolean> autoSwitch;
    private final Setting<Boolean> swapBack;
    private final Setting<ShieldMode> shieldMode;
    private final Setting<Boolean> onlyOnClick;
    private final Setting<Boolean> onlyOnLook;
    private final Setting<Boolean> pauseOnCombat;
    private final Setting<Set<EntityType<?>>> entities;
    private final Setting<SortPriority> priority;
    private final Setting<Integer> maxTargets;
    private final Setting<Double> range;
    private final Setting<Double> wallsRange;
    private final Setting<EntityAge> passiveMobAgeFilter;
    private final Setting<EntityAge> hostileMobAgeFilter;
    private final Setting<Boolean> ignoreNamed;
    private final Setting<Boolean> ignorePassive;
    private final Setting<Boolean> ignoreTamed;
    private final Setting<Boolean> pauseOnLag;
    private final Setting<Boolean> pauseOnUse;
    private final Setting<Boolean> pauseOnCA;
    private final Setting<Boolean> tpsSync;
    private final Setting<Boolean> customDelay;
    private final Setting<Integer> hitDelay;
    private final Setting<Integer> switchDelay;
    private static final ArrayList<Item> FILTER = new ArrayList<Item>(List.of(Items.DIAMOND_SWORD, Items.DIAMOND_AXE, Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_HOE, Items.MACE, Items.DIAMOND_SPEAR, Items.TRIDENT));
    private final List<Entity> targets;
    private int switchTimer;
    private int hitTimer;
    private boolean wasPathing;
    public boolean attacking;
    public boolean swapped;
    public static int previousSlot;

    public KillAura() {
        super(Categories.Combat, "kill-aura", "Attacks specified entities around you.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgTargeting = this.settings.createGroup("Targeting");
        this.sgTiming = this.settings.createGroup("Timing");
        this.attackWhenHolding = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("attack-when-holding")).description("Only attacks an entity when a specified item is in your hand.")).defaultValue(AttackItems.Weapons)).build());
        this.weapons = this.sgGeneral.add(((ItemListSetting.Builder)((ItemListSetting.Builder)((ItemListSetting.Builder)new ItemListSetting.Builder().name("selected-weapon-types")).description("Which types of weapons to attack with (if you select the diamond sword, any type of sword may be used to attack).")).defaultValue(Items.DIAMOND_SWORD, Items.DIAMOND_AXE, Items.TRIDENT).filter(FILTER::contains).visible(() -> this.attackWhenHolding.get() == AttackItems.Weapons)).build());
        this.rotation = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("rotate")).description("Determines when you should rotate towards the target.")).defaultValue(RotationMode.Always)).build());
        this.autoSwitch = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-switch")).description("Switches to an acceptable weapon when attacking the target.")).defaultValue(false)).build());
        this.swapBack = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("swap-back")).description("Switches to your previous slot when done attacking the target.")).defaultValue(false)).visible(this.autoSwitch::get)).build());
        this.shieldMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shield-mode")).description("    What to do when your target is blocking with a shield:\n    - Ignore:   Don't attack them if they are blocking\n    - Break:    Swap to an axe to disable the shield (Only if Auto Switch is enabled)\n    - None:     Attack them as normal\n")).defaultValue(ShieldMode.None)).build());
        this.onlyOnClick = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-on-click")).description("Only attacks when holding left click.")).defaultValue(false)).build());
        this.onlyOnLook = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-on-look")).description("Only attacks when looking at an entity.")).defaultValue(false)).build());
        this.pauseOnCombat = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-baritone")).description("Freezes Baritone temporarily until you are finished attacking the entity.")).defaultValue(true)).build());
        this.entities = this.sgTargeting.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entities")).description("Entities to attack.")).onlyAttackable().defaultValue(EntityType.PLAYER).build());
        this.priority = this.sgTargeting.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("priority")).description("How to filter targets within range.")).defaultValue(SortPriority.ClosestAngle)).build());
        this.maxTargets = this.sgTargeting.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("max-targets")).description("How many entities to target at once.")).defaultValue(1)).min(1).sliderRange(1, 5).visible(() -> this.onlyOnLook.get() == false)).build());
        this.range = this.sgTargeting.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("range")).description("The maximum range the entity can be to attack it.")).defaultValue(4.5).min(0.0).sliderMax(6.0).build());
        this.wallsRange = this.sgTargeting.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("walls-range")).description("The maximum range the entity can be attacked through walls.")).defaultValue(3.5).min(0.0).sliderMax(6.0).build());
        this.passiveMobAgeFilter = this.sgTargeting.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("passive-mob-age-filter")).description("Determines the age of passive mobs to target (animals, villagers).")).defaultValue(EntityAge.Adult)).build());
        this.hostileMobAgeFilter = this.sgTargeting.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("hostile-mob-age-filter")).description("Determines the age of hostile mobs to target (zombies, piglins, hoglins, zoglins).")).defaultValue(EntityAge.Both)).build());
        this.ignoreNamed = this.sgTargeting.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-named")).description("Whether or not to attack mobs with a name.")).defaultValue(false)).build());
        this.ignorePassive = this.sgTargeting.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-passive")).description("Will only attack sometimes passive mobs if they are targeting you.")).defaultValue(true)).build());
        this.ignoreTamed = this.sgTargeting.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-tamed")).description("Will avoid attacking mobs you tamed.")).defaultValue(false)).build());
        this.pauseOnLag = this.sgTiming.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-on-lag")).description("Pauses if the server is lagging.")).defaultValue(true)).build());
        this.pauseOnUse = this.sgTiming.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-on-use")).description("Does not attack while using an item.")).defaultValue(false)).build());
        this.pauseOnCA = this.sgTiming.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-on-CA")).description("Does not attack while CA is placing.")).defaultValue(true)).build());
        this.tpsSync = this.sgTiming.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("TPS-sync")).description("Tries to sync attack delay with the server's TPS.")).defaultValue(true)).build());
        this.customDelay = this.sgTiming.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("custom-delay")).description("Use a custom delay instead of the vanilla cooldown.")).defaultValue(false)).build());
        this.hitDelay = this.sgTiming.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("hit-delay")).description("How fast you hit the entity in ticks.")).defaultValue(11)).min(0).sliderMax(60).visible(this.customDelay::get)).build());
        this.switchDelay = this.sgTiming.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("switch-delay")).description("How many ticks to wait before hitting an entity after switching hotbar slots.")).defaultValue(0)).min(0).sliderMax(10).build());
        this.targets = new ArrayList<Entity>();
        this.wasPathing = false;
    }

    @Override
    public void onActivate() {
        previousSlot = -1;
        this.swapped = false;
    }

    @Override
    public void onDeactivate() {
        this.targets.clear();
        this.stopAttacking();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!this.mc.player.isAlive() || PlayerUtils.getGameMode() == GameType.SPECTATOR) {
            this.stopAttacking();
            return;
        }
        if (this.pauseOnUse.get().booleanValue() && (this.mc.gameMode.isDestroying() || this.mc.player.isUsingItem())) {
            this.stopAttacking();
            return;
        }
        if (this.onlyOnClick.get().booleanValue() && !this.mc.options.keyAttack.isDown()) {
            this.stopAttacking();
            return;
        }
        if (TickRate.INSTANCE.getTimeSinceLastTick() >= 1.0f && this.pauseOnLag.get().booleanValue()) {
            this.stopAttacking();
            return;
        }
        if (this.pauseOnCA.get().booleanValue() && Modules.get().get(CrystalAura.class).isActive() && Modules.get().get(CrystalAura.class).kaTimer > 0) {
            this.stopAttacking();
            return;
        }
        if (this.onlyOnLook.get().booleanValue()) {
            Entity targeted = this.mc.crosshairPickEntity;
            if (targeted == null || !this.entityCheck(targeted)) {
                this.stopAttacking();
                return;
            }
            this.targets.clear();
            this.targets.add(this.mc.crosshairPickEntity);
        } else {
            this.targets.clear();
            TargetUtils.getList(this.targets, this::entityCheck, this.priority.get(), this.maxTargets.get());
        }
        if (this.targets.isEmpty()) {
            this.stopAttacking();
            return;
        }
        Entity primary = this.targets.getFirst();
        if (this.autoSwitch.get().booleanValue()) {
            FindItemResult axeResult;
            FindItemResult weaponResult = new FindItemResult(this.mc.player.getInventory().getSelectedSlot(), -1);
            if (this.attackWhenHolding.get() == AttackItems.Weapons) {
                weaponResult = InvUtils.find(this::acceptableWeapon, 0, 8);
            }
            if (this.shouldShieldBreak() && (axeResult = InvUtils.find(itemStack -> itemStack.getItem() instanceof AxeItem, 0, 8)).found()) {
                weaponResult = axeResult;
            }
            if (!this.swapped) {
                previousSlot = this.mc.player.getInventory().getSelectedSlot();
                this.swapped = true;
            }
            InvUtils.swap(weaponResult.slot(), false);
        }
        if (!this.acceptableWeapon(this.mc.player.getMainHandItem())) {
            this.stopAttacking();
            return;
        }
        this.attacking = true;
        if (this.rotation.get() == RotationMode.Always) {
            Rotations.rotate(Rotations.getYaw(primary), Rotations.getPitch(primary, Target.Body));
        }
        if (this.pauseOnCombat.get().booleanValue() && PathManagers.get().isPathing() && !this.wasPathing) {
            PathManagers.get().pause();
            this.wasPathing = true;
        }
        if (this.delayCheck()) {
            this.targets.forEach(this::attack);
        }
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof ServerboundSetCarriedItemPacket) {
            this.switchTimer = this.switchDelay.get();
        }
    }

    private void stopAttacking() {
        if (!this.attacking) {
            return;
        }
        this.attacking = false;
        if (this.wasPathing) {
            PathManagers.get().resume();
            this.wasPathing = false;
        }
        if (this.swapBack.get().booleanValue() && this.swapped) {
            InvUtils.swap(previousSlot, false);
            this.swapped = false;
        }
    }

    private boolean shouldShieldBreak() {
        for (Entity target : this.targets) {
            Player player;
            if (!(target instanceof Player) || !(player = (Player)target).isBlocking() || this.shieldMode.get() != ShieldMode.Break) continue;
            return true;
        }
        return false;
    }

    private boolean entityCheck(Entity entity) {
        OwnableEntity tameable;
        LivingEntity livingEntity;
        if (entity.equals((Object)this.mc.player) || entity.equals((Object)this.mc.getCameraEntity())) {
            return false;
        }
        if (entity instanceof LivingEntity && (livingEntity = (LivingEntity)entity).isDeadOrDying() || !entity.isAlive()) {
            return false;
        }
        AABB hitbox = entity.getBoundingBox();
        if (!PlayerUtils.isWithin(Mth.clamp((double)this.mc.player.getX(), (double)hitbox.minX, (double)hitbox.maxX), Mth.clamp((double)this.mc.player.getY(), (double)hitbox.minY, (double)hitbox.maxY), Mth.clamp((double)this.mc.player.getZ(), (double)hitbox.minZ, (double)hitbox.maxZ), this.range.get())) {
            return false;
        }
        if (!this.entities.get().contains(entity.getType())) {
            return false;
        }
        if (this.ignoreNamed.get().booleanValue() && entity.hasCustomName()) {
            return false;
        }
        if (!PlayerUtils.canSeeEntity(entity) && !PlayerUtils.isWithin(entity, (double)this.wallsRange.get())) {
            return false;
        }
        if (this.ignoreTamed.get().booleanValue() && entity instanceof OwnableEntity && (tameable = (OwnableEntity)entity).getOwner() != null && tameable.getOwner().equals((Object)this.mc.player)) {
            return false;
        }
        if (this.ignorePassive.get().booleanValue()) {
            EnderMan enderman;
            if (entity instanceof EnderMan && !(enderman = (EnderMan)entity).isAngry()) {
                return false;
            }
            if ((entity instanceof Piglin || entity instanceof ZombifiedPiglin || entity instanceof Wolf) && !((Mob)entity).isAggressive()) {
                return false;
            }
        }
        if (entity instanceof Player) {
            Player player = (Player)entity;
            if (player.isCreative()) {
                return false;
            }
            if (!Friends.get().shouldAttack(player)) {
                return false;
            }
            if (this.shieldMode.get() == ShieldMode.Ignore && player.isBlocking()) {
                return false;
            }
            if (player instanceof FakePlayerEntity) {
                FakePlayerEntity fakePlayer = (FakePlayerEntity)player;
                if (fakePlayer.noHit) {
                    return false;
                }
            }
        }
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity2 = (LivingEntity)entity;
            if (entity instanceof Zombie || entity instanceof Piglin || entity instanceof Hoglin || entity instanceof Zoglin) {
                return switch (this.hostileMobAgeFilter.get().ordinal()) {
                    default -> throw new MatchException(null, null);
                    case 0 -> livingEntity2.isBaby();
                    case 1 -> {
                        if (!livingEntity2.isBaby()) {
                            yield true;
                        }
                        yield false;
                    }
                    case 2 -> true;
                };
            }
            if (entity instanceof AgeableMob && !(entity instanceof Frog) && !(entity instanceof Parrot)) {
                return switch (this.passiveMobAgeFilter.get().ordinal()) {
                    default -> throw new MatchException(null, null);
                    case 0 -> livingEntity2.isBaby();
                    case 1 -> {
                        if (!livingEntity2.isBaby()) {
                            yield true;
                        }
                        yield false;
                    }
                    case 2 -> true;
                };
            }
        }
        return true;
    }

    private boolean delayCheck() {
        float delay;
        if (this.switchTimer > 0) {
            --this.switchTimer;
            return false;
        }
        float f = delay = this.customDelay.get() != false ? (float)this.hitDelay.get().intValue() : 0.5f;
        if (this.tpsSync.get().booleanValue()) {
            delay /= TickRate.INSTANCE.getTickRate() / 20.0f;
        }
        if (this.customDelay.get().booleanValue()) {
            if ((float)this.hitTimer < delay) {
                ++this.hitTimer;
                return false;
            }
            return true;
        }
        return this.mc.player.getAttackStrengthScale(delay) >= 1.0f;
    }

    private void attack(Entity target) {
        if (this.rotation.get() == RotationMode.OnHit) {
            Rotations.rotate(Rotations.getYaw(target), Rotations.getPitch(target, Target.Body));
        }
        this.mc.gameMode.attack((Player)this.mc.player, target);
        this.mc.player.swing(InteractionHand.MAIN_HAND);
        this.hitTimer = 0;
    }

    private boolean acceptableWeapon(ItemStack stack) {
        if (this.shouldShieldBreak()) {
            return stack.getItem() instanceof AxeItem;
        }
        if (this.attackWhenHolding.get() == AttackItems.All) {
            return true;
        }
        if (this.weapons.get().contains(Items.DIAMOND_SWORD) && stack.is(ItemTags.SWORDS)) {
            return true;
        }
        if (this.weapons.get().contains(Items.DIAMOND_AXE) && stack.is(ItemTags.AXES)) {
            return true;
        }
        if (this.weapons.get().contains(Items.DIAMOND_PICKAXE) && stack.is(ItemTags.PICKAXES)) {
            return true;
        }
        if (this.weapons.get().contains(Items.DIAMOND_SHOVEL) && stack.is(ItemTags.SHOVELS)) {
            return true;
        }
        if (this.weapons.get().contains(Items.DIAMOND_HOE) && stack.is(ItemTags.HOES)) {
            return true;
        }
        if (this.weapons.get().contains(Items.MACE) && stack.getItem() instanceof MaceItem) {
            return true;
        }
        if (this.weapons.get().contains(Items.DIAMOND_SPEAR) && stack.is(ItemTags.SPEARS)) {
            return true;
        }
        return this.weapons.get().contains(Items.TRIDENT) && stack.getItem() instanceof TridentItem;
    }

    public Entity getTarget() {
        if (!this.targets.isEmpty()) {
            return this.targets.getFirst();
        }
        return null;
    }

    @Override
    public String getInfoString() {
        if (!this.targets.isEmpty()) {
            return EntityUtils.getName(this.getTarget());
        }
        return null;
    }

    public static enum AttackItems {
        Weapons,
        All;

    }

    public static enum RotationMode {
        Always,
        OnHit,
        None;

    }

    public static enum ShieldMode {
        Ignore,
        Break,
        None;

    }

    public static enum EntityAge {
        Baby,
        Adult,
        Both;

    }
}
