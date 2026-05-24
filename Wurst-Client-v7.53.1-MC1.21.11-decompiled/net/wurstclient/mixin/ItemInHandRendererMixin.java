package net.wurstclient.mixin;

import net.minecraft.class_11659;
import net.minecraft.class_1268;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_4587;
import net.minecraft.class_742;
import net.minecraft.class_759;
import net.wurstclient.WurstClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_759.class})
public abstract class ItemInHandRendererMixin {
    @Inject(method={"method_3228"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_759;method_3224(Lnet/minecraft/class_4587;Lnet/minecraft/class_1306;F)V", ordinal=3)})
    private void onApplyEquipOffsetBlocking(class_742 player, float tickProgress, float pitch, class_1268 hand, float swingProgress, class_1799 item, float equipProgress, class_4587 matrices, class_11659 entityRenderCommandQueue, int light, CallbackInfo ci) {
        if (item.method_7909() == class_1802.field_8255) {
            WurstClient.INSTANCE.getHax().noShieldOverlayHack.adjustShieldPosition(matrices, true);
        }
    }

    @Inject(method={"method_3228"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_759;method_65816(FLnet/minecraft/class_4587;ILnet/minecraft/class_1306;)V", ordinal=2)})
    private void onApplySwingOffsetNotBlocking(class_742 player, float tickProgress, float pitch, class_1268 hand, float swingProgress, class_1799 item, float equipProgress, class_4587 matrices, class_11659 entityRenderCommandQueue, int light, CallbackInfo ci) {
        if (item.method_7909() == class_1802.field_8255) {
            WurstClient.INSTANCE.getHax().noShieldOverlayHack.adjustShieldPosition(matrices, false);
        }
    }
}
