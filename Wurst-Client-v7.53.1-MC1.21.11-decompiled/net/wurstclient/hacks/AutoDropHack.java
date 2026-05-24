package net.wurstclient.hacks;

import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_465;
import net.minecraft.class_490;
import net.minecraft.class_7923;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.ItemListSetting;

@SearchTags(value={"auto drop", "AutoEject", "auto-eject", "auto eject", "InventoryCleaner", "inventory cleaner", "InvCleaner", "inv cleaner"})
public final class AutoDropHack
extends Hack
implements UpdateListener {
    private ItemListSetting items = new ItemListSetting("Items", "Unwanted items that will be dropped.", "minecraft:allium", "minecraft:azure_bluet", "minecraft:blue_orchid", "minecraft:cornflower", "minecraft:dandelion", "minecraft:lilac", "minecraft:lily_of_the_valley", "minecraft:orange_tulip", "minecraft:oxeye_daisy", "minecraft:peony", "minecraft:pink_tulip", "minecraft:poisonous_potato", "minecraft:poppy", "minecraft:red_tulip", "minecraft:rose_bush", "minecraft:rotten_flesh", "minecraft:sunflower", "minecraft:wheat_seeds", "minecraft:white_tulip");
    private final String renderName = Math.random() < 0.01 ? "AutoLinus" : this.getName();

    public AutoDropHack() {
        super("AutoDrop");
        this.setCategory(Category.ITEMS);
        this.addSetting(this.items);
    }

    @Override
    public String getRenderName() {
        return this.renderName;
    }

    @Override
    protected void onEnable() {
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
    }

    @Override
    public void onUpdate() {
        if (AutoDropHack.MC.field_1755 instanceof class_465 && !(AutoDropHack.MC.field_1755 instanceof class_490)) {
            return;
        }
        for (int slot = 9; slot < 45; ++slot) {
            class_1799 stack;
            int adjustedSlot = slot;
            if (adjustedSlot >= 36) {
                adjustedSlot -= 36;
            }
            if ((stack = AutoDropHack.MC.field_1724.method_31548().method_5438(adjustedSlot)).method_7960()) continue;
            class_1792 item = stack.method_7909();
            String itemName = class_7923.field_41178.method_10221((Object)item).toString();
            if (!this.items.getItemNames().contains(itemName)) continue;
            IMC.getInteractionManager().windowClick_THROW(slot);
        }
    }
}
