package meteordevelopment.meteorclient.utils.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import meteordevelopment.meteorclient.utils.render.CustomBannerGuiElementRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public class CustomBannerGuiElementRenderer
extends PictureInPictureRenderer<CustomBannerGuiElementRenderState> {
    private final SpriteGetter sprites;

    public CustomBannerGuiElementRenderer(MultiBufferSource.BufferSource immediate, SpriteGetter sprites) {
        super(immediate);
        this.sprites = sprites;
    }

    public Class<CustomBannerGuiElementRenderState> getRenderStateClass() {
        return CustomBannerGuiElementRenderState.class;
    }

    protected void renderToTexture(CustomBannerGuiElementRenderState state, PoseStack matrixStack) {
        Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_FLAT);
        matrixStack.translate(0.0f, 0.25f, 0.0f);
        FeatureRenderDispatcher renderDispatcher = Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher();
        SubmitNodeStorage orderedRenderCommandQueueImpl = renderDispatcher.getSubmitNodeStorage();
        BannerRenderer.submitPatterns((SpriteGetter)this.sprites, (PoseStack)matrixStack, (SubmitNodeCollector)orderedRenderCommandQueueImpl, (int)0xF000F0, (int)OverlayTexture.NO_OVERLAY, (Model)state.flag(), (Object)Float.valueOf(0.0f), (boolean)true, (DyeColor)state.baseColor(), (BannerPatternLayers)state.resultBannerPatterns(), null);
        renderDispatcher.renderAllFeatures();
    }

    protected String getTextureLabel() {
        return "custom banner";
    }
}
