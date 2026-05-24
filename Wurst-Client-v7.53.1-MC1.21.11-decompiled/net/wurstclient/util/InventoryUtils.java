package net.wurstclient.util;

import java.util.function.Predicate;
import java.util.stream.IntStream;
import net.minecraft.class_1661;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1935;
import net.minecraft.class_2596;
import net.minecraft.class_2873;
import net.minecraft.class_310;
import net.wurstclient.WurstClient;
import net.wurstclient.mixinterface.IClientPlayerInteractionManager;
import net.wurstclient.mixinterface.IMinecraftClient;

public final class InventoryUtils
extends Enum<InventoryUtils> {
    private static final class_310 MC;
    private static final IMinecraftClient IMC;
    private static final /* synthetic */ InventoryUtils[] $VALUES;

    public static InventoryUtils[] values() {
        return (InventoryUtils[])$VALUES.clone();
    }

    public static InventoryUtils valueOf(String name) {
        return Enum.valueOf(InventoryUtils.class, name);
    }

    public static int indexOf(class_1792 item) {
        return InventoryUtils.indexOf((class_1799 stack) -> stack.method_31574(item), 36, false);
    }

    public static int indexOf(class_1792 item, int maxInvSlot) {
        return InventoryUtils.indexOf((class_1799 stack) -> stack.method_31574(item), maxInvSlot, false);
    }

    public static int indexOf(class_1792 item, int maxInvSlot, boolean includeOffhand) {
        return InventoryUtils.indexOf((class_1799 stack) -> stack.method_31574(item), maxInvSlot, includeOffhand);
    }

    public static int indexOf(Predicate<class_1799> predicate) {
        return InventoryUtils.indexOf(predicate, 36, false);
    }

    public static int indexOf(Predicate<class_1799> predicate, int maxInvSlot) {
        return InventoryUtils.indexOf(predicate, maxInvSlot, false);
    }

    public static int indexOf(Predicate<class_1799> predicate, int maxInvSlot, boolean includeOffhand) {
        return InventoryUtils.getMatchingSlots(predicate, maxInvSlot, includeOffhand).findFirst().orElse(-1);
    }

    public static int count(class_1792 item) {
        return InventoryUtils.count((class_1799 stack) -> stack.method_31574(item), 36, false);
    }

    public static int count(class_1792 item, int maxInvSlot) {
        return InventoryUtils.count((class_1799 stack) -> stack.method_31574(item), maxInvSlot, false);
    }

    public static int count(class_1792 item, int maxInvSlot, boolean includeOffhand) {
        return InventoryUtils.count((class_1799 stack) -> stack.method_31574(item), maxInvSlot, includeOffhand);
    }

    public static int count(Predicate<class_1799> predicate) {
        return InventoryUtils.count(predicate, 36, false);
    }

    public static int count(Predicate<class_1799> predicate, int maxInvSlot) {
        return InventoryUtils.count(predicate, maxInvSlot, false);
    }

    public static int count(Predicate<class_1799> predicate, int maxInvSlot, boolean includeOffhand) {
        class_1661 inventory = InventoryUtils.MC.field_1724.method_31548();
        return InventoryUtils.getMatchingSlots(predicate, maxInvSlot, includeOffhand).map(slot -> inventory.method_5438(slot).method_7947()).sum();
    }

    private static IntStream getMatchingSlots(Predicate<class_1799> predicate, int maxInvSlot, boolean includeOffhand) {
        class_1661 inventory = InventoryUtils.MC.field_1724.method_31548();
        IntStream stream = IntStream.range(0, maxInvSlot);
        if (includeOffhand) {
            stream = IntStream.concat(stream, IntStream.of(40));
        }
        return stream.filter(i -> predicate.test(inventory.method_5438(i)));
    }

    public static boolean selectItem(class_1792 item) {
        return InventoryUtils.selectItem((class_1799 stack) -> stack.method_31574(item), 36, false);
    }

    public static boolean selectItem(class_1792 item, int maxInvSlot) {
        return InventoryUtils.selectItem((class_1799 stack) -> stack.method_31574(item), maxInvSlot, false);
    }

    public static boolean selectItem(class_1792 item, int maxInvSlot, boolean takeFromOffhand) {
        return InventoryUtils.selectItem((class_1799 stack) -> stack.method_31574(item), maxInvSlot, takeFromOffhand);
    }

    public static boolean selectItem(Predicate<class_1799> predicate) {
        return InventoryUtils.selectItem(predicate, 36, false);
    }

    public static boolean selectItem(Predicate<class_1799> predicate, int maxInvSlot) {
        return InventoryUtils.selectItem(predicate, maxInvSlot, false);
    }

    public static boolean selectItem(Predicate<class_1799> predicate, int maxInvSlot, boolean takeFromOffhand) {
        return InventoryUtils.selectItem(InventoryUtils.indexOf(predicate, maxInvSlot, takeFromOffhand));
    }

    public static boolean selectItem(int slot) {
        class_1661 inventory = InventoryUtils.MC.field_1724.method_31548();
        IClientPlayerInteractionManager im = IMC.getInteractionManager();
        if (slot < 0) {
            return false;
        }
        if (slot < 9) {
            inventory.method_61496(slot);
        } else if (inventory.method_7376() > -1 && inventory.method_7376() < 9) {
            im.windowClick_QUICK_MOVE(InventoryUtils.toNetworkSlot(slot));
        } else {
            im.windowClick_SWAP(InventoryUtils.toNetworkSlot(slot), inventory.method_67532());
        }
        return true;
    }

    public static int toNetworkSlot(int slot) {
        if (slot >= 0 && slot < 9) {
            return slot + 36;
        }
        if (slot >= 36 && slot < 40) {
            return 44 - slot;
        }
        if (slot == 40) {
            return 45;
        }
        return slot;
    }

    public static boolean giveCreativeItem(class_1792 item) {
        return InventoryUtils.giveCreativeItem(new class_1799((class_1935)item));
    }

    public static boolean giveCreativeItem(class_1799 stack) {
        return InventoryUtils.setCreativeStack(InventoryUtils.MC.field_1724.method_31548().method_7376(), stack);
    }

    public static boolean setCreativeStack(int slot, class_1792 item) {
        return InventoryUtils.setCreativeStack(slot, new class_1799((class_1935)item));
    }

    public static boolean setCreativeStack(int slot, class_1799 stack) {
        if (slot < 0) {
            return false;
        }
        if (!InventoryUtils.MC.field_1724.method_56992()) {
            return false;
        }
        InventoryUtils.MC.field_1724.method_31548().method_5447(slot, stack);
        InventoryUtils.MC.field_1724.field_3944.method_52787((class_2596)new class_2873(InventoryUtils.toNetworkSlot(slot), stack));
        return true;
    }

    private static /* synthetic */ InventoryUtils[] $values() {
        return new InventoryUtils[0];
    }

    static {
        $VALUES = InventoryUtils.$values();
        MC = WurstClient.MC;
        IMC = WurstClient.IMC;
    }
}
