package net.wurstclient.clickgui.components;

import java.util.Objects;
import net.minecraft.class_11909;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.wurstclient.Feature;
import net.wurstclient.clickgui.ClickGui;
import net.wurstclient.clickgui.ClickGuiIcons;
import net.wurstclient.clickgui.Component;
import net.wurstclient.clickgui.SettingsWindow;
import net.wurstclient.clickgui.Window;
import net.wurstclient.hacks.TooManyHaxHack;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.RenderUtils;

public final class FeatureButton
extends Component {
    private static final ClickGui GUI = WURST.getGui();
    private static final class_327 TR = FeatureButton.MC.field_1772;
    private final Feature feature;
    private final boolean hasSettings;
    private Window settingsWindow;

    public FeatureButton(Feature feature) {
        this.feature = Objects.requireNonNull(feature);
        this.setWidth(this.getDefaultWidth());
        this.setHeight(this.getDefaultHeight());
        this.hasSettings = !feature.getSettings().isEmpty();
    }

    @Override
    public void handleMouseClick(double mouseX, double mouseY, int mouseButton, class_11909 context) {
        if (mouseButton != 0) {
            return;
        }
        if (this.hasSettings && (mouseX > (double)(this.getX() + this.getWidth() - 12) || this.feature.getPrimaryAction().isEmpty())) {
            this.toggleSettingsWindow();
            return;
        }
        TooManyHaxHack tooManyHax = FeatureButton.WURST.getHax().tooManyHaxHack;
        if (tooManyHax.isEnabled() && tooManyHax.isBlocked(this.feature)) {
            ChatUtils.error(this.feature.getName() + " is blocked by TooManyHax.");
            return;
        }
        this.feature.doPrimaryAction();
    }

    private boolean isSettingsWindowOpen() {
        return this.settingsWindow != null && !this.settingsWindow.isClosing();
    }

    private void toggleSettingsWindow() {
        if (!this.isSettingsWindowOpen()) {
            this.settingsWindow = new SettingsWindow(this.feature, this.getParent(), this.getY());
            GUI.addWindow(this.settingsWindow);
        } else {
            this.settingsWindow.close();
            this.settingsWindow = null;
        }
    }

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float partialTicks) {
        boolean hSettings;
        int x1 = this.getX();
        int x2 = x1 + this.getWidth();
        int x3 = this.hasSettings ? x2 - 11 : x2;
        int y1 = this.getY();
        int y2 = y1 + this.getHeight();
        boolean hovering = this.isHovering(mouseX, mouseY);
        boolean hFeature = hovering && mouseX < x3;
        boolean bl = hSettings = hovering && mouseX >= x3;
        if (hFeature) {
            GUI.setTooltip(this.feature.getWrappedDescription(200));
        }
        context.method_25294(x1, y1, x3, y2, this.getButtonColor(this.feature.isEnabled(), hFeature));
        if (this.hasSettings) {
            context.method_25294(x3, y1, x2, y2, this.getButtonColor(false, hSettings));
        }
        context.field_59826.method_71067();
        int outlineColor = RenderUtils.toIntColor(GUI.getAcColor(), 0.5f);
        RenderUtils.drawBorder2D(context, x1, y1, x2, y2, outlineColor);
        if (this.hasSettings) {
            RenderUtils.drawLine2D(context, x3, y1, x3, y2, outlineColor);
        }
        if (this.hasSettings) {
            ClickGuiIcons.drawMinimizeArrow(context, x3, (float)y1 + 0.5f, x2, (float)y2 - 0.5f, hSettings, !this.isSettingsWindowOpen());
        }
        String name = this.feature.getName();
        int tx = x1 + (x3 - x1 - TR.method_1727(name)) / 2;
        int ty = y1 + 2;
        context.method_51433(TR, name, tx, ty, GUI.getTxtColor(), false);
    }

    private int getButtonColor(boolean enabled, boolean hovering) {
        float[] fArray;
        if (enabled) {
            float[] fArray2 = new float[3];
            fArray2[0] = 0.0f;
            fArray2[1] = 1.0f;
            fArray = fArray2;
            fArray2[2] = 0.0f;
        } else {
            fArray = GUI.getBgColor();
        }
        float[] rgb = fArray;
        float opacity = GUI.getOpacity() * (hovering ? 1.5f : 1.0f);
        return RenderUtils.toIntColor(rgb, opacity);
    }

    @Override
    public int getDefaultWidth() {
        int width = TR.method_1727(this.feature.getName());
        return width += this.hasSettings ? 15 : 4;
    }

    @Override
    public int getDefaultHeight() {
        return 11;
    }
}
