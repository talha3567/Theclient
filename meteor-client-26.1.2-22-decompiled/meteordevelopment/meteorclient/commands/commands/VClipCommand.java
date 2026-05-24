package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.world.entity.Entity;

public class VClipCommand
extends Command {
    public VClipCommand() {
        super("vclip", "Lets you clip through blocks vertically.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(VClipCommand.argument("blocks", DoubleArgumentType.doubleArg()).executes(context -> {
            double blocks = (Double)context.getArgument("blocks", Double.class);
            int packetsRequired = (int)Math.ceil(Math.abs(blocks / 10.0));
            if (packetsRequired > 20) {
                packetsRequired = 1;
            }
            if (VClipCommand.mc.player.isPassenger()) {
                for (int packetNumber = 0; packetNumber < packetsRequired - 1; ++packetNumber) {
                    VClipCommand.mc.player.connection.send((Packet)ServerboundMoveVehiclePacket.fromEntity((Entity)VClipCommand.mc.player.getVehicle()));
                }
                VClipCommand.mc.player.getVehicle().setPos(VClipCommand.mc.player.getVehicle().getX(), VClipCommand.mc.player.getVehicle().getY() + blocks, VClipCommand.mc.player.getVehicle().getZ());
                VClipCommand.mc.player.connection.send((Packet)ServerboundMoveVehiclePacket.fromEntity((Entity)VClipCommand.mc.player.getVehicle()));
            } else {
                for (int packetNumber = 0; packetNumber < packetsRequired - 1; ++packetNumber) {
                    VClipCommand.mc.player.connection.send((Packet)new ServerboundMovePlayerPacket.StatusOnly(true, VClipCommand.mc.player.horizontalCollision));
                }
                VClipCommand.mc.player.connection.send((Packet)new ServerboundMovePlayerPacket.Pos(VClipCommand.mc.player.getX(), VClipCommand.mc.player.getY() + blocks, VClipCommand.mc.player.getZ(), true, VClipCommand.mc.player.horizontalCollision));
                VClipCommand.mc.player.setPos(VClipCommand.mc.player.getX(), VClipCommand.mc.player.getY() + blocks, VClipCommand.mc.player.getZ());
            }
            return 1;
        }));
    }
}
