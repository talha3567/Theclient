package net.wurstclient.hacks.chestesp.groups;

import java.awt.Color;
import net.minecraft.class_1297;
import net.minecraft.class_1700;
import net.wurstclient.hacks.chestesp.ChestEspEntityGroup;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ColorSetting;

public final class HopperCartsGroup
extends ChestEspEntityGroup {
    @Override
    protected CheckboxSetting createIncludeSetting() {
        return new CheckboxSetting("Include hopper carts", false);
    }

    @Override
    protected ColorSetting createColorSetting() {
        return new ColorSetting("Hopper cart color", "Minecarts with hoppers will be highlighted in this color.", Color.YELLOW);
    }

    @Override
    protected boolean matches(class_1297 e) {
        return e instanceof class_1700;
    }
}
