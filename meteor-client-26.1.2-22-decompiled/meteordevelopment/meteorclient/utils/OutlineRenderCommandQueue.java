package meteordevelopment.meteorclient.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public class OutlineRenderCommandQueue
extends SubmitNodeStorage {
    private int color;
    private int[] tints;

    public void setColor(Color color) {
        this.color = color.getPacked();
    }

    public SubmitNodeCollection order(int i) {
        return (SubmitNodeCollection)this.submitsPerOrder.computeIfAbsent(i, n -> new OutlineBatchingRenderCommandQueue(this, this));
    }

    private class OutlineBatchingRenderCommandQueue
    extends SubmitNodeCollection {
        final /* synthetic */ OutlineRenderCommandQueue this$0;

        public OutlineBatchingRenderCommandQueue(OutlineRenderCommandQueue outlineRenderCommandQueue, SubmitNodeStorage orderedQueueImpl) {
            OutlineRenderCommandQueue outlineRenderCommandQueue2 = outlineRenderCommandQueue;
            Objects.requireNonNull(outlineRenderCommandQueue2);
            this.this$0 = outlineRenderCommandQueue2;
            super(orderedQueueImpl);
        }

        public void submitShadow(PoseStack poseStack, float shadowRadius, List<EntityRenderState.ShadowPiece> shadowPieces) {
        }

        public void submitNameTag(PoseStack poseStack, @Nullable Vec3 vec3, int i, Component component, boolean bl, int j, double d, CameraRenderState cameraRenderState) {
        }

        public void submitText(PoseStack poseStack, float x, float y, FormattedCharSequence text, boolean dropShadow, Font.DisplayMode layerType, int light, int color, int backgroundColor, int outlineColor) {
        }

        public void submitFlame(PoseStack poseStack, EntityRenderState entityRenderState, Quaternionf quaternionf) {
        }

        public void submitLeash(PoseStack poseStack, EntityRenderState.LeashState leashState) {
        }

        public <S> void submitModel(Model<? super S> model, S state, PoseStack matrices, RenderType renderLayer, int light, int overlay, int tintedColor, @Nullable TextureAtlasSprite sprite, int outlineColor, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
            super.submitModel(model, state, matrices, renderLayer, light, overlay, this.this$0.color, sprite, 0, crumblingOverlay);
        }

        public void submitModelPart(ModelPart part, PoseStack matrices, RenderType renderLayer, int light, int overlay, @Nullable TextureAtlasSprite sprite, boolean sheeted, boolean hasGlint, int tintedColor, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, int i) {
            super.submitModelPart(part, matrices, renderLayer, light, overlay, sprite, sheeted, hasGlint, this.this$0.color, crumblingOverlay, i);
        }

        public void submitMovingBlock(PoseStack matrices, MovingBlockRenderState state) {
        }

        public void submitBlockModel(PoseStack poseStack, RenderType renderType, List<BlockStateModelPart> modelParts, int[] tintLayers, int lightCoords, int overlayCoords, int outlineColor) {
            Arrays.fill(tintLayers, this.this$0.color);
            super.submitBlockModel(poseStack, renderType, modelParts, tintLayers, lightCoords, overlayCoords, outlineColor);
        }

        public void submitBreakingBlockModel(PoseStack poseStack, BlockStateModel model, long seed, int progress) {
        }

        public void submitItem(PoseStack poseStack, ItemDisplayContext displayContext, int lightCoords, int overlayCoords, int outlineColor, int[] tintLayers, List<BakedQuad> quads, ItemStackRenderState.FoilType foilType) {
            if (this.this$0.tints == null || this.this$0.tints[0] != this.this$0.color) {
                this.this$0.tints = new int[]{this.this$0.color, this.this$0.color, this.this$0.color, this.this$0.color};
            }
            super.submitItem(poseStack, displayContext, lightCoords, overlayCoords, outlineColor, tintLayers, quads, foilType);
        }

        public void submitCustomGeometry(PoseStack poseStack, RenderType renderType, SubmitNodeCollector.CustomGeometryRenderer customGeometryRenderer) {
        }

        public void submitParticleGroup(SubmitNodeCollector.ParticleGroupRenderer particleGroupRenderer) {
        }
    }
}
