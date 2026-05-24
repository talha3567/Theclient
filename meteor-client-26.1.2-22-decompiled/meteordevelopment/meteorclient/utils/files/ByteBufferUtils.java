package meteordevelopment.meteorclient.utils.files;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.IntFunction;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ByteBufferUtils {
    private ByteBufferUtils() {
    }

    public static ByteBuffer readFully(Path path, IntFunction<ByteBuffer> allocator) throws IOException {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);){
            long size = channel.size();
            if (size > Integer.MAX_VALUE) {
                throw new IOException("File too large to read into ByteBuffer: " + String.valueOf(path));
            }
            ByteBuffer byteBuffer = ByteBufferUtils.readFully(channel, (int)size, allocator);
            return byteBuffer;
        }
    }

    public static ByteBuffer readFully(ReadableByteChannel channel, IntFunction<ByteBuffer> allocator) throws IOException {
        int bytesRead;
        if (channel instanceof SeekableByteChannel) {
            SeekableByteChannel seekableChannel = (SeekableByteChannel)channel;
            long size = seekableChannel.size();
            if (size > Integer.MAX_VALUE) {
                throw new IOException("Channel content too large to read into ByteBuffer");
            }
            return ByteBufferUtils.readFully(seekableChannel, (int)size, allocator);
        }
        ByteBuffer buffer = ByteBufferUtils.requireCapacity(allocator.apply(8192), 8192);
        while ((bytesRead = channel.read(buffer)) != -1) {
            if (bytesRead == 0) {
                if (buffer.hasRemaining()) break;
                buffer = ByteBufferUtils.grow(buffer, allocator);
                continue;
            }
            if (buffer.hasRemaining()) continue;
            buffer = ByteBufferUtils.grow(buffer, allocator);
        }
        buffer.flip();
        return buffer;
    }

    private static ByteBuffer readFully(SeekableByteChannel channel, int size, IntFunction<ByteBuffer> allocator) throws IOException {
        int bytesRead;
        ByteBuffer buffer = ByteBufferUtils.requireCapacity(allocator.apply(size), size);
        while (buffer.hasRemaining() && (bytesRead = channel.read(buffer)) != -1) {
        }
        buffer.flip();
        return buffer;
    }

    private static ByteBuffer grow(ByteBuffer buffer, IntFunction<ByteBuffer> allocator) {
        int oldCap = buffer.capacity();
        int newCap = oldCap << 1;
        if (newCap <= 0) {
            throw new OutOfMemoryError("Buffer too large (overflow): " + oldCap);
        }
        ByteBuffer newBuffer = ByteBufferUtils.requireCapacity(allocator.apply(newCap), newCap);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }

    private static ByteBuffer requireCapacity(ByteBuffer buf, int minCap) {
        if (buf.capacity() < minCap) {
            throw new IllegalArgumentException("Allocator returned capacity " + buf.capacity() + " < " + minCap);
        }
        return buf;
    }
}
