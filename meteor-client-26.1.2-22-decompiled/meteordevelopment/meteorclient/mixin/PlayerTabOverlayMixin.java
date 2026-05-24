package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import java.util.List;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.BetterTab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={PlayerTabOverlay.class})
public abstract class PlayerTabOverlayMixin {
    @Shadow
    protected abstract List<PlayerInfo> getPlayerInfos();

    @ModifyConstant(constant={@Constant(longValue=80L)}, method={"getPlayerInfos"})
    private long modifyCount(long count) {
        BetterTab module = Modules.get().get(BetterTab.class);
        return module.isActive() ? (long)module.tabSize.get().intValue() : count;
    }

    @Inject(method={"getNameForDisplay"}, at={@At(value="HEAD")}, cancellable=true)
    public void getNameForDisplay(PlayerInfo info, CallbackInfoReturnable<Component> cir) {
        BetterTab betterTab = Modules.get().get(BetterTab.class);
        if (betterTab.isActive()) {
            cir.setReturnValue((Object)betterTab.getPlayerName(info));
        }
    }

    @ModifyArg(method={"extractRenderState"}, at=@At(value="INVOKE", target="Ljava/lang/Math;min(II)I"), index=0)
    private int modifyWidth(int width) {
        BetterTab module = Modules.get().get(BetterTab.class);
        return module.isActive() && module.accurateLatency.get() != false ? width + 30 : width;
    }

    @Inject(method={"extractRenderState"}, at={@At(value="INVOKE", target="Ljava/lang/Math;min(II)I", shift=At.Shift.BEFORE)})
    private void modifyHeight(CallbackInfo ci, @Local(name={"rows"}) LocalIntRef rows, @Local(name={"cols"}) LocalIntRef cols) {
        int newRows;
        BetterTab module = Modules.get().get(BetterTab.class);
        if (!module.isActive()) {
            return;
        }
        int newCols = 1;
        int totalPlayers = newRows = this.getPlayerInfos().size();
        while (newRows > module.tabHeight.get()) {
            newRows = (totalPlayers + ++newCols - 1) / newCols;
        }
        rows.set(newRows);
        cols.set(newCols);
    }

    @Inject(method={"extractPingIcon"}, at={@At(value="HEAD")}, cancellable=true)
    private void onExtractPingIcon(GuiGraphicsExtractor graphics, int slotWidth, int xo, int yo, PlayerInfo info, CallbackInfo ci) {
        BetterTab betterTab = Modules.get().get(BetterTab.class);
        if (betterTab.isActive() && betterTab.accurateLatency.get().booleanValue()) {
            Minecraft mc = Minecraft.getInstance();
            Font font = mc.font;
            int latency = Mth.clamp((int)info.getLatency(), (int)0, (int)9999);
            int color = latency < 150 ? -16717456 : (latency < 300 ? -1585120 : -2670024);
            String text = latency + "ms";
            graphics.text(font, text, xo + slotWidth - font.width(text), yo, color);
            ci.cancel();
        }
    }
}
