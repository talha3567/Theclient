package net.wurstclient.clickgui.components;

import java.util.Arrays;
import net.minecraft.class_11909;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.wurstclient.clickgui.ClickGui;
import net.wurstclient.clickgui.ClickGuiIcons;
import net.wurstclient.clickgui.ComboBoxPopup;
import net.wurstclient.clickgui.Component;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.util.RenderUtils;

public final class ComboBoxComponent<T extends Enum<T>>
extends Component {
    private static final ClickGui GUI = WURST.getGui();
    private static final class_327 TR = ComboBoxComponent.MC.field_1772;
    private static final int ARROW_SIZE = 11;
    private final EnumSetting<T> setting;
    private final int popupWidth;
    private ComboBoxPopup<T> popup;

    public ComboBoxComponent(EnumSetting<T> setting) {
        this.setting = setting;
        this.popupWidth = Arrays.stream(setting.getValues()).map(Enum::toString).mapToInt(s -> TR.method_1727(s)).max().getAsInt();
        this.setWidth(this.getDefaultWidth());
        this.setHeight(this.getDefaultHeight());
    }

    @Override
    public void handleMouseClick(double mouseX, double mouseY, int mouseButton, class_11909 context) {
        if (mouseX < (double)(this.getX() + this.getWidth() - this.popupWidth - 11 - 4)) {
            return;
        }
        switch (mouseButton) {
            case 0: {
                this.handleLeftClick();
                break;
            }
            case 1: {
                this.handleRightClick();
            }
        }
    }

    private void handleLeftClick() {
        if (this.isPopupOpen()) {
            this.popup.close();
            this.popup = null;
            return;
        }
        this.popup = new ComboBoxPopup<T>(this, this.setting, this.popupWidth);
        GUI.addPopup(this.popup);
    }

    private void handleRightClick() {
        if (this.isPopupOpen()) {
            return;
        }
        this.setting.setSelected(this.setting.getDefaultSelected());
    }

    private boolean isPopupOpen() {
        return this.popup != null && !this.popup.isClosing();
    }

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float partialTicks) {
        boolean hBox;
        int x1 = this.getX();
        int x2 = x1 + this.getWidth();
        int x3 = x2 - 11;
        int x4 = x3 - this.popupWidth - 4;
        int y1 = this.getY();
        int y2 = y1 + this.getHeight();
        boolean hovering = this.isHovering(mouseX, mouseY);
        boolean hText = hovering && mouseX < x4;
        boolean bl = hBox = hovering && mouseX >= x4;
        if (hText) {
            GUI.setTooltip(this.setting.getWrappedDescription(200));
        }
        context.method_25294(x1, y1, x4, y2, this.getFillColor(false));
        context.method_25294(x4, y1, x2, y2, this.getFillColor(hBox));
        context.field_59826.method_71067();
        int outlineColor = RenderUtils.toIntColor(GUI.getAcColor(), 0.5f);
        RenderUtils.drawBorder2D(context, x4, y1, x2, y2, outlineColor);
        RenderUtils.drawLine2D(context, x3, y1, x3, y2, outlineColor);
        ClickGuiIcons.drawMinimizeArrow(context, x3, (float)y1 + 0.5f, x2, (float)y2 - 0.5f, hBox, !this.isPopupOpen());
        String name = this.setting.getName();
        String value = String.valueOf(this.setting.getSelected());
        int txtColor = GUI.getTxtColor();
        context.method_51433(TR, name, x1, y1 + 2, txtColor, false);
        context.method_51433(TR, value, x4 + 2, y1 + 2, txtColor, false);
    }

    private int getFillColor(boolean hovering) {
        float opacity = GUI.getOpacity() * (hovering ? 1.5f : 1.0f);
        return RenderUtils.toIntColor(GUI.getBgColor(), opacity);
    }

    @Override
    public int getDefaultWidth() {
        return TR.method_1727(this.setting.getName()) + this.popupWidth + 11 + 6;
    }

    @Override
    public int getDefaultHeight() {
        return 11;
    }
}
