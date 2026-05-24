package meteordevelopment.meteorclient.utils.tooltip;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.tooltip.MeteorTooltipData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class EntityTooltipComponent
implements MeteorTooltipData,
ClientTooltipComponent {
    protected final LivingEntity entity;
    private static double spin;

    public EntityTooltipComponent(LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public ClientTooltipComponent getComponent() {
        return this;
    }

    public int getHeight(Font textRenderer) {
        return 48;
    }

    public int getWidth(Font textRenderer) {
        return 64;
    }

    public void extractImage(Font textRenderer, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
        LivingEntityRenderState state = (LivingEntityRenderState)MeteorClient.mc.getEntityRenderDispatcher().getRenderer((Entity)this.entity).createRenderState((Entity)this.entity, 1.0f);
        state.lightCoords = 0xF000F0;
        state.shadowPieces.clear();
        state.outlineColor = 0;
        state.bodyRot = (float)(spin % 360.0);
        state.yRot = 0.0f;
        state.xRot = 0.0f;
        x += (width - this.getWidth(null)) / 2;
        y += 4;
        width = this.getWidth(null);
        height = this.getHeight(null);
        float scale = (float)Math.max(width, height) / 2.0f * 1.25f;
        Vector3f translation = new Vector3f(0.0f, 0.1f, 0.0f);
        Quaternionf rotation = new Quaternionf().rotateZ((float)Math.PI);
        graphics.entity((EntityRenderState)state, scale, translation, rotation, null, x, y, x + width, y + height);
        spin += (double)(3.0f * MeteorClient.mc.getDeltaTracker().getGameTimeDeltaTicks());
    }
}
