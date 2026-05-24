package net.wurstclient.hacks;

import java.util.Random;
import net.minecraft.class_1747;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_238;
import net.minecraft.class_4587;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.FaceTargetSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SwingHandSetting;
import net.wurstclient.util.BlockPlacer;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.InteractionSimulator;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.RotationUtils;

@SearchTags(value={"build random", "RandomBuild", "random build", "PlaceRandom", "place random", "RandomPlace", "random place"})
public final class BuildRandomHack
extends Hack
implements UpdateListener,
RenderListener {
    private final SliderSetting range = new SliderSetting("Range", 5.0, 1.0, 6.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private SliderSetting maxAttempts = new SliderSetting("Max attempts", "Maximum number of random positions that BuildRandom will try to place a block at in one tick.\n\nHigher values speed up the building process at the cost of increased lag.", 128.0, 1.0, 1024.0, 1.0, SliderSetting.ValueDisplay.INTEGER);
    private final CheckboxSetting checkItem = new CheckboxSetting("Check held item", "Only builds when you are actually holding a block.\nTurn this off to build with fire, water, lava, spawn eggs, or if you just want to right click with an empty hand in random places.", true);
    private final CheckboxSetting checkLOS = new CheckboxSetting("Check line of sight", "Ensure that BuildRandom won't try to place blocks behind walls.", false);
    private final FaceTargetSetting faceTarget = FaceTargetSetting.withoutPacketSpam(this, FaceTargetSetting.FaceTarget.SERVER);
    private final SwingHandSetting swingHand = new SwingHandSetting(this, SwingHandSetting.SwingHand.SERVER);
    private final CheckboxSetting fastPlace = new CheckboxSetting("Always FastPlace", "Builds as if FastPlace was enabled, even if it's not.", false);
    private final CheckboxSetting placeWhileBreaking = new CheckboxSetting("Place while breaking", "Builds even while you are breaking a block.\nPossible with hacks, but wouldn't work in vanilla. May look suspicious.", false);
    private final CheckboxSetting placeWhileRiding = new CheckboxSetting("Place while riding", "Builds even while you are riding a vehicle.\nPossible with hacks, but wouldn't work in vanilla. May look suspicious.", false);
    private final CheckboxSetting indicator = new CheckboxSetting("Indicator", "Shows where BuildRandom is placing blocks.", true);
    private final Random random = new Random();
    private class_2338 lastPos;

    public BuildRandomHack() {
        super("BuildRandom");
        this.setCategory(Category.BLOCKS);
        this.addSetting(this.range);
        this.addSetting(this.maxAttempts);
        this.addSetting(this.checkItem);
        this.addSetting(this.checkLOS);
        this.addSetting(this.faceTarget);
        this.addSetting(this.swingHand);
        this.addSetting(this.fastPlace);
        this.addSetting(this.placeWhileBreaking);
        this.addSetting(this.placeWhileRiding);
        this.addSetting(this.indicator);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(RenderListener.class, this);
    }

    @Override
    protected void onDisable() {
        this.lastPos = null;
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(RenderListener.class, this);
    }

    @Override
    public void onUpdate() {
        class_2338 pos;
        this.lastPos = null;
        if (!this.fastPlace.isChecked() && BuildRandomHack.MC.field_1752 > 0) {
            return;
        }
        if (this.checkItem.isChecked() && !BuildRandomHack.MC.field_1724.method_24520(stack -> !stack.method_7960() && stack.method_7909() instanceof class_1747)) {
            return;
        }
        if (!this.placeWhileBreaking.isChecked() && BuildRandomHack.MC.field_1761.method_2923()) {
            return;
        }
        if (!this.placeWhileRiding.isChecked() && BuildRandomHack.MC.field_1724.method_3144()) {
            return;
        }
        int maxAttempts = this.maxAttempts.getValueI();
        int blockRange = this.range.getValueCeil();
        int bound = blockRange * 2 + 1;
        int attempts = 0;
        do {
            pos = class_2338.method_49638((class_2374)RotationUtils.getEyesPos()).method_10069(this.random.nextInt(bound) - blockRange, this.random.nextInt(bound) - blockRange, this.random.nextInt(bound) - blockRange);
        } while (++attempts < maxAttempts && !this.tryToPlaceBlock(pos));
    }

    private boolean tryToPlaceBlock(class_2338 pos) {
        if (!BlockUtils.getState(pos).method_45474()) {
            return false;
        }
        BlockPlacer.BlockPlacingParams params = BlockPlacer.getBlockPlacingParams(pos);
        if (params == null || params.distanceSq() > this.range.getValueSq() || params.requiresSneaking()) {
            return false;
        }
        if (this.checkLOS.isChecked() && !params.lineOfSight()) {
            return false;
        }
        BuildRandomHack.MC.field_1752 = 4;
        this.faceTarget.face(params.hitVec());
        this.lastPos = pos;
        InteractionSimulator.rightClickBlock(params.toHitResult(), (SwingHandSetting.SwingHand)((Object)this.swingHand.getSelected()));
        return true;
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        if (this.lastPos == null || !this.indicator.isChecked()) {
            return;
        }
        float red = partialTicks * 2.0f;
        float green = 2.0f - red;
        float[] rgb = new float[]{red, green, 0.0f};
        int quadColor = RenderUtils.toIntColor(rgb, 0.25f);
        int lineColor = RenderUtils.toIntColor(rgb, 0.5f);
        class_238 box = new class_238(this.lastPos);
        RenderUtils.drawSolidBox(matrixStack, box, quadColor, false);
        RenderUtils.drawOutlinedBox(matrixStack, box, lineColor, false);
    }
}
