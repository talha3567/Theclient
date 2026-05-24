package meteordevelopment.meteorclient.commands.arguments;

import com.google.common.collect.Streams;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class SettingArgumentType
implements ArgumentType<String> {
    private static final SettingArgumentType INSTANCE = new SettingArgumentType();
    private static final DynamicCommandExceptionType NO_SUCH_SETTING = new DynamicCommandExceptionType(name -> Component.literal((String)("No such setting '" + String.valueOf(name) + "'.")));

    public static SettingArgumentType create() {
        return INSTANCE;
    }

    public static Setting<?> get(CommandContext<?> context) throws CommandSyntaxException {
        Module module = (Module)context.getArgument("module", Module.class);
        return SettingArgumentType.get(context, module.settings);
    }

    public static Setting<?> get(CommandContext<?> context, Settings settings) throws CommandSyntaxException {
        String settingName = (String)context.getArgument("setting", String.class);
        Setting<?> setting = settings.get(settingName);
        if (setting == null) {
            throw NO_SUCH_SETTING.create((Object)settingName);
        }
        return setting;
    }

    private SettingArgumentType() {
    }

    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readString();
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SettingArgumentType.listSuggestions(builder, ((Module)context.getArgument((String)"module", Module.class)).settings);
    }

    public static CompletableFuture<Suggestions> listSuggestions(SuggestionsBuilder builder, Settings settings) {
        Stream<String> stream = Streams.stream(settings.iterator()).flatMap(sg -> Streams.stream(sg.iterator())).map(setting -> setting.name);
        return SharedSuggestionProvider.suggest(stream, (SuggestionsBuilder)builder);
    }
}
