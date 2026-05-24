package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.item.ItemStack;

public class GiveCommand
extends Command {
    private static final SimpleCommandExceptionType NOT_IN_CREATIVE = new SimpleCommandExceptionType((Message)Component.literal((String)"You must be in creative mode to use this."));
    private static final SimpleCommandExceptionType NO_SPACE = new SimpleCommandExceptionType((Message)Component.literal((String)"No space in hotbar."));

    public GiveCommand() {
        super("give", "Gives you any item.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(((RequiredArgumentBuilder)GiveCommand.argument("item", ItemArgument.item((CommandBuildContext)REGISTRY_ACCESS)).executes(context -> {
            if (!GiveCommand.mc.player.getAbilities().instabuild) {
                throw NOT_IN_CREATIVE.create();
            }
            ItemStack item = ItemArgument.getItem((CommandContext)context, (String)"item").createItemStack(1);
            this.giveItem(item);
            return 1;
        })).then(GiveCommand.argument("number", IntegerArgumentType.integer((int)1, (int)99)).executes(context -> {
            if (!GiveCommand.mc.player.getAbilities().instabuild) {
                throw NOT_IN_CREATIVE.create();
            }
            ItemStack item = ItemArgument.getItem((CommandContext)context, (String)"item").createItemStack(IntegerArgumentType.getInteger((CommandContext)context, (String)"number"));
            this.giveItem(item);
            return 1;
        })));
    }

    private void giveItem(ItemStack item) throws CommandSyntaxException {
        FindItemResult fir = InvUtils.find(ItemStack::isEmpty, 0, 8);
        if (!fir.found()) {
            throw NO_SPACE.create();
        }
        mc.getConnection().send((Packet)new ServerboundSetCreativeModeSlotPacket(36 + fir.slot(), item));
        GiveCommand.mc.player.inventoryMenu.getSlot(36 + fir.slot()).set(item);
    }
}
