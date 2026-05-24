package net.wurstclient.mixin;

import net.minecraft.class_1922;
import net.minecraft.class_2248;
import net.minecraft.class_2266;
import net.minecraft.class_2338;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_3726;
import net.minecraft.class_4970;
import net.wurstclient.WurstClient;
import net.wurstclient.event.EventManager;
import net.wurstclient.events.CactusCollisionShapeListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_2266.class})
public abstract class CactusBlockMixin
extends class_2248 {
    private CactusBlockMixin(WurstClient wurst, class_4970.class_2251 settings) {
        super(settings);
    }

    @Inject(method={"method_9549"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetCollisionShape(class_2680 state, class_1922 world, class_2338 pos, class_3726 context, CallbackInfoReturnable<class_265> cir) {
        CactusCollisionShapeListener.CactusCollisionShapeEvent event = new CactusCollisionShapeListener.CactusCollisionShapeEvent();
        EventManager.fire(event);
        class_265 collisionShape = event.getCollisionShape();
        if (collisionShape != null) {
            cir.setReturnValue((Object)collisionShape);
        }
    }
}
