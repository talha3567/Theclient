package net.wurstclient.mixin;

import net.minecraft.class_2561;
import net.minecraft.class_420;
import net.minecraft.class_437;
import net.minecraft.class_642;
import net.wurstclient.WurstClient;
import net.wurstclient.util.LastServerRememberer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_420.class})
public class DirectConnectScreenMixin
extends class_437 {
    @Shadow
    @Final
    private class_642 field_2460;

    private DirectConnectScreenMixin(WurstClient wurst, class_2561 title) {
        super(title);
    }

    @Inject(method={"method_2167"}, at={@At(value="TAIL")})
    private void onSaveAndClose(CallbackInfo ci) {
        LastServerRememberer.setLastServer(this.field_2460);
    }
}
