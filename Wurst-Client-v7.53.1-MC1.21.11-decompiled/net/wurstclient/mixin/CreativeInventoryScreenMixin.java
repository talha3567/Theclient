package net.wurstclient.mixin;

import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_2561;
import net.minecraft.class_465;
import net.minecraft.class_481;
import net.wurstclient.WurstClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_481.class})
public abstract class CreativeInventoryScreenMixin
extends class_465<class_481.class_483> {
    private CreativeInventoryScreenMixin(WurstClient wurst, class_481.class_483 screenHandler, class_1661 inventory, class_2561 title) {
        super((class_1703)screenHandler, inventory, title);
    }

    @Inject(method={"method_47419"}, at={@At(value="HEAD")}, cancellable=true)
    private void onShouldShowOperatorTab(class_1657 player, CallbackInfoReturnable<Boolean> cir) {
        if (WurstClient.INSTANCE.isEnabled()) {
            cir.setReturnValue((Object)true);
        }
    }
}
