package net.wurstclient.hacks.autofarm.plants;

import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.wurstclient.hacks.autofarm.AutoFarmPlantType;
import net.wurstclient.settings.PlantTypeSetting;

public final class AmethystPlantType
extends AutoFarmPlantType {
    @Override
    public final boolean isReplantingSpot(class_2338 pos, class_2680 state) {
        return state.method_27852(class_2246.field_27160);
    }

    @Override
    public final boolean hasPlantingSurface(class_2338 pos) {
        return true;
    }

    @Override
    public class_1792 getSeedItem() {
        return class_1802.field_27065;
    }

    @Override
    public boolean shouldHarvestByMining(class_2338 pos, class_2680 state) {
        return state.method_27852(class_2246.field_27161);
    }

    @Override
    protected PlantTypeSetting createSetting() {
        return new PlantTypeSetting("Amethyst", class_1802.field_27063, true, true);
    }
}
