package net.wurstclient.other_features;

import net.minecraft.class_10799;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_5250;
import net.minecraft.class_7919;
import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.other_feature.OtherFeature;
import net.wurstclient.settings.EnumSetting;

@SearchTags(value={"wurst options", "settings"})
@DontBlock
public final class WurstOptionsOtf
extends OtherFeature {
    private static final class_2960 WURST_TEXTURE = class_2960.method_60655((String)"wurst", (String)"wurst_128.png");
    private final EnumSetting<Location> location = new EnumSetting("Location", "description.wurst.setting.wurstoptions.location", (Enum[])Location.values(), (Enum)Location.GAME_MENU);

    public WurstOptionsOtf() {
        super("WurstOptions", "description.wurst.other_feature.wurstoptions");
        this.addSetting(this.location);
    }

    public boolean isVisibleInGameMenu() {
        return WURST.isEnabled() && this.location.getSelected() == Location.GAME_MENU;
    }

    public boolean isVisibleInStatistics() {
        return WURST.isEnabled() && this.location.getSelected() == Location.STATISTICS;
    }

    public class_4185.class_7840 buttonBuilder(class_4185.class_4241 onPress) {
        class_5250 message = class_2561.method_43470((String)"            Options");
        class_5250 narration = class_2561.method_43469((String)"gui.narrate.button", (Object[])new Object[]{"Wurst Options"});
        class_7919 tooltip = class_7919.method_47407((class_2561)class_2561.method_43470((String)this.getDescription()));
        return class_4185.method_46430((class_2561)message, (class_4185.class_4241)onPress).method_46435(sup -> narration).method_46436(tooltip);
    }

    public void drawWurstLogoOnButton(class_332 context, class_4185 wurstOptionsButton) {
        if (wurstOptionsButton == null) {
            return;
        }
        int x = wurstOptionsButton.method_46426() + 34;
        int y = wurstOptionsButton.method_46427() + 2;
        int w = 63;
        int h = 16;
        int fw = 63;
        int fh = 16;
        float u = 0.0f;
        float v = 0.0f;
        context.field_59826.method_71067();
        context.method_25290(class_10799.field_56883, WURST_TEXTURE, x, y, u, v, w, h, fw, fh);
    }

    private static enum Location {
        GAME_MENU("Game Menu"),
        STATISTICS("Statistics");

        private final String name;

        private Location(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}
