package net.wurstclient.mixin;

import net.minecraft.class_1297;
import net.minecraft.class_2248;
import net.minecraft.class_2263;
import net.minecraft.class_4970;
import net.minecraft.class_5635;
import net.wurstclient.WurstClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_5635.class})
public abstract class PowderSnowBlockMixin
extends class_2248
implements class_2263 {
    private PowderSnowBlockMixin(WurstClient wurst, class_4970.class_2251 settings) {
        super(settings);
    }

    @Inject(method={"method_32355"}, at={@At(value="HEAD")}, cancellable=true)
    private static void onCanWalkOnPowderSnow(class_1297 entity, CallbackInfoReturnable<Boolean> cir) {
        if (!WurstClient.INSTANCE.getHax().snowShoeHack.isEnabled()) {
            return;
        }
        if (entity == WurstClient.MC.field_1724) {
            cir.setReturnValue((Object)true);
        }
    }
}
