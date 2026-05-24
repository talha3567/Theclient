package net.wurstclient.mixin;

import net.minecraft.class_1076;
import net.minecraft.class_3300;
import net.minecraft.class_4013;
import net.wurstclient.WurstClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_1076.class})
public abstract class LanguageManagerMixin
implements class_4013 {
    @Inject(method={"method_14491"}, at={@At(value="HEAD")})
    private void onReload(class_3300 manager, CallbackInfo ci) {
        WurstClient.INSTANCE.getTranslator().method_14491(manager);
    }
}
