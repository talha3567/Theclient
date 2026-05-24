package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class HClipCommand
extends Command {
    public HClipCommand() {
        super("hclip", "Lets you clip through blocks horizontally.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(HClipCommand.argument("blocks", DoubleArgumentType.doubleArg()).executes(context -> {
            double blocks = (Double)context.getArgument("blocks", Double.class);
            Vec3 forward = Vec3.directionFromRotation((float)0.0f, (float)HClipCommand.mc.player.getYRot()).normalize();
            if (HClipCommand.mc.player.isPassenger()) {
                Entity vehicle = HClipCommand.mc.player.getVehicle();
                vehicle.setPos(vehicle.getX() + forward.x * blocks, vehicle.getY(), vehicle.getZ() + forward.z * blocks);
            }
            HClipCommand.mc.player.setPos(HClipCommand.mc.player.getX() + forward.x * blocks, HClipCommand.mc.player.getY(), HClipCommand.mc.player.getZ() + forward.z * blocks);
            return 1;
        }));
    }
}
