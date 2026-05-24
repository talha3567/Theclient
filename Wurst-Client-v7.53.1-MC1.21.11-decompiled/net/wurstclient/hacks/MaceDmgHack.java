package net.wurstclient.hacks;

import net.minecraft.class_1297;
import net.minecraft.class_1802;
import net.minecraft.class_239;
import net.minecraft.class_2596;
import net.minecraft.class_2828;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.PlayerAttacksEntityListener;
import net.wurstclient.hack.Hack;

@SearchTags(value={"mace dmg", "MaceDamage", "mace damage"})
public final class MaceDmgHack
extends Hack
implements PlayerAttacksEntityListener {
    public MaceDmgHack() {
        super("MaceDMG");
        this.setCategory(Category.COMBAT);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(PlayerAttacksEntityListener.class, this);
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(PlayerAttacksEntityListener.class, this);
    }

    @Override
    public void onPlayerAttacksEntity(class_1297 target) {
        if (MaceDmgHack.MC.field_1765 == null || MaceDmgHack.MC.field_1765.method_17783() != class_239.class_240.field_1331) {
            return;
        }
        if (!MaceDmgHack.MC.field_1724.method_6047().method_31574(class_1802.field_49814)) {
            return;
        }
        for (int i = 0; i < 4; ++i) {
            this.sendFakeY(0.0);
        }
        this.sendFakeY(Math.sqrt(500.0));
        this.sendFakeY(0.0);
    }

    private void sendFakeY(double offset) {
        MaceDmgHack.MC.field_1724.field_3944.method_52787((class_2596)new class_2828.class_2829(MaceDmgHack.MC.field_1724.method_23317(), MaceDmgHack.MC.field_1724.method_23318() + offset, MaceDmgHack.MC.field_1724.method_23321(), false, MaceDmgHack.MC.field_1724.field_5976));
    }
}
