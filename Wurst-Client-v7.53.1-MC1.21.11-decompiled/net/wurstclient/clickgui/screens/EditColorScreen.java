package net.wurstclient.clickgui.screens;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import net.minecraft.class_10799;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_364;
import net.minecraft.class_4068;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.util.ColorUtils;

public final class EditColorScreen
extends class_437 {
    private final class_437 prevScreen;
    private final ColorSetting colorSetting;
    private Color color;
    private class_342 hexValueField;
    private class_342 redValueField;
    private class_342 greenValueField;
    private class_342 blueValueField;
    private class_4185 doneButton;
    private final class_2960 paletteIdentifier = class_2960.method_60655((String)"wurst", (String)"colorpalette.png");
    private BufferedImage paletteAsBufferedImage;
    private int paletteX = 0;
    private int paletteY = 0;
    private final int paletteWidth = 200;
    private final int paletteHeight = 84;
    private int fieldsX = 0;
    private int fieldsY = 0;
    private boolean ignoreChanges;

    public EditColorScreen(class_437 prevScreen, ColorSetting colorSetting) {
        super((class_2561)class_2561.method_43470((String)""));
        this.prevScreen = prevScreen;
        this.colorSetting = colorSetting;
        this.color = colorSetting.getColor();
    }

    public void method_25426() {
        try (InputStream stream = this.field_22787.method_1478().getResourceOrThrow(this.paletteIdentifier).method_14482();){
            this.paletteAsBufferedImage = ImageIO.read(stream);
        }
        catch (IOException e) {
            this.paletteAsBufferedImage = null;
            e.printStackTrace();
        }
        class_327 tr = this.field_22787.field_1772;
        this.paletteX = this.field_22789 / 2 - 100;
        this.paletteY = 32;
        this.fieldsX = this.field_22789 / 2 - 100;
        this.fieldsY = 134;
        this.hexValueField = new class_342(tr, this.fieldsX, this.fieldsY, 92, 20, (class_2561)class_2561.method_43470((String)""));
        this.hexValueField.method_1852(ColorUtils.toHex(this.color).substring(1));
        this.hexValueField.method_1880(6);
        this.hexValueField.method_1863(s -> this.updateColor(true));
        this.redValueField = new class_342(tr, this.fieldsX, this.fieldsY + 35, 50, 20, (class_2561)class_2561.method_43470((String)""));
        this.redValueField.method_1852("" + this.color.getRed());
        this.redValueField.method_1880(3);
        this.redValueField.method_1863(s -> this.updateColor(false));
        this.greenValueField = new class_342(tr, this.fieldsX + 75, this.fieldsY + 35, 50, 20, (class_2561)class_2561.method_43470((String)""));
        this.greenValueField.method_1852("" + this.color.getGreen());
        this.greenValueField.method_1880(3);
        this.greenValueField.method_1863(s -> this.updateColor(false));
        this.blueValueField = new class_342(tr, this.fieldsX + 150, this.fieldsY + 35, 50, 20, (class_2561)class_2561.method_43470((String)""));
        this.blueValueField.method_1852("" + this.color.getBlue());
        this.blueValueField.method_1880(3);
        this.blueValueField.method_1863(s -> this.updateColor(false));
        this.method_25429((class_364)this.hexValueField);
        this.method_25429((class_364)this.redValueField);
        this.method_25429((class_364)this.greenValueField);
        this.method_25429((class_364)this.blueValueField);
        this.method_25395((class_364)this.hexValueField);
        this.hexValueField.method_25365(true);
        this.hexValueField.method_1875(0);
        this.hexValueField.method_1884(6);
        this.doneButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Done"), b -> this.done()).method_46434(this.fieldsX, this.field_22790 - 30, 200, 20).method_46431();
        this.method_37063((class_364)this.doneButton);
    }

    private void updateColor(boolean hex) {
        if (this.ignoreChanges) {
            return;
        }
        Color newColor = hex ? ColorUtils.tryParseHex("#" + this.hexValueField.method_1882()) : ColorUtils.tryParseRGB(this.redValueField.method_1882(), this.greenValueField.method_1882(), this.blueValueField.method_1882());
        if (newColor == null || newColor.equals(this.color)) {
            return;
        }
        this.color = newColor;
        this.ignoreChanges = true;
        this.hexValueField.method_1852(ColorUtils.toHex(this.color).substring(1));
        this.redValueField.method_1852("" + this.color.getRed());
        this.greenValueField.method_1852("" + this.color.getGreen());
        this.blueValueField.method_1852("" + this.color.getBlue());
        this.ignoreChanges = false;
    }

    private void done() {
        this.colorSetting.setColor(this.color);
        this.field_22787.method_1507(this.prevScreen);
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float partialTicks) {
        class_327 tr = this.field_22787.field_1772;
        context.method_25300(this.field_22787.field_1772, this.colorSetting.getName(), this.field_22789 / 2, 16, -986896);
        int x = this.paletteX;
        int y = this.paletteY;
        int w = 200;
        int h = 84;
        int fw = 200;
        int fh = 84;
        float u = 0.0f;
        float v = 0.0f;
        context.method_25290(class_10799.field_56883, this.paletteIdentifier, x, y, u, v, w, h, fw, fh);
        context.method_51433(tr, "#", this.fieldsX - 3 - tr.method_1727("#"), this.fieldsY + 6, -986896, false);
        context.method_51433(tr, "R:", this.fieldsX - 3 - tr.method_1727("R:"), this.fieldsY + 6 + 35, -65536, false);
        context.method_51433(tr, "G:", this.fieldsX + 75 - 3 - tr.method_1727("G:"), this.fieldsY + 6 + 35, -16711936, false);
        context.method_51433(tr, "B:", this.fieldsX + 150 - 3 - tr.method_1727("B:"), this.fieldsY + 6 + 35, -16776961, false);
        this.hexValueField.method_25394(context, mouseX, mouseY, partialTicks);
        this.redValueField.method_25394(context, mouseX, mouseY, partialTicks);
        this.greenValueField.method_25394(context, mouseX, mouseY, partialTicks);
        this.blueValueField.method_25394(context, mouseX, mouseY, partialTicks);
        int borderSize = 1;
        int boxWidth = 92;
        int boxHeight = 20;
        int boxX = this.field_22789 / 2 + 8;
        int boxY = this.fieldsY;
        context.method_25294(boxX - borderSize, boxY - borderSize, boxX + boxWidth + borderSize, boxY + boxHeight + borderSize, -6250336);
        context.method_25294(boxX, boxY, boxX + boxWidth, boxY + boxHeight, this.color.getRGB());
        for (class_4068 drawable : this.field_33816) {
            drawable.method_25394(context, mouseX, mouseY, partialTicks);
        }
    }

    public void method_25410(int width, int height) {
        String hex = this.hexValueField.method_1882();
        String r = this.redValueField.method_1882();
        String g = this.greenValueField.method_1882();
        String b = this.blueValueField.method_1882();
        this.method_25423(width, height);
        this.hexValueField.method_1852(hex);
        this.redValueField.method_1852(r);
        this.greenValueField.method_1852(g);
        this.blueValueField.method_1852(b);
    }

    public boolean method_25404(class_11908 context) {
        switch (context.comp_4795()) {
            case 257: {
                this.done();
                break;
            }
            case 256: {
                this.field_22787.method_1507(this.prevScreen);
            }
        }
        return super.method_25404(context);
    }

    public boolean method_25402(class_11909 context, boolean doubleClick) {
        double mouseX = context.comp_4798();
        double mouseY = context.comp_4799();
        if (mouseX >= (double)this.paletteX && mouseX <= (double)(this.paletteX + 200) && mouseY >= (double)this.paletteY && mouseY <= (double)(this.paletteY + 84)) {
            int rgb;
            Color color;
            if (this.paletteAsBufferedImage == null) {
                return super.method_25402(context, doubleClick);
            }
            int x = (int)Math.round((mouseX - (double)this.paletteX) / 200.0 * (double)this.paletteAsBufferedImage.getWidth());
            int y = (int)Math.round((mouseY - (double)this.paletteY) / 84.0 * (double)this.paletteAsBufferedImage.getHeight());
            if (x > 0 && y > 0 && x < this.paletteAsBufferedImage.getWidth() && y < this.paletteAsBufferedImage.getHeight() && (color = new Color(rgb = this.paletteAsBufferedImage.getRGB(x, y), true)).getAlpha() >= 255) {
                this.setColor(color);
            }
        }
        return super.method_25402(context, doubleClick);
    }

    private void setColor(Color color) {
        this.hexValueField.method_1852(ColorUtils.toHex(color).substring(1));
        this.redValueField.method_1852("" + color.getRed());
        this.greenValueField.method_1852("" + color.getGreen());
        this.blueValueField.method_1852("" + color.getBlue());
    }

    public boolean method_25421() {
        return false;
    }

    public boolean method_25422() {
        return false;
    }
}
