package meteordevelopment.meteorclient.mixin;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ResourceLoadStateTracker;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.server.Services;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={Minecraft.class})
public interface MinecraftAccessor {
    @Accessor(value="fps")
    public static int meteor$getFps() {
        return 0;
    }

    @Mutable
    @Accessor(value="user")
    public void meteor$setUser(User var1);

    @Accessor(value="reloadStateTracker")
    public ResourceLoadStateTracker meteor$getReloadStateTracker();

    @Accessor(value="missTime")
    public int meteor$getMissTime();

    @Accessor(value="missTime")
    public void meteor$setMissTime(int var1);

    @Invoker(value="startAttack")
    public boolean meteor$leftClick();

    @Mutable
    @Accessor(value="profileKeyPairManager")
    public void meteor$setProfileKeyPairManager(ProfileKeyPairManager var1);

    @Mutable
    @Accessor(value="userApiService")
    public void meteor$setUserApiService(UserApiService var1);

    @Mutable
    @Accessor(value="skinManager")
    public void meteor$setSkinManager(SkinManager var1);

    @Mutable
    @Accessor(value="playerSocialManager")
    public void meteor$setPlayerSocialManager(PlayerSocialManager var1);

    @Mutable
    @Accessor(value="reportingContext")
    public void meteor$setReportingContext(ReportingContext var1);

    @Mutable
    @Accessor(value="profileFuture")
    public void meteor$setProfileFuture(CompletableFuture<ProfileResult> var1);

    @Mutable
    @Accessor(value="services")
    public void meteor$setServices(Services var1);

    @Invoker(value="handleKeybinds")
    public void meteor$handleInputEvents();
}
