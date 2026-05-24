package meteordevelopment.meteorclient.mixin;

import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BookEditScreen.class})
public abstract class BookEditScreenMixin
extends Screen {
    @Shadow
    @Final
    private List<String> pages;
    @Shadow
    private int currentPage;

    @Shadow
    protected abstract void updatePageContent();

    @Shadow
    protected abstract void pageForward();

    @Shadow
    protected abstract void pageBack();

    public BookEditScreenMixin(Component title) {
        super(title);
    }

    @Inject(method={"init"}, at={@At(value="TAIL")})
    private void onInit(CallbackInfo ci) {
        this.addRenderableWidget((GuiEventListener)new Button.Builder((Component)Component.literal((String)"Copy"), button -> {
            ListTag listTag = new ListTag();
            this.pages.stream().map(StringTag::valueOf).forEach(arg_0 -> listTag.add(arg_0));
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
            try {
                GLFW.glfwSetClipboardString((long)MeteorClient.mc.getWindow().handle(), (CharSequence)Base64.getEncoder().encodeToString(bytes.array));
            }
            catch (OutOfMemoryError exception) {
                GLFW.glfwSetClipboardString((long)MeteorClient.mc.getWindow().handle(), (CharSequence)exception.toString());
            }
        }).pos(4, 4).size(120, 20).build());
        this.addRenderableWidget((GuiEventListener)new Button.Builder((Component)Component.literal((String)"Paste"), button -> {
            byte[] bytes;
            String clipboard = GLFW.glfwGetClipboardString((long)MeteorClient.mc.getWindow().handle());
            if (clipboard == null) {
                return;
            }
            try {
                bytes = Base64.getDecoder().decode(clipboard);
            }
            catch (IllegalArgumentException illegalArgumentException) {
                return;
            }
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
            try {
                CompoundTag tag = NbtIo.readCompressed((InputStream)in, (NbtAccounter)NbtAccounter.unlimitedHeap());
                ListTag listTag = tag.getListOrEmpty("pages").copy();
                this.pages.clear();
                for (int i = 0; i < listTag.size(); ++i) {
                    this.pages.add(listTag.getStringOr(i, ""));
                }
                if (this.pages.isEmpty()) {
                    this.pages.add("");
                }
                this.currentPage = tag.getIntOr("currentPage", 0);
                this.updatePageContent();
            }
            catch (IOException e) {
                MeteorClient.LOG.error("Error reading the data from your clipboard", e);
            }
        }).pos(4, 26).size(120, 20).build());
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
