package net.wurstclient.settings.filters;

import net.minecraft.class_1297;
import net.minecraft.class_7298;
import net.wurstclient.settings.filters.EntityFilterCheckbox;

public final class FilterAllaysSetting
extends EntityFilterCheckbox {
    public FilterAllaysSetting(String description, boolean checked) {
        super("Filter allays", description, checked);
    }

    @Override
    public boolean test(class_1297 e) {
        return !(e instanceof class_7298);
    }

    public static FilterAllaysSetting genericCombat(boolean checked) {
        return new FilterAllaysSetting("description.wurst.setting.generic.filter_allays_combat", checked);
    }

    public static FilterAllaysSetting genericVision(boolean checked) {
        return new FilterAllaysSetting("description.wurst.setting.generic.filter_allays_vision", checked);
    }
}
