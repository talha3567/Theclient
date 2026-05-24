package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.InventoryTweaks;
import meteordevelopment.meteorclient.systems.modules.render.BetterTooltips;
import meteordevelopment.meteorclient.systems.modules.render.ItemHighlight;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={AbstractContainerScreen.class})
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu>
extends Screen
implements MenuAccess<T> {
    @Shadow
    protected Slot hoveredSlot;
    @Shadow
    protected int leftPos;
    @Shadow
    protected int topPos;
    @Shadow
    private boolean doubleclick;

    @Shadow
    @Nullable
    protected abstract Slot getHoveredSlot(double var1, double var3);

    @Shadow
    public abstract T getMenu();

    @Shadow
    protected abstract void slotClicked(Slot var1, int var2, int var3, ContainerInput var4);

    @Shadow
    public abstract void onClose();

    public AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method={"init"}, at={@At(value="TAIL")})
    private void onInit(CallbackInfo ci) {
        InventoryTweaks invTweaks = Modules.get().get(InventoryTweaks.class);
        if (invTweaks.isActive() && invTweaks.showButtons() && invTweaks.canSteal((AbstractContainerMenu)this.getMenu())) {
            this.addRenderableWidget((GuiEventListener)new Button.Builder((Component)Component.literal((String)"Steal"), button -> invTweaks.steal((AbstractContainerMenu)this.getMenu())).pos(this.leftPos, this.topPos - 22).size(40, 20).build());
            this.addRenderableWidget((GuiEventListener)new Button.Builder((Component)Component.literal((String)"Dump"), button -> invTweaks.dump((AbstractContainerMenu)this.getMenu())).pos(this.leftPos + 42, this.topPos - 22).size(40, 20).build());
        }
    }

    @Inject(method={"mouseDragged"}, at={@At(value="TAIL")})
    private void onMouseDragged(MouseButtonEvent event, double dx, double dy, CallbackInfoReturnable<Boolean> cir) {
        if (event.button() != 0 || this.doubleclick || !Modules.get().get(InventoryTweaks.class).mouseDragItemMove()) {
            return;
        }
        Slot slot = this.getHoveredSlot(event.x(), event.y());
        if (slot != null && slot.hasItem() && MeteorClient.mc.hasShiftDown()) {
            this.slotClicked(slot, slot.index, event.button(), ContainerInput.QUICK_MOVE);
        }
    }

    @Inject(method={"mouseClicked"}, at={@At(value="HEAD")}, cancellable=true)
    private void mouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        BetterTooltips tooltips = Modules.get().get(BetterTooltips.class);
        if (tooltips.shouldOpenContents((InputWithModifiers)event) && this.hoveredSlot != null && !this.hoveredSlot.getItem().isEmpty() && this.getMenu().getCarried().isEmpty() && tooltips.openContent(this.hoveredSlot.getItem())) {
            cir.setReturnValue((Object)true);
        }
    }

    @Inject(method={"keyPressed"}, at={@At(value="HEAD")}, cancellable=true)
    private void keyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        BetterTooltips tooltips = Modules.get().get(BetterTooltips.class);
        if (tooltips.shouldOpenContents((InputWithModifiers)event) && this.hoveredSlot != null && !this.hoveredSlot.getItem().isEmpty() && this.getMenu().getCarried().isEmpty() && tooltips.openContent(this.hoveredSlot.getItem())) {
            cir.setReturnValue((Object)true);
        }
    }

    @Inject(method={"extractSlot"}, at={@At(value="HEAD")})
    private void onRenderSlot(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        int color = Modules.get().get(ItemHighlight.class).getColor(slot.getItem());
        if (color != -1) {
            graphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, color);
        }
    }

    @ModifyReturnValue(method={"showTooltipWithItemInHand"}, at={@At(value="RETURN")})
    private boolean showTooltipWithItemInHand(boolean original, ItemStack item) {
        Object var4_3 = item.getTooltipImage().orElse(null);
        if (var4_3 instanceof ClientTooltipComponent) {
            ClientTooltipComponent component = var4_3;
            return original || component.showTooltipWithItemInHand();
        }
        return original;
    }
}
