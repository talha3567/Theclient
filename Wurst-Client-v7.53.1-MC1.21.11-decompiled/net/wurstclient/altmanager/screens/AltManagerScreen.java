package net.wurstclient.altmanager.screens;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.class_11907;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_350;
import net.minecraft.class_3532;
import net.minecraft.class_3544;
import net.minecraft.class_364;
import net.minecraft.class_403;
import net.minecraft.class_4068;
import net.minecraft.class_410;
import net.minecraft.class_4185;
import net.minecraft.class_4280;
import net.minecraft.class_437;
import net.minecraft.class_5250;
import net.minecraft.class_5348;
import net.wurstclient.WurstClient;
import net.wurstclient.altmanager.Alt;
import net.wurstclient.altmanager.AltManager;
import net.wurstclient.altmanager.AltRenderer;
import net.wurstclient.altmanager.AltsFile;
import net.wurstclient.altmanager.CrackedAlt;
import net.wurstclient.altmanager.ExportAltsFileChooser;
import net.wurstclient.altmanager.ImportAltsFileChooser;
import net.wurstclient.altmanager.LoginException;
import net.wurstclient.altmanager.MojangAlt;
import net.wurstclient.altmanager.NameGenerator;
import net.wurstclient.altmanager.screens.AddAltScreen;
import net.wurstclient.altmanager.screens.DirectLoginScreen;
import net.wurstclient.altmanager.screens.EditAltScreen;
import net.wurstclient.mixinterface.IMinecraftClient;
import net.wurstclient.util.MultiProcessingUtils;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.json.WsonObject;
import org.jetbrains.annotations.Nullable;

public final class AltManagerScreen
extends class_437 {
    private static final HashSet<Alt> failedLogins = new HashSet();
    private final class_437 prevScreen;
    private final AltManager altManager;
    private ListGui listGui;
    private boolean shouldAsk = true;
    private int errorTimer;
    private class_4185 useButton;
    private class_4185 starButton;
    private class_4185 editButton;
    private class_4185 deleteButton;
    private class_4185 importButton;
    private class_4185 exportButton;
    private class_4185 logoutButton;

    public AltManagerScreen(class_437 prevScreen, AltManager altManager) {
        super((class_2561)class_2561.method_43470((String)"Alt Manager"));
        this.prevScreen = prevScreen;
        this.altManager = altManager;
    }

    public void method_25426() {
        boolean windowMode;
        this.listGui = new ListGui(this.field_22787, this, this.altManager.getList());
        this.method_25429((class_364)this.listGui);
        WurstClient wurst = WurstClient.INSTANCE;
        Exception folderException = this.altManager.getFolderException();
        if (folderException != null && this.shouldAsk) {
            title = class_2561.method_43470((String)wurst.translate("gui.wurst.altmanager.folder_error.title", new Object[0]));
            class_5250 message = class_2561.method_43470((String)wurst.translate("gui.wurst.altmanager.folder_error.message", folderException));
            class_5250 buttonText = class_2561.method_43471((String)"gui.done");
            Runnable action = () -> this.confirmGenerate(false);
            class_403 screen = new class_403(action, (class_2561)title, (class_2561)message, (class_2561)buttonText, false);
            this.field_22787.method_1507((class_437)screen);
        } else if (this.altManager.getList().isEmpty() && this.shouldAsk) {
            title = class_2561.method_43470((String)wurst.translate("gui.wurst.altmanager.empty.title", new Object[0]));
            class_5250 message = class_2561.method_43470((String)wurst.translate("gui.wurst.altmanager.empty.message", new Object[0]));
            BooleanConsumer callback = this::confirmGenerate;
            class_410 screen = new class_410(callback, (class_2561)title, (class_2561)message);
            this.field_22787.method_1507((class_437)screen);
        }
        this.useButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Login"), b -> this.pressLogin()).method_46434(this.field_22789 / 2 - 154, this.field_22790 - 52, 100, 20).method_46431();
        this.method_37063((class_364)this.useButton);
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Direct Login"), b -> this.field_22787.method_1507((class_437)new DirectLoginScreen(this))).method_46434(this.field_22789 / 2 - 50, this.field_22790 - 52, 100, 20).method_46431());
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Add"), b -> this.field_22787.method_1507((class_437)new AddAltScreen((class_437)this, this.altManager))).method_46434(this.field_22789 / 2 + 54, this.field_22790 - 52, 100, 20).method_46431());
        this.starButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Favorite"), b -> this.pressFavorite()).method_46434(this.field_22789 / 2 - 154, this.field_22790 - 28, 75, 20).method_46431();
        this.method_37063((class_364)this.starButton);
        this.editButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Edit"), b -> this.pressEdit()).method_46434(this.field_22789 / 2 - 76, this.field_22790 - 28, 74, 20).method_46431();
        this.method_37063((class_364)this.editButton);
        this.deleteButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Delete"), b -> this.pressDelete()).method_46434(this.field_22789 / 2 + 2, this.field_22790 - 28, 74, 20).method_46431();
        this.method_37063((class_364)this.deleteButton);
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Cancel"), b -> this.field_22787.method_1507(this.prevScreen)).method_46434(this.field_22789 / 2 + 80, this.field_22790 - 28, 75, 20).method_46431());
        this.importButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Import"), b -> this.pressImportAlts()).method_46434(8, 8, 50, 20).method_46431();
        this.method_37063((class_364)this.importButton);
        this.exportButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Export"), b -> this.pressExportAlts()).method_46434(58, 8, 50, 20).method_46431();
        this.method_37063((class_364)this.exportButton);
        this.logoutButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Logout"), b -> this.pressLogout()).method_46434(this.field_22789 - 50 - 8, 8, 50, 20).method_46431();
        this.method_37063((class_364)this.logoutButton);
        this.updateAltButtons();
        this.importButton.field_22763 = windowMode = (Boolean)this.field_22787.field_1690.method_42447().method_41753() == false;
        this.exportButton.field_22763 = windowMode;
    }

    private void updateAltButtons() {
        boolean altSelected;
        this.useButton.field_22763 = altSelected = this.listGui.method_25334() != null;
        this.starButton.field_22763 = altSelected;
        this.editButton.field_22763 = altSelected;
        this.deleteButton.field_22763 = altSelected;
        this.logoutButton.field_22763 = ((IMinecraftClient)this.field_22787).getWurstSession() != null;
    }

    public boolean method_25404(class_11908 context) {
        if (context.comp_4795() == 257) {
            this.useButton.method_25306((class_11907)context);
        }
        return super.method_25404(context);
    }

    public boolean method_25402(class_11909 context, boolean doubleClick) {
        if (context.method_74245() == 3) {
            this.method_25419();
            return true;
        }
        return super.method_25402(context, doubleClick);
    }

    private void pressLogin() {
        Alt alt = this.listGui.getSelectedAlt();
        if (alt == null) {
            return;
        }
        try {
            this.altManager.login(alt);
            failedLogins.remove(alt);
            this.field_22787.method_1507(this.prevScreen);
        }
        catch (LoginException e) {
            this.errorTimer = 8;
            failedLogins.add(alt);
        }
    }

    private void pressLogout() {
        ((IMinecraftClient)this.field_22787).setWurstSession(null);
        this.updateAltButtons();
    }

    private void pressFavorite() {
        Alt alt = this.listGui.getSelectedAlt();
        if (alt == null) {
            return;
        }
        this.altManager.toggleFavorite(alt);
        this.listGui.setSelected(null);
    }

    private void pressEdit() {
        Alt alt = this.listGui.getSelectedAlt();
        if (alt == null) {
            return;
        }
        this.field_22787.method_1507((class_437)new EditAltScreen(this, this.altManager, alt));
    }

    private void pressDelete() {
        Alt alt = this.listGui.getSelectedAlt();
        if (alt == null) {
            return;
        }
        class_5250 text = class_2561.method_43470((String)"Are you sure you want to remove this alt?");
        String altName = alt.getDisplayName();
        class_5250 message = class_2561.method_43470((String)("\"" + altName + "\" will be lost forever! (A long time!)"));
        class_410 screen = new class_410(this::confirmRemove, (class_2561)text, (class_2561)message, (class_2561)class_2561.method_43470((String)"Delete"), (class_2561)class_2561.method_43470((String)"Cancel"));
        this.field_22787.method_1507((class_437)screen);
    }

    private void pressImportAlts() {
        try {
            Process process = MultiProcessingUtils.startProcessWithIO(ImportAltsFileChooser.class, WurstClient.INSTANCE.getWurstFolder().toString());
            Path path = this.getFileChooserPath(process);
            process.waitFor();
            if (path.getFileName().toString().endsWith(".json")) {
                this.importAsJSON(path);
            } else {
                this.importAsTXT(path);
            }
        }
        catch (IOException | InterruptedException | JsonException e) {
            e.printStackTrace();
        }
    }

    private void importAsJSON(Path path) throws IOException, JsonException {
        WsonObject wson = JsonUtils.parseFileToObject(path);
        ArrayList<Alt> alts = AltsFile.parseJson(wson);
        this.altManager.addAll(alts);
    }

    private void importAsTXT(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        ArrayList<Alt> alts = new ArrayList<Alt>();
        for (String line : lines) {
            String[] data = line.split(":");
            switch (data.length) {
                case 1: {
                    alts.add(new CrackedAlt(data[0]));
                    break;
                }
                case 2: {
                    alts.add(new MojangAlt(data[0], data[1]));
                }
            }
        }
        this.altManager.addAll(alts);
    }

    private void pressExportAlts() {
        try {
            Process process = MultiProcessingUtils.startProcessWithIO(ExportAltsFileChooser.class, WurstClient.INSTANCE.getWurstFolder().toString());
            Path path = this.getFileChooserPath(process);
            process.waitFor();
            if (path.getFileName().toString().endsWith(".json")) {
                this.exportAsJSON(path);
            } else {
                this.exportAsTXT(path);
            }
        }
        catch (IOException | InterruptedException | JsonException e) {
            e.printStackTrace();
        }
    }

    private Path getFileChooserPath(Process process) throws IOException {
        try (BufferedReader bf = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));){
            String response = bf.readLine();
            if (response == null) {
                throw new IOException("No response from FileChooser");
            }
            try {
                Path path = Paths.get(response, new String[0]);
                return path;
            }
            catch (InvalidPathException e) {
                throw new IOException("Response from FileChooser is not a valid path");
            }
        }
    }

    private void exportAsJSON(Path path) throws IOException, JsonException {
        JsonObject json = AltsFile.createJson(this.altManager);
        JsonUtils.toJson(json, path);
    }

    private void exportAsTXT(Path path) throws IOException {
        ArrayList<String> lines = new ArrayList<String>();
        for (Alt alt : this.altManager.getList()) {
            lines.add(alt.exportAsTXT());
        }
        Files.write(path, lines, new OpenOption[0]);
    }

    private void confirmGenerate(boolean confirmed) {
        if (confirmed) {
            ArrayList<Alt> alts = new ArrayList<Alt>();
            for (int i = 0; i < 8; ++i) {
                alts.add(new CrackedAlt(NameGenerator.generateName()));
            }
            this.altManager.addAll(alts);
        }
        this.shouldAsk = false;
        this.field_22787.method_1507((class_437)this);
    }

    private void confirmRemove(boolean confirmed) {
        Alt alt = this.listGui.getSelectedAlt();
        if (alt == null) {
            return;
        }
        if (confirmed) {
            this.altManager.remove(alt);
        }
        this.field_22787.method_1507((class_437)this);
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float partialTicks) {
        this.listGui.method_25394(context, mouseX, mouseY, partialTicks);
        Alt alt = this.listGui.getSelectedAlt();
        if (alt != null) {
            AltRenderer.drawAltBack(context, alt.getName(), (this.field_22789 / 2 - 125) / 2 - 32, this.field_22790 / 2 - 64 - 9, 64, 128);
            AltRenderer.drawAltBody(context, alt.getName(), this.field_22789 - (this.field_22789 / 2 - 140) / 2 - 32, this.field_22790 / 2 - 64 - 9, 64, 128);
        }
        context.method_25300(this.field_22793, "Alt Manager", this.field_22789 / 2, 4, -1);
        context.method_25300(this.field_22793, "Alts: " + this.altManager.getList().size(), this.field_22789 / 2, 14, -6250336);
        context.method_25300(this.field_22793, "premium: " + this.altManager.getNumPremium() + ", cracked: " + this.altManager.getNumCracked(), this.field_22789 / 2, 24, -6250336);
        if (this.errorTimer > 0) {
            int alpha = (int)(Math.min(1.0f, (float)this.errorTimer / 16.0f) * 255.0f);
            int color = 0xFF0000 | alpha << 24;
            context.method_25294(0, 0, this.field_22789, this.field_22790, color);
            --this.errorTimer;
        }
        for (class_4068 drawable : this.field_33816) {
            drawable.method_25394(context, mouseX, mouseY, partialTicks);
        }
        this.renderButtonTooltip(context, mouseX, mouseY);
        this.renderAltTooltip(context, mouseX, mouseY);
    }

    private void renderAltTooltip(class_332 context, int mouseX, int mouseY) {
        if (!this.listGui.method_25405(mouseX, mouseY)) {
            return;
        }
        Entry hoveredEntry = this.listGui.getHoveredEntry(mouseX, mouseY);
        if (hoveredEntry == null) {
            return;
        }
        int hoveredIndex = this.listGui.method_25396().indexOf((Object)hoveredEntry);
        int itemX = mouseX - this.listGui.method_25342();
        int itemY = mouseY - this.listGui.method_25337(hoveredIndex);
        if (itemX < 31 || itemY < 15 || itemY >= 25) {
            return;
        }
        Alt alt = hoveredEntry.alt;
        ArrayList<class_2561> tooltip = new ArrayList<class_2561>();
        if (itemX >= 31 + this.field_22793.method_1727(hoveredEntry.getBottomText())) {
            return;
        }
        if (alt.isCracked()) {
            this.addTooltip(tooltip, "cracked");
        } else {
            this.addTooltip(tooltip, "premium");
            if (failedLogins.contains(alt)) {
                this.addTooltip(tooltip, "failed");
            }
            if (alt.isCheckedPremium()) {
                this.addTooltip(tooltip, "checked");
            } else {
                this.addTooltip(tooltip, "unchecked");
            }
        }
        if (alt.isFavorite()) {
            this.addTooltip(tooltip, "favorite");
        }
        context.method_51434(this.field_22793, tooltip, mouseX, mouseY);
    }

    private void renderButtonTooltip(class_332 context, int mouseX, int mouseY) {
        for (class_339 button : Screens.getButtons((class_437)this)) {
            if (!button.method_25367() || button != this.importButton && button != this.exportButton) continue;
            ArrayList<class_2561> tooltip = new ArrayList<class_2561>();
            this.addTooltip(tooltip, "window");
            if (((Boolean)this.field_22787.field_1690.method_42447().method_41753()).booleanValue()) {
                this.addTooltip(tooltip, "fullscreen");
            } else {
                this.addTooltip(tooltip, "window_freeze");
            }
            context.method_51434(this.field_22793, tooltip, mouseX, mouseY);
            break;
        }
    }

    private void addTooltip(ArrayList<class_2561> tooltip, String trKey) {
        String translated = WurstClient.INSTANCE.translate("description.wurst.altmanager." + trKey, new Object[0]);
        StringJoiner joiner = new StringJoiner("\n");
        this.field_22793.method_27527().method_27498(translated, 200, class_2583.field_24360).stream().map(class_5348::getString).forEach(s -> joiner.add((CharSequence)s));
        String wrapped = joiner.toString();
        for (String line : wrapped.split("\n")) {
            tooltip.add((class_2561)class_2561.method_43470((String)line));
        }
    }

    public void method_25419() {
        this.field_22787.method_1507(this.prevScreen);
    }

    private final class ListGui
    extends class_4280<Entry> {
        public ListGui(class_310 minecraft, AltManagerScreen screen, List<Alt> list) {
            super(minecraft, screen.field_22789, screen.field_22790 - 96, 36, 30);
            list.stream().map(x$0 -> new Entry((Alt)x$0)).forEach(x$0 -> this.method_25321((class_350.class_351)x$0));
        }

        public void setSelected(@Nullable Entry entry) {
            super.method_25313((class_350.class_351)entry);
            AltManagerScreen.this.updateAltButtons();
        }

        protected void method_25339() {
            super.method_25339();
            AltManagerScreen.this.updateAltButtons();
        }

        public Alt getSelectedAlt() {
            Entry selected = (Entry)this.method_25334();
            return selected != null ? selected.alt : null;
        }

        public Entry getHoveredEntry(double mouseX, double mouseY) {
            Optional hovered = this.method_19355(mouseX, mouseY);
            return hovered.map(e -> (Entry)((Object)e)).orElse(null);
        }
    }

    private final class Entry
    extends class_4280.class_4281<Entry> {
        private final Alt alt;
        private long lastClickTime;

        public Entry(Alt alt) {
            this.alt = Objects.requireNonNull(alt);
        }

        public class_2561 method_37006() {
            return class_2561.method_43469((String)"narrator.select", (Object[])new Object[]{"Alt " + String.valueOf(this.alt) + ", " + class_3544.method_15440((String)this.getBottomText())});
        }

        public boolean method_25402(class_11909 context, boolean doubleClick) {
            if (context.method_74245() != 0) {
                return false;
            }
            long timeSinceLastClick = class_156.method_658() - this.lastClickTime;
            this.lastClickTime = class_156.method_658();
            if (timeSinceLastClick < 250L) {
                AltManagerScreen.this.pressLogin();
            }
            return true;
        }

        public void method_25343(class_332 context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int x = this.method_73380();
            int y = this.method_73382();
            if (AltManagerScreen.this.field_22787.method_1548().method_1676().equals(this.alt.getName())) {
                float opacity = 0.3f - Math.abs(class_3532.method_15374((double)((float)(System.currentTimeMillis() % 10000L) / 10000.0f * (float)Math.PI * 2.0f)) * 0.15f);
                int color = 0xFF00 | (int)(opacity * 255.0f) << 24;
                context.method_25294(x - 2, y - 2, x + 218, y + 28, color);
            }
            AltRenderer.drawAltFace(context, this.alt.getName(), x + 1, y + 1, 24, 24, AltManagerScreen.this.listGui.method_25334() == this);
            class_327 tr = ((AltManagerScreen)AltManagerScreen.this).field_22787.field_1772;
            context.method_51433(tr, "Name: " + this.alt.getDisplayName(), x + 31, y + 3, -6250336, false);
            context.method_51433(tr, this.getBottomText(), x + 31, y + 15, -6250336, false);
        }

        private String getBottomText() {
            Object text;
            Object object = text = this.alt.isCracked() ? "\u00a78cracked" : "\u00a72premium";
            if (this.alt.isFavorite()) {
                text = (String)text + "\u00a7r, \u00a7efavorite";
            }
            if (failedLogins.contains(this.alt)) {
                text = (String)text + "\u00a7r, \u00a7cwrong password?";
            } else if (this.alt.isUncheckedPremium()) {
                text = (String)text + "\u00a7r, \u00a7cunchecked";
            }
            return text;
        }
    }
}
