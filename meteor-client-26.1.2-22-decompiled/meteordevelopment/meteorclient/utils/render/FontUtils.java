package meteordevelopment.meteorclient.utils.render;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.Fonts;
import meteordevelopment.meteorclient.renderer.text.BuiltinFontFace;
import meteordevelopment.meteorclient.renderer.text.FontFace;
import meteordevelopment.meteorclient.renderer.text.FontFamily;
import meteordevelopment.meteorclient.renderer.text.FontInfo;
import meteordevelopment.meteorclient.renderer.text.SystemFontFace;
import meteordevelopment.meteorclient.utils.files.ByteBufferUtils;
import net.minecraft.util.Util;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;

@NullMarked
public final class FontUtils {
    private FontUtils() {
    }

    public static @Nullable FontInfo getSysFontInfo(File file) {
        return FontUtils.getFontInfo(file);
    }

    public static @Nullable FontInfo getBuiltinFontInfo(String builtin) {
        return FontUtils.getFontInfo(FontUtils.builtinFontStream(builtin));
    }

    private static @Nullable FontInfo getFontInfo(@Nullable File file) {
        if (file == null || !file.isFile()) {
            return null;
        }
        try {
            return FontUtils.getFontInfo(ByteBufferUtils.readFully(file.toPath(), BufferUtils::createByteBuffer));
        }
        catch (Exception e) {
            MeteorClient.LOG.warn("Failed to read font file: {}", (Object)file, (Object)e);
            return null;
        }
    }

    public static @Nullable FontInfo getFontInfo(@Nullable InputStream stream) {
        FontInfo fontInfo;
        block9: {
            if (stream == null) {
                return null;
            }
            ReadableByteChannel ch = Channels.newChannel(stream);
            try {
                ByteBuffer buf = ByteBufferUtils.readFully(ch, BufferUtils::createByteBuffer);
                fontInfo = FontUtils.getFontInfo(buf);
                if (ch == null) break block9;
            }
            catch (Throwable throwable) {
                try {
                    if (ch != null) {
                        try {
                            ch.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (Exception e) {
                    MeteorClient.LOG.warn("Failed to read font stream.", e);
                    return null;
                }
            }
            ch.close();
        }
        return fontInfo;
    }

    private static @Nullable FontInfo getFontInfo(ByteBuffer buffer) {
        if (buffer.remaining() < 5) {
            return null;
        }
        if (buffer.get(0) != 0 || buffer.get(1) != 1 || buffer.get(2) != 0 || buffer.get(3) != 0 || buffer.get(4) != 0) {
            return null;
        }
        STBTTFontinfo fontInfo = STBTTFontinfo.create();
        if (!STBTruetype.stbtt_InitFont((STBTTFontinfo)fontInfo, (ByteBuffer)buffer)) {
            return null;
        }
        ByteBuffer nameBuffer = STBTruetype.stbtt_GetFontNameString((STBTTFontinfo)fontInfo, (int)3, (int)1, (int)1033, (int)1);
        ByteBuffer typeBuffer = STBTruetype.stbtt_GetFontNameString((STBTTFontinfo)fontInfo, (int)3, (int)1, (int)1033, (int)2);
        if (typeBuffer == null || nameBuffer == null) {
            return null;
        }
        return new FontInfo(StandardCharsets.UTF_16.decode(nameBuffer).toString(), FontInfo.Type.fromString(StandardCharsets.UTF_16.decode(typeBuffer).toString()));
    }

    public static Set<String> getSearchPaths() {
        ObjectOpenHashSet paths = new ObjectOpenHashSet();
        paths.add(System.getProperty("java.home") + "/lib/fonts");
        for (File dir : FontUtils.getUFontDirs()) {
            if (!dir.exists()) continue;
            paths.add(dir.getAbsolutePath());
        }
        for (File dir : FontUtils.getSFontDirs()) {
            if (!dir.exists()) continue;
            paths.add(dir.getAbsolutePath());
        }
        return paths;
    }

    public static List<File> getUFontDirs() {
        return switch (Util.getPlatform()) {
            case Util.OS.WINDOWS -> List.of(new File(System.getProperty("user.home") + "\\AppData\\Local\\Microsoft\\Windows\\Fonts"));
            case Util.OS.OSX -> List.of(new File(System.getProperty("user.home") + "/Library/Fonts/"));
            default -> List.of(new File(System.getProperty("user.home") + "/.local/share/fonts"), new File(System.getProperty("user.home") + "/.fonts"));
        };
    }

    public static List<File> getSFontDirs() {
        return switch (Util.getPlatform()) {
            case Util.OS.WINDOWS -> List.of(new File(System.getenv("SystemRoot") + "\\Fonts"));
            case Util.OS.OSX -> List.of(new File("/System/Library/Fonts/"));
            default -> List.of(new File("/usr/share/fonts/"));
        };
    }

    public static void loadBuiltin(List<FontFamily> fontList, String builtin) {
        FontInfo fontInfo = FontUtils.getBuiltinFontInfo(builtin);
        if (fontInfo == null) {
            return;
        }
        BuiltinFontFace fontFace = new BuiltinFontFace(fontInfo, builtin);
        if (!FontUtils.addFont(fontList, fontFace)) {
            MeteorClient.LOG.warn("Failed to load builtin font {}", (Object)fontFace);
        }
    }

    public static void loadSystem(List<FontFamily> fontList, File dir) {
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles(file -> file.isFile() && file.getName().endsWith(".ttf") || file.isDirectory());
        if (files == null) {
            return;
        }
        for (File file2 : files) {
            SystemFontFace fontFace;
            if (file2.isDirectory()) {
                FontUtils.loadSystem(fontList, file2);
                continue;
            }
            FontInfo fontInfo = FontUtils.getSysFontInfo(file2);
            if (fontInfo == null) continue;
            boolean isBuiltin = false;
            for (String builtinFont : Fonts.BUILTIN_FONTS) {
                if (!builtinFont.equals(fontInfo.family())) continue;
                isBuiltin = true;
                break;
            }
            if (isBuiltin || FontUtils.addFont(fontList, fontFace = new SystemFontFace(fontInfo, file2.toPath()))) continue;
            MeteorClient.LOG.warn("Failed to load system font {}", (Object)fontFace);
        }
    }

    private static boolean addFont(List<FontFamily> fontList, @Nullable FontFace font) {
        if (font == null) {
            return false;
        }
        FontInfo info = font.info;
        FontFamily family = Fonts.getFamily(info.family());
        if (family == null) {
            family = new FontFamily(info.family());
            fontList.add(family);
        }
        if (family.hasType(info.type())) {
            return false;
        }
        return family.addFont(font);
    }

    public static @Nullable InputStream builtinFontStream(String name) {
        return FontUtils.class.getResourceAsStream("/assets/meteor-client/fonts/" + name + ".ttf");
    }
}
