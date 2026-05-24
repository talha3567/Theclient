package net.wurstclient.clickgui;

import net.minecraft.class_327;
import net.minecraft.class_332;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.ClickGui;
import net.wurstclient.clickgui.Component;
import net.wurstclient.clickgui.Popup;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.util.RenderUtils;

public final class ComboBoxPopup<T extends Enum<T>>
extends Popup {
    private static final ClickGui GUI = WurstClient.INSTANCE.getGui();
    private static final class_327 TR = WurstClient.MC.field_1772;
    private final EnumSetting<T> setting;
    private final int popupWidth;

    public ComboBoxPopup(Component owner, EnumSetting<T> setting, int popupWidth) {
        super(owner);
        this.setting = setting;
        this.popupWidth = popupWidth;
        this.setWidth(this.getDefaultWidth());
        this.setHeight(this.getDefaultHeight());
        this.setX(owner.getWidth() - this.getWidth());
        this.setY(owner.getHeight());
    }

    @Override
    public void handleMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) {
            return;
        }
        int yi1 = this.getY() - 11;
        for (Enum value : this.setting.getValues()) {
            if (value == this.setting.getSelected()) continue;
            int yi2 = (yi1 += 11) + 11;
            if (mouseY < yi1 || mouseY >= yi2) continue;
            this.setting.setSelected(value);
            this.close();
            break;
        }
    }

    @Override
    public void render(class_332 context, int mouseX, int mouseY) {
        int y2;
        int y1;
        int x2;
        int x1 = this.getX();
        boolean hovering = this.isHovering(mouseX, mouseY, x1, x2 = x1 + this.getWidth(), y1 = this.getY(), y2 = y1 + this.getHeight());
        if (hovering) {
            GUI.setTooltip("");
        }
        RenderUtils.drawBorder2D(context, x1, y1, x2, y2, RenderUtils.toIntColor(GUI.getAcColor(), 0.5f));
        int yi1 = y1 - 11;
        for (Enum value : this.setting.getValues()) {
            if (value == this.setting.getSelected()) continue;
            int yi2 = (yi1 += 11) + 11;
            boolean hValue = hovering && mouseY >= yi1 && mouseY < yi2;
            context.method_25294(x1, yi1, x2, yi2, RenderUtils.toIntColor(GUI.getBgColor(), GUI.getOpacity() * (hValue ? 1.5f : 1.0f)));
            context.field_59826.method_71067();
            context.method_51433(TR, value.toString(), x1 + 2, yi1 + 2, GUI.getTxtColor(), false);
        }
    }

    private boolean isHovering(int mouseX, int mouseY, int x1, int x2, int y1, int y2) {
        return mouseX >= x1 && mouseY >= y1 && mouseX < x2 && mouseY < y2;
    }

    @Override
    public int getDefaultWidth() {
        return this.popupWidth + 15;
    }

    @Override
    public int getDefaultHeight() {
        int numValues = this.setting.getValues().length;
        return (numValues - 1) * 11;
    }
}
