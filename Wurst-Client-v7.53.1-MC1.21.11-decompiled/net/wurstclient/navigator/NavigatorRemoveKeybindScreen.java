package net.wurstclient.navigator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_364;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.ClickGui;
import net.wurstclient.keybinds.PossibleKeybind;
import net.wurstclient.navigator.NavigatorFeatureScreen;
import net.wurstclient.navigator.NavigatorScreen;

public class NavigatorRemoveKeybindScreen
extends NavigatorScreen {
    private NavigatorFeatureScreen parent;
    private TreeMap<String, PossibleKeybind> existingKeybinds;
    private String hoveredKey = "";
    private String selectedKey = "";
    private String text = "Select the keybind you want to remove.";
    private class_4185 removeButton;

    public NavigatorRemoveKeybindScreen(TreeMap<String, PossibleKeybind> existingKeybinds, NavigatorFeatureScreen parent) {
        this.existingKeybinds = existingKeybinds;
        this.parent = parent;
    }

    @Override
    protected void onResize() {
        this.removeButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Remove"), b -> this.remove()).method_46434(this.field_22789 / 2 - 151, this.field_22790 - 65, 149, 18).method_46431();
        this.removeButton.field_22763 = !this.selectedKey.isEmpty();
        this.method_37063((class_364)this.removeButton);
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Cancel"), b -> this.field_22787.method_1507((class_437)this.parent)).method_46434(this.field_22789 / 2 + 2, this.field_22790 - 65, 149, 18).method_46431());
    }

    private void remove() {
        String oldCommands = WurstClient.INSTANCE.getKeybinds().getCommands(this.selectedKey);
        if (oldCommands == null) {
            return;
        }
        ArrayList<String> commandsList = new ArrayList<String>(Arrays.asList(oldCommands.replace(";", "\u00a7").replace("\u00a7\u00a7", ";").split("\u00a7")));
        for (int i = 0; i < commandsList.size(); ++i) {
            commandsList.set(i, commandsList.get(i).trim());
        }
        String command = this.existingKeybinds.get(this.selectedKey).getCommand();
        while (commandsList.contains(command)) {
            commandsList.remove(command);
        }
        if (commandsList.isEmpty()) {
            WurstClient.INSTANCE.getKeybinds().remove(this.selectedKey);
        } else {
            String newCommands = String.join((CharSequence)"\u00a7", commandsList).replace(";", "\u00a7\u00a7").replace("\u00a7", ";");
            WurstClient.INSTANCE.getKeybinds().add(this.selectedKey, newCommands);
        }
        WurstClient.INSTANCE.getNavigator().addPreference(this.parent.getFeature().getName());
        this.field_22787.method_1507((class_437)this.parent);
    }

    @Override
    protected void onKeyPress(class_11908 context) {
        if (context.comp_4795() == 256 || context.comp_4795() == 259) {
            this.field_22787.method_1507((class_437)this.parent);
        }
    }

    @Override
    protected void onMouseClick(class_11909 context) {
        int button = context.method_74245();
        if (button == 3) {
            WurstClient.MC.method_1507((class_437)this.parent);
            return;
        }
        if (!this.hoveredKey.isEmpty()) {
            this.selectedKey = this.hoveredKey;
            this.removeButton.field_22763 = true;
        }
    }

    @Override
    protected void onUpdate() {
        this.setContentHeight(this.existingKeybinds.size() * 24 - 10);
    }

    @Override
    protected void onRender(class_332 context, int mouseX, int mouseY, float partialTicks) {
        ClickGui gui = WurstClient.INSTANCE.getGui();
        class_327 tr = this.field_22787.field_1772;
        int txtColor = gui.getTxtColor();
        context.method_25300(tr, "Remove Keybind", this.middleX, 32, txtColor);
        int bgx1 = this.middleX - 154;
        int bgx2 = this.middleX + 154;
        int bgy1 = 60;
        int bgy2 = this.field_22790 - 43;
        boolean noButtons = Screens.getButtons((class_437)this).isEmpty();
        int bgy3 = bgy2 - (noButtons ? 0 : 24);
        context.method_44379(bgx1, bgy1, bgx2, bgy3);
        this.hoveredKey = "";
        int yi = bgy1 - 12 + this.scroll;
        for (Map.Entry<String, PossibleKeybind> entry : this.existingKeybinds.entrySet()) {
            int buttonColor;
            String key = entry.getKey();
            PossibleKeybind keybind = entry.getValue();
            int x1 = bgx1 + 2;
            int x2 = bgx2 - 2;
            int y1 = yi += 24;
            int y2 = y1 + 20;
            if (mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2) {
                this.hoveredKey = key;
                buttonColor = key.equals(this.selectedKey) ? 0x6000FF00 : 0x60404040;
            } else {
                buttonColor = key.equals(this.selectedKey) ? 0x4000FF00 : 0x40404040;
            }
            this.drawBox(context, x1, y1, x2, y2, buttonColor);
            context.field_59826.method_71067();
            context.method_25303(tr, key.replace("key.keyboard.", "") + ": " + keybind.getDescription(), x1 + 1, y1 + 1, txtColor);
            String string = keybind.getCommand();
            Objects.requireNonNull(tr);
            context.method_25303(tr, string, x1 + 1, y1 + 1 + 9, txtColor);
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
            int x1 = button.method_46426();
            int x2 = x1 + button.method_25368();
            int y1 = button.method_46427();
            int y2 = y1 + 18;
            int buttonColor = !button.field_22763 ? 0x40000000 : (mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2 ? 0x40606060 : 0x40404040);
            this.drawBox(context, x1, y1, x2, y2, buttonColor);
            String buttonText = button.method_25369().getString();
            context.field_59826.method_71067();
            context.method_25300(tr, buttonText, (x1 + x2) / 2, y1 + 5, txtColor);
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
