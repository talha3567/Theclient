package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

public class ModulesCommand
extends Command {
    public ModulesCommand() {
        super("modules", "Displays a list of all modules.", "features");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.executes(commandContext -> {
            ChatUtils.info("--- Modules ((highlight)%d(default)) ---", Modules.get().getCount());
            Modules.loopCategories().forEach(category -> {
                MutableComponent categoryMessage = Component.literal((String)"");
                Modules.get().getGroup((Category)category).forEach(module -> categoryMessage.append((Component)this.getModuleText((Module)module)));
                ChatUtils.sendMsg(category.name, (Component)categoryMessage);
            });
            return 1;
        });
    }

    private MutableComponent getModuleText(Module module) {
        MutableComponent tooltip = Component.literal((String)"");
        tooltip.append((Component)Component.literal((String)module.title).withStyle(new ChatFormatting[]{ChatFormatting.BLUE, ChatFormatting.BOLD})).append("\n");
        tooltip.append((Component)Component.literal((String)module.name).withStyle(ChatFormatting.GRAY)).append("\n\n");
        tooltip.append((Component)Component.literal((String)module.description).withStyle(ChatFormatting.WHITE));
        MutableComponent finalModule = Component.literal((String)module.title);
        if (!module.isActive()) {
            finalModule.withStyle(ChatFormatting.GRAY);
        }
        if (!module.equals(Modules.get().getGroup(module.category).getLast())) {
            finalModule.append((Component)Component.literal((String)", ").withStyle(ChatFormatting.GRAY));
        }
        finalModule.setStyle(finalModule.getStyle().withHoverEvent((HoverEvent)new HoverEvent.ShowText((Component)tooltip)));
        return finalModule;
    }
}
