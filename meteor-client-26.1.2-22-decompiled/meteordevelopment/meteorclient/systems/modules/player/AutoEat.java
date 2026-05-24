package meteordevelopment.meteorclient.systems.modules.player;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import meteordevelopment.meteorclient.events.entity.player.ItemUseCrosshairTargetEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.AnchorAura;
import meteordevelopment.meteorclient.systems.modules.combat.BedAura;
import meteordevelopment.meteorclient.systems.modules.combat.CrystalAura;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.systems.modules.player.AutoGap;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AutoEat
extends Module {
    private static final Class<? extends Module>[] AURAS = new Class[]{KillAura.class, CrystalAura.class, AnchorAura.class, BedAura.class};
    private final SettingGroup sgGeneral;
    private final SettingGroup sgThreshold;
    public final Setting<List<Item>> blacklist;
    private final Setting<Boolean> pauseAuras;
    private final Setting<Boolean> pauseBaritone;
    private final Setting<Boolean> searchInventory;
    private final Setting<Priority> prioritise;
    private final Setting<ThresholdMode> thresholdMode;
    private final Setting<Double> healthThreshold;
    private final Setting<Integer> hungerThreshold;
    public boolean eating;
    private int slot;
    private int prevSlot;
    private final List<Class<? extends Module>> wasAura;
    private boolean wasBaritone;

    public AutoEat() {
        super(Categories.Player, "auto-eat", "Automatically eats food.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgThreshold = this.settings.createGroup("Threshold");
        this.blacklist = this.sgGeneral.add(((ItemListSetting.Builder)((ItemListSetting.Builder)new ItemListSetting.Builder().name("blacklist")).description("Which items to not eat.")).defaultValue(Items.ENCHANTED_GOLDEN_APPLE, Items.GOLDEN_APPLE, Items.CHORUS_FRUIT, Items.POISONOUS_POTATO, Items.PUFFERFISH, Items.CHICKEN, Items.ROTTEN_FLESH, Items.SPIDER_EYE, Items.SUSPICIOUS_STEW).filter(Utils::isFood).bypassFilterWhenSavingAndLoading().build());
        this.pauseAuras = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-auras")).description("Pauses all auras when eating.")).defaultValue(true)).build());
        this.pauseBaritone = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-baritone")).description("Pause baritone when eating.")).defaultValue(true)).build());
        this.searchInventory = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("search-inventory")).description("Search the full inventory for food, not only the hotbar.")).defaultValue(false)).build());
        this.prioritise = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("food-priority")).description("Which aspect of the food to prioritise selecting for.")).defaultValue(Priority.Saturation)).build());
        this.thresholdMode = this.sgThreshold.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("threshold-mode")).description("The threshold mode to trigger auto eat.\n'Both' == health AND hunger, 'Any' == health OR hunger")).defaultValue(ThresholdMode.Any)).build());
        this.healthThreshold = this.sgThreshold.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("health-threshold")).description("The level of health you eat at.")).defaultValue(10.0).range(1.0, 19.0).sliderRange(1.0, 19.0).visible(() -> this.thresholdMode.get() != ThresholdMode.Hunger)).build());
        this.hungerThreshold = this.sgThreshold.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("hunger-threshold")).description("The level of hunger you eat at.")).defaultValue(16)).range(1, 19).sliderRange(1, 19).visible(() -> this.thresholdMode.get() != ThresholdMode.Health)).build());
        this.wasAura = new ReferenceArrayList();
        this.wasBaritone = false;
    }

    @Override
    public void onDeactivate() {
        if (this.eating) {
            this.stopEating();
        }
    }

    @EventHandler(priority=-100)
    private void onTick(TickEvent.Pre event) {
        if (Modules.get().get(AutoGap.class).isEating()) {
            return;
        }
        if (this.eating) {
            if (!this.shouldEat()) {
                this.stopEating();
                return;
            }
            if (!Utils.isFood(this.mc.player.getInventory().getItem(this.slot))) {
                int newSlot = this.findSlot();
                if (newSlot == -1) {
                    this.stopEating();
                    return;
                }
                this.changeSlot(newSlot);
            }
            this.eat();
            return;
        }
        if (this.shouldEat()) {
            this.startEating();
        }
    }

    @EventHandler
    private void onItemUseCrosshairTarget(ItemUseCrosshairTargetEvent event) {
        if (this.eating) {
            event.target = null;
        }
    }

    private void startEating() {
        this.prevSlot = this.mc.player.getInventory().getSelectedSlot();
        this.eat();
        this.wasAura.clear();
        if (this.pauseAuras.get().booleanValue()) {
            for (Class<? extends Module> klass : AURAS) {
                Module module = Modules.get().get(klass);
                if (!module.isActive()) continue;
                this.wasAura.add(klass);
                module.toggle();
            }
        }
        if (this.pauseBaritone.get().booleanValue() && PathManagers.get().isPathing() && !this.wasBaritone) {
            this.wasBaritone = true;
            PathManagers.get().pause();
        }
    }

    private void eat() {
        if (!this.changeSlot(this.slot)) {
            return;
        }
        this.setPressed(true);
        if (!this.mc.player.isUsingItem()) {
            Utils.rightClick();
        }
        this.eating = true;
    }

    private void stopEating() {
        if (this.prevSlot != 40) {
            this.changeSlot(this.prevSlot);
        }
        this.setPressed(false);
        this.eating = false;
        if (this.pauseAuras.get().booleanValue()) {
            for (Class<? extends Module> klass : AURAS) {
                if (!this.wasAura.contains(klass)) continue;
                Modules.get().get(klass).enable();
            }
        }
        if (this.pauseBaritone.get().booleanValue() && this.wasBaritone) {
            this.wasBaritone = false;
            PathManagers.get().resume();
        }
    }

    private void setPressed(boolean pressed) {
        this.mc.options.keyUse.setDown(pressed);
    }

    private boolean changeSlot(int slot) {
        if (slot == 40) {
            this.slot = 40;
            return true;
        }
        if (SlotUtils.isHotbar(slot)) {
            InvUtils.swap(slot, false);
            this.slot = slot;
            return true;
        }
        int emptySlot = InvUtils.find(ItemStack::isEmpty, 0, 8).slot();
        if (emptySlot == -1) {
            return false;
        }
        InvUtils.move().from(slot).toHotbar(emptySlot);
        InvUtils.swap(emptySlot, false);
        this.slot = emptySlot;
        return true;
    }

    public boolean shouldEat() {
        boolean hungerLow;
        boolean healthLow = (double)this.mc.player.getHealth() <= this.healthThreshold.get();
        boolean bl = hungerLow = this.mc.player.getFoodData().getFoodLevel() <= this.hungerThreshold.get();
        if (!this.thresholdMode.get().test(healthLow, hungerLow)) {
            return false;
        }
        this.slot = this.findSlot();
        if (this.slot == -1) {
            return false;
        }
        ItemStack item = this.mc.player.getInventory().getItem(this.slot);
        FoodProperties prop = (FoodProperties)item.get(DataComponents.FOOD);
        if (prop == null || !Utils.isFood(item)) {
            return false;
        }
        return this.mc.player.getFoodData().needsFood() || prop.canAlwaysEat();
    }

    private int findSlot() {
        Item offHandItem = this.mc.player.getOffhandItem().getItem();
        if (Utils.isFood(offHandItem) && !this.blacklist.get().contains(offHandItem)) {
            return 40;
        }
        int slot = this.findBestFood(0, 8);
        if (slot != -1) {
            return slot;
        }
        if (this.searchInventory.get().booleanValue()) {
            return this.findBestFood(9, 35);
        }
        return -1;
    }

    private int findBestFood(int start, int end) {
        int best = -1;
        float bestHunger = -1.0f;
        for (int i = start; i <= end; ++i) {
            float hunger;
            ItemStack stack = this.mc.player.getInventory().getItem(i);
            FoodProperties food = (FoodProperties)stack.get(DataComponents.FOOD);
            if (!Utils.isFood(stack) || food == null) continue;
            Item item = stack.getItem();
            if (this.blacklist.get().contains(item) || !((hunger = this.prioritise.get().value(food)) > bestHunger)) continue;
            bestHunger = hunger;
            best = i;
        }
        return best;
    }

    public static enum Priority {
        Combined,
        Hunger,
        Saturation;


        public float value(FoodProperties food) {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> (float)food.nutrition() + food.saturation();
                case 1 -> food.nutrition();
                case 2 -> food.saturation();
            };
        }
    }

    public static enum ThresholdMode {
        Health((health, hunger) -> health),
        Hunger((health, hunger) -> hunger),
        Any((health, hunger) -> health != false || hunger != false),
        Both((health, hunger) -> health != false && hunger != false);

        private final BiPredicate<Boolean, Boolean> predicate;

        private ThresholdMode(BiPredicate<Boolean, Boolean> predicate) {
            this.predicate = predicate;
        }

        public boolean test(boolean health, boolean hunger) {
            return this.predicate.test(health, hunger);
        }
    }
}
