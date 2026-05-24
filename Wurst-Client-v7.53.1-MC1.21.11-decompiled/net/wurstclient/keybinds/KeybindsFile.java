package net.wurstclient.keybinds;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_3675;
import net.wurstclient.keybinds.Keybind;
import net.wurstclient.keybinds.KeybindList;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.json.WsonObject;

public final class KeybindsFile {
    private final Path path;

    public KeybindsFile(Path path) {
        this.path = path;
    }

    public void load(KeybindList list) {
        try {
            Set<Keybind> newKeybinds = this.parseFile(this.path);
            if (newKeybinds.isEmpty()) {
                newKeybinds = KeybindList.DEFAULT_KEYBINDS;
            }
            list.setKeybinds(newKeybinds);
        }
        catch (NoSuchFileException e) {
            list.setKeybinds(KeybindList.DEFAULT_KEYBINDS);
        }
        catch (IOException | JsonException e) {
            System.out.println("Couldn't load " + String.valueOf(this.path.getFileName()));
            e.printStackTrace();
            list.setKeybinds(KeybindList.DEFAULT_KEYBINDS);
        }
    }

    public void loadProfile(KeybindList list, Path profilePath) throws IOException, JsonException {
        if (!profilePath.getFileName().toString().endsWith(".json")) {
            throw new IllegalArgumentException();
        }
        list.setKeybinds(this.parseFile(profilePath));
    }

    private Set<Keybind> parseFile(Path path) throws IOException, JsonException {
        WsonObject wson = JsonUtils.parseFileToObject(path);
        HashSet<Keybind> newKeybinds = new HashSet<Keybind>();
        for (Map.Entry<String, String> entry : wson.getAllStrings().entrySet()) {
            String keyName = entry.getKey();
            String commands = entry.getValue();
            if (!this.isValidKeyName(keyName)) continue;
            Keybind keybind = new Keybind(keyName, commands);
            newKeybinds.add(keybind);
        }
        return newKeybinds;
    }

    private boolean isValidKeyName(String key) {
        try {
            class_3675.method_15981((String)key);
            return true;
        }
        catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void save(KeybindList list) {
        JsonObject json = this.createJson(list);
        try {
            JsonUtils.toJson(json, this.path);
        }
        catch (IOException | JsonException e) {
            System.out.println("Couldn't save " + String.valueOf(this.path.getFileName()));
            e.printStackTrace();
        }
    }

    public void saveProfile(KeybindList list, Path profilePath) throws IOException, JsonException {
        if (!profilePath.getFileName().toString().endsWith(".json")) {
            throw new IllegalArgumentException();
        }
        JsonObject json = this.createJson(list);
        Files.createDirectories(profilePath.getParent(), new FileAttribute[0]);
        JsonUtils.toJson(json, profilePath);
    }

    private JsonObject createJson(KeybindList list) {
        JsonObject json = new JsonObject();
        for (Keybind kb : list.getAllKeybinds()) {
            json.addProperty(kb.getKey(), kb.getCommands());
        }
        return json;
    }
}
