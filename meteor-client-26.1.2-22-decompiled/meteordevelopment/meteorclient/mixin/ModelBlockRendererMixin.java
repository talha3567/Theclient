package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.vertex.QuadInstance;
import java.util.List;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Xray;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ModelBlockRenderer.class})
public abstract class ModelBlockRendererMixin {
    @Shadow
    @Final
    private QuadInstance quadInstance;
    @Unique
    private static final ThreadLocal<Integer> ALPHAS = ThreadLocal.withInitial(() -> -1);

    @Inject(method={"tesselateFlat", "tesselateAmbientOcclusion"}, at={@At(value="HEAD")}, cancellable=true)
    private void tesselate$xray(BlockQuadOutput output, float x, float y, float z, List<BlockStateModelPart> parts, BlockAndTintGetter level, BlockState state, BlockPos pos, CallbackInfo ci) {
        int alpha = Xray.getAlpha(state, pos);
        if (alpha == 0) {
            ci.cancel();
        } else {
            ALPHAS.set(alpha);
        }
    }

    @Inject(method={"putQuadWithTint"}, at={@At(value="HEAD")})
    private void putQuadWithTint$xray(BlockQuadOutput output, float x, float y, float z, BlockAndTintGetter level, BlockState state, BlockPos pos, BakedQuad quad, CallbackInfo ci) {
        int alpha = ALPHAS.get();
        if (alpha != -1) {
            this.quadInstance.multiplyColor(ARGB.color((int)alpha, (int)255, (int)255, (int)255));
        }
    }

    @ModifyArg(method={"putQuadWithTint"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/block/BlockQuadOutput;put(FFFLnet/minecraft/client/resources/model/geometry/BakedQuad;Lcom/mojang/blaze3d/vertex/QuadInstance;)V"), index=3)
    private BakedQuad putQuadWithTint$xrayLayer(BakedQuad quad) {
        int alpha = ALPHAS.get();
        if (alpha <= 0 || alpha >= 255) {
            return quad;
        }
        BakedQuad.MaterialInfo materialInfo = quad.materialInfo();
        if (materialInfo.layer() == ChunkSectionLayer.TRANSLUCENT) {
            return quad;
        }
        BakedQuad.MaterialInfo translucentInfo = new BakedQuad.MaterialInfo(materialInfo.sprite(), ChunkSectionLayer.TRANSLUCENT, materialInfo.itemRenderType(), materialInfo.tintIndex(), materialInfo.shade(), materialInfo.lightEmission());
        return new BakedQuad(quad.position0(), quad.position1(), quad.position2(), quad.position3(), quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(), quad.direction(), translucentInfo);
    }

    @ModifyReturnValue(method={"shouldRenderFace"}, at={@At(value="RETURN")})
    private static boolean shouldRenderFace$xray(boolean original, BlockAndTintGetter level, BlockState state, Direction direction, BlockPos neighborPos) {
        Xray xray = Modules.get().get(Xray.class);
        if (xray.isActive()) {
            return xray.modifyDrawSide(state, (BlockGetter)level, neighborPos.relative(direction.getOpposite()), direction, original);
        }
        return original;
    }
}
