package meteordevelopment.meteorclient.systems.accounts;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ServicesKeySet;
import com.mojang.authlib.yggdrasil.ServicesKeyType;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixin.FileCacheAccessor;
import meteordevelopment.meteorclient.mixin.MinecraftAccessor;
import meteordevelopment.meteorclient.mixin.SkinManagerAccessor;
import meteordevelopment.meteorclient.systems.accounts.AccountCache;
import meteordevelopment.meteorclient.systems.accounts.AccountType;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.misc.NbtException;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.client.renderer.texture.SkinTextureDownloader;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.Services;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.Util;

public abstract class Account<T extends Account<?>>
implements ISerializable<T> {
    protected AccountType type;
    protected String name;
    protected final AccountCache cache;

    protected Account(AccountType type, String name) {
        this.type = type;
        this.name = name;
        this.cache = new AccountCache();
    }

    public abstract boolean fetchInfo();

    public boolean login() {
        YggdrasilAuthenticationService authenticationService = new YggdrasilAuthenticationService(MeteorClient.mc.getProxy());
        Account.applyLoginEnvironment(authenticationService);
        return true;
    }

    public String getUsername() {
        if (this.cache.username.isEmpty()) {
            return this.name;
        }
        return this.cache.username;
    }

    public AccountType getType() {
        return this.type;
    }

    public AccountCache getCache() {
        return this.cache;
    }

    public static void setSession(User session) {
        MinecraftAccessor mca = (MinecraftAccessor)MeteorClient.mc;
        mca.meteor$setUser(session);
        YggdrasilAuthenticationService yggdrasilAuthenticationService = new YggdrasilAuthenticationService(MeteorClient.mc.getProxy());
        UserApiService apiService = yggdrasilAuthenticationService.createUserApiService(session.getAccessToken());
        mca.meteor$setUserApiService(apiService);
        mca.meteor$setPlayerSocialManager(new PlayerSocialManager(MeteorClient.mc, apiService));
        mca.meteor$setProfileKeyPairManager(ProfileKeyPairManager.create((UserApiService)apiService, (User)session, (Path)MeteorClient.mc.gameDirectory.toPath()));
        mca.meteor$setReportingContext(ReportingContext.create((ReportEnvironment)ReportEnvironment.local(), (UserApiService)apiService));
        mca.meteor$setProfileFuture(CompletableFuture.supplyAsync(() -> MeteorClient.mc.services().sessionService().fetchProfile(MeteorClient.mc.getUser().getProfileId(), true), (Executor)Util.ioPool()));
    }

    public static void applyLoginEnvironment(YggdrasilAuthenticationService authService) {
        MinecraftAccessor mca = (MinecraftAccessor)MeteorClient.mc;
        SignatureValidator.from((ServicesKeySet)authService.getServicesKeySet(), (ServicesKeyType)ServicesKeyType.PROFILE_KEY);
        SkinManager.TextureCache skinCache = ((SkinManagerAccessor)MeteorClient.mc.getSkinManager()).meteor$getSkinTextures();
        Path skinCachePath = ((FileCacheAccessor)skinCache).meteor$getRoot();
        mca.meteor$setServices(Services.create((YggdrasilAuthenticationService)authService, (File)MeteorClient.mc.gameDirectory));
        mca.meteor$setSkinManager(new SkinManager(skinCachePath, MeteorClient.mc.services(), new SkinTextureDownloader(MeteorClient.mc.getProxy(), MeteorClient.mc.getTextureManager(), (Executor)MeteorClient.mc), (Executor)MeteorClient.mc));
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", this.type.name());
        tag.putString("name", this.name);
        tag.put("cache", (Tag)this.cache.toTag());
        return tag;
    }

    @Override
    public T fromTag(CompoundTag tag) {
        if (tag.getString("name").isEmpty() || tag.getCompound("cache").isEmpty()) {
            throw new NbtException();
        }
        this.name = (String)tag.getString("name").get();
        this.cache.fromTag((CompoundTag)tag.getCompound("cache").get());
        return (T)this;
    }
}
