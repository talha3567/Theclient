package net.wurstclient.clickgui.components;

import net.minecraft.class_11909;
import net.minecraft.class_1799;
import net.minecraft.class_1935;
import net.minecraft.class_2248;
import net.minecraft.class_2680;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.wurstclient.clickgui.ClickGui;
import net.wurstclient.clickgui.Component;
import net.wurstclient.clickgui.Window;
import net.wurstclient.clickgui.screens.EditBlockScreen;
import net.wurstclient.settings.BlockSetting;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.text.WText;

public final class BlockComponent
extends Component {
    private static final ClickGui GUI = WURST.getGui();
    private static final class_327 TR = BlockComponent.MC.field_1772;
    private static final int BLOCK_WIDTH = 24;
    private final BlockSetting setting;

    public BlockComponent(BlockSetting setting) {
        this.setting = setting;
        this.setWidth(this.getDefaultWidth());
        this.setHeight(this.getDefaultHeight());
    }

    @Override
    public void handleMouseClick(double mouseX, double mouseY, int mouseButton, class_11909 context) {
        if (mouseX < (double)(this.getX() + this.getWidth() - 24)) {
            return;
        }
        switch (mouseButton) {
            case 0: {
                MC.method_1507((class_437)new EditBlockScreen(BlockComponent.MC.field_1755, this.setting));
                break;
            }
            case 1: {
                this.setting.resetToDefault();
            }
        }
    }

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float partialTicks) {
        boolean hBlock;
        int y2;
        int x1 = this.getX();
        int x2 = x1 + this.getWidth();
        int x3 = x2 - 24;
        int y1 = this.getY();
        boolean hovering = this.isHovering(mouseX, mouseY, x1, y1, x2, y2 = y1 + this.getHeight());
        boolean hText = hovering && mouseX < x3;
        boolean bl = hBlock = hovering && mouseX >= x3;
        if (hText) {
            GUI.setTooltip(this.setting.getWrappedDescription(200));
        } else if (hBlock) {
            GUI.setTooltip(this.getBlockTooltip());
        }
        int bgColor = RenderUtils.toIntColor(GUI.getBgColor(), GUI.getOpacity());
        context.method_25294(x1, y1, x2, y2, bgColor);
        context.field_59826.method_71067();
        String name = this.setting.getName() + ":";
        context.method_51433(TR, name, x1, y1 + 2, GUI.getTxtColor(), false);
        class_1799 stack = new class_1799((class_1935)this.setting.getBlock());
        RenderUtils.drawItem(context, stack, x3, y1, true);
    }

    private boolean isHovering(int mouseX, int mouseY, int x1, int y1, int x2, int y2) {
        Window parent = this.getParent();
        boolean scrollEnabled = parent.isScrollingEnabled();
        int scroll = scrollEnabled ? parent.getScrollOffset() : 0;
        return mouseX >= x1 && mouseY >= y1 && mouseX < x2 && mouseY < y2 && mouseY >= -scroll && mouseY < parent.getHeight() - 13 - scroll;
    }

    private String getBlockTooltip() {
        class_2248 block = this.setting.getBlock();
        class_2680 state = block.method_9564();
        class_1799 stack = new class_1799((class_1935)block);
        String translatedName = stack.method_7960() ? WText.translated("gui.wurst.generic.unknown_block", new Object[0]).toString() : stack.method_7964().getString();
        String tooltip = "\u00a76Name:\u00a7r " + translatedName;
        String blockId = this.setting.getBlockName();
        tooltip = tooltip + "\n\u00a76ID:\u00a7r " + blockId;
        int blockNumber = class_2248.method_9507((class_2680)state);
        tooltip = tooltip + "\n\u00a76Block #:\u00a7r " + blockNumber;
        tooltip = tooltip + "\n\n" + String.valueOf(WText.translated("gui.wurst.generic.left_click_to_edit", new Object[0]));
        tooltip = tooltip + "\n" + String.valueOf(WText.translated("gui.wurst.generic.right_click_to_reset", new Object[0]));
        return tooltip;
    }

    @Override
    public int getDefaultWidth() {
        return TR.method_1727(this.setting.getName() + ":") + 24 + 4;
    }

    @Override
    public int getDefaultHeight() {
        return 24;
    }
}
