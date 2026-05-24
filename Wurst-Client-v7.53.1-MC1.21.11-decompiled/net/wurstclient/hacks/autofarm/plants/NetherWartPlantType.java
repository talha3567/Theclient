package net.wurstclient.hacks.autofarm.plants;

import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2421;
import net.minecraft.class_2680;
import net.minecraft.class_2769;
import net.wurstclient.hacks.autofarm.AutoFarmPlantType;
import net.wurstclient.settings.PlantTypeSetting;
import net.wurstclient.util.BlockUtils;

public final class NetherWartPlantType
extends AutoFarmPlantType {
    @Override
    public final boolean isReplantingSpot(class_2338 pos, class_2680 state) {
        return state.method_26204() instanceof class_2421;
    }

    @Override
    public final boolean hasPlantingSurface(class_2338 pos) {
        return BlockUtils.getState(pos.method_10074()).method_27852(class_2246.field_10114);
    }

    @Override
    public class_1792 getSeedItem() {
        return class_1802.field_8790;
    }

    @Override
    public boolean shouldHarvestByMining(class_2338 pos, class_2680 state) {
        if (!(state.method_26204() instanceof class_2421)) {
            return false;
        }
        return (Integer)state.method_11654((class_2769)class_2421.field_11306) >= 3;
    }

    @Override
    protected PlantTypeSetting createSetting() {
        return new PlantTypeSetting("Nether warts", class_1802.field_8790, true, true);
    }
}
