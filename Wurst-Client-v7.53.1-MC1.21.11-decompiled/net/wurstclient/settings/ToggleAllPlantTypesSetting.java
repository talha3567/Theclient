package net.wurstclient.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.Component;
import net.wurstclient.clickgui.components.ToggleAllPlantTypesComponent;
import net.wurstclient.keybinds.PossibleKeybind;
import net.wurstclient.settings.PlantTypeSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.text.WText;

public final class ToggleAllPlantTypesSetting
extends Setting {
    private final List<PlantTypeSetting> plantTypes;

    public ToggleAllPlantTypesSetting(String name, Stream<PlantTypeSetting> plantTypes) {
        super(name, WText.empty());
        this.plantTypes = plantTypes.toList();
    }

    public Boolean isHarvestingEnabled() {
        boolean allEnabled = this.plantTypes.stream().allMatch(PlantTypeSetting::isHarvestingEnabled);
        boolean allDisabled = this.plantTypes.stream().allMatch(type -> !type.isHarvestingEnabled());
        return allEnabled ? Boolean.TRUE : (allDisabled ? Boolean.FALSE : null);
    }

    public Boolean isReplantingEnabled() {
        boolean allEnabled = this.plantTypes.stream().allMatch(PlantTypeSetting::isReplantingEnabled);
        boolean allDisabled = this.plantTypes.stream().allMatch(type -> !type.isReplantingEnabled());
        return allEnabled ? Boolean.TRUE : (allDisabled ? Boolean.FALSE : null);
    }

    public void setHarvestingEnabled(boolean harvest) {
        this.plantTypes.forEach(type -> type.setHarvestingEnabledWithoutSaving(harvest));
        WurstClient.INSTANCE.saveSettings();
    }

    public void setReplantingEnabled(boolean replant) {
        this.plantTypes.forEach(type -> type.setReplantingEnabledWithoutSaving(replant));
        WurstClient.INSTANCE.saveSettings();
    }

    public void toggleHarvestingEnabled() {
        Boolean enabled = this.isHarvestingEnabled();
        if (enabled == null || enabled == Boolean.FALSE) {
            this.setHarvestingEnabled(true);
        } else {
            this.setHarvestingEnabled(false);
        }
    }

    public void toggleReplantingEnabled() {
        Boolean enabled = this.isReplantingEnabled();
        if (enabled == null || enabled == Boolean.FALSE) {
            this.setReplantingEnabled(true);
        } else {
            this.setReplantingEnabled(false);
        }
    }

    public void resetHarvestingEnabled() {
        for (PlantTypeSetting type : this.plantTypes) {
            type.setHarvestingEnabledWithoutSaving(type.isHarvestingEnabledByDefault());
        }
        WurstClient.INSTANCE.saveSettings();
    }

    public void resetReplantingEnabled() {
        for (PlantTypeSetting type : this.plantTypes) {
            type.setReplantingEnabledWithoutSaving(type.isReplantingEnabledByDefault());
        }
        WurstClient.INSTANCE.saveSettings();
    }

    @Override
    public Component getComponent() {
        return new ToggleAllPlantTypesComponent(this);
    }

    @Override
    public void fromJson(JsonElement json) {
    }

    @Override
    public JsonElement toJson() {
        return JsonNull.INSTANCE;
    }

    @Override
    public JsonObject exportWikiData() {
        return new JsonObject();
    }

    @Override
    public Set<PossibleKeybind> getPossibleKeybinds(String featureName) {
        return new LinkedHashSet<PossibleKeybind>();
    }
}
