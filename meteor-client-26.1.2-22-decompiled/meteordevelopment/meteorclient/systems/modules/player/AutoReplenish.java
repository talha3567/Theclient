package meteordevelopment.meteorclient.systems.modules.player;

import java.util.List;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.AutoTotem;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class AutoReplenish
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Integer> minCount;
    private final Setting<Integer> tickDelay;
    private final Setting<Boolean> offhand;
    private final Setting<Boolean> unstackable;
    private final Setting<Boolean> sameEnchants;
    private final Setting<Boolean> searchHotbar;
    private final Setting<List<Item>> excludedItems;
    private final ItemStack[] items;
    private boolean prevHadOpenScreen;
    private int tickDelayLeft;

    public AutoReplenish() {
        super(Categories.Player, "auto-replenish", "Automatically refills items in your hotbar, main hand, or offhand.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.minCount = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("min-count")).description("Replenish a slot when it reaches this item count.")).defaultValue(8)).min(1).sliderRange(1, 63).build());
        this.tickDelay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("delay")).description("How long in ticks to wait between replenishing your hotbar.")).defaultValue(1)).min(0).build());
        this.offhand = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("offhand")).description("Whether or not to replenish items in your offhand.")).defaultValue(true)).build());
        this.unstackable = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("unstackable")).description("Replenish unstackable items.")).defaultValue(true)).build());
        this.sameEnchants = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("same-enchants")).description("Only replace unstackables with items that have the same enchants.")).defaultValue(true)).visible(this.unstackable::get)).build());
        this.searchHotbar = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("search-hotbar")).description("Combine stacks in your hotbar/offhand as a last resort.")).defaultValue(false)).build());
        this.excludedItems = this.sgGeneral.add(((ItemListSetting.Builder)((ItemListSetting.Builder)new ItemListSetting.Builder().name("excluded-items")).description("Items that won't be replenished.")).build());
        this.items = new ItemStack[10];
    }

    @Override
    public void onActivate() {
        this.fillItems();
        this.tickDelayLeft = this.tickDelay.get();
        this.prevHadOpenScreen = this.mc.screen != null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.screen == null && this.prevHadOpenScreen) {
            this.fillItems();
        }
        boolean bl = this.prevHadOpenScreen = this.mc.screen != null;
        if (this.mc.player.containerMenu.getItems().size() != 46 || this.mc.screen != null) {
            return;
        }
        if (this.tickDelayLeft > 0) {
            --this.tickDelayLeft;
            return;
        }
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = this.mc.player.getInventory().getItem(i);
            this.checkSlot(i, stack);
        }
        if (this.offhand.get().booleanValue() && !Modules.get().get(AutoTotem.class).isLocked()) {
            ItemStack stack = this.mc.player.getOffhandItem();
            this.checkSlot(9, stack);
        }
        this.tickDelayLeft = this.tickDelay.get();
    }

    private void checkSlot(int slot, ItemStack stack) {
        ItemStack prevStack = this.items[slot];
        this.items[slot] = stack.copy();
        if (slot == 9) {
            slot = 40;
        }
        if (this.excludedItems.get().contains(stack.getItem())) {
            return;
        }
        if (this.excludedItems.get().contains(prevStack.getItem())) {
            return;
        }
        int fromSlot = -1;
        if (stack.isStackable() && !stack.isEmpty() && stack.getCount() <= this.minCount.get()) {
            fromSlot = this.findItem(stack, slot, this.minCount.get() - stack.getCount() + 1, true);
        }
        if (prevStack.isStackable() && stack.isEmpty() && !prevStack.isEmpty()) {
            fromSlot = this.findItem(prevStack, slot, this.minCount.get() - stack.getCount() + 1, false);
        }
        if (this.unstackable.get().booleanValue() && !prevStack.isStackable() && stack.isEmpty() && !prevStack.isEmpty()) {
            fromSlot = this.findItem(prevStack, slot, 1, false);
        }
        if (fromSlot == this.mc.player.getInventory().getSelectedSlot() || fromSlot == 40) {
            return;
        }
        if (fromSlot < 9 && fromSlot < slot && slot != this.mc.player.getInventory().getSelectedSlot() && slot != 40) {
            return;
        }
        InvUtils.move().from(fromSlot).to(slot);
    }

    private int findItem(ItemStack lookForStack, int excludedSlot, int goodEnoughCount, boolean mustCombine) {
        int slot = -1;
        int count = 0;
        for (int i = this.mc.player.getInventory().getContainerSize() - 2; i >= (this.searchHotbar.get() != false ? 0 : 9); --i) {
            ItemStack stack;
            if (i == excludedSlot || (stack = this.mc.player.getInventory().getItem(i)).getItem() != lookForStack.getItem() || mustCombine && !ItemStack.isSameItemSameComponents((ItemStack)lookForStack, (ItemStack)stack) || this.sameEnchants.get().booleanValue() && !stack.getEnchantments().equals((Object)lookForStack.getEnchantments()) || stack.getCount() <= count) continue;
            slot = i;
            count = stack.getCount();
            if (count >= goodEnoughCount) break;
        }
        return slot;
    }

    private void fillItems() {
        for (int i = 0; i < 9; ++i) {
            this.items[i] = this.mc.player.getInventory().getItem(i).copy();
        }
        this.items[9] = this.mc.player.getOffhandItem().copy();
    }
}
