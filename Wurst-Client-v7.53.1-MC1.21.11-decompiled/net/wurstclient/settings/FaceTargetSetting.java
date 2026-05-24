package net.wurstclient.settings;

import java.util.function.Consumer;
import net.minecraft.class_243;
import net.wurstclient.WurstClient;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.util.RotationUtils;
import net.wurstclient.util.text.WText;

public final class FaceTargetSetting
extends EnumSetting<FaceTarget> {
    private static final WurstClient WURST = WurstClient.INSTANCE;
    private static final WText FULL_DESCRIPTION_SUFFIX = FaceTargetSetting.buildDescriptionSuffix(true);
    private static final WText REDUCED_DESCRIPTION_SUFFIX = FaceTargetSetting.buildDescriptionSuffix(false);

    private FaceTargetSetting(WText description, FaceTarget[] values, FaceTarget selected) {
        super("Face target", description, (Enum[])values, (Enum)selected);
    }

    public static FaceTargetSetting withPacketSpam(Hack hack, FaceTarget selected) {
        return FaceTargetSetting.withPacketSpam(FaceTargetSetting.hackDescription(hack), selected);
    }

    public static FaceTargetSetting withPacketSpam(WText description, FaceTarget selected) {
        return new FaceTargetSetting(description.append(FULL_DESCRIPTION_SUFFIX), FaceTarget.values(), selected);
    }

    public static FaceTargetSetting withoutPacketSpam(Hack hack, FaceTarget selected) {
        return FaceTargetSetting.withoutPacketSpam(FaceTargetSetting.hackDescription(hack), selected);
    }

    public static FaceTargetSetting withoutPacketSpam(WText description, FaceTarget selected) {
        FaceTarget[] values = new FaceTarget[]{FaceTarget.OFF, FaceTarget.SERVER, FaceTarget.CLIENT};
        return new FaceTargetSetting(description.append(REDUCED_DESCRIPTION_SUFFIX), values, selected);
    }

    private static WText hackDescription(Hack hack) {
        return WText.translated("description.wurst.setting." + hack.getName().toLowerCase() + ".face_target", new Object[0]);
    }

    public void face(class_243 v) {
        ((FaceTarget)((Object)this.getSelected())).face(v);
    }

    private static WText buildDescriptionSuffix(boolean includePacketSpam) {
        FaceTarget[] values;
        FaceTarget[] faceTargetArray;
        WText text = WText.literal("\n\n");
        if (includePacketSpam) {
            faceTargetArray = FaceTarget.values();
        } else {
            FaceTarget[] faceTargetArray2 = new FaceTarget[3];
            faceTargetArray2[0] = FaceTarget.OFF;
            faceTargetArray2[1] = FaceTarget.SERVER;
            faceTargetArray = faceTargetArray2;
            faceTargetArray2[2] = FaceTarget.CLIENT;
        }
        for (FaceTarget value : values = faceTargetArray) {
            text.append("\u00a7l" + value.name + "\u00a7r - ").append(value.description).append("\n\n");
        }
        return text;
    }

    public static enum FaceTarget {
        OFF("Off", v -> {}),
        SERVER("Server-side", v -> WURST.getRotationFaker().faceVectorPacket((class_243)v)),
        CLIENT("Client-side", v -> WURST.getRotationFaker().faceVectorClient((class_243)v)),
        SPAM("Packet spam", v -> RotationUtils.getNeededRotations(v).sendPlayerLookPacket());

        private static final String TRANSLATION_KEY_PREFIX = "description.wurst.setting.generic.face_target.";
        private final String name;
        private final WText description;
        private final Consumer<class_243> face;

        private FaceTarget(String name, Consumer<class_243> face) {
            this.name = name;
            this.description = WText.translated(TRANSLATION_KEY_PREFIX + this.name().toLowerCase(), new Object[0]);
            this.face = face;
        }

        public void face(class_243 v) {
            this.face.accept(v);
        }

        public String toString() {
            return this.name;
        }
    }
}
