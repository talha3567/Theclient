package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.stream.Stream;
import net.minecraft.class_10124;
import net.minecraft.class_10132;
import net.minecraft.class_10134;
import net.minecraft.class_10138;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1321;
import net.minecraft.class_1646;
import net.minecraft.class_1661;
import net.minecraft.class_1702;
import net.minecraft.class_1799;
import net.minecraft.class_2237;
import net.minecraft.class_2248;
import net.minecraft.class_2304;
import net.minecraft.class_2338;
import net.minecraft.class_239;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_4174;
import net.minecraft.class_6880;
import net.minecraft.class_746;
import net.minecraft.class_9334;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.TakeItemsFromSetting;
import net.wurstclient.util.InventoryUtils;

@SearchTags(value={"auto eat", "AutoFood", "auto food", "AutoFeeder", "auto feeder", "AutoFeeding", "auto feeding", "AutoSoup", "auto soup"})
public final class AutoEatHack
extends Hack
implements UpdateListener {
    private final SliderSetting targetHunger = new SliderSetting("Target hunger", "description.wurst.setting.autoeat.target_hunger", 10.0, 0.0, 10.0, 0.5, SliderSetting.ValueDisplay.DECIMAL);
    private final SliderSetting minHunger = new SliderSetting("Min hunger", "description.wurst.setting.autoeat.min_hunger", 6.5, 0.0, 10.0, 0.5, SliderSetting.ValueDisplay.DECIMAL);
    private final SliderSetting injuredHunger = new SliderSetting("Injured hunger", "description.wurst.setting.autoeat.injured_hunger", 10.0, 0.0, 10.0, 0.5, SliderSetting.ValueDisplay.DECIMAL);
    private final SliderSetting injuryThreshold = new SliderSetting("Injury threshold", "description.wurst.setting.autoeat.injury_threshold", 1.5, 0.5, 10.0, 0.5, SliderSetting.ValueDisplay.DECIMAL);
    private final TakeItemsFromSetting takeItemsFrom = TakeItemsFromSetting.withHands(this, TakeItemsFromSetting.TakeItemsFrom.HOTBAR);
    private final CheckboxSetting allowOffhand = new CheckboxSetting("Allow offhand", true);
    private final CheckboxSetting eatWhileWalking = new CheckboxSetting("Eat while walking", "description.wurst.setting.autoeat.eat_while_walking", false);
    private final CheckboxSetting allowHunger = new CheckboxSetting("Allow hunger effect", "description.wurst.setting.autoeat.allow_hunger", true);
    private final CheckboxSetting allowPoison = new CheckboxSetting("Allow poison effect", "description.wurst.setting.autoeat.allow_poison", false);
    private final CheckboxSetting allowChorus = new CheckboxSetting("Allow chorus fruit", "description.wurst.setting.autoeat.allow_chorus", false);
    private int oldSlot = -1;

    public AutoEatHack() {
        super("AutoEat");
        this.setCategory(Category.ITEMS);
        this.addSetting(this.targetHunger);
        this.addSetting(this.minHunger);
        this.addSetting(this.injuredHunger);
        this.addSetting(this.injuryThreshold);
        this.addSetting(this.takeItemsFrom);
        this.addSetting(this.allowOffhand);
        this.addSetting(this.eatWhileWalking);
        this.addSetting(this.allowHunger);
        this.addSetting(this.allowPoison);
        this.addSetting(this.allowChorus);
    }

    @Override
    protected void onEnable() {
        AutoEatHack.WURST.getHax().autoSoupHack.setEnabled(false);
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        if (this.isEating()) {
            this.stopEating();
        }
    }

    @Override
    public void onUpdate() {
        class_746 player = AutoEatHack.MC.field_1724;
        if (!this.shouldEat()) {
            if (this.isEating()) {
                this.stopEating();
            }
            return;
        }
        class_1702 hungerManager = player.method_7344();
        int foodLevel = hungerManager.method_7586();
        int targetHungerI = (int)(this.targetHunger.getValue() * 2.0);
        int minHungerI = (int)(this.minHunger.getValue() * 2.0);
        int injuredHungerI = (int)(this.injuredHunger.getValue() * 2.0);
        if (this.isInjured(player) && foodLevel < injuredHungerI) {
            this.eat(-1);
            return;
        }
        if (foodLevel < minHungerI) {
            this.eat(-1);
            return;
        }
        if (foodLevel < targetHungerI) {
            int maxPoints = targetHungerI - foodLevel;
            this.eat(maxPoints);
        }
    }

    private void eat(int maxPoints) {
        class_1661 inventory = AutoEatHack.MC.field_1724.method_31548();
        int foodSlot = this.findBestFoodSlot(maxPoints);
        if (foodSlot == -1) {
            if (this.isEating()) {
                this.stopEating();
            }
            return;
        }
        if (foodSlot < 9) {
            if (!this.isEating()) {
                this.oldSlot = inventory.method_67532();
            }
            inventory.method_61496(foodSlot);
        } else if (foodSlot == 40) {
            if (!this.isEating()) {
                this.oldSlot = inventory.method_67532();
            }
        } else {
            InventoryUtils.selectItem(foodSlot);
            return;
        }
        AutoEatHack.MC.field_1690.field_1904.method_23481(true);
        IMC.getInteractionManager().rightClickItem();
    }

    private int findBestFoodSlot(int maxPoints) {
        class_1661 inventory = AutoEatHack.MC.field_1724.method_31548();
        class_4174 bestFood = null;
        int bestSlot = -1;
        int maxInvSlot = this.takeItemsFrom.getMaxInvSlot();
        ArrayList<Integer> slots = new ArrayList<Integer>();
        if (maxInvSlot == 0) {
            slots.add(inventory.method_67532());
        }
        if (this.allowOffhand.isChecked()) {
            slots.add(40);
        }
        Stream.iterate(0, i -> i < maxInvSlot, i -> i + 1).forEach(i -> slots.add((Integer)i));
        Comparator<class_4174> comparator = Comparator.comparingDouble(class_4174::comp_2492);
        Iterator iterator = slots.iterator();
        while (iterator.hasNext()) {
            int slot = (Integer)iterator.next();
            class_1799 stack = inventory.method_5438(slot);
            if (!stack.method_57826(class_9334.field_50075) || !this.isAllowedFood((class_10124)stack.method_58694(class_9334.field_53964))) continue;
            class_4174 food = (class_4174)stack.method_58694(class_9334.field_50075);
            if (maxPoints >= 0 && food.comp_2491() > maxPoints || bestFood != null && comparator.compare(food, bestFood) <= 0) continue;
            bestFood = food;
            bestSlot = slot;
        }
        return bestSlot;
    }

    private boolean shouldEat() {
        if (AutoEatHack.MC.field_1724.method_31549().field_7477) {
            return false;
        }
        if (!AutoEatHack.MC.field_1724.method_7332(false)) {
            return false;
        }
        if (!(this.eatWhileWalking.isChecked() || AutoEatHack.MC.field_1724.field_6250 == 0.0f && AutoEatHack.MC.field_1724.field_6212 == 0.0f)) {
            return false;
        }
        return !this.isClickable(AutoEatHack.MC.field_1765);
    }

    private void stopEating() {
        AutoEatHack.MC.field_1690.field_1904.method_23481(false);
        AutoEatHack.MC.field_1724.method_31548().method_61496(this.oldSlot);
        this.oldSlot = -1;
    }

    private boolean isAllowedFood(class_10124 consumable) {
        for (class_10134 consumeEffect : consumable.comp_3089()) {
            if (!this.allowChorus.isChecked() && consumeEffect instanceof class_10138) {
                return false;
            }
            if (!(consumeEffect instanceof class_10132)) continue;
            class_10132 applyEffectsConsumeEffect = (class_10132)consumeEffect;
            for (class_1293 effect : applyEffectsConsumeEffect.comp_3094()) {
                class_6880 entry = effect.method_5579();
                if (!this.allowHunger.isChecked() && entry == class_1294.field_5903) {
                    return false;
                }
                if (this.allowPoison.isChecked() || entry != class_1294.field_5899) continue;
                return false;
            }
        }
        return true;
    }

    public boolean isEating() {
        return this.oldSlot != -1;
    }

    private boolean isClickable(class_239 hitResult) {
        if (hitResult == null) {
            return false;
        }
        if (hitResult instanceof class_3966) {
            class_1297 entity = ((class_3966)hitResult).method_17782();
            return entity instanceof class_1646 || entity instanceof class_1321;
        }
        if (hitResult instanceof class_3965) {
            class_2338 pos = ((class_3965)hitResult).method_17777();
            if (pos == null) {
                return false;
            }
            class_2248 block = AutoEatHack.MC.field_1687.method_8320(pos).method_26204();
            return block instanceof class_2237 || block instanceof class_2304;
        }
        return false;
    }

    private boolean isInjured(class_746 player) {
        int injuryThresholdI = (int)(this.injuryThreshold.getValue() * 2.0);
        return player.method_6032() < player.method_6063() - (float)injuryThresholdI;
    }
}
