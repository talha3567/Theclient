package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

public class BindsCommand
extends Command {
    public BindsCommand() {
        super("binds", "List of all bound modules.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.executes(commandContext -> {
            List<Module> modules = Modules.get().getAll().stream().filter(module -> module.keybind.isSet()).toList();
            ChatUtils.info("--- Bound Modules ((highlight)%d(default)) ---", modules.size());
            for (Module module2 : modules) {
                HoverEvent.ShowText hoverEvent = new HoverEvent.ShowText((Component)this.getTooltip(module2));
                MutableComponent text = Component.literal((String)module2.title).withStyle(ChatFormatting.WHITE);
                text.setStyle(text.getStyle().withHoverEvent((HoverEvent)hoverEvent));
                MutableComponent sep = Component.literal((String)" - ");
                sep.setStyle(sep.getStyle().withHoverEvent((HoverEvent)hoverEvent));
                text.append((Component)sep.withStyle(ChatFormatting.GRAY));
                MutableComponent key = Component.literal((String)module2.keybind.toString());
                key.setStyle(key.getStyle().withHoverEvent((HoverEvent)hoverEvent));
                text.append((Component)key.withStyle(ChatFormatting.GRAY));
                ChatUtils.sendMsg((Component)text);
            }
            return 1;
        });
    }

    private MutableComponent getTooltip(Module module) {
        MutableComponent tooltip = Component.literal((String)Utils.nameToTitle(module.title)).withStyle(new ChatFormatting[]{ChatFormatting.BLUE, ChatFormatting.BOLD}).append("\n\n");
        tooltip.append((Component)Component.literal((String)module.description).withStyle(ChatFormatting.WHITE));
        return tooltip;
    }
}
