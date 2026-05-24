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

public final class CactusPlantType
extends AutoFarmPlantType {
    @Override
    public final boolean isReplantingSpot(class_2338 pos, class_2680 state) {
        if (!state.method_27852(class_2246.field_10029)) {
            return false;
        }
        return !BlockUtils.getState(pos.method_10074()).method_27852(class_2246.field_10029) && this.hasPlantingSurface(pos);
    }

    @Override
    public final boolean hasPlantingSurface(class_2338 pos) {
        class_2338 floorPos = pos.method_10074();
        class_2680 floor = BlockUtils.getState(floorPos);
        if (!floor.method_26164(class_3481.field_15466)) {
            return false;
        }
        return class_2350.class_2353.field_11062.method_29716().map(arg_0 -> ((class_2338)pos).method_10093(arg_0)).map(BlockUtils::getState).noneMatch(neighbor -> neighbor.method_51367() || neighbor.method_26227().method_15767(class_3486.field_15518));
    }

    @Override
    public class_1792 getSeedItem() {
        return class_1802.field_17520;
    }

    @Override
    public boolean shouldHarvestByMining(class_2338 pos, class_2680 state) {
        if (!state.method_27852(class_2246.field_10029) && !state.method_27852(class_2246.field_56564)) {
            return false;
        }
        class_2338 below = pos.method_10074();
        return this.isReplantingSpot(below, BlockUtils.getState(below));
    }

    @Override
    protected PlantTypeSetting createSetting() {
        return new PlantTypeSetting("Cactus", class_1802.field_17520, true, true);
    }
}
