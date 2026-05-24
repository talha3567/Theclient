package net.wurstclient.settings.filters;

import net.minecraft.class_1297;
import net.wurstclient.settings.filters.EntityFilterCheckbox;

public final class FilterInvisibleSetting
extends EntityFilterCheckbox {
    public FilterInvisibleSetting(String description, boolean checked) {
        super("Filter invisible", description, checked);
    }

    @Override
    public boolean test(class_1297 e) {
        return !e.method_5767();
    }

    public static FilterInvisibleSetting genericCombat(boolean checked) {
        return new FilterInvisibleSetting("description.wurst.setting.generic.filter_invisible_combat", checked);
    }

    public static FilterInvisibleSetting genericVision(boolean checked) {
        return new FilterInvisibleSetting("description.wurst.setting.generic.filter_invisible_vision", checked);
    }
}
