package meteordevelopment.meteorclient.utils.render.prompts;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.systems.config.Config;
import net.minecraft.client.gui.screens.Screen;

public abstract class Prompt<T> {
    protected final GuiTheme theme;
    protected final Screen parent;
    protected String title = "";
    protected final List<String> messages = new ArrayList<String>();
    protected boolean dontShowAgainCheckboxVisible = true;
    protected String id = null;

    protected Prompt(GuiTheme theme, Screen parent) {
        this.theme = theme;
        this.parent = parent;
    }

    public T title(String title) {
        this.title = title;
        return (T)this;
    }

    public T message(String message) {
        this.messages.add(message);
        return (T)this;
    }

    public T message(String message, Object ... args) {
        this.messages.add(String.format(message, args));
        return (T)this;
    }

    public T dontShowAgainCheckboxVisible(boolean visible) {
        this.dontShowAgainCheckboxVisible = visible;
        return (T)this;
    }

    public T id(String from) {
        this.id = from;
        return (T)this;
    }

    public boolean show() {
        if (this.id != null && Config.get().dontShowAgainPrompts.contains(this.id)) {
            return false;
        }
        if (!RenderSystem.isOnRenderThread()) {
            MeteorClient.mc.execute(() -> MeteorClient.mc.setScreen((Screen)new PromptScreen(this, this.theme)));
        } else {
            MeteorClient.mc.setScreen((Screen)new PromptScreen(this, this.theme));
        }
        return true;
    }

    protected void dontShowAgain(PromptScreen screen) {
        if (screen.dontShowAgainCheckbox != null && screen.dontShowAgainCheckbox.checked && this.id != null) {
            Config.get().dontShowAgainPrompts.add(this.id);
        }
    }

    protected abstract void initialiseWidgets(PromptScreen var1);

    protected class PromptScreen
    extends WindowScreen {
        protected WCheckbox dontShowAgainCheckbox;
        protected WHorizontalList list;
        final /* synthetic */ Prompt this$0;

        public PromptScreen(Prompt this$0, GuiTheme theme) {
            Prompt prompt = this$0;
            Objects.requireNonNull(prompt);
            this.this$0 = prompt;
            super(theme, this$0.title);
            this.parent = this$0.parent;
        }

        @Override
        public void initWidgets() {
            for (String line : this.this$0.messages) {
                this.add(this.theme.label(line)).expandX();
            }
            this.add(this.theme.horizontalSeparator()).expandX();
            if (this.this$0.dontShowAgainCheckboxVisible) {
                WHorizontalList checkboxContainer = this.add(this.theme.horizontalList()).expandX().widget();
                this.dontShowAgainCheckbox = checkboxContainer.add(this.theme.checkbox(false)).widget();
                checkboxContainer.add(this.theme.label("Don't show this again.")).expandX();
            } else {
                this.dontShowAgainCheckbox = null;
            }
            this.list = this.add(this.theme.horizontalList()).expandX().widget();
            this.this$0.initialiseWidgets(this);
        }
    }
}
