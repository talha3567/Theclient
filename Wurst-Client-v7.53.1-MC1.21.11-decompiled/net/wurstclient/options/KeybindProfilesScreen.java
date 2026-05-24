package net.wurstclient.options;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.minecraft.class_11908;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_350;
import net.minecraft.class_364;
import net.minecraft.class_4068;
import net.minecraft.class_4185;
import net.minecraft.class_4280;
import net.minecraft.class_437;
import net.wurstclient.WurstClient;
import net.wurstclient.options.EnterProfileNameScreen;
import net.wurstclient.util.json.JsonException;

public final class KeybindProfilesScreen
extends class_437 {
    private final class_437 prevScreen;
    private ListGui listGui;
    private class_4185 loadButton;

    public KeybindProfilesScreen(class_437 prevScreen) {
        super((class_2561)class_2561.method_43470((String)""));
        this.prevScreen = prevScreen;
    }

    public void method_25426() {
        this.listGui = new ListGui(this.field_22787, this, WurstClient.INSTANCE.getKeybinds().listProfiles());
        this.method_25429((class_364)this.listGui);
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Open Folder"), b -> this.openFolder()).method_46434(8, 8, 100, 20).method_46431());
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"New Profile"), b -> this.field_22787.method_1507((class_437)new EnterProfileNameScreen(this, this::newProfile))).method_46434(this.field_22789 / 2 - 154, this.field_22790 - 48, 100, 20).method_46431());
        this.loadButton = (class_4185)this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Load"), b -> this.loadSelected()).method_46434(this.field_22789 / 2 - 50, this.field_22790 - 48, 100, 20).method_46431());
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Cancel"), b -> this.field_22787.method_1507(this.prevScreen)).method_46434(this.field_22789 / 2 + 54, this.field_22790 - 48, 100, 20).method_46431());
    }

    private void openFolder() {
        class_156.method_668().method_672(WurstClient.INSTANCE.getKeybinds().getProfilesFolder().toFile());
    }

    private void newProfile(String name) {
        if (!((String)name).endsWith(".json")) {
            name = (String)name + ".json";
        }
        try {
            WurstClient.INSTANCE.getKeybinds().saveProfile((String)name);
        }
        catch (IOException | JsonException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadSelected() {
        Path path = this.listGui.getSelectedPath();
        if (path == null) {
            this.field_22787.method_1507(this.prevScreen);
            return;
        }
        try {
            String fileName = String.valueOf(path.getFileName());
            WurstClient.INSTANCE.getKeybinds().loadProfile(fileName);
            this.field_22787.method_1507(this.prevScreen);
        }
        catch (IOException | JsonException e) {
            e.printStackTrace();
            return;
        }
    }

    public boolean method_25404(class_11908 context) {
        if (context.comp_4795() == 257) {
            this.loadSelected();
        } else if (context.comp_4795() == 256) {
            this.field_22787.method_1507(this.prevScreen);
        }
        return super.method_25404(context);
    }

    public void method_25393() {
        this.loadButton.field_22763 = this.listGui.method_25334() != null;
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float partialTicks) {
        this.listGui.method_25394(context, mouseX, mouseY, partialTicks);
        context.method_25300(this.field_22787.field_1772, "Keybind Profiles", this.field_22789 / 2, 12, -1);
        for (class_4068 drawable : this.field_33816) {
            drawable.method_25394(context, mouseX, mouseY, partialTicks);
        }
        if (this.loadButton.method_25367() && !this.loadButton.field_22763) {
            context.method_51434(this.field_22793, Arrays.asList(class_2561.method_43470((String)"You must first select a file.")), mouseX, mouseY);
        }
    }

    public boolean method_25422() {
        return false;
    }

    private final class ListGui
    extends class_4280<Entry> {
        public ListGui(class_310 mc, KeybindProfilesScreen screen, List<Path> list) {
            super(mc, screen.field_22789, screen.field_22790 - 96, 36, 20);
            list.stream().map(x$0 -> new Entry((Path)x$0)).forEach(x$0 -> this.method_25321((class_350.class_351)x$0));
        }

        public Path getSelectedPath() {
            Entry selected = (Entry)this.method_25334();
            return selected != null ? selected.path : null;
        }
    }

    private final class Entry
    extends class_4280.class_4281<Entry> {
        private final Path path;

        public Entry(Path path) {
            this.path = Objects.requireNonNull(path);
        }

        public class_2561 method_37006() {
            return class_2561.method_43469((String)"narrator.select", (Object[])new Object[]{"Profile " + String.valueOf(this.path.getFileName())});
        }

        public void method_25343(class_332 context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int x = this.method_73380();
            int y = this.method_73382();
            class_327 tr = ((KeybindProfilesScreen)KeybindProfilesScreen.this).field_22787.field_1772;
            String fileName = String.valueOf(this.path.getFileName());
            context.method_25303(tr, fileName, x + 28, y, -986896);
            String relPath = String.valueOf(((KeybindProfilesScreen)KeybindProfilesScreen.this).field_22787.field_1697.toPath().relativize(this.path));
            context.method_25303(tr, relPath, x + 28, y + 9, -6250336);
        }
    }
}
