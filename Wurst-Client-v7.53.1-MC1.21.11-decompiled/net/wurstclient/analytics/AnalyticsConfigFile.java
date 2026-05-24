package net.wurstclient.analytics;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import net.wurstclient.analytics.PlausibleAnalytics;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.json.WsonObject;

public final class AnalyticsConfigFile {
    private final Path path;
    private boolean disableSaving;

    public AnalyticsConfigFile(Path path) {
        this.path = path;
    }

    public void load(PlausibleAnalytics plausible) {
        try {
            WsonObject wson = JsonUtils.parseFileToObject(this.path);
            this.loadJson(wson, plausible);
        }
        catch (NoSuchFileException wson) {
        }
        catch (IOException | JsonException e) {
            System.out.println("Couldn't load " + String.valueOf(this.path.getFileName()));
            e.printStackTrace();
        }
        this.save(plausible);
    }

    private void loadJson(WsonObject wson, PlausibleAnalytics plausible) throws JsonException {
        try {
            this.disableSaving = true;
            if (!wson.has("version")) {
                return;
            }
            plausible.setEnabled(wson.getBoolean("enabled"));
        }
        finally {
            this.disableSaving = false;
        }
    }

    public void save(PlausibleAnalytics plausible) {
        if (this.disableSaving) {
            return;
        }
        JsonObject json = this.createJson(plausible);
        try {
            JsonUtils.toJson(json, this.path);
        }
        catch (IOException | JsonException e) {
            System.out.println("Couldn't save " + String.valueOf(this.path.getFileName()));
            e.printStackTrace();
        }
    }

    private JsonObject createJson(PlausibleAnalytics plausible) {
        JsonObject json = new JsonObject();
        json.addProperty("version", 2);
        json.addProperty("enabled", plausible.isEnabled());
        return json;
    }
}
