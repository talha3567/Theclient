package net.wurstclient.hack;

import com.google.gson.JsonArray;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.stream.Stream;
import net.wurstclient.hack.Hack;
import net.wurstclient.hack.HackList;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.json.WsonArray;

public final class EnabledHacksFile {
    private final Path path;
    private boolean disableSaving;

    public EnabledHacksFile(Path path) {
        this.path = path;
    }

    public void load(HackList hackList) {
        try {
            WsonArray wson = JsonUtils.parseFileToArray(this.path);
            this.enableHacks(hackList, wson);
        }
        catch (NoSuchFileException wson) {
        }
        catch (IOException | JsonException e) {
            System.out.println("Couldn't load " + String.valueOf(this.path.getFileName()));
            e.printStackTrace();
        }
        this.save(hackList);
    }

    public void loadProfile(HackList hax, Path profilePath) throws IOException, JsonException {
        if (!profilePath.getFileName().toString().endsWith(".json")) {
            throw new IllegalArgumentException();
        }
        WsonArray wson = JsonUtils.parseFileToArray(profilePath);
        this.enableHacks(hax, wson);
        this.save(hax);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void enableHacks(HackList hax, WsonArray wson) {
        try {
            this.disableSaving = true;
            for (Hack hack : hax.getAllHax()) {
                hack.setEnabled(false);
            }
            for (String name : wson.getAllStrings()) {
                Hack hack = hax.getHackByName(name);
                if (hack == null || !hack.isStateSaved()) continue;
                hack.setEnabled(true);
            }
        }
        finally {
            this.disableSaving = false;
        }
    }

    public void save(HackList hax) {
        if (this.disableSaving) {
            return;
        }
        JsonArray json = this.createJson(hax);
        try {
            JsonUtils.toJson(json, this.path);
        }
        catch (IOException | JsonException e) {
            System.out.println("Couldn't save " + String.valueOf(this.path.getFileName()));
            e.printStackTrace();
        }
    }

    public void saveProfile(HackList hax, Path profilePath) throws IOException, JsonException {
        if (!profilePath.getFileName().toString().endsWith(".json")) {
            throw new IllegalArgumentException();
        }
        JsonArray json = this.createJson(hax);
        Files.createDirectories(profilePath.getParent(), new FileAttribute[0]);
        JsonUtils.toJson(json, profilePath);
    }

    private JsonArray createJson(HackList hax) {
        Stream<Hack> enabledHax = hax.getAllHax().stream().filter(Hack::isEnabled).filter(Hack::isStateSaved);
        JsonArray json = new JsonArray();
        enabledHax.map(Hack::getName).forEach(name -> json.add((String)name));
        return json;
    }
}
