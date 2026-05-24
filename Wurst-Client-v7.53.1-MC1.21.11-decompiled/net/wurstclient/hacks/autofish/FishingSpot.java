package net.wurstclient.hacks.autofish;

import net.minecraft.class_1536;
import net.minecraft.class_243;
import net.wurstclient.hacks.autofish.PositionAndRotation;

public record FishingSpot(PositionAndRotation input, class_243 bobberPos, boolean openWater) {
    public FishingSpot(PositionAndRotation input, class_1536 bobber) {
        this(input, bobber.method_73189(), bobber.method_26088());
    }
}
