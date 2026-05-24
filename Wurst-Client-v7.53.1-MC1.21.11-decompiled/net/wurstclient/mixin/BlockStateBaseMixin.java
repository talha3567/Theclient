package net.wurstclient.mixin;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.class_1922;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_259;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_2688;
import net.minecraft.class_2769;
import net.minecraft.class_3610;
import net.minecraft.class_3726;
import net.minecraft.class_4970;
import net.wurstclient.WurstClient;
import net.wurstclient.event.EventManager;
import net.wurstclient.events.IsNormalCubeListener;
import net.wurstclient.hack.HackList;
import net.wurstclient.hacks.HandNoClipHack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_4970.class_4971.class})
public abstract class BlockStateBaseMixin
extends class_2688<class_2248, class_2680> {
    private BlockStateBaseMixin(WurstClient wurst, class_2248 owner, Reference2ObjectArrayMap<class_2769<?>, Comparable<?>> propertyMap, MapCodec<class_2680> codec) {
        super((Object)owner, propertyMap, codec);
    }

    @Inject(method={"method_26234"}, at={@At(value="TAIL")}, cancellable=true)
    private void onIsFullCube(class_1922 world, class_2338 pos, CallbackInfoReturnable<Boolean> cir) {
        IsNormalCubeListener.IsNormalCubeEvent event = new IsNormalCubeListener.IsNormalCubeEvent();
        EventManager.fire(event);
        cir.setReturnValue((Object)((Boolean)cir.getReturnValue() != false && !event.isCancelled() ? 1 : 0));
    }

    @Inject(method={"method_26172"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetOutlineShape(class_1922 view, class_2338 pos, class_3726 context, CallbackInfoReturnable<class_265> cir) {
        if (context == class_3726.method_16194()) {
            return;
        }
        HackList hax = WurstClient.INSTANCE.getHax();
        if (hax == null) {
            return;
        }
        HandNoClipHack handNoClipHack = hax.handNoClipHack;
        if (!handNoClipHack.isEnabled() || handNoClipHack.isBlockInList(pos)) {
            return;
        }
        cir.setReturnValue((Object)class_259.method_1073());
    }

    @Inject(method={"method_26194"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetCollisionShape(class_1922 world, class_2338 pos, class_3726 context, CallbackInfoReturnable<class_265> cir) {
        if (this.method_26227().method_15769()) {
            return;
        }
        HackList hax = WurstClient.INSTANCE.getHax();
        if (hax == null || !hax.jesusHack.shouldBeSolid()) {
            return;
        }
        cir.setReturnValue((Object)class_259.method_1077());
        cir.cancel();
    }

    @Shadow
    public abstract class_3610 method_26227();
}
