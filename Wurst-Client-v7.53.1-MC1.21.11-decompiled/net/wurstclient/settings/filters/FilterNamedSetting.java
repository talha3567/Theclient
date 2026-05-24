package net.wurstclient.settings.filters;

import net.minecraft.class_1297;
import net.wurstclient.settings.filters.EntityFilterCheckbox;

public final class FilterNamedSetting
extends EntityFilterCheckbox {
    public FilterNamedSetting(String description, boolean checked) {
        super("Filter named", description, checked);
    }

    @Override
    public boolean test(class_1297 e) {
        return !e.method_16914();
    }

    public static FilterNamedSetting genericCombat(boolean checked) {
        return new FilterNamedSetting("description.wurst.setting.generic.filter_named_combat", checked);
    }

    public static FilterNamedSetting genericVision(boolean checked) {
        return new FilterNamedSetting("description.wurst.setting.generic.filter_named_vision", checked);
    }
}
