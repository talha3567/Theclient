package net.wurstclient.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_447;
import net.minecraft.class_8021;
import net.minecraft.class_8132;
import net.minecraft.class_8667;
import net.wurstclient.WurstClient;
import net.wurstclient.options.WurstOptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_447.class})
public abstract class StatsScreenMixin
extends class_437 {
    @Unique
    private class_4185 wurstOptionsButton;

    public StatsScreenMixin(WurstClient wurst, class_2561 title) {
        super(title);
    }

    @WrapOperation(method={"method_25426"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_8132;method_48996(Lnet/minecraft/class_8021;)Lnet/minecraft/class_8021;", ordinal=0)})
    private <T extends class_8021> T onAddToFooter(class_8132 layout, T doneWidget, Operation<T> original) {
        if (!(doneWidget instanceof class_4185)) {
            throw new IllegalStateException("The done button in the statistics screen somehow isn't a button");
        }
        class_4185 doneButton = (class_4185)doneWidget;
        WurstClient wurst = WurstClient.INSTANCE;
        if (wurst.getOtfs().disableOtf.shouldHideEnableButton()) {
            return (T)((class_8021)original.call(new Object[]{layout, doneButton}));
        }
        class_8667 vLayout = class_8667.method_52741().method_52735(5);
        class_8667 hLayout = class_8667.method_52742().method_52735(5);
        class_4185 toggleButton = class_4185.method_46430((class_2561)this.getToggleButtonText(), this::toggleWurst).method_46432(100).method_46431();
        hLayout.method_52736((class_8021)toggleButton);
        doneButton.method_25358(100);
        hLayout.method_52736((class_8021)doneButton);
        if (wurst.getOtfs().wurstOptionsOtf.isVisibleInStatistics()) {
            layout.method_48991(58);
            this.wurstOptionsButton = WurstClient.INSTANCE.getOtfs().wurstOptionsOtf.buttonBuilder(this::openWurstOptions).method_46432(205).method_46431();
            vLayout.method_52736((class_8021)this.wurstOptionsButton);
        }
        vLayout.method_52736((class_8021)hLayout);
        return (T)((class_8021)original.call(new Object[]{layout, vLayout}));
    }

    @Inject(method={"method_25394"}, at={@At(value="TAIL")})
    private void onRender(class_332 context, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        WurstClient.INSTANCE.getOtfs().wurstOptionsOtf.drawWurstLogoOnButton(context, this.wurstOptionsButton);
    }

    @Unique
    private void openWurstOptions(class_4185 button) {
        this.field_22787.method_1507((class_437)new WurstOptionsScreen(this));
    }

    @Unique
    private void toggleWurst(class_4185 toggleButton) {
        WurstClient wurst;
        wurst.setEnabled(!(wurst = WurstClient.INSTANCE).isEnabled());
        toggleButton.method_25355(this.getToggleButtonText());
        if (this.wurstOptionsButton != null) {
            this.wurstOptionsButton.field_22763 = wurst.isEnabled();
        }
    }

    @Unique
    private class_2561 getToggleButtonText() {
        WurstClient wurst = WurstClient.INSTANCE;
        String text = (wurst.isEnabled() ? "Disable" : "Enable") + " Wurst";
        return class_2561.method_43470((String)text);
    }
}
