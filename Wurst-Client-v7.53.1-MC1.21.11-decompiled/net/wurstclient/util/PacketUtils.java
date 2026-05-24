package net.wurstclient.util;

import net.minecraft.class_2828;

public final class PacketUtils
extends Enum<PacketUtils> {
    private static final /* synthetic */ PacketUtils[] $VALUES;

    public static PacketUtils[] values() {
        return (PacketUtils[])$VALUES.clone();
    }

    public static PacketUtils valueOf(String name) {
        return Enum.valueOf(PacketUtils.class, name);
    }

    public static class_2828 modifyPosition(class_2828 packet, double x, double y, double z) {
        if (packet instanceof class_2828.class_2831) {
            return new class_2828.class_2830(x, y, z, packet.method_12271(0.0f), packet.method_12270(0.0f), packet.method_12273(), packet.method_61225());
        }
        if (packet instanceof class_2828.class_5911) {
            return new class_2828.class_2829(x, y, z, packet.method_12273(), packet.method_61225());
        }
        if (packet instanceof class_2828.class_2830) {
            return new class_2828.class_2830(x, y, z, packet.method_12271(0.0f), packet.method_12270(0.0f), packet.method_12273(), packet.method_61225());
        }
        return new class_2828.class_2829(x, y, z, packet.method_12273(), packet.method_61225());
    }

    public static class_2828 modifyRotation(class_2828 packet, float yaw, float pitch) {
        if (packet instanceof class_2828.class_2829) {
            return new class_2828.class_2830(packet.method_12269(0.0), packet.method_12268(0.0), packet.method_12274(0.0), yaw, pitch, packet.method_12273(), packet.method_61225());
        }
        if (packet instanceof class_2828.class_5911) {
            return new class_2828.class_2831(yaw, pitch, packet.method_12273(), packet.method_61225());
        }
        if (packet instanceof class_2828.class_2830) {
            return new class_2828.class_2830(packet.method_12269(0.0), packet.method_12268(0.0), packet.method_12274(0.0), yaw, pitch, packet.method_12273(), packet.method_61225());
        }
        return new class_2828.class_2831(yaw, pitch, packet.method_12273(), packet.method_61225());
    }

    public static class_2828 modifyOnGround(class_2828 packet, boolean onGround) {
        if (packet instanceof class_2828.class_2830) {
            return new class_2828.class_2830(packet.method_12269(0.0), packet.method_12268(0.0), packet.method_12274(0.0), packet.method_12271(0.0f), packet.method_12270(0.0f), onGround, packet.method_61225());
        }
        if (packet instanceof class_2828.class_2829) {
            return new class_2828.class_2829(packet.method_12269(0.0), packet.method_12268(0.0), packet.method_12274(0.0), onGround, packet.method_61225());
        }
        if (packet instanceof class_2828.class_2831) {
            return new class_2828.class_2831(packet.method_12271(0.0f), packet.method_12270(0.0f), onGround, packet.method_61225());
        }
        return new class_2828.class_5911(onGround, packet.method_61225());
    }

    public static class_2828 modifyHorizontalCollision(class_2828 packet, boolean horizontalCollision) {
        if (packet instanceof class_2828.class_2830) {
            return new class_2828.class_2830(packet.method_12269(0.0), packet.method_12268(0.0), packet.method_12274(0.0), packet.method_12271(0.0f), packet.method_12270(0.0f), packet.method_12273(), horizontalCollision);
        }
        if (packet instanceof class_2828.class_2829) {
            return new class_2828.class_2829(packet.method_12269(0.0), packet.method_12268(0.0), packet.method_12274(0.0), packet.method_12273(), horizontalCollision);
        }
        if (packet instanceof class_2828.class_2831) {
            return new class_2828.class_2831(packet.method_12271(0.0f), packet.method_12270(0.0f), packet.method_12273(), horizontalCollision);
        }
        return new class_2828.class_5911(packet.method_12273(), horizontalCollision);
    }

    private static /* synthetic */ PacketUtils[] $values() {
        return new PacketUtils[0];
    }

    static {
        $VALUES = PacketUtils.$values();
    }
}
