package net.wurstclient.settings;

import net.wurstclient.settings.EnumSetting;

public final class EspBoxSizeSetting
extends EnumSetting<BoxSize> {
    public EspBoxSizeSetting(String description) {
        super("Box size", description, (Enum[])BoxSize.values(), (Enum)BoxSize.FANCY);
    }

    public EspBoxSizeSetting(String name, String description, BoxSize selected) {
        super(name, description, (Enum[])BoxSize.values(), (Enum)selected);
    }

    public float getExtraSize() {
        return ((BoxSize)((Object)this.getSelected())).extraSize;
    }

    public static enum BoxSize {
        ACCURATE("Accurate", 0.0f),
        FANCY("Fancy", 0.1f);

        private final String name;
        private final float extraSize;

        private BoxSize(String name, float extraSize) {
            this.name = name;
            this.extraSize = extraSize;
        }

        public float getExtraSize() {
            return this.extraSize;
        }

        public String toString() {
            return this.name;
        }
    }
}
