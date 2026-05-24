package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.DirectionArgumentType;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class RotationCommand
extends Command {
    public RotationCommand() {
        super("rotation", "Modifies your rotation.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        ((LiteralArgumentBuilder)builder.then(((LiteralArgumentBuilder)RotationCommand.literal("set").then(RotationCommand.argument("direction", DirectionArgumentType.create()).executes(context -> {
            RotationCommand.mc.player.setXRot((float)(((Direction)context.getArgument("direction", Direction.class)).getUnitVec3i().getY() * -90));
            RotationCommand.mc.player.setYRot(((Direction)context.getArgument("direction", Direction.class)).toYRot());
            return 1;
        }))).then(((RequiredArgumentBuilder)RotationCommand.argument("pitch", FloatArgumentType.floatArg((float)-90.0f, (float)90.0f)).executes(context -> {
            RotationCommand.mc.player.setXRot(((Float)context.getArgument("pitch", Float.class)).floatValue());
            return 1;
        })).then(RotationCommand.argument("yaw", FloatArgumentType.floatArg((float)-180.0f, (float)180.0f)).executes(context -> {
            RotationCommand.mc.player.setXRot(((Float)context.getArgument("pitch", Float.class)).floatValue());
            RotationCommand.mc.player.setYRot(((Float)context.getArgument("yaw", Float.class)).floatValue());
            return 1;
        }))))).then(RotationCommand.literal("add").then(((RequiredArgumentBuilder)RotationCommand.argument("pitch", FloatArgumentType.floatArg((float)-90.0f, (float)90.0f)).executes(context -> {
            float pitch = RotationCommand.mc.player.getXRot() + ((Float)context.getArgument("pitch", Float.class)).floatValue();
            RotationCommand.mc.player.setXRot(pitch >= 0.0f ? Math.min(pitch, 90.0f) : Math.max(pitch, -90.0f));
            return 1;
        })).then(RotationCommand.argument("yaw", FloatArgumentType.floatArg((float)-180.0f, (float)180.0f)).executes(context -> {
            float pitch = RotationCommand.mc.player.getXRot() + ((Float)context.getArgument("pitch", Float.class)).floatValue();
            RotationCommand.mc.player.setXRot(pitch >= 0.0f ? Math.min(pitch, 90.0f) : Math.max(pitch, -90.0f));
            float yaw = RotationCommand.mc.player.getYRot() + ((Float)context.getArgument("yaw", Float.class)).floatValue();
            RotationCommand.mc.player.setYRot(Mth.wrapDegrees((float)yaw));
            return 1;
        }))));
    }
}
