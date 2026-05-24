package net.wurstclient.mixin;

import com.mojang.brigadier.suggestion.Suggestions;
import java.util.concurrent.CompletableFuture;
import net.minecraft.class_342;
import net.minecraft.class_4717;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.AutoCompleteHack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_4717.class})
public abstract class ChatInputSuggestorMixin {
    @Shadow
    @Final
    private class_342 field_21599;
    @Shadow
    private CompletableFuture<Suggestions> field_21611;

    @Inject(method={"method_23934"}, at={@At(value="TAIL")})
    private void onRefresh(CallbackInfo ci) {
        AutoCompleteHack autoComplete = WurstClient.INSTANCE.getHax().autoCompleteHack;
        if (!autoComplete.isEnabled()) {
            return;
        }
        String draftMessage = this.field_21599.method_1882().substring(0, this.field_21599.method_1881());
        autoComplete.onRefresh(draftMessage, (builder, suggestion) -> {
            this.field_21599.method_1887(suggestion);
            this.field_21611 = builder.buildFuture();
            this.method_23920(false);
        });
    }

    @Shadow
    public abstract void method_23920(boolean var1);
}
