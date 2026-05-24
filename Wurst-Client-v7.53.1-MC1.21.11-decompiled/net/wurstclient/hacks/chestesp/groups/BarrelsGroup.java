package net.wurstclient.hacks.chestesp.groups;

import java.awt.Color;
import net.minecraft.class_2586;
import net.minecraft.class_3719;
import net.wurstclient.hacks.chestesp.ChestEspBlockGroup;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.util.LootrModCompat;

public final class BarrelsGroup
extends ChestEspBlockGroup {
    @Override
    protected CheckboxSetting createIncludeSetting() {
        return new CheckboxSetting("Include barrels", true);
    }

    @Override
    protected ColorSetting createColorSetting() {
        return new ColorSetting("Barrel color", "Barrels will be highlighted in this color.", Color.GREEN);
    }

    @Override
    protected boolean matches(class_2586 be) {
        return be instanceof class_3719 || LootrModCompat.isLootrBarrel(be);
    }
}
