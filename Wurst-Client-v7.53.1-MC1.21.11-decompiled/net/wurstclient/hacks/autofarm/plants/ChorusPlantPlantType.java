package net.wurstclient.hacks.autofarm.plants;

import com.google.common.collect.Maps;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2279;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2429;
import net.minecraft.class_2680;
import net.minecraft.class_2746;
import net.minecraft.class_2769;
import net.wurstclient.hacks.autofarm.AutoFarmPlantType;
import net.wurstclient.settings.PlantTypeSetting;
import net.wurstclient.util.BlockUtils;

public final class ChorusPlantPlantType
extends AutoFarmPlantType {
    private static final EnumMap<class_2350, class_2746> CHORUS_GROWING_DIRECTIONS = Maps.newEnumMap(Map.of(class_2350.field_11043, class_2429.field_11332, class_2350.field_11035, class_2429.field_11331, class_2350.field_11039, class_2429.field_11328, class_2350.field_11034, class_2429.field_11335, class_2350.field_11036, class_2429.field_11327));

    @Override
    public final boolean isReplantingSpot(class_2338 pos, class_2680 state) {
        return (state.method_27852(class_2246.field_10528) || state.method_27852(class_2246.field_10021)) && this.hasPlantingSurface(pos);
    }

    @Override
    public final boolean hasPlantingSurface(class_2338 pos) {
        return BlockUtils.getState(pos.method_10074()).method_27852(class_2246.field_10471);
    }

    @Override
    public class_1792 getSeedItem() {
        return class_1802.field_8710;
    }

    @Override
    public boolean shouldHarvestByMining(class_2338 pos, class_2680 state) {
        if (state.method_27852(class_2246.field_10528)) {
            return this.isFlowerFullyGrown(pos, state);
        }
        if (state.method_27852(class_2246.field_10021)) {
            return !this.hasAttachedFlowers(pos, state, new HashSet<class_2338>());
        }
        return false;
    }

    private boolean isFlowerFullyGrown(class_2338 pos, class_2680 state) {
        return (Integer)state.method_61767((class_2769)class_2279.field_10762, (Comparable)Integer.valueOf(0)) == 5 || !BlockUtils.getState(pos.method_10084()).method_26215();
    }

    private boolean hasAttachedFlowers(class_2338 pos, class_2680 state, HashSet<class_2338> visited) {
        if (visited.size() > 1000) {
            return true;
        }
        if (!visited.add(pos)) {
            return false;
        }
        for (Map.Entry<class_2350, class_2746> entry : CHORUS_GROWING_DIRECTIONS.entrySet()) {
            if (!((Boolean)state.method_61767((class_2769)entry.getValue(), (Comparable)Boolean.valueOf(false))).booleanValue()) continue;
            class_2350 direction = entry.getKey();
            class_2338 neighborPos = pos.method_10093(direction);
            class_2680 neighborState = BlockUtils.getState(neighborPos);
            if (neighborState.method_27852(class_2246.field_10528)) {
                return true;
            }
            if (!neighborState.method_27852(class_2246.field_10021) || direction.method_10166().method_10179() && ((Boolean)neighborState.method_61767((class_2769)class_2429.field_11330, (Comparable)Boolean.valueOf(false))).booleanValue() || !this.hasAttachedFlowers(neighborPos, neighborState, visited)) continue;
            return true;
        }
        return false;
    }

    @Override
    protected PlantTypeSetting createSetting() {
        return new PlantTypeSetting("Chorus plants", class_1802.field_8710, true, true);
    }
}
