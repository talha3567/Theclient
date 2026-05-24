package net.wurstclient.update;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.wurstclient.WurstClient;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.update.Version;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.json.WsonArray;
import net.wurstclient.util.json.WsonObject;

public final class WurstUpdater
implements UpdateListener {
    private Thread thread;
    private boolean outdated;
    private class_2561 component;

    @Override
    public void onUpdate() {
        if (this.thread == null) {
            this.thread = new Thread(this::checkForUpdates, "WurstUpdater");
            this.thread.start();
            return;
        }
        if (this.thread.isAlive()) {
            return;
        }
        if (this.component != null) {
            ChatUtils.component(this.component);
        }
        WurstClient.INSTANCE.getEventManager().remove(UpdateListener.class, this);
    }

    public void checkForUpdates() {
        if (System.getProperty("fabric.client.gametest") != null) {
            return;
        }
        Version currentVersion = new Version("7.53.1");
        Version latestVersion = null;
        try {
            WsonArray wson = JsonUtils.parseURLToArray("https://api.github.com/repos/Wurst-Imperium/Wurst-MCX2/releases");
            for (WsonObject release : wson.getAllObjects()) {
                if (!currentVersion.isPreRelease() && release.getBoolean("prerelease") || !this.containsCompatibleAsset(release.getArray("assets"))) continue;
                String tagName = release.getString("tag_name");
                latestVersion = new Version(tagName.substring(1));
                break;
            }
            if (latestVersion == null) {
                throw new NullPointerException("Latest version is missing!");
            }
            System.out.println("[Updater] Current version: " + String.valueOf(currentVersion));
            System.out.println("[Updater] Latest version: " + String.valueOf(latestVersion));
            this.outdated = currentVersion.shouldUpdateTo(latestVersion);
        }
        catch (Exception e) {
            System.err.println("[Updater] An error occurred!");
            e.printStackTrace();
        }
        String currentVersionEncoded = URLEncoder.encode("Wurst " + String.valueOf(currentVersion) + " MC1.21.11", StandardCharsets.UTF_8);
        String baseUrl = "https://www.wurstclient.net/download/";
        String utmSource = "Wurst+Client";
        String utmMedium = "WurstUpdater+chat+message";
        if (latestVersion == null || latestVersion.isInvalid()) {
            String text = "An error occurred while checking for updates. Click \u00a7nhere\u00a7r to check manually.";
            String url = baseUrl + "?utm_source=" + utmSource + "&utm_medium=" + utmMedium + "&utm_content=" + currentVersionEncoded + "+error+checking+updates+chat+message";
            this.showLink(text, url);
            return;
        }
        if (!this.outdated) {
            return;
        }
        String text = "Wurst " + String.valueOf(latestVersion) + " is now available for Minecraft 1.21.11. \u00a7nUpdate now\u00a7r to benefit from new features and/or bugfixes!";
        String utmContent = currentVersionEncoded + "+update+chat+message";
        String url = baseUrl + "?utm_source=" + utmSource + "&utm_medium=" + utmMedium + "&utm_content=" + utmContent;
        this.showLink(text, url);
    }

    private void showLink(String text, String url) {
        class_2558.class_10608 event = new class_2558.class_10608(URI.create(url));
        this.component = class_2561.method_43470((String)text).method_27694(arg_0 -> WurstUpdater.lambda$showLink$0((class_2558)event, arg_0));
    }

    private boolean containsCompatibleAsset(WsonArray wsonArray) throws JsonException {
        String compatibleSuffix = "MC1.21.11.jar";
        for (WsonObject asset : wsonArray.getAllObjects()) {
            String assetName = asset.getString("name");
            if (!assetName.endsWith(compatibleSuffix)) continue;
            return true;
        }
        return false;
    }

    public boolean isOutdated() {
        return this.outdated;
    }

    private static /* synthetic */ class_2583 lambda$showLink$0(class_2558 event, class_2583 s) {
        return s.method_10958(event);
    }
}
