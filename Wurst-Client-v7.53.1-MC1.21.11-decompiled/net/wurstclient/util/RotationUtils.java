package net.wurstclient.util;

import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_746;
import net.wurstclient.RotationFaker;
import net.wurstclient.WurstClient;
import net.wurstclient.util.Rotation;

public final class RotationUtils
extends Enum<RotationUtils> {
    private static final class_310 MC;
    private static final /* synthetic */ RotationUtils[] $VALUES;

    public static RotationUtils[] values() {
        return (RotationUtils[])$VALUES.clone();
    }

    public static RotationUtils valueOf(String name) {
        return Enum.valueOf(RotationUtils.class, name);
    }

    public static class_243 getEyesPos() {
        class_746 player = RotationUtils.MC.field_1724;
        float eyeHeight = player.method_18381(player.method_18376());
        return player.method_73189().method_1031(0.0, (double)eyeHeight, 0.0);
    }

    public static class_243 getClientLookVec(float partialTicks) {
        float yaw = RotationUtils.MC.field_1724.method_5705(partialTicks);
        float pitch = RotationUtils.MC.field_1724.method_5695(partialTicks);
        return new Rotation(yaw, pitch).toLookVec();
    }

    public static class_243 getServerLookVec() {
        RotationFaker rf = WurstClient.INSTANCE.getRotationFaker();
        return new Rotation(rf.getServerYaw(), rf.getServerPitch()).toLookVec();
    }

    public static Rotation getNeededRotations(class_243 vec) {
        class_243 eyes = RotationUtils.getEyesPos();
        double diffX = vec.field_1352 - eyes.field_1352;
        double diffZ = vec.field_1350 - eyes.field_1350;
        double yaw = Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0;
        double diffY = vec.field_1351 - eyes.field_1351;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        double pitch = -Math.toDegrees(Math.atan2(diffY, diffXZ));
        return Rotation.wrapped((float)yaw, (float)pitch);
    }

    public static double getAngleToLookVec(class_243 vec) {
        class_746 player = RotationUtils.MC.field_1724;
        Rotation current = new Rotation(player.method_36454(), player.method_36455());
        Rotation needed = RotationUtils.getNeededRotations(vec);
        return current.getAngleTo(needed);
    }

    public static float getHorizontalAngleToLookVec(class_243 vec) {
        float currentYaw = class_3532.method_15393((float)RotationUtils.MC.field_1724.method_36454());
        float neededYaw = RotationUtils.getNeededRotations(vec).yaw();
        return class_3532.method_15393((float)(currentYaw - neededYaw));
    }

    public static boolean isAlreadyFacing(Rotation rotation) {
        return RotationUtils.getAngleToLastReportedLookVec(rotation) <= 1.0;
    }

    public static double getAngleToLastReportedLookVec(class_243 vec) {
        Rotation needed = RotationUtils.getNeededRotations(vec);
        return RotationUtils.getAngleToLastReportedLookVec(needed);
    }

    public static double getAngleToLastReportedLookVec(Rotation rotation) {
        class_746 player = RotationUtils.MC.field_1724;
        Rotation lastReported = player.method_5765() ? new Rotation(player.method_36454(), player.method_36455()) : new Rotation(player.field_3941, player.field_3925);
        return lastReported.getAngleTo(rotation);
    }

    public static boolean isFacingBox(class_238 box, double range) {
        class_243 start = RotationUtils.getEyesPos();
        class_243 end = start.method_1019(RotationUtils.getServerLookVec().method_1021(range));
        return box.method_992(start, end).isPresent();
    }

    public static Rotation slowlyTurnTowards(Rotation end, float maxChange) {
        class_746 player = RotationUtils.MC.field_1724;
        float startYaw = player.method_5765() ? player.method_36454() : player.field_3941;
        float startPitch = player.method_5765() ? player.method_36455() : player.field_3925;
        float endYaw = end.yaw();
        float endPitch = end.pitch();
        float yawChange = Math.abs(class_3532.method_15393((float)(endYaw - startYaw)));
        float pitchChange = Math.abs(class_3532.method_15393((float)(endPitch - startPitch)));
        float maxChangeYaw = pitchChange == 0.0f ? maxChange : Math.min(maxChange, maxChange * yawChange / pitchChange);
        float maxChangePitch = yawChange == 0.0f ? maxChange : Math.min(maxChange, maxChange * pitchChange / yawChange);
        float nextYaw = RotationUtils.limitAngleChange(startYaw, endYaw, maxChangeYaw);
        float nextPitch = RotationUtils.limitAngleChange(startPitch, endPitch, maxChangePitch);
        return new Rotation(nextYaw, nextPitch);
    }

    public static float limitAngleChange(float current, float intended, float maxChange) {
        float currentWrapped = class_3532.method_15393((float)current);
        float intendedWrapped = class_3532.method_15393((float)intended);
        float change = class_3532.method_15393((float)(intendedWrapped - currentWrapped));
        change = class_3532.method_15363((float)change, (float)(-maxChange), (float)maxChange);
        return current + change;
    }

    public static float limitAngleChange(float current, float intended) {
        float currentWrapped = class_3532.method_15393((float)current);
        float intendedWrapped = class_3532.method_15393((float)intended);
        float change = class_3532.method_15393((float)(intendedWrapped - currentWrapped));
        return current + change;
    }

    private static /* synthetic */ RotationUtils[] $values() {
        return new RotationUtils[0];
    }

    static {
        $VALUES = RotationUtils.$values();
        MC = WurstClient.MC;
    }
}
