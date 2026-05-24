package net.wurstclient.hacks.chestesp.groups;

import java.awt.Color;
import net.minecraft.class_2586;
import net.minecraft.class_2611;
import net.wurstclient.hacks.chestesp.ChestEspBlockGroup;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ColorSetting;

public final class EnderChestsGroup
extends ChestEspBlockGroup {
    @Override
    protected CheckboxSetting createIncludeSetting() {
        return new CheckboxSetting("Include ender chests", true);
    }

    @Override
    protected ColorSetting createColorSetting() {
        return new ColorSetting("Ender color", "Ender chests will be highlighted in this color.", Color.CYAN);
    }

    @Override
    protected boolean matches(class_2586 be) {
        return be instanceof class_2611;
    }
}
