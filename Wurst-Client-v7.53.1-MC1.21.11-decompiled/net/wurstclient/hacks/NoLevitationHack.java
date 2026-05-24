package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;

@SearchTags(value={"no levitation", "levitation", "levitate"})
public final class NoLevitationHack
extends Hack {
    public NoLevitationHack() {
        super("NoLevitation");
        this.setCategory(Category.MOVEMENT);
    }
}
