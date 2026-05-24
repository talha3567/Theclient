package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;

@SearchTags(value={"no hurtcam", "no hurt cam"})
public final class NoHurtcamHack
extends Hack {
    public NoHurtcamHack() {
        super("NoHurtcam");
        this.setCategory(Category.RENDER);
    }
}
