package net.wurstclient.settings;

import java.util.function.Consumer;
import net.minecraft.class_1268;
import net.minecraft.class_2596;
import net.minecraft.class_2879;
import net.minecraft.class_310;
import net.wurstclient.WurstClient;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.util.text.WText;

public final class SwingHandSetting
extends EnumSetting<SwingHand> {
    private static final class_310 MC = WurstClient.MC;
    private static final WText FULL_DESCRIPTION_SUFFIX = SwingHandSetting.buildDescriptionSuffix(true);
    private static final WText REDUCED_DESCRIPTION_SUFFIX = SwingHandSetting.buildDescriptionSuffix(false);

    private SwingHandSetting(WText description, SwingHand[] values, SwingHand selected) {
        super("Swing hand", description, (Enum[])values, (Enum)selected);
    }

    public SwingHandSetting(WText description, SwingHand selected) {
        this(description.append(FULL_DESCRIPTION_SUFFIX), SwingHand.values(), selected);
    }

    public SwingHandSetting(Hack hack, SwingHand selected) {
        this(SwingHandSetting.hackDescription(hack), selected);
    }

    public static SwingHandSetting withoutOffOption(WText description, SwingHand selected) {
        SwingHand[] values = new SwingHand[]{SwingHand.SERVER, SwingHand.CLIENT};
        return new SwingHandSetting(description.append(REDUCED_DESCRIPTION_SUFFIX), values, selected);
    }

    public static SwingHandSetting withoutOffOption(Hack hack, SwingHand selected) {
        return SwingHandSetting.withoutOffOption(SwingHandSetting.hackDescription(hack), selected);
    }

    public static WText genericMiningDescription(Hack hack) {
        return WText.translated("description.wurst.setting.generic.swing_hand_mining", hack.getName());
    }

    public static WText genericCombatDescription(Hack hack) {
        return WText.translated("description.wurst.setting.generic.swing_hand_combat", hack.getName());
    }

    private static WText hackDescription(Hack hack) {
        return WText.translated("description.wurst.setting." + hack.getName().toLowerCase() + ".swing_hand", new Object[0]);
    }

    public void swing(class_1268 hand) {
        ((SwingHand)((Object)this.getSelected())).swing(hand);
    }

    private static WText buildDescriptionSuffix(boolean includeOff) {
        SwingHand[] values;
        SwingHand[] swingHandArray;
        WText text = WText.literal("\n\n");
        if (includeOff) {
            swingHandArray = SwingHand.values();
        } else {
            SwingHand[] swingHandArray2 = new SwingHand[2];
            swingHandArray2[0] = SwingHand.SERVER;
            swingHandArray = swingHandArray2;
            swingHandArray2[1] = SwingHand.CLIENT;
        }
        for (SwingHand value : values = swingHandArray) {
            text.append("\u00a7l" + value.name + "\u00a7r - ").append(value.description).append("\n\n");
        }
        return text;
    }

    public static enum SwingHand {
        OFF("Off", hand -> {}),
        SERVER("Server-side", hand -> SwingHandSetting.MC.field_1724.field_3944.method_52787((class_2596)new class_2879(hand))),
        CLIENT("Client-side", hand -> SwingHandSetting.MC.field_1724.method_6104(hand));

        private static final String TRANSLATION_KEY_PREFIX = "description.wurst.setting.generic.swing_hand.";
        private final String name;
        private final WText description;
        private final Consumer<class_1268> swing;

        private SwingHand(String name, Consumer<class_1268> swing) {
            this.name = name;
            this.description = WText.translated(TRANSLATION_KEY_PREFIX + this.name().toLowerCase(), new Object[0]);
            this.swing = swing;
        }

        public void swing(class_1268 hand) {
            this.swing.accept(hand);
        }

        public String toString() {
            return this.name;
        }
    }
}
