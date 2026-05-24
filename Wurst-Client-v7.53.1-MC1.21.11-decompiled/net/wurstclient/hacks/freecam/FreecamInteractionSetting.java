package net.wurstclient.hacks.freecam;

import net.wurstclient.settings.EnumSetting;
import net.wurstclient.util.text.WText;

public final class FreecamInteractionSetting
extends EnumSetting<InteractFrom> {
    private static final WText DESCRIPTION = FreecamInteractionSetting.buildDescription();

    public FreecamInteractionSetting() {
        super("Interact from", DESCRIPTION, (Enum[])InteractFrom.values(), (Enum)InteractFrom.PLAYER);
    }

    private static WText buildDescription() {
        WText text = WText.translated("description.wurst.setting.freecam.interact_from", new Object[0]);
        for (InteractFrom value : InteractFrom.values()) {
            text = text.append(WText.literal("\n\n\u00a7l" + value.name + ":\u00a7r ")).append(value.description);
        }
        return text;
    }

    public static enum InteractFrom {
        CAMERA("Camera"),
        PLAYER("Player");

        private static final String TRANSLATION_KEY_PREFIX = "description.wurst.setting.freecam.interact_from.";
        private final String name;
        private final WText description;

        private InteractFrom(String name) {
            this.name = name;
            this.description = WText.translated(TRANSLATION_KEY_PREFIX + this.name().toLowerCase(), new Object[0]);
        }

        public String toString() {
            return this.name;
        }
    }
}
