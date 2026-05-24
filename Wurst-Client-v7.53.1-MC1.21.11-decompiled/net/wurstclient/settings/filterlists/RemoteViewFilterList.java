package net.wurstclient.settings.filterlists;

import java.util.ArrayList;
import java.util.List;
import net.wurstclient.settings.filterlists.EntityFilterList;
import net.wurstclient.settings.filters.FilterAllaysSetting;
import net.wurstclient.settings.filters.FilterArmorStandsSetting;
import net.wurstclient.settings.filters.FilterBabiesSetting;
import net.wurstclient.settings.filters.FilterBatsSetting;
import net.wurstclient.settings.filters.FilterEndermenSetting;
import net.wurstclient.settings.filters.FilterFlyingSetting;
import net.wurstclient.settings.filters.FilterGolemsSetting;
import net.wurstclient.settings.filters.FilterHostileSetting;
import net.wurstclient.settings.filters.FilterInvisibleSetting;
import net.wurstclient.settings.filters.FilterNeutralSetting;
import net.wurstclient.settings.filters.FilterPassiveSetting;
import net.wurstclient.settings.filters.FilterPassiveWaterSetting;
import net.wurstclient.settings.filters.FilterPetsSetting;
import net.wurstclient.settings.filters.FilterPiglinsSetting;
import net.wurstclient.settings.filters.FilterPlayersSetting;
import net.wurstclient.settings.filters.FilterShulkersSetting;
import net.wurstclient.settings.filters.FilterSleepingSetting;
import net.wurstclient.settings.filters.FilterSlimesSetting;
import net.wurstclient.settings.filters.FilterVillagersSetting;
import net.wurstclient.settings.filters.FilterZombiePiglinsSetting;
import net.wurstclient.settings.filters.FilterZombieVillagersSetting;

public final class RemoteViewFilterList
extends EntityFilterList {
    private RemoteViewFilterList(List<EntityFilterList.EntityFilter> filters) {
        super(filters);
    }

    public static RemoteViewFilterList create() {
        ArrayList<EntityFilterList.EntityFilter> builder = new ArrayList<EntityFilterList.EntityFilter>();
        builder.add(new FilterPlayersSetting("description.wurst.setting.remoteview.filter_players", false));
        builder.add(new FilterSleepingSetting("description.wurst.setting.remoteview.filter_sleeping", false));
        builder.add(new FilterFlyingSetting("description.wurst.setting.remoteview.filter_flying", 0.0));
        builder.add(new FilterHostileSetting("Won't view hostile mobs like zombies and creepers.", true));
        builder.add(FilterNeutralSetting.onOffOnly("description.wurst.setting.remoteview.filter_neutral", true));
        builder.add(new FilterPassiveSetting("Won't view animals like pigs and cows, ambient mobs like bats, and water mobs like fish, squid and dolphins.", true));
        builder.add(new FilterPassiveWaterSetting("Won't view passive water mobs like fish, squid, dolphins and axolotls.", true));
        builder.add(new FilterBabiesSetting("Won't view baby pigs, baby villagers, etc.", true));
        builder.add(new FilterBatsSetting("description.wurst.setting.remoteview.filter_bats", true));
        builder.add(new FilterSlimesSetting("Won't view slimes.", true));
        builder.add(new FilterPetsSetting("description.wurst.setting.remoteview.filter_pets", true));
        builder.add(new FilterVillagersSetting("description.wurst.setting.remoteview.filter_villagers", true));
        builder.add(new FilterZombieVillagersSetting("description.wurst.setting.remoteview.filter_zombie_villagers", true));
        builder.add(new FilterGolemsSetting("description.wurst.setting.remoteview.filter_golems", true));
        builder.add(FilterPiglinsSetting.onOffOnly("Won't view piglins.", true));
        builder.add(FilterZombiePiglinsSetting.onOffOnly("description.wurst.setting.remoteview.filter_zombie_piglins", true));
        builder.add(FilterEndermenSetting.onOffOnly("description.wurst.setting.remoteview.filter_endermen", true));
        builder.add(new FilterShulkersSetting("description.wurst.setting.remoteview.filter_shulkers", true));
        builder.add(new FilterAllaysSetting("description.wurst.setting.remoteview.filter_allays", true));
        builder.add(new FilterInvisibleSetting("description.wurst.setting.remoteview.filter_invisible", false));
        builder.add(new FilterArmorStandsSetting("description.wurst.setting.remoteview.filter_armor_stands", true));
        return new RemoteViewFilterList(builder);
    }
}
