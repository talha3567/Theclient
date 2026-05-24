package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixininterface.IEntityRenderState;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Chams;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import org.lwjgl.opengl.GL11C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LivingEntityRenderer.class})
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
    @Unique
    private Chams chams;

    @ModifyExpressionValue(method={"shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/Minecraft;getCameraEntity()Lnet/minecraft/world/entity/Entity;")})
    private Entity hasLabelGetCameraEntityProxy(Entity cameraEntity) {
        return Modules.get().isActive(Freecam.class) ? null : cameraEntity;
    }

    @ModifyExpressionValue(method={"shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/LocalPlayer;getTeam()Lnet/minecraft/world/scores/PlayerTeam;")})
    private PlayerTeam hasLabelClientPlayerEntityGetScoreboardTeamProxy(PlayerTeam team) {
        return MeteorClient.mc.player == null ? null : team;
    }

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void init$chams(CallbackInfo ci) {
        this.chams = Modules.get().get(Chams.class);
    }

    @WrapWithCondition(method={"submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V")})
    private <TState> boolean render$render(SubmitNodeCollector instance, Model<? super TState> model, TState state, PoseStack matrixStack, RenderType renderLayer, int light, int overlay, int mixColor, TextureAtlasSprite sprite, int outlineColor, ModelFeatureRenderer.CrumblingOverlay crumblingOverlayCommand) {
        Entity entity;
        if (!(this.chams.isActive() && this.chams.players.get().booleanValue() && (entity = ((IEntityRenderState)state).meteor$getEntity()) instanceof Player)) {
            return true;
        }
        Player player = (Player)entity;
        if (this.chams.ignoreSelf.get().booleanValue() && player == MeteorClient.mc.player) {
            return true;
        }
        instance.submitModel(model, state, matrixStack, renderLayer, light, overlay, PlayerUtils.getPlayerColor(player, this.chams.playersColor.get()).getPacked(), sprite, outlineColor, null);
        return false;
    }

    @ModifyReturnValue(method={"getRenderType"}, at={@At(value="RETURN")})
    private RenderType getRenderPlayer(RenderType original, S state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {
        Entity entity;
        if (!this.chams.isActive() || !((entity = ((IEntityRenderState)state).meteor$getEntity()) instanceof Player)) {
            return original;
        }
        Player player = (Player)entity;
        if (!this.chams.players.get().booleanValue() || this.chams.playersTexture.get().booleanValue()) {
            return original;
        }
        if (this.chams.ignoreSelf.get().booleanValue() && player == MeteorClient.mc.player) {
            return original;
        }
        return RenderTypes.itemTranslucent((Identifier)Chams.BLANK);
    }

    @Inject(method={"submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void render$Head(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
        Entity entity = ((IEntityRenderState)state).meteor$getEntity();
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        LivingEntity livingEntity = (LivingEntity)entity;
        if (Modules.get().get(NoRender.class).noDeadEntities() && livingEntity.isDeadOrDying()) {
            ci.cancel();
        }
        if (this.chams.shouldRender(entity)) {
            GL11C.glEnable((int)32823);
            GL11C.glPolygonOffset((float)1.0f, (float)-1100000.0f);
        }
    }

    @Inject(method={"submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"}, at={@At(value="TAIL")})
    private void render$Tail(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
        Entity entity = ((IEntityRenderState)state).meteor$getEntity();
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        LivingEntity livingEntity = (LivingEntity)entity;
        if (this.chams.shouldRender((Entity)livingEntity)) {
            GL11C.glPolygonOffset((float)1.0f, (float)1100000.0f);
            GL11C.glDisable((int)32823);
        }
    }
}
