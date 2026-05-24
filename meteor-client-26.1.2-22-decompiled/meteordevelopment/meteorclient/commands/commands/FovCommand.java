package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.mixininterface.IOptionInstance;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class FovCommand
extends Command {
    public FovCommand() {
        super("fov", "Changes your fov.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(FovCommand.argument("fov", IntegerArgumentType.integer((int)1, (int)180)).executes(context -> {
            ((IOptionInstance)FovCommand.mc.options.fov()).meteor$set(context.getArgument("fov", Integer.class));
            return 1;
        }));
    }
}
