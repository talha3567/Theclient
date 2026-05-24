package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import meteordevelopment.meteorclient.systems.modules.world.Ambience;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value={FogRenderer.class})
public abstract class FogRendererMixin {
    @ModifyVariable(method={"updateBuffer(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"}, at=@At(value="HEAD"), argsOnly=true, name={"fogColor"})
    private Vector4f modifyFogDistance(Vector4f fogColor) {
        if (Modules.get() == null) {
            return fogColor;
        }
        Ambience ambience = Modules.get().get(Ambience.class);
        if (ambience.isActive() && ambience.customFogColor.get().booleanValue()) {
            return ambience.fogColor.get().getVec4f();
        }
        return fogColor;
    }

    @ModifyExpressionValue(method={"getBuffer"}, at={@At(value="FIELD", target="Lnet/minecraft/client/renderer/fog/FogRenderer;fogEnabled:Z", opcode=178)})
    private boolean modifyFogEnabled(boolean original) {
        if (Modules.get() == null) {
            return original;
        }
        return original && !Modules.get().get(NoRender.class).noFog();
    }
}
