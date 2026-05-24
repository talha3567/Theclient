package net.wurstclient.options;

import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_3675;
import net.minecraft.class_4068;
import net.minecraft.class_437;
import net.wurstclient.options.PressAKeyCallback;

public class PressAKeyScreen
extends class_437 {
    private PressAKeyCallback prevScreen;

    public PressAKeyScreen(PressAKeyCallback prevScreen) {
        super((class_2561)class_2561.method_43470((String)""));
        if (!(prevScreen instanceof class_437)) {
            throw new IllegalArgumentException("prevScreen is not a screen");
        }
        this.prevScreen = prevScreen;
    }

    public boolean method_25404(class_11908 event) {
        if (event.comp_4795() != 256) {
            this.prevScreen.setKey(class_3675.method_15985((class_11908)event).method_1441());
        }
        this.field_22787.method_1507((class_437)this.prevScreen);
        return super.method_25404(event);
    }

    public boolean method_25402(class_11909 event, boolean doubleClick) {
        this.prevScreen.setKey(class_3675.class_307.field_1672.method_1447(event.method_74245()).method_1441());
        this.field_22787.method_1507((class_437)this.prevScreen);
        return true;
    }

    public boolean method_25422() {
        return false;
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float partialTicks) {
        context.method_25300(this.field_22793, "Press a key or mouse button", this.field_22789 / 2, this.field_22790 / 4 + 48, -1);
        for (class_4068 drawable : this.field_33816) {
            drawable.method_25394(context, mouseX, mouseY, partialTicks);
        }
    }
}
