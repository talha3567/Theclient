package net.wurstclient.hacks.autofarm.plants;

import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.wurstclient.hacks.autofarm.AutoFarmPlantType;
import net.wurstclient.settings.PlantTypeSetting;
import net.wurstclient.util.BlockUtils;

public final class PumpkinPlantType
extends AutoFarmPlantType {
    @Override
    public final boolean isReplantingSpot(class_2338 pos, class_2680 state) {
        return state.method_27852(class_2246.field_46286) || state.method_27852(class_2246.field_46284);
    }

    @Override
    public final boolean hasPlantingSurface(class_2338 pos) {
        return BlockUtils.getState(pos.method_10074()).method_27852(class_2246.field_10362);
    }

    @Override
    public class_1792 getSeedItem() {
        return class_1802.field_46249;
    }

    @Override
    public boolean shouldHarvestByMining(class_2338 pos, class_2680 state) {
        return state.method_27852(class_2246.field_46282);
    }

    @Override
    protected PlantTypeSetting createSetting() {
        return new PlantTypeSetting("Pumpkins", class_1802.field_17518, true, true);
    }
}
