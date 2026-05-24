package net.wurstclient.hacks;

import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1321;
import net.minecraft.class_1646;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2237;
import net.minecraft.class_2248;
import net.minecraft.class_2304;
import net.minecraft.class_2338;
import net.minecraft.class_239;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;

@SearchTags(value={"auto soup", "AutoStew", "auto stew"})
public final class AutoSoupHack
extends Hack
implements UpdateListener {
    private final SliderSetting health = new SliderSetting("Health", "Eats a soup when your health reaches this value or falls below it.", 6.5, 0.5, 9.5, 0.5, SliderSetting.ValueDisplay.DECIMAL);
    private int oldSlot = -1;

    public AutoSoupHack() {
        super("AutoSoup");
        this.setCategory(Category.COMBAT);
        this.addSetting(this.health);
    }

    @Override
    protected void onEnable() {
        AutoSoupHack.WURST.getHax().autoEatHack.setEnabled(false);
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        this.stopIfEating();
    }

    @Override
    public void onUpdate() {
        for (int i = 0; i < 36; ++i) {
            class_1799 stack = AutoSoupHack.MC.field_1724.method_31548().method_5438(i);
            if (stack == null || stack.method_7909() != class_1802.field_8428 || i == 9) continue;
            class_1799 emptyBowlStack = AutoSoupHack.MC.field_1724.method_31548().method_5438(9);
            boolean swap = !emptyBowlStack.method_7960() && emptyBowlStack.method_7909() != class_1802.field_8428;
            IMC.getInteractionManager().windowClick_PICKUP(i < 9 ? 36 + i : i);
            IMC.getInteractionManager().windowClick_PICKUP(9);
            if (!swap) continue;
            IMC.getInteractionManager().windowClick_PICKUP(i < 9 ? 36 + i : i);
        }
        int soupInHotbar = this.findSoup(0, 9);
        if (soupInHotbar != -1) {
            if (!this.shouldEatSoup()) {
                this.stopIfEating();
                return;
            }
            if (this.oldSlot == -1) {
                this.oldSlot = AutoSoupHack.MC.field_1724.method_31548().method_67532();
            }
            AutoSoupHack.MC.field_1724.method_31548().method_61496(soupInHotbar);
            AutoSoupHack.MC.field_1690.field_1904.method_23481(true);
            IMC.getInteractionManager().rightClickItem();
            return;
        }
        this.stopIfEating();
        int soupInInventory = this.findSoup(9, 36);
        if (soupInInventory != -1) {
            IMC.getInteractionManager().windowClick_QUICK_MOVE(soupInInventory);
        }
    }

    private int findSoup(int startSlot, int endSlot) {
        List<class_1792> stews = List.of(class_1802.field_8208, class_1802.field_8308, class_1802.field_8515);
        for (int i = startSlot; i < endSlot; ++i) {
            class_1799 stack = AutoSoupHack.MC.field_1724.method_31548().method_5438(i);
            if (stack == null || !stews.contains(stack.method_7909())) continue;
            return i;
        }
        return -1;
    }

    private boolean shouldEatSoup() {
        if (AutoSoupHack.MC.field_1724.method_6032() > this.health.getValueF() * 2.0f) {
            return false;
        }
        return !this.isClickable(AutoSoupHack.MC.field_1765);
    }

    private boolean isClickable(class_239 hitResult) {
        if (hitResult == null) {
            return false;
        }
        if (hitResult instanceof class_3966) {
            class_1297 entity = ((class_3966)AutoSoupHack.MC.field_1765).method_17782();
            return entity instanceof class_1646 || entity instanceof class_1321;
        }
        if (hitResult instanceof class_3965) {
            class_2338 pos = ((class_3965)AutoSoupHack.MC.field_1765).method_17777();
            if (pos == null) {
                return false;
            }
            class_2248 block = AutoSoupHack.MC.field_1687.method_8320(pos).method_26204();
            return block instanceof class_2237 || block instanceof class_2304;
        }
        return false;
    }

    private void stopIfEating() {
        if (this.oldSlot == -1) {
            return;
        }
        AutoSoupHack.MC.field_1690.field_1904.method_23481(false);
        AutoSoupHack.MC.field_1724.method_31548().method_61496(this.oldSlot);
        this.oldSlot = -1;
    }
}
