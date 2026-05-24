package net.wurstclient.hacks.autolibrarian;

import net.wurstclient.hacks.autolibrarian.BookOffer;
import net.wurstclient.settings.BookOffersSetting;
import net.wurstclient.settings.EnumSetting;

public final class UpdateBooksSetting
extends EnumSetting<UpdateBooks> {
    public UpdateBooksSetting() {
        super("Update books", "Automatically updates the list of wanted books when a villager has learned to sell one of them.\n\n\u00a7lOff\u00a7r - Don't update the list.\n\n\u00a7lRemove\u00a7r - Remove the book from the list so that the next villager will learn a different book.\n\n\u00a7lPrice\u00a7r - Update the maximum price for the book so that the next villager has to sell it for a cheaper price.", (Enum[])UpdateBooks.values(), (Enum)UpdateBooks.REMOVE);
    }

    public static enum UpdateBooks {
        OFF("Off"),
        REMOVE("Remove"),
        PRICE("Price");

        private String name;

        private UpdateBooks(String name) {
            this.name = name;
        }

        public void update(BookOffersSetting wantedBooks, BookOffer offer) {
            int index = wantedBooks.indexOf(offer);
            switch (this.ordinal()) {
                case 0: {
                    return;
                }
                case 1: {
                    wantedBooks.remove(index);
                    break;
                }
                case 2: {
                    if (offer.price() <= 1) {
                        wantedBooks.remove(index);
                        break;
                    }
                    wantedBooks.replace(index, new BookOffer(offer.id(), offer.level(), offer.price() - 1));
                }
            }
        }

        public String toString() {
            return this.name;
        }
    }
}
