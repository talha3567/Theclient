package net.wurstclient.options;

import java.util.Objects;
import net.minecraft.class_11907;
import net.minecraft.class_11908;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_350;
import net.minecraft.class_364;
import net.minecraft.class_4068;
import net.minecraft.class_410;
import net.minecraft.class_4185;
import net.minecraft.class_4280;
import net.minecraft.class_437;
import net.wurstclient.WurstClient;
import net.wurstclient.keybinds.Keybind;
import net.wurstclient.keybinds.KeybindList;
import net.wurstclient.options.KeybindEditorScreen;
import net.wurstclient.options.KeybindProfilesScreen;

public final class KeybindManagerScreen
extends class_437 {
    private final class_437 prevScreen;
    private ListGui listGui;
    private class_4185 addButton;
    private class_4185 editButton;
    private class_4185 removeButton;
    private class_4185 backButton;

    public KeybindManagerScreen(class_437 prevScreen) {
        super((class_2561)class_2561.method_43470((String)""));
        this.prevScreen = prevScreen;
    }

    public void method_25426() {
        this.listGui = new ListGui(this.field_22787, this);
        this.method_25429((class_364)this.listGui);
        this.addButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Add"), b -> this.field_22787.method_1507((class_437)new KeybindEditorScreen(this))).method_46434(this.field_22789 / 2 - 102, this.field_22790 - 52, 100, 20).method_46431();
        this.method_37063((class_364)this.addButton);
        this.editButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Edit"), b -> this.edit()).method_46434(this.field_22789 / 2 + 2, this.field_22790 - 52, 100, 20).method_46431();
        this.method_37063((class_364)this.editButton);
        this.removeButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Remove"), b -> this.remove()).method_46434(this.field_22789 / 2 - 102, this.field_22790 - 28, 100, 20).method_46431();
        this.method_37063((class_364)this.removeButton);
        this.backButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Back"), b -> this.field_22787.method_1507(this.prevScreen)).method_46434(this.field_22789 / 2 + 2, this.field_22790 - 28, 100, 20).method_46431();
        this.method_37063((class_364)this.backButton);
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Reset Keybinds"), b -> this.field_22787.method_1507((class_437)new class_410(confirmed -> {
            if (confirmed) {
                WurstClient.INSTANCE.getKeybinds().setKeybinds(KeybindList.DEFAULT_KEYBINDS);
            }
            this.field_22787.method_1507((class_437)this);
        }, (class_2561)class_2561.method_43470((String)"Are you sure you want to reset your keybinds?"), (class_2561)class_2561.method_43470((String)"This cannot be undone!")))).method_46434(8, 8, 100, 20).method_46431());
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Profiles..."), b -> this.field_22787.method_1507((class_437)new KeybindProfilesScreen(this))).method_46434(this.field_22789 - 108, 8, 100, 20).method_46431());
    }

    private void edit() {
        Keybind keybind = this.listGui.getSelectedKeybind();
        if (keybind == null) {
            return;
        }
        this.field_22787.method_1507((class_437)new KeybindEditorScreen(this, keybind.getKey(), keybind.getCommands()));
    }

    private void remove() {
        Keybind keybind = this.listGui.getSelectedKeybind();
        if (keybind == null) {
            return;
        }
        WurstClient.INSTANCE.getKeybinds().remove(keybind.getKey());
        this.field_22787.method_1507((class_437)this);
    }

    public boolean method_25404(class_11908 context) {
        switch (context.comp_4795()) {
            case 257: {
                if (this.editButton.field_22763) {
                    this.editButton.method_25306((class_11907)context);
                    break;
                }
                this.addButton.method_25306((class_11907)context);
                break;
            }
            case 261: {
                this.removeButton.method_25306((class_11907)context);
                break;
            }
            case 256: {
                this.backButton.method_25306((class_11907)context);
                break;
            }
        }
        return super.method_25404(context);
    }

    public void method_25393() {
        boolean selected;
        this.editButton.field_22763 = selected = this.listGui.method_25334() != null;
        this.removeButton.field_22763 = selected;
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float partialTicks) {
        this.listGui.method_25394(context, mouseX, mouseY, partialTicks);
        context.method_25300(this.field_22793, "Keybind Manager", this.field_22789 / 2, 8, -1);
        int count = WurstClient.INSTANCE.getKeybinds().getAllKeybinds().size();
        context.method_25300(this.field_22793, "Keybinds: " + count, this.field_22789 / 2, 20, -1);
        for (class_4068 drawable : this.field_33816) {
            drawable.method_25394(context, mouseX, mouseY, partialTicks);
        }
    }

    public boolean method_25422() {
        return false;
    }

    private final class ListGui
    extends class_4280<Entry> {
        public ListGui(class_310 mc, KeybindManagerScreen screen) {
            super(mc, screen.field_22789, screen.field_22790 - 96, 36, 30);
            WurstClient.INSTANCE.getKeybinds().getAllKeybinds().stream().map(x$0 -> new Entry((Keybind)x$0)).forEach(x$0 -> this.method_25321((class_350.class_351)x$0));
        }

        public Keybind getSelectedKeybind() {
            Entry selected = (Entry)this.method_25334();
            return selected != null ? selected.keybind : null;
        }
    }

    private final class Entry
    extends class_4280.class_4281<Entry> {
        private final Keybind keybind;

        public Entry(Keybind keybind) {
            this.keybind = Objects.requireNonNull(keybind);
        }

        public class_2561 method_37006() {
            return class_2561.method_43469((String)"narrator.select", (Object[])new Object[]{"Keybind " + String.valueOf(this.keybind)});
        }

        public void method_25343(class_332 context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int x = this.method_73380();
            int y = this.method_73382();
            class_327 tr = ((KeybindManagerScreen)KeybindManagerScreen.this).field_22787.field_1772;
            String keyText = "Key: " + Keybind.getDisplayKey(this.keybind.getKey());
            context.method_51433(tr, keyText, x + 3, y + 3, -986896, false);
            String cmdText = "Commands: " + this.keybind.getCommands();
            context.method_51433(tr, cmdText, x + 3, y + 15, -6250336, false);
        }
    }
}
