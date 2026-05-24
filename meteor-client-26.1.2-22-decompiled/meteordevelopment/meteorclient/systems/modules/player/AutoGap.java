package meteordevelopment.meteorclient.systems.modules.player;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.events.entity.player.ItemUseCrosshairTargetEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.AnchorAura;
import meteordevelopment.meteorclient.systems.modules.combat.BedAura;
import meteordevelopment.meteorclient.systems.modules.combat.CrystalAura;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AutoGap
extends Module {
    private static final Class<? extends Module>[] AURAS = new Class[]{KillAura.class, CrystalAura.class, AnchorAura.class, BedAura.class};
    private final SettingGroup sgGeneral;
    private final SettingGroup sgPotions;
    private final SettingGroup sgHealth;
    private final Setting<Boolean> allowEgap;
    private final Setting<Boolean> always;
    private final Setting<Boolean> pauseAuras;
    private final Setting<Boolean> pauseBaritone;
    private final Setting<Boolean> beforeExpiry;
    private final Setting<Integer> expiryThreshold;
    private final Setting<Boolean> potionsRegeneration;
    private final Setting<Boolean> potionsFireResistance;
    private final Setting<Boolean> potionsAbsorption;
    private final Setting<Boolean> healthEnabled;
    private final Setting<Integer> healthThreshold;
    private boolean requiresEGap;
    private boolean eating;
    private int slot;
    private int prevSlot;
    private final List<Class<? extends Module>> wasAura;
    private boolean wasBaritone;

    public AutoGap() {
        super(Categories.Player, "auto-gap", "Automatically eats Gaps or E-Gaps.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgPotions = this.settings.createGroup("Potions");
        this.sgHealth = this.settings.createGroup("Health");
        this.allowEgap = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("allow-egap")).description("Allow eating E-Gaps over Gaps if found.")).defaultValue(true)).build());
        this.always = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("always")).description("If it should always eat.")).defaultValue(false)).build());
        this.pauseAuras = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-auras")).description("Pauses all auras when eating.")).defaultValue(true)).build());
        this.pauseBaritone = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-baritone")).description("Pause baritone when eating.")).defaultValue(true)).build());
        this.beforeExpiry = this.sgPotions.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("before-expiry")).description("If it should eat before potion effects expire.")).defaultValue(false)).build());
        this.expiryThreshold = this.sgPotions.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("expiry-threshold")).description("Time in ticks before the potion effect expires to start eating.")).defaultValue(60)).min(0).sliderMax(200).visible(this.beforeExpiry::get)).build());
        this.potionsRegeneration = this.sgPotions.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("potions-regeneration")).description("If it should eat when Regeneration runs out.")).defaultValue(false)).build());
        this.potionsFireResistance = this.sgPotions.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("potions-fire-resistance")).description("If it should eat when Fire Resistance runs out. Requires E-Gaps.")).defaultValue(true)).visible(this.allowEgap::get)).build());
        this.potionsAbsorption = this.sgPotions.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("potions-absorption")).description("If it should eat when Absorption runs out. Requires E-Gaps.")).defaultValue(false)).visible(this.allowEgap::get)).build());
        this.healthEnabled = this.sgHealth.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("health-enabled")).description("If it should eat when health drops below threshold.")).defaultValue(true)).build());
        this.healthThreshold = this.sgHealth.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("health-threshold")).description("Health threshold to eat at. Includes absorption.")).defaultValue(20)).min(0).sliderMax(40).build());
        this.wasAura = new ReferenceArrayList();
    }

    @Override
    public void onDeactivate() {
        if (this.eating) {
            this.stopEating();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.eating) {
            if (this.shouldEat()) {
                if (this.isNotGapOrEGap(this.mc.player.getInventory().getItem(this.slot))) {
                    int slot = this.findSlot();
                    if (slot == -1) {
                        this.stopEating();
                        return;
                    }
                    this.changeSlot(slot);
                }
                this.eat();
            } else {
                this.stopEating();
            }
        } else if (this.shouldEat()) {
            this.slot = this.findSlot();
            if (this.slot != -1) {
                this.startEating();
            }
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
        this.wasBaritone = false;
        if (this.pauseBaritone.get().booleanValue() && PathManagers.get().isPathing()) {
            this.wasBaritone = true;
            PathManagers.get().pause();
        }
    }

    private void eat() {
        this.changeSlot(this.slot);
        this.setPressed(true);
        if (!this.mc.player.isUsingItem()) {
            Utils.rightClick();
        }
        this.eating = true;
    }

    private void stopEating() {
        this.changeSlot(this.prevSlot);
        this.setPressed(false);
        this.eating = false;
        if (this.pauseAuras.get().booleanValue()) {
            for (Class<? extends Module> klass : AURAS) {
                if (!this.wasAura.contains(klass)) continue;
                Modules.get().get(klass).enable();
            }
        }
        if (this.pauseBaritone.get().booleanValue() && this.wasBaritone) {
            PathManagers.get().resume();
        }
    }

    private void setPressed(boolean pressed) {
        this.mc.options.keyUse.setDown(pressed);
    }

    private void changeSlot(int slot) {
        InvUtils.swap(slot, false);
        this.slot = slot;
    }

    private boolean shouldEat() {
        this.requiresEGap = false;
        if (this.always.get().booleanValue()) {
            return true;
        }
        if (this.shouldEatPotions()) {
            return true;
        }
        return this.shouldEatHealth();
    }

    private boolean shouldEatPotions() {
        MobEffectInstance effect;
        Map effects = this.mc.player.getActiveEffectsMap();
        if (this.potionsRegeneration.get().booleanValue() && ((effect = (MobEffectInstance)effects.get(MobEffects.REGENERATION)) == null || this.beforeExpiry.get().booleanValue() && effect.getDuration() <= this.expiryThreshold.get())) {
            return true;
        }
        if (this.potionsFireResistance.get().booleanValue() && ((effect = (MobEffectInstance)effects.get(MobEffects.FIRE_RESISTANCE)) == null || this.beforeExpiry.get().booleanValue() && effect.getDuration() <= this.expiryThreshold.get())) {
            this.requiresEGap = true;
            return true;
        }
        if (this.potionsAbsorption.get().booleanValue() && ((effect = (MobEffectInstance)effects.get(MobEffects.ABSORPTION)) == null || this.beforeExpiry.get().booleanValue() && effect.getDuration() <= this.expiryThreshold.get())) {
            this.requiresEGap = true;
            return true;
        }
        return false;
    }

    private boolean shouldEatHealth() {
        if (!this.healthEnabled.get().booleanValue()) {
            return false;
        }
        int health = Math.round(this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount());
        return health < this.healthThreshold.get();
    }

    private int findSlot() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = this.mc.player.getInventory().getItem(i);
            if (stack.isEmpty() || this.isNotGapOrEGap(stack)) continue;
            Item item = stack.getItem();
            if (item == Items.ENCHANTED_GOLDEN_APPLE && this.allowEgap.get().booleanValue()) {
                return i;
            }
            if (item != Items.GOLDEN_APPLE || this.requiresEGap) continue;
            return i;
        }
        return -1;
    }

    private boolean isNotGapOrEGap(ItemStack stack) {
        Item item = stack.getItem();
        return item != Items.GOLDEN_APPLE && item != Items.ENCHANTED_GOLDEN_APPLE;
    }

    public boolean isEating() {
        return this.isActive() && this.eating;
    }
}
