package net.wurstclient.other_features;

import java.awt.Color;
import java.util.Comparator;
import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.WurstClient;
import net.wurstclient.hack.Hack;
import net.wurstclient.other_feature.OtherFeature;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.EnumSetting;

@SearchTags(value={"hack list", "HakList", "hak list", "HacksList", "hacks list", "HaxList", "hax list", "ArrayList", "array list", "ModList", "mod list", "CheatList", "cheat list"})
@DontBlock
public final class HackListOtf
extends OtherFeature {
    private final EnumSetting<Mode> mode = new EnumSetting("Mode", "\u00a7lAuto\u00a7r mode renders the whole list if it fits onto the screen.\n\u00a7lCount\u00a7r mode only renders the number of active hacks.\n\u00a7lHidden\u00a7r mode renders nothing.", (Enum[])Mode.values(), (Enum)Mode.AUTO);
    private final EnumSetting<Position> position = new EnumSetting("Position", "Which side of the screen the HackList should be shown on.\nChange this to \u00a7lRight\u00a7r when using TabGUI.", (Enum[])Position.values(), (Enum)Position.LEFT);
    private final ColorSetting color = new ColorSetting("Color", "Color of the HackList text.\nOnly visible when \u00a76RainbowUI\u00a7r is disabled.", Color.WHITE);
    private final EnumSetting<SortBy> sortBy = new EnumSetting("Sort by", "Determines how the HackList entries are sorted.\nOnly visible when \u00a76Mode\u00a7r is set to \u00a76Auto\u00a7r.", (Enum[])SortBy.values(), (Enum)SortBy.NAME);
    private final CheckboxSetting revSort = new CheckboxSetting("Reverse sorting", false);
    private final CheckboxSetting animations = new CheckboxSetting("Animations", "When enabled, entries slide into and out of the HackList as hacks are enabled and disabled.", true);
    private SortBy prevSortBy;
    private Boolean prevRevSort;

    public HackListOtf() {
        super("HackList", "Shows a list of active hacks on the screen.");
        this.addSetting(this.mode);
        this.addSetting(this.position);
        this.addSetting(this.color);
        this.addSetting(this.sortBy);
        this.addSetting(this.revSort);
        this.addSetting(this.animations);
    }

    public Mode getMode() {
        return this.mode.getSelected();
    }

    public Position getPosition() {
        return this.position.getSelected();
    }

    public boolean isAnimations() {
        return this.animations.isChecked();
    }

    public Comparator<Hack> getComparator() {
        if (this.revSort.isChecked()) {
            return this.sortBy.getSelected().comparator.reversed();
        }
        return this.sortBy.getSelected().comparator;
    }

    public boolean shouldSort() {
        try {
            if (this.sortBy.getSelected() == SortBy.WIDTH) {
                boolean bl = true;
                return bl;
            }
            if (this.sortBy.getSelected() != this.prevSortBy) {
                boolean bl = true;
                return bl;
            }
            if (!Boolean.valueOf(this.revSort.isChecked()).equals(this.prevRevSort)) {
                boolean bl = true;
                return bl;
            }
            boolean bl = false;
            return bl;
        }
        finally {
            this.prevSortBy = this.sortBy.getSelected();
            this.prevRevSort = this.revSort.isChecked();
        }
    }

    public int getColor(int alpha) {
        return this.color.getColorI(alpha);
    }

    public static enum Mode {
        AUTO("Auto"),
        COUNT("Count"),
        HIDDEN("Hidden");

        private final String name;

        private Mode(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }

    public static enum Position {
        LEFT("Left"),
        RIGHT("Right");

        private final String name;

        private Position(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }

    public static enum SortBy {
        NAME("Name", (a, b) -> a.getName().compareToIgnoreCase(b.getName())),
        WIDTH("Width", Comparator.comparingInt(h -> WurstClient.MC.field_1772.method_1727(h.getRenderName())));

        private final String name;
        private final Comparator<Hack> comparator;

        private SortBy(String name, Comparator<Hack> comparator) {
            this.name = name;
            this.comparator = comparator;
        }

        public String toString() {
            return this.name;
        }
    }
}
