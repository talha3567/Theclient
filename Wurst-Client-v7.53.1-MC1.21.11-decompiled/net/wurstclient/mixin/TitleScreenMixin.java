package net.wurstclient.mixin;

import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.class_1074;
import net.minecraft.class_2561;
import net.minecraft.class_339;
import net.minecraft.class_364;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.wurstclient.WurstClient;
import net.wurstclient.altmanager.screens.AltManagerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_442.class})
public abstract class TitleScreenMixin
extends class_437 {
    private class_339 realmsButton = null;
    private class_4185 altsButton;

    private TitleScreenMixin(WurstClient wurst, class_2561 title) {
        super(title);
    }

    @Inject(method={"method_2249"}, at={@At(value="RETURN")})
    private void onAddNormalWidgets(int y, int spacingY, CallbackInfoReturnable<Integer> cir) {
        if (!WurstClient.INSTANCE.isEnabled()) {
            return;
        }
        for (class_339 button : Screens.getButtons((class_437)this)) {
            if (!button.method_25369().getString().equals(class_1074.method_4662((String)"menu.online", (Object[])new Object[0]))) continue;
            this.realmsButton = button;
            break;
        }
        if (this.realmsButton == null) {
            throw new IllegalStateException("Couldn't find realms button!");
        }
        this.realmsButton.method_25358(98);
        this.altsButton = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Alt Manager"), b -> this.field_22787.method_1507((class_437)new AltManagerScreen(this, WurstClient.INSTANCE.getAltManager()))).method_46434(this.field_22789 / 2 + 2, this.realmsButton.method_46427(), 98, 20).method_46431();
        this.method_37063((class_364)this.altsButton);
    }

    @Inject(method={"method_25393"}, at={@At(value="RETURN")})
    private void onTick(CallbackInfo ci) {
        if (this.realmsButton == null || this.altsButton == null) {
            return;
        }
        this.altsButton.method_46419(this.realmsButton.method_46427());
    }

    @Inject(method={"method_44692"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetMultiplayerDisabledText(CallbackInfoReturnable<class_2561> cir) {
        cir.setReturnValue(null);
    }
}
