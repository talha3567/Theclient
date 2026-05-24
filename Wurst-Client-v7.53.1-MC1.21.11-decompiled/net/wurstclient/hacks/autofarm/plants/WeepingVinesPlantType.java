package net.wurstclient.hacks.autofarm.plants;

import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_1922;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.autofarm.AutoFarmPlantType;
import net.wurstclient.settings.PlantTypeSetting;
import net.wurstclient.util.BlockUtils;

public final class WeepingVinesPlantType
extends AutoFarmPlantType {
    @Override
    public final boolean isReplantingSpot(class_2338 pos, class_2680 state) {
        return (state.method_27852(class_2246.field_22123) || state.method_27852(class_2246.field_22124)) && this.hasPlantingSurface(pos);
    }

    @Override
    public final boolean hasPlantingSurface(class_2338 pos) {
        class_2680 ceiling = BlockUtils.getState(pos.method_10084());
        return !ceiling.method_27852(class_2246.field_22123) && !ceiling.method_27852(class_2246.field_22124) && ceiling.method_26206((class_1922)WurstClient.MC.field_1687, pos, class_2350.field_11033);
    }

    @Override
    public class_1792 getSeedItem() {
        return class_1802.field_21992;
    }

    @Override
    public boolean shouldHarvestByMining(class_2338 pos, class_2680 state) {
        return (state.method_27852(class_2246.field_22123) || state.method_27852(class_2246.field_22124)) && !this.isReplantingSpot(pos, state);
    }

    @Override
    protected PlantTypeSetting createSetting() {
        return new PlantTypeSetting("Weeping vines", class_1802.field_21992, false, false);
    }
}
