package net.wurstclient.altmanager.screens;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import net.minecraft.class_11907;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_364;
import net.minecraft.class_4068;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_5481;
import net.wurstclient.WurstClient;
import net.wurstclient.altmanager.AltRenderer;
import net.wurstclient.altmanager.NameGenerator;
import net.wurstclient.altmanager.SkinStealer;

public abstract class AltEditorScreen
extends class_437 {
    private final Path skinFolder = WurstClient.INSTANCE.getWurstFolder().resolve("skins");
    protected final class_437 prevScreen;
    private class_342 nameOrEmailBox;
    private class_342 passwordBox;
    private class_4185 doneButton;
    private class_4185 stealSkinButton;
    protected String message = "";
    private int errorTimer;

    public AltEditorScreen(class_437 prevScreen, class_2561 title) {
        super(title);
        this.prevScreen = prevScreen;
    }

    public final void method_25426() {
        this.nameOrEmailBox = new class_342(this.field_22793, this.field_22789 / 2 - 100, 60, 200, 20, (class_2561)class_2561.method_43470((String)""));
        this.nameOrEmailBox.method_1880(48);
        this.nameOrEmailBox.method_25365(true);
        this.nameOrEmailBox.method_1852(this.getDefaultNameOrEmail());
        this.method_25429((class_364)this.nameOrEmailBox);
        this.passwordBox = new class_342(this.field_22793, this.field_22789 / 2 - 100, 100, 200, 20, (class_2561)class_2561.method_43470((String)""));
        this.passwordBox.method_1852(this.getDefaultPassword());
        this.passwordBox.method_73210((text, startIndex) -> class_5481.method_30747((String)"*".repeat(text.length()), (class_2583)class_2583.field_24360));
        this.passwordBox.method_1880(256);
        this.method_25429((class_364)this.passwordBox);
        this.doneButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)this.getDoneButtonText()), b -> this.pressDoneButton()).method_46434(this.field_22789 / 2 - 100, this.field_22790 / 4 + 72 + 12, 200, 20).method_46431();
        this.method_37063((class_364)this.doneButton);
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Cancel"), b -> this.method_25419()).method_46434(this.field_22789 / 2 - 100, this.field_22790 / 4 + 120 + 12, 200, 20).method_46431());
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Random Name"), b -> this.nameOrEmailBox.method_1852(NameGenerator.generateName())).method_46434(this.field_22789 / 2 - 100, this.field_22790 / 4 + 96 + 12, 200, 20).method_46431());
        this.stealSkinButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Steal Skin"), b -> {
            this.message = this.stealSkin(this.getNameOrEmail());
        }).method_46434(this.field_22789 - (this.field_22789 / 2 - 100) / 2 - 64, this.field_22790 - 32, 128, 20).method_46431();
        this.method_37063((class_364)this.stealSkinButton);
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Open Skin Folder"), b -> this.openSkinFolder()).method_46434((this.field_22789 / 2 - 100) / 2 - 64, this.field_22790 - 32, 128, 20).method_46431());
        this.method_25395((class_364)this.nameOrEmailBox);
    }

    private void openSkinFolder() {
        this.createSkinFolder();
        class_156.method_668().method_672(this.skinFolder.toFile());
    }

    private void createSkinFolder() {
        try {
            Files.createDirectories(this.skinFolder, new FileAttribute[0]);
        }
        catch (IOException e) {
            e.printStackTrace();
            this.message = "\u00a74\u00a7lSkin folder could not be created.";
        }
    }

    public final void method_25393() {
        String nameOrEmail = this.nameOrEmailBox.method_1882().trim();
        boolean alex = nameOrEmail.equalsIgnoreCase("Alexander01998");
        this.doneButton.field_22763 = !nameOrEmail.isEmpty() && (!alex || !this.passwordBox.method_1882().isEmpty());
        this.doneButton.method_25355((class_2561)class_2561.method_43470((String)this.getDoneButtonText()));
        this.stealSkinButton.field_22763 = !alex;
    }

    protected final String getNameOrEmail() {
        return this.nameOrEmailBox.method_1882();
    }

    protected final String getPassword() {
        return this.passwordBox.method_1882();
    }

    protected String getDefaultNameOrEmail() {
        return this.field_22787.method_1548().method_1676();
    }

    protected String getDefaultPassword() {
        return "";
    }

    protected abstract String getDoneButtonText();

    protected abstract void pressDoneButton();

    protected final void doErrorEffect() {
        this.errorTimer = 8;
    }

    private final String stealSkin(String name) {
        this.createSkinFolder();
        Path path = this.skinFolder.resolve(name + ".png");
        try {
            URL url = SkinStealer.getSkinUrl(name);
            try (InputStream in = url.openStream();){
                Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
            }
            return "\u00a7a\u00a7lSaved skin as " + name + ".png";
        }
        catch (IOException e) {
            e.printStackTrace();
            return "\u00a74\u00a7lSkin could not be saved.";
        }
        catch (NullPointerException e) {
            e.printStackTrace();
            return "\u00a74\u00a7lPlayer does not exist.";
        }
    }

    public boolean method_25404(class_11908 context) {
        if (context.comp_4795() == 257) {
            this.doneButton.method_25306((class_11907)context);
        }
        return super.method_25404(context);
    }

    public boolean method_25402(class_11909 context, boolean doubleClick) {
        this.nameOrEmailBox.method_25402(context, doubleClick);
        this.passwordBox.method_25402(context, doubleClick);
        if (this.nameOrEmailBox.method_25370() || this.passwordBox.method_25370()) {
            this.message = "";
        }
        if (context.method_74245() == 3) {
            this.method_25419();
            return true;
        }
        return super.method_25402(context, doubleClick);
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float partialTicks) {
        AltRenderer.drawAltBack(context, this.nameOrEmailBox.method_1882(), (this.field_22789 / 2 - 100) / 2 - 64, this.field_22790 / 2 - 128, 128, 256);
        AltRenderer.drawAltBody(context, this.nameOrEmailBox.method_1882(), this.field_22789 - (this.field_22789 / 2 - 100) / 2 - 64, this.field_22790 / 2 - 128, 128, 256);
        String accountType = this.getPassword().isEmpty() ? "cracked" : "premium";
        context.method_25303(this.field_22793, "Name (for cracked alts), or", this.field_22789 / 2 - 100, 37, -6250336);
        context.method_25303(this.field_22793, "E-Mail (for premium alts)", this.field_22789 / 2 - 100, 47, -6250336);
        context.method_25303(this.field_22793, "Password (for premium alts)", this.field_22789 / 2 - 100, 87, -6250336);
        context.method_25303(this.field_22793, "Account type: " + accountType, this.field_22789 / 2 - 100, 127, -6250336);
        String[] lines = this.message.split("\n");
        for (int i = 0; i < lines.length; ++i) {
            context.method_25300(this.field_22793, lines[i], this.field_22789 / 2, 142 + 10 * i, -1);
        }
        this.nameOrEmailBox.method_25394(context, mouseX, mouseY, partialTicks);
        this.passwordBox.method_25394(context, mouseX, mouseY, partialTicks);
        if (this.errorTimer > 0) {
            int alpha = (int)(Math.min(1.0f, (float)this.errorTimer / 16.0f) * 255.0f);
            int color = 0xFF0000 | alpha << 24;
            context.method_25294(0, 0, this.field_22789, this.field_22790, color);
            --this.errorTimer;
        }
        for (class_4068 drawable : this.field_33816) {
            drawable.method_25394(context, mouseX, mouseY, partialTicks);
        }
    }

    public final void method_25419() {
        this.field_22787.method_1507(this.prevScreen);
    }
}
