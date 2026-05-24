package net.wurstclient.mixin;

import net.minecraft.class_2561;
import net.minecraft.class_437;
import net.minecraft.class_5375;
import net.wurstclient.WurstClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_5375.class})
public class PackScreenMixin
extends class_437 {
    private PackScreenMixin(WurstClient wurst, class_2561 title) {
        super(title);
    }

    @Inject(method={"method_25419"}, at={@At(value="HEAD")})
    public void onClose(CallbackInfo ci) {
        WurstClient.INSTANCE.getProblematicPackDetector().start();
    }
}
