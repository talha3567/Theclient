package net.wurstclient.hacks.chestesp.groups;

import java.awt.Color;
import net.minecraft.class_2586;
import net.minecraft.class_2646;
import net.wurstclient.hacks.chestesp.ChestEspBlockGroup;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.util.LootrModCompat;

public final class TrapChestsGroup
extends ChestEspBlockGroup {
    @Override
    protected CheckboxSetting createIncludeSetting() {
        return new CheckboxSetting("Include trap chests", true);
    }

    @Override
    protected ColorSetting createColorSetting() {
        return new ColorSetting("Trap chest color", "Trapped chests will be highlighted in this color.", new Color(0xFF8000));
    }

    @Override
    protected boolean matches(class_2586 be) {
        return be instanceof class_2646 || LootrModCompat.isLootrTrappedChest(be);
    }
}
