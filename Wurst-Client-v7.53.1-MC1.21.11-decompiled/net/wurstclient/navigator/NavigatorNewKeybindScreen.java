package net.wurstclient.navigator;

import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_364;
import net.minecraft.class_3675;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.ClickGui;
import net.wurstclient.keybinds.Keybind;
import net.wurstclient.keybinds.PossibleKeybind;
import net.wurstclient.navigator.NavigatorFeatureScreen;
import net.wurstclient.navigator.NavigatorScreen;

public class NavigatorNewKeybindScreen
extends NavigatorScreen {
    private Set<PossibleKeybind> possibleKeybinds;
    private NavigatorFeatureScreen parent;
    private PossibleKeybind hoveredCommand;
    private PossibleKeybind selectedCommand;
    private String selectedKey = "key.keyboard.unknown";
    private String text = "";
    private class_4185 okButton;
    private class_4185 cancelButton;
    private boolean choosingKey;

    public NavigatorNewKeybindScreen(Set<PossibleKeybind> possibleKeybinds, NavigatorFeatureScreen parent) {
        this.possibleKeybinds = possibleKeybinds;
        this.parent = parent;
    }

    @Override
    protected void onResize() {
        this.okButton = new class_4185(this, this.field_22789 / 2 - 151, this.field_22790 - 65, 149, 18, (class_2561)class_2561.method_43470((String)"OK"), b -> {
            if (this.choosingKey) {
                Object newCommands = this.selectedCommand.getCommand();
                String oldCommands = WurstClient.INSTANCE.getKeybinds().getCommands(this.selectedKey);
                if (oldCommands != null) {
                    newCommands = oldCommands + " ; " + (String)newCommands;
                }
                WurstClient.INSTANCE.getKeybinds().add(this.selectedKey, (String)newCommands);
                WurstClient.INSTANCE.getNavigator().addPreference(this.parent.getFeature().getName());
                this.field_22787.method_1507((class_437)this.parent);
            } else {
                this.choosingKey = true;
                this.okButton.field_22763 = false;
            }
        }, Supplier::get){

            public boolean method_25404(class_11908 context) {
                return false;
            }

            protected void method_75752(class_332 drawContext, int i, int j, float f) {
                this.method_75794(drawContext);
                this.method_75793(drawContext.method_75787((class_339)this, class_332.class_12228.field_63850));
            }
        };
        this.okButton.field_22763 = this.selectedCommand != null;
        this.method_37063((class_364)this.okButton);
        this.cancelButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Cancel"), b -> WurstClient.MC.method_1507((class_437)this.parent)).method_46434(this.field_22789 / 2 + 2, this.field_22790 - 65, 149, 18).method_46431();
        this.method_37063((class_364)this.cancelButton);
    }

    @Override
    protected void onKeyPress(class_11908 context) {
        if (this.choosingKey) {
            this.selectedKey = class_3675.method_15985((class_11908)context).method_1441();
            this.okButton.field_22763 = !this.selectedKey.equals("key.keyboard.unknown");
        } else if (context.comp_4795() == 256 || context.comp_4795() == 259) {
            this.field_22787.method_1507((class_437)this.parent);
        }
    }

    @Override
    protected void onMouseClick(class_11909 context) {
        int mouseX = (int)context.comp_4798();
        int mouseY = (int)context.comp_4799();
        int button = context.method_74245();
        if (this.choosingKey && !this.okButton.method_48202().method_58137(mouseX, mouseY) && !this.cancelButton.method_25405((double)mouseX, (double)mouseY)) {
            this.selectedKey = class_3675.class_307.field_1672.method_1447(button).method_1441();
            this.okButton.field_22763 = !this.selectedKey.equals("key.keyboard.unknown");
            return;
        }
        if (button == 3) {
            this.field_22787.method_1507((class_437)this.parent);
            return;
        }
        if (this.hoveredCommand != null) {
            this.selectedCommand = this.hoveredCommand;
            this.okButton.field_22763 = true;
        }
    }

    @Override
    protected void onUpdate() {
        if (this.choosingKey) {
            this.text = "Now press the key or mouse button that should trigger this keybind.";
            if (!this.selectedKey.equals("key.keyboard.unknown")) {
                this.text = this.text + "\n\nKey: " + Keybind.getDisplayKey(this.selectedKey);
                String commands = WurstClient.INSTANCE.getKeybinds().getCommands(this.selectedKey);
                if (commands != null) {
                    this.text = this.text + "\n\nWARNING: This key is already bound to the following\ncommand(s):";
                    commands = commands.replace(";", "\u00a7").replace("\u00a7\u00a7", ";");
                    for (String cmd : commands.split("\u00a7")) {
                        this.text = this.text + "\n- " + cmd;
                    }
                }
            }
        } else {
            this.text = "Select what this keybind should do.";
        }
        if (this.choosingKey) {
            this.setContentHeight(this.getStringHeight(this.text));
        } else {
            this.setContentHeight(this.possibleKeybinds.size() * 24 - 10);
        }
    }

    @Override
    protected void onRender(class_332 context, int mouseX, int mouseY, float partialTicks) {
        int buttonColor;
        int y2;
        int y1;
        int x1;
        ClickGui gui = WurstClient.INSTANCE.getGui();
        class_327 tr = this.field_22787.field_1772;
        int txtColor = gui.getTxtColor();
        context.method_25300(tr, "New Keybind", this.middleX, 32, txtColor);
        int bgx1 = this.middleX - 154;
        int bgx2 = this.middleX + 154;
        int bgy1 = 60;
        int bgy2 = this.field_22790 - 43;
        boolean noButtons = Screens.getButtons((class_437)this).isEmpty();
        int bgy3 = bgy2 - (noButtons ? 0 : 24);
        context.method_44379(bgx1, bgy1, bgx2, bgy3);
        if (!this.choosingKey) {
            this.hoveredCommand = null;
            int yi = bgy1 - 12 + this.scroll;
            for (PossibleKeybind pkb : this.possibleKeybinds) {
                x1 = bgx1 + 2;
                int x2 = bgx2 - 2;
                y1 = yi += 24;
                y2 = y1 + 20;
                if (mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2 && mouseY <= bgy2 - 24) {
                    this.hoveredCommand = pkb;
                    buttonColor = pkb == this.selectedCommand ? 0x6000FF00 : 0x60404040;
                } else {
                    buttonColor = pkb == this.selectedCommand ? 0x4000FF00 : 0x40404040;
                }
                this.drawBox(context, x1, y1, x2, y2, buttonColor);
                context.field_59826.method_71067();
                context.method_25303(tr, pkb.getDescription(), x1 + 1, y1 + 1, txtColor);
                String string = pkb.getCommand();
                Objects.requireNonNull(tr);
                context.method_25303(tr, string, x1 + 1, y1 + 1 + 9, txtColor);
            }
        }
        int textY = bgy1 + this.scroll + 2;
        context.field_59826.method_71067();
        for (String line : this.text.split("\n")) {
            context.method_25303(tr, line, bgx1 + 2, textY, txtColor);
            Objects.requireNonNull(tr);
            textY += 9;
        }
        context.method_44380();
        for (class_339 button : Screens.getButtons((class_437)this)) {
            x1 = button.method_46426();
            int x2 = x1 + button.method_25368();
            y1 = button.method_46427();
            y2 = y1 + 18;
            buttonColor = !button.field_22763 ? 0x40000000 : (mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2 ? 0x40606060 : 0x40404040);
            this.drawBox(context, x1, y1, x2, y2, buttonColor);
            context.field_59826.method_71067();
            context.method_25300(tr, button.method_25369().getString(), (x1 + x2) / 2, y1 + 5, txtColor);
        }
    }

    @Override
    protected void onMouseDrag(double mouseX, double mouseY, int button, double double_3, double double_4) {
    }

    @Override
    protected void onMouseRelease(double x, double y, int button) {
    }

    public boolean method_25422() {
        return false;
    }
}
