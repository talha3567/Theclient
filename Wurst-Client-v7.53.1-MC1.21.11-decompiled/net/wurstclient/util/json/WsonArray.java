package net.wurstclient.util.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.json.WsonObject;

public final class WsonArray {
    private final JsonArray json;

    public WsonArray(JsonArray json) {
        this.json = Objects.requireNonNull(json);
    }

    public boolean getBoolean(int index) throws JsonException {
        try {
            return JsonUtils.getAsBoolean(this.getElement(index));
        }
        catch (JsonException e) {
            throw new JsonException("Boolean at [" + index + "] not found.", e);
        }
    }

    public boolean getBoolean(int index, boolean fallback) {
        return JsonUtils.getAsBoolean(this.getElement(index, null), fallback);
    }

    public int getInt(int index) throws JsonException {
        try {
            return JsonUtils.getAsInt(this.getElement(index));
        }
        catch (JsonException e) {
            throw new JsonException("Number at [" + index + "] not found.", e);
        }
    }

    public int getInt(int index, int fallback) {
        return JsonUtils.getAsInt(this.getElement(index, null), fallback);
    }

    public long getLong(int index) throws JsonException {
        try {
            return JsonUtils.getAsLong(this.getElement(index));
        }
        catch (JsonException e) {
            throw new JsonException("Number at [" + index + "] not found.", e);
        }
    }

    public long getLong(int index, long fallback) {
        return JsonUtils.getAsLong(this.getElement(index, null), fallback);
    }

    public float getFloat(int index) throws JsonException {
        try {
            return JsonUtils.getAsFloat(this.getElement(index));
        }
        catch (JsonException e) {
            throw new JsonException("Number at [" + index + "] not found.", e);
        }
    }

    public float getFloat(int index, float fallback) {
        return JsonUtils.getAsFloat(this.getElement(index, null), fallback);
    }

    public double getDouble(int index) throws JsonException {
        try {
            return JsonUtils.getAsDouble(this.getElement(index));
        }
        catch (JsonException e) {
            throw new JsonException("Number at [" + index + "] not found.", e);
        }
    }

    public double getDouble(int index, double fallback) {
        return JsonUtils.getAsDouble(this.getElement(index, null), fallback);
    }

    public String getString(int index) throws JsonException {
        try {
            return JsonUtils.getAsString(this.getElement(index));
        }
        catch (JsonException e) {
            throw new JsonException("String at [" + index + "] not found.", e);
        }
    }

    public String getString(int index, String fallback) {
        return JsonUtils.getAsString(this.getElement(index, null), fallback);
    }

    public WsonArray getArray(int index) throws JsonException {
        try {
            return JsonUtils.getAsArray(this.getElement(index));
        }
        catch (JsonException e) {
            throw new JsonException("Array at [" + index + "] not found.", e);
        }
    }

    public WsonObject getObject(int index) throws JsonException {
        try {
            return JsonUtils.getAsObject(this.getElement(index));
        }
        catch (JsonException e) {
            throw new JsonException("Object at [" + index + "] not found.", e);
        }
    }

    public JsonElement getElement(int index) throws JsonException {
        try {
            return this.json.get(index);
        }
        catch (IndexOutOfBoundsException e) {
            throw new JsonException(e.getMessage());
        }
    }

    public JsonElement getElement(int index, JsonElement fallback) {
        try {
            return this.json.get(index);
        }
        catch (IndexOutOfBoundsException e) {
            return fallback;
        }
    }

    public ArrayList<String> getAllStrings() {
        return StreamSupport.stream(this.json.spliterator(), false).filter(JsonUtils::isString).map(JsonElement::getAsString).collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<WsonObject> getAllObjects() {
        return StreamSupport.stream(this.json.spliterator(), false).filter(JsonElement::isJsonObject).map(JsonElement::getAsJsonObject).map(WsonObject::new).collect(Collectors.toCollection(ArrayList::new));
    }

    public int size() {
        return this.json.size();
    }

    public boolean isEmpty() {
        return this.json.isEmpty();
    }

    public JsonArray toJsonArray() {
        return this.json;
    }

    public String toString() {
        return this.json.toString();
    }
}
