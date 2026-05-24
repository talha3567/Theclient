package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;

public class ChestSwap
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Chestplate> chestplate;
    private final Setting<Boolean> stayOn;
    private final Setting<Boolean> closeInventory;

    public ChestSwap() {
        super(Categories.Player, "chest-swap", "Automatically swaps between a chestplate and an elytra.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.chestplate = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("chestplate")).description("Which type of chestplate to swap to.")).defaultValue(Chestplate.PreferNetherite)).build());
        this.stayOn = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("stay-on")).description("Stays on and activates when you turn it off.")).defaultValue(false)).build());
        this.closeInventory = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("close-inventory")).description("Sends inventory close after swap.")).defaultValue(true)).build());
    }

    @Override
    public void onActivate() {
        this.swap();
        if (!this.stayOn.get().booleanValue()) {
            this.toggle();
        }
    }

    @Override
    public void onDeactivate() {
        if (this.stayOn.get().booleanValue()) {
            this.swap();
        }
    }

    public void swap() {
        ItemStack currentItem = this.mc.player.getItemBySlot(EquipmentSlot.CHEST);
        if (currentItem.has(DataComponents.GLIDER)) {
            this.equipChestplate();
        } else if (currentItem.has(DataComponents.EQUIPPABLE) && ((Equippable)currentItem.get(DataComponents.EQUIPPABLE)).slot().getIndex() == EquipmentSlot.CHEST.getIndex()) {
            this.equipElytra();
        } else if (!this.equipChestplate()) {
            this.equipElytra();
        }
    }

    private boolean equipChestplate() {
        int bestSlot = -1;
        boolean breakLoop = false;
        for (int i = 0; i < this.mc.player.getInventory().getNonEquipmentItems().size(); ++i) {
            Item item = ((ItemStack)this.mc.player.getInventory().getNonEquipmentItems().get(i)).getItem();
            switch (this.chestplate.get().ordinal()) {
                case 0: {
                    if (item != Items.DIAMOND_CHESTPLATE) break;
                    bestSlot = i;
                    breakLoop = true;
                    break;
                }
                case 1: {
                    if (item != Items.NETHERITE_CHESTPLATE) break;
                    bestSlot = i;
                    breakLoop = true;
                    break;
                }
                case 2: {
                    if (item == Items.DIAMOND_CHESTPLATE) {
                        bestSlot = i;
                        breakLoop = true;
                        break;
                    }
                    if (item != Items.NETHERITE_CHESTPLATE) break;
                    bestSlot = i;
                    break;
                }
                case 3: {
                    if (item == Items.DIAMOND_CHESTPLATE) {
                        bestSlot = i;
                        break;
                    }
                    if (item != Items.NETHERITE_CHESTPLATE) break;
                    bestSlot = i;
                    breakLoop = true;
                }
            }
            if (breakLoop) break;
        }
        if (bestSlot != -1) {
            this.equip(bestSlot);
        }
        return bestSlot != -1;
    }

    private void equipElytra() {
        for (int i = 0; i < this.mc.player.getInventory().getNonEquipmentItems().size(); ++i) {
            ItemStack item = (ItemStack)this.mc.player.getInventory().getNonEquipmentItems().get(i);
            if (!item.has(DataComponents.GLIDER)) continue;
            this.equip(i);
            break;
        }
    }

    private void equip(int slot) {
        InvUtils.move().from(slot).toArmor(2);
        if (this.closeInventory.get().booleanValue()) {
            this.mc.getConnection().send((Packet)new ServerboundContainerClosePacket(0));
        }
    }

    @Override
    public void sendToggledMsg() {
        if (this.stayOn.get().booleanValue()) {
            super.sendToggledMsg();
        } else if (Config.get().chatFeedback.get().booleanValue() && this.chatFeedback) {
            this.info("Triggered (highlight)%s(default).", this.title);
        }
    }

    public static enum Chestplate {
        Diamond,
        Netherite,
        PreferDiamond,
        PreferNetherite;

    }
}
