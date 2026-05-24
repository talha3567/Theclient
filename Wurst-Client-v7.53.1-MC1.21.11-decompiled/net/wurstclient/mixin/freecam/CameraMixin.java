package net.wurstclient.mixin.freecam;

import net.minecraft.class_11200;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_4184;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.FreecamHack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_4184.class})
public abstract class CameraMixin
implements class_11200.class_11297 {
    @Shadow
    private boolean field_18719;

    @Inject(method={"method_19321"}, at={@At(value="RETURN")}, cancellable=false)
    public void onSetup(class_1937 level, class_1297 entity, boolean bl, boolean bl2, float partialTicks, CallbackInfo ci) {
        FreecamHack freecam = WurstClient.INSTANCE.getHax().freecamHack;
        if (!freecam.isEnabled()) {
            return;
        }
        this.field_18719 = true;
        this.method_19322(freecam.getCamPos(partialTicks));
        this.method_19325(freecam.getCamYaw(), freecam.getCamPitch());
    }

    @Shadow
    protected abstract void method_19322(class_243 var1);

    @Shadow
    protected abstract void method_19325(float var1, float var2);
}
