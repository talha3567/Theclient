package net.wurstclient.hacks.chestesp.groups;

import java.awt.Color;
import net.minecraft.class_2586;
import net.minecraft.class_2608;
import net.wurstclient.hacks.chestesp.ChestEspBlockGroup;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ColorSetting;

public final class DroppersGroup
extends ChestEspBlockGroup {
    @Override
    protected CheckboxSetting createIncludeSetting() {
        return new CheckboxSetting("Include droppers", false);
    }

    @Override
    protected ColorSetting createColorSetting() {
        return new ColorSetting("Dropper color", "Droppers will be highlighted in this color.", Color.WHITE);
    }

    @Override
    protected boolean matches(class_2586 be) {
        return be instanceof class_2608;
    }
}
