package meteordevelopment.meteorclient.systems.modules.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import meteordevelopment.meteorclient.events.render.ApplyTransformationEvent;
import meteordevelopment.meteorclient.events.render.RenderItemEntityEvent;
import meteordevelopment.meteorclient.mixin.ItemStackRenderStateAccessor;
import meteordevelopment.meteorclient.mixin.LayerRenderStateAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class ItemPhysics
extends Module {
    private static final float PIXEL_SIZE = 0.0625f;
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> randomRotation;
    private final RandomSource random;
    private boolean skipTransformation;

    public ItemPhysics() {
        super(Categories.Render, "item-physics", "Applies physics to items on the ground.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.randomRotation = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("random-rotation")).description("Adds a random rotation to every item.")).defaultValue(true)).build());
        this.random = RandomSource.createThreadLocalInstance();
    }

    @EventHandler
    private void onRenderItemEntity(RenderItemEntityEvent event) {
        event.cancel();
        if (event.renderState.item.isEmpty() || event.itemEntity == null) {
            return;
        }
        PoseStack matrices = event.matrixStack;
        this.random.setSeed((long)event.itemEntity.getId() * 89748956L);
        for (int i = 0; i < ((ItemStackRenderStateAccessor)event.renderState.item).meteor$getActiveLayerCount(); ++i) {
            ItemStackRenderState.LayerRenderState layer = ((ItemStackRenderStateAccessor)event.renderState.item).meteor$getLayers()[i];
            ModelInfo info = this.getInfo(layer.prepareQuadList());
            matrices.pushPose();
            this.applyTransformation(matrices, ((LayerRenderStateAccessor)layer).meteor$getTransform());
            matrices.translate(0.0f, info.offsetY, 0.0f);
            this.offsetInWater(matrices, event.itemEntity);
            if (info.flat) {
                matrices.mulPose((Quaternionfc)Axis.XP.rotationDegrees(90.0f));
                matrices.translate(0.0f, 0.0f, info.offsetZ);
            }
            if (this.randomRotation.get().booleanValue()) {
                Axis axis = Axis.YP;
                float x = 0.5f;
                float y = 0.0f;
                float z = 0.5f;
                if (info.flat) {
                    axis = Axis.ZP;
                    y = 0.5f;
                    z = 0.0f;
                }
                float degrees = (this.random.nextFloat() * 2.0f - 1.0f) * 90.0f;
                matrices.translate(x, y, z);
                matrices.mulPose((Quaternionfc)axis.rotationDegrees(degrees));
                matrices.translate(-x, -y, -z);
            }
            this.renderLayer(event, info);
            matrices.popPose();
        }
    }

    @EventHandler
    private void onApplyTransformation(ApplyTransformationEvent event) {
        if (this.skipTransformation) {
            event.cancel();
        }
    }

    private void renderLayer(RenderItemEntityEvent event, ModelInfo info) {
        PoseStack matrices = event.matrixStack;
        this.skipTransformation = true;
        for (int j = 0; j < event.renderState.count; ++j) {
            matrices.pushPose();
            if (j > 0) {
                float x = (this.random.nextFloat() * 2.0f - 1.0f) * 0.25f;
                float z = (this.random.nextFloat() * 2.0f - 1.0f) * 0.25f;
                this.translate(matrices, info, x, 0.0f, z);
            }
            event.renderState.item.submit(matrices, event.renderCommandQueue, event.light, OverlayTexture.NO_OVERLAY, event.renderState.outlineColor);
            matrices.popPose();
            float y = Math.max(this.random.nextFloat() * 0.0625f, 0.03125f);
            this.translate(matrices, info, 0.0f, y, 0.0f);
        }
        this.skipTransformation = false;
    }

    private void translate(PoseStack matrices, ModelInfo info, float x, float y, float z) {
        if (info.flat) {
            float temp = y;
            y = z;
            z = -temp;
        }
        matrices.translate(x, y, z);
    }

    private void applyTransformation(PoseStack matrices, ItemTransform transform) {
        transform = new ItemTransform(transform.rotation(), (Vector3fc)new Vector3f(transform.translation().x(), 0.0f, transform.translation().z()), transform.scale());
        transform.apply(false, matrices.last());
    }

    private void offsetInWater(PoseStack matrices, ItemEntity entity) {
        if (entity.isInWater()) {
            matrices.translate(0.0f, 0.333f, 0.0f);
        }
    }

    private ModelInfo getInfo(List<BakedQuad> quads) {
        float minX = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = Float.MIN_VALUE;
        float minZ = Float.MAX_VALUE;
        float maxZ = Float.MIN_VALUE;
        for (BakedQuad quad : quads) {
            for (int i = 0; i < 4; ++i) {
                Vector3fc vec = quad.position(i);
                minY = Math.min(minY, vec.y());
                maxY = Math.max(maxY, vec.y());
                minZ = Math.min(minZ, vec.z());
                maxZ = Math.max(maxZ, vec.z());
                minX = Math.min(minX, vec.x());
                maxX = Math.max(maxX, vec.x());
            }
        }
        if (minX == Float.MAX_VALUE) {
            minX = 0.0f;
        }
        if (minY == Float.MAX_VALUE) {
            minY = 0.0f;
        }
        if (minZ == Float.MAX_VALUE) {
            minZ = 0.0f;
        }
        if (maxX == Float.MIN_VALUE) {
            maxX = 1.0f;
        }
        if (maxY == Float.MIN_VALUE) {
            maxY = 1.0f;
        }
        if (maxZ == Float.MIN_VALUE) {
            maxZ = 1.0f;
        }
        float x = maxX - minX;
        float y = maxY - minY;
        float z = maxZ - minZ;
        boolean flat = x > 0.0625f && y > 0.0625f && z <= 0.0625f;
        return new ModelInfo(flat, 0.5f - minY, -maxZ);
    }

    record ModelInfo(boolean flat, float offsetY, float offsetZ) {
    }
}
