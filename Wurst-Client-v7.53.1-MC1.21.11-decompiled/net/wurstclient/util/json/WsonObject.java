package net.wurstclient.util.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.json.WsonArray;

public final class WsonObject {
    private final JsonObject json;

    public WsonObject(JsonObject json) {
        this.json = Objects.requireNonNull(json);
    }

    public boolean getBoolean(String key) throws JsonException {
        try {
            return JsonUtils.getAsBoolean(this.json.get(key));
        }
        catch (JsonException e) {
            throw new JsonException("Boolean \"" + key + "\" not found.", e);
        }
    }

    public boolean getBoolean(String key, boolean fallback) {
        return JsonUtils.getAsBoolean(this.json.get(key), fallback);
    }

    public int getInt(String key) throws JsonException {
        try {
            return JsonUtils.getAsInt(this.json.get(key));
        }
        catch (JsonException e) {
            throw new JsonException("Number \"" + key + "\" not found.", e);
        }
    }

    public int getInt(String key, int fallback) {
        return JsonUtils.getAsInt(this.json.get(key), fallback);
    }

    public long getLong(String key) throws JsonException {
        try {
            return JsonUtils.getAsLong(this.json.get(key));
        }
        catch (JsonException e) {
            throw new JsonException("Number \"" + key + "\" not found.", e);
        }
    }

    public long getLong(String key, long fallback) {
        return JsonUtils.getAsLong(this.json.get(key), fallback);
    }

    public float getFloat(String key) throws JsonException {
        try {
            return JsonUtils.getAsFloat(this.json.get(key));
        }
        catch (JsonException e) {
            throw new JsonException("Number \"" + key + "\" not found.", e);
        }
    }

    public float getFloat(String key, float fallback) {
        return JsonUtils.getAsFloat(this.json.get(key), fallback);
    }

    public double getDouble(String key) throws JsonException {
        try {
            return JsonUtils.getAsDouble(this.json.get(key));
        }
        catch (JsonException e) {
            throw new JsonException("Number \"" + key + "\" not found.", e);
        }
    }

    public double getDouble(String key, double fallback) {
        return JsonUtils.getAsDouble(this.json.get(key), fallback);
    }

    public String getString(String key) throws JsonException {
        try {
            return JsonUtils.getAsString(this.json.get(key));
        }
        catch (JsonException e) {
            throw new JsonException("String \"" + key + "\" not found.", e);
        }
    }

    public String getString(String key, String fallback) {
        return JsonUtils.getAsString(this.json.get(key), fallback);
    }

    public WsonArray getArray(String key) throws JsonException {
        try {
            return JsonUtils.getAsArray(this.json.get(key));
        }
        catch (JsonException e) {
            throw new JsonException("Array \"" + key + "\" not found.", e);
        }
    }

    public WsonObject getObject(String key) throws JsonException {
        try {
            return JsonUtils.getAsObject(this.json.get(key));
        }
        catch (JsonException e) {
            throw new JsonException("Object \"" + key + "\" not found.", e);
        }
    }

    public JsonElement getElement(String key) {
        return this.json.get(key);
    }

    public LinkedHashMap<String, String> getAllStrings() {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        for (Map.Entry<String, JsonElement> entry : this.json.entrySet()) {
            JsonElement value = entry.getValue();
            if (!JsonUtils.isString(value)) continue;
            map.put(entry.getKey(), value.getAsString());
        }
        return map;
    }

    public LinkedHashMap<String, Number> getAllNumbers() {
        LinkedHashMap<String, Number> map = new LinkedHashMap<String, Number>();
        for (Map.Entry<String, JsonElement> entry : this.json.entrySet()) {
            JsonElement value = entry.getValue();
            if (!JsonUtils.isNumber(value)) continue;
            map.put(entry.getKey(), value.getAsNumber());
        }
        return map;
    }

    public LinkedHashMap<String, JsonObject> getAllJsonObjects() {
        LinkedHashMap<String, JsonObject> map = new LinkedHashMap<String, JsonObject>();
        for (Map.Entry<String, JsonElement> entry : this.json.entrySet()) {
            JsonElement value = entry.getValue();
            if (!value.isJsonObject()) continue;
            map.put(entry.getKey(), value.getAsJsonObject());
        }
        return map;
    }

    public boolean has(String memberName) {
        return this.json.has(memberName);
    }

    public JsonObject toJsonObject() {
        return this.json;
    }

    public String toString() {
        return this.json.toString();
    }
}
