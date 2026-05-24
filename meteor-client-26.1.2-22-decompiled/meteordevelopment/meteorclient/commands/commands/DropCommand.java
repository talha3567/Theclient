package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DropCommand
extends Command {
    private static final SimpleCommandExceptionType NOT_SPECTATOR = new SimpleCommandExceptionType((Message)Component.literal((String)"Can't drop items while in spectator."));
    private static final SimpleCommandExceptionType NO_SUCH_ITEM = new SimpleCommandExceptionType((Message)Component.literal((String)"Could not find an item with that name!"));

    public DropCommand() {
        super("drop", "Automatically drops specified items.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(DropCommand.literal("hand").executes(commandContext -> this.drop(player -> player.drop(true))));
        builder.then(DropCommand.literal("offhand").executes(commandContext -> this.drop(localPlayer -> InvUtils.drop().slotOffhand())));
        builder.then(DropCommand.literal("hotbar").executes(commandContext -> this.drop(localPlayer -> {
            for (int i = 0; i < 9; ++i) {
                InvUtils.drop().slotHotbar(i);
            }
        })));
        builder.then(DropCommand.literal("inventory").executes(commandContext -> this.drop(localPlayer -> {
            for (int i = 0; i < 27; ++i) {
                InvUtils.drop().slotMain(i);
            }
        })));
        builder.then(DropCommand.literal("all").executes(commandContext -> this.drop(player -> {
            for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
                InvUtils.drop().slot(i);
            }
            if (!DropCommand.mc.player.getOffhandItem().isEmpty()) {
                InvUtils.drop().slotOffhand();
            }
        })));
        builder.then(DropCommand.literal("armor").executes(commandContext -> this.drop(localPlayer -> {
            for (EquipmentSlot equipmentSlot : EquipmentSlotGroup.ARMOR) {
                if (equipmentSlot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;
                InvUtils.drop().slotArmor(equipmentSlot.getIndex());
            }
        })));
        builder.then(((RequiredArgumentBuilder)DropCommand.argument("item", ItemArgument.item((CommandBuildContext)REGISTRY_ACCESS)).executes(context -> this.drop(player -> this.dropItem(player, (CommandContext<ClientSuggestionProvider>)context, Integer.MAX_VALUE)))).then(DropCommand.argument("amount", IntegerArgumentType.integer((int)1)).executes(context -> this.drop(player -> {
            int amount = IntegerArgumentType.getInteger((CommandContext)context, (String)"amount");
            this.dropItem(player, (CommandContext<ClientSuggestionProvider>)context, amount);
        }))));
    }

    private void dropItem(LocalPlayer player, CommandContext<ClientSuggestionProvider> context, int amount) throws CommandSyntaxException {
        ItemStack stack = ItemArgument.getItem(context, (String)"item").createItemStack(1);
        if (stack == null || stack.getItem() == Items.AIR) {
            throw NO_SUCH_ITEM.create();
        }
        for (int i = 0; i < player.getInventory().getContainerSize() && amount > 0; ++i) {
            ItemStack invStack = player.getInventory().getItem(i);
            if (invStack.isEmpty() || stack.getItem() != invStack.getItem()) continue;
            int dropCount = Math.min(amount, invStack.getCount());
            if (dropCount == invStack.getCount()) {
                InvUtils.drop().slot(i);
            } else {
                for (int j = 0; j < dropCount; ++j) {
                    InvUtils.dropOne().slot(i);
                }
            }
            amount -= dropCount;
        }
    }

    private int drop(PlayerConsumer consumer) throws CommandSyntaxException {
        if (DropCommand.mc.player.isSpectator()) {
            throw NOT_SPECTATOR.create();
        }
        consumer.accept(DropCommand.mc.player);
        return 1;
    }

    @FunctionalInterface
    private static interface PlayerConsumer {
        public void accept(LocalPlayer var1) throws CommandSyntaxException;
    }
}
