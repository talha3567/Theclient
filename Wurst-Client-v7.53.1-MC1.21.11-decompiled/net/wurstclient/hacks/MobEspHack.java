package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.CameraTransformViewBobbingListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.EspBoxSizeSetting;
import net.wurstclient.settings.EspStyleSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.filterlists.EntityFilterList;
import net.wurstclient.settings.filters.AttackDetectingEntityFilter;
import net.wurstclient.settings.filters.FilterAllaysSetting;
import net.wurstclient.settings.filters.FilterArmorStandsSetting;
import net.wurstclient.settings.filters.FilterBatsSetting;
import net.wurstclient.settings.filters.FilterEndermenSetting;
import net.wurstclient.settings.filters.FilterGolemsSetting;
import net.wurstclient.settings.filters.FilterHostileSetting;
import net.wurstclient.settings.filters.FilterInvisibleSetting;
import net.wurstclient.settings.filters.FilterNamedSetting;
import net.wurstclient.settings.filters.FilterNeutralSetting;
import net.wurstclient.settings.filters.FilterPassiveSetting;
import net.wurstclient.settings.filters.FilterPassiveWaterSetting;
import net.wurstclient.settings.filters.FilterPetsSetting;
import net.wurstclient.settings.filters.FilterPiglinsSetting;
import net.wurstclient.settings.filters.FilterShulkersSetting;
import net.wurstclient.settings.filters.FilterSlimesSetting;
import net.wurstclient.settings.filters.FilterVillagersSetting;
import net.wurstclient.settings.filters.FilterZombiePiglinsSetting;
import net.wurstclient.settings.filters.FilterZombieVillagersSetting;
import net.wurstclient.util.EntityUtils;
import net.wurstclient.util.RenderUtils;

@SearchTags(value={"mob esp", "MobTracers", "mob tracers"})
public final class MobEspHack
extends Hack
implements UpdateListener,
CameraTransformViewBobbingListener,
RenderListener {
    private final EspStyleSetting style = new EspStyleSetting();
    private final EspBoxSizeSetting boxSize = new EspBoxSizeSetting("\u00a7lAccurate\u00a7r mode shows the exact hitbox of each mob.\n\u00a7lFancy\u00a7r mode shows slightly larger boxes that look better.");
    private final EntityFilterList entityFilters = new EntityFilterList(FilterHostileSetting.genericVision(false), FilterNeutralSetting.genericVision(AttackDetectingEntityFilter.Mode.OFF), FilterPassiveSetting.genericVision(false), FilterPassiveWaterSetting.genericVision(false), FilterBatsSetting.genericVision(false), FilterSlimesSetting.genericVision(false), FilterPetsSetting.genericVision(false), FilterVillagersSetting.genericVision(false), FilterZombieVillagersSetting.genericVision(false), FilterGolemsSetting.genericVision(false), FilterPiglinsSetting.genericVision(AttackDetectingEntityFilter.Mode.OFF), FilterZombiePiglinsSetting.genericVision(AttackDetectingEntityFilter.Mode.OFF), FilterEndermenSetting.genericVision(AttackDetectingEntityFilter.Mode.OFF), FilterShulkersSetting.genericVision(false), FilterAllaysSetting.genericVision(false), FilterInvisibleSetting.genericVision(false), FilterNamedSetting.genericVision(false), FilterArmorStandsSetting.genericVision(true));
    private final ArrayList<class_1309> mobs = new ArrayList();

    public MobEspHack() {
        super("MobESP");
        this.setCategory(Category.RENDER);
        this.addSetting(this.style);
        this.addSetting(this.boxSize);
        this.entityFilters.forEach(x$0 -> this.addSetting((Setting)x$0));
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
        this.mobs.clear();
        Stream<class_1309> stream = StreamSupport.stream(MobEspHack.MC.field_1687.method_18112().spliterator(), false).filter(class_1309.class::isInstance).map(e -> (class_1309)e).filter(e -> !(e instanceof class_1657)).filter(e -> !e.method_31481() && e.method_6032() > 0.0f);
        stream = this.entityFilters.applyTo(stream);
        this.mobs.addAll(stream.collect(Collectors.toList()));
    }

    @Override
    public void onCameraTransformViewBobbing(CameraTransformViewBobbingListener.CameraTransformViewBobbingEvent event) {
        if (this.style.hasLines()) {
            event.cancel();
        }
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        if (this.style.hasBoxes()) {
            double extraSize = this.boxSize.getExtraSize() / 2.0f;
            ArrayList<RenderUtils.ColoredBox> boxes = new ArrayList<RenderUtils.ColoredBox>(this.mobs.size());
            for (class_1309 e : this.mobs) {
                class_238 box = EntityUtils.getLerpedBox((class_1297)e, partialTicks).method_989(0.0, extraSize, 0.0).method_1014(extraSize);
                boxes.add(new RenderUtils.ColoredBox(box, this.getColor(e)));
            }
            RenderUtils.drawOutlinedBoxes(matrixStack, boxes, false);
        }
        if (this.style.hasLines()) {
            ArrayList<RenderUtils.ColoredPoint> ends = new ArrayList<RenderUtils.ColoredPoint>(this.mobs.size());
            for (class_1309 e : this.mobs) {
                class_243 point = EntityUtils.getLerpedBox((class_1297)e, partialTicks).method_1005();
                ends.add(new RenderUtils.ColoredPoint(point, this.getColor(e)));
            }
            RenderUtils.drawTracers(matrixStack, partialTicks, ends, false);
        }
    }

    private int getColor(class_1309 e) {
        float f = MobEspHack.MC.field_1724.method_5739((class_1297)e) / 20.0f;
        float r = class_3532.method_15363((float)(2.0f - f), (float)0.0f, (float)1.0f);
        float g = class_3532.method_15363((float)f, (float)0.0f, (float)1.0f);
        float[] rgb = new float[]{r, g, 0.0f};
        return RenderUtils.toIntColor(rgb, 0.5f);
    }
}
