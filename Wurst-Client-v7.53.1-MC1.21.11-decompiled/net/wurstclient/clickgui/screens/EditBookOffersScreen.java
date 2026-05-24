package net.wurstclient.clickgui.screens;

import java.util.List;
import java.util.Objects;
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
import net.minecraft.class_6880;
import net.minecraft.class_7923;
import net.minecraft.class_9636;
import net.wurstclient.clickgui.screens.AddBookOfferScreen;
import net.wurstclient.clickgui.screens.EditBookOfferScreen;
import net.wurstclient.hacks.autolibrarian.BookOffer;
import net.wurstclient.settings.BookOffersSetting;
import net.wurstclient.util.RenderUtils;

public final class EditBookOffersScreen
extends class_437 {
    private final class_437 prevScreen;
    private final BookOffersSetting bookOffers;
    private ListGui listGui;
    private class_4185 editButton;
    private class_4185 removeButton;
    private class_4185 doneButton;

    public EditBookOffersScreen(class_437 prevScreen, BookOffersSetting bookOffers) {
        super((class_2561)class_2561.method_43470((String)""));
        this.prevScreen = prevScreen;
        this.bookOffers = bookOffers;
    }

    public void method_25426() {
        this.listGui = new ListGui(this.field_22787, this, this.bookOffers.getOffers());
        this.method_25429((class_364)this.listGui);
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Add"), b -> this.field_22787.method_1507((class_437)new AddBookOfferScreen(this, this.bookOffers))).method_46434(this.field_22789 / 2 - 154, this.field_22790 - 56, 100, 20).method_46431());
        this.editButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Edit"), b -> {
            BookOffer selected = this.listGui.getSelectedOffer();
            if (selected == null) {
                return;
            }
            this.field_22787.method_1507((class_437)new EditBookOfferScreen(this, this.bookOffers, this.bookOffers.indexOf(selected)));
        }).method_46434(this.field_22789 / 2 - 50, this.field_22790 - 56, 100, 20).method_46431();
        this.method_37063((class_364)this.editButton);
        this.editButton.field_22763 = false;
        this.removeButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Remove"), b -> {
            this.bookOffers.remove(this.bookOffers.indexOf(this.listGui.getSelectedOffer()));
            this.field_22787.method_1507((class_437)this);
        }).method_46434(this.field_22789 / 2 + 54, this.field_22790 - 56, 100, 20).method_46431();
        this.method_37063((class_364)this.removeButton);
        this.removeButton.field_22763 = false;
        this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Reset to Defaults"), b -> this.field_22787.method_1507((class_437)new class_410(b2 -> {
            if (b2) {
                this.bookOffers.resetToDefaults();
            }
            this.field_22787.method_1507((class_437)this);
        }, (class_2561)class_2561.method_43470((String)"Reset to Defaults"), (class_2561)class_2561.method_43470((String)"Are you sure?")))).method_46434(this.field_22789 - 106, 6, 100, 20).method_46431());
        this.doneButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Done"), b -> this.field_22787.method_1507(this.prevScreen)).method_46434(this.field_22789 / 2 - 100, this.field_22790 - 32, 200, 20).method_46431();
        this.method_37063((class_364)this.doneButton);
    }

    public boolean method_25402(class_11909 context, boolean doubleClick) {
        boolean childClicked = super.method_25402(context, doubleClick);
        if (context.method_74245() == 3) {
            this.doneButton.method_25306((class_11907)context);
        }
        return childClicked;
    }

    public boolean method_25404(class_11908 context) {
        switch (context.comp_4795()) {
            case 257: {
                if (!this.editButton.field_22763) break;
                this.editButton.method_25306((class_11907)context);
                break;
            }
            case 261: {
                this.removeButton.method_25306((class_11907)context);
                break;
            }
            case 256: 
            case 259: {
                this.doneButton.method_25306((class_11907)context);
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
        context.method_25300(this.field_22787.field_1772, this.bookOffers.getName() + " (" + this.bookOffers.getOffers().size() + ")", this.field_22789 / 2, 12, -1);
        for (class_4068 drawable : this.field_33816) {
            drawable.method_25394(context, mouseX, mouseY, partialTicks);
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
        public ListGui(class_310 minecraft, EditBookOffersScreen screen, List<BookOffer> list) {
            super(minecraft, screen.field_22789, screen.field_22790 - 108, 36, 30);
            list.stream().map(x$0 -> new Entry((BookOffer)x$0)).forEach(x$0 -> this.method_25321((class_350.class_351)x$0));
        }

        public BookOffer getSelectedOffer() {
            Entry entry = (Entry)this.method_25334();
            return entry != null ? entry.bookOffer : null;
        }
    }

    private final class Entry
    extends class_4280.class_4281<Entry> {
        private final BookOffer bookOffer;

        public Entry(BookOffer bookOffer) {
            this.bookOffer = Objects.requireNonNull(bookOffer);
        }

        public class_2561 method_37006() {
            return class_2561.method_43469((String)"narrator.select", (Object[])new Object[]{"Book offer " + this.bookOffer.getEnchantmentNameWithLevel() + ", ID " + this.bookOffer.id() + ", " + this.getPriceText()});
        }

        public void method_25343(class_332 context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int x = this.method_73380();
            int y = this.method_73382();
            class_1792 item = (class_1792)class_7923.field_41178.method_63535(class_2960.method_60654((String)"enchanted_book"));
            class_1799 stack = new class_1799((class_1935)item);
            RenderUtils.drawItem(context, stack, x + 1, y + 1, true);
            class_327 tr = ((EditBookOffersScreen)EditBookOffersScreen.this).field_22787.field_1772;
            String name = this.bookOffer.getEnchantmentNameWithLevel();
            class_6880<class_1887> enchantment = this.bookOffer.getEnchantmentEntry().get();
            int nameColor = enchantment.method_40220(class_9636.field_51551) ? -43691 : -986896;
            context.method_51433(tr, name, x + 28, y, nameColor, false);
            context.method_51433(tr, this.bookOffer.id(), x + 28, y + 9, -6250336, false);
            String price = this.getPriceText();
            context.method_51433(tr, price, x + 28, y + 18, -6250336, false);
            if (this.bookOffer.price() < 64) {
                RenderUtils.drawItem(context, new class_1799((class_1935)class_1802.field_8687), x + 28 + tr.method_1727(price), y + 16, false);
            }
        }

        private String getPriceText() {
            if (this.bookOffer.price() >= 64) {
                return "any price";
            }
            return "max " + this.bookOffer.price();
        }
    }
}
