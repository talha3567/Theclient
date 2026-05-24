package net.wurstclient.hacks.autofarm.plants;

import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_3481;
import net.wurstclient.hacks.autofarm.AutoFarmPlantType;
import net.wurstclient.settings.PlantTypeSetting;
import net.wurstclient.util.BlockUtils;

public final class BambooPlantType
extends AutoFarmPlantType {
    @Override
    public final boolean isReplantingSpot(class_2338 pos, class_2680 state) {
        if (!state.method_27852(class_2246.field_10211) && !state.method_27852(class_2246.field_10108)) {
            return false;
        }
        class_2680 floor = BlockUtils.getState(pos.method_10074());
        return !floor.method_27852(class_2246.field_10211) && !floor.method_27852(class_2246.field_10108) && floor.method_26164(class_3481.field_15497);
    }

    @Override
    public final boolean hasPlantingSurface(class_2338 pos) {
        return BlockUtils.getState(pos.method_10074()).method_26164(class_3481.field_15497);
    }

    @Override
    public class_1792 getSeedItem() {
        return class_1802.field_8648;
    }

    @Override
    public boolean shouldHarvestByMining(class_2338 pos, class_2680 state) {
        if (!state.method_27852(class_2246.field_10211)) {
            return false;
        }
        class_2338 below = pos.method_10074();
        return this.isReplantingSpot(below, BlockUtils.getState(below));
    }

    @Override
    protected PlantTypeSetting createSetting() {
        return new PlantTypeSetting("Bamboo", class_1802.field_8648, true, true);
    }
}
