package net.wurstclient.mixin;

import net.minecraft.class_5223;
import net.wurstclient.WurstClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value={class_5223.class})
public abstract class TextVisitFactoryMixin {
    @ModifyArg(method={"method_27472"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_5223;method_27473(Ljava/lang/String;ILnet/minecraft/class_2583;Lnet/minecraft/class_2583;Lnet/minecraft/class_5224;)Z", ordinal=0), index=0)
    private static String adjustText(String text) {
        return WurstClient.INSTANCE.getHax().nameProtectHack.protect(text);
    }
}
