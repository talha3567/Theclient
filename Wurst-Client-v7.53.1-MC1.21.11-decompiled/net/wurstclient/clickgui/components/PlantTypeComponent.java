package net.wurstclient.clickgui.components;

import net.minecraft.class_11909;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.wurstclient.clickgui.ClickGui;
import net.wurstclient.clickgui.ClickGuiIcons;
import net.wurstclient.clickgui.Component;
import net.wurstclient.settings.PlantTypeSetting;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.text.WText;

public final class PlantTypeComponent
extends Component {
    private static final ClickGui GUI = WURST.getGui();
    private static final class_327 TR = PlantTypeComponent.MC.field_1772;
    private static final int BOX_SIZE = 11;
    private static final int ICON_SIZE = 24;
    private static final String HARVEST = "Harvest";
    private static final String REPLANT = "Replant";
    private final PlantTypeSetting setting;

    public PlantTypeComponent(PlantTypeSetting setting) {
        this.setting = setting;
        this.setWidth(this.getDefaultWidth());
        this.setHeight(this.getDefaultHeight());
    }

    @Override
    public void handleMouseClick(double mouseX, double mouseY, int mouseButton, class_11909 context) {
        if (mouseX < (double)(this.getX() + 24)) {
            return;
        }
        if (mouseY < (double)(this.getY() + this.getHeight() - 11)) {
            return;
        }
        boolean hHarvest = mouseX < (double)(this.getX() + 24 + 11 + TR.method_1727(HARVEST) + 4);
        switch (mouseButton) {
            case 0: {
                if (hHarvest) {
                    this.setting.toggleHarvestingEnabled();
                    break;
                }
                this.setting.toggleReplantingEnabled();
                break;
            }
            case 1: {
                if (hHarvest) {
                    this.setting.resetHarvestingEnabled();
                    break;
                }
                this.setting.resetReplantingEnabled();
            }
        }
    }

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float partialTicks) {
        boolean hReplant;
        int harvestWidth = TR.method_1727(HARVEST);
        int x1 = this.getX();
        int x2 = x1 + this.getWidth();
        int x3 = x1 + 24;
        int x4 = x3 + 11;
        int x5 = x4 + harvestWidth + 4;
        int x6 = x5 + 11;
        int y1 = this.getY();
        int y2 = y1 + this.getHeight();
        int y3 = y2 - 11;
        boolean hovering = this.isHovering(mouseX, mouseY);
        boolean hIcon = hovering && mouseX < x3;
        boolean hName = hovering && mouseX >= x3 && mouseY < y3;
        boolean hHarvest = hovering && mouseX >= x3 && mouseX < x5 && mouseY >= y3;
        boolean bl = hReplant = hovering && mouseX >= x5 && mouseY >= y3;
        if (hIcon) {
            GUI.setTooltip(this.setting.getIcon().method_7964().getString());
        } else if (hName) {
            GUI.setTooltip(this.setting.getWrappedDescription(200));
        } else if (hHarvest) {
            GUI.setTooltip(String.valueOf(WText.translated("gui.wurst.autofarm.harvest", new Object[0])));
        } else if (hReplant) {
            GUI.setTooltip(String.valueOf(WText.translated("gui.wurst.autofarm.replant", new Object[0])));
        }
        int bgColor = this.getFillColor(false);
        context.method_25294(x1, y1, x2, y3, bgColor);
        context.method_25294(x1, y3, x3, y2, bgColor);
        context.method_25294(x4, y3, x5, y2, bgColor);
        context.method_25294(x6, y3, x2, y2, bgColor);
        RenderUtils.drawItem(context, this.setting.getIcon(), x1, y1, true);
        context.method_25294(x3, y3, x4, y2, this.getFillColor(hHarvest));
        context.method_25294(x5, y3, x6, y2, this.getFillColor(hReplant));
        int outlineColor = RenderUtils.toIntColor(GUI.getAcColor(), 0.5f);
        RenderUtils.drawBorder2D(context, x3, y3, x4, y2, outlineColor);
        RenderUtils.drawBorder2D(context, x5, y3, x6, y2, outlineColor);
        if (this.setting.isHarvestingEnabled()) {
            ClickGuiIcons.drawCheck(context, x3, y3, x4, y2, hHarvest, false);
        }
        if (this.setting.isReplantingEnabled()) {
            ClickGuiIcons.drawCheck(context, x5, y3, x6, y2, hReplant, false);
        }
        String name = this.setting.getName();
        context.method_51433(TR, name, x3 + 2, y1 + 3, GUI.getTxtColor(), false);
        context.method_51433(TR, HARVEST, x4 + 2, y3 + 2, GUI.getTxtColor(), false);
        context.method_51433(TR, REPLANT, x6 + 2, y3 + 2, GUI.getTxtColor(), false);
    }

    private int getFillColor(boolean hovering) {
        float opacity = GUI.getOpacity() * (hovering ? 1.5f : 1.0f);
        return RenderUtils.toIntColor(GUI.getBgColor(), opacity);
    }

    @Override
    public int getDefaultWidth() {
        int nameWidth = TR.method_1727(this.setting.getName());
        int boxesWidth = 22 + TR.method_1727(HARVEST) + TR.method_1727(REPLANT) + 6;
        return 24 + Math.max(nameWidth, boxesWidth);
    }

    @Override
    public int getDefaultHeight() {
        return 24;
    }
}
