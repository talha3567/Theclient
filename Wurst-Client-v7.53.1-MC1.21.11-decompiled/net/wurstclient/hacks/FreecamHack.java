package net.wurstclient.hacks;

import java.awt.Color;
import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_746;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.CameraTransformViewBobbingListener;
import net.wurstclient.events.MouseScrollListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.events.VisGraphListener;
import net.wurstclient.hack.DontSaveState;
import net.wurstclient.hack.Hack;
import net.wurstclient.hacks.freecam.FreecamInitialPosSetting;
import net.wurstclient.hacks.freecam.FreecamInputSetting;
import net.wurstclient.hacks.freecam.FreecamInteractionSetting;
import net.wurstclient.mixinterface.IKeyMapping;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.EntityUtils;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.RotationUtils;

@DontSaveState
@SearchTags(value={"free camera", "spectator"})
public final class FreecamHack
extends Hack
implements UpdateListener,
VisGraphListener,
CameraTransformViewBobbingListener,
RenderListener,
MouseScrollListener {
    private final FreecamInputSetting applyInputTo = new FreecamInputSetting();
    private final FreecamInteractionSetting interactFrom = new FreecamInteractionSetting();
    private final SliderSetting horizontalSpeed = new SliderSetting("Horizontal speed", "description.wurst.setting.freecam.horizontal_speed", 1.0, 0.05, 10.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final SliderSetting verticalSpeed = new SliderSetting("Vertical speed", "description.wurst.setting.freecam.vertical_speed", 1.0, 0.05, 5.0, 0.05, v -> SliderSetting.ValueDisplay.DECIMAL.getValueString(this.getActualVerticalSpeed()));
    private final CheckboxSetting scrollToChangeSpeed = new CheckboxSetting("Scroll to change speed", "description.wurst.setting.freecam.scroll_to_change_speed", true);
    private final CheckboxSetting renderSpeed = new CheckboxSetting("Show speed in HackList", "description.wurst.setting.freecam.show_speed_in_hacklist", true);
    private final FreecamInitialPosSetting initialPos = new FreecamInitialPosSetting();
    private final CheckboxSetting tracer = new CheckboxSetting("Tracer", "description.wurst.setting.freecam.tracer", false);
    private final ColorSetting color = new ColorSetting("Tracer color", Color.WHITE);
    private final CheckboxSetting hideHand = new CheckboxSetting("Hide hand", "description.wurst.setting.freecam.hide_hand", true);
    private final CheckboxSetting disableOnDamage = new CheckboxSetting("Disable on damage", "description.wurst.setting.freecam.disable_on_damage", true);
    private final CheckboxSetting reloadChunks = new CheckboxSetting("Reload chunks", "description.wurst.setting.freecam.reload_chunks", true);
    private class_243 camPos;
    private class_243 prevCamPos;
    private float camYaw;
    private float camPitch;
    private float lastHealth;

    public FreecamHack() {
        super("Freecam");
        this.setCategory(Category.RENDER);
        this.addSetting(this.applyInputTo);
        this.addSetting(this.interactFrom);
        this.addSetting(this.horizontalSpeed);
        this.addSetting(this.verticalSpeed);
        this.addSetting(this.scrollToChangeSpeed);
        this.addSetting(this.renderSpeed);
        this.addSetting(this.initialPos);
        this.addSetting(this.tracer);
        this.addSetting(this.color);
        this.addSetting(this.hideHand);
        this.addSetting(this.disableOnDamage);
        this.addSetting(this.reloadChunks);
    }

    @Override
    public String getRenderName() {
        if (!this.renderSpeed.isChecked()) {
            return this.getName();
        }
        return this.getName() + " [" + this.horizontalSpeed.getValueString() + ", " + this.verticalSpeed.getValueString() + "]";
    }

    @Override
    protected void onEnable() {
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(VisGraphListener.class, this);
        EVENTS.add(CameraTransformViewBobbingListener.class, this);
        EVENTS.add(RenderListener.class, this);
        EVENTS.add(MouseScrollListener.class, this);
        this.lastHealth = Float.MIN_VALUE;
        this.prevCamPos = this.camPos = RotationUtils.getEyesPos().method_1019(((FreecamInitialPosSetting.InitialPosition)((Object)this.initialPos.getSelected())).getOffset());
        this.camYaw = FreecamHack.MC.field_1724.method_36454();
        this.camPitch = FreecamHack.MC.field_1724.method_36455();
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(VisGraphListener.class, this);
        EVENTS.remove(CameraTransformViewBobbingListener.class, this);
        EVENTS.remove(RenderListener.class, this);
        EVENTS.remove(MouseScrollListener.class, this);
        if (this.reloadChunks.isChecked()) {
            FreecamHack.MC.field_1769.method_3279();
        }
    }

    @Override
    public void onUpdate() {
        class_746 player = FreecamHack.MC.field_1724;
        float currentHealth = player.method_6032();
        if (this.disableOnDamage.isChecked() && currentHealth < this.lastHealth) {
            this.setEnabled(false);
            return;
        }
        this.lastHealth = currentHealth;
        if (!this.isMovingCamera() || FreecamHack.MC.field_1755 != null) {
            this.prevCamPos = this.camPos;
            return;
        }
        class_241 moveVector = player.field_3913.method_3128();
        double yawRad = FreecamHack.MC.field_1773.method_19418().method_19330() * ((float)Math.PI / 180);
        double sinYaw = class_3532.method_15374((double)yawRad);
        double cosYaw = class_3532.method_15362((double)yawRad);
        double offsetX = (double)moveVector.field_1343 * cosYaw - (double)moveVector.field_1342 * sinYaw;
        double offsetZ = (double)moveVector.field_1343 * sinYaw + (double)moveVector.field_1342 * cosYaw;
        double offsetY = 0.0;
        double vSpeed = this.getActualVerticalSpeed();
        if (IKeyMapping.get(FreecamHack.MC.field_1690.field_1903).isActuallyDown()) {
            offsetY += vSpeed;
        }
        if (IKeyMapping.get(FreecamHack.MC.field_1690.field_1832).isActuallyDown()) {
            offsetY -= vSpeed;
        }
        class_243 offsetVec = new class_243(offsetX, 0.0, offsetZ).method_1021((double)this.horizontalSpeed.getValueF()).method_1031(0.0, offsetY, 0.0);
        this.prevCamPos = this.camPos;
        this.camPos = this.camPos.method_1019(offsetVec);
    }

    private double getActualVerticalSpeed() {
        return class_3532.method_15350((double)(this.horizontalSpeed.getValue() * this.verticalSpeed.getValue()), (double)0.05, (double)10.0);
    }

    @Override
    public void onMouseScroll(double amount) {
        if (!this.isControllingScrollEvents()) {
            return;
        }
        if (amount > 0.0) {
            this.horizontalSpeed.increaseValue();
        } else if (amount < 0.0) {
            this.horizontalSpeed.decreaseValue();
        }
    }

    public boolean isControllingScrollEvents() {
        return this.isMovingCamera() && this.scrollToChangeSpeed.isChecked() && FreecamHack.MC.field_1755 == null && !FreecamHack.WURST.getOtfs().zoomOtf.isControllingScrollEvents();
    }

    public boolean isMovingCamera() {
        return this.isEnabled() && this.applyInputTo.getSelected() == FreecamInputSetting.ApplyInputTo.CAMERA;
    }

    public boolean isClickingFromCamera() {
        return this.isEnabled() && this.interactFrom.getSelected() == FreecamInteractionSetting.InteractFrom.CAMERA;
    }

    @Override
    public void onVisGraph(VisGraphListener.VisGraphEvent event) {
        event.cancel();
    }

    @Override
    public void onCameraTransformViewBobbing(CameraTransformViewBobbingListener.CameraTransformViewBobbingEvent event) {
        event.cancel();
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        if (!this.tracer.isChecked()) {
            return;
        }
        int colorI = this.color.getColorI(128);
        double extraSize = 0.05;
        class_238 rawBox = EntityUtils.getLerpedBox((class_1297)FreecamHack.MC.field_1724, partialTicks);
        class_238 box = rawBox.method_989(0.0, extraSize, 0.0).method_1014(extraSize);
        RenderUtils.drawOutlinedBox(matrixStack, box, colorI, false);
        RenderUtils.drawTracer(matrixStack, partialTicks, rawBox.method_1005(), colorI, false);
    }

    public boolean shouldHideHand() {
        return this.isEnabled() && this.hideHand.isChecked();
    }

    public class_243 getCamPos(float partialTicks) {
        return class_3532.method_61342((double)partialTicks, (class_243)this.prevCamPos, (class_243)this.camPos);
    }

    public void turn(double deltaYaw, double deltaPitch) {
        this.camYaw += (float)(deltaYaw * 0.15);
        this.camPitch += (float)(deltaPitch * 0.15);
        this.camPitch = class_3532.method_15363((float)this.camPitch, (float)-90.0f, (float)90.0f);
    }

    public float getCamYaw() {
        return this.camYaw;
    }

    public float getCamPitch() {
        return this.camPitch;
    }

    public class_243 getScaledCamDir(double scale) {
        return class_243.method_1030((float)this.camPitch, (float)this.camYaw).method_1021(scale);
    }
}
