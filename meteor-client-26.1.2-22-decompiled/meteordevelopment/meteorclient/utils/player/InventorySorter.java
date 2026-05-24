package meteordevelopment.meteorclient.utils.player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import meteordevelopment.meteorclient.mixininterface.ISlot;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.meteorclient.utils.render.PeekScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Tuple;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class InventorySorter {
    private final AbstractContainerScreen<?> screen;
    private final InvPart originInvPart;
    private boolean invalid;
    private List<Action> actions;
    private int timer;
    private int currentActionI;

    public InventorySorter(AbstractContainerScreen<?> screen, Slot originSlot) {
        this.screen = screen;
        this.originInvPart = this.getInvPart(originSlot);
        if (this.originInvPart == InvPart.Invalid || this.originInvPart == InvPart.Hotbar || screen instanceof PeekScreen) {
            this.invalid = true;
            return;
        }
        this.actions = new ArrayList<Action>();
        this.generateActions();
    }

    public boolean tick(int delay) {
        if (this.invalid) {
            return true;
        }
        if (this.currentActionI >= this.actions.size()) {
            return true;
        }
        if (this.timer < delay) {
            ++this.timer;
            return false;
        }
        this.timer = 0;
        Action action = this.actions.get(this.currentActionI);
        InvUtils.move().fromId(action.from).toId(action.to);
        ++this.currentActionI;
        return false;
    }

    private void generateActions() {
        ArrayList<MySlot> slots = new ArrayList<MySlot>();
        for (Slot slot : this.screen.getMenu().slots) {
            if (this.getInvPart(slot) != this.originInvPart) continue;
            slots.add(new MySlot(((ISlot)slot).meteor$getIndex(), slot.getItem()));
        }
        slots.sort(Comparator.comparingInt(value -> value.id));
        this.generateStackingActions(slots);
        this.generateSortingActions(slots);
    }

    private void generateStackingActions(List<MySlot> slots) {
        SlotMap slotMap = new SlotMap();
        for (MySlot mySlot : slots) {
            if (mySlot.itemStack.isEmpty() || !mySlot.itemStack.isStackable() || mySlot.itemStack.getCount() >= mySlot.itemStack.getMaxStackSize()) continue;
            slotMap.get(mySlot.itemStack).add(mySlot);
        }
        for (Tuple tuple : slotMap.map) {
            List slotsToStack = (List)tuple.getB();
            MySlot slotToStackTo = null;
            for (int i = 0; i < slotsToStack.size(); ++i) {
                MySlot slot = (MySlot)slotsToStack.get(i);
                if (slotToStackTo == null) {
                    slotToStackTo = slot;
                    continue;
                }
                this.actions.add(new Action(slot.id, slotToStackTo.id));
                if (slotToStackTo.itemStack.getCount() + slot.itemStack.getCount() <= slotToStackTo.itemStack.getMaxStackSize()) {
                    slotToStackTo.itemStack = new ItemStack((ItemLike)slotToStackTo.itemStack.getItem(), slotToStackTo.itemStack.getCount() + slot.itemStack.getCount());
                    slot.itemStack = ItemStack.EMPTY;
                    if (slotToStackTo.itemStack.getCount() < slotToStackTo.itemStack.getMaxStackSize()) continue;
                    slotToStackTo = null;
                    continue;
                }
                int needed = slotToStackTo.itemStack.getMaxStackSize() - slotToStackTo.itemStack.getCount();
                slotToStackTo.itemStack = new ItemStack((ItemLike)slotToStackTo.itemStack.getItem(), slotToStackTo.itemStack.getMaxStackSize());
                slot.itemStack = new ItemStack((ItemLike)slot.itemStack.getItem(), slot.itemStack.getCount() - needed);
                slotToStackTo = null;
                --i;
            }
        }
    }

    private void generateSortingActions(List<MySlot> slots) {
        for (int i = 0; i < slots.size(); ++i) {
            MySlot bestSlot = null;
            for (int j = i; j < slots.size(); ++j) {
                MySlot slot = slots.get(j);
                if (bestSlot == null) {
                    bestSlot = slot;
                    continue;
                }
                if (!this.isSlotBetter(bestSlot, slot)) continue;
                bestSlot = slot;
            }
            if (bestSlot.itemStack.isEmpty()) continue;
            MySlot toSlot = slots.get(i);
            int from = bestSlot.id;
            int to = toSlot.id;
            if (from == to) continue;
            ItemStack temp = bestSlot.itemStack;
            bestSlot.itemStack = toSlot.itemStack;
            toSlot.itemStack = temp;
            this.actions.add(new Action(from, to));
        }
    }

    private boolean isSlotBetter(MySlot best, MySlot slot) {
        ItemStack bestI = best.itemStack;
        ItemStack slotI = slot.itemStack;
        if (bestI.isEmpty() && !slotI.isEmpty()) {
            return true;
        }
        if (!bestI.isEmpty() && slotI.isEmpty()) {
            return false;
        }
        int c = BuiltInRegistries.ITEM.getKey((Object)bestI.getItem()).compareTo(BuiltInRegistries.ITEM.getKey((Object)slotI.getItem()));
        if (c == 0) {
            if (slotI.getCount() != bestI.getCount()) {
                return slotI.getCount() > bestI.getCount();
            }
            if (slotI.getDamageValue() != bestI.getDamageValue()) {
                return slotI.getDamageValue() > bestI.getDamageValue();
            }
        }
        return c > 0;
    }

    private InvPart getInvPart(Slot slot) {
        int i = ((ISlot)slot).meteor$getSlot();
        if (slot.container instanceof Inventory && (!(this.screen instanceof CreativeModeInventoryScreen) || ((ISlot)slot).meteor$getIndex() > 8)) {
            if (SlotUtils.isHotbar(i)) {
                return InvPart.Hotbar;
            }
            if (SlotUtils.isMain(i)) {
                return InvPart.Player;
            }
        } else if ((this.screen instanceof ContainerScreen || this.screen instanceof ShulkerBoxScreen) && slot.container instanceof SimpleContainer) {
            return InvPart.Main;
        }
        return InvPart.Invalid;
    }

    private static enum InvPart {
        Hotbar,
        Player,
        Main,
        Invalid;

    }

    private record Action(int from, int to) {
    }

    private static class MySlot {
        public final int id;
        public ItemStack itemStack;

        public MySlot(int id, ItemStack itemStack) {
            this.id = id;
            this.itemStack = itemStack;
        }
    }

    private static class SlotMap {
        private final List<Tuple<ItemStack, List<MySlot>>> map = new ArrayList<Tuple<ItemStack, List<MySlot>>>();

        private SlotMap() {
        }

        public List<MySlot> get(ItemStack itemStack) {
            for (Tuple<ItemStack, List<MySlot>> entry : this.map) {
                if (!ItemStack.isSameItemSameComponents((ItemStack)itemStack, (ItemStack)((ItemStack)entry.getA()))) continue;
                return (List)entry.getB();
            }
            ArrayList<MySlot> list = new ArrayList<MySlot>();
            this.map.add((Tuple<ItemStack, List<MySlot>>)new Tuple((Object)itemStack, list));
            return list;
        }
    }
}
