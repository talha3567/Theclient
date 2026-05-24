package net.wurstclient.settings.filters;

import net.minecraft.class_1297;
import net.minecraft.class_1421;
import net.wurstclient.settings.filters.EntityFilterCheckbox;

public final class FilterBatsSetting
extends EntityFilterCheckbox {
    public FilterBatsSetting(String description, boolean checked) {
        super("Filter bats", description, checked);
    }

    @Override
    public boolean test(class_1297 e) {
        return !(e instanceof class_1421);
    }

    public static FilterBatsSetting genericCombat(boolean checked) {
        return new FilterBatsSetting("description.wurst.setting.generic.filter_bats_combat", checked);
    }

    public static FilterBatsSetting genericVision(boolean checked) {
        return new FilterBatsSetting("description.wurst.setting.generic.filter_bats_vision", checked);
    }
}
