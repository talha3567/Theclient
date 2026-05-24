package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.NoFall;
import meteordevelopment.meteorclient.systems.modules.player.AntiHunger;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

public class DamageCommand
extends Command {
    private static final SimpleCommandExceptionType INVULNERABLE = new SimpleCommandExceptionType((Message)Component.literal((String)"You are invulnerable."));

    public DamageCommand() {
        super("damage", "Damages self", "dmg");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(DamageCommand.argument("damage", IntegerArgumentType.integer((int)1, (int)7)).executes(context -> {
            int amount = IntegerArgumentType.getInteger((CommandContext)context, (String)"damage");
            if (DamageCommand.mc.player.getAbilities().invulnerable) {
                throw INVULNERABLE.create();
            }
            this.damagePlayer(amount);
            return 1;
        }));
    }

    private void damagePlayer(int amount) {
        boolean antiHunger;
        boolean noFall = Modules.get().isActive(NoFall.class);
        if (noFall) {
            Modules.get().get(NoFall.class).toggle();
        }
        if (antiHunger = Modules.get().isActive(AntiHunger.class)) {
            Modules.get().get(AntiHunger.class).toggle();
        }
        Vec3 pos = DamageCommand.mc.player.position();
        for (int i = 0; i < 80; ++i) {
            this.sendPositionPacket(pos.x, pos.y + (double)amount + 2.1, pos.z, false);
            this.sendPositionPacket(pos.x, pos.y + 0.05, pos.z, false);
        }
        this.sendPositionPacket(pos.x, pos.y, pos.z, true);
        if (noFall) {
            Modules.get().get(NoFall.class).toggle();
        }
        if (antiHunger) {
            Modules.get().get(AntiHunger.class).toggle();
        }
    }

    private void sendPositionPacket(double x, double y, double z, boolean onGround) {
        DamageCommand.mc.player.connection.send((Packet)new ServerboundMovePlayerPacket.Pos(x, y, z, onGround, DamageCommand.mc.player.horizontalCollision));
    }
}
