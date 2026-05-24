package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.class_1661;
import net.minecraft.class_1799;
import net.minecraft.class_465;
import net.minecraft.class_7923;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.mixinterface.IClientPlayerInteractionManager;
import net.wurstclient.settings.ItemListSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.InventoryUtils;

@SearchTags(value={"AutoRestock", "auto-restock", "auto restock"})
public final class RestockHack
extends Hack
implements UpdateListener {
    public static final int OFFHAND_ID = 40;
    public static final int OFFHAND_PKT_ID = 45;
    private static final List<Integer> SEARCH_SLOTS = Stream.concat(IntStream.range(0, 36).boxed(), Stream.of(Integer.valueOf(40))).collect(Collectors.toCollection(ArrayList::new));
    private ItemListSetting items = new ItemListSetting("Items", "Item(s) to be restocked.", "minecraft:minecart");
    private final SliderSetting restockSlot = new SliderSetting("Slot", "To which slot should we restock.", 0.0, -1.0, 9.0, 1.0, SliderSetting.ValueDisplay.INTEGER.withLabel(9.0, "offhand").withLabel(-1.0, "current"));
    private final SliderSetting restockAmount = new SliderSetting("Minimum amount", "Minimum amount of items in hand before a new round of restocking is triggered.", 1.0, 1.0, 64.0, 1.0, SliderSetting.ValueDisplay.INTEGER);
    private final SliderSetting repairMode = new SliderSetting("Tools repair mode", "Swaps out tools when their durability reaches the given threshold, so you can repair them before they break.\nCan be adjusted from 0 (off) to 100 remaining uses.", 0.0, 0.0, 100.0, 1.0, SliderSetting.ValueDisplay.INTEGER.withLabel(0.0, "off"));

    public RestockHack() {
        super("Restock");
        this.setCategory(Category.ITEMS);
        this.addSetting(this.items);
        this.addSetting(this.restockSlot);
        this.addSetting(this.restockAmount);
        this.addSetting(this.repairMode);
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
        if (RestockHack.MC.field_1755 instanceof class_465) {
            return;
        }
        class_1661 inv = RestockHack.MC.field_1724.method_31548();
        IClientPlayerInteractionManager im = IMC.getInteractionManager();
        int hotbarSlot = this.restockSlot.getValueI();
        if (hotbarSlot == -1) {
            hotbarSlot = inv.method_67532();
        } else if (hotbarSlot == 9) {
            hotbarSlot = 40;
        }
        for (String itemName : this.items.getItemNames()) {
            boolean wrongItem;
            class_1799 hotbarStack = inv.method_5438(hotbarSlot);
            boolean bl = wrongItem = hotbarStack.method_7960() || !this.itemEqual(itemName, hotbarStack);
            if (!wrongItem && hotbarStack.method_7947() >= Math.min(this.restockAmount.getValueI(), hotbarStack.method_7914())) {
                return;
            }
            List<Integer> searchResult = this.searchSlotsWithItem(itemName, hotbarSlot);
            for (int itemIndex : searchResult) {
                int pickupIndex = InventoryUtils.toNetworkSlot(itemIndex);
                im.windowClick_PICKUP(pickupIndex);
                im.windowClick_PICKUP(InventoryUtils.toNetworkSlot(hotbarSlot));
                if (!RestockHack.MC.field_1724.field_7498.method_34255().method_7960()) {
                    im.windowClick_PICKUP(pickupIndex);
                }
                if (hotbarStack.method_7947() < hotbarStack.method_7914()) continue;
                break;
            }
            if (wrongItem && searchResult.isEmpty()) continue;
        }
        class_1799 restockStack = inv.method_5438(hotbarSlot);
        if (this.repairMode.getValueI() > 0 && restockStack.method_7963() && this.isTooDamaged(restockStack)) {
            for (int i : SEARCH_SLOTS) {
                class_1799 stack;
                if (i == hotbarSlot || i == 40 || !(stack = inv.method_5438(i)).method_7960() && stack.method_7963()) continue;
                IMC.getInteractionManager().windowClick_SWAP(i, InventoryUtils.toNetworkSlot(hotbarSlot));
                break;
            }
        }
    }

    private boolean isTooDamaged(class_1799 stack) {
        return stack.method_7936() - stack.method_7919() <= this.repairMode.getValueI();
    }

    private List<Integer> searchSlotsWithItem(String itemName, int slotToSkip) {
        ArrayList<Integer> slots = new ArrayList<Integer>();
        for (int i : SEARCH_SLOTS) {
            class_1799 stack;
            if (i == slotToSkip || (stack = RestockHack.MC.field_1724.method_31548().method_5438(i)).method_7960() || !this.itemEqual(itemName, stack)) continue;
            slots.add(i);
        }
        return slots;
    }

    private boolean itemEqual(String itemName, class_1799 stack) {
        if (this.repairMode.getValueI() > 0 && stack.method_7963() && this.isTooDamaged(stack)) {
            return false;
        }
        return class_7923.field_41178.method_10221((Object)stack.method_7909()).toString().equals(itemName);
    }
}
