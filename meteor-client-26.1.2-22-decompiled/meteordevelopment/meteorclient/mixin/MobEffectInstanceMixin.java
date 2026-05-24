package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.PotionSaver;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={MobEffectInstance.class})
public abstract class MobEffectInstanceMixin {
    @Inject(method={"tickDownDuration"}, at={@At(value="HEAD")}, cancellable=true)
    private void tick(CallbackInfo ci) {
        if (!Utils.canUpdate()) {
            return;
        }
        if (Modules.get().get(PotionSaver.class).shouldFreeze((MobEffect)((MobEffectInstance)this).getEffect().value())) {
            ci.cancel();
        }
    }
}
