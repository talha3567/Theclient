package net.wurstclient.settings.filters;

import net.minecraft.class_1297;
import net.minecraft.class_1678;
import net.wurstclient.settings.filters.EntityFilterCheckbox;

public final class FilterShulkerBulletSetting
extends EntityFilterCheckbox {
    public FilterShulkerBulletSetting(String description, boolean checked) {
        super("Filter shulker bullets", description, checked);
    }

    @Override
    public boolean test(class_1297 e) {
        return !(e instanceof class_1678);
    }

    public static FilterShulkerBulletSetting genericCombat(boolean checked) {
        return new FilterShulkerBulletSetting("description.wurst.setting.generic.filter_shulker_bullets_combat", checked);
    }
}
