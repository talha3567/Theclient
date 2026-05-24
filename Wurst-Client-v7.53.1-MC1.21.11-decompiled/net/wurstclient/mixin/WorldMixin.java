package net.wurstclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.class_1936;
import net.minecraft.class_1937;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.NoWeatherHack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_1937.class})
public abstract class WorldMixin
implements class_1936,
AutoCloseable {
    @Inject(method={"method_8430"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetRainGradient(float delta, CallbackInfoReturnable<Float> cir) {
        if (WurstClient.INSTANCE.getHax().noWeatherHack.isRainDisabled()) {
            cir.setReturnValue((Object)Float.valueOf(0.0f));
        }
    }

    @ModifyReturnValue(method={"method_8532"}, at={@At(value="RETURN")})
    public long onGetTimeOfDay(long original) {
        NoWeatherHack noWeather = WurstClient.INSTANCE.getHax().noWeatherHack;
        return noWeather.isTimeChanged() ? noWeather.getChangedTime() : original;
    }
}
