package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Optional;
import net.minecraft.class_1304;
import net.minecraft.class_1661;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1890;
import net.minecraft.class_1893;
import net.minecraft.class_2378;
import net.minecraft.class_2813;
import net.minecraft.class_465;
import net.minecraft.class_490;
import net.minecraft.class_5455;
import net.minecraft.class_6880;
import net.minecraft.class_746;
import net.minecraft.class_7924;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.WurstClient;
import net.wurstclient.events.PacketOutputListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.InventoryUtils;
import net.wurstclient.util.ItemUtils;

@SearchTags(value={"auto armor"})
public final class AutoArmorHack
extends Hack
implements UpdateListener,
PacketOutputListener {
    private final CheckboxSetting useEnchantments = new CheckboxSetting("Use enchantments", "Whether or not to consider the Protection enchantment when calculating armor strength.", true);
    private final CheckboxSetting swapWhileMoving = new CheckboxSetting("Swap while moving", "Whether or not to swap armor pieces while the player is moving.\n\n\u00a7c\u00a7lWARNING:\u00a7r This would not be possible without cheats. It may raise suspicion.", false);
    private final SliderSetting delay = new SliderSetting("Delay", "Amount of ticks to wait before swapping the next piece of armor.", 2.0, 0.0, 20.0, 1.0, SliderSetting.ValueDisplay.INTEGER);
    private int timer;

    public AutoArmorHack() {
        super("AutoArmor");
        this.setCategory(Category.COMBAT);
        this.addSetting(this.useEnchantments);
        this.addSetting(this.swapWhileMoving);
        this.addSetting(this.delay);
    }

    @Override
    protected void onEnable() {
        this.timer = 0;
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(PacketOutputListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(PacketOutputListener.class, this);
    }

    @Override
    public void onUpdate() {
        if (this.timer > 0) {
            --this.timer;
            return;
        }
        if (AutoArmorHack.MC.field_1755 instanceof class_465 && !(AutoArmorHack.MC.field_1755 instanceof class_490)) {
            return;
        }
        class_746 player = AutoArmorHack.MC.field_1724;
        class_1661 inventory = player.method_31548();
        if (!this.swapWhileMoving.isChecked() && player.field_3913.method_3128().method_35584() > 1.0E-5f) {
            return;
        }
        EnumMap<class_1304, ArmorData> bestArmor = new EnumMap<class_1304, ArmorData>(class_1304.class);
        ArrayList<class_1304> armorTypes = new ArrayList<class_1304>(Arrays.asList(class_1304.field_6166, class_1304.field_6172, class_1304.field_6174, class_1304.field_6169));
        for (class_1304 type : armorTypes) {
            bestArmor.put(type, new ArmorData(-1, 0));
            class_1799 stack = player.method_6118(type);
            if (!AutoArmorHack.MC.field_1724.method_63623(stack, type)) continue;
            bestArmor.put(type, new ArmorData(-1, this.getArmorValue(stack)));
        }
        for (int slot = 0; slot < 36; ++slot) {
            class_1799 stack = inventory.method_5438(slot);
            class_1304 armorType = ItemUtils.getArmorSlot(stack.method_7909());
            if (armorType == null) continue;
            int armorValue = this.getArmorValue(stack);
            ArmorData data = (ArmorData)bestArmor.get(armorType);
            if (data != null && armorValue <= data.armorValue()) continue;
            bestArmor.put(armorType, new ArmorData(slot, armorValue));
        }
        Collections.shuffle(armorTypes);
        for (class_1304 type : armorTypes) {
            class_1799 oldArmor;
            ArmorData data = (ArmorData)bestArmor.get(type);
            if (data == null || data.invSlot() == -1 || !(oldArmor = player.method_6118(type)).method_7960() && inventory.method_7376() == -1) continue;
            if (!oldArmor.method_7960()) {
                IMC.getInteractionManager().windowClick_QUICK_MOVE(8 - type.method_5927());
            }
            IMC.getInteractionManager().windowClick_QUICK_MOVE(InventoryUtils.toNetworkSlot(data.invSlot()));
            break;
        }
    }

    @Override
    public void onSentPacket(PacketOutputListener.PacketOutputEvent event) {
        if (event.getPacket() instanceof class_2813) {
            this.timer = this.delay.getValueI();
        }
    }

    private int getArmorValue(class_1799 stack) {
        class_1792 item = stack.method_7909();
        int armorPoints = (int)ItemUtils.getArmorPoints(item);
        int prtPoints = 0;
        int armorToughness = (int)ItemUtils.getToughness(item);
        if (this.useEnchantments.isChecked()) {
            class_5455 drm = WurstClient.MC.field_1687.method_30349();
            class_2378 registry = drm.method_30530(class_7924.field_41265);
            Optional protection = registry.method_46746(class_1893.field_9111);
            prtPoints = protection.map(entry -> class_1890.method_8225((class_6880)entry, (class_1799)stack)).orElse(0);
        }
        return armorPoints * 5 + prtPoints * 3 + armorToughness;
    }

    private record ArmorData(int invSlot, int armorValue) {
    }
}
