package net.wurstclient.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.Component;
import net.wurstclient.clickgui.components.TextFieldEditButton;
import net.wurstclient.keybinds.PossibleKeybind;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.text.WText;

public final class TextFieldSetting
extends Setting {
    private String value = "";
    private final String defaultValue;
    private final Predicate<String> validator;

    public TextFieldSetting(String name, WText description, String defaultValue, Predicate<String> validator) {
        super(name, description);
        Objects.requireNonNull(defaultValue);
        Objects.requireNonNull(validator);
        if (!validator.test(defaultValue)) {
            throw new IllegalArgumentException("Default value is not valid: " + defaultValue);
        }
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.validator = validator;
    }

    public TextFieldSetting(String name, String descriptionKey, String defaultValue, Predicate<String> validator) {
        this(name, WText.translated(descriptionKey, new Object[0]), defaultValue, validator);
    }

    public TextFieldSetting(String name, String defaultValue, Predicate<String> validator) {
        this(name, WText.empty(), defaultValue, validator);
    }

    public TextFieldSetting(String name, WText description, String defaultValue) {
        this(name, description, defaultValue, (String s) -> true);
    }

    public TextFieldSetting(String name, String descriptionKey, String defaultValue) {
        this(name, WText.translated(descriptionKey, new Object[0]), defaultValue, (String s) -> true);
    }

    public TextFieldSetting(String name, String defaultValue) {
        this(name, WText.empty(), defaultValue, (String s) -> true);
    }

    public String getValue() {
        return this.value;
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public void setValue(String value) {
        if (value == null) {
            return;
        }
        if (this.value.equals(value)) {
            return;
        }
        if (!this.validator.test(value)) {
            return;
        }
        this.value = value;
        WurstClient.INSTANCE.saveSettings();
    }

    public void resetToDefault() {
        this.value = this.defaultValue;
        WurstClient.INSTANCE.saveSettings();
    }

    @Override
    public Component getComponent() {
        return new TextFieldEditButton(this);
    }

    @Override
    public void fromJson(JsonElement json) {
        try {
            String newValue = JsonUtils.getAsString(json);
            if (!this.validator.test(newValue)) {
                throw new JsonException();
            }
            this.value = newValue;
        }
        catch (JsonException e) {
            e.printStackTrace();
            this.resetToDefault();
        }
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(this.value);
    }

    @Override
    public JsonObject exportWikiData() {
        JsonObject json = new JsonObject();
        json.addProperty("name", this.getName());
        json.addProperty("description", this.getDescription());
        json.addProperty("type", "TextField");
        json.addProperty("defaultValue", this.defaultValue);
        return json;
    }

    @Override
    public Set<PossibleKeybind> getPossibleKeybinds(String featureName) {
        return new LinkedHashSet<PossibleKeybind>();
    }
}
