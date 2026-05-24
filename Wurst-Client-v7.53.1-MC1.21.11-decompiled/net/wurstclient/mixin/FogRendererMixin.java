package net.wurstclient.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.nio.ByteBuffer;
import net.minecraft.class_758;
import net.wurstclient.WurstClient;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={class_758.class})
public class FogRendererMixin {
    @WrapOperation(method={"method_3211"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_758;method_71110(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V")})
    private void wrapApplyFog(class_758 instance, ByteBuffer buffer, int bufPos, Vector4f fogColor, float environmentalStart, float environmentalEnd, float renderDistanceStart, float renderDistanceEnd, float skyEnd, float cloudEnd, Operation<Void> original) {
        if (WurstClient.INSTANCE.getHax().noFogHack.isEnabled()) {
            renderDistanceStart = 1000000.0f;
            renderDistanceEnd = 1000000.0f;
        }
        original.call(new Object[]{instance, buffer, bufPos, fogColor, Float.valueOf(environmentalStart), Float.valueOf(environmentalEnd), Float.valueOf(renderDistanceStart), Float.valueOf(renderDistanceEnd), Float.valueOf(skyEnd), Float.valueOf(cloudEnd)});
    }
}
