package net.wurstclient.navigator;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import net.wurstclient.Feature;
import net.wurstclient.command.CmdList;
import net.wurstclient.hack.HackList;
import net.wurstclient.navigator.PreferencesFile;
import net.wurstclient.other_feature.OtfList;

public final class Navigator {
    private final ArrayList<Feature> navigatorList = new ArrayList();
    private final HashMap<String, Long> preferences = new HashMap();
    private final PreferencesFile preferencesFile;

    public Navigator(Path path, HackList hax, CmdList cmds, OtfList otfs) {
        this.navigatorList.addAll(hax.getAllHax());
        this.navigatorList.addAll(cmds.getAllCmds());
        this.navigatorList.addAll(otfs.getAllOtfs());
        this.preferencesFile = new PreferencesFile(path, this.preferences);
        this.preferencesFile.load();
    }

    public void copyNavigatorList(ArrayList<Feature> list) {
        if (list.equals(this.navigatorList)) {
            return;
        }
        list.clear();
        list.addAll(this.navigatorList);
    }

    public void getSearchResults(ArrayList<Feature> list, String query) {
        list.clear();
        for (Feature mod : this.navigatorList) {
            if (!mod.getName().toLowerCase().contains(query) && !mod.getSearchTags().toLowerCase().contains(query) && !mod.getDescription().toLowerCase().contains(query)) continue;
            list.add(mod);
        }
        Comparator c = (o1, o2) -> {
            int index2;
            int index1 = o1.toLowerCase().indexOf(query);
            if (index1 == (index2 = o2.toLowerCase().indexOf(query))) {
                return 0;
            }
            if (index1 == -1) {
                return 1;
            }
            if (index2 == -1) {
                return -1;
            }
            return index1 - index2;
        };
        list.sort(Comparator.comparing(Feature::getName, c).thenComparing(Feature::getSearchTags, c).thenComparing(Feature::getDescription, c));
    }

    public long getPreference(String feature) {
        Long preference = this.preferences.get(feature);
        if (preference == null) {
            preference = 0L;
        }
        return preference;
    }

    public void addPreference(String feature) {
        Long preference = this.preferences.get(feature);
        if (preference == null) {
            preference = 0L;
        }
        Long l = preference;
        preference = preference + 1L;
        this.preferences.put(feature, preference);
        this.preferencesFile.save();
    }

    public List<Feature> getList() {
        return Collections.unmodifiableList(this.navigatorList);
    }

    public void sortFeatures() {
        this.navigatorList.sort(Comparator.comparingLong(f -> this.getPreference(f.getName())).reversed());
    }

    public int countAllFeatures() {
        return this.navigatorList.size();
    }
}
