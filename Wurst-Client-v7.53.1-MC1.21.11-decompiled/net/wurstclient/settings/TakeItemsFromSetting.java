package net.wurstclient.settings;

import net.wurstclient.hack.Hack;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.util.text.WText;

public final class TakeItemsFromSetting
extends EnumSetting<TakeItemsFrom> {
    private static final WText FULL_DESCRIPTION_SUFFIX = TakeItemsFromSetting.buildDescriptionSuffix(true);
    private static final WText REDUCED_DESCRIPTION_SUFFIX = TakeItemsFromSetting.buildDescriptionSuffix(false);

    private TakeItemsFromSetting(String name, WText description, TakeItemsFrom[] values, TakeItemsFrom selected) {
        super(name, description, (Enum[])values, (Enum)selected);
    }

    public static TakeItemsFromSetting withHands(Hack hack, TakeItemsFrom selected) {
        return TakeItemsFromSetting.withHands(TakeItemsFromSetting.hackDescription(hack), selected);
    }

    public static TakeItemsFromSetting withHands(WText description, TakeItemsFrom selected) {
        return new TakeItemsFromSetting("Take items from", description.append(FULL_DESCRIPTION_SUFFIX), TakeItemsFrom.values(), selected);
    }

    public static TakeItemsFromSetting withoutHands(Hack hack, TakeItemsFrom selected) {
        return TakeItemsFromSetting.withoutHands(TakeItemsFromSetting.hackDescription(hack), selected);
    }

    public static TakeItemsFromSetting withoutHands(WText description, TakeItemsFrom selected) {
        TakeItemsFrom[] values = new TakeItemsFrom[]{TakeItemsFrom.HOTBAR, TakeItemsFrom.INVENTORY};
        return new TakeItemsFromSetting("Take items from", description.append(REDUCED_DESCRIPTION_SUFFIX), values, selected);
    }

    private static WText hackDescription(Hack hack) {
        return WText.translated("description.wurst.setting." + hack.getName().toLowerCase() + ".take_items_from", new Object[0]);
    }

    public int getMaxInvSlot() {
        return ((TakeItemsFrom)((Object)this.getSelected())).maxInvSlot;
    }

    private static WText buildDescriptionSuffix(boolean includeHands) {
        TakeItemsFrom[] values;
        TakeItemsFrom[] takeItemsFromArray;
        WText text = WText.literal("\n\n");
        if (includeHands) {
            takeItemsFromArray = TakeItemsFrom.values();
        } else {
            TakeItemsFrom[] takeItemsFromArray2 = new TakeItemsFrom[2];
            takeItemsFromArray2[0] = TakeItemsFrom.HOTBAR;
            takeItemsFromArray = takeItemsFromArray2;
            takeItemsFromArray2[1] = TakeItemsFrom.INVENTORY;
        }
        for (TakeItemsFrom value : values = takeItemsFromArray) {
            text.append("\u00a7l" + value.name + "\u00a7r - ").append(value.description).append("\n\n");
        }
        return text;
    }

    public static enum TakeItemsFrom {
        HANDS("Hands", 0),
        HOTBAR("Hotbar", 9),
        INVENTORY("Inventory", 36);

        private static final String TRANSLATION_KEY_PREFIX = "description.wurst.setting.generic.take_items_from.";
        private final String name;
        private final WText description;
        private final int maxInvSlot;

        private TakeItemsFrom(String name, int maxInvSlot) {
            this.name = name;
            this.description = WText.translated(TRANSLATION_KEY_PREFIX + this.name().toLowerCase(), new Object[0]);
            this.maxInvSlot = maxInvSlot;
        }

        public int getMaxInvSlot() {
            return this.maxInvSlot;
        }

        public String toString() {
            return this.name;
        }
    }
}
