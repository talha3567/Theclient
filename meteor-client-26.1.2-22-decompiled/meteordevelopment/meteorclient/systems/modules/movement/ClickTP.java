package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ClickTP
extends Module {
    public ClickTP() {
        super(Categories.Movement, "click-tp", "Teleports you to the block you click on.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        Vec3 direction;
        Vec3 targetPos;
        Camera camera;
        Vec3 cameraPos;
        ClipContext context;
        BlockHitResult hitResult;
        if (this.mc.player.getInventory().getSelectedItem().getUseAnimation() != ItemUseAnimation.NONE) {
            return;
        }
        if (!this.mc.options.keyUse.isDown()) {
            return;
        }
        if (this.mc.hitResult != null) {
            if (this.mc.hitResult.getType() == HitResult.Type.ENTITY && this.mc.player.interactOn(((EntityHitResult)this.mc.hitResult).getEntity(), InteractionHand.MAIN_HAND, this.mc.hitResult.getLocation()) != InteractionResult.PASS) {
                return;
            }
            if (this.mc.hitResult.getType() == HitResult.Type.BLOCK && this.mc.player.getMainHandItem().getItem() instanceof BlockItem) {
                return;
            }
        }
        if ((hitResult = this.mc.level.clip(context = new ClipContext(cameraPos = (camera = this.mc.gameRenderer.getMainCamera()).position(), targetPos = cameraPos.add(direction = Vec3.directionFromRotation((float)camera.xRot(), (float)camera.yRot()).scale(210.0)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, (Entity)this.mc.player))).getType() == HitResult.Type.BLOCK) {
            BlockPos pos = hitResult.getBlockPos();
            Direction side = hitResult.getDirection();
            if (this.mc.level.getBlockState(pos).useWithoutItem((Level)this.mc.level, (Player)this.mc.player, hitResult) != InteractionResult.PASS) {
                return;
            }
            BlockState state = this.mc.level.getBlockState(pos);
            VoxelShape shape = state.getCollisionShape((BlockGetter)this.mc.level, pos);
            if (shape.isEmpty()) {
                shape = state.getShape((BlockGetter)this.mc.level, pos);
            }
            double height = shape.isEmpty() ? 1.0 : shape.max(Direction.Axis.Y);
            Vec3 newPos = new Vec3((double)pos.getX() + 0.5 + (double)side.getStepX(), (double)pos.getY() + height, (double)pos.getZ() + 0.5 + (double)side.getStepZ());
            int packetsRequired = (int)Math.ceil(this.mc.player.position().distanceTo(newPos) / 10.0) - 1;
            if (packetsRequired > 19) {
                packetsRequired = 0;
            }
            for (int packetNumber = 0; packetNumber < packetsRequired; ++packetNumber) {
                this.mc.player.connection.send((Packet)new ServerboundMovePlayerPacket.StatusOnly(true, true));
            }
            this.mc.player.connection.send((Packet)new ServerboundMovePlayerPacket.Pos(newPos.x, newPos.y, newPos.z, true, true));
            this.mc.player.setPos(newPos);
        }
    }
}
