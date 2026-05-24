package net.wurstclient.hacks.autofarm.plants;

import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2282;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_2769;
import net.minecraft.class_3481;
import net.wurstclient.hacks.autofarm.AutoFarmPlantType;
import net.wurstclient.settings.PlantTypeSetting;
import net.wurstclient.util.BlockUtils;

public final class CocoaBeanPlantType
extends AutoFarmPlantType {
    @Override
    public final boolean isReplantingSpot(class_2338 pos, class_2680 state) {
        return state.method_26204() instanceof class_2282;
    }

    @Override
    public final boolean hasPlantingSurface(class_2338 pos) {
        return class_2350.class_2353.field_11062.method_29716().map(arg_0 -> ((class_2338)pos).method_10093(arg_0)).map(BlockUtils::getState).anyMatch(neighbor -> neighbor.method_26164(class_3481.field_15474));
    }

    @Override
    public class_1792 getSeedItem() {
        return class_1802.field_8116;
    }

    @Override
    public boolean shouldHarvestByMining(class_2338 pos, class_2680 state) {
        if (!(state.method_26204() instanceof class_2282)) {
            return false;
        }
        return (Integer)state.method_11654((class_2769)class_2282.field_10779) >= 2;
    }

    @Override
    protected PlantTypeSetting createSetting() {
        return new PlantTypeSetting("Cocoa beans", class_1802.field_8116, true, true);
    }
}
