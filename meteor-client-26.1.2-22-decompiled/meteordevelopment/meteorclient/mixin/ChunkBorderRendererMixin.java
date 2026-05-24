package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.ChunkBorderRenderer;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={ChunkBorderRenderer.class})
public abstract class ChunkBorderRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyExpressionValue(method={"emitGizmos"}, at={@At(value="INVOKE", target="Lnet/minecraft/core/SectionPos;of(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/SectionPos;")})
    private SectionPos emitGizmos$getChunkPos(SectionPos original) {
        Freecam freecam = Modules.get().get(Freecam.class);
        if (!freecam.isActive()) {
            return original;
        }
        float delta = this.minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        return SectionPos.of((int)SectionPos.posToSectionCoord((double)Mth.floor((double)freecam.getX(delta))), (int)SectionPos.posToSectionCoord((double)Mth.floor((double)freecam.getY(delta))), (int)SectionPos.posToSectionCoord((double)Mth.floor((double)freecam.getZ(delta))));
    }
}
