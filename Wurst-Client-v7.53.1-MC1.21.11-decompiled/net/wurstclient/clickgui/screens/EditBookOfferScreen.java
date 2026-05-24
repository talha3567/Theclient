package net.wurstclient.clickgui.screens;

import net.minecraft.class_11907;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1887;
import net.minecraft.class_1935;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_364;
import net.minecraft.class_4068;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_6880;
import net.minecraft.class_7923;
import net.minecraft.class_9636;
import net.wurstclient.hacks.autolibrarian.BookOffer;
import net.wurstclient.settings.BookOffersSetting;
import net.wurstclient.util.MathUtils;
import net.wurstclient.util.RenderUtils;
import org.joml.Matrix3x2fStack;

public final class EditBookOfferScreen
extends class_437 {
    private final class_437 prevScreen;
    private final BookOffersSetting bookOffers;
    private class_342 levelField;
    private class_4185 levelPlusButton;
    private class_4185 levelMinusButton;
    private class_342 priceField;
    private class_4185 pricePlusButton;
    private class_4185 priceMinusButton;
    private class_4185 saveButton;
    private class_4185 cancelButton;
    private BookOffer offerToSave;
    private int index;
    private boolean alreadyAdded;

    public EditBookOfferScreen(class_437 prevScreen, BookOffersSetting bookOffers, int index) {
        super((class_2561)class_2561.method_43470((String)""));
        this.prevScreen = prevScreen;
        this.bookOffers = bookOffers;
        this.index = index;
        this.offerToSave = bookOffers.getOffers().get(index);
    }

    public void method_25426() {
        this.levelField = new class_342(this.field_22787.field_1772, this.field_22789 / 2 - 32, 110, 28, 12, (class_2561)class_2561.method_43470((String)""));
        this.method_25429((class_364)this.levelField);
        this.levelField.method_1880(2);
        this.levelField.method_1890(t -> {
            if (t.isEmpty()) {
                return true;
            }
            if (!MathUtils.isInteger(t)) {
                return false;
            }
            int level = Integer.parseInt(t);
            if (level < 1 || level > 10) {
                return false;
            }
            if (this.offerToSave == null) {
                return true;
            }
            class_1887 enchantment = this.offerToSave.getEnchantment();
            return level <= enchantment.method_8183();
        });
        this.levelField.method_1863(t -> {
            if (!MathUtils.isInteger(t)) {
                return;
            }
            int level = Integer.parseInt(t);
            this.updateLevel(level, false);
        });
        this.priceField = new class_342(this.field_22787.field_1772, this.field_22789 / 2 - 32, 126, 28, 12, (class_2561)class_2561.method_43470((String)""));
        this.method_25429((class_364)this.priceField);
        this.priceField.method_1880(2);
        this.priceField.method_1890(t -> t.isEmpty() || MathUtils.isInteger(t) && Integer.parseInt(t) >= 1 && Integer.parseInt(t) <= 64);
        this.priceField.method_1863(t -> {
            if (!MathUtils.isInteger(t)) {
                return;
            }
            int price = Integer.parseInt(t);
            this.updatePrice(price, false);
        });
        this.levelPlusButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"+"), b -> this.updateLevel(1, true)).method_46434(this.field_22789 / 2 + 2, 110, 20, 12).method_46431();
        this.method_37063((class_364)this.levelPlusButton);
        this.levelPlusButton.field_22763 = false;
        this.levelMinusButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"-"), b -> this.updateLevel(-1, true)).method_46434(this.field_22789 / 2 + 26, 110, 20, 12).method_46431();
        this.method_37063((class_364)this.levelMinusButton);
        this.levelMinusButton.field_22763 = false;
        this.pricePlusButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"+"), b -> this.updatePrice(1, true)).method_46434(this.field_22789 / 2 + 2, 126, 20, 12).method_46431();
        this.method_37063((class_364)this.pricePlusButton);
        this.pricePlusButton.field_22763 = false;
        this.priceMinusButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"-"), b -> this.updatePrice(-1, true)).method_46434(this.field_22789 / 2 + 26, 126, 20, 12).method_46431();
        this.method_37063((class_364)this.priceMinusButton);
        this.priceMinusButton.field_22763 = false;
        this.saveButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Save"), b -> {
            if (this.offerToSave == null || !this.offerToSave.isFullyValid()) {
                return;
            }
            this.bookOffers.replace(this.index, this.offerToSave);
            this.field_22787.method_1507(this.prevScreen);
        }).method_46434(this.field_22789 / 2 - 102, this.field_22790 / 3 * 2, 100, 20).method_46431();
        this.method_37063((class_364)this.saveButton);
        this.saveButton.field_22763 = false;
        this.cancelButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Cancel"), b -> this.field_22787.method_1507(this.prevScreen)).method_46434(this.field_22789 / 2 + 2, this.field_22790 / 3 * 2, 100, 20).method_46431();
        this.method_37063((class_364)this.cancelButton);
        this.updateSelectedOffer(this.offerToSave);
    }

    private void updateLevel(int i, boolean offset) {
        if (this.offerToSave == null) {
            return;
        }
        String id = this.offerToSave.id();
        int level = offset ? this.offerToSave.level() + i : i;
        int price = this.offerToSave.price();
        class_1887 enchantment = this.offerToSave.getEnchantment();
        if (level < 1 || level > enchantment.method_8183()) {
            return;
        }
        this.updateSelectedOffer(new BookOffer(id, level, price));
    }

    private void updatePrice(int i, boolean offset) {
        int price;
        if (this.offerToSave == null) {
            return;
        }
        String id = this.offerToSave.id();
        int level = this.offerToSave.level();
        int n = price = offset ? this.offerToSave.price() + i : i;
        if (price < 1 || price > 64) {
            return;
        }
        this.updateSelectedOffer(new BookOffer(id, level, price));
    }

    private void updateSelectedOffer(BookOffer offer) {
        this.offerToSave = offer;
        this.alreadyAdded = offer != null && !offer.equals(this.bookOffers.getOffers().get(this.index)) && this.bookOffers.contains(offer);
        boolean bl = this.saveButton.field_22763 = offer != null && !this.alreadyAdded;
        if (offer == null) {
            if (!this.levelField.method_1882().isEmpty()) {
                this.levelField.method_1852("");
            }
            if (!this.priceField.method_1882().isEmpty()) {
                this.priceField.method_1852("");
            }
        } else {
            String level = "" + offer.level();
            if (!this.levelField.method_1882().equals(level)) {
                this.levelField.method_1852(level);
            }
            String price = "" + offer.price();
            if (!this.priceField.method_1882().equals(price)) {
                this.priceField.method_1852(price);
            }
        }
    }

    public boolean method_25402(class_11909 context, boolean doubleClick) {
        boolean childClicked = super.method_25402(context, doubleClick);
        this.levelField.method_25402(context, doubleClick);
        this.priceField.method_25402(context, doubleClick);
        if (context.method_74245() == 3) {
            this.cancelButton.method_25306((class_11907)context);
        }
        return childClicked;
    }

    public boolean method_25404(class_11908 context) {
        switch (context.comp_4795()) {
            case 257: {
                if (!this.saveButton.field_22763) break;
                this.saveButton.method_25306((class_11907)context);
                break;
            }
            case 256: {
                this.cancelButton.method_25306((class_11907)context);
                break;
            }
        }
        return super.method_25404(context);
    }

    public void method_25393() {
        this.levelPlusButton.field_22763 = this.offerToSave != null && this.offerToSave.level() < this.offerToSave.getEnchantment().method_8183();
        this.levelMinusButton.field_22763 = this.offerToSave != null && this.offerToSave.level() > 1;
        this.pricePlusButton.field_22763 = this.offerToSave != null && this.offerToSave.price() < 64;
        this.priceMinusButton.field_22763 = this.offerToSave != null && this.offerToSave.price() > 1;
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float partialTicks) {
        Object price;
        Matrix3x2fStack matrixStack = context.method_51448();
        matrixStack.pushMatrix();
        class_327 tr = this.field_22787.field_1772;
        String titleText = "Edit Book Offer";
        context.method_25300(tr, titleText, this.field_22789 / 2, 12, -1);
        int x = this.field_22789 / 2 - 100;
        int y = 64;
        class_1792 item = (class_1792)class_7923.field_41178.method_63535(class_2960.method_60654((String)"enchanted_book"));
        class_1799 stack = new class_1799((class_1935)item);
        RenderUtils.drawItem(context, stack, x + 1, y + 1, true);
        BookOffer bookOffer = this.offerToSave;
        String name = bookOffer.getEnchantmentNameWithLevel();
        class_6880<class_1887> enchantment = bookOffer.getEnchantmentEntry().get();
        int nameColor = enchantment.method_40220(class_9636.field_51551) ? -43691 : -1;
        context.method_25303(tr, name, x + 28, y, nameColor);
        context.method_51433(tr, bookOffer.id(), x + 28, y + 9, -6250336, false);
        if (bookOffer.price() >= 64) {
            price = "any price";
        } else {
            price = "max " + bookOffer.price();
            RenderUtils.drawItem(context, new class_1799((class_1935)class_1802.field_8687), x + 28 + tr.method_1727((String)price), y + 16, false);
        }
        context.method_51433(tr, (String)price, x + 28, y + 18, -6250336, false);
        this.levelField.method_25394(context, mouseX, mouseY, partialTicks);
        this.priceField.method_25394(context, mouseX, mouseY, partialTicks);
        for (class_4068 drawable : this.field_33816) {
            drawable.method_25394(context, mouseX, mouseY, partialTicks);
        }
        matrixStack.translate((float)(this.field_22789 / 2 - 100), 112.0f);
        context.method_25303(tr, "Level:", 0, 0, -986896);
        context.method_25303(tr, "Max price:", 0, 16, -986896);
        if (this.alreadyAdded && this.offerToSave != null) {
            String errorText = this.offerToSave.getEnchantmentNameWithLevel() + " is already on your list!";
            context.method_25303(tr, errorText, 0, 32, -43691);
        }
        matrixStack.popMatrix();
        RenderUtils.drawItem(context, new class_1799((class_1935)class_1802.field_8687), this.field_22789 / 2 - 16, 126, false);
    }

    public boolean method_25421() {
        return false;
    }

    public boolean method_25422() {
        return false;
    }
}
