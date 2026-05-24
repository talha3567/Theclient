package net.wurstclient.mixin.xray;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.class_1920;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_3610;
import net.minecraft.class_4588;
import net.minecraft.class_775;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.XRayHack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value={class_775.class})
public class FluidRendererMixin {
    @Unique
    private static final ThreadLocal<Float> currentOpacity = ThreadLocal.withInitial(() -> Float.valueOf(1.0f));

    @WrapOperation(method={"method_3347"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_775;method_3344(Lnet/minecraft/class_2350;FLnet/minecraft/class_2680;)Z")})
    private boolean modifyShouldSkipRendering(class_2350 side, float height, class_2680 neighborState, Operation<Boolean> original, class_1920 world, class_2338 pos, class_4588 vertexConsumer, class_2680 blockState, class_3610 fluidState) {
        XRayHack xray = WurstClient.INSTANCE.getHax().xRayHack;
        Boolean shouldDrawSide = xray.shouldDrawSide(blockState, null);
        if (!xray.isOpacityMode() || xray.isVisible(blockState.method_26204(), pos)) {
            currentOpacity.set(Float.valueOf(1.0f));
        } else {
            currentOpacity.set(Float.valueOf(xray.getOpacityFloat()));
        }
        if (shouldDrawSide != null) {
            return shouldDrawSide == false;
        }
        return (Boolean)original.call(new Object[]{side, Float.valueOf(height), neighborState});
    }

    @ModifyConstant(method={"method_23072"}, constant={@Constant(floatValue=1.0f, ordinal=0)})
    private float modifyOpacity(float original) {
        return currentOpacity.get().floatValue();
    }
}
