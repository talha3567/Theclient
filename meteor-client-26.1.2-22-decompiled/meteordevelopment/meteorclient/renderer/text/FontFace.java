package meteordevelopment.meteorclient.renderer.text;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import meteordevelopment.meteorclient.renderer.text.BuiltinFontFace;
import meteordevelopment.meteorclient.renderer.text.FontInfo;
import meteordevelopment.meteorclient.renderer.text.SystemFontFace;
import meteordevelopment.meteorclient.utils.files.ByteBufferUtils;
import org.jspecify.annotations.NullMarked;
import org.lwjgl.BufferUtils;

@NullMarked
public abstract sealed class FontFace
permits BuiltinFontFace, SystemFontFace {
    public final FontInfo info;

    protected FontFace(FontInfo info) {
        this.info = info;
    }

    public abstract ReadableByteChannel byteChannelForRead() throws IOException;

    public final ByteBuffer readToDirectByteBuffer() throws IOException {
        try (ReadableByteChannel channel = this.byteChannelForRead();){
            ByteBuffer byteBuffer = ByteBufferUtils.readFully(channel, BufferUtils::createByteBuffer);
            return byteBuffer;
        }
    }

    public String toString() {
        return this.info.toString();
    }
}
