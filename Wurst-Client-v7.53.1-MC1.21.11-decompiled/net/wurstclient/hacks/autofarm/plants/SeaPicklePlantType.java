package net.wurstclient.hacks.autofarm.plants;

import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_2472;
import net.minecraft.class_2680;
import net.minecraft.class_2769;
import net.minecraft.class_3481;
import net.minecraft.class_3486;
import net.minecraft.class_3610;
import net.wurstclient.hacks.autofarm.AutoFarmPlantType;
import net.wurstclient.settings.PlantTypeSetting;
import net.wurstclient.util.BlockUtils;

public final class SeaPicklePlantType
extends AutoFarmPlantType {
    @Override
    public final boolean isReplantingSpot(class_2338 pos, class_2680 state) {
        return state.method_26204() instanceof class_2472 && this.hasPlantingSurface(pos);
    }

    @Override
    public final boolean hasPlantingSurface(class_2338 pos) {
        class_2680 floor = BlockUtils.getState(pos.method_10074());
        if (!floor.method_26164(class_3481.field_15461)) {
            return false;
        }
        class_3610 fluid = BlockUtils.getState(pos).method_26227();
        return fluid.method_15767(class_3486.field_15517) && fluid.method_15761() == 8;
    }

    @Override
    public class_1792 getSeedItem() {
        return class_1802.field_17498;
    }

    @Override
    public boolean shouldHarvestByMining(class_2338 pos, class_2680 state) {
        if (!(state.method_26204() instanceof class_2472)) {
            return false;
        }
        return (Integer)state.method_11654((class_2769)class_2472.field_11472) > 1;
    }

    @Override
    protected PlantTypeSetting createSetting() {
        return new PlantTypeSetting("Sea pickles", class_1802.field_17498, true, true);
    }
}
