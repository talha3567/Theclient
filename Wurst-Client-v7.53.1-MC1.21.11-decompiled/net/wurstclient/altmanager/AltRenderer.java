package net.wurstclient.altmanager;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.class_1068;
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4844;
import net.minecraft.class_640;
import net.minecraft.class_7920;
import net.wurstclient.WurstClient;
import net.wurstclient.altmanager.SkinStealer;

public final class AltRenderer {
    private static final ExecutorService BACKGROUND_THREAD = Executors.newSingleThreadExecutor();
    private static final ConcurrentHashMap<String, class_2960> onlineSkins = new ConcurrentHashMap();
    private static final HashMap<String, class_2960> offlineSkins = new HashMap();

    private static class_2960 getSkinTexture(String name) {
        class_2960 onlineSkin;
        class_2960 offlineSkin;
        if (name.isEmpty()) {
            name = "Steve";
        }
        if ((offlineSkin = offlineSkins.get(name)) == null) {
            AltRenderer.queueOnlineSkinLoading(name);
            offlineSkin = AltRenderer.loadOfflineSkin(name);
        }
        return (onlineSkin = onlineSkins.get(name)) != null ? onlineSkin : offlineSkin;
    }

    private static class_2960 loadOfflineSkin(String name) {
        UUID uuid = class_4844.method_43344((String)name);
        GameProfile profile = new GameProfile(uuid, name);
        class_640 entry = new class_640(profile, false);
        class_2960 texture = entry.method_52810().comp_1626().comp_3627();
        offlineSkins.put(name, texture);
        return texture;
    }

    private static void queueOnlineSkinLoading(String name) {
        class_310 mc = WurstClient.MC;
        ((CompletableFuture)CompletableFuture.supplyAsync(() -> {
            UUID uuid = SkinStealer.getUUIDOrNull(name);
            ProfileResult result = mc.method_73361().comp_837().fetchProfile(uuid, false);
            return result == null ? null : result.profile();
        }, BACKGROUND_THREAD).thenComposeAsync(profile -> {
            if (profile == null) {
                return CompletableFuture.completedFuture(null);
            }
            CompletableFuture skinFuture = mc.method_1582().method_52863(profile);
            return skinFuture.thenApplyAsync(opt -> opt.orElse(null));
        }, (Executor)BACKGROUND_THREAD)).thenAcceptAsync(skinTextures -> {
            if (skinTextures != null) {
                onlineSkins.put(name, skinTextures.comp_1626().comp_3627());
            }
        }, (Executor)BACKGROUND_THREAD);
    }

    public static void drawAltFace(class_332 context, String name, int x, int y, int w, int h, boolean selected) {
        try {
            class_2960 texture = AltRenderer.getSkinTexture(name);
            int color = selected ? -1 : -2039584;
            int fw = 192;
            int fh = 192;
            float u = 24.0f;
            float v = 24.0f;
            context.method_25291(class_10799.field_56883, texture, x, y, u, v, w, h, fw, fh, color);
            fw = 192;
            fh = 192;
            u = 120.0f;
            v = 24.0f;
            context.method_25291(class_10799.field_56883, texture, x, y, u, v, w, h, fw, fh, color);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void drawAltBody(class_332 context, String name, int x, int y, int width, int height) {
        try {
            class_2960 texture = AltRenderer.getSkinTexture(name);
            boolean slim = class_1068.method_4648((UUID)class_4844.method_43344((String)name)).comp_1629() == class_7920.field_41122;
            x += width / 4;
            y += 0;
            int w = width / 2;
            int h = height / 4;
            int fw = height * 2;
            int fh = height * 2;
            float u = height / 4;
            float v = height / 4;
            context.method_25290(class_10799.field_56883, texture, x, y, u, v, w, h, fw, fh);
            w = width / 2;
            h = height / 4;
            u = height / 4 * 5;
            v = height / 4;
            context.method_25290(class_10799.field_56883, texture, x += 0, y += 0, u, v, w, h, fw, fh);
            w = width / 2;
            h = height / 8 * 3;
            u = (float)(height / 4) * 2.5f;
            v = (float)(height / 4) * 2.5f;
            context.method_25290(class_10799.field_56883, texture, x += 0, y += height / 4, u, v, w, h, fw, fh);
            w = width / 2;
            h = height / 8 * 3;
            u = (float)(height / 4) * 2.5f;
            v = (float)(height / 4) * 4.5f;
            context.method_25290(class_10799.field_56883, texture, x += 0, y += 0, u, v, w, h, fw, fh);
            x -= width / 16 * (slim ? 3 : 4);
            w = width / 16 * (slim ? 3 : 4);
            h = height / 8 * 3;
            u = (float)(height / 4) * 5.5f;
            v = (float)(height / 4) * 2.5f;
            context.method_25290(class_10799.field_56883, texture, x, y += slim ? height / 32 : 0, u, v, w, h, fw, fh);
            x += 0;
            w = width / 16 * (slim ? 3 : 4);
            h = height / 8 * 3;
            u = (float)(height / 4) * 5.5f;
            v = (float)(height / 4) * 4.5f;
            context.method_25290(class_10799.field_56883, texture, x, y += 0, u, v, w, h, fw, fh);
            x += width / 16 * (slim ? 11 : 12);
            w = width / 16 * (slim ? 3 : 4);
            h = height / 8 * 3;
            u = (float)(height / 4) * 5.5f;
            v = (float)(height / 4) * 2.5f;
            context.method_25290(class_10799.field_56883, texture, x, y += 0, u, v, w, h, fw, fh);
            x += 0;
            w = width / 16 * (slim ? 3 : 4);
            h = height / 8 * 3;
            u = (float)(height / 4) * 5.5f;
            v = (float)(height / 4) * 4.5f;
            context.method_25290(class_10799.field_56883, texture, x, y += 0, u, v, w, h, fw, fh);
            x -= width / 2;
            int n = slim ? 11 : 12;
            w = width / 4;
            h = height / 8 * 3;
            u = (float)(height / 4) * 0.5f;
            v = (float)(height / 4) * 2.5f;
            context.method_25290(class_10799.field_56883, texture, x, y += height / 32 * n, u, v, w, h, fw, fh);
            w = width / 4;
            h = height / 8 * 3;
            u = (float)(height / 4) * 0.5f;
            v = (float)(height / 4) * 4.5f;
            context.method_25290(class_10799.field_56883, texture, x += 0, y += 0, u, v, w, h, fw, fh);
            w = width / 4;
            h = height / 8 * 3;
            u = (float)(height / 4) * 0.5f;
            v = (float)(height / 4) * 2.5f;
            context.method_25290(class_10799.field_56883, texture, x += width / 4, y += 0, u, v, w, h, fw, fh);
            w = width / 4;
            h = height / 8 * 3;
            u = (float)(height / 4) * 0.5f;
            v = (float)(height / 4) * 4.5f;
            context.method_25290(class_10799.field_56883, texture, x += 0, y += 0, u, v, w, h, fw, fh);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void drawAltBack(class_332 context, String name, int x, int y, int width, int height) {
        try {
            class_2960 texture = AltRenderer.getSkinTexture(name);
            boolean slim = class_1068.method_4648((UUID)class_4844.method_43344((String)name)).comp_1629() == class_7920.field_41122;
            x += width / 4;
            y += 0;
            int w = width / 2;
            int h = height / 4;
            int fw = height * 2;
            int fh = height * 2;
            float u = height / 4 * 3;
            float v = height / 4;
            context.method_25290(class_10799.field_56883, texture, x, y, u, v, w, h, fw, fh);
            w = width / 2;
            h = height / 4;
            u = height / 4 * 7;
            v = height / 4;
            context.method_25290(class_10799.field_56883, texture, x += 0, y += 0, u, v, w, h, fw, fh);
            w = width / 2;
            h = height / 8 * 3;
            u = height / 4 * 4;
            v = (float)(height / 4) * 2.5f;
            context.method_25290(class_10799.field_56883, texture, x += 0, y += height / 4, u, v, w, h, fw, fh);
            w = width / 2;
            h = height / 8 * 3;
            u = height / 4 * 4;
            v = (float)(height / 4) * 4.5f;
            context.method_25290(class_10799.field_56883, texture, x += 0, y += 0, u, v, w, h, fw, fh);
            x -= width / 16 * (slim ? 3 : 4);
            w = width / 16 * (slim ? 3 : 4);
            h = height / 8 * 3;
            u = (float)(height / 4) * (slim ? 6.375f : 6.5f);
            v = (float)(height / 4) * 2.5f;
            context.method_25290(class_10799.field_56883, texture, x, y += slim ? height / 32 : 0, u, v, w, h, fw, fh);
            x += 0;
            w = width / 16 * (slim ? 3 : 4);
            h = height / 8 * 3;
            u = (float)(height / 4) * (slim ? 6.375f : 6.5f);
            v = (float)(height / 4) * 4.5f;
            context.method_25290(class_10799.field_56883, texture, x, y += 0, u, v, w, h, fw, fh);
            x += width / 16 * (slim ? 11 : 12);
            w = width / 16 * (slim ? 3 : 4);
            h = height / 8 * 3;
            u = (float)(height / 4) * (slim ? 6.375f : 6.5f);
            v = (float)(height / 4) * 2.5f;
            context.method_25290(class_10799.field_56883, texture, x, y += 0, u, v, w, h, fw, fh);
            x += 0;
            w = width / 16 * (slim ? 3 : 4);
            h = height / 8 * 3;
            u = (float)(height / 4) * (slim ? 6.375f : 6.5f);
            v = (float)(height / 4) * 4.5f;
            context.method_25290(class_10799.field_56883, texture, x, y += 0, u, v, w, h, fw, fh);
            x -= width / 2;
            int n = slim ? 11 : 12;
            w = width / 4;
            h = height / 8 * 3;
            u = (float)(height / 4) * 1.5f;
            v = (float)(height / 4) * 2.5f;
            context.method_25290(class_10799.field_56883, texture, x, y += height / 32 * n, u, v, w, h, fw, fh);
            w = width / 4;
            h = height / 8 * 3;
            u = (float)(height / 4) * 1.5f;
            v = (float)(height / 4) * 4.5f;
            context.method_25290(class_10799.field_56883, texture, x += 0, y += 0, u, v, w, h, fw, fh);
            w = width / 4;
            h = height / 8 * 3;
            u = (float)(height / 4) * 1.5f;
            v = (float)(height / 4) * 2.5f;
            context.method_25290(class_10799.field_56883, texture, x += width / 4, y += 0, u, v, w, h, fw, fh);
            w = width / 4;
            h = height / 8 * 3;
            u = (float)(height / 4) * 1.5f;
            v = (float)(height / 4) * 4.5f;
            context.method_25290(class_10799.field_56883, texture, x += 0, y += 0, u, v, w, h, fw, fh);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
