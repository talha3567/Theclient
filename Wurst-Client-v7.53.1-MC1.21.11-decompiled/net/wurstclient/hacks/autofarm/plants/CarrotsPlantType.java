package net.wurstclient.hacks.autofarm.plants;

import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2271;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.wurstclient.hacks.autofarm.AutoFarmPlantType;
import net.wurstclient.settings.PlantTypeSetting;
import net.wurstclient.util.BlockUtils;

public final class CarrotsPlantType
extends AutoFarmPlantType {
    @Override
    public final boolean isReplantingSpot(class_2338 pos, class_2680 state) {
        return state.method_26204() instanceof class_2271;
    }

    @Override
    public final boolean hasPlantingSurface(class_2338 pos) {
        return BlockUtils.getState(pos.method_10074()).method_27852(class_2246.field_10362);
    }

    @Override
    public class_1792 getSeedItem() {
        return class_1802.field_8179;
    }

    @Override
    public boolean shouldHarvestByMining(class_2338 pos, class_2680 state) {
        class_2248 class_22482 = state.method_26204();
        if (!(class_22482 instanceof class_2271)) {
            return false;
        }
        class_2271 carrots = (class_2271)class_22482;
        return carrots.method_9825(state);
    }

    @Override
    protected PlantTypeSetting createSetting() {
        return new PlantTypeSetting("Carrots", class_1802.field_8179, true, true);
    }
}
