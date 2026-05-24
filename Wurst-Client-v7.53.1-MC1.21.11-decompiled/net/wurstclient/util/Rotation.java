package net.wurstclient.util;

import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2828;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.wurstclient.WurstClient;
import net.wurstclient.util.RotationUtils;
import org.joml.Quaternionf;

public record Rotation(float yaw, float pitch) {
    private static final class_310 MC = WurstClient.MC;

    public void applyToClientPlayer() {
        float adjustedYaw = RotationUtils.limitAngleChange(Rotation.MC.field_1724.method_36454(), this.yaw);
        Rotation.MC.field_1724.method_36456(adjustedYaw);
        Rotation.MC.field_1724.method_36457(this.pitch);
    }

    public void sendPlayerLookPacket() {
        this.sendPlayerLookPacket(Rotation.MC.field_1724.method_24828(), Rotation.MC.field_1724.field_5976);
    }

    public void sendPlayerLookPacket(boolean onGround, boolean horizontalCollision) {
        Rotation.MC.field_1724.field_3944.method_52787((class_2596)new class_2828.class_2831(this.yaw, this.pitch, onGround, horizontalCollision));
    }

    public double getAngleTo(Rotation other) {
        float yaw1 = class_3532.method_15393((float)this.yaw);
        float yaw2 = class_3532.method_15393((float)other.yaw);
        float diffYaw = class_3532.method_15393((float)(yaw1 - yaw2));
        float pitch1 = class_3532.method_15393((float)this.pitch);
        float pitch2 = class_3532.method_15393((float)other.pitch);
        float diffPitch = class_3532.method_15393((float)(pitch1 - pitch2));
        return Math.sqrt(diffYaw * diffYaw + diffPitch * diffPitch);
    }

    public Rotation withYaw(float yaw) {
        return new Rotation(yaw, this.pitch);
    }

    public Rotation withPitch(float pitch) {
        return new Rotation(this.yaw, pitch);
    }

    public class_243 toLookVec() {
        float radPerDeg = (float)Math.PI / 180;
        float pi = (float)Math.PI;
        float adjustedYaw = -class_3532.method_15393((float)this.yaw) * radPerDeg - pi;
        float cosYaw = class_3532.method_15362((double)adjustedYaw);
        float sinYaw = class_3532.method_15374((double)adjustedYaw);
        float adjustedPitch = -class_3532.method_15393((float)this.pitch) * radPerDeg;
        float nCosPitch = -class_3532.method_15362((double)adjustedPitch);
        float sinPitch = class_3532.method_15374((double)adjustedPitch);
        return new class_243((double)(sinYaw * nCosPitch), (double)sinPitch, (double)(cosYaw * nCosPitch));
    }

    public Quaternionf toQuaternion() {
        float radPerDeg = (float)Math.PI / 180;
        float yawRad = -class_3532.method_15393((float)this.yaw) * radPerDeg;
        float pitchRad = class_3532.method_15393((float)this.pitch) * radPerDeg;
        float sinYaw = class_3532.method_15374((double)(yawRad / 2.0f));
        float cosYaw = class_3532.method_15362((double)(yawRad / 2.0f));
        float sinPitch = class_3532.method_15374((double)(pitchRad / 2.0f));
        float cosPitch = class_3532.method_15362((double)(pitchRad / 2.0f));
        float x = sinPitch * cosYaw;
        float y = cosPitch * sinYaw;
        float z = -sinPitch * sinYaw;
        float w = cosPitch * cosYaw;
        return new Quaternionf(x, y, z, w);
    }

    public static Rotation wrapped(float yaw, float pitch) {
        return new Rotation(class_3532.method_15393((float)yaw), class_3532.method_15393((float)pitch));
    }
}
