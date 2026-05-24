package net.wurstclient.hacks.autofarm.plants;

import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2756;
import net.minecraft.class_2769;
import net.minecraft.class_8237;
import net.wurstclient.hacks.autofarm.AutoFarmPlantType;
import net.wurstclient.settings.PlantTypeSetting;
import net.wurstclient.util.BlockUtils;

public final class PitcherPlantPlantType
extends AutoFarmPlantType {
    @Override
    public final boolean isReplantingSpot(class_2338 pos, class_2680 state) {
        return state.method_27852(class_2246.field_43228) && state.method_11654((class_2769)class_8237.field_55784) == class_2756.field_12607 && this.hasPlantingSurface(pos);
    }

    @Override
    public final boolean hasPlantingSurface(class_2338 pos) {
        return BlockUtils.getState(pos.method_10074()).method_27852(class_2246.field_10362);
    }

    @Override
    public class_1792 getSeedItem() {
        return class_1802.field_43195;
    }

    @Override
    public boolean shouldHarvestByMining(class_2338 pos, class_2680 state) {
        return state.method_27852(class_2246.field_43228) && (Integer)state.method_11654((class_2769)class_8237.field_43239) >= 4;
    }

    @Override
    protected PlantTypeSetting createSetting() {
        return new PlantTypeSetting("Pitcher plants", class_1802.field_43192, true, true);
    }
}
