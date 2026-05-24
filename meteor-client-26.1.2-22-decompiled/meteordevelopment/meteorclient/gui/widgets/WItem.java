package meteordevelopment.meteorclient.gui.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import net.minecraft.world.item.ItemStack;

public class WItem
extends WWidget {
    protected ItemStack itemStack;

    public WItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    protected void onCalculateSize() {
        double s;
        this.width = s = this.theme.scale(32.0);
        this.height = s;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!this.itemStack.isEmpty()) {
            renderer.post(() -> {
                double s = this.theme.scale(2.0);
                renderer.item(this.itemStack, (int)this.x, (int)this.y, (float)s, true);
            });
        }
    }

    public void set(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
}
