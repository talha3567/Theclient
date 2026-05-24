package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.mixinterface.IKeyMapping;

@SearchTags(value={"auto walk"})
public final class AutoWalkHack
extends Hack
implements UpdateListener {
    public AutoWalkHack() {
        super("AutoWalk");
        this.setCategory(Category.MOVEMENT);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        IKeyMapping.get(AutoWalkHack.MC.field_1690.field_1894).resetPressedState();
    }

    @Override
    public void onUpdate() {
        AutoWalkHack.MC.field_1690.field_1894.method_23481(true);
    }
}
