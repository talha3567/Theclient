package net.wurstclient.settings.filters;

import net.minecraft.class_1297;
import net.minecraft.class_1560;
import net.wurstclient.settings.filters.AttackDetectingEntityFilter;

public final class FilterEndermenSetting
extends AttackDetectingEntityFilter {
    private FilterEndermenSetting(String description, AttackDetectingEntityFilter.Mode selected, boolean checked) {
        super("Filter endermen", description, selected, checked);
    }

    public FilterEndermenSetting(String description, AttackDetectingEntityFilter.Mode selected) {
        this(description, selected, false);
    }

    @Override
    public boolean onTest(class_1297 e) {
        return !(e instanceof class_1560);
    }

    @Override
    public boolean ifCalmTest(class_1297 e) {
        class_1560 ee;
        return !(e instanceof class_1560) || (ee = (class_1560)e).method_6510();
    }

    public static FilterEndermenSetting genericCombat(AttackDetectingEntityFilter.Mode selected) {
        return new FilterEndermenSetting("description.wurst.setting.generic.filter_endermen_combat", selected);
    }

    public static FilterEndermenSetting genericVision(AttackDetectingEntityFilter.Mode selected) {
        return new FilterEndermenSetting("description.wurst.setting.generic.filter_endermen_vision", selected);
    }

    public static FilterEndermenSetting onOffOnly(String description, boolean onByDefault) {
        return new FilterEndermenSetting(description, null, onByDefault);
    }
}
