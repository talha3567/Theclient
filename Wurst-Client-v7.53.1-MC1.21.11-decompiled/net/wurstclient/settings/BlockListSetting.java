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
import net.minecraft.class_2248;
import net.minecraft.class_2960;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.Component;
import net.wurstclient.clickgui.components.BlockListEditButton;
import net.wurstclient.keybinds.PossibleKeybind;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.text.WText;

public class BlockListSetting
extends Setting {
    private final ArrayList<String> blockNames = new ArrayList();
    private final String[] defaultNames;

    public BlockListSetting(String name, WText description, String ... blocks) {
        super(name, description);
        ((Stream)Arrays.stream(blocks).parallel()).map(BlockUtils::getBlockFromNameOrID).filter(Objects::nonNull).map(BlockUtils::getName).distinct().sorted().forEachOrdered(s -> this.blockNames.add((String)s));
        this.defaultNames = this.blockNames.toArray(new String[0]);
    }

    public BlockListSetting(String name, String descriptionKey, String ... blocks) {
        this(name, WText.translated(descriptionKey, new Object[0]), blocks);
    }

    public List<String> getBlockNames() {
        return Collections.unmodifiableList(this.blockNames);
    }

    public int indexOf(String name) {
        if (name == null) {
            return -1;
        }
        return Collections.binarySearch(this.blockNames, name);
    }

    public int indexOf(class_2248 block) {
        return this.indexOf(BlockUtils.getName(block));
    }

    public boolean contains(String name) {
        return this.indexOf(name) >= 0;
    }

    public boolean contains(class_2248 block) {
        return this.indexOf(block) >= 0;
    }

    public int size() {
        return this.blockNames.size();
    }

    public void add(class_2248 block) {
        String name = BlockUtils.getName(block);
        if (Collections.binarySearch(this.blockNames, name) >= 0) {
            return;
        }
        this.blockNames.add(name);
        Collections.sort(this.blockNames);
        WurstClient.INSTANCE.saveSettings();
    }

    public void remove(int index) {
        if (index < 0 || index >= this.blockNames.size()) {
            return;
        }
        this.blockNames.remove(index);
        WurstClient.INSTANCE.saveSettings();
    }

    public void resetToDefaults() {
        this.blockNames.clear();
        this.blockNames.addAll(Arrays.asList(this.defaultNames));
        WurstClient.INSTANCE.saveSettings();
    }

    @Override
    public Component getComponent() {
        return new BlockListEditButton(this);
    }

    @Override
    public void fromJson(JsonElement json) {
        try {
            this.blockNames.clear();
            if (JsonUtils.getAsString(json, "nope").equals("default")) {
                this.blockNames.addAll(Arrays.asList(this.defaultNames));
                return;
            }
            for (String rawName : JsonUtils.getAsArray(json).getAllStrings()) {
                class_2960 id = class_2960.method_12829((String)rawName);
                if (id == null) {
                    System.out.println("Discarding BlockList entry \"" + rawName + "\" as it is not a valid identifier");
                    continue;
                }
                String name = id.toString();
                if (this.blockNames.contains(name)) {
                    System.out.println("Discarding BlockList entry \"" + rawName + "\" as \"" + name + "\" is already in the list");
                    continue;
                }
                this.blockNames.add(name);
            }
            this.blockNames.sort(null);
        }
        catch (JsonException e) {
            e.printStackTrace();
            this.resetToDefaults();
        }
    }

    @Override
    public JsonElement toJson() {
        if (this.blockNames.equals(Arrays.asList(this.defaultNames))) {
            return new JsonPrimitive("default");
        }
        JsonArray json = new JsonArray();
        this.blockNames.forEach(s -> json.add((String)s));
        return json;
    }

    @Override
    public JsonObject exportWikiData() {
        JsonObject json = new JsonObject();
        json.addProperty("name", this.getName());
        json.addProperty("description", this.getDescription());
        json.addProperty("type", "BlockList");
        JsonArray defaultBlocksJson = new JsonArray();
        for (String blockName : this.defaultNames) {
            defaultBlocksJson.add(blockName);
        }
        json.add("defaultBlocks", defaultBlocksJson);
        return json;
    }

    @Override
    public Set<PossibleKeybind> getPossibleKeybinds(String featureName) {
        String fullName = featureName + " " + this.getName();
        String command = ".blocklist " + featureName.toLowerCase() + " ";
        command = command + this.getName().toLowerCase().replace(" ", "_") + " ";
        LinkedHashSet<PossibleKeybind> pkb = new LinkedHashSet<PossibleKeybind>();
        pkb.add(new PossibleKeybind(command + "reset", "Reset " + fullName));
        return pkb;
    }
}
