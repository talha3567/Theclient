package net.wurstclient.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.awt.Color;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import net.minecraft.class_3532;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.Component;
import net.wurstclient.clickgui.components.ColorComponent;
import net.wurstclient.keybinds.PossibleKeybind;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.ColorUtils;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.text.WText;

public final class ColorSetting
extends Setting {
    private Color color;
    private final Color defaultColor;

    public ColorSetting(String name, WText description, Color color) {
        super(name, description);
        this.color = Objects.requireNonNull(color);
        this.defaultColor = color;
    }

    public ColorSetting(String name, String descriptionKey, Color color) {
        this(name, WText.translated(descriptionKey, new Object[0]), color);
    }

    public ColorSetting(String name, Color color) {
        this(name, WText.empty(), color);
    }

    public Color getColor() {
        return this.color;
    }

    public float[] getColorF() {
        float red = (float)this.color.getRed() / 255.0f;
        float green = (float)this.color.getGreen() / 255.0f;
        float blue = (float)this.color.getBlue() / 255.0f;
        return new float[]{red, green, blue};
    }

    public int getColorI() {
        return this.color.getRGB() | 0xFF000000;
    }

    public int getColorI(int alpha) {
        return this.color.getRGB() & 0xFFFFFF | alpha << 24;
    }

    public int getColorI(float alpha) {
        return this.getColorI((int)(class_3532.method_15363((float)alpha, (float)0.0f, (float)1.0f) * 255.0f));
    }

    public int getRed() {
        return this.color.getRed();
    }

    public int getGreen() {
        return this.color.getGreen();
    }

    public int getBlue() {
        return this.color.getBlue();
    }

    public Color getDefaultColor() {
        return this.defaultColor;
    }

    public void setColor(Color color) {
        this.color = Objects.requireNonNull(color);
        WurstClient.INSTANCE.saveSettings();
    }

    @Override
    public Component getComponent() {
        return new ColorComponent(this);
    }

    @Override
    public void fromJson(JsonElement json) {
        if (!JsonUtils.isString(json)) {
            return;
        }
        try {
            this.setColor(ColorUtils.parseHex(json.getAsString()));
        }
        catch (JsonException e) {
            e.printStackTrace();
            this.setColor(this.defaultColor);
        }
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(ColorUtils.toHex(this.color));
    }

    @Override
    public JsonObject exportWikiData() {
        JsonObject json = new JsonObject();
        json.addProperty("name", this.getName());
        json.addProperty("description", this.getDescription());
        json.addProperty("type", "Color");
        json.addProperty("defaultColor", ColorUtils.toHex(this.defaultColor));
        return json;
    }

    @Override
    public Set<PossibleKeybind> getPossibleKeybinds(String featureName) {
        String description = "Set " + featureName + " " + this.getName() + " to ";
        String command = ".setcolor " + featureName.toLowerCase() + " " + this.getName().toLowerCase().replace(" ", "_") + " ";
        LinkedHashSet<PossibleKeybind> pkb = new LinkedHashSet<PossibleKeybind>();
        this.addPKB(pkb, command + "#FF0000", description + "red");
        this.addPKB(pkb, command + "#00FF00", description + "green");
        this.addPKB(pkb, command + "#0000FF", description + "blue");
        this.addPKB(pkb, command + "#FFFF00", description + "yellow");
        this.addPKB(pkb, command + "#00FFFF", description + "cyan");
        this.addPKB(pkb, command + "#FF00FF", description + "magenta");
        this.addPKB(pkb, command + "#FFFFFF", description + "white");
        this.addPKB(pkb, command + "#000000", description + "black");
        return pkb;
    }

    private void addPKB(LinkedHashSet<PossibleKeybind> pkb, String command, String description) {
        pkb.add(new PossibleKeybind(command, description));
    }
}
