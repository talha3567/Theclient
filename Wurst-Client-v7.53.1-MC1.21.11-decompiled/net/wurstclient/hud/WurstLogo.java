package net.wurstclient.hud;

import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.wurstclient.WurstClient;
import net.wurstclient.other_features.WurstLogoOtf;
import net.wurstclient.util.RenderUtils;

public final class WurstLogo {
    private static final WurstClient WURST = WurstClient.INSTANCE;
    private static final class_2960 LOGO_TEXTURE = class_2960.method_60655((String)"wurst", (String)"wurst_128.png");

    public void render(class_332 context) {
        WurstLogoOtf otf = WurstLogo.WURST.getOtfs().wurstLogoOtf;
        if (!otf.isVisible()) {
            return;
        }
        String version = this.getVersionString();
        class_327 tr = WurstClient.MC.field_1772;
        int bgColor = WurstLogo.WURST.getHax().rainbowUiHack.isEnabled() ? RenderUtils.toIntColor(WURST.getGui().getAcColor(), 0.5f) : otf.getBackgroundColor();
        context.method_25294(0, 6, tr.method_1727(version) + 76, 17, bgColor);
        context.field_59826.method_71067();
        context.method_51433(tr, version, 74, 8, otf.getTextColor(), false);
        context.method_25290(class_10799.field_56883, LOGO_TEXTURE, 0, 3, 0.0f, 0.0f, 72, 18, 72, 18);
    }

    private String getVersionString() {
        Object version = "v7.53.1";
        version = (String)version + " MC1.21.11";
        if (WURST.getUpdater().isOutdated()) {
            version = (String)version + " (outdated)";
        }
        return version;
    }
}
