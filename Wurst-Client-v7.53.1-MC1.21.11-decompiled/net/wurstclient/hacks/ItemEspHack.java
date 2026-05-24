package net.wurstclient.hacks;

import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.class_1297;
import net.minecraft.class_1542;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.CameraTransformViewBobbingListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.EspBoxSizeSetting;
import net.wurstclient.settings.EspStyleSetting;
import net.wurstclient.util.EntityUtils;
import net.wurstclient.util.RenderUtils;

@SearchTags(value={"item esp", "ItemTracers", "item tracers"})
public final class ItemEspHack
extends Hack
implements UpdateListener,
CameraTransformViewBobbingListener,
RenderListener {
    private final EspStyleSetting style = new EspStyleSetting();
    private final EspBoxSizeSetting boxSize = new EspBoxSizeSetting("\u00a7lAccurate\u00a7r mode shows the exact hitbox of each item.\n\u00a7lFancy\u00a7r mode shows larger boxes that look better.");
    private final ColorSetting color = new ColorSetting("Color", "Items will be highlighted in this color.", Color.YELLOW);
    private final ArrayList<class_1542> items = new ArrayList();

    public ItemEspHack() {
        super("ItemESP");
        this.setCategory(Category.RENDER);
        this.addSetting(this.style);
        this.addSetting(this.boxSize);
        this.addSetting(this.color);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(CameraTransformViewBobbingListener.class, this);
        EVENTS.add(RenderListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(CameraTransformViewBobbingListener.class, this);
        EVENTS.remove(RenderListener.class, this);
    }

    @Override
    public void onUpdate() {
        this.items.clear();
        for (class_1297 entity : ItemEspHack.MC.field_1687.method_18112()) {
            if (!(entity instanceof class_1542)) continue;
            this.items.add((class_1542)entity);
        }
    }

    @Override
    public void onCameraTransformViewBobbing(CameraTransformViewBobbingListener.CameraTransformViewBobbingEvent event) {
        if (this.style.hasLines()) {
            event.cancel();
        }
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        int lineColor = this.color.getColorI(128);
        if (this.style.hasBoxes()) {
            double extraSize = this.boxSize.getExtraSize() / 2.0f;
            ArrayList<class_238> boxes = new ArrayList<class_238>(this.items.size());
            for (class_1542 e : this.items) {
                boxes.add(EntityUtils.getLerpedBox((class_1297)e, partialTicks).method_989(0.0, extraSize, 0.0).method_1014(extraSize));
            }
            RenderUtils.drawOutlinedBoxes(matrixStack, boxes, lineColor, false);
        }
        if (this.style.hasLines()) {
            ArrayList<class_243> ends = new ArrayList<class_243>(this.items.size());
            for (class_1542 e : this.items) {
                ends.add(EntityUtils.getLerpedBox((class_1297)e, partialTicks).method_1005());
            }
            RenderUtils.drawTracers(matrixStack, partialTicks, ends, lineColor, false);
        }
    }
}
