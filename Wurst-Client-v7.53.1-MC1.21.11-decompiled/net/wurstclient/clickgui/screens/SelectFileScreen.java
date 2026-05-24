package net.wurstclient.clickgui.screens;

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
import net.minecraft.class_410;
import net.minecraft.class_4185;
import net.minecraft.class_4280;
import net.minecraft.class_437;
import net.minecraft.class_5250;
import net.wurstclient.settings.FileSetting;

public final class SelectFileScreen
extends class_437 {
    private final class_437 prevScreen;
    private final FileSetting setting;
    private ListGui listGui;
    private class_4185 doneButton;

    public SelectFileScreen(class_437 prevScreen, FileSetting blockList) {
        super((class_2561)class_2561.method_43470((String)""));
        this.prevScreen = prevScreen;
        this.setting = blockList;
    }

    public void method_25426() {
        this.listGui = new ListGui(this.field_22787, this, this.setting.listFiles());
        this.method_25429((class_364)this.listGui);
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Open Folder"), b -> this.openFolder()).method_46434(8, 8, 100, 20).method_46431());
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Reset to Defaults"), b -> this.askToConfirmReset()).method_46434(this.field_22789 - 108, 8, 100, 20).method_46431());
        this.doneButton = (class_4185)this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Done"), b -> this.done()).method_46434(this.field_22789 / 2 - 102, this.field_22790 - 48, 100, 20).method_46431());
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Cancel"), b -> this.openPrevScreen()).method_46434(this.field_22789 / 2 + 2, this.field_22790 - 48, 100, 20).method_46431());
    }

    private void openFolder() {
        class_156.method_668().method_672(this.setting.getFolder().toFile());
    }

    private void openPrevScreen() {
        this.field_22787.method_1507(this.prevScreen);
    }

    private void done() {
        Path path = this.listGui.getSelectedPath();
        if (path != null) {
            String fileName = String.valueOf(path.getFileName());
            this.setting.setSelectedFile(fileName);
        }
        this.openPrevScreen();
    }

    private void askToConfirmReset() {
        class_5250 title = class_2561.method_43470((String)"Reset Folder");
        class_5250 message = class_2561.method_43470((String)("This will empty the '" + String.valueOf(this.setting.getFolder().getFileName()) + "' folder and then re-generate the default files.\nAre you sure you want to do this?"));
        this.field_22787.method_1507((class_437)new class_410(this::confirmReset, (class_2561)title, (class_2561)message));
    }

    private void confirmReset(boolean confirmed) {
        if (confirmed) {
            this.setting.resetFolder();
        }
        this.field_22787.method_1507((class_437)this);
    }

    public boolean method_25404(class_11908 context) {
        if (context.comp_4795() == 257) {
            this.done();
        } else if (context.comp_4795() == 256) {
            this.openPrevScreen();
        }
        return super.method_25404(context);
    }

    public void method_25393() {
        this.doneButton.field_22763 = this.listGui.method_25334() != null;
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float partialTicks) {
        this.listGui.method_25394(context, mouseX, mouseY, partialTicks);
        context.method_25300(this.field_22787.field_1772, this.setting.getName(), this.field_22789 / 2, 12, -1);
        for (class_4068 drawable : this.field_33816) {
            drawable.method_25394(context, mouseX, mouseY, partialTicks);
        }
        if (this.doneButton.method_25367() && !this.doneButton.field_22763) {
            context.method_51434(this.field_22793, Arrays.asList(class_2561.method_43470((String)"You must first select a file.")), mouseX, mouseY);
        }
    }

    public boolean method_25421() {
        return false;
    }

    public boolean method_25422() {
        return false;
    }

    private final class ListGui
    extends class_4280<Entry> {
        public ListGui(class_310 mc, SelectFileScreen screen, List<Path> list) {
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
            return class_2561.method_43469((String)"narrator.select", (Object[])new Object[]{"File " + String.valueOf(this.path.getFileName())});
        }

        public void method_25343(class_332 context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int x = this.method_73380();
            int y = this.method_73382();
            class_327 tr = ((SelectFileScreen)SelectFileScreen.this).field_22787.field_1772;
            String fileName = String.valueOf(this.path.getFileName());
            context.method_25303(tr, fileName, x + 28, y, -986896);
            String relPath = String.valueOf(((SelectFileScreen)SelectFileScreen.this).field_22787.field_1697.toPath().relativize(this.path));
            context.method_25303(tr, relPath, x + 28, y + 9, -6250336);
        }
    }
}
