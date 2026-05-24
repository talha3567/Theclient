package meteordevelopment.meteorclient.systems.modules.world;

import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.PotionSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.MyPotion;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

public class AutoBrewer
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<MyPotion> potion;
    private int ingredientI;
    private boolean first;
    private int timer;

    public AutoBrewer() {
        super(Categories.World, "auto-brewer", "Automatically brews the specified potion.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.potion = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new PotionSetting.Builder().name("potion")).description("The type of potion to brew.")).defaultValue(MyPotion.Strength)).build());
    }

    @Override
    public void onActivate() {
        this.first = false;
    }

    public void onBrewingStandClose() {
        this.first = false;
    }

    public void tick(BrewingStandMenu c) {
        ++this.timer;
        if (!this.first) {
            this.first = true;
            this.ingredientI = -2;
            this.timer = 0;
        }
        if (c.getBrewingTicks() != 0 || this.timer < 5) {
            return;
        }
        if (this.ingredientI == -2) {
            if (this.takePotions(c)) {
                return;
            }
            ++this.ingredientI;
            this.timer = 0;
        } else if (this.ingredientI == -1) {
            if (this.insertWaterBottles(c)) {
                return;
            }
            ++this.ingredientI;
            this.timer = 0;
        } else if (this.ingredientI < this.potion.get().ingredients.length) {
            if (this.checkFuel(c)) {
                return;
            }
            if (this.insertIngredient(c, this.potion.get().ingredients[this.ingredientI])) {
                return;
            }
            ++this.ingredientI;
            this.timer = 0;
        } else {
            this.ingredientI = -2;
            this.timer = 0;
        }
    }

    private boolean insertIngredient(BrewingStandMenu c, Item ingredient) {
        int slot = -1;
        for (int slotI = 5; slotI < c.slots.size(); ++slotI) {
            if (((Slot)c.slots.get(slotI)).getItem().getItem() != ingredient) continue;
            slot = slotI;
            break;
        }
        if (slot == -1) {
            this.error("You do not have any %s left in your inventory... disabling.", I18n.get((String)ingredient.getDescriptionId(), (Object[])new Object[0]));
            this.toggle();
            return true;
        }
        this.moveOneItem(c, slot, 3);
        return false;
    }

    private boolean checkFuel(BrewingStandMenu c) {
        if (c.getFuel() == 0) {
            int slot = -1;
            for (int slotI = 5; slotI < c.slots.size(); ++slotI) {
                if (((Slot)c.slots.get(slotI)).getItem().getItem() != Items.BLAZE_POWDER) continue;
                slot = slotI;
                break;
            }
            if (slot == -1) {
                this.error("You do not have a sufficient amount of blaze powder to use as fuel for the brew... disabling.", new Object[0]);
                this.toggle();
                return true;
            }
            this.moveOneItem(c, slot, 4);
        }
        return false;
    }

    private void moveOneItem(BrewingStandMenu c, int from, int to) {
        InvUtils.move().fromId(from).toId(to);
    }

    private boolean insertWaterBottles(BrewingStandMenu c) {
        for (int i = 0; i < 3; ++i) {
            int slot = -1;
            for (int slotI = 5; slotI < c.slots.size(); ++slotI) {
                Potion potion;
                if (((Slot)c.slots.get(slotI)).getItem().getItem() != Items.POTION || (potion = (Potion)((Holder)((PotionContents)((Slot)c.slots.get(slotI)).getItem().get(DataComponents.POTION_CONTENTS)).potion().get()).value()) != Potions.WATER.value()) continue;
                slot = slotI;
                break;
            }
            if (slot == -1) {
                this.error("You do not have a sufficient amount of water bottles to complete this brew... disabling.", new Object[0]);
                this.toggle();
                return true;
            }
            InvUtils.move().fromId(slot).toId(i);
        }
        return false;
    }

    private boolean takePotions(BrewingStandMenu c) {
        for (int i = 0; i < 3; ++i) {
            InvUtils.shiftClick().slotId(i);
            if (((Slot)c.slots.get(i)).getItem().isEmpty()) continue;
            this.error("You do not have a sufficient amount of inventory space... disabling.", new Object[0]);
            this.toggle();
            return true;
        }
        return false;
    }
}
