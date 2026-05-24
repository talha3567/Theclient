package meteordevelopment.meteorclient.mixin.sodium;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value={SodiumWorldRenderer.class})
public abstract class SodiumWorldRendererMixin {
    @Unique
    private static final FogParameters DISABLED_FOG = new FogParameters(0.0f, 0.0f, 0.0f, 0.0f, Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);

    @ModifyVariable(method={"setupTerrain"}, at=@At(value="HEAD"), argsOnly=true, name={"fogParameters"})
    private FogParameters modifyFogParameters(FogParameters fogParameters) {
        if (Modules.get() == null) {
            return fogParameters;
        }
        if (Modules.get().get(NoRender.class).noFog()) {
            return DISABLED_FOG;
        }
        return fogParameters;
    }
}
