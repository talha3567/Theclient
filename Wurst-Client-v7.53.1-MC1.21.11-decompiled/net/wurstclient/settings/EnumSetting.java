package net.wurstclient.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.LinkedHashSet;
import java.util.Objects;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.Component;
import net.wurstclient.clickgui.components.ComboBoxComponent;
import net.wurstclient.keybinds.PossibleKeybind;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.text.WText;

public class EnumSetting<T extends Enum<T>>
extends Setting {
    private final T[] values;
    private T selected;
    private final T defaultSelected;

    public EnumSetting(String name, WText description, T[] values, T selected) {
        super(name, description);
        this.values = (Enum[])Objects.requireNonNull(values);
        this.selected = (Enum)Objects.requireNonNull(selected);
        this.defaultSelected = selected;
    }

    public EnumSetting(String name, String descriptionKey, T[] values, T selected) {
        this(name, WText.translated(descriptionKey, new Object[0]), (Enum[])values, (Enum)selected);
    }

    public EnumSetting(String name, T[] values, T selected) {
        this(name, WText.empty(), (Enum[])values, (Enum)selected);
    }

    public T[] getValues() {
        return this.values;
    }

    public T getSelected() {
        return this.selected;
    }

    public T getDefaultSelected() {
        return this.defaultSelected;
    }

    public void setSelected(T selected) {
        this.selected = (Enum)Objects.requireNonNull(selected);
        WurstClient.INSTANCE.saveSettings();
    }

    public boolean setSelected(String selected) {
        for (T value : this.values) {
            if (!((Enum)value).toString().equalsIgnoreCase(selected)) continue;
            this.setSelected(value);
            return true;
        }
        return false;
    }

    public void selectNext() {
        int next = ((Enum)this.selected).ordinal() + 1;
        if (next >= this.values.length) {
            next = 0;
        }
        this.setSelected(this.values[next]);
    }

    public void selectPrev() {
        int prev = ((Enum)this.selected).ordinal() - 1;
        if (prev < 0) {
            prev = this.values.length - 1;
        }
        this.setSelected(this.values[prev]);
    }

    @Override
    public Component getComponent() {
        return new ComboBoxComponent(this);
    }

    @Override
    public void fromJson(JsonElement json) {
        if (!JsonUtils.isString(json)) {
            return;
        }
        this.setSelected(json.getAsString());
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(((Enum)this.selected).toString());
    }

    @Override
    public JsonObject exportWikiData() {
        JsonObject json = new JsonObject();
        json.addProperty("name", this.getName());
        json.addProperty("description", this.getDescription());
        json.addProperty("type", "Enum");
        JsonArray values = new JsonArray();
        for (T value : this.values) {
            values.add(((Enum)value).toString());
        }
        json.add("values", values);
        json.addProperty("defaultValue", ((Enum)this.defaultSelected).toString());
        return json;
    }

    public LinkedHashSet<PossibleKeybind> getPossibleKeybinds(String featureName) {
        String fullName = featureName + " " + this.getName();
        String command = ".setmode " + featureName.toLowerCase() + " " + this.getName().toLowerCase().replace(" ", "_") + " ";
        String description = "Set " + fullName + " to ";
        LinkedHashSet<PossibleKeybind> pkb = new LinkedHashSet<PossibleKeybind>();
        pkb.add(new PossibleKeybind(command + "next", "Next " + fullName));
        pkb.add(new PossibleKeybind(command + "prev", "Previous " + fullName));
        for (T v : this.values) {
            String vName = ((Enum)v).toString().replace(" ", "_").toLowerCase();
            pkb.add(new PossibleKeybind(command + vName, description + String.valueOf(v)));
        }
        return pkb;
    }
}
