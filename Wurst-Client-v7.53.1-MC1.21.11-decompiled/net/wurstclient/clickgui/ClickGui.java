package net.wurstclient.clickgui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_5348;
import net.wurstclient.Category;
import net.wurstclient.Feature;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.ClickGuiIcons;
import net.wurstclient.clickgui.Component;
import net.wurstclient.clickgui.Popup;
import net.wurstclient.clickgui.SettingsWindow;
import net.wurstclient.clickgui.Window;
import net.wurstclient.clickgui.components.FeatureButton;
import net.wurstclient.hacks.ClickGuiHack;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.json.JsonUtils;
import org.joml.Matrix3x2fStack;

public final class ClickGui {
    private static final WurstClient WURST = WurstClient.INSTANCE;
    private static final class_310 MC = WurstClient.MC;
    private final ArrayList<Window> windows = new ArrayList();
    private final ArrayList<Popup> popups = new ArrayList();
    private final Path windowsFile;
    private float[] bgColor = new float[3];
    private float[] acColor = new float[3];
    private int txtColor;
    private float opacity;
    private float ttOpacity;
    private int maxHeight;
    private int maxSettingsHeight;
    private String tooltip = "";
    private boolean leftMouseButtonPressed;

    public ClickGui(Path windowsFile) {
        this.windowsFile = windowsFile;
    }

    public void init() {
        JsonObject json;
        this.updateColors();
        LinkedHashMap<Category, Window> windowMap = new LinkedHashMap<Category, Window>();
        for (Category category : Category.values()) {
            windowMap.put(category, new Window(category.getName()));
        }
        ArrayList<Feature> features = new ArrayList<Feature>();
        features.addAll(WURST.getHax().getAllHax());
        features.addAll(WURST.getCmds().getAllCmds());
        features.addAll(WURST.getOtfs().getAllOtfs());
        for (Feature f : features) {
            if (f.getCategory() == null) continue;
            ((Window)windowMap.get((Object)f.getCategory())).add(new FeatureButton(f));
        }
        this.windows.addAll(windowMap.values());
        Window uiSettings = new Window("UI Settings");
        uiSettings.add(new FeatureButton(ClickGui.WURST.getOtfs().wurstLogoOtf));
        uiSettings.add(new FeatureButton(ClickGui.WURST.getOtfs().hackListOtf));
        uiSettings.add(new FeatureButton(ClickGui.WURST.getOtfs().keybindManagerOtf));
        uiSettings.add(new FeatureButton(ClickGui.WURST.getOtfs().wurstOptionsOtf));
        ClickGuiHack clickGuiHack = ClickGui.WURST.getHax().clickGuiHack;
        Stream<Setting> settings = clickGuiHack.getSettings().values().stream();
        settings.map(Setting::getComponent).forEach(c -> uiSettings.add((Component)c));
        this.windows.add(uiSettings);
        for (Window window : this.windows) {
            window.setMinimized(true);
        }
        this.windows.add(WurstClient.INSTANCE.getHax().radarHack.getWindow());
        int x = 5;
        int y = 5;
        int scaledWidth = MC.method_22683().method_4486();
        for (Window window : this.windows) {
            window.pack();
            if (x + window.getWidth() + 5 > scaledWidth) {
                x = 5;
                y += 18;
            }
            window.setX(x);
            window.setY(y);
            x += window.getWidth() + 5;
        }
        try (BufferedReader reader = Files.newBufferedReader(this.windowsFile);){
            json = JsonParser.parseReader(reader).getAsJsonObject();
        }
        catch (NoSuchFileException e) {
            this.saveWindows();
            return;
        }
        catch (Exception e) {
            System.out.println("Failed to load " + String.valueOf(this.windowsFile.getFileName()));
            e.printStackTrace();
            this.saveWindows();
            return;
        }
        for (Window window : this.windows) {
            JsonElement jsonPinned;
            JsonElement jsonMinimized;
            JsonElement jsonY;
            JsonElement jsonWindow = json.get(window.getTitle());
            if (jsonWindow == null || !jsonWindow.isJsonObject()) continue;
            JsonElement jsonX = jsonWindow.getAsJsonObject().get("x");
            if (jsonX.isJsonPrimitive() && jsonX.getAsJsonPrimitive().isNumber()) {
                window.setX(jsonX.getAsInt());
            }
            if ((jsonY = jsonWindow.getAsJsonObject().get("y")).isJsonPrimitive() && jsonY.getAsJsonPrimitive().isNumber()) {
                window.setY(jsonY.getAsInt());
            }
            if ((jsonMinimized = jsonWindow.getAsJsonObject().get("minimized")).isJsonPrimitive() && jsonMinimized.getAsJsonPrimitive().isBoolean()) {
                window.setMinimized(jsonMinimized.getAsBoolean());
            }
            if (!(jsonPinned = jsonWindow.getAsJsonObject().get("pinned")).isJsonPrimitive() || !jsonPinned.getAsJsonPrimitive().isBoolean()) continue;
            window.setPinned(jsonPinned.getAsBoolean());
        }
        this.saveWindows();
    }

    private void saveWindows() {
        JsonObject json = new JsonObject();
        for (Window window : this.windows) {
            if (window.isClosable()) continue;
            JsonObject jsonWindow = new JsonObject();
            jsonWindow.addProperty("x", window.getActualX());
            jsonWindow.addProperty("y", window.getActualY());
            jsonWindow.addProperty("minimized", window.isMinimized());
            jsonWindow.addProperty("pinned", window.isPinned());
            json.add(window.getTitle(), jsonWindow);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(this.windowsFile, new OpenOption[0]);){
            JsonUtils.PRETTY_GSON.toJson((JsonElement)json, (Appendable)writer);
        }
        catch (IOException e) {
            System.out.println("Failed to save " + String.valueOf(this.windowsFile.getFileName()));
            e.printStackTrace();
        }
    }

    public void handleMouseClick(class_11909 context) {
        boolean popupClicked;
        int mouseX = (int)context.comp_4798();
        int mouseY = (int)context.comp_4799();
        int mouseButton = context.method_74245();
        if (mouseButton == 0) {
            this.leftMouseButtonPressed = true;
        }
        if (!(popupClicked = this.handlePopupMouseClick(mouseX, mouseY, mouseButton))) {
            this.handleWindowMouseClick(mouseX, mouseY, mouseButton, context);
            this.closeInvalidPopups();
        }
        this.windows.removeIf(Window::isClosing);
    }

    public void handleMouseRelease(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 0) {
            this.leftMouseButtonPressed = false;
        }
    }

    public void handleMouseScroll(double mouseX, double mouseY, double delta) {
        int dWheel = (int)delta * 4;
        if (dWheel == 0) {
            return;
        }
        for (int i = this.windows.size() - 1; i >= 0; --i) {
            Window window = this.windows.get(i);
            if (!window.isScrollingEnabled() || window.isMinimized() || window.isInvisible() || mouseX < (double)window.getX() || mouseY < (double)(window.getY() + 13) || mouseX >= (double)(window.getX() + window.getWidth()) || mouseY >= (double)(window.getY() + window.getHeight())) continue;
            int scroll = window.getScrollOffset() + dWheel;
            scroll = Math.min(scroll, 0);
            scroll = Math.max(scroll, -window.getInnerHeight() + window.getHeight() - 13);
            window.setScrollOffset(scroll);
            this.closeInvalidPopups();
            break;
        }
    }

    public void handleNavigatorMouseClick(double cMouseX, double cMouseY, int mouseButton, Window window, class_11909 context) {
        if (mouseButton == 0) {
            this.leftMouseButtonPressed = true;
        }
        this.handleComponentMouseClick(window, cMouseX, cMouseY, mouseButton, context);
        this.closeInvalidPopups();
    }

    public void closePopupsOutsideArea(Window window, int x1, int y1, int x2, int y2) {
        for (Popup popup : this.popups) {
            Component owner = popup.getOwner();
            if (owner.getParent() != window || this.isComponentVisibleWithinBounds(owner, x1, y1, x2, y2)) continue;
            popup.close();
        }
        this.popups.removeIf(Popup::isClosing);
    }

    public boolean handlePopupMouseClick(double mouseX, double mouseY, int mouseButton) {
        this.closeInvalidPopups();
        for (int i = this.popups.size() - 1; i >= 0; --i) {
            Popup popup = this.popups.get(i);
            Component owner = popup.getOwner();
            Window parent = owner.getParent();
            int x0 = parent.getX() + owner.getX();
            int y0 = parent.getY() + 13 + parent.getScrollOffset() + owner.getY();
            int x1 = x0 + popup.getX();
            int y1 = y0 + popup.getY();
            int x2 = x1 + popup.getWidth();
            int y2 = y1 + popup.getHeight();
            if (mouseX < (double)x1 || mouseY < (double)y1 || mouseX >= (double)x2 || mouseY >= (double)y2) continue;
            int cMouseX = (int)(mouseX - (double)x0);
            int cMouseY = (int)(mouseY - (double)y0);
            popup.handleMouseClick(cMouseX, cMouseY, mouseButton);
            this.popups.remove(i);
            this.popups.add(popup);
            this.closeInvalidPopups();
            return true;
        }
        return false;
    }

    private void closeInvalidPopups() {
        for (Popup popup : this.popups) {
            Window parent = popup.getOwner().getParent();
            if (parent != null && !parent.isClosing() && this.isPopupOwnerVisible(popup)) continue;
            popup.close();
        }
        this.popups.removeIf(Popup::isClosing);
    }

    private boolean isPopupOwnerVisible(Popup popup) {
        Component owner = popup.getOwner();
        Window parent = owner.getParent();
        if (parent == null || parent.isInvisible() || parent.isMinimized()) {
            return false;
        }
        int x1 = parent.getX();
        int y1 = parent.getY() + 13;
        int x2 = x1 + parent.getWidth();
        int y2 = parent.getY() + parent.getHeight();
        return this.isComponentVisibleWithinBounds(owner, x1, y1, x2, y2);
    }

    private boolean isComponentVisibleWithinBounds(Component c, int x1, int y1, int x2, int y2) {
        Window parent = c.getParent();
        int cx1 = parent.getX() + c.getX();
        int cy1 = parent.getY() + 13 + parent.getScrollOffset() + c.getY();
        int cx2 = cx1 + c.getWidth();
        int cy2 = cy1 + c.getHeight();
        return cx2 > x1 && cx1 < x2 && cy2 > y1 && cy1 < y2;
    }

    private void handleWindowMouseClick(int mouseX, int mouseY, int mouseButton, class_11909 context) {
        for (int i = this.windows.size() - 1; i >= 0; --i) {
            Window window = this.windows.get(i);
            if (window.isInvisible()) continue;
            int x1 = window.getX();
            int y1 = window.getY();
            int x2 = x1 + window.getWidth();
            int y2 = y1 + window.getHeight();
            int y3 = y1 + 13;
            if (mouseX < x1 || mouseY < y1 || mouseX >= x2 || mouseY >= y2) continue;
            if (mouseY < y3) {
                this.handleTitleBarMouseClick(window, mouseX, mouseY, mouseButton);
            } else {
                if (window.isMinimized()) continue;
                window.validate();
                int cMouseX = mouseX - x1;
                int cMouseY = mouseY - y3;
                if (window.isScrollingEnabled() && mouseX >= x2 - 3) {
                    this.handleScrollbarMouseClick(window, cMouseX, cMouseY, mouseButton);
                } else {
                    if (window.isScrollingEnabled()) {
                        cMouseY -= window.getScrollOffset();
                    }
                    this.handleComponentMouseClick(window, cMouseX, cMouseY, mouseButton, context);
                }
            }
            this.windows.remove(i);
            this.windows.add(window);
            break;
        }
    }

    private void handleTitleBarMouseClick(Window window, int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) {
            return;
        }
        if (mouseY < window.getY() + 2 || mouseY >= window.getY() + 11) {
            window.startDragging(mouseX, mouseY);
            return;
        }
        int x3 = window.getX() + window.getWidth();
        if (window.isClosable() && mouseX >= (x3 -= 11) && mouseX < x3 + 9) {
            window.close();
            return;
        }
        if (window.isPinnable() && mouseX >= (x3 -= 11) && mouseX < x3 + 9) {
            window.setPinned(!window.isPinned());
            this.saveWindows();
            return;
        }
        if (window.isMinimizable() && mouseX >= (x3 -= 11) && mouseX < x3 + 9) {
            window.setMinimized(!window.isMinimized());
            this.saveWindows();
            return;
        }
        window.startDragging(mouseX, mouseY);
    }

    private void handleScrollbarMouseClick(Window window, int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) {
            return;
        }
        if (mouseX >= window.getWidth() - 1) {
            return;
        }
        double outerHeight = window.getHeight() - 13;
        double innerHeight = window.getInnerHeight();
        double maxScrollbarHeight = outerHeight - 2.0;
        int scrollbarY = (int)(outerHeight * ((double)(-window.getScrollOffset()) / innerHeight) + 1.0);
        int scrollbarHeight = (int)(maxScrollbarHeight * outerHeight / innerHeight);
        if (mouseY < scrollbarY || mouseY >= scrollbarY + scrollbarHeight) {
            return;
        }
        window.startDraggingScrollbar(window.getY() + 13 + mouseY);
    }

    private void handleComponentMouseClick(Window window, double mouseX, double mouseY, int mouseButton, class_11909 context) {
        for (int i2 = window.countChildren() - 1; i2 >= 0; --i2) {
            Component c = window.getChild(i2);
            if (mouseX < (double)c.getX() || mouseY < (double)c.getY() || mouseX >= (double)(c.getX() + c.getWidth()) || mouseY >= (double)(c.getY() + c.getHeight())) continue;
            c.handleMouseClick(mouseX, mouseY, mouseButton, context);
            break;
        }
    }

    public void render(class_332 context, int mouseX, int mouseY, float partialTicks) {
        this.updateColors();
        Matrix3x2fStack matrixStack = context.method_51448();
        matrixStack.pushMatrix();
        this.tooltip = "";
        for (Window window : this.windows) {
            if (window.isInvisible()) continue;
            if (window.isDragging()) {
                if (this.leftMouseButtonPressed) {
                    window.dragTo(mouseX, mouseY);
                } else {
                    window.stopDragging();
                    this.saveWindows();
                }
            }
            if (window.isDraggingScrollbar()) {
                if (this.leftMouseButtonPressed) {
                    window.dragScrollbarTo(mouseY);
                } else {
                    window.stopDraggingScrollbar();
                }
            }
            context.field_59826.method_71067();
            this.renderWindow(context, window, mouseX, mouseY, partialTicks);
        }
        this.renderPopups(context, mouseX, mouseY);
        this.renderTooltip(context, mouseX, mouseY);
        matrixStack.popMatrix();
    }

    public void renderPopups(class_332 context, int mouseX, int mouseY) {
        this.closeInvalidPopups();
        Matrix3x2fStack matrixStack = context.method_51448();
        for (Popup popup : this.popups) {
            Component owner = popup.getOwner();
            Window parent = owner.getParent();
            int x1 = parent.getX() + owner.getX();
            int y1 = parent.getY() + 13 + parent.getScrollOffset() + owner.getY();
            matrixStack.pushMatrix();
            matrixStack.translate((float)x1, (float)y1);
            context.field_59826.method_71067();
            int cMouseX = mouseX - x1;
            int cMouseY = mouseY - y1;
            popup.render(context, cMouseX, cMouseY);
            matrixStack.popMatrix();
        }
    }

    public void renderTooltip(class_332 context, int mouseX, int mouseY) {
        if (this.tooltip.isEmpty()) {
            return;
        }
        String[] lines = this.tooltip.split("\n");
        class_327 tr = ClickGui.MC.field_1772;
        int tw = 0;
        int n = lines.length;
        Objects.requireNonNull(tr);
        int th = n * 9;
        for (String line : lines) {
            int lw = tr.method_1727(line);
            if (lw <= tw) continue;
            tw = lw;
        }
        int sw = ClickGui.MC.field_1755.field_22789;
        int sh = ClickGui.MC.field_1755.field_22790;
        int xt1 = mouseX + tw + 11 <= sw ? mouseX + 8 : mouseX - tw - 8;
        int xt2 = xt1 + tw + 3;
        int yt1 = mouseY + th - 2 <= sh ? mouseY - 4 : mouseY - th - 4;
        int yt2 = yt1 + th + 2;
        context.field_59826.method_71067();
        context.method_25294(xt1, yt1, xt2, yt2, RenderUtils.toIntColor(this.bgColor, this.ttOpacity));
        RenderUtils.drawBorder2D(context, xt1, yt1, xt2, yt2, RenderUtils.toIntColor(this.acColor, 0.5f));
        context.field_59826.method_71067();
        for (int i = 0; i < lines.length; ++i) {
            String string = lines[i];
            Objects.requireNonNull(tr);
            context.method_51433(tr, string, xt1 + 2, yt1 + 2 + i * 9, this.txtColor, false);
        }
    }

    public void renderPinnedWindows(class_332 context, float partialTicks) {
        for (Window window : this.windows) {
            if (!window.isPinned() || window.isInvisible()) continue;
            context.field_59826.method_71067();
            this.renderWindow(context, window, Integer.MIN_VALUE, Integer.MIN_VALUE, partialTicks);
        }
    }

    public void updateColors() {
        ClickGuiHack clickGui = ClickGui.WURST.getHax().clickGuiHack;
        this.opacity = clickGui.getOpacity();
        this.ttOpacity = clickGui.getTooltipOpacity();
        this.bgColor = clickGui.getBackgroundColor();
        this.txtColor = clickGui.getTextColor();
        this.maxHeight = clickGui.getMaxHeight();
        this.maxSettingsHeight = clickGui.getMaxSettingsHeight();
        this.acColor = WurstClient.INSTANCE.getHax().rainbowUiHack.isEnabled() ? RenderUtils.getRainbowColor() : clickGui.getAccentColor();
    }

    private void renderWindow(class_332 context, Window window, int mouseX, int mouseY, float partialTicks) {
        boolean hovering;
        int x4;
        boolean hoveringY;
        int x3;
        int x1 = window.getX();
        int y1 = window.getY();
        int x2 = x1 + window.getWidth();
        int y2 = y1 + window.getHeight();
        int y3 = y1 + 13;
        int windowBgColor = RenderUtils.toIntColor(this.bgColor, this.opacity);
        int outlineColor = RenderUtils.toIntColor(this.acColor, 0.5f);
        Matrix3x2fStack matrixStack = context.method_51448();
        if (window.isMinimized()) {
            y2 = y3;
        }
        if (mouseX >= x1 && mouseY >= y1 && mouseX < x2 && mouseY < y2) {
            this.tooltip = "";
        }
        if (!window.isMinimized()) {
            int yc1;
            window.setMaxHeight(window instanceof SettingsWindow ? this.maxSettingsHeight : this.maxHeight);
            window.validate();
            if (window.isScrollingEnabled()) {
                int xs1 = x2 - 3;
                int xs2 = xs1 + 2;
                int xs3 = x2;
                double outerHeight = y2 - y3;
                double innerHeight = window.getInnerHeight();
                double maxScrollbarHeight = outerHeight - 2.0;
                double scrollbarY = outerHeight * ((double)(-window.getScrollOffset()) / innerHeight) + 1.0;
                double scrollbarHeight = maxScrollbarHeight * outerHeight / innerHeight;
                int ys1 = y3;
                int ys2 = y2;
                int ys3 = ys1 + (int)scrollbarY;
                int ys4 = ys3 + (int)scrollbarHeight;
                context.method_25294(xs2, ys1, xs3, ys2, windowBgColor);
                context.method_25294(xs1, ys1, xs2, ys3, windowBgColor);
                context.method_25294(xs1, ys4, xs2, ys2, windowBgColor);
                boolean hovering2 = mouseX >= xs1 && mouseY >= ys3 && mouseX < xs2 && mouseY < ys4;
                int scrollbarColor = RenderUtils.toIntColor(this.acColor, hovering2 ? this.opacity * 1.5f : this.opacity);
                context.method_25294(xs1, ys3, xs2, ys4, scrollbarColor);
                RenderUtils.drawBorder2D(context, xs1, ys3, xs2, ys4, outlineColor);
            }
            x3 = x1 + 2;
            int x42 = window.isScrollingEnabled() ? x2 - 3 : x2;
            int x5 = x42 - 2;
            int y4 = y3 + window.getScrollOffset();
            context.method_25294(x1, y3, x3, y2, windowBgColor);
            context.method_25294(x5, y3, x42, y2, windowBgColor);
            context.method_44379(x1, y3, x2, y2);
            matrixStack.pushMatrix();
            matrixStack.translate((float)x1, (float)y4);
            int xc1 = 2;
            int xc2 = x5 - x1;
            for (int i = 0; i < window.countChildren(); ++i) {
                int yc12 = window.getChild(i).getY();
                int yc2 = yc12 - 2;
                context.method_25294(xc1, yc2, xc2, yc12, windowBgColor);
            }
            if (window.countChildren() == 0) {
                yc1 = 0;
            } else {
                Component lastChild = window.getChild(window.countChildren() - 1);
                yc1 = lastChild.getY() + lastChild.getHeight();
            }
            int yc2 = yc1 + 2;
            context.method_25294(xc1, yc2, xc2, yc1, windowBgColor);
            int cMouseX = mouseX - x1;
            int cMouseY = mouseY - y4;
            for (int i = 0; i < window.countChildren(); ++i) {
                window.getChild(i).render(context, cMouseX, cMouseY, partialTicks);
            }
            matrixStack.popMatrix();
            context.method_44380();
        }
        RenderUtils.drawBorder2D(context, x1, y1, x2, y2, outlineColor);
        if (!window.isMinimized()) {
            RenderUtils.drawLine2D(context, x1, y3, x2, y3, outlineColor);
        }
        x3 = x2;
        int y4 = y1 + 2;
        int y5 = y3 - 2;
        boolean bl = hoveringY = mouseY >= y4 && mouseY < y5;
        if (window.isClosable()) {
            x4 = (x3 -= 11) + 9;
            hovering = hoveringY && mouseX >= x3 && mouseX < x4;
            this.renderTitleBarButton(context, x3, y4, x4, y5, hovering);
            ClickGuiIcons.drawCross(context, x3, y4, x4, y5, hovering);
        }
        if (window.isPinnable()) {
            x4 = (x3 -= 11) + 9;
            hovering = hoveringY && mouseX >= x3 && mouseX < x4;
            this.renderTitleBarButton(context, x3, y4, x4, y5, hovering);
            ClickGuiIcons.drawPin(context, x3, y4, x4, y5, hovering, window.isPinned());
        }
        if (window.isMinimizable()) {
            x4 = (x3 -= 11) + 9;
            hovering = hoveringY && mouseX >= x3 && mouseX < x4;
            this.renderTitleBarButton(context, x3, y4, x4, y5, hovering);
            ClickGuiIcons.drawMinimizeArrow(context, x3, y4, x4, y5, hovering, window.isMinimized());
        }
        int titleBgColor = RenderUtils.toIntColor(this.acColor, this.opacity);
        context.method_25294(x3, y1, x2, y4, titleBgColor);
        context.method_25294(x3, y5, x2, y3, titleBgColor);
        context.method_25294(x1, y1, x3, y3, titleBgColor);
        class_327 tr = ClickGui.MC.field_1772;
        String title = tr.method_1714((class_5348)class_2561.method_43470((String)window.getTitle()), x3 - x1).getString();
        context.field_59826.method_71067();
        context.method_51433(tr, title, x1 + 2, y1 + 3, this.txtColor, false);
    }

    private void renderTitleBarButton(class_332 context, int x1, int y1, int x2, int y2, boolean hovering) {
        int x3 = x2 + 2;
        int buttonBgColor = RenderUtils.toIntColor(this.bgColor, hovering ? this.opacity * 1.5f : this.opacity);
        context.method_25294(x1, y1, x2, y2, buttonBgColor);
        int windowBgColor = RenderUtils.toIntColor(this.acColor, this.opacity);
        context.method_25294(x2, y1, x3, y2, windowBgColor);
        int outlineColor = RenderUtils.toIntColor(this.acColor, 0.5f);
        RenderUtils.drawBorder2D(context, x1, y1, x2, y2, outlineColor);
    }

    public float[] getBgColor() {
        return this.bgColor;
    }

    public float[] getAcColor() {
        return this.acColor;
    }

    public int getTxtColor() {
        return this.txtColor;
    }

    public float getOpacity() {
        return this.opacity;
    }

    public float getTooltipOpacity() {
        return this.ttOpacity;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = Objects.requireNonNull(tooltip);
    }

    public void addWindow(Window window) {
        this.windows.add(window);
    }

    public void addPopup(Popup popup) {
        this.popups.add(popup);
    }

    public boolean isLeftMouseButtonPressed() {
        return this.leftMouseButtonPressed;
    }
}
