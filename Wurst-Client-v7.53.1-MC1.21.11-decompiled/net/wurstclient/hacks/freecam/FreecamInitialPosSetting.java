package net.wurstclient.hacks.freecam;

import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.wurstclient.WurstClient;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.util.text.WText;

public final class FreecamInitialPosSetting
extends EnumSetting<InitialPosition> {
    private static final WText DESCRIPTION = FreecamInitialPosSetting.buildDescription();

    public FreecamInitialPosSetting() {
        super("Initial position", DESCRIPTION, (Enum[])InitialPosition.values(), (Enum)InitialPosition.INSIDE);
    }

    private static WText buildDescription() {
        WText text = WText.translated("description.wurst.setting.freecam.initial_position", new Object[0]);
        for (InitialPosition value : InitialPosition.values()) {
            text = text.append(WText.literal("\n\n\u00a7l" + value.name + ":\u00a7r ")).append(value.description);
        }
        return text;
    }

    public static enum InitialPosition {
        INSIDE("Inside"){

            @Override
            public class_243 getOffset() {
                return class_243.field_1353;
            }
        }
        ,
        IN_FRONT("In Front"){

            @Override
            public class_243 getOffset() {
                double distance = 0.55 * (double)WurstClient.MC.field_1724.method_55693();
                float yawRad = WurstClient.MC.field_1724.method_36454() * ((float)Math.PI / 180);
                double offsetX = (double)(-class_3532.method_15374((double)yawRad)) * distance;
                double offsetZ = (double)class_3532.method_15362((double)yawRad) * distance;
                return new class_243(offsetX, 0.0, offsetZ);
            }
        }
        ,
        ABOVE("Above"){

            @Override
            public class_243 getOffset() {
                double distance = 0.55 * (double)WurstClient.MC.field_1724.method_55693();
                return new class_243(0.0, distance, 0.0);
            }
        };

        private static final String TRANSLATION_KEY_PREFIX = "description.wurst.setting.freecam.initial_position.";
        private final String name;
        private final WText description;

        private InitialPosition(String name) {
            this.name = name;
            this.description = WText.translated(TRANSLATION_KEY_PREFIX + this.name().toLowerCase(), new Object[0]);
        }

        public abstract class_243 getOffset();

        public String toString() {
            return this.name;
        }
    }
}
