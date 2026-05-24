package meteordevelopment.meteorclient.gui.themes.meteor.widgets;

import java.util.Objects;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorWidget;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.pressable.WTriangle;

public class WMeteorSection
extends WSection {
    public WMeteorSection(String title, boolean expanded, WWidget headerWidget) {
        super(title, expanded, headerWidget);
    }

    @Override
    protected WSection.WHeader createHeader() {
        return new WMeteorHeader(this, this.title);
    }

    protected class WMeteorHeader
    extends WSection.WHeader {
        private WTriangle triangle;
        final /* synthetic */ WMeteorSection this$0;

        public WMeteorHeader(WMeteorSection this$0, String title) {
            WMeteorSection wMeteorSection = this$0;
            Objects.requireNonNull(wMeteorSection);
            this.this$0 = wMeteorSection;
            super(this$0, title);
        }

        @Override
        public void init() {
            this.add(this.theme.horizontalSeparator(this.title)).expandX();
            if (this.this$0.headerWidget != null) {
                this.add(this.this$0.headerWidget);
            }
            this.triangle = new WHeaderTriangle();
            this.triangle.theme = this.theme;
            WMeteorHeader wMeteorHeader = this;
            this.triangle.action = () -> wMeteorHeader.onClick();
            this.add(this.triangle);
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            this.triangle.rotation = (1.0 - this.this$0.animProgress) * -90.0;
        }
    }

    protected static class WHeaderTriangle
    extends WTriangle
    implements MeteorWidget {
        protected WHeaderTriangle() {
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            renderer.rotatedQuad(this.x, this.y, this.width, this.height, this.rotation, GuiRenderer.TRIANGLE, this.theme().textColor.get());
        }
    }
}
