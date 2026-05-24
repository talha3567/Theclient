package net.wurstclient.hacks.newchunks;

import net.wurstclient.settings.EnumSetting;

public final class NewChunksShowSetting
extends EnumSetting<Show> {
    public NewChunksShowSetting() {
        super("Show", (Enum[])Show.values(), (Enum)Show.NEW_CHUNKS);
    }

    public static enum Show {
        NEW_CHUNKS("New Chunks", true, false),
        OLD_CHUNKS("Old Chunks", false, true),
        BOTH("Both", true, true);

        private final String name;
        private final boolean includeNew;
        private final boolean includeOld;

        private Show(String name, boolean showNew, boolean showOld) {
            this.name = name;
            this.includeNew = showNew;
            this.includeOld = showOld;
        }

        public String toString() {
            return this.name;
        }

        public boolean includesNew() {
            return this.includeNew;
        }

        public boolean includesOld() {
            return this.includeOld;
        }
    }
}
