package meteordevelopment.meteorclient.mixin;

import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.screens.EditBookTitleAndAuthorScreen;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BookViewScreen.class})
public abstract class BookViewScreenMixin
extends Screen {
    @Shadow
    private BookViewScreen.BookAccess bookAccess;
    @Shadow
    private int currentPage;

    @Shadow
    protected abstract void pageForward();

    @Shadow
    protected abstract void pageBack();

    public BookViewScreenMixin(Component title) {
        super(title);
    }

    @Inject(method={"init"}, at={@At(value="TAIL")})
    private void onInit(CallbackInfo ci) {
        this.addRenderableWidget((GuiEventListener)new Button.Builder((Component)Component.literal((String)"Copy"), button -> {
            ListTag listTag = new ListTag();
            for (int i = 0; i < this.bookAccess.getPageCount(); ++i) {
                listTag.add((Object)StringTag.valueOf((String)this.bookAccess.getPage(i).getString()));
            }
            CompoundTag tag = new CompoundTag();
            tag.put("pages", (Tag)listTag);
            tag.putInt("currentPage", this.currentPage);
            FastByteArrayOutputStream bytes = new FastByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream((OutputStream)bytes);
            try {
                NbtIo.write((CompoundTag)tag, (DataOutput)out);
            }
            catch (IOException e) {
                MeteorClient.LOG.error("Error writing the book to the output stream", e);
            }
            String encoded = Base64.getEncoder().encodeToString(bytes.array);
            long available = MemoryStack.stackGet().getPointer();
            long size = MemoryUtil.memLengthUTF8((CharSequence)encoded, (boolean)true);
            if (size > available) {
                ChatUtils.error("Could not copy to clipboard: Out of memory.", new Object[0]);
            } else {
                GLFW.glfwSetClipboardString((long)MeteorClient.mc.getWindow().handle(), (CharSequence)encoded);
            }
        }).pos(4, 4).size(120, 20).build());
        ItemStack itemStack = MeteorClient.mc.player.getMainHandItem();
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (itemStack.getItem() != Items.WRITTEN_BOOK) {
            itemStack = MeteorClient.mc.player.getOffhandItem();
            hand = InteractionHand.OFF_HAND;
        }
        if (itemStack.getItem() != Items.WRITTEN_BOOK) {
            return;
        }
        ItemStack book = itemStack;
        InteractionHand hand2 = hand;
        this.addRenderableWidget((GuiEventListener)new Button.Builder((Component)Component.literal((String)"Edit title & author"), button -> MeteorClient.mc.setScreen((Screen)new EditBookTitleAndAuthorScreen(GuiThemes.get(), book, hand2))).pos(4, 26).size(120, 20).build());
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount == 0.0) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        if (verticalAmount < 0.0) {
            this.pageForward();
        } else {
            this.pageBack();
        }
        return true;
    }
}
