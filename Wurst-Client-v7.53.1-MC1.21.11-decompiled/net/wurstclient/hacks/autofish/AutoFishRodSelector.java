package net.wurstclient.hacks.autofish;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.class_1661;
import net.minecraft.class_1787;
import net.minecraft.class_1799;
import net.minecraft.class_1890;
import net.minecraft.class_1893;
import net.minecraft.class_2378;
import net.minecraft.class_310;
import net.minecraft.class_5455;
import net.minecraft.class_6880;
import net.minecraft.class_7924;
import net.minecraft.class_9331;
import net.minecraft.class_9701;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.AutoFishHack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.InventoryUtils;

public final class AutoFishRodSelector {
    private static final class_310 MC = WurstClient.MC;
    private final CheckboxSetting stopWhenOutOfRods = new CheckboxSetting("Stop when out of rods", "If enabled, AutoFish will turn itself off when it runs out of fishing rods.", false);
    private final CheckboxSetting stopWhenInvFull = new CheckboxSetting("Stop when inv full", "If enabled, AutoFish will turn itself off when your inventory is full.", false);
    private final AutoFishHack autoFish;
    private int bestRodSlot;

    public AutoFishRodSelector(AutoFishHack autoFish) {
        this.autoFish = autoFish;
    }

    public Stream<Setting> getSettings() {
        return Stream.of(this.stopWhenOutOfRods, this.stopWhenInvFull);
    }

    public void reset() {
        this.bestRodSlot = -1;
    }

    public boolean isOutOfRods() {
        return this.bestRodSlot == -1;
    }

    public boolean update() {
        int selectedSlot;
        class_1661 inventory = AutoFishRodSelector.MC.field_1724.method_31548();
        class_1799 selectedStack = inventory.method_5438(selectedSlot = inventory.method_67532());
        int bestRodValue = this.getRodValue(selectedStack);
        this.bestRodSlot = bestRodValue > -1 ? selectedSlot : -1;
        IntStream stream = IntStream.range(0, 36);
        stream = IntStream.concat(stream, IntStream.of(40));
        for (int slot : stream.toArray()) {
            class_1799 stack = inventory.method_5438(slot);
            int rodValue = this.getRodValue(stack);
            if (rodValue <= bestRodValue) continue;
            bestRodValue = rodValue;
            this.bestRodSlot = slot;
        }
        if (WurstClient.INSTANCE.getHax().autoEatHack.isEating()) {
            return false;
        }
        if (this.stopWhenOutOfRods.isChecked() && this.bestRodSlot == -1) {
            ChatUtils.message("AutoFish has run out of fishing rods.");
            this.autoFish.setEnabled(false);
            return false;
        }
        if (this.stopWhenInvFull.isChecked() && inventory.method_7376() == -1) {
            ChatUtils.message("AutoFish has stopped because your inventory is full.");
            this.autoFish.setEnabled(false);
            return false;
        }
        if (selectedSlot == this.bestRodSlot) {
            return true;
        }
        InventoryUtils.selectItem(this.bestRodSlot);
        return false;
    }

    private int getRodValue(class_1799 stack) {
        if (stack.method_7960() || !(stack.method_7909() instanceof class_1787)) {
            return -1;
        }
        class_5455 drm = AutoFishRodSelector.MC.field_1687.method_30349();
        class_2378 registry = drm.method_30530(class_7924.field_41265);
        Optional luckOTS = registry.method_46746(class_1893.field_9114);
        int luckOTSLvl = luckOTS.map(entry -> class_1890.method_8225((class_6880)entry, (class_1799)stack)).orElse(0);
        Optional lure = registry.method_46746(class_1893.field_9100);
        int lureLvl = lure.map(entry -> class_1890.method_8225((class_6880)entry, (class_1799)stack)).orElse(0);
        Optional unbreaking = registry.method_46746(class_1893.field_9119);
        int unbreakingLvl = unbreaking.map(entry -> class_1890.method_8225((class_6880)entry, (class_1799)stack)).orElse(0);
        Optional mending = registry.method_46746(class_1893.field_9101);
        int mendingBonus = mending.map(entry -> class_1890.method_8225((class_6880)entry, (class_1799)stack)).orElse(0);
        int noVanishBonus = class_1890.method_60142((class_1799)stack, (class_9331)class_9701.field_51655) ? 0 : 1;
        return luckOTSLvl * 9 + lureLvl * 9 + unbreakingLvl * 2 + mendingBonus + noVanishBonus;
    }
}
