package meteordevelopment.meteorclient.gui;

import com.mojang.blaze3d.platform.MacosUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiKeyEvents;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiDebugRenderer;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WRoot;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.CursorStyle;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public abstract class WidgetScreen
extends Screen {
    private static final GuiRenderer RENDERER = new GuiRenderer();
    private static final GuiDebugRenderer DEBUG_RENDERER = new GuiDebugRenderer();
    public Runnable taskAfterRender;
    protected Runnable enterAction;
    public Screen parent;
    private final WContainer root;
    protected final GuiTheme theme;
    public boolean locked;
    public boolean lockedAllowClose;
    private boolean closed;
    private boolean onClose;
    private boolean debug;
    private boolean closing;
    private double lastMouseX;
    private double lastMouseY;
    public double animProgress;
    private List<Runnable> onClosed;
    protected boolean firstInit = true;

    public WidgetScreen(GuiTheme theme, String title) {
        super((Component)Component.literal((String)title));
        this.parent = MeteorClient.mc.screen;
        this.root = new WFullScreenRoot();
        this.theme = theme;
        this.root.theme = theme;
        if (this.parent != null) {
            this.animProgress = 1.0;
            if (this instanceof TabScreen && this.parent instanceof TabScreen) {
                this.parent = ((TabScreen)this.parent).parent;
            }
        }
    }

    public <W extends WWidget> Cell<W> add(W widget) {
        return this.root.add(widget);
    }

    public void clear() {
        this.root.clear();
    }

    public void invalidate() {
        this.root.invalidate();
    }

    protected void init() {
        MeteorClient.EVENT_BUS.subscribe((Object)this);
        this.closed = false;
        if (this.firstInit) {
            this.firstInit = false;
            this.initWidgets();
        }
    }

    public abstract void initWidgets();

    public void reload() {
        this.clear();
        this.initWidgets();
    }

    public void onClosed(Runnable action) {
        if (this.onClosed == null) {
            this.onClosed = new ArrayList<Runnable>(2);
        }
        this.onClosed.add(action);
    }

    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (this.locked) {
            return false;
        }
        double mouseX = click.x();
        double mouseY = click.y();
        double s = MeteorClient.mc.getWindow().getGuiScale();
        return this.root.mouseClicked(new MouseButtonEvent(mouseX *= s, mouseY *= s, click.buttonInfo()), doubled);
    }

    public boolean mouseReleased(MouseButtonEvent click) {
        if (this.locked) {
            return false;
        }
        double mouseX = click.x();
        double mouseY = click.y();
        double s = MeteorClient.mc.getWindow().getGuiScale();
        mouseX *= s;
        mouseY *= s;
        if (this.debug && click.button() == 1) {
            DEBUG_RENDERER.mouseReleased(this.root, new MouseButtonEvent(mouseX, mouseY, click.buttonInfo()), 0);
        }
        return this.root.mouseReleased(new MouseButtonEvent(mouseX, mouseY, click.buttonInfo()));
    }

    public void mouseMoved(double mouseX, double mouseY) {
        if (this.locked) {
            return;
        }
        double s = MeteorClient.mc.getWindow().getGuiScale();
        this.root.mouseMoved(mouseX *= s, mouseY *= s, this.lastMouseX, this.lastMouseY);
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.locked) {
            return false;
        }
        this.root.mouseScrolled(verticalAmount);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean keyReleased(KeyEvent input) {
        if (this.locked) {
            return false;
        }
        if ((input.modifiers() == 2 || input.modifiers() == 8) && input.key() == 57) {
            this.debug = !this.debug;
            return true;
        }
        if ((input.key() == 257 || input.key() == 335) && this.enterAction != null) {
            this.enterAction.run();
            return true;
        }
        return super.keyReleased(input);
    }

    public boolean keyPressed(KeyEvent input) {
        boolean shouldReturn;
        if (this.locked) {
            return false;
        }
        boolean bl = shouldReturn = this.root.keyPressed(input) || super.keyPressed(input);
        if (shouldReturn) {
            return true;
        }
        if (input.key() == 258) {
            AtomicReference<Object> firstTextBox = new AtomicReference<Object>(null);
            AtomicBoolean done = new AtomicBoolean(false);
            AtomicBoolean foundFocused = new AtomicBoolean(false);
            this.loopWidgets(this.root, wWidget -> {
                if (done.get() || !(wWidget instanceof WTextBox)) {
                    return;
                }
                WTextBox textBox = (WTextBox)wWidget;
                if (foundFocused.get()) {
                    textBox.setFocused(true);
                    textBox.setCursorMax();
                    done.set(true);
                } else if (textBox.isFocused()) {
                    textBox.setFocused(false);
                    foundFocused.set(true);
                }
                if (firstTextBox.get() == null) {
                    firstTextBox.set(textBox);
                }
            });
            if (!done.get() && firstTextBox.get() != null) {
                ((WTextBox)firstTextBox.get()).setFocused(true);
                ((WTextBox)firstTextBox.get()).setCursorMax();
            }
            return true;
        }
        boolean control = MacosUtil.IS_MACOS ? input.modifiers() == 8 : input.modifiers() == 2;
        return control && input.key() == 67 && this.toClipboard() || control && input.key() == 86 && this.fromClipboard();
    }

    public void keyRepeated(KeyEvent input) {
        if (this.locked) {
            return;
        }
        this.root.keyRepeated(input);
    }

    public boolean charTyped(CharacterEvent input) {
        if (this.locked) {
            return false;
        }
        return this.root.charTyped(input);
    }

    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
        if (this.minecraft.level == null) {
            this.extractPanorama(graphics, deltaTicks);
        }
    }

    public void renderCustom(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int s = MeteorClient.mc.getWindow().getGuiScale();
        mouseX *= s;
        mouseY *= s;
        this.animProgress += (double)(delta / 20.0f * 14.0f * (float)(this.closing ? -1 : 1));
        this.animProgress = Mth.clamp((double)this.animProgress, (double)0.0, (double)1.0);
        if (this.closing && (this.animProgress == 0.0 || this.parent != null)) {
            this.closeInternal();
        }
        GuiKeyEvents.canUseKeys = true;
        Utils.unscaledProjection();
        this.onRenderBefore(graphics, mouseX, mouseY, delta);
        WidgetScreen.RENDERER.theme = this.theme;
        this.theme.beforeRender();
        RENDERER.begin(graphics);
        RENDERER.setAlpha(this.animProgress);
        this.root.render(RENDERER, mouseX, mouseY, delta / 20.0f);
        RENDERER.setAlpha(1.0);
        RENDERER.end();
        boolean tooltip = RENDERER.renderTooltip(graphics, mouseX, mouseY, delta / 20.0f);
        if (this.debug) {
            DEBUG_RENDERER.render(this.root);
            if (tooltip) {
                DEBUG_RENDERER.render(WidgetScreen.RENDERER.tooltipWidget);
            }
        }
        Utils.scaledProjection();
        this.runAfterRenderTasks();
    }

    protected void runAfterRenderTasks() {
        if (this.taskAfterRender != null) {
            this.taskAfterRender.run();
            this.taskAfterRender = null;
        }
    }

    protected void onRenderBefore(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    }

    public void resize(int width, int height) {
        super.resize(width, height);
        this.root.invalidate();
    }

    public void onClose() {
        if (!this.locked || this.lockedAllowClose) {
            this.closing = true;
        }
    }

    public void removed() {
        if (!this.closed || this.lockedAllowClose) {
            this.closed = true;
            this.onClosed();
            Input.setCursorStyle(CursorStyle.Default);
            this.loopWidgets(this.root, widget -> {
                WTextBox textBox;
                if (widget instanceof WTextBox && (textBox = (WTextBox)widget).isFocused()) {
                    textBox.setFocused(false);
                }
            });
            MeteorClient.EVENT_BUS.unsubscribe((Object)this);
            GuiKeyEvents.canUseKeys = true;
            if (this.onClosed != null) {
                for (Runnable action : this.onClosed) {
                    action.run();
                }
            }
            if (this.onClose) {
                this.taskAfterRender = () -> {
                    this.locked = true;
                    MeteorClient.mc.setScreen(this.parent);
                };
            }
        }
    }

    private void closeInternal() {
        boolean preOnClose = this.onClose;
        this.onClose = true;
        super.onClose();
        this.removed();
        this.onClose = preOnClose;
    }

    private void loopWidgets(WWidget widget, Consumer<WWidget> action) {
        action.accept(widget);
        if (widget instanceof WContainer) {
            WContainer wContainer = (WContainer)widget;
            for (Cell<?> cell : wContainer.cells) {
                this.loopWidgets((WWidget)cell.widget(), action);
            }
        }
    }

    protected void onClosed() {
    }

    public boolean toClipboard() {
        return false;
    }

    public boolean fromClipboard() {
        return false;
    }

    public boolean shouldCloseOnEsc() {
        return !this.locked || this.lockedAllowClose;
    }

    public boolean isPauseScreen() {
        return false;
    }

    private static class WFullScreenRoot
    extends WContainer
    implements WRoot {
        private boolean valid;

        private WFullScreenRoot() {
        }

        @Override
        public void invalidate() {
            this.valid = false;
        }

        @Override
        protected void onCalculateSize() {
            this.width = Utils.getWindowWidth();
            this.height = Utils.getWindowHeight();
        }

        @Override
        protected void onCalculateWidgetPositions() {
            for (Cell cell : this.cells) {
                cell.x = 0.0;
                cell.y = 0.0;
                cell.width = this.width;
                cell.height = this.height;
                cell.alignWidget();
            }
        }

        @Override
        public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            if (!this.valid) {
                this.calculateSize();
                this.calculateWidgetPositions();
                this.valid = true;
                this.mouseMoved(MeteorClient.mc.mouseHandler.xpos(), MeteorClient.mc.mouseHandler.ypos(), MeteorClient.mc.mouseHandler.xpos(), MeteorClient.mc.mouseHandler.ypos());
            }
            return super.render(renderer, mouseX, mouseY, delta);
        }
    }
}
