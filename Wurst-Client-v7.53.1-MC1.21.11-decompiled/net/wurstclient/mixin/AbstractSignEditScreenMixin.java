package net.wurstclient.mixin;

import net.minecraft.class_2561;
import net.minecraft.class_437;
import net.minecraft.class_7743;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.AutoSignHack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_7743.class})
public abstract class AbstractSignEditScreenMixin
extends class_437 {
    @Shadow
    @Final
    private String[] field_40425;

    private AbstractSignEditScreenMixin(WurstClient wurst, class_2561 title) {
        super(title);
    }

    @Inject(method={"method_25426"}, at={@At(value="HEAD")})
    private void onInit(CallbackInfo ci) {
        AutoSignHack autoSignHack = WurstClient.INSTANCE.getHax().autoSignHack;
        String[] autoSignText = autoSignHack.getSignText();
        if (autoSignText == null) {
            return;
        }
        for (int i = 0; i < 4; ++i) {
            this.field_40425[i] = autoSignText[i];
        }
        this.method_45662();
    }

    @Inject(method={"method_45662"}, at={@At(value="HEAD")})
    private void onFinishEditing(CallbackInfo ci) {
        WurstClient.INSTANCE.getHax().autoSignHack.setSignText(this.field_40425);
    }

    @Shadow
    private void method_45662() {
    }
}
