package net.wurstclient.util;

public final class WurstColors
extends Enum<WurstColors> {
    public static final int VERY_LIGHT_GRAY = -986896;
    public static final int LIGHT_RED = -43691;
    private static final /* synthetic */ WurstColors[] $VALUES;

    public static WurstColors[] values() {
        return (WurstColors[])$VALUES.clone();
    }

    public static WurstColors valueOf(String name) {
        return Enum.valueOf(WurstColors.class, name);
    }

    private static /* synthetic */ WurstColors[] $values() {
        return new WurstColors[0];
    }

    static {
        $VALUES = WurstColors.$values();
    }
}
