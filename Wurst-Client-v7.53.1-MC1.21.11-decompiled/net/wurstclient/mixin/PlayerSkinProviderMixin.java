package net.wurstclient.mixin;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import net.minecraft.class_1071;
import net.minecraft.class_4844;
import net.minecraft.class_8685;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.json.WsonObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_1071.class})
public abstract class PlayerSkinProviderMixin {
    @Unique
    private static HashMap<String, String> capes;
    @Unique
    private MinecraftProfileTexture currentCape;

    @Inject(method={"method_52859"}, at={@At(value="HEAD")})
    private void onFetchSkinTextures(UUID uuid, MinecraftProfileTextures textures, CallbackInfoReturnable<CompletableFuture<class_8685>> cir) {
        String uuidString = uuid.toString();
        try {
            if (capes == null) {
                this.setupWurstCapes();
            }
            if (capes.containsKey(uuidString)) {
                String capeURL = capes.get(uuidString);
                this.currentCape = new MinecraftProfileTexture(capeURL, null);
            } else {
                this.currentCape = null;
            }
        }
        catch (Exception e) {
            System.err.println("[Wurst] Failed to load cape for UUID " + uuidString);
            e.printStackTrace();
        }
    }

    @ModifyVariable(method={"method_52859"}, at=@At(value="STORE"), ordinal=1, name={"minecraftProfileTexture2"})
    private MinecraftProfileTexture modifyCapeTexture(MinecraftProfileTexture old) {
        if (this.currentCape == null) {
            return old;
        }
        MinecraftProfileTexture result = this.currentCape;
        this.currentCape = null;
        return result;
    }

    @Unique
    private void setupWurstCapes() {
        try {
            capes = new HashMap();
            Pattern uuidPattern = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
            WsonObject rawCapes = JsonUtils.parseURLToObject("https://www.wurstclient.net/api/v1/capes.json");
            for (Map.Entry<String, String> entry : rawCapes.getAllStrings().entrySet()) {
                String name = entry.getKey();
                String capeURL = entry.getValue();
                if (uuidPattern.matcher(name).matches()) {
                    capes.put(name, capeURL);
                    continue;
                }
                String offlineUUID = String.valueOf(class_4844.method_43344((String)name));
                capes.put(offlineUUID, capeURL);
            }
        }
        catch (Exception e) {
            System.err.println("[Wurst] Failed to load capes from wurstclient.net!");
            e.printStackTrace();
        }
    }
}
