package net.wurstclient.hacks.chestesp.groups;

import java.awt.Color;
import net.minecraft.class_2586;
import net.minecraft.class_2614;
import net.wurstclient.hacks.chestesp.ChestEspBlockGroup;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ColorSetting;

public final class HoppersGroup
extends ChestEspBlockGroup {
    @Override
    protected CheckboxSetting createIncludeSetting() {
        return new CheckboxSetting("Include hoppers", false);
    }

    @Override
    protected ColorSetting createColorSetting() {
        return new ColorSetting("Hopper color", "Hoppers will be highlighted in this color.", Color.WHITE);
    }

    @Override
    protected boolean matches(class_2586 be) {
        return be instanceof class_2614;
    }
}
