package meteordevelopment.meteorclient.utils.render.postprocess;

import meteordevelopment.meteorclient.renderer.MeshRenderer;
import meteordevelopment.meteorclient.renderer.MeteorRenderPipelines;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.ESP;
import meteordevelopment.meteorclient.utils.render.postprocess.EntityShader;
import meteordevelopment.meteorclient.utils.render.postprocess.OutlineUniforms;
import net.minecraft.world.entity.Entity;

public class EntityOutlineShader
extends EntityShader {
    private static ESP esp;

    public EntityOutlineShader() {
        super(MeteorRenderPipelines.POST_OUTLINE);
    }

    @Override
    protected boolean shouldDraw() {
        if (esp == null) {
            esp = Modules.get().get(ESP.class);
        }
        return esp.isShader();
    }

    @Override
    public boolean shouldDraw(Entity entity) {
        if (!this.shouldDraw()) {
            return false;
        }
        return !esp.shouldSkip(entity);
    }

    @Override
    protected void setupPass(MeshRenderer renderer) {
        renderer.uniform("OutlineData", OutlineUniforms.write(EntityOutlineShader.esp.outlineWidth.get(), EntityOutlineShader.esp.fillOpacity.get().floatValue(), EntityOutlineShader.esp.shapeMode.get().ordinal(), EntityOutlineShader.esp.glowMultiplier.get().floatValue()));
    }
}
