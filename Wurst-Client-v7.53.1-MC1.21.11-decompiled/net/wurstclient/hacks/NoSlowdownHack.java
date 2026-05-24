package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;

@SearchTags(value={"no slowdown", "no slow down"})
public final class NoSlowdownHack
extends Hack {
    public NoSlowdownHack() {
        super("NoSlowdown");
        this.setCategory(Category.MOVEMENT);
    }
}
