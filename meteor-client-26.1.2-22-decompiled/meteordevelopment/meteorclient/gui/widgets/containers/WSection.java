package meteordevelopment.meteorclient.gui.widgets.containers;

import java.util.Objects;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Mth;

public abstract class WSection
extends WVerticalList {
    public Runnable action;
    protected String title;
    protected boolean expanded;
    protected double animProgress;
    private WHeader header;
    protected final WWidget headerWidget;
    private double actualWidth;
    private double actualHeight;
    private double forcedHeight = -1.0;
    private boolean firstTime = true;

    public WSection(String title, boolean expanded, WWidget headerWidget) {
        this.title = title;
        this.expanded = expanded;
        this.headerWidget = headerWidget;
        this.animProgress = expanded ? 1.0 : 0.0;
    }

    @Override
    public void init() {
        this.header = this.createHeader();
        this.header.theme = this.theme;
        super.add(this.header).expandX();
    }

    @Override
    public <T extends WWidget> Cell<T> add(T widget) {
        return super.add(widget).padHorizontal(6.0);
    }

    protected abstract WHeader createHeader();

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean isExpanded() {
        return this.expanded;
    }

    @Override
    protected void onCalculateSize() {
        if (this.forcedHeight == -1.0) {
            super.onCalculateSize();
            this.actualWidth = this.width;
            this.actualHeight = this.height;
        } else {
            this.width = this.actualWidth;
            this.height = this.forcedHeight;
            if (this.animProgress == 1.0) {
                this.forcedHeight = -1.0;
            }
        }
        if (this.firstTime) {
            this.firstTime = false;
            this.forcedHeight = (this.actualHeight - this.header.height) * this.animProgress + this.header.height;
            this.onCalculateSize();
        }
    }

    @Override
    public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        boolean scissor;
        if (!this.visible) {
            return true;
        }
        double preProgress = this.animProgress;
        this.animProgress += (double)(this.expanded ? 1 : -1) * delta * 14.0;
        this.animProgress = Mth.clamp((double)this.animProgress, (double)0.0, (double)1.0);
        if (this.animProgress != preProgress) {
            this.forcedHeight = (this.actualHeight - this.header.height) * this.animProgress + this.header.height;
            this.invalidate();
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
    }

    @Override
    protected boolean propagateEvents(WWidget widget) {
        return this.expanded || widget instanceof WHeader;
    }

    protected abstract class WHeader
    extends WHorizontalList {
        protected String title;
        final /* synthetic */ WSection this$0;

        public WHeader(WSection this$0, String title) {
            WSection wSection = this$0;
            Objects.requireNonNull(wSection);
            this.this$0 = wSection;
            this.title = title;
        }

        @Override
        public boolean onMouseClicked(MouseButtonEvent click, boolean doubled) {
            if (this.mouseOver && click.button() == 0 && !doubled) {
                this.onClick();
                return true;
            }
            return false;
        }

        protected void onClick() {
            this.this$0.setExpanded(!this.this$0.expanded);
            if (this.this$0.action != null) {
                this.this$0.action.run();
            }
        }
    }
}
