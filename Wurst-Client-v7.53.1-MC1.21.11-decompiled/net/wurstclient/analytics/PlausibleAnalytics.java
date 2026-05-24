package net.wurstclient.analytics;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.class_1076;
import net.minecraft.class_310;
import net.minecraft.class_638;
import net.minecraft.class_642;
import net.wurstclient.analytics.AnalyticsConfigFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PlausibleAnalytics {
    private static final Gson GSON = new Gson();
    private static final Logger LOGGER = LoggerFactory.getLogger("Plausible");
    private static final String MOD_ID = "wurst";
    private static final URI API_ENDPOINT = URI.create("https://plausible.wurstclient.net/api/event");
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5L)).build();
    private final LinkedBlockingQueue<PlausibleEvent> eventQueue = new LinkedBlockingQueue();
    private final JsonObject sessionProps = new JsonObject();
    private final AnalyticsConfigFile configFile;
    private boolean enabled = true;

    public PlausibleAnalytics(Path configFile) {
        this.configFile = new AnalyticsConfigFile(configFile);
        this.configFile.load(this);
        this.sessionProp("version", this.getVersion(MOD_ID));
        this.sessionProp("short_version", this.getShortVersion(MOD_ID));
        this.sessionProp("mc_version", this.getVersion("minecraft"));
        this.sessionProp("fabric_api_version", this.getVersion("fabric-api"));
        this.sessionProp("fabric_loader_version", this.getVersion("fabricloader"));
        this.sessionProp("modmenu_version", this.getVersion("modmenu"));
        this.sessionProp("sodium_version", this.getVersion("sodium"));
        this.sessionProp("sinytra_connector_version", this.getVersion("connector"));
        Thread.ofPlatform().daemon().name("Plausible").start(this::runBackgroundLoop);
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register(this::onWorldChange);
    }

    private String getVersion(String modId) {
        return FabricLoader.getInstance().getModContainer(modId).map(ModContainer::getMetadata).map(ModMetadata::getVersion).map(Version::toString).orElse(null);
    }

    private String getShortVersion(String modId) {
        String version = this.getVersion(modId);
        if (version != null && version.contains("-MC")) {
            return version.substring(0, version.indexOf("-MC"));
        }
        return version;
    }

    private void onWorldChange(class_310 client, class_638 world) {
        this.sessionProp("language", this.getLanguage(client));
        this.sessionProp("game_type", this.getGameType(client));
        this.pageview("/in-game");
    }

    private String getLanguage(class_310 client) {
        return Optional.ofNullable(client.method_1526()).map(class_1076::method_4669).map(String::toLowerCase).orElse(null);
    }

    private String getGameType(class_310 client) {
        class_642 server = client.method_1558();
        if (server == null) {
            return "singleplayer";
        }
        if (server.method_2994()) {
            return "lan";
        }
        if (server.method_52811()) {
            return "realms";
        }
        return "multiplayer";
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.configFile.save(this);
    }

    private boolean isDebugMode() {
        return FabricLoader.getInstance().isDevelopmentEnvironment() || System.getProperty("fabric.client.gametest") != null;
    }

    private void runBackgroundLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                this.sendEvent(this.eventQueue.take());
                Thread.sleep(50L);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            catch (Exception e) {
                LOGGER.error("Plausible error", e);
            }
        }
    }

    private void sendEvent(PlausibleEvent event) {
        String body = this.createRequestBody(event);
        if (this.isDebugMode()) {
            LOGGER.info("Event ({} props): {}", (Object)event.props().size(), (Object)body);
            return;
        }
        HttpRequest request = HttpRequest.newBuilder().uri(API_ENDPOINT).header("User-Agent", this.getUserAgent()).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).timeout(Duration.ofSeconds(5L)).build();
        this.httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding()).exceptionally(ex -> null);
    }

    private String getUserAgent() {
        return System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") version " + System.getProperty("os.version");
    }

    private String createRequestBody(PlausibleEvent event) {
        JsonObject body = new JsonObject();
        body.addProperty("name", event.name());
        body.addProperty("url", event.url());
        body.addProperty("domain", MOD_ID);
        if (event.props() != null && !event.props().isEmpty()) {
            body.add("props", event.props());
        }
        return GSON.toJson(body);
    }

    public void pageview(String path) {
        this.event("pageview", path, null);
    }

    public void pageview(String path, Map<String, String> props) {
        this.event("pageview", path, props);
    }

    public void event(String name, String path) {
        this.event(name, path, null);
    }

    public void event(String name, String path, Map<String, String> props) {
        if (!this.isEnabled() || name == null || path == null) {
            return;
        }
        String url = this.buildURL(path);
        JsonObject jsonProps = this.buildJsonProps(props);
        this.eventQueue.offer(new PlausibleEvent(name, url, jsonProps));
    }

    private String buildURL(String path) {
        Object adjustedPath = path.startsWith("/") ? path : "/" + path;
        return "mod://wurst" + (String)adjustedPath;
    }

    private JsonObject buildJsonProps(Map<String, String> props) {
        JsonObject jsonProps = this.sessionProps.deepCopy();
        if (props == null || props.isEmpty()) {
            return jsonProps;
        }
        for (Map.Entry<String, String> entry : props.entrySet()) {
            String key = entry.getKey();
            if (this.isDebugMode() && key.length() > 300) {
                LOGGER.warn("Property key is too long ({} characters): {}", (Object)key.length(), (Object)key);
            }
            String value = entry.getValue();
            if (this.isDebugMode() && value.length() > 2000) {
                LOGGER.warn("Property value is too long ({} characters): {}", (Object)value.length(), (Object)value);
            }
            if (key == null || value == null) continue;
            jsonProps.addProperty(key, value);
        }
        if (this.isDebugMode() && jsonProps.size() > 30) {
            LOGGER.warn("Too many properties ({})", (Object)jsonProps.size());
        }
        return jsonProps;
    }

    public void sessionProp(String name, String value) {
        if (name != null && value != null) {
            this.sessionProps.addProperty(name, value);
        }
    }

    public void removeSessionProp(String name) {
        if (name != null) {
            this.sessionProps.remove(name);
        }
    }

    private record PlausibleEvent(String name, String url, JsonObject props) {
    }
}
