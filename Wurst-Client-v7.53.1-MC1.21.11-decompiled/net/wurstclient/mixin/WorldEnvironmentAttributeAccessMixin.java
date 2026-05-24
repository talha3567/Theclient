package net.wurstclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.class_12197;
import net.minecraft.class_12204;
import net.minecraft.class_12205;
import net.minecraft.class_12206;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.NoWeatherHack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={class_12205.class})
public abstract class WorldEnvironmentAttributeAccessMixin
implements class_12204 {
    @ModifyReturnValue(method={"method_75694", "method_75696"}, at={@At(value="RETURN")}, require=2)
    public Object onGetAttributeValue(Object original, class_12197<?> attribute) {
        NoWeatherHack noWeather = WurstClient.INSTANCE.getHax().noWeatherHack;
        if (attribute == class_12206.field_64343 && noWeather.isMoonPhaseChanged()) {
            return noWeather.getChangedMoonPhase();
        }
        return original;
    }
}
