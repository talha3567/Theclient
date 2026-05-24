package net.wurstclient.util.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.MalformedJsonException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.WsonArray;
import net.wurstclient.util.json.WsonObject;

public final class JsonUtils
extends Enum<JsonUtils> {
    public static final Gson GSON;
    public static final Gson PRETTY_GSON;
    private static final /* synthetic */ JsonUtils[] $VALUES;

    public static JsonUtils[] values() {
        return (JsonUtils[])$VALUES.clone();
    }

    public static JsonUtils valueOf(String name) {
        return Enum.valueOf(JsonUtils.class, name);
    }

    public static JsonElement parseFile(Path path) throws IOException, JsonException {
        JsonElement jsonElement;
        block9: {
            BufferedReader reader = Files.newBufferedReader(path);
            try {
                jsonElement = JsonParser.parseReader(reader);
                if (reader == null) break block9;
            }
            catch (Throwable throwable) {
                try {
                    if (reader != null) {
                        try {
                            reader.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (JsonParseException e) {
                    Throwable throwable3 = e.getCause();
                    if (throwable3 instanceof MalformedJsonException) {
                        MalformedJsonException c = (MalformedJsonException)throwable3;
                        throw new JsonException(c.getMessage(), c);
                    }
                    throw new JsonException(e);
                }
            }
            reader.close();
        }
        return jsonElement;
    }

    public static WsonArray parseFileToArray(Path path) throws IOException, JsonException {
        return JsonUtils.getAsArray(JsonUtils.parseFile(path));
    }

    public static WsonObject parseFileToObject(Path path) throws IOException, JsonException {
        return JsonUtils.getAsObject(JsonUtils.parseFile(path));
    }

    public static JsonElement parseURL(String url) throws IOException, JsonException {
        JsonElement jsonElement;
        block9: {
            URI uri = URI.create(url);
            InputStream input = uri.toURL().openStream();
            try {
                InputStreamReader reader = new InputStreamReader(input);
                BufferedReader bufferedReader = new BufferedReader(reader);
                jsonElement = JsonParser.parseReader(bufferedReader);
                if (input == null) break block9;
            }
            catch (Throwable reader) {
                try {
                    if (input != null) {
                        try {
                            input.close();
                        }
                        catch (Throwable throwable) {
                            reader.addSuppressed(throwable);
                        }
                    }
                    throw reader;
                }
                catch (JsonParseException e) {
                    Throwable throwable = e.getCause();
                    if (throwable instanceof MalformedJsonException) {
                        MalformedJsonException c = (MalformedJsonException)throwable;
                        throw new JsonException(c.getMessage(), c);
                    }
                    throw new JsonException(e);
                }
            }
            input.close();
        }
        return jsonElement;
    }

    public static WsonArray parseURLToArray(String url) throws IOException, JsonException {
        return JsonUtils.getAsArray(JsonUtils.parseURL(url));
    }

    public static WsonObject parseURLToObject(String url) throws IOException, JsonException {
        return JsonUtils.getAsObject(JsonUtils.parseURL(url));
    }

    public static JsonElement parseConnection(URLConnection connection) throws IOException, JsonException {
        JsonElement jsonElement;
        block9: {
            InputStream input = connection.getInputStream();
            try {
                InputStreamReader reader = new InputStreamReader(input);
                BufferedReader bufferedReader = new BufferedReader(reader);
                jsonElement = JsonParser.parseReader(bufferedReader);
                if (input == null) break block9;
            }
            catch (Throwable reader) {
                try {
                    if (input != null) {
                        try {
                            input.close();
                        }
                        catch (Throwable throwable) {
                            reader.addSuppressed(throwable);
                        }
                    }
                    throw reader;
                }
                catch (JsonParseException e) {
                    Throwable throwable = e.getCause();
                    if (throwable instanceof MalformedJsonException) {
                        MalformedJsonException c = (MalformedJsonException)throwable;
                        throw new JsonException(c.getMessage(), c);
                    }
                    throw new JsonException(e);
                }
            }
            input.close();
        }
        return jsonElement;
    }

    public static WsonArray parseConnectionToArray(URLConnection connection) throws IOException, JsonException {
        return JsonUtils.getAsArray(JsonUtils.parseConnection(connection));
    }

    public static WsonObject parseConnectionToObject(URLConnection connection) throws IOException, JsonException {
        return JsonUtils.getAsObject(JsonUtils.parseConnection(connection));
    }

    public static void toJson(JsonElement json, Path path) throws IOException, JsonException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, new OpenOption[0]);){
            PRETTY_GSON.toJson(json, (Appendable)writer);
        }
        catch (JsonParseException e) {
            throw new JsonException(e);
        }
    }

    public static boolean isBoolean(JsonElement json) {
        if (json == null || !json.isJsonPrimitive()) {
            return false;
        }
        JsonPrimitive primitive = json.getAsJsonPrimitive();
        return primitive.isBoolean();
    }

    public static boolean getAsBoolean(JsonElement json) throws JsonException {
        if (!JsonUtils.isBoolean(json)) {
            throw new JsonException("Not a boolean: " + String.valueOf(json));
        }
        return json.getAsBoolean();
    }

    public static boolean getAsBoolean(JsonElement json, boolean fallback) {
        if (!JsonUtils.isBoolean(json)) {
            return fallback;
        }
        return json.getAsBoolean();
    }

    public static boolean isNumber(JsonElement json) {
        if (json == null || !json.isJsonPrimitive()) {
            return false;
        }
        JsonPrimitive primitive = json.getAsJsonPrimitive();
        return primitive.isNumber();
    }

    public static int getAsInt(JsonElement json) throws JsonException {
        if (!JsonUtils.isNumber(json)) {
            throw new JsonException("Not a number: " + String.valueOf(json));
        }
        return json.getAsInt();
    }

    public static int getAsInt(JsonElement json, int fallback) {
        if (!JsonUtils.isNumber(json)) {
            return fallback;
        }
        return json.getAsInt();
    }

    public static long getAsLong(JsonElement json) throws JsonException {
        if (!JsonUtils.isNumber(json)) {
            throw new JsonException("Not a number: " + String.valueOf(json));
        }
        return json.getAsLong();
    }

    public static long getAsLong(JsonElement json, long fallback) {
        if (!JsonUtils.isNumber(json)) {
            return fallback;
        }
        return json.getAsLong();
    }

    public static float getAsFloat(JsonElement json) throws JsonException {
        if (!JsonUtils.isNumber(json)) {
            throw new JsonException("Not a number: " + String.valueOf(json));
        }
        return json.getAsFloat();
    }

    public static float getAsFloat(JsonElement json, float fallback) {
        if (!JsonUtils.isNumber(json)) {
            return fallback;
        }
        return json.getAsFloat();
    }

    public static double getAsDouble(JsonElement json) throws JsonException {
        if (!JsonUtils.isNumber(json)) {
            throw new JsonException("Not a number: " + String.valueOf(json));
        }
        return json.getAsDouble();
    }

    public static double getAsDouble(JsonElement json, double fallback) {
        if (!JsonUtils.isNumber(json)) {
            return fallback;
        }
        return json.getAsDouble();
    }

    public static boolean isString(JsonElement json) {
        if (json == null || !json.isJsonPrimitive()) {
            return false;
        }
        JsonPrimitive primitive = json.getAsJsonPrimitive();
        return primitive.isString();
    }

    public static String getAsString(JsonElement json) throws JsonException {
        if (!JsonUtils.isString(json)) {
            throw new JsonException("Not a string: " + String.valueOf(json));
        }
        return json.getAsString();
    }

    public static String getAsString(JsonElement json, String fallback) {
        if (!JsonUtils.isString(json)) {
            return fallback;
        }
        return json.getAsString();
    }

    public static WsonArray getAsArray(JsonElement json) throws JsonException {
        if (json == null || !json.isJsonArray()) {
            throw new JsonException("Not an array: " + String.valueOf(json));
        }
        return new WsonArray(json.getAsJsonArray());
    }

    public static WsonObject getAsObject(JsonElement json) throws JsonException {
        if (json == null || !json.isJsonObject()) {
            throw new JsonException("Not an object: " + String.valueOf(json));
        }
        return new WsonObject(json.getAsJsonObject());
    }

    private static /* synthetic */ JsonUtils[] $values() {
        return new JsonUtils[0];
    }

    static {
        $VALUES = JsonUtils.$values();
        GSON = new Gson();
        PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();
    }
}
