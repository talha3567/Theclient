package net.wurstclient.hacks;

import java.util.Optional;
import net.minecraft.class_1536;
import net.minecraft.class_238;
import net.minecraft.class_4587;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.RenderListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.util.RenderUtils;

@SearchTags(value={"open water esp", "AutoFishESP", "auto fish esp"})
public final class OpenWaterEspHack
extends Hack
implements RenderListener {
    public OpenWaterEspHack() {
        super("OpenWaterESP");
        this.setCategory(Category.RENDER);
    }

    @Override
    public String getRenderName() {
        class_1536 bobber = Optional.ofNullable(OpenWaterEspHack.MC.field_1724).map(player -> player.field_7513).orElse(null);
        if (bobber == null) {
            return this.getName();
        }
        return this.getName() + (this.isInOpenWater(bobber) ? " [open]" : " [shallow]");
    }

    @Override
    protected void onEnable() {
        EVENTS.add(RenderListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(RenderListener.class, this);
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        int color;
        class_1536 bobber = OpenWaterEspHack.MC.field_1724.field_7513;
        if (bobber == null) {
            return;
        }
        class_238 box = new class_238(-2.0, -1.0, -2.0, 3.0, 2.0, 3.0).method_996(bobber.method_24515());
        boolean inOpenWater = this.isInOpenWater(bobber);
        int n = color = inOpenWater ? -2147418368 : -2130771968;
        if (!inOpenWater) {
            RenderUtils.drawCrossBox(matrixStack, box, color, false);
        }
        RenderUtils.drawOutlinedBox(matrixStack, box, color, false);
    }

    private boolean isInOpenWater(class_1536 bobber) {
        return bobber.method_26086(bobber.method_24515());
    }
}
