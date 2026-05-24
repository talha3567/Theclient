package net.wurstclient.settings;

import java.util.function.Function;
import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.util.RotationUtils;

public final class AimAtSetting
extends EnumSetting<AimAt> {
    private static final String FULL_DESCRIPTION_SUFFIX = AimAtSetting.buildDescriptionSuffix();

    private AimAtSetting(String name, String description, AimAt[] values, AimAt selected) {
        super(name, description, (Enum[])values, (Enum)selected);
    }

    public AimAtSetting(String name, String description, AimAt selected) {
        this(name, description + FULL_DESCRIPTION_SUFFIX, AimAt.values(), selected);
    }

    public AimAtSetting(String description, AimAt selected) {
        this("Aim at", description, selected);
    }

    public AimAtSetting(String description) {
        this(description, AimAt.AUTO);
    }

    public class_243 getAimPoint(class_1297 e) {
        return ((AimAt)((Object)this.getSelected())).aimFunction.apply(e);
    }

    private static String buildDescriptionSuffix() {
        AimAt[] values;
        StringBuilder builder = new StringBuilder("\n\n");
        for (AimAt value : values = AimAt.values()) {
            builder.append("\u00a7l").append(value.name).append("\u00a7r - ").append(value.description).append("\n\n");
        }
        return builder.toString();
    }

    private static class_243 aimAtClosestPoint(class_1297 e) {
        class_243 eyes;
        class_238 box = e.method_5829();
        if (box.method_1006(eyes = RotationUtils.getEyesPos())) {
            return eyes;
        }
        double clampedX = class_3532.method_15350((double)eyes.field_1352, (double)box.field_1323, (double)box.field_1320);
        double clampedY = class_3532.method_15350((double)eyes.field_1351, (double)box.field_1322, (double)box.field_1325);
        double clampedZ = class_3532.method_15350((double)eyes.field_1350, (double)box.field_1321, (double)box.field_1324);
        return new class_243(clampedX, clampedY, clampedZ);
    }

    private static class_243 aimAtHead(class_1297 e) {
        float eyeHeight = e.method_18381(e.method_18376());
        return e.method_73189().method_1031(0.0, (double)eyeHeight, 0.0);
    }

    private static class_243 aimAtCenter(class_1297 e) {
        return e.method_5829().method_1005();
    }

    private static class_243 aimAtFeet(class_1297 e) {
        return e.method_73189().method_1031(0.0, 0.001, 0.0);
    }

    public static enum AimAt {
        AUTO("Auto", "Aims at the closest point of the target's hitbox.", AimAtSetting::aimAtClosestPoint),
        HEAD("Head", "Aims at the target's eye position.", AimAtSetting::aimAtHead),
        CENTER("Center", "Aims at the center of the target's hitbox.", AimAtSetting::aimAtCenter),
        FEET("Feet", "Aims at the bottom of the target's hitbox.", AimAtSetting::aimAtFeet);

        private final String name;
        private final String description;
        private final Function<class_1297, class_243> aimFunction;

        private AimAt(String name, String description, Function<class_1297, class_243> aimFunction) {
            this.name = name;
            this.description = description;
            this.aimFunction = aimFunction;
        }

        public class_243 getAimPoint(class_1297 e) {
            return this.aimFunction.apply(e);
        }

        public String toString() {
            return this.name;
        }
    }
}
