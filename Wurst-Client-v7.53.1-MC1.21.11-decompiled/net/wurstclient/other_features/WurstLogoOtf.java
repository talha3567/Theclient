package net.wurstclient.other_features;

import java.awt.Color;
import java.util.function.BooleanSupplier;
import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.other_feature.OtherFeature;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.EnumSetting;

@SearchTags(value={"wurst logo", "top left corner"})
@DontBlock
public final class WurstLogoOtf
extends OtherFeature {
    private final ColorSetting bgColor = new ColorSetting("Background", "Background color.\nOnly visible when \u00a76RainbowUI\u00a7r is disabled.", Color.WHITE);
    private final ColorSetting txtColor = new ColorSetting("Text", "Text color.", Color.BLACK);
    private final EnumSetting<Visibility> visibility = new EnumSetting("Visibility", (Enum[])Visibility.values(), (Enum)Visibility.ALWAYS);

    public WurstLogoOtf() {
        super("WurstLogo", "Shows the Wurst logo and version on the screen.");
        this.addSetting(this.bgColor);
        this.addSetting(this.txtColor);
        this.addSetting(this.visibility);
    }

    public boolean isVisible() {
        return this.visibility.getSelected().isVisible();
    }

    public int getBackgroundColor() {
        return this.bgColor.getColorI(128);
    }

    public int getTextColor() {
        return this.txtColor.getColorI();
    }

    public static enum Visibility {
        ALWAYS("Always", () -> true),
        ONLY_OUTDATED("Only when outdated", () -> WURST.getUpdater().isOutdated());

        private final String name;
        private final BooleanSupplier visible;

        private Visibility(String name, BooleanSupplier visible) {
            this.name = name;
            this.visible = visible;
        }

        public boolean isVisible() {
            return this.visible.getAsBoolean();
        }

        public String toString() {
            return this.name;
        }
    }
}
