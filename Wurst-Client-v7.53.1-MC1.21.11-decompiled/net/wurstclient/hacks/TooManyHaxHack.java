package net.wurstclient.hacks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.wurstclient.Category;
import net.wurstclient.DontBlock;
import net.wurstclient.Feature;
import net.wurstclient.SearchTags;
import net.wurstclient.TooManyHaxFile;
import net.wurstclient.hack.Hack;
import net.wurstclient.util.json.JsonException;

@SearchTags(value={"too many hax", "TooManyHacks", "too many hacks", "YesCheat+", "YesCheatPlus", "yes cheat plus"})
@DontBlock
public final class TooManyHaxHack
extends Hack {
    private final ArrayList<Feature> blockedFeatures = new ArrayList();
    private final Path profilesFolder;
    private final TooManyHaxFile file;

    public TooManyHaxHack() {
        super("TooManyHax");
        this.setCategory(Category.OTHER);
        Path wurstFolder = WURST.getWurstFolder();
        this.profilesFolder = wurstFolder.resolve("toomanyhax");
        Path filePath = wurstFolder.resolve("toomanyhax.json");
        this.file = new TooManyHaxFile(filePath, this.blockedFeatures);
    }

    public void loadBlockedHacksFile() {
        this.file.load();
    }

    @Override
    public String getRenderName() {
        return this.getName() + " [" + this.blockedFeatures.size() + " blocked]";
    }

    @Override
    protected void onEnable() {
        this.disableBlockedHacks();
    }

    private void disableBlockedHacks() {
        for (Feature feature : this.blockedFeatures) {
            if (!(feature instanceof Hack)) continue;
            ((Hack)feature).setEnabled(false);
        }
    }

    public ArrayList<Path> listProfiles() {
        ArrayList arrayList;
        block9: {
            if (!Files.isDirectory(this.profilesFolder, new LinkOption[0])) {
                return new ArrayList<Path>();
            }
            Stream<Path> files = Files.list(this.profilesFolder);
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

    public void loadProfile(String fileName) throws IOException, JsonException {
        this.file.loadProfile(this.profilesFolder.resolve(fileName));
        this.disableBlockedHacks();
    }

    public void saveProfile(String fileName) throws IOException, JsonException {
        this.file.saveProfile(this.profilesFolder.resolve(fileName));
    }

    public boolean isBlocked(Feature feature) {
        return this.blockedFeatures.contains(feature);
    }

    public void setBlocked(Feature feature, boolean blocked) {
        if (blocked) {
            if (!feature.isSafeToBlock()) {
                throw new IllegalArgumentException();
            }
            this.blockedFeatures.add(feature);
            this.blockedFeatures.sort(Comparator.comparing(f -> f.getName().toLowerCase()));
        } else {
            this.blockedFeatures.remove(feature);
        }
        this.file.save();
    }

    public void blockAll() {
        this.blockedFeatures.clear();
        ArrayList<Feature> features = new ArrayList<Feature>();
        features.addAll(WURST.getHax().getAllHax());
        features.addAll(WURST.getCmds().getAllCmds());
        features.addAll(WURST.getOtfs().getAllOtfs());
        for (Feature feature : features) {
            if (!feature.isSafeToBlock()) continue;
            this.blockedFeatures.add(feature);
        }
        this.blockedFeatures.sort(Comparator.comparing(f -> f.getName().toLowerCase()));
        this.file.save();
    }

    public void unblockAll() {
        this.blockedFeatures.clear();
        this.file.save();
    }

    public List<Feature> getBlockedFeatures() {
        return Collections.unmodifiableList(this.blockedFeatures);
    }
}
