package meteordevelopment.meteorclient.mixin;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.BetterBeacons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BeaconScreen.class})
public abstract class BeaconScreenMixin
extends AbstractContainerScreen<BeaconMenu> {
    @Shadow
    protected abstract <T extends AbstractWidget> void addBeaconButton(T var1);

    public BeaconScreenMixin(BeaconMenu handler, Inventory inventory, Component title) {
        super((AbstractContainerMenu)handler, inventory, title);
    }

    @Inject(method={"init"}, at={@At(value="INVOKE", target="Ljava/util/List;clear()V", shift=At.Shift.AFTER)}, cancellable=true)
    private void changeButtons(CallbackInfo ci) {
        if (!Modules.get().get(BetterBeacons.class).isActive()) {
            return;
        }
        List effects = BeaconBlockEntity.BEACON_EFFECTS.stream().flatMap(Collection::stream).toList();
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof BeaconScreen) {
            BeaconScreen beaconScreen;
            BeaconScreen beaconScreen2 = beaconScreen = (BeaconScreen)screen;
            Objects.requireNonNull(beaconScreen2);
            this.addBeaconButton(new BeaconScreen.BeaconConfirmButton(beaconScreen2, this.leftPos + 164, this.topPos + 107));
            BeaconScreen beaconScreen3 = beaconScreen;
            Objects.requireNonNull(beaconScreen3);
            this.addBeaconButton(new BeaconScreen.BeaconCancelButton(beaconScreen3, this.leftPos + 190, this.topPos + 107));
            for (int x = 0; x < 3; ++x) {
                for (int y = 0; y < 2; ++y) {
                    Holder effect = (Holder)effects.get(x * 2 + y);
                    int xMin = this.leftPos + x * 25;
                    int yMin = this.topPos + y * 25;
                    BeaconScreen beaconScreen4 = beaconScreen;
                    Objects.requireNonNull(beaconScreen4);
                    this.addBeaconButton(new BeaconScreen.BeaconPowerButton(beaconScreen4, xMin + 27, yMin + 32, effect, true, -1));
                    BeaconScreen beaconScreen5 = beaconScreen;
                    Objects.requireNonNull(beaconScreen5);
                    BeaconScreen.BeaconPowerButton secondaryWidget = new BeaconScreen.BeaconPowerButton(beaconScreen5, xMin + 133, yMin + 32, effect, false, 3);
                    if (((BeaconMenu)this.getMenu()).getLevels() != 4) {
                        secondaryWidget.active = false;
                    }
                    this.addBeaconButton(secondaryWidget);
                }
            }
        }
        ci.cancel();
    }

    @Inject(method={"extractBackground"}, at={@At(value="TAIL")})
    private void onExtractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (!Modules.get().get(BetterBeacons.class).isActive()) {
            return;
        }
        graphics.fill(this.leftPos + 10, this.topPos + 7, this.leftPos + 220, this.topPos + 98, -14606047);
    }
}
