package net.wurstclient.hacks.autofarm.plants;

import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2439;
import net.minecraft.class_2680;
import net.wurstclient.hacks.autofarm.AutoFarmPlantType;
import net.wurstclient.settings.PlantTypeSetting;
import net.wurstclient.util.BlockUtils;

public final class PotatoesPlantType
extends AutoFarmPlantType {
    @Override
    public final boolean isReplantingSpot(class_2338 pos, class_2680 state) {
        return state.method_26204() instanceof class_2439;
    }

    @Override
    public final boolean hasPlantingSurface(class_2338 pos) {
        return BlockUtils.getState(pos.method_10074()).method_27852(class_2246.field_10362);
    }

    @Override
    public class_1792 getSeedItem() {
        return class_1802.field_8567;
    }

    @Override
    public boolean shouldHarvestByMining(class_2338 pos, class_2680 state) {
        class_2248 class_22482 = state.method_26204();
        if (!(class_22482 instanceof class_2439)) {
            return false;
        }
        class_2439 potato = (class_2439)class_22482;
        return potato.method_9825(state);
    }

    @Override
    protected PlantTypeSetting createSetting() {
        return new PlantTypeSetting("Potatoes", class_1802.field_8567, true, true);
    }
}
