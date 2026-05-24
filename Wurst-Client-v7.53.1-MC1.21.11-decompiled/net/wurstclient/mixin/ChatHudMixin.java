package net.wurstclient.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_303;
import net.minecraft.class_338;
import net.minecraft.class_7469;
import net.minecraft.class_7591;
import net.wurstclient.WurstClient;
import net.wurstclient.event.EventManager;
import net.wurstclient.events.ChatInputListener;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_338.class})
public class ChatHudMixin {
    @Shadow
    @Final
    private List<class_303.class_7590> field_2064;

    @Inject(method={"method_44811"}, at={@At(value="HEAD")}, cancellable=true)
    private void onAddMessage(class_2561 messageDontUse, @Nullable class_7469 signature, @Nullable class_7591 indicatorDontUse, CallbackInfo ci, @Local(argsOnly=true) LocalRef<class_2561> message, @Local(argsOnly=true) LocalRef<class_7591> indicator) {
        ChatInputListener.ChatInputEvent event = new ChatInputListener.ChatInputEvent((class_2561)message.get(), this.field_2064);
        EventManager.fire(event);
        if (event.isCancelled()) {
            ci.cancel();
            return;
        }
        message.set((Object)event.getComponent());
        indicator.set((Object)WurstClient.INSTANCE.getOtfs().noChatReportsOtf.modifyIndicator((class_2561)message.get(), signature, (class_7591)indicator.get()));
    }
}
