package net.wurstclient.settings.filters;

import net.minecraft.class_1297;
import net.minecraft.class_1308;
import net.minecraft.class_1454;
import net.minecraft.class_4836;
import net.minecraft.class_5354;
import net.wurstclient.settings.filters.AttackDetectingEntityFilter;

public final class FilterNeutralSetting
extends AttackDetectingEntityFilter {
    private FilterNeutralSetting(String description, AttackDetectingEntityFilter.Mode selected, boolean checked) {
        super("Filter neutral mobs", description, selected, checked);
    }

    public FilterNeutralSetting(String description, AttackDetectingEntityFilter.Mode selected) {
        this(description, selected, false);
    }

    @Override
    public boolean onTest(class_1297 e) {
        return !(e instanceof class_5354) && !(e instanceof class_1454) && !(e instanceof class_4836);
    }

    @Override
    public boolean ifCalmTest(class_1297 e) {
        if (e instanceof class_1454) {
            class_1454 pfe = (class_1454)e;
            return pfe.method_6594() > 0;
        }
        if ((e instanceof class_5354 || e instanceof class_4836) && e instanceof class_1308) {
            class_1308 me = (class_1308)e;
            return me.method_6510();
        }
        return true;
    }

    public static FilterNeutralSetting genericCombat(AttackDetectingEntityFilter.Mode selected) {
        return new FilterNeutralSetting("description.wurst.setting.generic.filter_neutral_combat", selected);
    }

    public static FilterNeutralSetting genericVision(AttackDetectingEntityFilter.Mode selected) {
        return new FilterNeutralSetting("description.wurst.setting.generic.filter_neutral_vision", selected);
    }

    public static FilterNeutralSetting onOffOnly(String description, boolean onByDefault) {
        return new FilterNeutralSetting(description, null, onByDefault);
    }
}
