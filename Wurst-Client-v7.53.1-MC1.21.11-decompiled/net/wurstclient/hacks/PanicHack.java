package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;

@SearchTags(value={"legit", "disable"})
@DontBlock
public final class PanicHack
extends Hack
implements UpdateListener {
    public PanicHack() {
        super("Panic");
        this.setCategory(Category.OTHER);
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
        for (Hack hack : WURST.getHax().getAllHax()) {
            if (!hack.isEnabled() || hack == this) continue;
            hack.setEnabled(false);
        }
        this.setEnabled(false);
    }
}
