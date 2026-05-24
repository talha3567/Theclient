package net.wurstclient.hacks;

import java.awt.Color;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_3965;
import net.minecraft.class_4587;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.RightClickListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.InteractionSimulator;
import net.wurstclient.util.RenderUtils;

@SearchTags(value={"air place"})
public final class AirPlaceHack
extends Hack
implements RightClickListener,
UpdateListener,
RenderListener {
    private final SliderSetting range = new SliderSetting("Range", 5.0, 1.0, 6.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final CheckboxSetting guide = new CheckboxSetting("Guide", "description.wurst.setting.airplace.guide", true);
    private final ColorSetting guideColor = new ColorSetting("Guide color", "description.wurst.setting.airplace.guide_color", Color.RED);
    private class_2338 renderPos;

    public AirPlaceHack() {
        super("AirPlace");
        this.setCategory(Category.BLOCKS);
        this.addSetting(this.range);
        this.addSetting(this.guide);
        this.addSetting(this.guideColor);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(RenderListener.class, this);
        EVENTS.add(RightClickListener.class, this);
        this.renderPos = null;
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(RenderListener.class, this);
        EVENTS.remove(RightClickListener.class, this);
    }

    @Override
    public void onRightClick(RightClickListener.RightClickEvent event) {
        class_3965 hitResult = this.getHitResultIfMissed();
        if (hitResult == null) {
            return;
        }
        AirPlaceHack.MC.field_1752 = 4;
        if (AirPlaceHack.MC.field_1724.method_3144()) {
            return;
        }
        InteractionSimulator.rightClickBlock(hitResult);
        event.cancel();
    }

    @Override
    public void onUpdate() {
        this.renderPos = null;
        if (!this.guide.isChecked()) {
            return;
        }
        if (AirPlaceHack.MC.field_1724.method_6047().method_7960() && AirPlaceHack.MC.field_1724.method_6079().method_7960()) {
            return;
        }
        if (AirPlaceHack.MC.field_1724.method_3144()) {
            return;
        }
        class_3965 hitResult = this.getHitResultIfMissed();
        if (hitResult != null) {
            this.renderPos = hitResult.method_17777();
        }
    }

    private class_3965 getHitResultIfMissed() {
        class_239 hitResult = AirPlaceHack.MC.field_1724.method_5745(this.range.getValue(), 0.0f, false);
        if (hitResult.method_17783() != class_239.class_240.field_1333) {
            return null;
        }
        if (!(hitResult instanceof class_3965)) {
            return null;
        }
        class_3965 blockHitResult = (class_3965)hitResult;
        return blockHitResult;
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        if (this.renderPos == null) {
            return;
        }
        class_238 box = new class_238(this.renderPos);
        int quadColor = this.guideColor.getColorI(26);
        RenderUtils.drawSolidBox(matrixStack, box, quadColor, false);
        int lineColor = this.guideColor.getColorI(192);
        RenderUtils.drawOutlinedBox(matrixStack, box, lineColor, false);
    }
}
