package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.item.properties.numeric.CompassAngleState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={CompassAngleState.class})
public abstract class CompassAngleStateMixin {
    @ModifyExpressionValue(method={"getWrappedVisualRotationY"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/ItemOwner;getVisualRotationYInDegrees()F")})
    private static float callLivingEntityGetYaw(float original) {
        if (Modules.get().isActive(Freecam.class)) {
            return MeteorClient.mc.gameRenderer.getMainCamera().yRot();
        }
        return original;
    }

    @ModifyReturnValue(method={"getAngleFromEntityToPos(Lnet/minecraft/world/entity/ItemOwner;Lnet/minecraft/core/BlockPos;)D"}, at={@At(value="RETURN")})
    private static double modifyGetAngleTo(double original, ItemOwner owner, BlockPos position) {
        if (Modules.get().isActive(Freecam.class)) {
            Vec3 vec3d = Vec3.atCenterOf((Vec3i)position);
            Camera camera = MeteorClient.mc.gameRenderer.getMainCamera();
            return Math.atan2(vec3d.z() - camera.position().z, vec3d.x() - camera.position().x) / 6.2831854820251465;
        }
        return original;
    }
}
