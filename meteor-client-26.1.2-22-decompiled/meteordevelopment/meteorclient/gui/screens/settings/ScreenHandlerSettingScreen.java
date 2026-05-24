package meteordevelopment.meteorclient.gui.screens.settings;

import java.util.Collection;
import java.util.List;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.screens.settings.base.CollectionListSettingScreen;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;

public class ScreenHandlerSettingScreen
extends CollectionListSettingScreen<MenuType<?>> {
    public ScreenHandlerSettingScreen(GuiTheme theme, Setting<List<MenuType<?>>> setting) {
        super(theme, "Select Screen Handlers", setting, (Collection)setting.get(), BuiltInRegistries.MENU);
    }

    @Override
    protected WWidget getValueWidget(MenuType<?> value) {
        return this.theme.label(ScreenHandlerSettingScreen.getName(value));
    }

    @Override
    protected String[] getValueNames(MenuType<?> type) {
        return new String[]{ScreenHandlerSettingScreen.getName(type)};
    }

    private static String getName(MenuType<?> type) {
        return BuiltInRegistries.MENU.getKey(type).toString();
    }
}
