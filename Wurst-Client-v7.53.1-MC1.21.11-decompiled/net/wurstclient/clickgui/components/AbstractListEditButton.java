package net.wurstclient.clickgui.components;

import net.minecraft.class_11909;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.wurstclient.clickgui.ClickGui;
import net.wurstclient.clickgui.Component;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.RenderUtils;

public abstract class AbstractListEditButton
extends Component {
    private static final ClickGui GUI = WURST.getGui();
    private static final class_327 TR = AbstractListEditButton.MC.field_1772;
    private final String buttonText = "Edit...";
    private final int buttonWidth = TR.method_1727("Edit...");

    protected abstract void openScreen();

    protected abstract String getText();

    protected abstract Setting getSetting();

    @Override
    public void handleMouseClick(double mouseX, double mouseY, int mouseButton, class_11909 context) {
        if (mouseButton != 0) {
            return;
        }
        if (mouseX < (double)(this.getX() + this.getWidth() - this.buttonWidth - 4)) {
            return;
        }
        this.openScreen();
    }

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float partialTicks) {
        boolean hBox;
        int x1 = this.getX();
        int x2 = x1 + this.getWidth();
        int x3 = x2 - this.buttonWidth - 4;
        int y1 = this.getY();
        int y2 = y1 + this.getHeight();
        boolean hovering = this.isHovering(mouseX, mouseY);
        boolean hText = hovering && mouseX < x3;
        boolean bl = hBox = hovering && mouseX >= x3;
        if (hText) {
            GUI.setTooltip(this.getSetting().getWrappedDescription(200));
        }
        context.method_25294(x1, y1, x3, y2, this.getFillColor(false));
        context.method_25294(x3, y1, x2, y2, this.getFillColor(hBox));
        int outlineColor = RenderUtils.toIntColor(GUI.getAcColor(), 0.5f);
        RenderUtils.drawBorder2D(context, x3, y1, x2, y2, outlineColor);
        int txtColor = GUI.getTxtColor();
        context.field_59826.method_71067();
        context.method_51433(TR, this.getText(), x1, y1 + 2, txtColor, false);
        context.method_51433(TR, "Edit...", x3 + 2, y1 + 2, txtColor, false);
    }

    private int getFillColor(boolean hovering) {
        float opacity = GUI.getOpacity() * (hovering ? 1.5f : 1.0f);
        return RenderUtils.toIntColor(GUI.getBgColor(), opacity);
    }

    @Override
    public int getDefaultWidth() {
        return TR.method_1727(this.getText()) + this.buttonWidth + 6;
    }

    @Override
    public int getDefaultHeight() {
        return 11;
    }
}
