package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.clickgui.Window;
import net.wurstclient.clickgui.components.RadarComponent;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.filterlists.EntityFilterList;
import net.wurstclient.settings.filters.FilterBatsSetting;
import net.wurstclient.settings.filters.FilterHostileSetting;
import net.wurstclient.settings.filters.FilterInvisibleSetting;
import net.wurstclient.settings.filters.FilterPassiveSetting;
import net.wurstclient.settings.filters.FilterPassiveWaterSetting;
import net.wurstclient.settings.filters.FilterPlayersSetting;
import net.wurstclient.settings.filters.FilterSleepingSetting;
import net.wurstclient.settings.filters.FilterSlimesSetting;
import net.wurstclient.util.FakePlayerEntity;

@SearchTags(value={"MiniMap", "mini map"})
public final class RadarHack
extends Hack
implements UpdateListener {
    private final Window window;
    private final ArrayList<class_1297> entities = new ArrayList();
    private final SliderSetting radius = new SliderSetting("Radius", "Radius in blocks.", 100.0, 1.0, 100.0, 1.0, SliderSetting.ValueDisplay.INTEGER);
    private final CheckboxSetting rotate = new CheckboxSetting("Rotate with player", true);
    private final EntityFilterList entityFilters = new EntityFilterList(FilterPlayersSetting.genericVision(false), FilterSleepingSetting.genericVision(false), FilterHostileSetting.genericVision(false), FilterPassiveSetting.genericVision(false), FilterPassiveWaterSetting.genericVision(false), FilterBatsSetting.genericVision(true), FilterSlimesSetting.genericVision(false), FilterInvisibleSetting.genericVision(false));

    public RadarHack() {
        super("Radar");
        this.setCategory(Category.RENDER);
        this.addSetting(this.radius);
        this.addSetting(this.rotate);
        this.entityFilters.forEach(x$0 -> this.addSetting((Setting)x$0));
        this.window = new Window("Radar");
        this.window.setPinned(true);
        this.window.setInvisible(true);
        this.window.add(new RadarComponent(this));
    }

    @Override
    protected void onEnable() {
        EVENTS.add(UpdateListener.class, this);
        this.window.setInvisible(false);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        this.window.setInvisible(true);
    }

    @Override
    public void onUpdate() {
        class_746 player = RadarHack.MC.field_1724;
        class_638 world = RadarHack.MC.field_1687;
        this.entities.clear();
        Stream<class_1297> stream = StreamSupport.stream(world.method_18112().spliterator(), true).filter(e -> !e.method_31481() && e != player).filter(e -> !(e instanceof FakePlayerEntity)).filter(class_1309.class::isInstance).filter(e -> ((class_1309)e).method_6032() > 0.0f);
        stream = this.entityFilters.applyTo(stream);
        this.entities.addAll(stream.collect(Collectors.toList()));
    }

    public Window getWindow() {
        return this.window;
    }

    public Iterable<class_1297> getEntities() {
        return Collections.unmodifiableList(this.entities);
    }

    public double getRadius() {
        return this.radius.getValue();
    }

    public boolean isRotateEnabled() {
        return this.rotate.isChecked();
    }
}
