package net.wurstclient.hacks.autofarm.plants;

import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2769;
import net.minecraft.class_3481;
import net.minecraft.class_3830;
import net.wurstclient.hacks.autofarm.AutoFarmPlantType;
import net.wurstclient.settings.PlantTypeSetting;
import net.wurstclient.util.BlockUtils;

public final class SweetBerryPlantType
extends AutoFarmPlantType {
    @Override
    public final boolean isReplantingSpot(class_2338 pos, class_2680 state) {
        return state.method_26204() instanceof class_3830 && this.hasPlantingSurface(pos);
    }

    @Override
    public final boolean hasPlantingSurface(class_2338 pos) {
        class_2680 floor = BlockUtils.getState(pos.method_10074());
        return floor.method_26164(class_3481.field_29822) || floor.method_27852(class_2246.field_10362);
    }

    @Override
    public class_1792 getSeedItem() {
        return class_1802.field_16998;
    }

    @Override
    public boolean shouldHarvestByMining(class_2338 pos, class_2680 state) {
        return false;
    }

    @Override
    public boolean shouldHarvestByInteracting(class_2338 pos, class_2680 state) {
        if (!(state.method_26204() instanceof class_3830)) {
            return false;
        }
        return (Integer)state.method_11654((class_2769)class_3830.field_17000) > 1;
    }

    @Override
    protected PlantTypeSetting createSetting() {
        return new PlantTypeSetting("Sweet berries", class_1802.field_16998, true, true);
    }
}
