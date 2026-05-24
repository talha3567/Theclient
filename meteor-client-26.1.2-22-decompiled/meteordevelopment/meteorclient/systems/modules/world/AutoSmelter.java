package meteordevelopment.meteorclient.systems.modules.world;

import java.util.List;
import meteordevelopment.meteorclient.mixininterface.IAbstractFurnaceMenu;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipePropertySet;

public class AutoSmelter
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<List<Item>> fuelItems;
    private final Setting<Integer> fuelItemsPerRefill;
    private final Setting<List<Item>> smeltableItems;
    private final Setting<Boolean> disableWhenOutOfItems;
    private final Setting<Boolean> autoClose;

    public AutoSmelter() {
        super(Categories.World, "auto-smelter", "Automatically smelts items from your inventory");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.fuelItems = this.sgGeneral.add(((ItemListSetting.Builder)((ItemListSetting.Builder)new ItemListSetting.Builder().name("fuel-items")).description("Items to use as fuel")).defaultValue(Items.COAL, Items.CHARCOAL).filter(this::fuelItemFilter).bypassFilterWhenSavingAndLoading().build());
        this.fuelItemsPerRefill = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("fuel-items-per-refill")).description("How many fuel items to put into the furnace each time it refills")).defaultValue(64)).range(1, 64).sliderRange(1, 16).build());
        this.smeltableItems = this.sgGeneral.add(((ItemListSetting.Builder)((ItemListSetting.Builder)new ItemListSetting.Builder().name("smeltable-items")).description("Items to smelt")).defaultValue(Items.IRON_ORE, Items.GOLD_ORE, Items.COPPER_ORE, Items.RAW_IRON, Items.RAW_COPPER, Items.RAW_GOLD).filter(this::smeltableItemFilter).bypassFilterWhenSavingAndLoading().build());
        this.disableWhenOutOfItems = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("disable-when-out-of-items")).description("Disable the module when you run out of items")).defaultValue(true)).build());
        this.autoClose = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-close")).defaultValue(false)).build());
    }

    private boolean fuelItemFilter(Item item) {
        if (!Utils.canUpdate()) {
            return false;
        }
        return this.mc.getConnection().fuelValues().fuelItems().contains(item);
    }

    private boolean smeltableItemFilter(Item item) {
        return this.mc.level != null && this.mc.level.recipeAccess().propertySet(RecipePropertySet.FURNACE_INPUT).test(item.getDefaultInstance());
    }

    public void tick(AbstractFurnaceMenu c) {
        if (this.mc.player.tickCount % 10 == 0) {
            return;
        }
        this.checkFuel(c);
        this.takeResults(c);
        this.insertItems(c);
        if (this.autoClose.get().booleanValue()) {
            this.mc.setScreen(null);
        }
    }

    private void insertItems(AbstractFurnaceMenu c) {
        ItemStack inputItemStack = ((Slot)c.slots.getFirst()).getItem();
        if (!inputItemStack.isEmpty()) {
            return;
        }
        int slot = -1;
        for (int i = 3; i < c.slots.size(); ++i) {
            ItemStack item = ((Slot)c.slots.get(i)).getItem();
            if (!((IAbstractFurnaceMenu)c).meteor$canSmelt(item) || !this.smeltableItems.get().contains(item.getItem()) || !this.smeltableItemFilter(item.getItem())) continue;
            slot = i;
            break;
        }
        if (this.disableWhenOutOfItems.get().booleanValue() && slot == -1) {
            this.error("You do not have any items in your inventory that can be smelted. Disabling.", new Object[0]);
            this.toggle();
            return;
        }
        if (slot == -1) {
            return;
        }
        InvUtils.move().fromId(slot).toId(0);
        ((Slot)c.slots.getFirst()).getItem().isEmpty();
    }

    private void checkFuel(AbstractFurnaceMenu c) {
        ItemStack fuelStack = ((Slot)c.slots.get(1)).getItem();
        if (c.getLitProgress() > 0.0f) {
            return;
        }
        if (!fuelStack.isEmpty()) {
            return;
        }
        int slot = -1;
        for (int i = 3; i < c.slots.size(); ++i) {
            ItemStack item = ((Slot)c.slots.get(i)).getItem();
            if (!this.fuelItems.get().contains(item.getItem()) || !this.fuelItemFilter(item.getItem())) continue;
            slot = i;
            break;
        }
        if (this.disableWhenOutOfItems.get().booleanValue() && slot == -1) {
            this.error("You do not have any fuel in your inventory. Disabling.", new Object[0]);
            this.toggle();
            return;
        }
        if (slot == -1) {
            return;
        }
        ItemStack sourceStack = ((Slot)c.slots.get(slot)).getItem();
        int moveCount = Math.min(this.fuelItemsPerRefill.get(), Math.min(sourceStack.getCount(), ((Slot)c.slots.get(1)).getMaxStackSize(sourceStack)));
        if (moveCount <= 0) {
            return;
        }
        this.moveFuelItems(c, slot, moveCount);
    }

    private void moveFuelItems(AbstractFurnaceMenu c, int fromId, int amount) {
        if (amount <= 0 || this.mc.player == null || this.mc.gameMode == null) {
            return;
        }
        if (!this.mc.player.containerMenu.getCarried().isEmpty()) {
            return;
        }
        this.mc.gameMode.handleContainerInput(c.containerId, fromId, 0, ContainerInput.PICKUP, (Player)this.mc.player);
        for (int i = 0; i < amount && !this.mc.player.containerMenu.getCarried().isEmpty(); ++i) {
            this.mc.gameMode.handleContainerInput(c.containerId, 1, 1, ContainerInput.PICKUP, (Player)this.mc.player);
        }
        if (!this.mc.player.containerMenu.getCarried().isEmpty()) {
            this.mc.gameMode.handleContainerInput(c.containerId, fromId, 0, ContainerInput.PICKUP, (Player)this.mc.player);
        }
        ((Slot)c.slots.get(1)).getItem().isEmpty();
    }

    private void takeResults(AbstractFurnaceMenu c) {
        ItemStack resultStack = ((Slot)c.slots.get(2)).getItem();
        if (resultStack.isEmpty()) {
            return;
        }
        InvUtils.shiftClick().slotId(2);
        if (!resultStack.isEmpty()) {
            this.error("Your inventory is full. Disabling.", new Object[0]);
            this.toggle();
        }
    }
}
