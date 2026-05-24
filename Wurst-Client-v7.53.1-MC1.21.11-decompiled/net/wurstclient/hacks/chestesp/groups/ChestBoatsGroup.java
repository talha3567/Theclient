package net.wurstclient.hacks.chestesp.groups;

import java.awt.Color;
import net.minecraft.class_10256;
import net.minecraft.class_1297;
import net.wurstclient.hacks.chestesp.ChestEspEntityGroup;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ColorSetting;

public final class ChestBoatsGroup
extends ChestEspEntityGroup {
    @Override
    protected CheckboxSetting createIncludeSetting() {
        return new CheckboxSetting("Include chest boats", true);
    }

    @Override
    protected ColorSetting createColorSetting() {
        return new ColorSetting("Chest boat color", "Boats with chests will be highlighted in this color.", Color.YELLOW);
    }

    @Override
    protected boolean matches(class_1297 e) {
        return e instanceof class_10256;
    }
}
