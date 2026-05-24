package net.wurstclient.hacks;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_4587;
import net.wurstclient.Category;
import net.wurstclient.events.CameraTransformViewBobbingListener;
import net.wurstclient.events.PacketInputListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.hacks.portalesp.PortalEspBlockGroup;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ChunkAreaSetting;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.EspStyleSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.chunk.ChunkSearcher;
import net.wurstclient.util.chunk.ChunkSearcherCoordinator;

public final class PortalEspHack
extends Hack
implements UpdateListener,
CameraTransformViewBobbingListener,
RenderListener {
    private final EspStyleSetting style = new EspStyleSetting();
    private final PortalEspBlockGroup netherPortal = new PortalEspBlockGroup(class_2246.field_10316, new ColorSetting("Nether portal color", "Nether portals will be highlighted in this color.", Color.RED), new CheckboxSetting("Include nether portals", true));
    private final PortalEspBlockGroup endPortal = new PortalEspBlockGroup(class_2246.field_10027, new ColorSetting("End portal color", "End portals will be highlighted in this color.", Color.GREEN), new CheckboxSetting("Include end portals", true));
    private final PortalEspBlockGroup endPortalFrame = new PortalEspBlockGroup(class_2246.field_10398, new ColorSetting("End portal frame color", "End portal frames will be highlighted in this color.", Color.BLUE), new CheckboxSetting("Include end portal frames", true));
    private final PortalEspBlockGroup endGateway = new PortalEspBlockGroup(class_2246.field_10613, new ColorSetting("End gateway color", "End gateways will be highlighted in this color.", Color.YELLOW), new CheckboxSetting("Include end gateways", true));
    private final List<PortalEspBlockGroup> groups = Arrays.asList(this.netherPortal, this.endPortal, this.endPortalFrame, this.endGateway);
    private final ChunkAreaSetting area = new ChunkAreaSetting("Area", "The area around the player to search in.\nHigher values require a faster computer.");
    private final BiPredicate<class_2338, class_2680> query = (pos, state) -> state.method_26204() == class_2246.field_10316 || state.method_26204() == class_2246.field_10027 || state.method_26204() == class_2246.field_10398 || state.method_26204() == class_2246.field_10613;
    private final ChunkSearcherCoordinator coordinator = new ChunkSearcherCoordinator(this.query, this.area);
    private boolean groupsUpToDate;

    public PortalEspHack() {
        super("PortalESP");
        this.setCategory(Category.RENDER);
        this.addSetting(this.style);
        this.groups.stream().flatMap(PortalEspBlockGroup::getSettings).forEach(x$0 -> this.addSetting((Setting)x$0));
        this.addSetting(this.area);
    }

    @Override
    protected void onEnable() {
        this.groupsUpToDate = false;
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(PacketInputListener.class, this.coordinator);
        EVENTS.add(CameraTransformViewBobbingListener.class, this);
        EVENTS.add(RenderListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(PacketInputListener.class, this.coordinator);
        EVENTS.remove(CameraTransformViewBobbingListener.class, this);
        EVENTS.remove(RenderListener.class, this);
        this.coordinator.reset();
        this.groups.forEach(PortalEspBlockGroup::clear);
    }

    @Override
    public void onCameraTransformViewBobbing(CameraTransformViewBobbingListener.CameraTransformViewBobbingEvent event) {
        if (((EspStyleSetting.EspStyle)((Object)this.style.getSelected())).hasLines()) {
            event.cancel();
        }
    }

    @Override
    public void onUpdate() {
        boolean searchersChanged = this.coordinator.update();
        if (searchersChanged) {
            this.groupsUpToDate = false;
        }
        if (!this.groupsUpToDate && this.coordinator.isDone()) {
            this.updateGroupBoxes();
        }
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        if (((EspStyleSetting.EspStyle)((Object)this.style.getSelected())).hasBoxes()) {
            this.renderBoxes(matrixStack);
        }
        if (((EspStyleSetting.EspStyle)((Object)this.style.getSelected())).hasLines()) {
            this.renderTracers(matrixStack, partialTicks);
        }
    }

    private void renderBoxes(class_4587 matrixStack) {
        for (PortalEspBlockGroup group : this.groups) {
            if (!group.isEnabled()) {
                return;
            }
            List<class_238> boxes = group.getBoxes();
            int quadsColor = group.getColorI(64);
            int linesColor = group.getColorI(128);
            RenderUtils.drawSolidBoxes(matrixStack, boxes, quadsColor, false);
            RenderUtils.drawOutlinedBoxes(matrixStack, boxes, linesColor, false);
        }
    }

    private void renderTracers(class_4587 matrixStack, float partialTicks) {
        for (PortalEspBlockGroup group : this.groups) {
            if (!group.isEnabled()) {
                return;
            }
            List<class_238> boxes = group.getBoxes();
            List<class_243> ends = boxes.stream().map(class_238::method_1005).toList();
            int color = group.getColorI(128);
            RenderUtils.drawTracers(matrixStack, partialTicks, ends, color, false);
        }
    }

    private void updateGroupBoxes() {
        this.groups.forEach(PortalEspBlockGroup::clear);
        this.coordinator.getMatches().forEach(this::addToGroupBoxes);
        this.groupsUpToDate = true;
    }

    private void addToGroupBoxes(ChunkSearcher.Result result) {
        for (PortalEspBlockGroup group : this.groups) {
            if (result.state().method_26204() != group.getBlock()) continue;
            group.add(result.pos());
            break;
        }
    }
}
