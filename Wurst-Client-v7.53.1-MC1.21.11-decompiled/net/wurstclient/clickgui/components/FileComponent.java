package net.wurstclient.clickgui.components;

import net.minecraft.class_11909;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.wurstclient.clickgui.ClickGui;
import net.wurstclient.clickgui.Component;
import net.wurstclient.clickgui.screens.SelectFileScreen;
import net.wurstclient.settings.FileSetting;
import net.wurstclient.util.RenderUtils;

public final class FileComponent
extends Component {
    private static final ClickGui GUI = WURST.getGui();
    private static final class_327 TR = FileComponent.MC.field_1772;
    private final FileSetting setting;

    public FileComponent(FileSetting setting) {
        this.setting = setting;
        this.setWidth(this.getDefaultWidth());
        this.setHeight(this.getDefaultHeight());
    }

    @Override
    public void handleMouseClick(double mouseX, double mouseY, int mouseButton, class_11909 context) {
        if (mouseButton != 0) {
            return;
        }
        if (mouseX < (double)(this.getX() + this.getWidth() - this.getButtonWidth() - 4)) {
            return;
        }
        MC.method_1507((class_437)new SelectFileScreen(FileComponent.MC.field_1755, this.setting));
    }

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float partialTicks) {
        boolean hBox;
        int x1 = this.getX();
        int x2 = x1 + this.getWidth();
        int x3 = x2 - this.getButtonWidth() - 4;
        int y1 = this.getY();
        int y2 = y1 + this.getHeight();
        boolean hovering = this.isHovering(mouseX, mouseY);
        boolean hText = hovering && mouseX < x3;
        boolean bl = hBox = hovering && mouseX >= x3;
        if (hText) {
            GUI.setTooltip(this.setting.getWrappedDescription(200));
        } else if (hBox) {
            GUI.setTooltip("\u00a7e[left-click]\u00a7r to select file");
        }
        context.method_25294(x1, y1, x3, y2, this.getFillColor(false));
        context.method_25294(x3, y1, x2, y2, this.getFillColor(hBox));
        int outlineColor = RenderUtils.toIntColor(GUI.getAcColor(), 0.5f);
        RenderUtils.drawBorder2D(context, x3, y1, x2, y2, outlineColor);
        int txtColor = GUI.getTxtColor();
        String labelText = this.setting.getName() + ":";
        String buttonText = this.setting.getSelectedFileName();
        context.field_59826.method_71067();
        context.method_51433(TR, labelText, x1, y1 + 2, txtColor, false);
        context.method_51433(TR, buttonText, x3 + 2, y1 + 2, txtColor, false);
    }

    private int getFillColor(boolean hovering) {
        float opacity = GUI.getOpacity() * (hovering ? 1.5f : 1.0f);
        return RenderUtils.toIntColor(GUI.getBgColor(), opacity);
    }

    private int getButtonWidth() {
        return TR.method_1727(this.setting.getSelectedFileName());
    }

    @Override
    public int getDefaultWidth() {
        String text = this.setting.getName() + ":";
        return TR.method_1727(text) + this.getButtonWidth() + 6;
    }

    @Override
    public int getDefaultHeight() {
        return 11;
    }
}
