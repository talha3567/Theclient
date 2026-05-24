package net.wurstclient.mixin;

import java.util.Optional;
import net.minecraft.class_6368;
import net.minecraft.class_6369;
import net.minecraft.class_6370;
import net.minecraft.class_6371;
import net.minecraft.class_639;
import net.wurstclient.WurstClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_6370.class})
public class AllowedAddressResolverMixin {
    @Shadow
    @Final
    private class_6369 field_33746;
    @Shadow
    @Final
    private class_6371 field_33747;

    @Inject(method={"method_36907"}, at={@At(value="HEAD")}, cancellable=true)
    public void resolve(class_639 address, CallbackInfoReturnable<Optional<class_6368>> cir) {
        if (!WurstClient.INSTANCE.isEnabled()) {
            return;
        }
        Optional optionalAddress = this.field_33746.resolve(address);
        Optional optionalRedirect = this.field_33747.lookupRedirect(address);
        if (optionalRedirect.isPresent()) {
            optionalAddress = this.field_33746.resolve((class_639)optionalRedirect.get());
        }
        cir.setReturnValue((Object)optionalAddress);
    }
}
