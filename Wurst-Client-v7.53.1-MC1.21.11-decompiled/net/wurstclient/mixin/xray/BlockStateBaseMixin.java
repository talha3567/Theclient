package net.wurstclient.mixin.xray;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.class_1922;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2688;
import net.minecraft.class_2769;
import net.minecraft.class_4970;
import net.wurstclient.WurstClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_4970.class_4971.class})
public abstract class BlockStateBaseMixin
extends class_2688<class_2248, class_2680> {
    private BlockStateBaseMixin(WurstClient wurst, class_2248 owner, Reference2ObjectArrayMap<class_2769<?>, Comparable<?>> propertyMap, MapCodec<class_2680> codec) {
        super((Object)owner, propertyMap, codec);
    }

    @Inject(method={"method_26210"}, at={@At(value="RETURN")}, cancellable=true, order=980)
    private void onGetShadeBrightness(class_1922 blockGetter, class_2338 blockPos, CallbackInfoReturnable<Float> original) {
        if (!WurstClient.INSTANCE.getHax().xRayHack.isEnabled()) {
            return;
        }
        original.setReturnValue((Object)Float.valueOf(1.0f));
    }
}
