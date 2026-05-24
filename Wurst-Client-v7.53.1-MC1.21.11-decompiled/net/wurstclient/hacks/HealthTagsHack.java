package net.wurstclient.hacks;

import net.minecraft.class_124;
import net.minecraft.class_1309;
import net.minecraft.class_2561;
import net.minecraft.class_5250;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;

@SearchTags(value={"health tags"})
public final class HealthTagsHack
extends Hack {
    public HealthTagsHack() {
        super("HealthTags");
        this.setCategory(Category.RENDER);
    }

    public class_2561 addHealth(class_1309 entity, class_5250 nametag) {
        if (!this.isEnabled()) {
            return nametag;
        }
        int health = (int)entity.method_6032();
        class_5250 formattedHealth = class_2561.method_43470((String)" ").method_27693(Integer.toString(health)).method_27692(this.getColor(health));
        return nametag.method_10852((class_2561)formattedHealth);
    }

    private class_124 getColor(int health) {
        if (health <= 5) {
            return class_124.field_1079;
        }
        if (health <= 10) {
            return class_124.field_1065;
        }
        if (health <= 15) {
            return class_124.field_1054;
        }
        return class_124.field_1060;
    }
}
