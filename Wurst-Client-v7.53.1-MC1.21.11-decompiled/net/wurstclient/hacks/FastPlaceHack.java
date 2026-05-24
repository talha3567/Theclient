package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;

@SearchTags(value={"fast place"})
public final class FastPlaceHack
extends Hack
implements UpdateListener {
    public FastPlaceHack() {
        super("FastPlace");
        this.setCategory(Category.BLOCKS);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(UpdateListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
    }

    @Override
    public void onUpdate() {
        FastPlaceHack.MC.field_1752 = 0;
    }
}
