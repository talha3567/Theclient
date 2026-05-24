package meteordevelopment.meteorclient.gui.tabs.builtin;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.input.WDropdown;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import net.minecraft.client.gui.screens.Screen;

public class GuiTab
extends Tab {
    public GuiTab() {
        super("GUI");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new GuiScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof GuiScreen;
    }

    private static class GuiScreen
    extends WindowTabScreen {
        public GuiScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);
            theme.settings.onActivated();
        }

        @Override
        public void initWidgets() {
            WHorizontalList opts = this.add(this.theme.horizontalList()).expandX().widget();
            opts.add(this.theme.label("Theme:"));
            WDropdown<String> themeW = opts.add(this.theme.dropdown(GuiThemes.getNames(), GuiThemes.get().name)).widget();
            themeW.action = () -> {
                GuiThemes.select((String)themeW.get());
                MeteorClient.mc.setScreen(null);
                this.tab.openScreen(GuiThemes.get());
            };
            WButton resetLayout = opts.add(this.theme.button("Reset Layout")).expandX().widget();
            resetLayout.action = this.theme::clearWindowConfigs;
            WButton reset = opts.add(this.theme.button("Reset Colors")).right().widget();
            reset.action = () -> {
                this.theme.settings.reset();
                MeteorClient.mc.setScreen(null);
                this.tab.openScreen(GuiThemes.get());
            };
            WButton copyButton = opts.add(this.theme.button(GuiRenderer.COPY)).widget();
            copyButton.action = this::toClipboard;
            copyButton.tooltip = "Copy config";
            WButton pasteButton = opts.add(this.theme.button(GuiRenderer.PASTE)).right().widget();
            pasteButton.action = this::fromClipboard;
            pasteButton.tooltip = "Paste config";
            this.add(this.theme.settings(this.theme.settings)).expandX();
        }

        @Override
        public boolean toClipboard() {
            return NbtUtils.toClipboard(this.theme);
        }

        @Override
        public boolean fromClipboard() {
            return NbtUtils.fromClipboard(this.theme);
        }
    }
}
