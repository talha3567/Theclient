package net.wurstclient.mixin.xray;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.List;
import net.minecraft.class_10889;
import net.minecraft.class_1920;
import net.minecraft.class_1935;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_777;
import net.minecraft.class_778;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.XRayHack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value={class_778.class})
public abstract class BlockModelRendererMixin
implements class_1935 {
    private static ThreadLocal<Float> currentOpacity = ThreadLocal.withInitial(() -> Float.valueOf(1.0f));

    @WrapOperation(method={"method_68826"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_2248;method_9607(Lnet/minecraft/class_2680;Lnet/minecraft/class_2680;Lnet/minecraft/class_2350;)Z")})
    private static boolean onRenderSmoothOrFlat(class_2680 state, class_2680 otherState, class_2350 side, Operation<Boolean> original, class_1920 world, class_2680 stateButFromTheOtherMethod, boolean cull, class_2350 sideButFromTheOtherMethod, class_2338 neighborPos) {
        XRayHack xray = WurstClient.INSTANCE.getHax().xRayHack;
        class_2338 pos = neighborPos.method_10093(side.method_10153());
        Boolean shouldDrawSide = xray.shouldDrawSide(state, pos);
        if (!xray.isOpacityMode() || xray.isVisible(state.method_26204(), pos)) {
            currentOpacity.set(Float.valueOf(1.0f));
        } else {
            currentOpacity.set(Float.valueOf(xray.getOpacityFloat()));
        }
        if (shouldDrawSide != null) {
            return shouldDrawSide;
        }
        return (Boolean)original.call(new Object[]{state, otherState, side});
    }

    @ModifyConstant(method={"method_23073"}, constant={@Constant(floatValue=1.0f)})
    private float modifyOpacity(float original) {
        return currentOpacity.get().floatValue();
    }

    @WrapOperation(method={"method_3373", "method_3361"}, at={@At(value="INVOKE", target="Ljava/util/List;isEmpty()Z", ordinal=1)})
    private boolean pretendEmptyToStopSecondRenderModelFaceFlatCall(List<class_777> instance, Operation<Boolean> original, class_1920 world, List<class_10889> list, class_2680 state, class_2338 pos, class_4587 poseStack, class_4588 vertexConsumer, boolean cull, int light) {
        XRayHack xray = WurstClient.INSTANCE.getHax().xRayHack;
        Boolean shouldDrawSide = xray.shouldDrawSide(state, pos);
        if (Boolean.FALSE.equals(shouldDrawSide)) {
            return true;
        }
        return (Boolean)original.call(new Object[]{instance});
    }
}
