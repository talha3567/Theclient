package net.wurstclient.hacks;

import net.minecraft.class_1657;
import net.minecraft.class_2960;
import net.wurstclient.Category;
import net.wurstclient.hack.DontSaveState;
import net.wurstclient.hack.Hack;

@DontSaveState
public final class LsdHack
extends Hack {
    public LsdHack() {
        super("LSD");
        this.setCategory(Category.FUN);
    }

    @Override
    protected void onEnable() {
        if (!(MC.method_1560() instanceof class_1657)) {
            this.setEnabled(false);
            return;
        }
        if (LsdHack.MC.field_1773.method_62906() != null) {
            LsdHack.MC.field_1773.method_62905();
        }
        LsdHack.MC.field_1773.method_62904(class_2960.method_60655((String)"wurst", (String)"lsd"));
    }

    @Override
    protected void onDisable() {
        if (LsdHack.MC.field_1773.method_62906() != null) {
            LsdHack.MC.field_1773.method_62905();
        }
    }
}
