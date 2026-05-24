package net.wurstclient.hacks;

import net.minecraft.class_1297;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.filterlists.EntityFilterList;
import net.wurstclient.settings.filters.AttackDetectingEntityFilter;
import net.wurstclient.settings.filters.FilterAllaysSetting;
import net.wurstclient.settings.filters.FilterArmorStandsSetting;
import net.wurstclient.settings.filters.FilterBatsSetting;
import net.wurstclient.settings.filters.FilterEndermenSetting;
import net.wurstclient.settings.filters.FilterGolemsSetting;
import net.wurstclient.settings.filters.FilterHostileSetting;
import net.wurstclient.settings.filters.FilterNamedSetting;
import net.wurstclient.settings.filters.FilterNeutralSetting;
import net.wurstclient.settings.filters.FilterPassiveSetting;
import net.wurstclient.settings.filters.FilterPassiveWaterSetting;
import net.wurstclient.settings.filters.FilterPetsSetting;
import net.wurstclient.settings.filters.FilterPiglinsSetting;
import net.wurstclient.settings.filters.FilterShulkersSetting;
import net.wurstclient.settings.filters.FilterSlimesSetting;
import net.wurstclient.settings.filters.FilterVillagersSetting;
import net.wurstclient.settings.filters.FilterZombiePiglinsSetting;
import net.wurstclient.settings.filters.FilterZombieVillagersSetting;

@SearchTags(value={"true sight"})
public final class TrueSightHack
extends Hack {
    private final EntityFilterList entityFilters = new EntityFilterList(FilterHostileSetting.genericVision(false), FilterNeutralSetting.genericVision(AttackDetectingEntityFilter.Mode.OFF), FilterPassiveSetting.genericVision(false), FilterPassiveWaterSetting.genericVision(false), FilterBatsSetting.genericVision(false), FilterSlimesSetting.genericVision(false), FilterPetsSetting.genericVision(false), FilterVillagersSetting.genericVision(false), FilterZombieVillagersSetting.genericVision(false), FilterGolemsSetting.genericVision(false), FilterPiglinsSetting.genericVision(AttackDetectingEntityFilter.Mode.OFF), FilterZombiePiglinsSetting.genericVision(AttackDetectingEntityFilter.Mode.OFF), FilterEndermenSetting.genericVision(AttackDetectingEntityFilter.Mode.OFF), FilterShulkersSetting.genericVision(false), FilterAllaysSetting.genericVision(false), FilterNamedSetting.genericVision(false), FilterArmorStandsSetting.genericVision(false));

    public TrueSightHack() {
        super("TrueSight");
        this.setCategory(Category.RENDER);
        this.entityFilters.forEach(x$0 -> this.addSetting((Setting)x$0));
    }

    public boolean shouldBeVisible(class_1297 entity) {
        return this.isEnabled() && this.entityFilters.testOne(entity);
    }
}
