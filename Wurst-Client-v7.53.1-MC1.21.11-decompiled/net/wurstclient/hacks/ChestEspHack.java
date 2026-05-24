package net.wurstclient.hacks;

import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2586;
import net.minecraft.class_4587;
import net.wurstclient.Category;
import net.wurstclient.events.CameraTransformViewBobbingListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.hacks.chestesp.ChestEspGroup;
import net.wurstclient.hacks.chestesp.ChestEspGroupManager;
import net.wurstclient.settings.EspStyleSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.chunk.ChunkUtils;

public class ChestEspHack
extends Hack
implements UpdateListener,
CameraTransformViewBobbingListener,
RenderListener {
    private final EspStyleSetting style = new EspStyleSetting();
    private final ChestEspGroupManager groups = new ChestEspGroupManager();

    public ChestEspHack() {
        super("ChestESP");
        this.setCategory(Category.RENDER);
        this.addSetting(this.style);
        this.groups.allGroups.stream().flatMap(ChestEspGroup::getSettings).forEach(x$0 -> this.addSetting((Setting)x$0));
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
        this.groups.allGroups.forEach(ChestEspGroup::clear);
    }

    @Override
    public void onUpdate() {
        this.groups.allGroups.forEach(ChestEspGroup::clear);
        ChunkUtils.getLoadedBlockEntities().forEach(be -> this.groups.blockGroups.forEach(group -> group.addIfMatches((class_2586)be)));
        ChestEspHack.MC.field_1687.method_18112().forEach(e -> this.groups.entityGroups.forEach(group -> group.addIfMatches((class_1297)e)));
    }

    @Override
    public void onCameraTransformViewBobbing(CameraTransformViewBobbingListener.CameraTransformViewBobbingEvent event) {
        if (this.style.hasLines()) {
            event.cancel();
        }
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        this.groups.entityGroups.stream().filter(ChestEspGroup::isEnabled).forEach(g -> g.updateBoxes(partialTicks));
        if (this.style.hasBoxes()) {
            this.renderBoxes(matrixStack);
        }
        if (this.style.hasLines()) {
            this.renderTracers(matrixStack, partialTicks);
        }
    }

    private void renderBoxes(class_4587 matrixStack) {
        for (ChestEspGroup group : this.groups.allGroups) {
            if (!group.isEnabled()) continue;
            List<class_238> boxes = group.getBoxes();
            int quadsColor = group.getColorI(64);
            int linesColor = group.getColorI(128);
            RenderUtils.drawSolidBoxes(matrixStack, boxes, quadsColor, false);
            RenderUtils.drawOutlinedBoxes(matrixStack, boxes, linesColor, false);
        }
    }

    private void renderTracers(class_4587 matrixStack, float partialTicks) {
        for (ChestEspGroup group : this.groups.allGroups) {
            if (!group.isEnabled()) continue;
            List<class_238> boxes = group.getBoxes();
            List<class_243> ends = boxes.stream().map(class_238::method_1005).toList();
            int color = group.getColorI(128);
            RenderUtils.drawTracers(matrixStack, partialTicks, ends, color, false);
        }
    }
}
