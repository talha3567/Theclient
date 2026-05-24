package net.wurstclient.hacks.autofish;

import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.wurstclient.util.Rotation;

public record PositionAndRotation(class_243 pos, Rotation rotation) {
    public PositionAndRotation(class_1297 entity) {
        this(entity.method_73189(), Rotation.wrapped(entity.method_36454(), entity.method_36455()));
    }

    public boolean isNearlyIdenticalTo(PositionAndRotation other) {
        return this.pos.method_1022(other.pos) < 0.5 && this.rotation.getAngleTo(other.rotation) < 5.0;
    }

    public double differenceTo(PositionAndRotation other) {
        return this.pos.method_1022(other.pos) + this.rotation.getAngleTo(other.rotation) / 100.0;
    }
}
