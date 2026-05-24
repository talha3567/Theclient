package meteordevelopment.meteorclient.utils.misc.input;

import com.mojang.blaze3d.platform.InputConstants;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class KeyBinds {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register((Identifier)MeteorClient.identifier("meteor-client"));
    public static KeyMapping OPEN_GUI = new KeyMapping("key.meteor-client.open-gui", InputConstants.Type.KEYSYM, 344, CATEGORY);
    public static KeyMapping OPEN_COMMANDS = new KeyMapping("key.meteor-client.open-commands", InputConstants.Type.KEYSYM, 46, CATEGORY);

    private KeyBinds() {
    }

    public static KeyMapping[] apply(KeyMapping[] binds) {
        KeyMapping[] newBinds = new KeyMapping[binds.length + 2];
        System.arraycopy(binds, 0, newBinds, 0, binds.length);
        newBinds[binds.length] = OPEN_GUI;
        newBinds[binds.length + 1] = OPEN_COMMANDS;
        return newBinds;
    }
}
