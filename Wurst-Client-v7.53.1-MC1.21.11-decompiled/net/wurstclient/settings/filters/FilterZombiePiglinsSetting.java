package net.wurstclient.settings.filters;

import net.minecraft.class_1297;
import net.minecraft.class_1590;
import net.wurstclient.settings.filters.AttackDetectingEntityFilter;

public final class FilterZombiePiglinsSetting
extends AttackDetectingEntityFilter {
    private FilterZombiePiglinsSetting(String description, AttackDetectingEntityFilter.Mode selected, boolean checked) {
        super("Filter zombie piglins", description, selected, checked);
    }

    public FilterZombiePiglinsSetting(String description, AttackDetectingEntityFilter.Mode selected) {
        this(description, selected, false);
    }

    @Override
    public boolean onTest(class_1297 e) {
        return !(e instanceof class_1590);
    }

    @Override
    public boolean ifCalmTest(class_1297 e) {
        class_1590 zpe;
        return !(e instanceof class_1590) || (zpe = (class_1590)e).method_6510();
    }

    public static FilterZombiePiglinsSetting genericCombat(AttackDetectingEntityFilter.Mode selected) {
        return new FilterZombiePiglinsSetting("description.wurst.setting.generic.filter_zombie_piglins_combat", selected);
    }

    public static FilterZombiePiglinsSetting genericVision(AttackDetectingEntityFilter.Mode selected) {
        return new FilterZombiePiglinsSetting("description.wurst.setting.generic.filter_zombie_piglins_vision", selected);
    }

    public static FilterZombiePiglinsSetting onOffOnly(String description, boolean onByDefault) {
        return new FilterZombiePiglinsSetting(description, null, onByDefault);
    }
}
