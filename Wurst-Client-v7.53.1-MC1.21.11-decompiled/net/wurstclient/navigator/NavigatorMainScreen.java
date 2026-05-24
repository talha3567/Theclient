package net.wurstclient.navigator;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_364;
import net.minecraft.class_437;
import net.wurstclient.Feature;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.ClickGui;
import net.wurstclient.clickgui.ClickGuiIcons;
import net.wurstclient.hacks.TooManyHaxHack;
import net.wurstclient.navigator.Navigator;
import net.wurstclient.navigator.NavigatorFeatureScreen;
import net.wurstclient.navigator.NavigatorScreen;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.RenderUtils;

public final class NavigatorMainScreen
extends NavigatorScreen {
    private static final ArrayList<Feature> navigatorDisplayList = new ArrayList();
    private class_342 searchBar;
    private String lastSearchText = "";
    private String tooltip;
    private int hoveredFeature = -1;
    private int selectedFeature = 0;
    private boolean mouseMoved = false;
    private boolean hoveringArrow;
    private int clickTimer = -1;
    private boolean expanding = false;
    private Feature expandingFeature;

    public NavigatorMainScreen() {
        this.hasBackground = false;
        this.nonScrollableArea = 0;
        Navigator navigator = WurstClient.INSTANCE.getNavigator();
        navigator.copyNavigatorList(navigatorDisplayList);
    }

    @Override
    protected void onResize() {
        ClickGui gui = WurstClient.INSTANCE.getGui();
        int txtColor = gui.getTxtColor();
        class_327 tr = WurstClient.MC.field_1772;
        this.searchBar = new class_342(tr, 0, 32, 200, 20, (class_2561)class_2561.method_43470((String)""));
        this.searchBar.method_1868(txtColor);
        this.searchBar.method_1858(false);
        this.searchBar.method_1880(128);
        this.method_25429((class_364)this.searchBar);
        this.method_25395((class_364)this.searchBar);
        this.searchBar.method_25365(true);
        this.searchBar.method_46421(this.middleX - 100);
        this.setContentHeight(navigatorDisplayList.size() / 3 * 20);
    }

    @Override
    protected void onKeyPress(class_11908 context) {
        int keyCode = context.comp_4795();
        boolean hasShiftDown = context.method_74239();
        if (keyCode == 257) {
            this.leftClick(this.selectedFeature);
        }
        if (keyCode == 32) {
            this.expand(this.selectedFeature);
        }
        if (keyCode == 262 || keyCode == 258 && !hasShiftDown) {
            if (this.selectedFeature + 1 < navigatorDisplayList.size()) {
                ++this.selectedFeature;
            }
        } else if (keyCode == 263 || keyCode == 258 && hasShiftDown) {
            if (this.selectedFeature - 1 > -1) {
                --this.selectedFeature;
            }
        } else if (keyCode == 264) {
            if (this.selectedFeature + 3 < navigatorDisplayList.size()) {
                this.selectedFeature += 3;
            }
        } else if (keyCode == 265 && this.selectedFeature - 3 > -1) {
            this.selectedFeature -= 3;
        }
    }

    @Override
    protected void onMouseClick(class_11909 context) {
        int button = context.method_74245();
        boolean hasShiftDown = context.method_74239();
        if (this.clickTimer != -1) {
            return;
        }
        if (button == 3) {
            WurstClient.MC.method_1507((class_437)null);
            return;
        }
        if (this.hoveredFeature == -1) {
            return;
        }
        if (button == 0 && (hasShiftDown || this.hoveringArrow) || button == 2) {
            this.expand(this.hoveredFeature);
            return;
        }
        if (button == 0) {
            this.leftClick(this.hoveredFeature);
        }
    }

    private void expand(int i) {
        if (i < 0 || i >= navigatorDisplayList.size()) {
            return;
        }
        this.expandingFeature = navigatorDisplayList.get(i);
        this.expanding = true;
    }

    private void leftClick(int i) {
        if (i < 0 || i >= navigatorDisplayList.size()) {
            return;
        }
        Feature feature = navigatorDisplayList.get(i);
        if (feature.getPrimaryAction().isEmpty()) {
            this.expanding = true;
            this.expandingFeature = feature;
            return;
        }
        WurstClient wurst = WurstClient.INSTANCE;
        TooManyHaxHack tooManyHax = wurst.getHax().tooManyHaxHack;
        if (tooManyHax.isEnabled() && tooManyHax.isBlocked(feature)) {
            ChatUtils.error(feature.getName() + " is blocked by TooManyHax.");
            return;
        }
        feature.doPrimaryAction();
        wurst.getNavigator().addPreference(feature.getName());
    }

    @Override
    protected void onUpdate() {
        String newText = this.searchBar.method_1882();
        if (this.clickTimer == -1 && !newText.equals(this.lastSearchText)) {
            Navigator navigator = WurstClient.INSTANCE.getNavigator();
            if (newText.isEmpty()) {
                navigator.copyNavigatorList(navigatorDisplayList);
            } else {
                newText = newText.toLowerCase().trim();
                navigator.getSearchResults(navigatorDisplayList, newText);
            }
            this.setContentHeight(navigatorDisplayList.size() / 3 * 20);
            this.lastSearchText = newText;
            this.selectedFeature = 0;
        }
        if (this.expanding) {
            if (this.clickTimer < 4) {
                ++this.clickTimer;
            } else {
                WurstClient.MC.method_1507((class_437)new NavigatorFeatureScreen(this.expandingFeature, this));
            }
        } else if (!this.expanding && this.clickTimer > -1) {
            --this.clickTimer;
        }
        this.scrollbarLocked = this.clickTimer != -1;
    }

    public void method_16014(double mouseX, double mouseY) {
        this.mouseMoved = true;
    }

    @Override
    protected void onRender(class_332 context, int mouseX, int mouseY, float partialTicks) {
        ClickGui gui = WurstClient.INSTANCE.getGui();
        int txtColor = gui.getTxtColor();
        boolean clickTimerRunning = this.clickTimer != -1;
        this.tooltip = null;
        if (!clickTimerRunning) {
            context.method_25303(WurstClient.MC.field_1772, "Search: ", this.middleX - 150, 32, txtColor);
            this.searchBar.method_25394(context, mouseX, mouseY, partialTicks);
        }
        int listX = this.middleX - 154;
        if (!clickTimerRunning) {
            this.hoveredFeature = -1;
        }
        context.method_44379(0, 59, this.field_22789, this.field_22790 - 42);
        for (int i = Math.max(-this.scroll * 3 / 20 - 3, 0); i < navigatorDisplayList.size(); ++i) {
            int featureX = listX + 104 * (i % 3);
            int featureY = 60 + i / 3 * 20 + this.scroll;
            if (featureY < 40) continue;
            if (featureY > this.field_22790 - 40) break;
            this.renderFeature(context, mouseX, mouseY, partialTicks, i, featureX, featureY);
        }
        context.method_44380();
        if (this.tooltip != null) {
            context.field_59826.method_71067();
            String[] lines = this.tooltip.split("\n");
            class_327 tr = this.field_22787.field_1772;
            int tw = 0;
            int n = lines.length;
            Objects.requireNonNull(tr);
            int th = n * 9;
            for (String line : lines) {
                int lw = tr.method_1727(line);
                if (lw <= tw) continue;
                tw = lw;
            }
            int sw = this.field_22787.field_1755.field_22789;
            int sh = this.field_22787.field_1755.field_22790;
            int xt1 = mouseX + tw + 11 <= sw ? mouseX + 8 : mouseX - tw - 8;
            int xt2 = xt1 + tw + 3;
            int yt1 = mouseY + th - 2 <= sh ? mouseY - 4 : mouseY - th - 4;
            int yt2 = yt1 + th + 2;
            int bgColor = RenderUtils.toIntColor(gui.getBgColor(), gui.getTooltipOpacity());
            context.method_25294(xt1, yt1, xt2, yt2, bgColor);
            int acColor = RenderUtils.toIntColor(gui.getAcColor(), 0.5f);
            RenderUtils.drawBorder2D(context, xt1, yt1, xt2, yt2, acColor);
            context.field_59826.method_71067();
            for (int i = 0; i < lines.length; ++i) {
                String string = lines[i];
                Objects.requireNonNull(tr);
                context.method_51433(tr, string, xt1 + 2, yt1 + 2 + i * 9, txtColor, false);
            }
        }
    }

    private void renderFeature(class_332 context, int mouseX, int mouseY, float partialTicks, int i, int x, int y) {
        float[] fArray;
        boolean renderAsHovered;
        ClickGui gui = WurstClient.INSTANCE.getGui();
        boolean clickTimerRunning = this.clickTimer != -1;
        Feature feature = navigatorDisplayList.get(i);
        Rectangle area = new Rectangle(x, y, 100, 16);
        if (clickTimerRunning) {
            if (feature != this.expandingFeature) {
                return;
            }
            float factor = this.expanding ? (this.clickTimer == 4 ? 1.0f : ((float)this.clickTimer + partialTicks) / 4.0f) : (this.clickTimer == 0 ? 0.0f : ((float)this.clickTimer - partialTicks) / 4.0f);
            float antiFactor = 1.0f - factor;
            area.x = (int)((float)area.x * antiFactor + (float)(this.middleX - 154) * factor);
            area.y = (int)((float)area.y * antiFactor + 60.0f * factor);
            area.width = (int)((float)area.width * antiFactor + 308.0f * factor);
            area.height = (int)((float)area.height * antiFactor + (float)(this.field_22790 - 103) * factor);
            this.drawBackgroundBox(context, area.x, area.y, area.x + area.width, area.y + area.height);
            return;
        }
        boolean hovering = area.contains(mouseX, mouseY);
        if (hovering) {
            this.hoveredFeature = i;
            if (this.mouseMoved) {
                this.selectedFeature = i;
                this.mouseMoved = false;
            }
        }
        boolean bl = renderAsHovered = hovering || this.selectedFeature == i;
        if (hovering) {
            this.tooltip = feature.getWrappedDescription(200);
        }
        if (feature.isEnabled()) {
            float[] fArray2 = new float[3];
            fArray2[0] = 0.0f;
            fArray2[1] = 1.0f;
            fArray = fArray2;
            fArray2[2] = 0.0f;
        } else {
            fArray = gui.getBgColor();
        }
        int featColor = RenderUtils.toIntColor(fArray, gui.getOpacity() * (renderAsHovered ? 1.5f : 1.0f));
        this.drawBox(context, area.x, area.y, area.x + area.width, area.y + area.height, featColor);
        int bx1 = area.x + area.width - area.height;
        int by1 = area.y + 2;
        int by2 = by1 + area.height - 4;
        int sepColor = RenderUtils.toIntColor(gui.getAcColor(), 0.5f);
        RenderUtils.drawLine2D(context, bx1, by1, bx1, by2, sepColor);
        if (hovering) {
            this.hoveringArrow = mouseX >= bx1;
        }
        context.field_59826.method_71067();
        ClickGuiIcons.drawMinimizeArrow(context, bx1 + 2, (float)area.y + 2.5f, area.x + area.width - 2, area.y + area.height - 3, hovering, true);
        if (!clickTimerRunning) {
            class_327 tr = this.field_22787.field_1772;
            String buttonText = feature.getName();
            int bx = area.x + 4;
            int by = area.y + 4;
            int txtColor = gui.getTxtColor();
            context.method_51433(tr, buttonText, bx, by, txtColor, false);
        }
    }

    public void setExpanding(boolean expanding) {
        this.expanding = expanding;
    }

    @Override
    protected void onMouseDrag(double mouseX, double mouseY, int button, double double_3, double double_4) {
    }

    @Override
    protected void onMouseRelease(double x, double y, int button) {
    }
}
