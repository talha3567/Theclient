package meteordevelopment.meteorclient.systems.modules.world;

import java.util.LinkedHashMap;
import java.util.Set;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

public class AutoBreed
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Set<EntityType<?>>> entities;
    private final Setting<Double> range;
    private final Setting<InteractionHand> hand;
    private final Setting<EntityAge> mobAgeFilter;
    private final Setting<Boolean> continuousBreeding;
    private final Setting<Integer> breedingInterval;
    private final LinkedHashMap<Entity, Integer> animalsFed;
    private int tickCounter;

    public AutoBreed() {
        super(Categories.World, "auto-breed", "Automatically breeds specified animals.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.entities = this.sgGeneral.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entities")).description("Entities to breed.")).defaultValue(EntityType.HORSE, EntityType.DONKEY, EntityType.COW, EntityType.MOOSHROOM, EntityType.SHEEP, EntityType.PIG, EntityType.CHICKEN, EntityType.WOLF, EntityType.CAT, EntityType.OCELOT, EntityType.RABBIT, EntityType.LLAMA, EntityType.TURTLE, EntityType.PANDA, EntityType.FOX, EntityType.BEE, EntityType.STRIDER, EntityType.HOGLIN).onlyAttackable().build());
        this.range = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("range")).description("How far away the animals can be to be bred.")).min(0.0).defaultValue(4.5).build());
        this.hand = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("hand-for-breeding")).description("The hand to use for breeding.")).defaultValue(InteractionHand.MAIN_HAND)).build());
        this.mobAgeFilter = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mob-age-filter")).description("Determines the age of the mobs to target (baby, adult, or both).")).defaultValue(EntityAge.Adult)).build());
        this.continuousBreeding = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("continuous-breeding")).description("Whether to feed the same animal again after a certain time period.")).defaultValue(false)).build());
        this.breedingInterval = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("breeding-interval")).description("Determines how often the same animal is fed in ticks.")).min(1).sliderMax(24000).defaultValue(6600)).visible(this.continuousBreeding::get)).build());
        this.animalsFed = new LinkedHashMap();
        this.tickCounter = 0;
    }

    @Override
    public void onActivate() {
        this.animalsFed.clear();
        this.tickCounter = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        for (Entity entity : this.mc.level.entitiesForRendering()) {
            if (!(entity instanceof Animal)) continue;
            Animal animal = (Animal)entity;
            if (!this.entities.get().contains(animal.getType()) || !this.isCorrectAge(animal) || this.animalsFed.containsKey(animal) || !PlayerUtils.isWithin((Entity)animal, (double)this.range.get()) || !animal.isFood(this.hand.get() == InteractionHand.MAIN_HAND ? this.mc.player.getMainHandItem() : this.mc.player.getOffhandItem())) continue;
            Rotations.rotate(Rotations.getYaw(entity), Rotations.getPitch(entity), -100, () -> {
                EntityHitResult location = new EntityHitResult((Entity)animal, animal.getBoundingBox().getCenter());
                this.mc.gameMode.interact((Player)this.mc.player, (Entity)animal, location, this.hand.get());
                this.mc.player.swing(this.hand.get());
                this.animalsFed.putLast((Entity)animal, this.tickCounter);
            });
            break;
        }
        if (this.continuousBreeding.get().booleanValue()) {
            while (!this.animalsFed.isEmpty() && (Integer)this.animalsFed.firstEntry().getValue() < this.tickCounter - this.breedingInterval.get()) {
                this.animalsFed.pollFirstEntry();
            }
            ++this.tickCounter;
        }
    }

    private boolean isCorrectAge(Animal animal) {
        return switch (this.mobAgeFilter.get().ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> animal.isBaby();
            case 1 -> {
                if (!animal.isBaby()) {
                    yield true;
                }
                yield false;
            }
            case 2 -> true;
        };
    }

    public static enum EntityAge {
        Baby,
        Adult,
        Both;

    }
}
