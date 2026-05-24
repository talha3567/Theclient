package net.wurstclient.hacks.autofish;

import java.awt.Color;
import java.util.stream.Stream;
import net.minecraft.class_1297;
import net.minecraft.class_1536;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2767;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.wurstclient.WurstClient;
import net.wurstclient.WurstRenderLayers;
import net.wurstclient.hacks.autofish.FishingSpot;
import net.wurstclient.hacks.autofish.FishingSpotManager;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.EntityUtils;
import net.wurstclient.util.RenderUtils;
import org.joml.Quaternionfc;

public final class AutoFishDebugDraw {
    private static final class_310 MC = WurstClient.MC;
    private final CheckboxSetting debugDraw = new CheckboxSetting("Debug draw", "Shows where bites are occurring and where they will be detected. Useful for optimizing your 'Valid range' setting.", false);
    private final ColorSetting ddColor = new ColorSetting("DD color", "Color of the debug draw, if enabled.", Color.RED);
    private final SliderSetting validRange;
    private final FishingSpotManager fishingSpots;
    private class_243 lastSoundPos;

    public AutoFishDebugDraw(SliderSetting validRange, FishingSpotManager fishingSpots) {
        this.validRange = validRange;
        this.fishingSpots = fishingSpots;
    }

    public Stream<Setting> getSettings() {
        return Stream.of(this.debugDraw, this.ddColor);
    }

    public void reset() {
        this.lastSoundPos = null;
    }

    public void updateSoundPos(class_2767 sound) {
        this.lastSoundPos = new class_243(sound.method_11890(), sound.method_11889(), sound.method_11893());
    }

    public void render(class_4587 matrices, float partialTicks) {
        if (!this.debugDraw.isChecked() && !this.fishingSpots.isMcmmoMode()) {
            return;
        }
        if (this.debugDraw.isChecked()) {
            class_1536 bobber = AutoFishDebugDraw.MC.field_1724.field_7513;
            if (bobber != null) {
                this.drawValidRange(matrices, partialTicks, bobber);
            }
            if (this.lastSoundPos != null) {
                this.drawLastBite(matrices);
            }
            this.drawFishingSpots(matrices);
        }
        if (this.fishingSpots.isMcmmoMode()) {
            this.drawMcmmoRange(matrices);
        }
    }

    private void drawValidRange(class_4587 matrices, float partialTicks, class_1536 bobber) {
        double vr = this.validRange.getValue();
        class_243 pos = EntityUtils.getLerpedPos((class_1297)bobber, partialTicks);
        class_238 vrBox = new class_238(-vr, -0.0625, -vr, vr, 0.0625, vr).method_997(pos);
        RenderUtils.drawOutlinedBox(matrices, vrBox, this.ddColor.getColorI(128), false);
    }

    private void drawLastBite(class_4587 matrixStack) {
        class_243 pos = this.lastSoundPos;
        int color = this.ddColor.getColorI(128);
        RenderUtils.drawLine(matrixStack, pos.method_1031(-0.125, 0.0, -0.125), pos.method_1031(0.125, 0.0, 0.125), color, false);
        RenderUtils.drawLine(matrixStack, pos.method_1031(0.125, 0.0, -0.125), pos.method_1031(-0.125, 0.0, 0.125), color, false);
    }

    private void drawFishingSpots(class_4587 matrices) {
        class_238 headBox = new class_238(-0.25, 0.0, -0.25, 0.25, 0.5, 0.25);
        class_238 noseBox = headBox.method_989(0.125, 0.125, 0.5).method_1002(0.25, 0.35, 0.45);
        int color = this.ddColor.getColorI(192);
        class_4597.class_4598 vcp = RenderUtils.getVCP();
        class_243 camPos = RenderUtils.getCameraPos();
        for (FishingSpot spot : this.fishingSpots.getFishingSpots()) {
            class_243 playerPos = spot.input().pos();
            class_243 bobberPos = spot.bobberPos();
            matrices.method_22903();
            matrices.method_22904(playerPos.field_1352 - camPos.field_1352, playerPos.field_1351 - camPos.field_1351, playerPos.field_1350 - camPos.field_1350);
            matrices.method_22907((Quaternionfc)spot.input().rotation().toQuaternion());
            class_4588 lineBuffer = vcp.method_73477(WurstRenderLayers.ESP_LINES);
            RenderUtils.drawOutlinedBox(matrices, lineBuffer, headBox, color);
            RenderUtils.drawOutlinedBox(matrices, lineBuffer, noseBox, color);
            if (!spot.openWater()) {
                RenderUtils.drawCrossBox(matrices, lineBuffer, headBox, color);
            }
            matrices.method_22909();
            RenderUtils.drawArrow(matrices, lineBuffer, playerPos.method_1020(camPos), bobberPos.method_1020(camPos), color, 0.1f);
            vcp.method_22994(WurstRenderLayers.ESP_LINES);
        }
    }

    private void drawMcmmoRange(class_4587 matrices) {
        FishingSpot lastSpot = this.fishingSpots.getLastSpot();
        if (lastSpot == null) {
            return;
        }
        if (this.fishingSpots.isSetupDone() && !this.debugDraw.isChecked()) {
            return;
        }
        int mcmmoRange = this.fishingSpots.getRange();
        class_243 bobberPos = lastSpot.bobberPos();
        class_238 rangeBox = new class_238(0.0, 0.0, 0.0, 0.0, 0.0, 0.0).method_1009((double)mcmmoRange, 1.0, (double)mcmmoRange).method_997(bobberPos);
        int quadsColor = 0x40FF0000;
        RenderUtils.drawSolidBox(matrices, rangeBox, quadsColor, false);
        int linesColor = -2130771968;
        RenderUtils.drawOutlinedBox(matrices, rangeBox, linesColor, false);
        RenderUtils.drawOutlinedBox(matrices, rangeBox.method_35580(0.0, 1.0, 0.0), linesColor, false);
    }
}
