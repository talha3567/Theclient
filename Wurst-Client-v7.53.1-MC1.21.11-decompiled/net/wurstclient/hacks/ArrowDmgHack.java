package net.wurstclient.hacks;

import net.minecraft.class_1297;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2828;
import net.minecraft.class_2848;
import net.minecraft.class_634;
import net.minecraft.class_746;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.StopUsingItemListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;

@SearchTags(value={"arrow dmg", "ArrowDamage", "arrow damage"})
public final class ArrowDmgHack
extends Hack
implements StopUsingItemListener {
    private final SliderSetting strength = new SliderSetting("Strength", "description.wurst.setting.arrowdmg.strength", 10.0, 0.1, 10.0, 0.1, SliderSetting.ValueDisplay.DECIMAL);
    private final CheckboxSetting yeetTridents = new CheckboxSetting("Trident yeet mode", "description.wurst.setting.arrowdmg.trident_yeet_mode", false);

    public ArrowDmgHack() {
        super("ArrowDMG");
        this.setCategory(Category.COMBAT);
        this.addSetting(this.strength);
        this.addSetting(this.yeetTridents);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(StopUsingItemListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(StopUsingItemListener.class, this);
    }

    @Override
    public void onStopUsingItem() {
        class_746 player = ArrowDmgHack.MC.field_1724;
        class_634 netHandler = player.field_3944;
        if (!this.isValidItem(player.method_6047().method_7909())) {
            return;
        }
        netHandler.method_52787((class_2596)new class_2848((class_1297)player, class_2848.class_2849.field_12981));
        double x = player.method_23317();
        double y = player.method_23318();
        double z = player.method_23321();
        double adjustedStrength = this.strength.getValue() / 10.0 * Math.sqrt(500.0);
        class_243 lookVec = player.method_5828(1.0f).method_1021(adjustedStrength);
        for (int i = 0; i < 4; ++i) {
            this.sendPos(x, y, z, true);
        }
        this.sendPos(x - lookVec.field_1352, y, z - lookVec.field_1350, true);
        this.sendPos(x, y, z, false);
    }

    private void sendPos(double x, double y, double z, boolean onGround) {
        class_634 netHandler = ArrowDmgHack.MC.field_1724.field_3944;
        netHandler.method_52787((class_2596)new class_2828.class_2829(x, y, z, onGround, ArrowDmgHack.MC.field_1724.field_5976));
    }

    private boolean isValidItem(class_1792 item) {
        if (this.yeetTridents.isChecked() && item == class_1802.field_8547) {
            return true;
        }
        return item == class_1802.field_8102;
    }
}
