package net.wurstclient.util;

import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_243;

public record RegionPos(int x, int z) {
    public static RegionPos of(class_2338 pos) {
        return new RegionPos(pos.method_10263() >> 9 << 9, pos.method_10260() >> 9 << 9);
    }

    public static RegionPos of(class_1923 pos) {
        return new RegionPos(pos.field_9181 >> 5 << 9, pos.field_9180 >> 5 << 9);
    }

    public RegionPos negate() {
        return new RegionPos(-this.x, -this.z);
    }

    public class_243 toVec3d() {
        return new class_243((double)this.x, 0.0, (double)this.z);
    }

    public class_2338 toBlockPos() {
        return new class_2338(this.x, 0, this.z);
    }
}
