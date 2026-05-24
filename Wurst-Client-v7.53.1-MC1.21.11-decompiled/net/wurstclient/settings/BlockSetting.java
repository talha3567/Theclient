package net.wurstclient.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import net.minecraft.class_2189;
import net.minecraft.class_2248;
import net.minecraft.class_2960;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.Component;
import net.wurstclient.clickgui.components.BlockComponent;
import net.wurstclient.keybinds.PossibleKeybind;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.text.WText;

public final class BlockSetting
extends Setting {
    private String blockName = "";
    private final String defaultName;
    private final boolean allowAir;

    public BlockSetting(String name, WText description, String blockName, boolean allowAir) {
        super(name, description);
        class_2248 block = BlockUtils.getBlockFromNameOrID(blockName);
        Objects.requireNonNull(block);
        this.defaultName = this.blockName = BlockUtils.getName(block);
        this.allowAir = allowAir;
    }

    public BlockSetting(String name, String descriptionKey, String blockName, boolean allowAir) {
        this(name, WText.translated(descriptionKey, new Object[0]), blockName, allowAir);
    }

    public BlockSetting(String name, String blockName, boolean allowAir) {
        this(name, WText.empty(), blockName, allowAir);
    }

    public class_2248 getBlock() {
        return BlockUtils.getBlockFromName(this.blockName);
    }

    public String getBlockName() {
        return this.blockName;
    }

    public String getShortBlockName() {
        return this.blockName.replace("minecraft:", "");
    }

    public void setBlock(class_2248 block) {
        if (block == null) {
            return;
        }
        if (!this.allowAir && block instanceof class_2189) {
            return;
        }
        String newName = Objects.requireNonNull(BlockUtils.getName(block));
        if (this.blockName.equals(newName)) {
            return;
        }
        this.blockName = newName;
        WurstClient.INSTANCE.saveSettings();
    }

    public void setBlockName(String blockName) {
        class_2248 block = BlockUtils.getBlockFromNameOrID(blockName);
        Objects.requireNonNull(block);
        this.setBlock(block);
    }

    public void resetToDefault() {
        this.blockName = this.defaultName;
        WurstClient.INSTANCE.saveSettings();
    }

    @Override
    public Component getComponent() {
        return new BlockComponent(this);
    }

    @Override
    public void fromJson(JsonElement json) {
        try {
            String rawName = JsonUtils.getAsString(json);
            class_2960 id = class_2960.method_12829((String)rawName);
            if (id == null) {
                throw new JsonException("Discarding Block \"" + rawName + "\" as it is not a valid identifier");
            }
            String name = id.toString();
            if (!this.allowAir && "minecraft:air".equals(name)) {
                throw new JsonException("Discarding Block \"" + rawName + "\" as this setting does not allow air blocks");
            }
            this.blockName = name;
        }
        catch (JsonException e) {
            e.printStackTrace();
            this.resetToDefault();
        }
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(this.blockName);
    }

    @Override
    public JsonObject exportWikiData() {
        JsonObject json = new JsonObject();
        json.addProperty("name", this.getName());
        json.addProperty("description", this.getDescription());
        json.addProperty("type", "Block");
        json.addProperty("defaultValue", this.defaultName);
        json.addProperty("allowAir", this.allowAir);
        return json;
    }

    @Override
    public Set<PossibleKeybind> getPossibleKeybinds(String featureName) {
        String fullName = featureName + " " + this.getName();
        String command = ".setblock " + featureName.toLowerCase() + " ";
        command = command + this.getName().toLowerCase().replace(" ", "_") + " ";
        LinkedHashSet<PossibleKeybind> pkb = new LinkedHashSet<PossibleKeybind>();
        pkb.add(new PossibleKeybind(command + "reset", "Reset " + fullName));
        return pkb;
    }
}
