package net.wurstclient.hacks;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import net.minecraft.class_1661;
import net.minecraft.class_1799;
import net.minecraft.class_1890;
import net.minecraft.class_1893;
import net.minecraft.class_2338;
import net.minecraft.class_2378;
import net.minecraft.class_239;
import net.minecraft.class_2680;
import net.minecraft.class_3489;
import net.minecraft.class_5455;
import net.minecraft.class_6880;
import net.minecraft.class_746;
import net.minecraft.class_7924;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.WurstClient;
import net.wurstclient.events.BlockBreakingProgressListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.mixinterface.IClientPlayerInteractionManager;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.InventoryUtils;

@SearchTags(value={"auto tool", "AutoSwitch", "auto switch"})
public final class AutoToolHack
extends Hack
implements BlockBreakingProgressListener,
UpdateListener {
    private final CheckboxSetting useSwords = new CheckboxSetting("Use swords", "Uses swords to break leaves, cobwebs, etc.", false);
    private final CheckboxSetting useHands = new CheckboxSetting("Use hands", "Uses an empty hand or a non-damageable item when no applicable tool is found.", true);
    private final SliderSetting repairMode = new SliderSetting("Repair mode", "Prevents tools from being used when their durability reaches the given threshold, so you can repair them before they break.\nCan be adjusted from 0 (off) to 100 remaining uses.", 0.0, 0.0, 100.0, 1.0, SliderSetting.ValueDisplay.INTEGER.withLabel(0.0, "off"));
    private final CheckboxSetting switchBack = new CheckboxSetting("Switch back", "After using a tool, automatically switches back to the previously selected slot.", false);
    private int prevSelectedSlot;

    public AutoToolHack() {
        super("AutoTool");
        this.setCategory(Category.BLOCKS);
        this.addSetting(this.useSwords);
        this.addSetting(this.useHands);
        this.addSetting(this.repairMode);
        this.addSetting(this.switchBack);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(BlockBreakingProgressListener.class, this);
        EVENTS.add(UpdateListener.class, this);
        this.prevSelectedSlot = -1;
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(BlockBreakingProgressListener.class, this);
        EVENTS.remove(UpdateListener.class, this);
    }

    @Override
    public void onBlockBreakingProgress(BlockBreakingProgressListener.BlockBreakingProgressEvent event) {
        class_2338 pos = event.getBlockPos();
        if (!BlockUtils.canBeClicked(pos)) {
            return;
        }
        if (this.prevSelectedSlot == -1) {
            this.prevSelectedSlot = AutoToolHack.MC.field_1724.method_31548().method_67532();
        }
        this.equipBestTool(pos, this.useSwords.isChecked(), this.useHands.isChecked(), this.repairMode.getValueI());
    }

    @Override
    public void onUpdate() {
        if (this.prevSelectedSlot == -1 || AutoToolHack.MC.field_1761.method_2923()) {
            return;
        }
        class_239 hitResult = AutoToolHack.MC.field_1765;
        if (hitResult != null && hitResult.method_17783() == class_239.class_240.field_1332) {
            return;
        }
        if (this.switchBack.isChecked()) {
            AutoToolHack.MC.field_1724.method_31548().method_61496(this.prevSelectedSlot);
        }
        this.prevSelectedSlot = -1;
    }

    public void equipIfEnabled(class_2338 pos) {
        if (!this.isEnabled()) {
            return;
        }
        this.equipBestTool(pos, this.useSwords.isChecked(), this.useHands.isChecked(), this.repairMode.getValueI());
    }

    public void equipBestTool(class_2338 pos, boolean useSwords, boolean useHands, int repairMode) {
        class_2680 state;
        int bestSlot;
        class_746 player = AutoToolHack.MC.field_1724;
        if (player.method_31549().field_7477) {
            return;
        }
        class_1799 heldItem = player.method_6047();
        boolean heldItemDamageable = this.isDamageable(heldItem);
        if (heldItemDamageable && this.isTooDamaged(heldItem, repairMode)) {
            this.putAwayDamagedTool(repairMode);
        }
        if ((bestSlot = this.getBestSlot(state = BlockUtils.getState(pos), useSwords, repairMode)) == -1) {
            if (useHands && heldItemDamageable && this.isWrongTool(heldItem, state)) {
                this.selectFallbackSlot();
            }
            return;
        }
        player.method_31548().method_61496(bestSlot);
    }

    private int getBestSlot(class_2680 state, boolean useSwords, int repairMode) {
        class_746 player = AutoToolHack.MC.field_1724;
        class_1661 inventory = player.method_31548();
        class_1799 heldItem = AutoToolHack.MC.field_1724.method_6047();
        float bestSpeed = this.getMiningSpeed(heldItem, state);
        if (this.isTooDamaged(heldItem, repairMode)) {
            bestSpeed = 1.0f;
        }
        int bestSlot = -1;
        for (int slot = 0; slot < 9; ++slot) {
            class_1799 stack;
            float speed;
            if (slot == inventory.method_67532() || (speed = this.getMiningSpeed(stack = inventory.method_5438(slot), state)) <= bestSpeed || !useSwords && stack.method_31573(class_3489.field_42611) || this.isTooDamaged(stack, repairMode)) continue;
            bestSpeed = speed;
            bestSlot = slot;
        }
        return bestSlot;
    }

    private float getMiningSpeed(class_1799 stack, class_2680 state) {
        class_5455 drm;
        class_2378 registry;
        Optional efficiency;
        int effLvl;
        float speed = stack.method_7924(state);
        if (speed > 1.0f && (effLvl = (efficiency = (registry = (drm = WurstClient.MC.field_1687.method_30349()).method_30530(class_7924.field_41265)).method_46746(class_1893.field_9131)).map(entry -> class_1890.method_8225((class_6880)entry, (class_1799)stack)).orElse(0).intValue()) > 0 && !stack.method_7960()) {
            speed += (float)(effLvl * effLvl + 1);
        }
        return speed;
    }

    private boolean isDamageable(class_1799 stack) {
        return !stack.method_7960() && stack.method_7963();
    }

    private boolean isTooDamaged(class_1799 stack, int repairMode) {
        return stack.method_7936() - stack.method_7919() <= repairMode;
    }

    private void putAwayDamagedTool(int repairMode) {
        class_1661 inv = AutoToolHack.MC.field_1724.method_31548();
        int selectedSlot = inv.method_67532();
        IClientPlayerInteractionManager im = IMC.getInteractionManager();
        OptionalInt emptySlot = IntStream.range(9, 36).filter(i -> !inv.method_5438(i).method_7960()).findFirst();
        if (emptySlot.isPresent()) {
            im.windowClick_QUICK_MOVE(InventoryUtils.toNetworkSlot(selectedSlot));
            return;
        }
        OptionalInt nonDamageableSlot = IntStream.range(9, 36).filter(i -> !this.isDamageable(inv.method_5438(i))).findFirst();
        if (nonDamageableSlot.isPresent()) {
            im.windowClick_SWAP(nonDamageableSlot.getAsInt(), selectedSlot);
            return;
        }
        OptionalInt notTooDamagedSlot = IntStream.range(9, 36).filter(i -> !this.isTooDamaged(inv.method_5438(i), repairMode)).findFirst();
        if (notTooDamagedSlot.isPresent()) {
            im.windowClick_SWAP(notTooDamagedSlot.getAsInt(), selectedSlot);
            return;
        }
        im.windowClick_SWAP(0, selectedSlot);
    }

    private boolean isWrongTool(class_1799 heldItem, class_2680 state) {
        return this.getMiningSpeed(heldItem, state) <= 1.0f;
    }

    private void selectFallbackSlot() {
        int fallbackSlot = this.getFallbackSlot();
        class_1661 inventory = AutoToolHack.MC.field_1724.method_31548();
        if (fallbackSlot == -1) {
            int prevSlot = inventory.method_67532();
            if (prevSlot == 8) {
                inventory.method_61496(0);
            } else {
                inventory.method_61496(prevSlot + 1);
            }
            return;
        }
        inventory.method_61496(fallbackSlot);
    }

    private int getFallbackSlot() {
        class_1661 inventory = AutoToolHack.MC.field_1724.method_31548();
        for (int slot = 0; slot < 9; ++slot) {
            class_1799 stack;
            if (slot == inventory.method_67532() || this.isDamageable(stack = inventory.method_5438(slot))) continue;
            return slot;
        }
        return -1;
    }
}
