package net.wurstclient.clickgui.components;

import java.util.Objects;
import net.minecraft.class_11909;
import net.minecraft.class_2583;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.wurstclient.clickgui.ClickGui;
import net.wurstclient.clickgui.Component;
import net.wurstclient.clickgui.screens.EditTextFieldScreen;
import net.wurstclient.settings.TextFieldSetting;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.RenderUtils;

public final class TextFieldEditButton
extends Component {
    private static final ClickGui GUI = WURST.getGui();
    private static final class_327 TR = TextFieldEditButton.MC.field_1772;
    private static final int TEXT_HEIGHT = 11;
    private final TextFieldSetting setting;

    public TextFieldEditButton(TextFieldSetting setting) {
        this.setting = Objects.requireNonNull(setting);
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
                MC.method_1507((class_437)new EditTextFieldScreen(TextFieldEditButton.MC.field_1755, this.setting));
                break;
            }
            case 1: {
                this.setting.resetToDefault();
            }
        }
    }

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float partialTicks) {
        boolean hBox;
        float[] bgColor = GUI.getBgColor();
        float opacity = GUI.getOpacity();
        int x1 = this.getX();
        int x2 = x1 + this.getWidth();
        int y1 = this.getY();
        int y2 = y1 + this.getHeight();
        int y3 = y1 + 11;
        boolean hovering = this.isHovering(mouseX, mouseY);
        boolean hText = hovering && mouseY < y3;
        boolean bl = hBox = hovering && mouseY >= y3;
        if (hText) {
            GUI.setTooltip(ChatUtils.wrapText(this.setting.getDescription(), 200));
        } else if (hBox) {
            GUI.setTooltip(ChatUtils.wrapText(this.setting.getValue(), 200));
        }
        context.method_25294(x1, y1, x2, y3, RenderUtils.toIntColor(bgColor, opacity));
        context.method_25294(x1, y3, x2, y2, RenderUtils.toIntColor(bgColor, opacity * (hBox ? 1.5f : 1.0f)));
        RenderUtils.drawBorder2D(context, x1, y3, x2, y2, RenderUtils.toIntColor(GUI.getAcColor(), 0.5f));
        int txtColor = GUI.getTxtColor();
        context.field_59826.method_71067();
        context.method_51433(TR, this.setting.getName(), x1, y1 + 2, txtColor, false);
        Object value = this.setting.getValue();
        int maxWidth = this.getWidth() - TR.method_1727("...") - 2;
        int maxLength = TR.method_27527().method_27484((String)value, maxWidth, class_2583.field_24360);
        if (maxLength < ((String)value).length()) {
            value = ((String)value).substring(0, maxLength) + "...";
        }
        context.method_51433(TR, (String)value, x1 + 2, y3 + 2, txtColor, false);
    }

    @Override
    public int getDefaultWidth() {
        return TR.method_1727(this.setting.getName()) + 4;
    }

    @Override
    public int getDefaultHeight() {
        return 22;
    }
}
