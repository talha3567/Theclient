package net.wurstclient.hacks.mobspawnesp;

import java.util.function.Function;
import net.minecraft.class_1299;
import net.minecraft.class_2338;
import net.minecraft.class_310;
import net.wurstclient.WurstClient;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.util.text.WText;

public final class HitboxCheckSetting
extends EnumSetting<HitboxCheck> {
    private static final class_310 MC = WurstClient.MC;
    private static final WText DESCRIPTION = WText.translated("description.wurst.setting.mobspawnesp.hitbox_check", new Object[0]).append(HitboxCheckSetting.buildDescriptionSuffix());

    public HitboxCheckSetting() {
        super("Hitbox check", DESCRIPTION, (Enum[])HitboxCheck.values(), (Enum)HitboxCheck.OFF);
    }

    public boolean isSpaceEmpty(class_2338 pos) {
        return ((HitboxCheck)((Object)this.getSelected())).check.apply(pos);
    }

    private static synchronized boolean slowHitboxCheck(class_2338 pos) {
        return HitboxCheckSetting.unstableHitboxCheck(pos);
    }

    private static boolean unstableHitboxCheck(class_2338 pos) {
        return HitboxCheckSetting.MC.field_1687.method_18026(class_1299.field_6046.method_58629((double)pos.method_10263() + 0.5, (double)pos.method_10264(), (double)pos.method_10260() + 0.5));
    }

    private static WText buildDescriptionSuffix() {
        HitboxCheck[] values;
        WText text = WText.literal("\n\n");
        for (HitboxCheck value : values = HitboxCheck.values()) {
            text.append("\u00a7l" + value.name + "\u00a7r - ").append(value.description).append("\n\n");
        }
        return text;
    }

    public static enum HitboxCheck {
        OFF("Off", pos -> true),
        SLOW("Slow", HitboxCheckSetting::slowHitboxCheck),
        UNSTABLE("Unstable", HitboxCheckSetting::unstableHitboxCheck);

        private static final String TRANSLATION_KEY_PREFIX = "description.wurst.setting.mobspawnesp.hitbox_check.";
        private final String name;
        private final WText description;
        private final Function<class_2338, Boolean> check;

        private HitboxCheck(String name, Function<class_2338, Boolean> check) {
            this.name = name;
            this.description = WText.translated(TRANSLATION_KEY_PREFIX + this.name().toLowerCase(), new Object[0]);
            this.check = check;
        }

        public String toString() {
            return this.name;
        }
    }
}
