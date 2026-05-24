package meteordevelopment.meteorclient.gui.widgets;

import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

public abstract class WTopBar
extends WHorizontalList {
    protected abstract Color getButtonColor(boolean var1, boolean var2);

    protected abstract Color getNameColor();

    public WTopBar() {
        this.spacing = 0.0;
    }

    @Override
    public void init() {
        for (Tab tab : Tabs.get()) {
            this.add(new WTopBarButton(this, tab));
        }
    }

    protected class WTopBarButton
    extends WPressable {
        private final Tab tab;
        final /* synthetic */ WTopBar this$0;

        public WTopBarButton(WTopBar this$0, Tab tab) {
            WTopBar wTopBar = this$0;
            Objects.requireNonNull(wTopBar);
            this.this$0 = wTopBar;
            this.tab = tab;
        }

        @Override
        protected void onCalculateSize() {
            double pad = this.pad();
            this.width = pad + this.theme.textWidth(this.tab.name) + pad;
            this.height = pad + this.theme.textHeight() + pad;
        }

        @Override
        protected void onPressed(int button) {
            block3: {
                block2: {
                    Screen screen = MeteorClient.mc.screen;
                    if (!(screen instanceof TabScreen)) break block2;
                    TabScreen tabScreen = (TabScreen)screen;
                    if (tabScreen.tab == this.tab) break block3;
                }
                double mouseX = MeteorClient.mc.mouseHandler.xpos();
                double mouseY = MeteorClient.mc.mouseHandler.ypos();
                this.tab.openScreen(this.theme);
                GLFW.glfwSetCursorPos((long)MeteorClient.mc.getWindow().handle(), (double)mouseX, (double)mouseY);
            }
        }

        /*
         * Unable to fully structure code
         */
        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            pad = this.pad();
            if (this.pressed) ** GOTO lbl-1000
            var12_6 = MeteorClient.mc.screen;
            if (var12_6 instanceof TabScreen) {
                tabScreen = (TabScreen)var12_6;
                ** if (tabScreen.tab != this.tab) goto lbl-1000
            }
            ** GOTO lbl-1000
lbl-1000:
            // 2 sources

            {
                v0 = true;
                ** GOTO lbl11
            }
lbl-1000:
            // 2 sources

            {
                v0 = false;
            }
lbl11:
            // 2 sources

            color = this.this$0.getButtonColor(v0, this.mouseOver);
            renderer.quad(this.x, this.y, this.width, this.height, color);
            renderer.text(this.tab.name, this.x + pad, this.y + pad, this.this$0.getNameColor(), false);
        }
    }
}
