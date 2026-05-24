package net.wurstclient.hacks.autofarm.plants;

import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_3481;
import net.minecraft.class_3486;
import net.wurstclient.hacks.autofarm.AutoFarmPlantType;
import net.wurstclient.settings.PlantTypeSetting;
import net.wurstclient.util.BlockUtils;

public final class SugarCanePlantType
extends AutoFarmPlantType {
    @Override
    public final boolean isReplantingSpot(class_2338 pos, class_2680 state) {
        if (!state.method_27852(class_2246.field_10424)) {
            return false;
        }
        return !BlockUtils.getState(pos.method_10074()).method_27852(class_2246.field_10424) && this.hasPlantingSurface(pos);
    }

    @Override
    public final boolean hasPlantingSurface(class_2338 pos) {
        class_2338 floorPos = pos.method_10074();
        class_2680 floor = BlockUtils.getState(floorPos);
        if (!floor.method_26164(class_3481.field_29822) && !floor.method_26164(class_3481.field_15466)) {
            return false;
        }
        for (class_2350 side : class_2350.class_2353.field_11062) {
            class_2680 floorNeighbor = BlockUtils.getState(floorPos.method_10093(side));
            if (!floorNeighbor.method_26227().method_15767(class_3486.field_15517) && !floorNeighbor.method_27852(class_2246.field_10110)) continue;
            return true;
        }
        return false;
    }

    @Override
    public class_1792 getSeedItem() {
        return class_1802.field_17531;
    }

    @Override
    public boolean shouldHarvestByMining(class_2338 pos, class_2680 state) {
        if (!state.method_27852(class_2246.field_10424)) {
            return false;
        }
        class_2338 below = pos.method_10074();
        return this.isReplantingSpot(below, BlockUtils.getState(below));
    }

    @Override
    protected PlantTypeSetting createSetting() {
        return new PlantTypeSetting("Sugar cane", class_1802.field_17531, true, true);
    }
}
