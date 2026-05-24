package net.wurstclient.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.wurstclient.Feature;
import net.wurstclient.command.CmdList;
import net.wurstclient.command.Command;
import net.wurstclient.hack.Hack;
import net.wurstclient.hack.HackList;
import net.wurstclient.other_feature.OtfList;
import net.wurstclient.other_feature.OtherFeature;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.json.WsonObject;

public final class SettingsFile {
    private final Path path;
    private final Map<String, Feature> featuresWithSettings;
    private boolean disableSaving;

    public SettingsFile(Path path, HackList hax, CmdList cmds, OtfList otfs) {
        this.path = path;
        this.featuresWithSettings = this.createFeatureMap(hax, cmds, otfs);
    }

    private Map<String, Feature> createFeatureMap(HackList hax, CmdList cmds, OtfList otfs) {
        LinkedHashMap<String, Feature> map = new LinkedHashMap<String, Feature>();
        for (Hack hack : hax.getAllHax()) {
            if (hack.getSettings().isEmpty()) continue;
            map.put(hack.getName(), hack);
        }
        for (Command cmd : cmds.getAllCmds()) {
            if (cmd.getSettings().isEmpty()) continue;
            map.put(cmd.getName(), cmd);
        }
        for (OtherFeature otf : otfs.getAllOtfs()) {
            if (otf.getSettings().isEmpty()) continue;
            map.put(otf.getName(), otf);
        }
        return Collections.unmodifiableMap(map);
    }

    public void load() {
        try {
            WsonObject wson = JsonUtils.parseFileToObject(this.path);
            this.loadSettings(wson);
        }
        catch (NoSuchFileException wson) {
        }
        catch (IOException | JsonException e) {
            System.out.println("Couldn't load " + String.valueOf(this.path.getFileName()));
            e.printStackTrace();
        }
        this.save();
    }

    public void loadProfile(Path profilePath) throws IOException, JsonException {
        if (!profilePath.getFileName().toString().endsWith(".json")) {
            throw new IllegalArgumentException();
        }
        WsonObject wson = JsonUtils.parseFileToObject(profilePath);
        this.loadSettings(wson);
        this.save();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void loadSettings(WsonObject wson) {
        try {
            this.disableSaving = true;
            for (Map.Entry<String, JsonObject> e : wson.getAllJsonObjects().entrySet()) {
                Feature feature = this.featuresWithSettings.get(e.getKey());
                if (feature == null) continue;
                this.loadSettings(feature, e.getValue());
            }
        }
        finally {
            this.disableSaving = false;
        }
    }

    private void loadSettings(Feature feature, JsonObject json) {
        Map<String, Setting> settings = feature.getSettings();
        for (Map.Entry<String, JsonElement> e : json.entrySet()) {
            String key = e.getKey().toLowerCase();
            if (!settings.containsKey(key)) continue;
            settings.get(key).fromJson(e.getValue());
        }
    }

    public void save() {
        if (this.disableSaving) {
            return;
        }
        JsonObject json = this.createJson();
        try {
            JsonUtils.toJson(json, this.path);
        }
        catch (IOException | JsonException e) {
            System.out.println("Couldn't save " + String.valueOf(this.path.getFileName()));
            e.printStackTrace();
        }
    }

    public void saveProfile(Path profilePath) throws IOException, JsonException {
        if (!profilePath.getFileName().toString().endsWith(".json")) {
            throw new IllegalArgumentException();
        }
        JsonObject json = this.createJson();
        Files.createDirectories(profilePath.getParent(), new FileAttribute[0]);
        JsonUtils.toJson(json, profilePath);
    }

    private JsonObject createJson() {
        JsonObject json = new JsonObject();
        for (Feature feature : this.featuresWithSettings.values()) {
            Collection<Setting> settings = feature.getSettings().values();
            JsonObject jsonSettings = new JsonObject();
            settings.forEach(s -> jsonSettings.add(s.getName(), s.toJson()));
            json.add(feature.getName(), jsonSettings);
        }
        return json;
    }
}
