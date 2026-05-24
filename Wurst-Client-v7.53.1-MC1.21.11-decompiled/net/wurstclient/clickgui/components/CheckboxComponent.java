package net.wurstclient.clickgui.components;

import net.minecraft.class_11909;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.wurstclient.clickgui.ClickGui;
import net.wurstclient.clickgui.ClickGuiIcons;
import net.wurstclient.clickgui.Component;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.util.RenderUtils;

public final class CheckboxComponent
extends Component {
    private static final ClickGui GUI = WURST.getGui();
    private static final class_327 TR = CheckboxComponent.MC.field_1772;
    private static final int BOX_SIZE = 11;
    private final CheckboxSetting setting;

    public CheckboxComponent(CheckboxSetting setting) {
        this.setting = setting;
        this.setWidth(this.getDefaultWidth());
        this.setHeight(this.getDefaultHeight());
    }

    @Override
    public void handleMouseClick(double mouseX, double mouseY, int mouseButton, class_11909 context) {
        switch (mouseButton) {
            case 0: {
                this.setting.setChecked(!this.setting.isChecked());
                break;
            }
            case 1: {
                this.setting.setChecked(this.setting.isCheckedByDefault());
            }
        }
    }

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float partialTicks) {
        boolean hText;
        int x1 = this.getX();
        int x2 = x1 + this.getWidth();
        int x3 = x1 + 11;
        int y1 = this.getY();
        int y2 = y1 + this.getHeight();
        boolean hovering = this.isHovering(mouseX, mouseY);
        boolean bl = hText = hovering && mouseX >= x3;
        if (hText) {
            GUI.setTooltip(this.getTooltip());
        }
        if (this.setting.isLocked()) {
            hovering = false;
        }
        context.method_25294(x3, y1, x2, y2, this.getFillColor(false));
        context.method_25294(x1, y1, x3, y2, this.getFillColor(hovering));
        int outlineColor = RenderUtils.toIntColor(GUI.getAcColor(), 0.5f);
        RenderUtils.drawBorder2D(context, x1, y1, x3, y2, outlineColor);
        context.field_59826.method_71067();
        if (this.setting.isChecked()) {
            ClickGuiIcons.drawCheck(context, x1, y1, x3, y2, hovering, this.setting.isLocked());
        }
        String name = this.setting.getName();
        context.method_51433(TR, name, x3 + 2, y1 + 2, GUI.getTxtColor(), false);
    }

    private int getFillColor(boolean hovering) {
        float opacity = GUI.getOpacity() * (hovering ? 1.5f : 1.0f);
        return RenderUtils.toIntColor(GUI.getBgColor(), opacity);
    }

    private String getTooltip() {
        Object tooltip = this.setting.getWrappedDescription(200);
        if (this.setting.isLocked()) {
            tooltip = (String)tooltip + "\n\nThis checkbox is locked to ";
            tooltip = (String)tooltip + this.setting.isChecked() + ".";
        }
        return tooltip;
    }

    @Override
    public int getDefaultWidth() {
        return 11 + TR.method_1727(this.setting.getName()) + 2;
    }

    @Override
    public int getDefaultHeight() {
        return 11;
    }
}
