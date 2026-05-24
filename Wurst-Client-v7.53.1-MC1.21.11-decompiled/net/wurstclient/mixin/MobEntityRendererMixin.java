package net.wurstclient.mixin;

import net.minecraft.class_927;
import net.wurstclient.WurstClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_927.class})
public abstract class MobEntityRendererMixin {
    @Inject(method={"method_4071"}, at={@At(value="FIELD", target="Lnet/minecraft/class_898;field_4678:Lnet/minecraft/class_1297;", opcode=180, ordinal=0)}, cancellable=true)
    private void onHasLabel(CallbackInfoReturnable<Boolean> cir) {
        if (WurstClient.INSTANCE.getHax().nameTagsHack.shouldForceMobNametags()) {
            cir.setReturnValue((Object)true);
        }
    }
}
