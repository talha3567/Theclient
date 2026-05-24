package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.DataResult;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.ComponentMapArgumentType;
import meteordevelopment.meteorclient.utils.misc.text.MeteorClickEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.commands.data.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class NbtCommand
extends Command {
    private static final DynamicCommandExceptionType MALFORMED_ITEM_EXCEPTION = new DynamicCommandExceptionType(error -> Component.translatableEscape((String)"arguments.item.malformed", (Object[])new Object[]{error}));
    private final MutableComponent copyButton = Component.literal((String)"NBT").setStyle(Style.EMPTY.applyFormat(ChatFormatting.UNDERLINE).withClickEvent((ClickEvent)new MeteorClickEvent(this.toString("copy"))).withHoverEvent((HoverEvent)new HoverEvent.ShowText((Component)Component.literal((String)"Copy the NBT data to your clipboard."))));

    public NbtCommand() {
        super("nbt", "Modifies NBT data for an item, example: .nbt add {display:{Name:'{\"text\":\"$cRed Name\"}'}}", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.then(NbtCommand.literal("add").then(NbtCommand.argument("component", ComponentMapArgumentType.componentMap(REGISTRY_ACCESS)).executes(ctx -> {
            ItemStack stack = NbtCommand.mc.player.getInventory().getSelectedItem();
            if (this.validBasic(stack)) {
                DataComponentMap itemComponents = stack.getComponents();
                DataComponentMap newComponents = ComponentMapArgumentType.getComponentMap(ctx, "component");
                DataComponentMap testComponents = DataComponentMap.composite((DataComponentMap)itemComponents, (DataComponentMap)newComponents);
                ItemStack testStack = stack.copy();
                testStack.applyComponents(testComponents);
                DataResult dataResult = ItemStack.validateStrict((ItemStack)testStack);
                dataResult.getOrThrow(arg_0 -> ((DynamicCommandExceptionType)MALFORMED_ITEM_EXCEPTION).create(arg_0));
                stack.applyComponents(testComponents);
                this.setStack(stack);
            }
            return 1;
        })));
        builder.then(NbtCommand.literal("set").then(NbtCommand.argument("component", ComponentMapArgumentType.componentMap(REGISTRY_ACCESS)).executes(ctx -> {
            ItemStack stack = NbtCommand.mc.player.getInventory().getSelectedItem();
            if (this.validBasic(stack)) {
                DataComponentMap components = ComponentMapArgumentType.getComponentMap(ctx, "component");
                PatchedDataComponentMap stackComponents = (PatchedDataComponentMap)stack.getComponents();
                ItemStack testStack = stack.copy();
                testStack.applyComponents(components);
                DataResult dataResult = ItemStack.validateStrict((ItemStack)testStack);
                dataResult.getOrThrow(arg_0 -> ((DynamicCommandExceptionType)MALFORMED_ITEM_EXCEPTION).create(arg_0));
                DataComponentPatch.Builder changesBuilder = DataComponentPatch.builder();
                HashSet types = new HashSet(stackComponents.keySet());
                for (TypedDataComponent entry : components) {
                    changesBuilder.set(entry);
                    types.remove(entry.type());
                }
                for (DataComponentType type : types) {
                    changesBuilder.remove(type);
                }
                stackComponents.applyPatch(changesBuilder.build());
                this.setStack(stack);
            }
            return 1;
        })));
        builder.then(NbtCommand.literal("remove").then((ArgumentBuilder)((RequiredArgumentBuilder)NbtCommand.argument("component", ResourceKeyArgument.key((ResourceKey)Registries.DATA_COMPONENT_TYPE)).executes(ctx -> {
            ItemStack stack = NbtCommand.mc.player.getInventory().getSelectedItem();
            if (this.validBasic(stack)) {
                ResourceKey componentTypeKey = (ResourceKey)ctx.getArgument("component", ResourceKey.class);
                DataComponentType componentType = (DataComponentType)BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(componentTypeKey);
                PatchedDataComponentMap components = (PatchedDataComponentMap)stack.getComponents();
                components.applyPatch(DataComponentPatch.builder().remove(componentType).build());
                this.setStack(stack);
            }
            return 1;
        })).suggests((commandContext, suggestionsBuilder) -> {
            ItemStack stack = NbtCommand.mc.player.getInventory().getSelectedItem();
            if (stack != ItemStack.EMPTY) {
                DataComponentMap components = stack.getComponents();
                String remaining = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
                SharedSuggestionProvider.filterResources(components.keySet().stream().map(arg_0 -> ((Registry)BuiltInRegistries.DATA_COMPONENT_TYPE).wrapAsHolder(arg_0)).toList(), (String)remaining, entry -> {
                    if (entry.unwrapKey().isPresent()) {
                        return ((ResourceKey)entry.unwrapKey().get()).identifier();
                    }
                    return null;
                }, entry -> {
                    DataComponentType dataComponentType = (DataComponentType)entry.value();
                    if (dataComponentType.codec() != null && entry.unwrapKey().isPresent()) {
                        suggestionsBuilder.suggest(((ResourceKey)entry.unwrapKey().get()).identifier().toString());
                    }
                });
            }
            return suggestionsBuilder.buildFuture();
        })));
        builder.then(NbtCommand.literal("get").executes(commandContext -> {
            EntityDataAccessor dataCommandObject = new EntityDataAccessor((Entity)NbtCommand.mc.player);
            NbtPathArgument.NbtPath handPath = NbtPathArgument.NbtPath.of((String)"SelectedItem");
            MutableComponent text = Component.empty().append((Component)this.copyButton);
            try {
                List nbtElement = handPath.get((Tag)dataCommandObject.getData());
                if (!nbtElement.isEmpty()) {
                    text.append(" ").append(NbtUtils.toPrettyComponent((Tag)((Tag)nbtElement.getFirst())));
                }
            }
            catch (CommandSyntaxException commandSyntaxException) {
                text.append("{}");
            }
            this.info((Component)text);
            return 1;
        }));
        builder.then(NbtCommand.literal("copy").executes(commandContext -> {
            EntityDataAccessor dataCommandObject = new EntityDataAccessor((Entity)NbtCommand.mc.player);
            NbtPathArgument.NbtPath handPath = NbtPathArgument.NbtPath.of((String)"SelectedItem");
            MutableComponent text = Component.empty().append((Component)this.copyButton);
            String nbt = "{}";
            try {
                List nbtElement = handPath.get((Tag)dataCommandObject.getData());
                if (!nbtElement.isEmpty()) {
                    text.append(" ").append(NbtUtils.toPrettyComponent((Tag)((Tag)nbtElement.getFirst())));
                    nbt = ((Tag)nbtElement.getFirst()).toString();
                }
            }
            catch (CommandSyntaxException commandSyntaxException) {
                text.append("{}");
            }
            NbtCommand.mc.keyboardHandler.setClipboard(nbt);
            text.append(" data copied!");
            this.info((Component)text);
            return 1;
        }));
        builder.then(NbtCommand.literal("count").then(NbtCommand.argument("count", IntegerArgumentType.integer((int)-127, (int)127)).executes(context -> {
            ItemStack stack = NbtCommand.mc.player.getInventory().getSelectedItem();
            if (this.validBasic(stack)) {
                int count = IntegerArgumentType.getInteger((CommandContext)context, (String)"count");
                stack.setCount(count);
                this.setStack(stack);
                this.info("Set mainhand stack count to %s.", count);
            }
            return 1;
        })));
    }

    private void setStack(ItemStack stack) {
        NbtCommand.mc.player.connection.send((Packet)new ServerboundSetCreativeModeSlotPacket(36 + NbtCommand.mc.player.getInventory().getSelectedSlot(), stack));
    }

    private boolean validBasic(ItemStack stack) {
        if (!NbtCommand.mc.player.getAbilities().instabuild) {
            this.error("Creative mode only.", new Object[0]);
            return false;
        }
        if (stack == ItemStack.EMPTY) {
            this.error("You must hold an item in your main hand.", new Object[0]);
            return false;
        }
        return true;
    }
}
