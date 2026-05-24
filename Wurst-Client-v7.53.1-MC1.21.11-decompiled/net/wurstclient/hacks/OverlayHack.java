package net.wurstclient.hacks;

import net.minecraft.class_239;
import net.minecraft.class_3965;
import net.minecraft.class_4587;
import net.wurstclient.Category;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.util.OverlayRenderer;

public final class OverlayHack
extends Hack
implements UpdateListener,
RenderListener {
    private final OverlayRenderer renderer = new OverlayRenderer();

    public OverlayHack() {
        super("Overlay");
        this.setCategory(Category.RENDER);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(RenderListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(RenderListener.class, this);
        this.renderer.resetProgress();
    }

    @Override
    public void onUpdate() {
        if (OverlayHack.MC.field_1761.method_2923()) {
            this.renderer.updateProgress();
        } else {
            this.renderer.resetProgress();
        }
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        class_3965 blockHitResult;
        if (!OverlayHack.MC.field_1761.method_2923()) {
            return;
        }
        class_239 class_2392 = OverlayHack.MC.field_1765;
        if (!(class_2392 instanceof class_3965) || (blockHitResult = (class_3965)class_2392).method_17783() != class_239.class_240.field_1332) {
            return;
        }
        this.renderer.render(matrixStack, partialTicks, blockHitResult.method_17777());
    }
}
