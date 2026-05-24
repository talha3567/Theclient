package meteordevelopment.meteorclient.utils.player;

import java.lang.runtime.SwitchBootstraps;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixin.AbstractMountInventoryMenuAccessor;
import meteordevelopment.meteorclient.mixin.CreativeModeInventoryScreenAccessor;
import meteordevelopment.meteorclient.mixin.CreativeModeTabsAccessor;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.BlastFurnaceMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraft.world.inventory.StonecutterMenu;

public class SlotUtils {
    public static final int HOTBAR_START = 0;
    public static final int HOTBAR_END = 8;
    public static final int MAIN_START = 9;
    public static final int MAIN_END = 35;
    public static final int ARMOR_START = 36;
    public static final int ARMOR_END = 39;
    public static final int OFFHAND = 40;

    private SlotUtils() {
    }

    public static int indexToId(int i) {
        int n;
        AbstractContainerMenu handler;
        if (MeteorClient.mc.player == null) {
            return -1;
        }
        AbstractContainerMenu abstractContainerMenu = handler = MeteorClient.mc.player.containerMenu;
        Objects.requireNonNull(abstractContainerMenu);
        AbstractContainerMenu abstractContainerMenu2 = abstractContainerMenu;
        int n2 = 0;
        block23: while (true) {
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{InventoryMenu.class, CreativeModeInventoryScreen.ItemPickerMenu.class, ChestMenu.class, CraftingMenu.class, FurnaceMenu.class, BlastFurnaceMenu.class, SmokerMenu.class, DispenserMenu.class, EnchantmentMenu.class, BrewingStandMenu.class, MerchantMenu.class, BeaconMenu.class, AnvilMenu.class, HopperMenu.class, ShulkerBoxMenu.class, HorseInventoryMenu.class, CartographyTableMenu.class, GrindstoneMenu.class, LecternMenu.class, LoomMenu.class, StonecutterMenu.class, CrafterMenu.class, SmithingMenu.class}, (AbstractContainerMenu)abstractContainerMenu2, n2)) {
                case 0: {
                    n = SlotUtils.survivalInventory(i);
                    break block23;
                }
                case 1: {
                    n = SlotUtils.creativeInventory(i);
                    break block23;
                }
                case 2: {
                    ChestMenu chestMenu = (ChestMenu)abstractContainerMenu2;
                    n = SlotUtils.genericContainer(i, chestMenu.getRowCount());
                    break block23;
                }
                case 3: {
                    n = SlotUtils.craftingTable(i);
                    break block23;
                }
                case 4: 
                case 5: 
                case 6: {
                    if (!(abstractContainerMenu2 instanceof FurnaceMenu || abstractContainerMenu2 instanceof BlastFurnaceMenu || abstractContainerMenu2 instanceof SmokerMenu)) {
                        n2 = 7;
                        continue block23;
                    }
                    n = SlotUtils.furnace(i);
                    break block23;
                }
                case 7: {
                    n = SlotUtils.generic3x3(i);
                    break block23;
                }
                case 8: {
                    n = SlotUtils.enchantmentTable(i);
                    break block23;
                }
                case 9: {
                    n = SlotUtils.brewingStand(i);
                    break block23;
                }
                case 10: {
                    n = SlotUtils.villager(i);
                    break block23;
                }
                case 11: {
                    n = SlotUtils.beacon(i);
                    break block23;
                }
                case 12: {
                    n = SlotUtils.anvil(i);
                    break block23;
                }
                case 13: {
                    n = SlotUtils.hopper(i);
                    break block23;
                }
                case 14: {
                    n = SlotUtils.genericContainer(i, 3);
                    break block23;
                }
                case 15: {
                    n = SlotUtils.horse(handler, i);
                    break block23;
                }
                case 16: {
                    n = SlotUtils.cartographyTable(i);
                    break block23;
                }
                case 17: {
                    n = SlotUtils.grindstone(i);
                    break block23;
                }
                case 18: {
                    n = SlotUtils.lectern();
                    break block23;
                }
                case 19: {
                    n = SlotUtils.loom(i);
                    break block23;
                }
                case 20: {
                    n = SlotUtils.stonecutter(i);
                    break block23;
                }
                case 21: {
                    n = SlotUtils.crafter(i);
                    break block23;
                }
                case 22: {
                    n = SlotUtils.smithingTable(i);
                    break block23;
                }
                default: {
                    n = -1;
                    break block23;
                }
            }
            break;
        }
        return n;
    }

    private static int survivalInventory(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 36 + i;
        }
        if (SlotUtils.isArmor(i)) {
            return 5 + (i - 36);
        }
        if (i == 40) {
            return 45;
        }
        return i;
    }

    private static int creativeInventory(int i) {
        if (CreativeModeInventoryScreenAccessor.meteor$getSelectedTab() != BuiltInRegistries.CREATIVE_MODE_TAB.getValue(CreativeModeTabsAccessor.meteor$getInventory())) {
            return -1;
        }
        return SlotUtils.survivalInventory(i);
    }

    private static int genericContainer(int i, int rows) {
        if (SlotUtils.isHotbar(i)) {
            return (rows + 3) * 9 + i;
        }
        if (SlotUtils.isMain(i)) {
            return rows * 9 + (i - 9);
        }
        return -1;
    }

    private static int craftingTable(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 37 + i;
        }
        if (SlotUtils.isMain(i)) {
            return i + 1;
        }
        return -1;
    }

    private static int furnace(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 30 + i;
        }
        if (SlotUtils.isMain(i)) {
            return 3 + (i - 9);
        }
        return -1;
    }

    private static int generic3x3(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 36 + i;
        }
        if (SlotUtils.isMain(i)) {
            return i;
        }
        return -1;
    }

    private static int enchantmentTable(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 29 + i;
        }
        if (SlotUtils.isMain(i)) {
            return 2 + (i - 9);
        }
        return -1;
    }

    private static int brewingStand(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 32 + i;
        }
        if (SlotUtils.isMain(i)) {
            return 5 + (i - 9);
        }
        return -1;
    }

    private static int villager(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 30 + i;
        }
        if (SlotUtils.isMain(i)) {
            return 3 + (i - 9);
        }
        return -1;
    }

    private static int beacon(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 28 + i;
        }
        if (SlotUtils.isMain(i)) {
            return 1 + (i - 9);
        }
        return -1;
    }

    private static int anvil(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 30 + i;
        }
        if (SlotUtils.isMain(i)) {
            return 3 + (i - 9);
        }
        return -1;
    }

    private static int hopper(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 32 + i;
        }
        if (SlotUtils.isMain(i)) {
            return 5 + (i - 9);
        }
        return -1;
    }

    private static int horse(AbstractContainerMenu handler, int i) {
        LivingEntity entity = ((AbstractMountInventoryMenuAccessor)handler).meteor$getMount();
        if (entity instanceof Llama) {
            Llama llamaEntity = (Llama)entity;
            int strength = llamaEntity.getStrength();
            if (SlotUtils.isHotbar(i)) {
                return 2 + 3 * strength + 28 + i;
            }
            if (SlotUtils.isMain(i)) {
                return 2 + 3 * strength + 1 + (i - 9);
            }
        } else if (entity instanceof Horse || entity instanceof SkeletonHorse || entity instanceof ZombieHorse || entity instanceof Camel) {
            if (SlotUtils.isHotbar(i)) {
                return 29 + i;
            }
            if (SlotUtils.isMain(i)) {
                return 2 + (i - 9);
            }
        } else if (entity instanceof AbstractChestedHorse) {
            AbstractChestedHorse abstractDonkeyEntity = (AbstractChestedHorse)entity;
            boolean chest = abstractDonkeyEntity.hasChest();
            if (SlotUtils.isHotbar(i)) {
                return (chest ? 44 : 29) + i;
            }
            if (SlotUtils.isMain(i)) {
                return (chest ? 17 : 2) + (i - 9);
            }
        }
        return -1;
    }

    private static int cartographyTable(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 30 + i;
        }
        if (SlotUtils.isMain(i)) {
            return 3 + (i - 9);
        }
        return -1;
    }

    private static int grindstone(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 30 + i;
        }
        if (SlotUtils.isMain(i)) {
            return 3 + (i - 9);
        }
        return -1;
    }

    private static int lectern() {
        return -1;
    }

    private static int loom(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 31 + i;
        }
        if (SlotUtils.isMain(i)) {
            return 4 + (i - 9);
        }
        return -1;
    }

    private static int stonecutter(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 29 + i;
        }
        if (SlotUtils.isMain(i)) {
            return 2 + (i - 9);
        }
        return -1;
    }

    private static int crafter(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 36 + i;
        }
        if (SlotUtils.isMain(i)) {
            return i;
        }
        return -1;
    }

    private static int smithingTable(int i) {
        if (SlotUtils.isHotbar(i)) {
            return 31 + i;
        }
        if (SlotUtils.isMain(i)) {
            return 4 + (i - 9);
        }
        return -1;
    }

    public static boolean isHotbar(int slotIndex) {
        return slotIndex >= 0 && slotIndex <= 8;
    }

    public static boolean isMain(int slotIndex) {
        return slotIndex >= 9 && slotIndex <= 35;
    }

    public static boolean isArmor(int slotIndex) {
        return slotIndex >= 36 && slotIndex <= 39;
    }
}
