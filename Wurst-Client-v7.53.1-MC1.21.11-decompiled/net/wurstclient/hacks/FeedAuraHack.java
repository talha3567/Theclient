package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1297;
import net.minecraft.class_1321;
import net.minecraft.class_1429;
import net.minecraft.class_1496;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3966;
import net.minecraft.class_4587;
import net.minecraft.class_636;
import net.minecraft.class_746;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.HandleInputListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.filters.FilterBabiesSetting;
import net.wurstclient.util.EntityUtils;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.RotationUtils;

@SearchTags(value={"feed aura", "BreedAura", "breed aura", "AutoBreeder", "auto breeder"})
public final class FeedAuraHack
extends Hack
implements UpdateListener,
HandleInputListener,
RenderListener {
    private final SliderSetting range = new SliderSetting("Range", "Determines how far FeedAura will reach to feed animals.\nAnything that is further away than the specified value will not be fed.", 5.0, 1.0, 10.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final FilterBabiesSetting filterBabies = new FilterBabiesSetting("Won't feed baby animals.\nSaves food, but doesn't speed up baby growth.", true);
    private final CheckboxSetting filterUntamed = new CheckboxSetting("Filter untamed", "Won't feed tameable animals that haven't been tamed yet.", false);
    private final CheckboxSetting filterHorses = new CheckboxSetting("Filter horse-like animals", "Won't feed horses, llamas, donkeys, etc.\nRecommended in Minecraft versions before 1.20.3 due to MC-233276,which causes these animals to consume items indefinitely.", false);
    private final Random random = new Random();
    private class_1429 target;
    private class_1429 renderTarget;

    public FeedAuraHack() {
        super("FeedAura");
        this.setCategory(Category.OTHER);
        this.addSetting(this.range);
        this.addSetting(this.filterBabies);
        this.addSetting(this.filterUntamed);
        this.addSetting(this.filterHorses);
    }

    @Override
    protected void onEnable() {
        FeedAuraHack.WURST.getHax().clickAuraHack.setEnabled(false);
        FeedAuraHack.WURST.getHax().fightBotHack.setEnabled(false);
        FeedAuraHack.WURST.getHax().killauraLegitHack.setEnabled(false);
        FeedAuraHack.WURST.getHax().multiAuraHack.setEnabled(false);
        FeedAuraHack.WURST.getHax().protectHack.setEnabled(false);
        FeedAuraHack.WURST.getHax().triggerBotHack.setEnabled(false);
        FeedAuraHack.WURST.getHax().tpAuraHack.setEnabled(false);
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(HandleInputListener.class, this);
        EVENTS.add(RenderListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(HandleInputListener.class, this);
        EVENTS.remove(RenderListener.class, this);
        this.target = null;
        this.renderTarget = null;
    }

    @Override
    public void onUpdate() {
        ArrayList targets;
        class_1799 heldStack = FeedAuraHack.MC.field_1724.method_31548().method_7391();
        double rangeSq = this.range.getValueSq();
        Stream<class_1429> stream = EntityUtils.getValidAnimals().filter(e -> EntityUtils.distanceToHitboxSq((class_1297)e) <= rangeSq).filter(e -> e.method_6481(heldStack)).filter(class_1429::method_6482);
        if (this.filterBabies.isChecked()) {
            stream = stream.filter(this.filterBabies);
        }
        if (this.filterUntamed.isChecked()) {
            stream = stream.filter(e -> !this.isUntamed((class_1429)e));
        }
        if (this.filterHorses.isChecked()) {
            stream = stream.filter(e -> !(e instanceof class_1496));
        }
        this.renderTarget = this.target = (targets = stream.collect(Collectors.toCollection(ArrayList::new))).isEmpty() ? null : (class_1429)targets.get(this.random.nextInt(targets.size()));
        if (this.target == null) {
            return;
        }
        WURST.getRotationFaker().faceVectorPacket(this.target.method_5829().method_1005());
    }

    @Override
    public void onHandleInput() {
        class_1269.class_9860 success;
        class_243 end;
        class_243 start;
        if (this.target == null) {
            return;
        }
        class_636 im = FeedAuraHack.MC.field_1761;
        class_746 player = FeedAuraHack.MC.field_1724;
        class_1268 hand = class_1268.field_5808;
        if (im.method_2923() || player.method_3144()) {
            return;
        }
        class_238 box = this.target.method_5829();
        class_243 hitVec = box.method_992(start = RotationUtils.getEyesPos(), end = box.method_1005()).orElse(start);
        class_3966 hitResult = new class_3966((class_1297)this.target, hitVec);
        class_1269 actionResult = im.method_2917((class_1657)player, (class_1297)this.target, hitResult, hand);
        if (!actionResult.method_23665()) {
            actionResult = im.method_2905((class_1657)player, (class_1297)this.target, hand);
        }
        if (actionResult instanceof class_1269.class_9860 && (success = (class_1269.class_9860)actionResult).comp_2909() == class_1269.class_9861.field_52427) {
            player.method_6104(hand);
        }
        this.target = null;
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        if (this.renderTarget == null) {
            return;
        }
        float p = 1.0f;
        if ((double)this.renderTarget.method_6063() > 1.0E-5) {
            p = this.renderTarget.method_6032() / this.renderTarget.method_6063();
        }
        float green = p * 2.0f;
        float red = 2.0f - green;
        float[] rgb = new float[]{red, green, 0.0f};
        int quadColor = RenderUtils.toIntColor(rgb, 0.25f);
        int lineColor = RenderUtils.toIntColor(rgb, 0.5f);
        class_238 box = EntityUtils.getLerpedBox((class_1297)this.renderTarget, partialTicks);
        if (p < 1.0f) {
            box = box.method_35580((double)(1.0f - p) * 0.5 * box.method_17939(), (double)(1.0f - p) * 0.5 * box.method_17940(), (double)(1.0f - p) * 0.5 * box.method_17941());
        }
        RenderUtils.drawSolidBox(matrixStack, box, quadColor, false);
        RenderUtils.drawOutlinedBox(matrixStack, box, lineColor, false);
    }

    private boolean isUntamed(class_1429 e) {
        class_1321 tame;
        class_1496 horse;
        if (e instanceof class_1496 && !(horse = (class_1496)e).method_6727()) {
            return true;
        }
        return e instanceof class_1321 && !(tame = (class_1321)e).method_6181();
    }
}
