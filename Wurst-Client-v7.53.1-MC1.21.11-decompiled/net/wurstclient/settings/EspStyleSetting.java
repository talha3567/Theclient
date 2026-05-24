package net.wurstclient.settings;

import net.wurstclient.settings.EnumSetting;

public final class EspStyleSetting
extends EnumSetting<EspStyle> {
    public EspStyleSetting() {
        super("Style", (Enum[])EspStyle.values(), (Enum)EspStyle.BOXES);
    }

    public EspStyleSetting(EspStyle selected) {
        super("Style", (Enum[])EspStyle.values(), (Enum)selected);
    }

    public EspStyleSetting(String name, String description, EspStyle selected) {
        super(name, description, (Enum[])EspStyle.values(), (Enum)selected);
    }

    public boolean hasBoxes() {
        return ((EspStyle)((Object)this.getSelected())).boxes;
    }

    public boolean hasLines() {
        return ((EspStyle)((Object)this.getSelected())).lines;
    }

    public static enum EspStyle {
        BOXES("Boxes only", true, false),
        LINES("Lines only", false, true),
        LINES_AND_BOXES("Lines and boxes", true, true);

        private final String name;
        private final boolean boxes;
        private final boolean lines;

        private EspStyle(String name, boolean boxes, boolean lines) {
            this.name = name;
            this.boxes = boxes;
            this.lines = lines;
        }

        public boolean hasBoxes() {
            return this.boxes;
        }

        public boolean hasLines() {
            return this.lines;
        }

        public String toString() {
            return this.name;
        }
    }
}
