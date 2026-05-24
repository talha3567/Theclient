package net.wurstclient.mixin;

import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_1733;
import net.minecraft.class_2561;
import net.minecraft.class_364;
import net.minecraft.class_4185;
import net.minecraft.class_465;
import net.minecraft.class_495;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.AutoStealHack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={class_495.class})
public abstract class ShulkerBoxScreenMixin
extends class_465<class_1733> {
    @Unique
    private final AutoStealHack autoSteal;

    private ShulkerBoxScreenMixin(WurstClient wurst, class_1733 handler, class_1661 inventory, class_2561 title) {
        super((class_1703)handler, inventory, title);
        this.autoSteal = WurstClient.INSTANCE.getHax().autoStealHack;
    }

    public void method_25426() {
        super.method_25426();
        if (!WurstClient.INSTANCE.isEnabled()) {
            return;
        }
        if (this.autoSteal.areButtonsVisible()) {
            this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Steal"), b -> this.autoSteal.steal(this, 3)).method_46434(this.field_2776 + this.field_2792 - 108, this.field_2800 + 4, 50, 12).method_46431());
            this.method_37063((class_364)class_4185.method_46430((class_2561)class_2561.method_43470((String)"Store"), b -> this.autoSteal.store(this, 3)).method_46434(this.field_2776 + this.field_2792 - 56, this.field_2800 + 4, 50, 12).method_46431());
        }
        if (this.autoSteal.isEnabled()) {
            this.autoSteal.steal(this, 3);
        }
    }
}
