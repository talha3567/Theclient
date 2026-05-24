package net.wurstclient;

import com.google.gson.JsonArray;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Comparator;
import net.wurstclient.Feature;
import net.wurstclient.WurstClient;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.json.WsonArray;

public final class TooManyHaxFile {
    private final Path path;
    private final ArrayList<Feature> blockedFeatures;

    public TooManyHaxFile(Path path, ArrayList<Feature> blockedFeatures) {
        this.path = path;
        this.blockedFeatures = blockedFeatures;
    }

    public void load() {
        try {
            WsonArray wson = JsonUtils.parseFileToArray(this.path);
            this.setBlockedFeatures(wson);
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
        WsonArray wson = JsonUtils.parseFileToArray(profilePath);
        this.setBlockedFeatures(wson);
        this.save();
    }

    private void setBlockedFeatures(WsonArray wson) {
        this.blockedFeatures.clear();
        for (String name : wson.getAllStrings()) {
            Feature feature = WurstClient.INSTANCE.getFeatureByName(name);
            if (feature == null || !feature.isSafeToBlock()) continue;
            this.blockedFeatures.add(feature);
        }
        this.blockedFeatures.sort(Comparator.comparing(f -> f.getName().toLowerCase()));
    }

    public void save() {
        JsonArray json = this.createJson();
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
        JsonArray json = this.createJson();
        Files.createDirectories(profilePath.getParent(), new FileAttribute[0]);
        JsonUtils.toJson(json, profilePath);
    }

    private JsonArray createJson() {
        JsonArray json = new JsonArray();
        this.blockedFeatures.stream().filter(Feature::isSafeToBlock).map(Feature::getName).forEach(name -> json.add((String)name));
        return json;
    }
}
