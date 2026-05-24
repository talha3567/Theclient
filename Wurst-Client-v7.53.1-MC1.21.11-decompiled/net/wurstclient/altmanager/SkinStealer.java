package net.wurstclient.altmanager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

public final class SkinStealer
extends Enum<SkinStealer> {
    private static final /* synthetic */ SkinStealer[] $VALUES;

    public static SkinStealer[] values() {
        return (SkinStealer[])$VALUES.clone();
    }

    public static SkinStealer valueOf(String name) {
        return Enum.valueOf(SkinStealer.class, name);
    }

    public static URL getSkinUrl(String username) throws IOException {
        String uuid = SkinStealer.getUUIDString(username);
        JsonObject texturesValueJson = SkinStealer.getTexturesValue(uuid);
        JsonObject tJObj = texturesValueJson.get("textures").getAsJsonObject();
        JsonObject skinJObj = tJObj.get("SKIN").getAsJsonObject();
        String skin = skinJObj.get("url").getAsString();
        return URI.create(skin).toURL();
    }

    private static JsonObject getTexturesValue(String uuid) throws IOException {
        JsonObject sessionJson = SkinStealer.getSessionJson(uuid);
        JsonArray propertiesJson = sessionJson.get("properties").getAsJsonArray();
        JsonObject firstProperty = propertiesJson.get(0).getAsJsonObject();
        String texturesBase64 = firstProperty.get("value").getAsString();
        byte[] texturesBytes = Base64.decodeBase64((byte[])texturesBase64.getBytes());
        JsonObject texturesJson = new Gson().fromJson(new String(texturesBytes), JsonObject.class);
        return texturesJson;
    }

    private static JsonObject getSessionJson(String uuid) throws IOException {
        URL sessionURL = URI.create("https://sessionserver.mojang.com/session/minecraft/profile/").resolve(uuid).toURL();
        try (InputStream sessionInputStream = sessionURL.openStream();){
            JsonObject jsonObject = new Gson().fromJson(IOUtils.toString((InputStream)sessionInputStream, (Charset)StandardCharsets.UTF_8), JsonObject.class);
            return jsonObject;
        }
    }

    private static String getUUIDString(String username) throws IOException {
        URL profileURL = URI.create("https://api.mojang.com/users/profiles/minecraft/").resolve(URLEncoder.encode(username, "UTF-8")).toURL();
        try (InputStream profileInputStream = profileURL.openStream();){
            JsonObject profileJson = new Gson().fromJson(IOUtils.toString((InputStream)profileInputStream, (Charset)StandardCharsets.UTF_8), JsonObject.class);
            String string = profileJson.get("id").getAsString();
            return string;
        }
    }

    public static UUID getUUIDOrNull(String name) {
        try {
            String uuid = SkinStealer.getUUIDString(name);
            if (uuid.length() != 32) {
                return null;
            }
            long mostSigBits = Long.parseUnsignedLong(uuid.substring(0, 16), 16);
            long leastSigBits = Long.parseUnsignedLong(uuid.substring(16), 16);
            return new UUID(mostSigBits, leastSigBits);
        }
        catch (IOException e) {
            return null;
        }
    }

    private static /* synthetic */ SkinStealer[] $values() {
        return new SkinStealer[0];
    }

    static {
        $VALUES = SkinStealer.$values();
    }
}
