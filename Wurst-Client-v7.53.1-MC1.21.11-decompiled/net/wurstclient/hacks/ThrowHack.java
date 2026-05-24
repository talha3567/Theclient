package net.wurstclient.hacks;

import net.minecraft.class_239;
import net.minecraft.class_3965;
import net.wurstclient.Category;
import net.wurstclient.events.RightClickListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;

public final class ThrowHack
extends Hack
implements RightClickListener {
    private final SliderSetting amount = new SliderSetting("Amount", "Amount of uses per click.", 16.0, 2.0, 1000000.0, 1.0, SliderSetting.ValueDisplay.INTEGER);

    public ThrowHack() {
        super("Throw");
        this.setCategory(Category.OTHER);
        this.addSetting(this.amount);
    }

    @Override
    public String getRenderName() {
        return this.getName() + " [" + this.amount.getValueString() + "]";
    }

    @Override
    protected void onEnable() {
        EVENTS.add(RightClickListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(RightClickListener.class, this);
    }

    @Override
    public void onRightClick(RightClickListener.RightClickEvent event) {
        if (ThrowHack.MC.field_1752 > 0) {
            return;
        }
        if (!ThrowHack.MC.field_1690.field_1904.method_1434()) {
            return;
        }
        for (int i = 0; i < this.amount.getValueI(); ++i) {
            if (ThrowHack.MC.field_1765.method_17783() == class_239.class_240.field_1332) {
                class_3965 hitResult = (class_3965)ThrowHack.MC.field_1765;
                IMC.getInteractionManager().rightClickBlock(hitResult.method_17777(), hitResult.method_17780(), hitResult.method_17784());
            }
            IMC.getInteractionManager().rightClickItem();
        }
    }
}
