package net.wurstclient.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1935;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.Component;
import net.wurstclient.clickgui.components.PlantTypeComponent;
import net.wurstclient.keybinds.PossibleKeybind;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.json.WsonObject;
import net.wurstclient.util.text.WText;

public final class PlantTypeSetting
extends Setting {
    private final class_1799 icon;
    private boolean harvest;
    private final boolean harvestByDefault;
    private boolean replant;
    private final boolean replantByDefault;

    public PlantTypeSetting(String name, WText description, class_1792 icon, boolean harvest, boolean replant) {
        super(name, description);
        this.icon = new class_1799((class_1935)icon);
        this.harvest = harvest;
        this.harvestByDefault = harvest;
        this.replant = replant;
        this.replantByDefault = replant;
    }

    public PlantTypeSetting(String name, String descriptionKey, class_1792 icon, boolean harvest, boolean replant) {
        this(name, WText.translated(descriptionKey, new Object[0]), icon, harvest, replant);
    }

    public PlantTypeSetting(String name, class_1792 icon, boolean harvest, boolean replant) {
        this(name, WText.empty(), icon, harvest, replant);
    }

    public class_1799 getIcon() {
        return this.icon;
    }

    public boolean isHarvestingEnabled() {
        return this.harvest;
    }

    public boolean isHarvestingEnabledByDefault() {
        return this.harvestByDefault;
    }

    public boolean isReplantingEnabled() {
        return this.replant;
    }

    public boolean isReplantingEnabledByDefault() {
        return this.replantByDefault;
    }

    public void setHarvestingEnabled(boolean harvest) {
        this.setHarvestingEnabledWithoutSaving(harvest);
        WurstClient.INSTANCE.saveSettings();
    }

    void setHarvestingEnabledWithoutSaving(boolean harvest) {
        this.harvest = harvest;
        this.update();
    }

    public void setReplantingEnabled(boolean replant) {
        this.setReplantingEnabledWithoutSaving(replant);
        WurstClient.INSTANCE.saveSettings();
    }

    void setReplantingEnabledWithoutSaving(boolean replant) {
        this.replant = replant;
        this.update();
    }

    public void toggleHarvestingEnabled() {
        this.setHarvestingEnabled(!this.isHarvestingEnabled());
    }

    public void toggleReplantingEnabled() {
        this.setReplantingEnabled(!this.isReplantingEnabled());
    }

    public void resetHarvestingEnabled() {
        this.setHarvestingEnabled(this.isHarvestingEnabledByDefault());
    }

    public void resetReplantingEnabled() {
        this.setReplantingEnabled(this.isReplantingEnabledByDefault());
    }

    @Override
    public Component getComponent() {
        return new PlantTypeComponent(this);
    }

    @Override
    public void fromJson(JsonElement json) {
        if (JsonUtils.getAsString(json, "nope").equals("default")) {
            this.harvest = this.harvestByDefault;
            this.replant = this.replantByDefault;
            return;
        }
        try {
            WsonObject object = JsonUtils.getAsObject(json);
            this.harvest = object.getBoolean("harvest");
            this.replant = object.getBoolean("replant");
        }
        catch (JsonException e) {
            e.printStackTrace();
        }
    }

    @Override
    public JsonElement toJson() {
        if (this.harvest == this.harvestByDefault && this.replant == this.replantByDefault) {
            return new JsonPrimitive("default");
        }
        JsonObject json = new JsonObject();
        json.addProperty("harvest", this.harvest);
        json.addProperty("replant", this.replant);
        return json;
    }

    @Override
    public JsonObject exportWikiData() {
        JsonObject json = new JsonObject();
        json.addProperty("name", this.getName());
        json.addProperty("description", this.getDescription());
        json.addProperty("type", "PlantType");
        json.addProperty("harvestByDefault", this.harvestByDefault);
        json.addProperty("replantByDefault", this.replantByDefault);
        return json;
    }

    @Override
    public Set<PossibleKeybind> getPossibleKeybinds(String featureName) {
        return new LinkedHashSet<PossibleKeybind>();
    }
}
