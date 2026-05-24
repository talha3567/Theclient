package net.wurstclient.navigator;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_11910;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_3417;
import net.minecraft.class_3532;
import net.minecraft.class_364;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_6880;
import net.wurstclient.Feature;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.ClickGui;
import net.wurstclient.clickgui.Component;
import net.wurstclient.clickgui.Window;
import net.wurstclient.command.Command;
import net.wurstclient.hack.Hack;
import net.wurstclient.hacks.TooManyHaxHack;
import net.wurstclient.keybinds.Keybind;
import net.wurstclient.keybinds.PossibleKeybind;
import net.wurstclient.navigator.NavigatorMainScreen;
import net.wurstclient.navigator.NavigatorNewKeybindScreen;
import net.wurstclient.navigator.NavigatorRemoveKeybindScreen;
import net.wurstclient.navigator.NavigatorScreen;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.RenderUtils;
import org.joml.Matrix3x2fStack;

public final class NavigatorFeatureScreen
extends NavigatorScreen {
    private Feature feature;
    private NavigatorMainScreen parent;
    private ButtonData activeButton;
    private class_4185 primaryButton;
    private String text;
    private ArrayList<ButtonData> buttonDatas = new ArrayList();
    private Window window = new Window("");
    private int windowComponentY;

    public NavigatorFeatureScreen(Feature feature, NavigatorMainScreen parent) {
        this.feature = feature;
        this.parent = parent;
        this.hasBackground = false;
        for (Setting setting : feature.getSettings().values()) {
            Component c = setting.getComponent();
            if (c == null) continue;
            this.window.add(c);
        }
        this.window.setWidth(308);
        this.window.setFixedWidth(true);
        this.window.setPositionClampingEnabled(false);
        this.window.pack();
    }

    @Override
    protected void onResize() {
        Set<PossibleKeybind> possibleKeybinds;
        String description;
        boolean hasPrimaryAction;
        this.buttonDatas.clear();
        String primaryAction = this.feature.getPrimaryAction();
        boolean bl = hasPrimaryAction = !primaryAction.isEmpty();
        if (hasPrimaryAction) {
            this.primaryButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)primaryAction), b -> {
                TooManyHaxHack tooManyHax = WurstClient.INSTANCE.getHax().tooManyHaxHack;
                if (tooManyHax.isEnabled() && tooManyHax.isBlocked(this.feature)) {
                    ChatUtils.error(this.feature.getName() + " is blocked by TooManyHax.");
                    return;
                }
                this.feature.doPrimaryAction();
                this.primaryButton.method_25355((class_2561)class_2561.method_43470((String)this.feature.getPrimaryAction()));
                WurstClient.INSTANCE.getNavigator().addPreference(this.feature.getName());
            }).method_46434(this.field_22789 / 2 - 151, this.field_22790 - 65, 302, 18).method_46431();
            this.method_37063((class_364)this.primaryButton);
        }
        this.text = "Type: ";
        this.text = this.feature instanceof Hack ? this.text + "Hack" : (this.feature instanceof Command ? this.text + "Command" : this.text + "Other Feature");
        if (this.feature.getCategory() != null) {
            this.text = this.text + ", Category: " + this.feature.getCategory().getName();
        }
        if (!(description = this.feature.getWrappedDescription(300)).isEmpty()) {
            this.text = this.text + "\n\nDescription:\n" + description;
        }
        Rectangle area = new Rectangle(this.middleX - 154, 60, 308, this.field_22790 - 103);
        Collection<Setting> settings = this.feature.getSettings().values();
        if (!settings.isEmpty()) {
            this.text = this.text + "\n\nSettings:";
            this.windowComponentY = this.getStringHeight(this.text) + 2;
            int i = 0;
            while ((double)i < Math.ceil((double)this.window.getInnerHeight() / 9.0)) {
                this.text = this.text + "\n";
                ++i;
            }
        }
        if (!(possibleKeybinds = this.feature.getPossibleKeybinds()).isEmpty()) {
            this.text = this.text + "\n\nKeybinds:";
            ButtonData addKeybindButton = new ButtonData(area.x + area.width - 16, area.y + this.getStringHeight(this.text) - 7, 12, 8, "+", 65280){

                @Override
                public void press() {
                    WurstClient.MC.method_1507((class_437)new NavigatorNewKeybindScreen(possibleKeybinds, NavigatorFeatureScreen.this));
                }
            };
            this.buttonDatas.add(addKeybindButton);
            HashMap<String, String> possibleKeybindsMap = new HashMap<String, String>();
            for (PossibleKeybind possibleKeybind : possibleKeybinds) {
                possibleKeybindsMap.put(possibleKeybind.getCommand(), possibleKeybind.getDescription());
            }
            final TreeMap<String, PossibleKeybind> existingKeybinds = new TreeMap<String, PossibleKeybind>();
            boolean noKeybindsSet = true;
            for (Keybind keybind : WurstClient.INSTANCE.getKeybinds().getAllKeybinds()) {
                String commands = keybind.getCommands();
                commands = commands.replace(";", "\u00a7").replace("\u00a7\u00a7", ";");
                for (String command : commands.split("\u00a7")) {
                    String keybindDescription = (String)possibleKeybindsMap.get(command = command.trim());
                    if (keybindDescription != null) {
                        if (noKeybindsSet) {
                            noKeybindsSet = false;
                        }
                        this.text = this.text + "\n" + keybind.getKey().replace("key.keyboard.", "") + ": " + keybindDescription;
                        existingKeybinds.put(keybind.getKey(), new PossibleKeybind(command, keybindDescription));
                        continue;
                    }
                    if (!(this.feature instanceof Hack) || !command.equalsIgnoreCase(this.feature.getName())) continue;
                    if (noKeybindsSet) {
                        noKeybindsSet = false;
                    }
                    this.text = this.text + "\n" + keybind.getKey().replace("key.keyboard.", "") + ": Toggle " + this.feature.getName();
                    existingKeybinds.put(keybind.getKey(), new PossibleKeybind(command, "Toggle " + this.feature.getName()));
                }
            }
            if (noKeybindsSet) {
                this.text = this.text + "\nNone";
            } else {
                this.buttonDatas.add(new ButtonData(addKeybindButton.x, addKeybindButton.y, addKeybindButton.width, addKeybindButton.height, "-", 0xFF0000){

                    @Override
                    public void press() {
                        NavigatorFeatureScreen.this.field_22787.method_1507((class_437)new NavigatorRemoveKeybindScreen(existingKeybinds, NavigatorFeatureScreen.this));
                    }
                });
                addKeybindButton.x -= 16;
            }
        }
        this.setContentHeight(this.getStringHeight(this.text));
    }

    @Override
    protected void onKeyPress(class_11908 context) {
        int keyCode = context.comp_4795();
        if (keyCode == 256 || keyCode == 259) {
            this.goBack();
        }
    }

    @Override
    protected void onMouseClick(class_11909 context) {
        double x = context.comp_4798();
        double y = context.comp_4799();
        int button = context.method_74245();
        if (WurstClient.INSTANCE.getGui().handlePopupMouseClick(x, y, button)) {
            return;
        }
        if (button == 3) {
            this.goBack();
            return;
        }
        boolean noButtons = Screens.getButtons((class_437)this).isEmpty();
        Rectangle area = new Rectangle(this.field_22789 / 2 - 154, 60, 308, this.field_22790 - 60 - (noButtons ? 43 : 67));
        if (!area.contains(x, y)) {
            return;
        }
        if (this.activeButton != null) {
            this.field_22787.method_1483().method_4873((class_1113)class_1109.method_47978((class_6880)class_3417.field_15015, (float)1.0f));
            this.activeButton.press();
            WurstClient.INSTANCE.getNavigator().addPreference(this.feature.getName());
            return;
        }
        WurstClient.INSTANCE.getGui().handleNavigatorMouseClick(x - (double)this.middleX + 154.0, y - 60.0 - (double)this.scroll - (double)this.windowComponentY, button, this.window, context);
    }

    private void goBack() {
        this.parent.setExpanding(false);
        this.field_22787.method_1507((class_437)this.parent);
    }

    @Override
    protected void onMouseDrag(double mouseX, double mouseY, int button, double double_3, double double_4) {
    }

    @Override
    protected void onMouseRelease(double x, double y, int button) {
        WurstClient.INSTANCE.getGui().handleMouseRelease(x, y, button);
    }

    @Override
    protected void onUpdate() {
        if (this.primaryButton != null) {
            this.primaryButton.method_25355((class_2561)class_2561.method_43470((String)this.feature.getPrimaryAction()));
        }
    }

    @Override
    protected void onRender(class_332 context, int mouseX, int mouseY, float partialTicks) {
        int yc1;
        Matrix3x2fStack matrixStack = context.method_51448();
        ClickGui gui = WurstClient.INSTANCE.getGui();
        int txtColor = gui.getTxtColor();
        context.method_25300(this.field_22787.field_1772, this.feature.getName(), this.middleX, 32, txtColor);
        int bgx1 = this.middleX - 154;
        this.window.setX(bgx1);
        int bgx2 = this.middleX + 154;
        int bgy1 = 60;
        int bgy2 = this.field_22790 - 43;
        boolean noButtons = Screens.getButtons((class_437)this).isEmpty();
        int bgy3 = bgy2 - (noButtons ? 0 : 24);
        int windowY1 = bgy1 + this.scroll + this.windowComponentY;
        int windowY2 = windowY1 + this.window.getInnerHeight();
        context.method_25294(bgx1, bgy1, bgx2, class_3532.method_15340((int)windowY1, (int)bgy1, (int)bgy3), this.getBackgroundColor());
        context.method_25294(bgx1, class_3532.method_15340((int)windowY2, (int)bgy1, (int)bgy3), bgx2, bgy2, this.getBackgroundColor());
        RenderUtils.drawBoxShadow2D(context, bgx1, bgy1, bgx2, bgy2);
        context.method_44379(bgx1, bgy1, bgx2, bgy3);
        gui.setTooltip("");
        this.window.validate();
        this.window.setY(windowY1 - 13);
        matrixStack.pushMatrix();
        matrixStack.translate((float)bgx1, (float)windowY1);
        int x1 = 0;
        int y1 = -13;
        int x2 = x1 + this.window.getWidth();
        int y2 = y1 + this.window.getHeight();
        int y3 = y1 + 13;
        int x3 = x1 + 2;
        int x5 = x2 - 2;
        int bgColor = this.getBackgroundColor();
        context.method_25294(x1, y3, x3, y2, bgColor);
        context.method_25294(x5, y3, x2, y2, bgColor);
        int xc1 = 2;
        int xc2 = x5 - x1;
        for (int i = 0; i < this.window.countChildren(); ++i) {
            int yc12 = this.window.getChild(i).getY();
            int yc2 = yc12 - 2;
            if (yc12 < bgy1 - windowY1) continue;
            if (yc2 > bgy3 - windowY1) break;
            context.method_25294(xc1, yc12, xc2, yc2, bgColor);
        }
        if (this.window.countChildren() == 0) {
            yc1 = 0;
        } else {
            Component lastChild = this.window.getChild(this.window.countChildren() - 1);
            yc1 = lastChild.getY() + lastChild.getHeight();
        }
        int yc2 = yc1 + 2;
        context.method_25294(xc1, yc1, xc2, yc2, bgColor);
        for (int i = 0; i < this.window.countChildren(); ++i) {
            Component child = this.window.getChild(i);
            if (child.getY() + child.getHeight() < bgy1 - windowY1) continue;
            if (child.getY() > bgy3 - windowY1) break;
            child.render(context, mouseX - bgx1, mouseY - windowY1, partialTicks);
        }
        matrixStack.popMatrix();
        this.activeButton = null;
        for (ButtonData buttonData : this.buttonDatas) {
            float alpha;
            int x12 = buttonData.x;
            int x22 = x12 + buttonData.width;
            int y12 = buttonData.y + this.scroll;
            int y22 = y12 + buttonData.height;
            if (buttonData.isLocked()) {
                alpha = 0.25f;
            } else if (mouseX >= x12 && mouseX <= x22 && mouseY >= y12 && mouseY <= y22) {
                alpha = 0.75f;
                this.activeButton = buttonData;
            } else {
                alpha = 0.375f;
            }
            float[] rgb = buttonData.color.getColorComponents(null);
            this.drawBox(context, x12, y12, x22, y22, RenderUtils.toIntColor(rgb, alpha));
            context.field_59826.method_71067();
            context.method_25300(this.field_22787.field_1772, buttonData.buttonText, (x12 + x22) / 2, y12 + (buttonData.height - 10) / 2 + 1, buttonData.isLocked() ? -986896 : buttonData.textColor);
        }
        int textY = bgy1 + this.scroll + 2;
        context.field_59826.method_71067();
        for (String line : this.text.split("\n")) {
            context.method_51433(this.field_22787.field_1772, line, bgx1 + 2, textY, txtColor, false);
            Objects.requireNonNull(this.field_22787.field_1772);
            textY += 9;
        }
        context.method_44380();
        for (class_339 button : Screens.getButtons((class_437)this)) {
            boolean hovering;
            int x13 = button.method_46426();
            int x23 = x13 + button.method_25368();
            int y13 = button.method_46427();
            int y23 = y13 + 18;
            boolean bl = hovering = mouseX >= x13 && mouseX <= x23 && mouseY >= y13 && mouseY <= y23;
            int buttonColor = this.feature.isEnabled() && button == this.primaryButton ? (hovering ? 0x4000FF00 : 0x4000E000) : (hovering ? 0x40606060 : 0x40404040);
            this.drawBox(context, x13, y13, x23, y23, buttonColor);
            String buttonText = button.method_25369().getString();
            context.field_59826.method_71067();
            context.method_51433(this.field_22787.field_1772, buttonText, (x13 + x23 - this.field_22787.field_1772.method_1727(buttonText)) / 2, y13 + 5, txtColor, false);
        }
        gui.closePopupsOutsideArea(this.window, bgx1, bgy1, bgx2, bgy3);
        gui.renderPopups(context, mouseX, mouseY);
        gui.renderTooltip(context, mouseX, mouseY);
    }

    public void method_25419() {
        this.window.close();
        WurstClient.INSTANCE.getGui().handleMouseClick(new class_11909(Double.MIN_VALUE, Double.MIN_VALUE, new class_11910(0, 0)));
    }

    public Feature getFeature() {
        return this.feature;
    }

    public int getMiddleX() {
        return this.middleX;
    }

    public void addText(String text) {
        this.text = this.text + text;
    }

    public int getTextHeight() {
        return this.getStringHeight(this.text);
    }

    public static abstract class ButtonData
    extends Rectangle {
        public String buttonText;
        public Color color;
        public int textColor = -1;

        public ButtonData(int x, int y, int width, int height, String buttonText, int color) {
            super(x, y, width, height);
            this.buttonText = buttonText;
            this.color = new Color(color);
        }

        public abstract void press();

        public boolean isLocked() {
            return false;
        }
    }
}
