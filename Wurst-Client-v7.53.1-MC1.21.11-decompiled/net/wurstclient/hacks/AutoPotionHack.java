package net.wurstclient.hacks;

import net.minecraft.class_1291;
import net.minecraft.class_1294;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_6880;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.ItemUtils;
import net.wurstclient.util.Rotation;

@SearchTags(value={"AutoPotion", "auto potion", "AutoSplashPotion", "auto splash potion"})
public final class AutoPotionHack
extends Hack
implements UpdateListener {
    private final SliderSetting health = new SliderSetting("Health", "Throws a potion when your health reaches this value or falls below it.", 6.0, 0.5, 9.5, 0.5, SliderSetting.ValueDisplay.DECIMAL.withSuffix(" hearts"));
    private int timer;

    public AutoPotionHack() {
        super("AutoPotion");
        this.setCategory(Category.COMBAT);
        this.addSetting(this.health);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        this.timer = 0;
    }

    @Override
    public void onUpdate() {
        int potionInHotbar = this.findPotion(0, 9);
        if (potionInHotbar != -1) {
            if (this.timer > 0) {
                --this.timer;
                return;
            }
            if (AutoPotionHack.MC.field_1724.method_6032() > this.health.getValueF() * 2.0f) {
                return;
            }
            int oldSlot = AutoPotionHack.MC.field_1724.method_31548().method_67532();
            AutoPotionHack.MC.field_1724.method_31548().method_61496(potionInHotbar);
            new Rotation(AutoPotionHack.MC.field_1724.method_36454(), 90.0f).sendPlayerLookPacket();
            IMC.getInteractionManager().rightClickItem();
            AutoPotionHack.MC.field_1724.method_31548().method_61496(oldSlot);
            new Rotation(AutoPotionHack.MC.field_1724.method_36454(), AutoPotionHack.MC.field_1724.method_36455()).sendPlayerLookPacket();
            this.timer = 10;
            return;
        }
        int potionInInventory = this.findPotion(9, 36);
        if (potionInInventory != -1) {
            IMC.getInteractionManager().windowClick_QUICK_MOVE(potionInInventory);
        }
    }

    private int findPotion(int startSlot, int endSlot) {
        for (int i = startSlot; i < endSlot; ++i) {
            class_1799 stack = AutoPotionHack.MC.field_1724.method_31548().method_5438(i);
            if (stack.method_7909() != class_1802.field_8436 || !ItemUtils.hasEffect(stack, (class_6880<class_1291>)class_1294.field_5915)) continue;
            return i;
        }
        return -1;
    }
}
