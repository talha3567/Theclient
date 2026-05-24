package net.wurstclient.keybinds;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.wurstclient.WurstClient;
import net.wurstclient.keybinds.Keybind;
import net.wurstclient.keybinds.KeybindsFile;
import net.wurstclient.util.json.JsonException;

public final class KeybindList {
    public static final Set<Keybind> DEFAULT_KEYBINDS = KeybindList.createDefaultKeybinds();
    private final ArrayList<Keybind> keybinds = new ArrayList();
    private final KeybindsFile keybindsFile;
    private final Path profilesFolder = WurstClient.INSTANCE.getWurstFolder().resolve("keybinds");

    public KeybindList(Path keybindsFile) {
        this.keybindsFile = new KeybindsFile(keybindsFile);
        this.keybindsFile.load(this);
    }

    public String getCommands(String key) {
        for (Keybind keybind : this.keybinds) {
            if (!key.equals(keybind.getKey())) continue;
            return keybind.getCommands();
        }
        return null;
    }

    public List<Keybind> getAllKeybinds() {
        return Collections.unmodifiableList(this.keybinds);
    }

    public void add(String key, String commands) {
        this.keybinds.removeIf(keybind -> key.equals(keybind.getKey()));
        this.keybinds.add(new Keybind(key, commands));
        this.keybinds.sort(null);
        this.keybindsFile.save(this);
    }

    public void setKeybinds(Set<Keybind> keybinds) {
        this.keybinds.clear();
        this.keybinds.addAll(keybinds);
        this.keybinds.sort(null);
        this.keybindsFile.save(this);
    }

    public void remove(String key) {
        this.keybinds.removeIf(keybind -> key.equals(keybind.getKey()));
        this.keybindsFile.save(this);
    }

    public void removeAll() {
        this.keybinds.clear();
        this.keybindsFile.save(this);
    }

    public Path getProfilesFolder() {
        return this.profilesFolder;
    }

    public ArrayList<Path> listProfiles() {
        ArrayList arrayList;
        block9: {
            if (!Files.isDirectory(this.profilesFolder, new LinkOption[0])) {
                return new ArrayList<Path>();
            }
            Stream<Path> files = Files.list(this.profilesFolder);
            try {
                arrayList = files.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).filter(path -> path.getFileName().toString().endsWith(".json")).collect(Collectors.toCollection(ArrayList::new));
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

    public void loadProfile(String fileName) throws IOException, JsonException {
        this.keybindsFile.loadProfile(this, this.profilesFolder.resolve(fileName));
    }

    public void saveProfile(String fileName) throws IOException, JsonException {
        this.keybindsFile.saveProfile(this, this.profilesFolder.resolve(fileName));
    }

    private static Set<Keybind> createDefaultKeybinds() {
        LinkedHashSet<Keybind> set = new LinkedHashSet<Keybind>();
        KeybindList.addKB(set, "b", "fastplace;fastbreak");
        KeybindList.addKB(set, "c", "fullbright");
        KeybindList.addKB(set, "g", "flight");
        KeybindList.addKB(set, "semicolon", "speednuker");
        KeybindList.addKB(set, "h", "say /home");
        KeybindList.addKB(set, "j", "jesus");
        KeybindList.addKB(set, "k", "multiaura");
        KeybindList.addKB(set, "n", "nuker");
        KeybindList.addKB(set, "r", "killaura");
        KeybindList.addKB(set, "right.shift", "navigator");
        KeybindList.addKB(set, "right.control", "clickgui");
        KeybindList.addKB(set, "u", "freecam");
        KeybindList.addKB(set, "x", "x-ray");
        KeybindList.addKB(set, "y", "sneak");
        return Collections.unmodifiableSet(set);
    }

    private static void addKB(Set<Keybind> set, String key, String cmds) {
        set.add(new Keybind("key.keyboard." + key, cmds));
    }
}
