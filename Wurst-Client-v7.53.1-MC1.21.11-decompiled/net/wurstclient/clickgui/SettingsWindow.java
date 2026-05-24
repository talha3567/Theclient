package net.wurstclient.clickgui;

import java.util.stream.Stream;
import net.minecraft.class_1041;
import net.minecraft.class_3532;
import net.wurstclient.Feature;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.Window;
import net.wurstclient.settings.Setting;

public final class SettingsWindow
extends Window {
    public SettingsWindow(Feature feature, Window parent, int buttonY) {
        super(feature.getName() + " Settings");
        Stream<Setting> settings = feature.getSettings().values().stream();
        settings.map(Setting::getComponent).forEach(this::add);
        this.setClosable(true);
        this.setMinimizable(false);
        this.setMaxHeight(200);
        this.pack();
        this.setInitialPosition(parent, buttonY);
    }

    private void setInitialPosition(Window parent, int buttonY) {
        int scroll = parent.isScrollingEnabled() ? parent.getScrollOffset() : 0;
        int x = parent.getX() + parent.getWidth() + 5;
        int y = parent.getY() + 12 + buttonY + scroll;
        class_1041 mcWindow = WurstClient.MC.method_22683();
        if (x + this.getWidth() > mcWindow.method_4486()) {
            x = parent.getX() - this.getWidth() - 5;
        }
        if (y + this.getHeight() > mcWindow.method_4502()) {
            y -= this.getHeight() - 14;
        }
        x = class_3532.method_15340((int)x, (int)0, (int)mcWindow.method_4486());
        y = class_3532.method_15340((int)y, (int)0, (int)mcWindow.method_4502());
        this.setX(x);
        this.setY(y);
    }
}
