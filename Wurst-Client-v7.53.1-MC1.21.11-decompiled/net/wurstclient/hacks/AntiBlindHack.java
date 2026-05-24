package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;

@SearchTags(value={"AntiBlindness", "NoBlindness", "anti blindness", "no blindness", "AntiDarkness", "NoDarkness", "anti darkness", "no darkness", "AntiWardenEffect", "anti warden effect", "NoWardenEffect", "no warden effect"})
public final class AntiBlindHack
extends Hack {
    public AntiBlindHack() {
        super("AntiBlind");
        this.setCategory(Category.RENDER);
    }
}
