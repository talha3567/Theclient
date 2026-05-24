package net.wurstclient.settings.filters;

import net.minecraft.class_1297;
import net.minecraft.class_1321;
import net.minecraft.class_1496;
import net.wurstclient.settings.filters.EntityFilterCheckbox;

public final class FilterPetsSetting
extends EntityFilterCheckbox {
    public FilterPetsSetting(String description, boolean checked) {
        super("Filter pets", description, checked);
    }

    @Override
    public boolean test(class_1297 e) {
        return !(e instanceof class_1321 && ((class_1321)e).method_6181() || e instanceof class_1496 && ((class_1496)e).method_6727());
    }

    public static FilterPetsSetting genericCombat(boolean checked) {
        return new FilterPetsSetting("description.wurst.setting.generic.filter_pets_combat", checked);
    }

    public static FilterPetsSetting genericVision(boolean checked) {
        return new FilterPetsSetting("description.wurst.setting.generic.filter_pets_vision", checked);
    }
}
