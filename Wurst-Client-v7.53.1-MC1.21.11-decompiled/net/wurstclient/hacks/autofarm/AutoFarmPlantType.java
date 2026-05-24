package net.wurstclient.hacks.autofarm;

import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.class_1792;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.wurstclient.settings.PlantTypeSetting;

public abstract class AutoFarmPlantType {
    private final PlantTypeSetting setting = Objects.requireNonNull(this.createSetting());

    public final boolean isHarvestingEnabled() {
        return this.setting.isHarvestingEnabled();
    }

    public final boolean isReplantingEnabled() {
        return this.setting.isReplantingEnabled();
    }

    public final Stream<PlantTypeSetting> getSettings() {
        return Stream.of(this.setting);
    }

    public abstract boolean isReplantingSpot(class_2338 var1, class_2680 var2);

    public abstract boolean hasPlantingSurface(class_2338 var1);

    public abstract class_1792 getSeedItem();

    public abstract boolean shouldHarvestByMining(class_2338 var1, class_2680 var2);

    public boolean shouldHarvestByInteracting(class_2338 pos, class_2680 state) {
        return false;
    }

    protected abstract PlantTypeSetting createSetting();
}
