package meteordevelopment.meteorclient.gui.themes.meteor.widgets;

import java.util.Objects;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;

public class WMeteorWindow
extends WWindow
implements MeteorWidget {
    public WMeteorWindow(WWidget icon, String title) {
        super(icon, title);
    }

    @Override
    protected WWindow.WHeader header(WWidget icon) {
        return new WMeteorHeader(this, icon);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (this.expanded || this.animProgress > 0.0) {
            renderer.quad(this.x, this.y + this.header.height, this.width, this.height - this.header.height, this.theme().backgroundColor.get());
        }
    }

    private class WMeteorHeader
    extends WWindow.WHeader {
        final /* synthetic */ WMeteorWindow this$0;

        public WMeteorHeader(WMeteorWindow wMeteorWindow, WWidget icon) {
            WMeteorWindow wMeteorWindow2 = wMeteorWindow;
            Objects.requireNonNull(wMeteorWindow2);
            this.this$0 = wMeteorWindow2;
            super(wMeteorWindow, icon);
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            renderer.quad(this, this.this$0.theme().accentColor.get());
        }
    }
}
