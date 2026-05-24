package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.ToIntFunction;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.RegistryEntryReferenceArgumentType;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantCommand
extends Command {
    private static final SimpleCommandExceptionType NOT_IN_CREATIVE = new SimpleCommandExceptionType((Message)Component.literal((String)"You must be in creative mode to use this."));
    private static final SimpleCommandExceptionType NOT_HOLDING_ITEM = new SimpleCommandExceptionType((Message)Component.literal((String)"You need to hold some item to enchant."));

    public EnchantCommand() {
        super("enchant", "Enchants the item in your hand. REQUIRES Creative mode.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(EnchantCommand.literal("one").then(((RequiredArgumentBuilder)EnchantCommand.argument("enchantment", RegistryEntryReferenceArgumentType.enchantment()).then(EnchantCommand.literal("level").then(EnchantCommand.argument("level", IntegerArgumentType.integer()).executes(context -> {
            this.one((CommandContext<ClientSuggestionProvider>)context, enchantment -> (Integer)context.getArgument("level", Integer.class));
            return 1;
        })))).then(EnchantCommand.literal("max").executes(context -> {
            this.one((CommandContext<ClientSuggestionProvider>)context, Enchantment::getMaxLevel);
            return 1;
        }))));
        builder.then(((LiteralArgumentBuilder)EnchantCommand.literal("all_possible").then(EnchantCommand.literal("level").then(EnchantCommand.argument("level", IntegerArgumentType.integer()).executes(context -> {
            this.all(true, enchantment -> (Integer)context.getArgument("level", Integer.class));
            return 1;
        })))).then(EnchantCommand.literal("max").executes(commandContext -> {
            this.all(true, Enchantment::getMaxLevel);
            return 1;
        })));
        builder.then(((LiteralArgumentBuilder)EnchantCommand.literal("all").then(EnchantCommand.literal("level").then(EnchantCommand.argument("level", IntegerArgumentType.integer()).executes(context -> {
            this.all(false, enchantment -> (Integer)context.getArgument("level", Integer.class));
            return 1;
        })))).then(EnchantCommand.literal("max").executes(commandContext -> {
            this.all(false, Enchantment::getMaxLevel);
            return 1;
        })));
        builder.then(EnchantCommand.literal("clear").executes(commandContext -> {
            ItemStack itemStack = this.tryGetItemStack();
            Utils.clearEnchantments(itemStack);
            this.syncItem();
            return 1;
        }));
        builder.then(EnchantCommand.literal("remove").then(EnchantCommand.argument("enchantment", RegistryEntryReferenceArgumentType.enchantment()).executes(context -> {
            ItemStack itemStack = this.tryGetItemStack();
            Holder.Reference<Enchantment> enchantment = RegistryEntryReferenceArgumentType.getEnchantment(context, "enchantment");
            Utils.removeEnchantment(itemStack, (Enchantment)enchantment.value());
            this.syncItem();
            return 1;
        })));
    }

    private void one(CommandContext<ClientSuggestionProvider> context, ToIntFunction<Enchantment> level) throws CommandSyntaxException {
        ItemStack itemStack = this.tryGetItemStack();
        Holder.Reference<Enchantment> enchantment = RegistryEntryReferenceArgumentType.getEnchantment(context, "enchantment");
        Utils.addEnchantment(itemStack, enchantment, level.applyAsInt((Enchantment)enchantment.value()));
        this.syncItem();
    }

    private void all(boolean onlyPossible, ToIntFunction<Enchantment> level) throws CommandSyntaxException {
        ItemStack itemStack = this.tryGetItemStack();
        mc.getConnection().registryAccess().lookup(Registries.ENCHANTMENT).ifPresent(registry -> registry.listElements().forEach(enchantment -> {
            if (!onlyPossible || ((Enchantment)enchantment.value()).isSupportedItem(itemStack)) {
                Utils.addEnchantment(itemStack, (Holder<Enchantment>)enchantment, level.applyAsInt((Enchantment)enchantment.value()));
            }
        }));
        this.syncItem();
    }

    private void syncItem() {
        mc.setScreen((Screen)new InventoryScreen((Player)EnchantCommand.mc.player));
        mc.setScreen(null);
    }

    private ItemStack tryGetItemStack() throws CommandSyntaxException {
        if (!EnchantCommand.mc.player.isCreative()) {
            throw NOT_IN_CREATIVE.create();
        }
        ItemStack itemStack = this.getItemStack();
        if (itemStack == null) {
            throw NOT_HOLDING_ITEM.create();
        }
        return itemStack;
    }

    private ItemStack getItemStack() {
        ItemStack itemStack = EnchantCommand.mc.player.getMainHandItem();
        if (itemStack == null) {
            itemStack = EnchantCommand.mc.player.getOffhandItem();
        }
        return itemStack.isEmpty() ? null : itemStack;
    }
}
