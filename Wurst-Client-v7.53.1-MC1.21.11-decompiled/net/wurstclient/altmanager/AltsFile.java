package net.wurstclient.altmanager;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Map;
import net.wurstclient.altmanager.Alt;
import net.wurstclient.altmanager.AltManager;
import net.wurstclient.altmanager.CrackedAlt;
import net.wurstclient.altmanager.Encryption;
import net.wurstclient.altmanager.MojangAlt;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.json.WsonObject;

public final class AltsFile {
    private final Path path;
    private final Path encFolder;
    private boolean disableSaving;
    private Encryption encryption;
    private IOException folderException;

    public AltsFile(Path path, Path encFolder) {
        this.path = path;
        this.encFolder = encFolder;
    }

    public void load(AltManager altManager) {
        try {
            if (this.encryption == null) {
                this.encryption = new Encryption(this.encFolder);
            }
        }
        catch (IOException e) {
            System.out.println("Couldn't create '.Wurst encryption' folder.");
            e.printStackTrace();
            this.folderException = e;
            return;
        }
        try {
            WsonObject wson = this.encryption.parseFileToObject(this.path);
            this.loadAlts(wson, altManager);
        }
        catch (NoSuchFileException wson) {
        }
        catch (IOException | JsonException e) {
            System.out.println("Couldn't load " + String.valueOf(this.path.getFileName()));
            e.printStackTrace();
            this.renameCorrupted();
        }
        this.save(altManager);
    }

    private void renameCorrupted() {
        try {
            Path newPath = this.path.resolveSibling("!CORRUPTED_" + String.valueOf(this.path.getFileName()));
            Files.move(this.path, newPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Renamed to " + String.valueOf(newPath.getFileName()));
        }
        catch (IOException e) {
            System.out.println("Couldn't rename corrupted file " + String.valueOf(this.path.getFileName()));
            e.printStackTrace();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void loadAlts(WsonObject wson, AltManager altManager) {
        ArrayList<Alt> alts = AltsFile.parseJson(wson);
        try {
            this.disableSaving = true;
            altManager.addAll(alts);
        }
        finally {
            this.disableSaving = false;
        }
    }

    public static ArrayList<Alt> parseJson(WsonObject wson) {
        ArrayList<Alt> alts = new ArrayList<Alt>();
        for (Map.Entry<String, JsonObject> e : wson.getAllJsonObjects().entrySet()) {
            String nameOrEmail = e.getKey();
            if (nameOrEmail.isEmpty()) continue;
            JsonObject jsonAlt = e.getValue();
            alts.add(AltsFile.loadAlt(nameOrEmail, jsonAlt));
        }
        return alts;
    }

    private static Alt loadAlt(String nameOrEmail, JsonObject jsonAlt) {
        String password = JsonUtils.getAsString(jsonAlt.get("password"), "");
        boolean starred = JsonUtils.getAsBoolean(jsonAlt.get("starred"), false);
        if (password.isEmpty()) {
            return new CrackedAlt(nameOrEmail, starred);
        }
        String name = JsonUtils.getAsString(jsonAlt.get("name"), "");
        return new MojangAlt(nameOrEmail, password, name, starred);
    }

    public void save(AltManager alts) {
        if (this.disableSaving) {
            return;
        }
        try {
            if (this.encryption == null) {
                this.encryption = new Encryption(this.encFolder);
            }
        }
        catch (IOException e) {
            System.out.println("Couldn't create '.Wurst encryption' folder.");
            e.printStackTrace();
            this.folderException = e;
            return;
        }
        JsonObject json = AltsFile.createJson(alts);
        try {
            this.encryption.toEncryptedJson(json, this.path);
        }
        catch (IOException | JsonException e) {
            System.out.println("Couldn't save " + String.valueOf(this.path.getFileName()));
            e.printStackTrace();
        }
    }

    public static JsonObject createJson(AltManager alts) {
        JsonObject json = new JsonObject();
        for (Alt alt : alts.getList()) {
            alt.exportAsJson(json);
        }
        return json;
    }

    public IOException getFolderException() {
        return this.folderException;
    }
}
