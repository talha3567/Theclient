package meteordevelopment.meteorclient.gui.widgets.containers;

import java.util.Objects;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.utils.WindowConfig;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WView;
import meteordevelopment.meteorclient.gui.widgets.pressable.WTriangle;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Mth;

public abstract class WWindow
extends WVerticalList {
    public double padding = 8.0;
    public Consumer<WContainer> beforeHeaderInit;
    public String id;
    public final WWidget icon;
    protected final String title;
    protected WHeader header;
    public WView view;
    protected boolean dragging;
    protected boolean expanded = true;
    protected boolean dragged;
    protected double animProgress = 1.0;
    protected boolean moved = false;
    protected double movedX;
    protected double movedY;
    private boolean propagateEventsExpanded;

    public WWindow(WWidget icon, String title) {
        this.icon = icon;
        this.title = title;
    }

    @Override
    public void init() {
        this.header = this.header(this.icon);
        this.header.theme = this.theme;
        super.add(this.header).expandWidgetX().widget();
        this.view = super.add(this.theme.view()).expandX().pad(this.padding).widget();
        if (this.id != null) {
            this.expanded = this.theme.getWindowConfig((String)this.id).expanded;
            this.animProgress = this.expanded ? 1.0 : 0.0;
        }
    }

    protected abstract WHeader header(WWidget var1);

    @Override
    public <T extends WWidget> Cell<T> add(T widget) {
        return this.view.add(widget);
    }

    @Override
    public void clear() {
        this.view.clear();
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        if (this.id != null) {
            WindowConfig config = this.theme.getWindowConfig(this.id);
            config.expanded = expanded;
        }
    }

    @Override
    protected void onCalculateWidgetPositions() {
        if (this.id != null) {
            WindowConfig config = this.theme.getWindowConfig(this.id);
            if (config.x != -1.0) {
                this.x = config.x;
                if (this.x + this.width > (double)Utils.getWindowWidth()) {
                    this.x = (double)Utils.getWindowWidth() - this.width;
                }
            }
            if (config.y != -1.0) {
                this.y = config.y;
                if (this.y + this.height > (double)Utils.getWindowHeight()) {
                    this.y = (double)Utils.getWindowHeight() - this.height;
                }
            }
        }
        super.onCalculateWidgetPositions();
        if (this.moved) {
            this.move(this.movedX - this.x, this.movedY - this.y);
        }
    }

    @Override
    public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        boolean scissor;
        if (!this.visible) {
            return true;
        }
        boolean bl = scissor = this.animProgress != 0.0 && this.animProgress != 1.0 || this.expanded && this.animProgress != 1.0;
        if (scissor) {
            renderer.scissorStart(this.x, this.y, this.width, (this.height - this.header.height) * this.animProgress + this.header.height);
        }
        boolean toReturn = super.render(renderer, mouseX, mouseY, delta);
        if (scissor) {
            renderer.scissorEnd();
        }
        return toReturn;
    }

    @Override
    protected void renderWidget(WWidget widget, GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (this.expanded || this.animProgress > 0.0 || widget instanceof WHeader) {
            widget.render(renderer, mouseX, mouseY, delta);
        }
        this.propagateEventsExpanded = this.expanded;
    }

    @Override
    protected boolean propagateEvents(WWidget widget) {
        return widget instanceof WHeader || this.propagateEventsExpanded;
    }

    protected abstract class WHeader
    extends WContainer {
        private final WWidget icon;
        private WTriangle triangle;
        private WHorizontalList list;
        final /* synthetic */ WWindow this$0;

        public WHeader(WWindow this$0, WWidget icon) {
            WWindow wWindow = this$0;
            Objects.requireNonNull(wWindow);
            this.this$0 = wWindow;
            this.icon = icon;
        }

        @Override
        public void init() {
            if (this.icon != null) {
                this.createList();
                this.add(this.icon).centerY();
            }
            if (this.this$0.beforeHeaderInit != null) {
                this.createList();
                this.this$0.beforeHeaderInit.accept(this);
            }
            this.add(this.theme.label(this.this$0.title, true)).expandCellX().center().pad(4.0);
            this.triangle = this.add(this.theme.triangle()).pad(4.0).right().centerY().widget();
            this.triangle.action = () -> this.this$0.setExpanded(!this.this$0.expanded);
        }

        private void createList() {
            this.list = this.add(this.theme.horizontalList()).expandX().widget();
            this.list.spacing = 0.0;
        }

        @Override
        public <T extends WWidget> Cell<T> add(T widget) {
            if (this.list != null) {
                return this.list.add(widget);
            }
            return super.add(widget);
        }

        @Override
        protected void onCalculateSize() {
            this.width = 0.0;
            this.height = 0.0;
            for (Cell cell : this.cells) {
                double w = cell.padLeft() + ((WWidget)cell.widget()).width + cell.padRight();
                if (cell.widget() instanceof WTriangle) {
                    w *= 2.0;
                }
                this.width += w;
                this.height = Math.max(this.height, cell.padTop() + ((WWidget)cell.widget()).height + cell.padBottom());
            }
        }

        @Override
        public boolean onMouseClicked(MouseButtonEvent click, boolean doubled) {
            if (this.mouseOver && !doubled) {
                if (click.button() == 1) {
                    this.this$0.setExpanded(!this.this$0.expanded);
                } else {
                    this.this$0.dragging = true;
                    this.this$0.dragged = false;
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean onMouseReleased(MouseButtonEvent click) {
            if (this.this$0.dragging) {
                this.this$0.dragging = false;
                if (!this.this$0.dragged) {
                    this.this$0.setExpanded(!this.this$0.expanded);
                }
            }
            return false;
        }

        @Override
        public void onMouseMoved(double mouseX, double mouseY, double lastMouseX, double lastMouseY) {
            if (this.this$0.dragging) {
                this.this$0.move(mouseX - lastMouseX, mouseY - lastMouseY);
                this.this$0.moved = true;
                this.this$0.movedX = this.x;
                this.this$0.movedY = this.y;
                if (this.this$0.id != null) {
                    WindowConfig config = this.theme.getWindowConfig(this.this$0.id);
                    config.x = this.x;
                    config.y = this.y;
                }
                this.this$0.dragged = true;
            }
        }

        @Override
        public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            this.this$0.animProgress = this.this$0.animProgress + (double)(this.this$0.expanded ? 1 : -1) * delta * 14.0;
            this.this$0.animProgress = Mth.clamp((double)this.this$0.animProgress, (double)0.0, (double)1.0);
            this.triangle.rotation = (1.0 - this.this$0.animProgress) * -90.0;
            return super.render(renderer, mouseX, mouseY, delta);
        }
    }
}
