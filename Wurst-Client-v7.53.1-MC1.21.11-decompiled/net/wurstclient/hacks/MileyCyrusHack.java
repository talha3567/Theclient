package net.wurstclient.hacks;

import net.minecraft.class_304;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.mixinterface.IKeyMapping;
import net.wurstclient.settings.SliderSetting;

@SearchTags(value={"miley cyrus", "twerk", "wrecking ball"})
public final class MileyCyrusHack
extends Hack
implements UpdateListener {
    private final SliderSetting twerkSpeed = new SliderSetting("Twerk speed", "I came in like a wreeecking baaall...", 5.0, 1.0, 10.0, 1.0, SliderSetting.ValueDisplay.INTEGER);
    private int timer;

    public MileyCyrusHack() {
        super("MileyCyrus");
        this.setCategory(Category.FUN);
        this.addSetting(this.twerkSpeed);
    }

    @Override
    protected void onEnable() {
        this.timer = 0;
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        IKeyMapping.get(MileyCyrusHack.MC.field_1690.field_1832).resetPressedState();
    }

    @Override
    public void onUpdate() {
        class_304 sneakKey;
        ++this.timer;
        if (this.timer < 10 - this.twerkSpeed.getValueI()) {
            return;
        }
        sneakKey.method_23481(!(sneakKey = MileyCyrusHack.MC.field_1690.field_1832).method_1434());
        this.timer = -1;
    }
}
