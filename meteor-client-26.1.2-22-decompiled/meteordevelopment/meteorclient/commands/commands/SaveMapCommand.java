package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.mixin.MapTextureManagerAccessor;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.resources.MapTextureManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class SaveMapCommand
extends Command {
    private static final SimpleCommandExceptionType MAP_NOT_FOUND = new SimpleCommandExceptionType((Message)Component.literal((String)"You must be holding a filled map."));
    private static final SimpleCommandExceptionType OOPS = new SimpleCommandExceptionType((Message)Component.literal((String)"Something went wrong."));
    private final PointerBuffer filters = BufferUtils.createPointerBuffer((int)1);

    public SaveMapCommand() {
        super("save-map", "Saves a map to an image.", "sm");
        ByteBuffer pngFilter = MemoryUtil.memASCII((CharSequence)"*.png");
        this.filters.put(pngFilter);
        this.filters.rewind();
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        ((LiteralArgumentBuilder)builder.executes(commandContext -> {
            this.saveMap(128);
            return 1;
        })).then(SaveMapCommand.argument("scale", IntegerArgumentType.integer((int)1)).executes(context -> {
            this.saveMap(IntegerArgumentType.getInteger((CommandContext)context, (String)"scale"));
            return 1;
        }));
    }

    private void saveMap(int scale) throws CommandSyntaxException {
        ItemStack map = this.getMap();
        MapItemSavedData state = this.getMapState();
        if (map == null || state == null) {
            throw MAP_NOT_FOUND.create();
        }
        File path = this.getPath();
        if (path == null) {
            throw OOPS.create();
        }
        MapTextureManagerAccessor textureManager = (MapTextureManagerAccessor)SaveMapCommand.mc.gameRenderer.getMinecraft().getMapTextureManager();
        MapTextureManager.MapInstance texture = textureManager.meteor$invokeGetOrCreateMapInstance((MapId)map.get(DataComponents.MAP_ID), state);
        if (texture.texture.getPixels() == null) {
            throw OOPS.create();
        }
        try {
            if (scale == 128) {
                texture.texture.getPixels().writeToFile(path);
            } else {
                int[] data = texture.texture.getPixels().makePixelArray();
                BufferedImage image = new BufferedImage(128, 128, 2);
                image.setRGB(0, 0, image.getWidth(), image.getHeight(), data, 0, 128);
                BufferedImage scaledImage = new BufferedImage(scale, scale, 2);
                scaledImage.createGraphics().drawImage(image, 0, 0, scale, scale, null);
                ImageIO.write((RenderedImage)scaledImage, "png", path);
            }
        }
        catch (IOException e) {
            this.error("Error writing map texture", new Object[0]);
            MeteorClient.LOG.error(e.toString());
        }
    }

    @Nullable
    private MapItemSavedData getMapState() {
        ItemStack map = this.getMap();
        if (map == null) {
            return null;
        }
        return MapItem.getSavedData((MapId)((MapId)map.get(DataComponents.MAP_ID)), (Level)SaveMapCommand.mc.level);
    }

    @Nullable
    private File getPath() {
        Object path = TinyFileDialogs.tinyfd_saveFileDialog((CharSequence)"Save image", null, (PointerBuffer)this.filters, null);
        if (path == null) {
            return null;
        }
        if (!((String)path).endsWith(".png")) {
            path = (String)path + ".png";
        }
        return new File((String)path);
    }

    @Nullable
    private ItemStack getMap() {
        ItemStack itemStack = SaveMapCommand.mc.player.getMainHandItem();
        if (itemStack.getItem() == Items.FILLED_MAP) {
            return itemStack;
        }
        itemStack = SaveMapCommand.mc.player.getOffhandItem();
        if (itemStack.getItem() == Items.FILLED_MAP) {
            return itemStack;
        }
        return null;
    }
}
