package net.wurstclient.settings.filters;

import net.minecraft.class_1297;
import net.minecraft.class_1511;
import net.wurstclient.settings.filters.EntityFilterCheckbox;

public final class FilterCrystalsSetting
extends EntityFilterCheckbox {
    public FilterCrystalsSetting(String description, boolean checked) {
        super("Filter end crystals", description, checked);
    }

    @Override
    public boolean test(class_1297 e) {
        return !(e instanceof class_1511);
    }

    public static FilterCrystalsSetting genericCombat(boolean checked) {
        return new FilterCrystalsSetting("description.wurst.setting.generic.filter_crystals_combat", checked);
    }
}
