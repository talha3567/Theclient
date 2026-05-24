package net.wurstclient.settings.filters;

import net.minecraft.class_1297;
import net.minecraft.class_1531;
import net.wurstclient.settings.filters.EntityFilterCheckbox;

public final class FilterArmorStandsSetting
extends EntityFilterCheckbox {
    public FilterArmorStandsSetting(String description, boolean checked) {
        super("Filter armor stands", description, checked);
    }

    @Override
    public boolean test(class_1297 e) {
        return !(e instanceof class_1531);
    }

    public static FilterArmorStandsSetting genericCombat(boolean checked) {
        return new FilterArmorStandsSetting("description.wurst.setting.generic.filter_armor_stands_combat", checked);
    }

    public static FilterArmorStandsSetting genericVision(boolean checked) {
        return new FilterArmorStandsSetting("description.wurst.setting.generic.filter_armor_stands_vision", checked);
    }
}
