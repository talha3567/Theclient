package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;

public class PeekCommand
extends Command {
    private static final ItemStack[] ITEMS = new ItemStack[27];
    private static final SimpleCommandExceptionType CANT_PEEK = new SimpleCommandExceptionType((Message)Component.literal((String)"You must be holding a storage block or looking at an item frame."));

    public PeekCommand() {
        super("peek", "Lets you see what's inside storage block items.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.executes(commandContext -> {
            ItemFrame itemFrame;
            if (Utils.openContainer(PeekCommand.mc.player.getMainHandItem(), ITEMS, true)) {
                return 1;
            }
            if (Utils.openContainer(PeekCommand.mc.player.getOffhandItem(), ITEMS, true)) {
                return 1;
            }
            Entity patt0$temp = PeekCommand.mc.crosshairPickEntity;
            if (patt0$temp instanceof ItemFrame && Utils.openContainer((itemFrame = (ItemFrame)patt0$temp).getItem(), ITEMS, true)) {
                return 1;
            }
            throw CANT_PEEK.create();
        });
    }
}
