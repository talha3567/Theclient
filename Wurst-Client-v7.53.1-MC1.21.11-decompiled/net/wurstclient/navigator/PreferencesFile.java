package net.wurstclient.navigator;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.json.WsonObject;

public final class PreferencesFile {
    private final Path path;
    private final HashMap<String, Long> preferences;

    public PreferencesFile(Path path, HashMap<String, Long> preferences) {
        this.path = path;
        this.preferences = preferences;
    }

    public void load() {
        try {
            WsonObject wson = JsonUtils.parseFileToObject(this.path);
            for (Map.Entry<String, Number> e : wson.getAllNumbers().entrySet()) {
                this.preferences.put(e.getKey(), e.getValue().longValue());
            }
        }
        catch (NoSuchFileException wson) {
        }
        catch (IOException | JsonException e) {
            System.out.println("Couldn't load " + String.valueOf(this.path.getFileName()));
            e.printStackTrace();
        }
        this.save();
    }

    public void save() {
        JsonObject json = this.createJson();
        try {
            JsonUtils.toJson(json, this.path);
        }
        catch (IOException | JsonException e) {
            System.out.println("Couldn't save " + String.valueOf(this.path.getFileName()));
            e.printStackTrace();
        }
    }

    private JsonObject createJson() {
        JsonObject json = new JsonObject();
        for (Map.Entry<String, Long> e : this.preferences.entrySet()) {
            json.addProperty(e.getKey(), e.getValue());
        }
        return json;
    }
}
