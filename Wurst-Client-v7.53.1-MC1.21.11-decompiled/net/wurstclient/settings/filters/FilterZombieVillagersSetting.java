package net.wurstclient.settings.filters;

import net.minecraft.class_1297;
import net.minecraft.class_1641;
import net.wurstclient.settings.filters.EntityFilterCheckbox;

public final class FilterZombieVillagersSetting
extends EntityFilterCheckbox {
    public FilterZombieVillagersSetting(String description, boolean checked) {
        super("Filter zombie villagers", description, checked);
    }

    @Override
    public boolean test(class_1297 e) {
        return !(e instanceof class_1641);
    }

    public static FilterZombieVillagersSetting genericCombat(boolean checked) {
        return new FilterZombieVillagersSetting("description.wurst.setting.generic.filter_zombie_villagers_combat", checked);
    }

    public static FilterZombieVillagersSetting genericVision(boolean checked) {
        return new FilterZombieVillagersSetting("description.wurst.setting.generic.filter_zombie_villagers_vision", checked);
    }
}
