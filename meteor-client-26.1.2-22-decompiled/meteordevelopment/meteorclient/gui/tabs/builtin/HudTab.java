package meteordevelopment.meteorclient.gui.tabs.builtin;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.screens.HudEditorScreen;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import net.minecraft.client.gui.screens.Screen;

public class HudTab
extends Tab {
    public HudTab() {
        super("HUD");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new HudScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof HudScreen;
    }

    public static class HudScreen
    extends WindowTabScreen {
        private WContainer settingsContainer;
        private final Hud hud = Hud.get();

        public HudScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);
            this.hud.settings.onActivated();
        }

        @Override
        public void initWidgets() {
            this.settingsContainer = this.add(this.theme.verticalList()).expandX().widget();
            this.settingsContainer.add(this.theme.settings(this.hud.settings)).expandX().widget();
            this.add(this.theme.horizontalSeparator()).expandX();
            WButton openEditor = this.add(this.theme.button("Edit")).expandX().widget();
            openEditor.action = () -> MeteorClient.mc.setScreen((Screen)new HudEditorScreen(this.theme));
            WHorizontalList buttons = this.add(this.theme.horizontalList()).expandX().widget();
            buttons.add(this.theme.confirmedButton((String)"Clear", (String)"Confirm")).expandX().widget().action = this.hud::clear;
            buttons.add(this.theme.confirmedButton((String)"Reset to default elements", (String)"Confirm")).expandX().widget().action = this.hud::resetToDefaultElements;
            this.add(this.theme.horizontalSeparator()).expandX();
            WHorizontalList bottom = this.add(this.theme.horizontalList()).expandX().widget();
            bottom.add(this.theme.label("Active: "));
            WCheckbox active = bottom.add(this.theme.checkbox(this.hud.active)).expandCellX().widget();
            active.action = () -> {
                this.hud.active = active.checked;
            };
            WButton resetSettings = bottom.add(this.theme.button(GuiRenderer.RESET)).widget();
            resetSettings.action = this.hud.settings::reset;
            resetSettings.tooltip = "Reset";
        }

        public void tick() {
            super.tick();
            this.hud.settings.tick(this.settingsContainer, this.theme);
        }

        @Override
        public boolean toClipboard() {
            return NbtUtils.toClipboard(this.hud);
        }

        @Override
        public boolean fromClipboard() {
            return NbtUtils.fromClipboard(this.hud);
        }
    }
}
