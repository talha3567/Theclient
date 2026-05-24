package net.wurstclient.other_feature;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.TreeMap;
import net.minecraft.class_128;
import net.minecraft.class_148;
import net.wurstclient.other_feature.OtherFeature;
import net.wurstclient.other_features.ChangelogOtf;
import net.wurstclient.other_features.CleanUpOtf;
import net.wurstclient.other_features.DisableOtf;
import net.wurstclient.other_features.HackListOtf;
import net.wurstclient.other_features.KeybindManagerOtf;
import net.wurstclient.other_features.LastServerOtf;
import net.wurstclient.other_features.NoChatReportsOtf;
import net.wurstclient.other_features.NoTelemetryOtf;
import net.wurstclient.other_features.ReconnectOtf;
import net.wurstclient.other_features.ServerFinderOtf;
import net.wurstclient.other_features.TabGuiOtf;
import net.wurstclient.other_features.TranslationsOtf;
import net.wurstclient.other_features.VanillaSpoofOtf;
import net.wurstclient.other_features.WikiDataExportOtf;
import net.wurstclient.other_features.WurstCapesOtf;
import net.wurstclient.other_features.WurstLogoOtf;
import net.wurstclient.other_features.WurstOptionsOtf;
import net.wurstclient.other_features.ZoomOtf;

public final class OtfList {
    public final ChangelogOtf changelogOtf = new ChangelogOtf();
    public final CleanUpOtf cleanUpOtf = new CleanUpOtf();
    public final DisableOtf disableOtf = new DisableOtf();
    public final HackListOtf hackListOtf = new HackListOtf();
    public final KeybindManagerOtf keybindManagerOtf = new KeybindManagerOtf();
    public final LastServerOtf lastServerOtf = new LastServerOtf();
    public final NoChatReportsOtf noChatReportsOtf = new NoChatReportsOtf();
    public final NoTelemetryOtf noTelemetryOtf = new NoTelemetryOtf();
    public final ReconnectOtf reconnectOtf = new ReconnectOtf();
    public final ServerFinderOtf serverFinderOtf = new ServerFinderOtf();
    public final TabGuiOtf tabGuiOtf = new TabGuiOtf();
    public final TranslationsOtf translationsOtf = new TranslationsOtf();
    public final VanillaSpoofOtf vanillaSpoofOtf = new VanillaSpoofOtf();
    public final WikiDataExportOtf wikiDataExportOtf = new WikiDataExportOtf();
    public final WurstCapesOtf wurstCapesOtf = new WurstCapesOtf();
    public final WurstLogoOtf wurstLogoOtf = new WurstLogoOtf();
    public final WurstOptionsOtf wurstOptionsOtf = new WurstOptionsOtf();
    public final ZoomOtf zoomOtf = new ZoomOtf();
    private final TreeMap<String, OtherFeature> otfs = new TreeMap(String::compareToIgnoreCase);

    public OtfList() {
        try {
            for (Field field : OtfList.class.getDeclaredFields()) {
                if (!field.getName().endsWith("Otf")) continue;
                OtherFeature otf = (OtherFeature)field.get(this);
                this.otfs.put(otf.getName(), otf);
            }
        }
        catch (Exception e) {
            String message = "Initializing other Wurst features";
            class_128 report = class_128.method_560((Throwable)e, (String)message);
            throw new class_148(report);
        }
    }

    public OtherFeature getOtfByName(String name) {
        return this.otfs.get(name);
    }

    public Collection<OtherFeature> getAllOtfs() {
        return this.otfs.values();
    }

    public int countOtfs() {
        return this.otfs.size();
    }
}
