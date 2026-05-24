package net.wurstclient.hacks.chestesp.groups;

import java.awt.Color;
import net.minecraft.class_2586;
import net.minecraft.class_2609;
import net.wurstclient.hacks.chestesp.ChestEspBlockGroup;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ColorSetting;

public final class FurnacesGroup
extends ChestEspBlockGroup {
    @Override
    protected CheckboxSetting createIncludeSetting() {
        return new CheckboxSetting("Include furnaces", false);
    }

    @Override
    protected ColorSetting createColorSetting() {
        return new ColorSetting("Furnace color", "Furnaces, smokers, and blast furnaces will be highlighted in this color.", Color.RED);
    }

    @Override
    protected boolean matches(class_2586 be) {
        return be instanceof class_2609;
    }
}
