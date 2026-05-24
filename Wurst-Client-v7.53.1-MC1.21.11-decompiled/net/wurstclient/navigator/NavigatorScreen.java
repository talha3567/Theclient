package net.wurstclient.navigator;

import java.awt.Rectangle;
import java.util.Objects;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.ClickGui;
import net.wurstclient.util.RenderUtils;

public abstract class NavigatorScreen
extends class_437 {
    protected int scroll = 0;
    private int scrollKnobPosition = 2;
    private boolean scrolling;
    private int maxScroll;
    protected boolean scrollbarLocked;
    protected int middleX;
    protected boolean hasBackground = true;
    protected int nonScrollableArea = 26;
    private boolean showScrollbar;

    public NavigatorScreen() {
        super((class_2561)class_2561.method_43470((String)""));
    }

    protected final void method_25426() {
        this.middleX = this.field_22789 / 2;
        this.onResize();
    }

    public final boolean method_25404(class_11908 context) {
        this.onKeyPress(context);
        return super.method_25404(context);
    }

    public final boolean method_25402(class_11909 context, boolean doubleClick) {
        if (new Rectangle(this.field_22789 / 2 + 170, 60, 12, this.field_22790 - 103).contains(context.comp_4798(), context.comp_4799())) {
            this.scrolling = true;
        }
        this.onMouseClick(context);
        return super.method_25402(context, doubleClick);
    }

    public final boolean method_25403(class_11909 context, double double_3, double double_4) {
        if (this.scrolling && !this.scrollbarLocked && context.method_74245() == 0) {
            this.scroll = this.maxScroll == 0 ? 0 : (int)((context.comp_4799() - 72.0) * (double)this.maxScroll / (double)(this.field_22790 - 131));
            if (this.scroll > 0) {
                this.scroll = 0;
            } else if (this.scroll < this.maxScroll) {
                this.scroll = this.maxScroll;
            }
            this.scrollKnobPosition = this.maxScroll == 0 ? 0 : (int)((float)((this.field_22790 - 131) * this.scroll) / (float)this.maxScroll);
            this.scrollKnobPosition += 2;
        }
        this.onMouseDrag(context.comp_4798(), context.comp_4799(), context.method_74245(), double_3, double_4);
        return super.method_25403(context, double_3, double_4);
    }

    public final boolean method_25406(class_11909 context) {
        this.scrolling = false;
        this.onMouseRelease(context.comp_4798(), context.comp_4799(), context.method_74245());
        return super.method_25406(context);
    }

    public final boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!this.scrollbarLocked) {
            this.scroll = (int)((double)this.scroll + verticalAmount * 4.0);
            if (this.scroll > 0) {
                this.scroll = 0;
            } else if (this.scroll < this.maxScroll) {
                this.scroll = this.maxScroll;
            }
            this.scrollKnobPosition = this.maxScroll == 0 ? 0 : (int)((float)((this.field_22790 - 131) * this.scroll) / (float)this.maxScroll);
            this.scrollKnobPosition += 2;
        }
        return super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public final void method_25393() {
        this.onUpdate();
    }

    public final void method_25394(class_332 context, int mouseX, int mouseY, float partialTicks) {
        int bgx1 = this.middleX - 154;
        int bgx2 = this.middleX + 154;
        int bgy1 = 60;
        int bgy2 = this.field_22790 - 43;
        if (this.hasBackground) {
            this.drawBackgroundBox(context, bgx1, bgy1, bgx2, bgy2);
        }
        if (this.showScrollbar) {
            int x1 = bgx2 + 16;
            int x2 = x1 + 12;
            int y1 = bgy1;
            int y2 = bgy2;
            this.drawBackgroundBox(context, x1, y1, x2, y2);
            y2 = (y1 += this.scrollKnobPosition) + 24;
            this.drawBackgroundBox(context, x1 += 2, y1, x2 -= 2, y2);
            ++x1;
            --x2;
            y1 += 8;
            y2 -= 15;
            for (int i = 0; i < 3; ++i) {
                this.drawDownShadow(context, x1, y1, x2, y2);
                y1 += 4;
                y2 += 4;
            }
        }
        this.onRender(context, mouseX, mouseY, partialTicks);
    }

    public void method_25420(class_332 context, int mouseX, int mouseY, float deltaTicks) {
    }

    public final boolean method_25421() {
        return false;
    }

    protected abstract void onResize();

    protected abstract void onKeyPress(class_11908 var1);

    protected abstract void onMouseClick(class_11909 var1);

    protected abstract void onMouseDrag(double var1, double var3, int var5, double var6, double var8);

    protected abstract void onMouseRelease(double var1, double var3, int var5);

    protected abstract void onUpdate();

    protected abstract void onRender(class_332 var1, int var2, int var3, float var4);

    protected final int getStringHeight(String s) {
        int fontHeight;
        Objects.requireNonNull(this.field_22787.field_1772);
        int height = fontHeight = 9;
        for (int i = 0; i < s.length(); ++i) {
            if (s.charAt(i) != '\n') continue;
            height += fontHeight;
        }
        return height;
    }

    protected final void setContentHeight(int contentHeight) {
        this.maxScroll = this.field_22790 - contentHeight - this.nonScrollableArea - 120;
        if (this.maxScroll > 0) {
            this.maxScroll = 0;
        }
        boolean bl = this.showScrollbar = this.maxScroll != 0;
        if (this.scroll < this.maxScroll) {
            this.scroll = this.maxScroll;
        }
    }

    protected final void drawDownShadow(class_332 context, int x1, int y1, int x2, int y2) {
        float[] acColor = WurstClient.INSTANCE.getGui().getAcColor();
        int lineColor = RenderUtils.toIntColor(acColor, 0.5f);
        RenderUtils.drawLine2D(context, (float)x1 + 0.1f, y1, (float)x2 + 0.1f, y1, lineColor);
        int shadowColor1 = RenderUtils.toIntColor(acColor, 0.75f);
        int shadowColor2 = 0;
        context.method_25296(x1, y1, x2, y2, shadowColor1, shadowColor2);
    }

    protected final void drawBox(class_332 context, int x1, int y1, int x2, int y2, int color) {
        context.method_25294(x1, y1, x2, y2, color);
        RenderUtils.drawBoxShadow2D(context, x1, y1, x2, y2);
    }

    protected final int getBackgroundColor() {
        ClickGui gui = WurstClient.INSTANCE.getGui();
        gui.updateColors();
        return RenderUtils.toIntColor(gui.getBgColor(), gui.getOpacity());
    }

    protected final void drawBackgroundBox(class_332 context, int x1, int y1, int x2, int y2) {
        this.drawBox(context, x1, y1, x2, y2, this.getBackgroundColor());
    }
}
