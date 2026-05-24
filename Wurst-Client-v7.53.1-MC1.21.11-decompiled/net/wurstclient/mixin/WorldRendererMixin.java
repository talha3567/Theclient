package net.wurstclient.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_761;
import net.minecraft.class_9779;
import net.minecraft.class_9922;
import net.wurstclient.WurstClient;
import net.wurstclient.event.EventManager;
import net.wurstclient.events.RenderListener;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_761.class})
public class WorldRendererMixin {
    @Inject(method={"method_43788"}, at={@At(value="HEAD")}, cancellable=true)
    private void onHasBlindnessOrDarkness(class_4184 camera, CallbackInfoReturnable<Boolean> ci) {
        if (WurstClient.INSTANCE.getHax().antiBlindHack.isEnabled()) {
            ci.setReturnValue((Object)false);
        }
    }

    @Inject(method={"method_22710"}, at={@At(value="RETURN")})
    private void onRender(class_9922 allocator, class_9779 tickCounter, boolean renderBlockOutline, class_4184 camera, Matrix4f positionMatrix, Matrix4f projectionMatrix, Matrix4f matrix4f2, GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl, CallbackInfo ci) {
        class_4587 matrixStack = new class_4587();
        matrixStack.method_34425((Matrix4fc)positionMatrix);
        float tickProgress = tickCounter.method_60637(false);
        RenderListener.RenderEvent event = new RenderListener.RenderEvent(matrixStack, tickProgress);
        EventManager.fire(event);
    }
}
