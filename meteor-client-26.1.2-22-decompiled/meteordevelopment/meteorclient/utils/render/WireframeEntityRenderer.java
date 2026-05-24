package meteordevelopment.meteorclient.utils.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.mixin.RenderTypeAccessor;
import meteordevelopment.meteorclient.renderer.Renderer3D;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.utils.render.NoopImmediateVertexConsumerProvider;
import meteordevelopment.meteorclient.utils.render.NoopOutlineVertexConsumerProvider;
import meteordevelopment.meteorclient.utils.render.NoopVertexConsumer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class WireframeEntityRenderer {
    private static final PoseStack matrices = new PoseStack();
    private static Renderer3D renderer;
    private static final SubmitNodeStorage renderCommandQueue;
    private static final FeatureRenderDispatcher renderDispatcher;
    private static Color sideColor;
    private static Color lineColor;
    private static ShapeMode shapeMode;
    private static double offsetX;
    private static double offsetY;
    private static double offsetZ;

    private WireframeEntityRenderer() {
    }

    public static void render(Render3DEvent event, Entity entity, double scale, Color sideColor, Color lineColor, ShapeMode shapeMode) {
        renderer = event.renderer;
        WireframeEntityRenderer.sideColor = sideColor;
        WireframeEntityRenderer.lineColor = lineColor;
        WireframeEntityRenderer.shapeMode = shapeMode;
        float tickDelta = MeteorClient.mc.level.tickRateManager().isFrozen() ? 1.0f : event.tickDelta;
        offsetX = Mth.lerp((double)tickDelta, (double)entity.xOld, (double)entity.getX());
        offsetY = Mth.lerp((double)tickDelta, (double)entity.yOld, (double)entity.getY());
        offsetZ = Mth.lerp((double)tickDelta, (double)entity.zOld, (double)entity.getZ());
        EntityRenderer renderer = MeteorClient.mc.getEntityRenderDispatcher().getRenderer(entity);
        EntityRenderState state = renderer.createRenderState(entity, tickDelta);
        Vec3 entityOffset = renderer.getRenderOffset(state);
        offsetX += entityOffset.x;
        offsetY += entityOffset.y;
        offsetZ += entityOffset.z;
        matrices.pushPose();
        matrices.scale((float)scale, (float)scale, (float)scale);
        renderer.submit(state, matrices, (SubmitNodeCollector)renderCommandQueue, MeteorClient.mc.gameRenderer.getGameRenderState().levelRenderState.cameraRenderState);
        matrices.popPose();
        renderDispatcher.renderAllFeatures();
        renderCommandQueue.endFrame();
    }

    static {
        renderCommandQueue = new SubmitNodeStorage();
        renderDispatcher = new FeatureRenderDispatcher(renderCommandQueue, MeteorClient.mc.getModelManager(), (MultiBufferSource.BufferSource)MyVertexConsumerProvider.INSTANCE, MeteorClient.mc.getAtlasManager(), (OutlineBufferSource)NoopOutlineVertexConsumerProvider.INSTANCE, (MultiBufferSource.BufferSource)NoopImmediateVertexConsumerProvider.INSTANCE, MeteorClient.mc.font, MeteorClient.mc.gameRenderer.getGameRenderState());
    }

    private static class MyVertexConsumerProvider
    extends MultiBufferSource.BufferSource {
        public static final MyVertexConsumerProvider INSTANCE = new MyVertexConsumerProvider();
        private final Object2ObjectOpenHashMap<RenderType, MyVertexConsumer> buffers = new Object2ObjectOpenHashMap();

        protected MyVertexConsumerProvider() {
            super(null, null);
        }

        public VertexConsumer getBuffer(RenderType layer) {
            if (((RenderTypeAccessor)layer).getState().outputTarget == OutputTarget.ITEM_ENTITY_TARGET) {
                return NoopVertexConsumer.INSTANCE;
            }
            return (VertexConsumer)this.buffers.computeIfAbsent((Object)layer, object -> new MyVertexConsumer());
        }

        public void endBatch() {
            throw new RuntimeException();
        }

        public void endBatch(RenderType layer) {
            throw new RuntimeException();
        }
    }

    private static class MyVertexConsumer
    implements VertexConsumer {
        private final float[] xs = new float[4];
        private final float[] ys = new float[4];
        private final float[] zs = new float[4];
        private int i = 0;

        private MyVertexConsumer() {
        }

        public VertexConsumer addVertex(float x, float y, float z) {
            this.xs[this.i] = x;
            this.ys[this.i] = y;
            this.zs[this.i] = z;
            ++this.i;
            if (this.i == 4) {
                renderer.side(offsetX + (double)this.xs[0], offsetY + (double)this.ys[0], offsetZ + (double)this.zs[0], offsetX + (double)this.xs[1], offsetY + (double)this.ys[1], offsetZ + (double)this.zs[1], offsetX + (double)this.xs[2], offsetY + (double)this.ys[2], offsetZ + (double)this.zs[2], offsetX + (double)this.xs[3], offsetY + (double)this.ys[3], offsetZ + (double)this.zs[3], sideColor, lineColor, shapeMode);
                this.i = 0;
            }
            return this;
        }

        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            return this;
        }

        public VertexConsumer setColor(int argb) {
            return this;
        }

        public VertexConsumer setUv(float u, float v) {
            return this;
        }

        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        public VertexConsumer setNormal(float x, float y, float z) {
            return this;
        }

        public VertexConsumer setLineWidth(float width) {
            return this;
        }
    }
}
