package net.wurstclient.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.class_1792;
import net.minecraft.class_2960;
import net.minecraft.class_7923;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.Component;
import net.wurstclient.clickgui.components.ItemListEditButton;
import net.wurstclient.keybinds.PossibleKeybind;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.text.WText;

public final class ItemListSetting
extends Setting {
    private final ArrayList<String> itemNames = new ArrayList();
    private final String[] defaultNames;

    public ItemListSetting(String name, WText description, String ... items) {
        super(name, description);
        ((Stream)Arrays.stream(items).parallel()).map(s -> (class_1792)class_7923.field_41178.method_63535(class_2960.method_60654((String)s))).filter(Objects::nonNull).map(i -> class_7923.field_41178.method_10221(i).toString()).distinct().sorted().forEachOrdered(s -> this.itemNames.add((String)s));
        this.defaultNames = this.itemNames.toArray(new String[0]);
    }

    public ItemListSetting(String name, String descriptionKey, String ... items) {
        this(name, WText.translated(descriptionKey, new Object[0]), items);
    }

    public List<String> getItemNames() {
        return Collections.unmodifiableList(this.itemNames);
    }

    public void add(class_1792 item) {
        String name = class_7923.field_41178.method_10221((Object)item).toString();
        if (Collections.binarySearch(this.itemNames, name) >= 0) {
            return;
        }
        this.itemNames.add(name);
        Collections.sort(this.itemNames);
        WurstClient.INSTANCE.saveSettings();
    }

    public void remove(int index) {
        if (index < 0 || index >= this.itemNames.size()) {
            return;
        }
        this.itemNames.remove(index);
        WurstClient.INSTANCE.saveSettings();
    }

    public void resetToDefaults() {
        this.itemNames.clear();
        this.itemNames.addAll(Arrays.asList(this.defaultNames));
        WurstClient.INSTANCE.saveSettings();
    }

    @Override
    public Component getComponent() {
        return new ItemListEditButton(this);
    }

    @Override
    public void fromJson(JsonElement json) {
        try {
            this.itemNames.clear();
            if (JsonUtils.getAsString(json, "nope").equals("default")) {
                this.itemNames.addAll(Arrays.asList(this.defaultNames));
                return;
            }
            JsonUtils.getAsArray(json).getAllStrings().parallelStream().map(s -> (class_1792)class_7923.field_41178.method_63535(class_2960.method_60654((String)s))).filter(Objects::nonNull).map(i -> class_7923.field_41178.method_10221(i).toString()).distinct().sorted().forEachOrdered(s -> this.itemNames.add((String)s));
        }
        catch (JsonException e) {
            e.printStackTrace();
            this.resetToDefaults();
        }
    }

    @Override
    public JsonElement toJson() {
        if (this.itemNames.equals(Arrays.asList(this.defaultNames))) {
            return new JsonPrimitive("default");
        }
        JsonArray json = new JsonArray();
        this.itemNames.forEach(s -> json.add((String)s));
        return json;
    }

    @Override
    public JsonObject exportWikiData() {
        JsonObject json = new JsonObject();
        json.addProperty("name", this.getName());
        json.addProperty("description", this.getDescription());
        json.addProperty("type", "ItemList");
        JsonArray defaultItems = new JsonArray();
        Arrays.stream(this.defaultNames).forEachOrdered(s -> defaultItems.add((String)s));
        json.add("defaultItems", defaultItems);
        return json;
    }

    @Override
    public Set<PossibleKeybind> getPossibleKeybinds(String featureName) {
        String fullName = featureName + " " + this.getName();
        String command = ".itemlist " + featureName.toLowerCase() + " ";
        command = command + this.getName().toLowerCase().replace(" ", "_") + " ";
        LinkedHashSet<PossibleKeybind> pkb = new LinkedHashSet<PossibleKeybind>();
        pkb.add(new PossibleKeybind(command + "reset", "Reset " + fullName));
        return pkb;
    }
}
