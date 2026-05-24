package net.wurstclient.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.Component;
import net.wurstclient.clickgui.components.FileComponent;
import net.wurstclient.keybinds.PossibleKeybind;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.text.WText;

public final class FileSetting
extends Setting {
    private final Path folder;
    private String selectedFile = "";
    private final Consumer<Path> createDefaultFiles;

    public FileSetting(String name, WText description, String folderName, Consumer<Path> createDefaultFiles) {
        super(name, description);
        this.folder = WurstClient.INSTANCE.getWurstFolder().resolve(folderName);
        this.createDefaultFiles = createDefaultFiles;
        this.setSelectedFileToDefault();
    }

    public FileSetting(String name, String descriptionKey, String folderName, Consumer<Path> createDefaultFiles) {
        this(name, WText.translated(descriptionKey, new Object[0]), folderName, createDefaultFiles);
    }

    public Path getFolder() {
        return this.folder;
    }

    public String getSelectedFileName() {
        return this.selectedFile;
    }

    public Path getSelectedFile() {
        return this.folder.resolve(this.selectedFile);
    }

    public void setSelectedFile(String selectedFile) {
        Objects.requireNonNull(selectedFile);
        Path newSelectedFile = this.folder.resolve(selectedFile);
        if (!Files.exists(newSelectedFile, new LinkOption[0])) {
            return;
        }
        this.selectedFile = selectedFile;
        WurstClient.INSTANCE.saveSettings();
    }

    private void setSelectedFileToDefault() {
        ArrayList<Path> files = this.listFiles();
        if (files.isEmpty()) {
            files = this.createDefaultFiles();
        }
        this.selectedFile = String.valueOf(files.get(0).getFileName());
    }

    private ArrayList<Path> createDefaultFiles() {
        try {
            Files.createDirectories(this.folder, new FileAttribute[0]);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.createDefaultFiles.accept(this.folder);
        ArrayList<Path> files = this.listFiles();
        if (files.isEmpty()) {
            throw new IllegalStateException("Created default files but folder is still empty!");
        }
        return files;
    }

    public void resetFolder() {
        for (Path path : this.listFiles()) {
            try {
                Files.delete(path);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        this.setSelectedFileToDefault();
        WurstClient.INSTANCE.saveSettings();
    }

    public ArrayList<Path> listFiles() {
        ArrayList arrayList;
        block9: {
            if (!Files.isDirectory(this.folder, new LinkOption[0])) {
                return new ArrayList<Path>();
            }
            Stream<Path> files = Files.list(this.folder);
            try {
                arrayList = files.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).collect(Collectors.toCollection(ArrayList::new));
                if (files == null) break block9;
            }
            catch (Throwable throwable) {
                try {
                    if (files != null) {
                        try {
                            files.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            files.close();
        }
        return arrayList;
    }

    @Override
    public Component getComponent() {
        return new FileComponent(this);
    }

    @Override
    public void fromJson(JsonElement json) {
        try {
            String newFile = JsonUtils.getAsString(json);
            if (newFile.isEmpty() || !Files.exists(this.folder.resolve(newFile), new LinkOption[0])) {
                throw new JsonException("File not found: " + newFile);
            }
            this.selectedFile = newFile;
        }
        catch (JsonException e) {
            e.printStackTrace();
        }
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(this.selectedFile);
    }

    @Override
    public JsonObject exportWikiData() {
        JsonObject json = new JsonObject();
        json.addProperty("name", this.getName());
        json.addProperty("description", this.getDescription());
        json.addProperty("type", "File");
        Path mcFolder = WurstClient.INSTANCE.getWurstFolder().getParent();
        if (this.folder.startsWith(mcFolder)) {
            json.addProperty("folder", mcFolder.relativize(this.folder).toString());
        }
        return json;
    }

    @Override
    public Set<PossibleKeybind> getPossibleKeybinds(String featureName) {
        return new LinkedHashSet<PossibleKeybind>();
    }
}
