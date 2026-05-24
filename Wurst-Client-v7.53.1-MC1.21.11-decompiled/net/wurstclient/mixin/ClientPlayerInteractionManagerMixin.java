package net.wurstclient.mixin;

import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2846;
import net.minecraft.class_2885;
import net.minecraft.class_310;
import net.minecraft.class_3965;
import net.minecraft.class_636;
import net.minecraft.class_638;
import net.minecraft.class_7204;
import net.minecraft.class_746;
import net.wurstclient.event.EventManager;
import net.wurstclient.events.BlockBreakingProgressListener;
import net.wurstclient.events.PlayerAttacksEntityListener;
import net.wurstclient.events.StopUsingItemListener;
import net.wurstclient.mixinterface.IClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_636.class})
public abstract class ClientPlayerInteractionManagerMixin
implements IClientPlayerInteractionManager {
    @Shadow
    @Final
    private class_310 field_3712;

    @Inject(method={"method_2902"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_746;method_5628()I", ordinal=0)})
    private void onPlayerDamageBlock(class_2338 pos, class_2350 direction, CallbackInfoReturnable<Boolean> cir) {
        EventManager.fire(new BlockBreakingProgressListener.BlockBreakingProgressEvent(pos, direction));
    }

    @Inject(method={"method_2897"}, at={@At(value="HEAD")})
    private void onStopUsingItem(class_1657 player, CallbackInfo ci) {
        EventManager.fire(StopUsingItemListener.StopUsingItemEvent.INSTANCE);
    }

    @Inject(method={"method_2918"}, at={@At(value="HEAD")})
    private void onAttackEntity(class_1657 player, class_1297 target, CallbackInfo ci) {
        if (player != this.field_3712.field_1724) {
            return;
        }
        EventManager.fire(new PlayerAttacksEntityListener.PlayerAttacksEntityEvent(target));
    }

    @Override
    public void windowClick_PICKUP(int slot) {
        this.method_2906(0, slot, 0, class_1713.field_7790, (class_1657)this.field_3712.field_1724);
    }

    @Override
    public void windowClick_QUICK_MOVE(int slot) {
        this.method_2906(0, slot, 0, class_1713.field_7794, (class_1657)this.field_3712.field_1724);
    }

    @Override
    public void windowClick_THROW(int slot) {
        this.method_2906(0, slot, 1, class_1713.field_7795, (class_1657)this.field_3712.field_1724);
    }

    @Override
    public void windowClick_SWAP(int from, int to) {
        this.method_2906(0, from, to, class_1713.field_7791, (class_1657)this.field_3712.field_1724);
    }

    @Override
    public void rightClickItem() {
        this.method_2919((class_1657)this.field_3712.field_1724, class_1268.field_5808);
    }

    @Override
    public void rightClickBlock(class_2338 pos, class_2350 side, class_243 hitVec) {
        class_3965 hitResult = new class_3965(hitVec, side, pos, false);
        class_1268 hand = class_1268.field_5808;
        this.method_2896(this.field_3712.field_1724, hand, hitResult);
        this.method_2919((class_1657)this.field_3712.field_1724, hand);
    }

    @Override
    public void sendPlayerActionC2SPacket(class_2846.class_2847 action, class_2338 blockPos, class_2350 direction) {
        this.method_41931(this.field_3712.field_1687, i -> new class_2846(action, blockPos, direction, i));
    }

    @Override
    public void sendPlayerInteractBlockPacket(class_1268 hand, class_3965 blockHitResult) {
        this.method_41931(this.field_3712.field_1687, i -> new class_2885(hand, blockHitResult, i));
    }

    @Shadow
    private void method_41931(class_638 world, class_7204 packetCreator) {
    }

    @Shadow
    public abstract class_1269 method_2896(class_746 var1, class_1268 var2, class_3965 var3);

    @Shadow
    public abstract class_1269 method_2919(class_1657 var1, class_1268 var2);

    @Shadow
    public abstract void method_2906(int var1, int var2, int var3, class_1713 var4, class_1657 var5);
}
