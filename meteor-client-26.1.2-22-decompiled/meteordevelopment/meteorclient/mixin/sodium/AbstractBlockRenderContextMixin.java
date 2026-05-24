package meteordevelopment.meteorclient.mixin.sodium;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Xray;
import net.caffeinemc.mods.sodium.client.render.model.AbstractBlockRenderContext;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={AbstractBlockRenderContext.class}, remap=false)
public abstract class AbstractBlockRenderContextMixin {
    @Shadow
    protected BlockState state;
    @Shadow
    protected BlockAndTintGetter level;
    @Shadow
    protected BlockPos pos;
    @Unique
    private Xray xray;

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void onInit(CallbackInfo ci) {
        this.xray = Modules.get().get(Xray.class);
    }

    @Inject(method={"shouldDrawSide"}, at={@At(value="HEAD")}, cancellable=true)
    private void meteor$forceXrayFace(Direction facing, CallbackInfoReturnable<Boolean> cir) {
        if (this.xray != null && this.xray.isActive() && !this.xray.isBlocked(this.state.getBlock(), null)) {
            cir.setReturnValue((Object)true);
        }
    }

    @ModifyReturnValue(method={"shouldDrawSide"}, at={@At(value="RETURN")})
    private boolean shouldDrawSide(boolean original, Direction facing) {
        if (this.xray.isActive()) {
            return this.xray.modifyDrawSide(this.state, (BlockGetter)this.level, this.pos, facing, original);
        }
        return original;
    }
}
