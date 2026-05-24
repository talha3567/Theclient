package net.wurstclient.hacks.autofarm.plants;

import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_1922;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_3486;
import net.minecraft.class_3610;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.autofarm.AutoFarmPlantType;
import net.wurstclient.settings.PlantTypeSetting;
import net.wurstclient.util.BlockUtils;

public final class KelpPlantType
extends AutoFarmPlantType {
    @Override
    public final boolean isReplantingSpot(class_2338 pos, class_2680 state) {
        if (!state.method_27852(class_2246.field_9993) && !state.method_27852(class_2246.field_10463)) {
            return false;
        }
        class_2680 floor = BlockUtils.getState(pos.method_10074());
        return !floor.method_27852(class_2246.field_9993) && !floor.method_27852(class_2246.field_10463) && this.hasPlantingSurface(pos);
    }

    @Override
    public final boolean hasPlantingSurface(class_2338 pos) {
        class_3610 fluid = BlockUtils.getState(pos).method_26227();
        if (!fluid.method_15767(class_3486.field_15517) || fluid.method_15761() != 8) {
            return false;
        }
        class_2680 floor = BlockUtils.getState(pos.method_10074());
        return !floor.method_27852(class_2246.field_10092) && floor.method_26206((class_1922)WurstClient.MC.field_1687, pos, class_2350.field_11036);
    }

    @Override
    public class_1792 getSeedItem() {
        return class_1802.field_17532;
    }

    @Override
    public boolean shouldHarvestByMining(class_2338 pos, class_2680 state) {
        if (!state.method_27852(class_2246.field_9993) && !state.method_27852(class_2246.field_10463)) {
            return false;
        }
        class_2338 below = pos.method_10074();
        return this.isReplantingSpot(below, BlockUtils.getState(below));
    }

    @Override
    protected PlantTypeSetting createSetting() {
        return new PlantTypeSetting("Kelp", class_1802.field_17532, true, true);
    }
}
