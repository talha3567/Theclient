package net.wurstclient.clickgui.components;

import net.minecraft.class_11909;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.wurstclient.clickgui.ClickGui;
import net.wurstclient.clickgui.Component;
import net.wurstclient.clickgui.screens.EditColorScreen;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.util.ColorUtils;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.text.WText;

public final class ColorComponent
extends Component {
    private static final ClickGui GUI = WURST.getGui();
    private static final class_327 TR = ColorComponent.MC.field_1772;
    private static final int TEXT_HEIGHT = 11;
    private final ColorSetting setting;

    public ColorComponent(ColorSetting setting) {
        this.setting = setting;
        this.setWidth(this.getDefaultWidth());
        this.setHeight(this.getDefaultHeight());
    }

    @Override
    public void handleMouseClick(double mouseX, double mouseY, int mouseButton, class_11909 context) {
        if (mouseY < (double)(this.getY() + 11)) {
            return;
        }
        switch (mouseButton) {
            case 0: {
                MC.method_1507((class_437)new EditColorScreen(ColorComponent.MC.field_1755, this.setting));
                break;
            }
            case 1: {
                this.setting.setColor(this.setting.getDefaultColor());
            }
        }
    }

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float partialTicks) {
        boolean hColor;
        int x1 = this.getX();
        int x2 = x1 + this.getWidth();
        int y1 = this.getY();
        int y2 = y1 + this.getHeight();
        int y3 = y1 + 11;
        boolean hovering = this.isHovering(mouseX, mouseY);
        boolean hText = hovering && mouseY < y3;
        boolean bl = hColor = hovering && mouseY >= y3;
        if (hText) {
            GUI.setTooltip(this.setting.getWrappedDescription(200));
        } else if (hColor) {
            GUI.setTooltip(this.getColorTooltip());
        }
        float opacity = GUI.getOpacity();
        int bgColor = RenderUtils.toIntColor(GUI.getBgColor(), opacity);
        context.method_25294(x1, y1, x2, y3, bgColor);
        context.method_25294(x1, y3, x2, y2, this.setting.getColorI(hovering ? 1.0f : opacity));
        int outlineColor = RenderUtils.toIntColor(GUI.getAcColor(), 0.5f);
        RenderUtils.drawBorder2D(context, x1, y3, x2, y2, outlineColor);
        String name = this.setting.getName();
        String value = ColorUtils.toHex(this.setting.getColor());
        int valueWidth = TR.method_1727(value);
        int txtColor = GUI.getTxtColor();
        context.field_59826.method_71067();
        context.method_51433(TR, name, x1, y1 + 2, txtColor, false);
        context.method_51433(TR, value, x2 - valueWidth, y1 + 2, txtColor, false);
    }

    private String getColorTooltip() {
        return WText.literal("\u00a7cR:\u00a7r" + this.setting.getRed()).append(WText.literal(" \u00a7aG:\u00a7r" + this.setting.getGreen())).append(WText.literal(" \u00a79B:\u00a7r" + this.setting.getBlue())).append(WText.literal("\n\n")).append(WText.translated("gui.wurst.generic.left_click_to_edit", new Object[0])).append(WText.literal("\n")).append(WText.translated("gui.wurst.generic.right_click_to_reset", new Object[0])).toString();
    }

    @Override
    public int getDefaultWidth() {
        return TR.method_1727(this.setting.getName() + "#FFFFFF") + 6;
    }

    @Override
    public int getDefaultHeight() {
        return 22;
    }
}
