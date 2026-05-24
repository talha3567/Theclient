package net.wurstclient.other_features;

import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.other_feature.OtherFeature;
import net.wurstclient.settings.EnumSetting;

@SearchTags(value={"tab gui", "HackMenu", "hack menu", "SideBar", "side bar", "blocks movement combat render chat fun items other"})
@DontBlock
public final class TabGuiOtf
extends OtherFeature {
    private final EnumSetting<Status> status = new EnumSetting("Status", (Enum[])Status.values(), (Enum)Status.DISABLED);

    public TabGuiOtf() {
        super("TabGUI", "Allows you to quickly toggle hacks while playing.\nUse the arrow keys to navigate.\n\nChange the \u00a76HackList \u00a76Position\u00a7r setting to \u00a76Right\u00a7r to prevent TabGUI from overlapping with the HackList.");
        this.addSetting(this.status);
    }

    public boolean isHidden() {
        return this.status.getSelected() == Status.DISABLED;
    }

    private static enum Status {
        ENABLED("Enabled"),
        DISABLED("Disabled");

        private final String name;

        private Status(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}
