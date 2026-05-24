package net.wurstclient.settings.filterlists;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.class_1297;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.filters.AttackDetectingEntityFilter;
import net.wurstclient.settings.filters.FilterAllaysSetting;
import net.wurstclient.settings.filters.FilterArmorStandsSetting;
import net.wurstclient.settings.filters.FilterBabiesSetting;
import net.wurstclient.settings.filters.FilterBatsSetting;
import net.wurstclient.settings.filters.FilterCrystalsSetting;
import net.wurstclient.settings.filters.FilterEndermenSetting;
import net.wurstclient.settings.filters.FilterFlyingSetting;
import net.wurstclient.settings.filters.FilterGolemsSetting;
import net.wurstclient.settings.filters.FilterHostileSetting;
import net.wurstclient.settings.filters.FilterInvisibleSetting;
import net.wurstclient.settings.filters.FilterNamedSetting;
import net.wurstclient.settings.filters.FilterNeutralSetting;
import net.wurstclient.settings.filters.FilterPassiveSetting;
import net.wurstclient.settings.filters.FilterPassiveWaterSetting;
import net.wurstclient.settings.filters.FilterPetsSetting;
import net.wurstclient.settings.filters.FilterPiglinsSetting;
import net.wurstclient.settings.filters.FilterPlayersSetting;
import net.wurstclient.settings.filters.FilterShulkerBulletSetting;
import net.wurstclient.settings.filters.FilterShulkersSetting;
import net.wurstclient.settings.filters.FilterSleepingSetting;
import net.wurstclient.settings.filters.FilterSlimesSetting;
import net.wurstclient.settings.filters.FilterVillagersSetting;
import net.wurstclient.settings.filters.FilterZombiePiglinsSetting;
import net.wurstclient.settings.filters.FilterZombieVillagersSetting;

public class EntityFilterList {
    private final List<EntityFilter> entityFilters;

    public EntityFilterList(EntityFilter ... filters) {
        this(Arrays.asList(filters));
    }

    public EntityFilterList(List<EntityFilter> filters) {
        this.entityFilters = Collections.unmodifiableList(filters);
    }

    public final void forEach(Consumer<? super Setting> action) {
        this.entityFilters.stream().map(EntityFilter::getSetting).forEach(action);
    }

    public final <T extends class_1297> Stream<T> applyTo(Stream<T> stream) {
        for (EntityFilter filter : this.entityFilters) {
            if (!filter.isFilterEnabled()) continue;
            stream = stream.filter(filter);
        }
        return stream;
    }

    public final boolean testOne(class_1297 entity) {
        for (EntityFilter filter : this.entityFilters) {
            if (!filter.isFilterEnabled() || filter.test(entity)) continue;
            return false;
        }
        return true;
    }

    public static EntityFilterList genericCombat() {
        return new EntityFilterList(FilterPlayersSetting.genericCombat(false), FilterSleepingSetting.genericCombat(false), FilterFlyingSetting.genericCombat(0.0), FilterHostileSetting.genericCombat(false), FilterNeutralSetting.genericCombat(AttackDetectingEntityFilter.Mode.OFF), FilterPassiveSetting.genericCombat(false), FilterPassiveWaterSetting.genericCombat(false), FilterBabiesSetting.genericCombat(false), FilterBatsSetting.genericCombat(false), FilterSlimesSetting.genericCombat(false), FilterPetsSetting.genericCombat(false), FilterVillagersSetting.genericCombat(false), FilterZombieVillagersSetting.genericCombat(false), FilterGolemsSetting.genericCombat(false), FilterPiglinsSetting.genericCombat(AttackDetectingEntityFilter.Mode.OFF), FilterZombiePiglinsSetting.genericCombat(AttackDetectingEntityFilter.Mode.OFF), FilterEndermenSetting.genericCombat(AttackDetectingEntityFilter.Mode.OFF), FilterShulkersSetting.genericCombat(false), FilterAllaysSetting.genericCombat(false), FilterInvisibleSetting.genericCombat(false), FilterNamedSetting.genericCombat(false), FilterShulkerBulletSetting.genericCombat(false), FilterArmorStandsSetting.genericCombat(false), FilterCrystalsSetting.genericCombat(false));
    }

    public static interface EntityFilter
    extends Predicate<class_1297> {
        public boolean isFilterEnabled();

        public Setting getSetting();
    }
}
