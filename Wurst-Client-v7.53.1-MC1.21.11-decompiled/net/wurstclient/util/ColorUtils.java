package net.wurstclient.util;

import java.awt.Color;
import net.minecraft.class_3532;
import net.wurstclient.util.json.JsonException;

public final class ColorUtils
extends Enum<ColorUtils> {
    private static final /* synthetic */ ColorUtils[] $VALUES;

    public static ColorUtils[] values() {
        return (ColorUtils[])$VALUES.clone();
    }

    public static ColorUtils valueOf(String name) {
        return Enum.valueOf(ColorUtils.class, name);
    }

    public static String toHex(Color color) {
        return String.format("#%06X", color.getRGB() & 0xFFFFFF);
    }

    public static Color parseHex(String s) throws JsonException {
        if (!s.startsWith("#")) {
            throw new JsonException("Missing '#' prefix.");
        }
        if (s.length() != 7) {
            throw new JsonException("Expected String of length 7, got " + s.length() + " instead.");
        }
        int[] rgb = new int[3];
        try {
            for (int i = 0; i < rgb.length; ++i) {
                String channelString = s.substring(i * 2 + 1, i * 2 + 3);
                int channel = Integer.parseUnsignedInt(channelString, 16);
                rgb[i] = class_3532.method_15340((int)channel, (int)0, (int)255);
            }
        }
        catch (NumberFormatException e) {
            throw new JsonException(e);
        }
        return new Color(rgb[0], rgb[1], rgb[2]);
    }

    public static Color tryParseHex(String s) {
        try {
            return ColorUtils.parseHex(s);
        }
        catch (JsonException e) {
            return null;
        }
    }

    public static Color parseRGB(String red, String green, String blue) throws JsonException {
        String[] rgbStrings = new String[]{red, green, blue};
        int[] rgb = new int[3];
        try {
            for (int i = 0; i < rgb.length; ++i) {
                int channel = Integer.parseInt(rgbStrings[i]);
                rgb[i] = class_3532.method_15340((int)channel, (int)0, (int)255);
            }
        }
        catch (NumberFormatException e) {
            throw new JsonException(e);
        }
        return new Color(rgb[0], rgb[1], rgb[2]);
    }

    public static Color tryParseRGB(String red, String green, String blue) {
        try {
            return ColorUtils.parseRGB(red, green, blue);
        }
        catch (JsonException e) {
            return null;
        }
    }

    private static /* synthetic */ ColorUtils[] $values() {
        return new ColorUtils[0];
    }

    static {
        $VALUES = ColorUtils.$values();
    }
}
