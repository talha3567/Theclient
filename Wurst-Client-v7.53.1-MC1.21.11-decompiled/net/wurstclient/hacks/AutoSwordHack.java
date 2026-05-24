package net.wurstclient.hacks;

import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1320;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_239;
import net.minecraft.class_3966;
import net.minecraft.class_5134;
import net.minecraft.class_6880;
import net.minecraft.class_9334;
import net.minecraft.class_9362;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.EntityUtils;
import net.wurstclient.util.ItemUtils;

@SearchTags(value={"auto sword"})
public final class AutoSwordHack
extends Hack
implements UpdateListener {
    private final EnumSetting<Priority> priority = new EnumSetting("Priority", (Enum[])Priority.values(), (Enum)Priority.SPEED);
    private final CheckboxSetting switchBack = new CheckboxSetting("Switch back", "Switches back to the previously selected slot after \u00a7lRelease time\u00a7r has passed.", true);
    private final SliderSetting releaseTime = new SliderSetting("Release time", "Time until AutoSword will switch back from the weapon to the previously selected slot.\n\nOnly works when \u00a7lSwitch back\u00a7r is checked.", 10.0, 1.0, 200.0, 1.0, SliderSetting.ValueDisplay.INTEGER.withSuffix(" ticks").withLabel(1.0, "1 tick"));
    private int oldSlot;
    private int timer;

    public AutoSwordHack() {
        super("AutoSword");
        this.setCategory(Category.COMBAT);
        this.addSetting(this.priority);
        this.addSetting(this.switchBack);
        this.addSetting(this.releaseTime);
    }

    @Override
    protected void onEnable() {
        this.oldSlot = -1;
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        this.resetSlot();
    }

    @Override
    public void onUpdate() {
        class_1297 entity;
        if (AutoSwordHack.MC.field_1765 != null && AutoSwordHack.MC.field_1765.method_17783() == class_239.class_240.field_1331 && (entity = ((class_3966)AutoSwordHack.MC.field_1765).method_17782()) instanceof class_1309 && EntityUtils.IS_ATTACKABLE.test(entity)) {
            this.setSlot(entity);
        }
        if (this.timer > 0) {
            --this.timer;
            return;
        }
        this.resetSlot();
    }

    public void setSlot(class_1297 entity) {
        if (!this.isEnabled()) {
            return;
        }
        if (AutoSwordHack.WURST.getHax().autoEatHack.isEating()) {
            return;
        }
        float bestValue = -2.1474836E9f;
        int bestSlot = -1;
        for (int i = 0; i < 9; ++i) {
            class_1799 stack;
            float value;
            if (AutoSwordHack.MC.field_1724.method_31548().method_5438(i).method_7960() || !((value = this.getValue(stack = AutoSwordHack.MC.field_1724.method_31548().method_5438(i), entity)) > bestValue)) continue;
            bestValue = value;
            bestSlot = i;
        }
        if (bestSlot == -1) {
            return;
        }
        if (this.oldSlot == -1) {
            this.oldSlot = AutoSwordHack.MC.field_1724.method_31548().method_67532();
        }
        AutoSwordHack.MC.field_1724.method_31548().method_61496(bestSlot);
        this.timer = this.releaseTime.getValueI();
    }

    private float getValue(class_1799 stack, class_1297 entity) {
        class_1792 item = stack.method_7909();
        if (stack.method_58694(class_9334.field_50077) == null && stack.method_58694(class_9334.field_55878) == null) {
            return -2.1474836E9f;
        }
        switch (this.priority.getSelected().ordinal()) {
            case 0: {
                return (float)ItemUtils.getAttribute(item, (class_6880<class_1320>)class_5134.field_23723).orElse(-2.147483648E9);
            }
            case 1: {
                float dmg = (float)ItemUtils.getAttribute(item, (class_6880<class_1320>)class_5134.field_23721).orElse(-2.147483648E9);
                if (item instanceof class_9362) {
                    class_9362 mace = (class_9362)item;
                    dmg = mace.method_58403((class_1297)AutoSwordHack.MC.field_1724, dmg, entity.method_48923().method_48802((class_1657)AutoSwordHack.MC.field_1724));
                }
                return dmg;
            }
        }
        return -2.1474836E9f;
    }

    private void resetSlot() {
        if (!this.switchBack.isChecked()) {
            this.oldSlot = -1;
            return;
        }
        if (this.oldSlot != -1) {
            AutoSwordHack.MC.field_1724.method_31548().method_61496(this.oldSlot);
            this.oldSlot = -1;
        }
    }

    private static enum Priority {
        SPEED("Speed (swords)"),
        DAMAGE("Damage (axes)");

        private final String name;

        private Priority(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}
