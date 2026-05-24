package meteordevelopment.meteorclient.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

public class MeshBuilder {
    private static final boolean DEBUG = FabricLoader.getInstance().isDevelopmentEnvironment() || Boolean.getBoolean("meteor.render.debug");
    public double alpha = 1.0;
    private final VertexFormat format;
    private final int primitiveVerticesSize;
    private final int primitiveIndicesCount;
    private ByteBuffer vertices = null;
    private long verticesPointerStart;
    private long verticesPointer;
    private ByteBuffer indices = null;
    private long indicesPointer;
    private int vertexI;
    private int indicesCount;
    private boolean building;
    private double cameraX;
    private double cameraZ;

    public MeshBuilder(RenderPipeline pipeline) {
        this(pipeline.getVertexFormat(), pipeline.getVertexFormatMode());
    }

    public MeshBuilder(VertexFormat format, VertexFormat.Mode drawMode) {
        this.format = format;
        this.primitiveVerticesSize = format.getVertexSize();
        this.primitiveIndicesCount = drawMode.primitiveLength;
    }

    public MeshBuilder(VertexFormat format, VertexFormat.Mode drawMode, int vertexCount, int indexCount) {
        this(format, drawMode);
        this.allocateBuffers(vertexCount, indexCount);
    }

    public void begin() {
        if (this.building) {
            throw new IllegalStateException("Mesh.begin() called while already building.");
        }
        this.verticesPointer = this.verticesPointerStart;
        this.vertexI = 0;
        this.indicesCount = 0;
        this.building = true;
        if (Utils.rendering3D) {
            Vec3 camera = MeteorClient.mc.gameRenderer.getMainCamera().position();
            this.cameraX = camera.x;
            this.cameraZ = camera.z;
        } else {
            this.cameraX = 0.0;
            this.cameraZ = 0.0;
        }
    }

    public MeshBuilder vec3(double x, double y, double z) {
        this.debugVertexBufferCapacity();
        long p = this.verticesPointer;
        MemoryUtil.memPutFloat((long)p, (float)((float)(x - this.cameraX)));
        MemoryUtil.memPutFloat((long)(p + 4L), (float)((float)y));
        MemoryUtil.memPutFloat((long)(p + 8L), (float)((float)(z - this.cameraZ)));
        this.verticesPointer += 12L;
        return this;
    }

    public MeshBuilder vec2(double x, double y) {
        this.debugVertexBufferCapacity();
        long p = this.verticesPointer;
        MemoryUtil.memPutFloat((long)p, (float)((float)x));
        MemoryUtil.memPutFloat((long)(p + 4L), (float)((float)y));
        this.verticesPointer += 8L;
        return this;
    }

    public MeshBuilder color(Color c) {
        this.debugVertexBufferCapacity();
        long p = this.verticesPointer;
        MemoryUtil.memPutByte((long)p, (byte)((byte)c.r));
        MemoryUtil.memPutByte((long)(p + 1L), (byte)((byte)c.g));
        MemoryUtil.memPutByte((long)(p + 2L), (byte)((byte)c.b));
        MemoryUtil.memPutByte((long)(p + 3L), (byte)((byte)((float)c.a * (float)this.alpha)));
        this.verticesPointer += 4L;
        return this;
    }

    public int next() {
        return this.vertexI++;
    }

    public void line(int i1, int i2) {
        this.debugIndexBufferCapacity();
        long p = this.indicesPointer + (long)this.indicesCount * 4L;
        MemoryUtil.memPutInt((long)p, (int)i1);
        MemoryUtil.memPutInt((long)(p + 4L), (int)i2);
        this.indicesCount += 2;
    }

    public void quad(int i1, int i2, int i3, int i4) {
        this.debugIndexBufferCapacity();
        long p = this.indicesPointer + (long)this.indicesCount * 4L;
        MemoryUtil.memPutInt((long)p, (int)i1);
        MemoryUtil.memPutInt((long)(p + 4L), (int)i2);
        MemoryUtil.memPutInt((long)(p + 8L), (int)i3);
        MemoryUtil.memPutInt((long)(p + 12L), (int)i3);
        MemoryUtil.memPutInt((long)(p + 16L), (int)i4);
        MemoryUtil.memPutInt((long)(p + 20L), (int)i1);
        this.indicesCount += 6;
    }

    public void triangle(int i1, int i2, int i3) {
        this.debugIndexBufferCapacity();
        long p = this.indicesPointer + (long)this.indicesCount * 4L;
        MemoryUtil.memPutInt((long)p, (int)i1);
        MemoryUtil.memPutInt((long)(p + 4L), (int)i2);
        MemoryUtil.memPutInt((long)(p + 8L), (int)i3);
        this.indicesCount += 3;
    }

    public void ensureQuadCapacity() {
        this.ensureCapacity(4, 6);
    }

    public void ensureTriCapacity() {
        this.ensureCapacity(3, 3);
    }

    public void ensureLineCapacity() {
        this.ensureCapacity(2, 2);
    }

    public void ensureCapacity(int vertexCount, int indexCount) {
        if (DEBUG && indexCount % this.primitiveIndicesCount != 0) {
            throw new IllegalArgumentException("Unexpected amount of indices written to MeshBuilder.");
        }
        if (this.vertices == null || this.indices == null) {
            this.allocateBuffers(1024, 2048);
            return;
        }
        if ((this.vertexI + vertexCount) * this.primitiveVerticesSize >= this.vertices.capacity()) {
            int offset = this.getVerticesOffset();
            int newSize = Math.max(this.vertices.capacity() * 2, this.vertices.capacity() + vertexCount * this.primitiveVerticesSize);
            ByteBuffer newVertices = BufferUtils.createByteBuffer((int)newSize);
            MemoryUtil.memCopy((long)MemoryUtil.memAddress0((ByteBuffer)this.vertices), (long)MemoryUtil.memAddress0((ByteBuffer)newVertices), (long)offset);
            this.vertices = newVertices;
            this.verticesPointerStart = MemoryUtil.memAddress0((ByteBuffer)this.vertices);
            this.verticesPointer = this.verticesPointerStart + (long)offset;
        }
        if ((this.indicesCount + indexCount) * 4 >= this.indices.capacity()) {
            int newSize = Math.max(this.indices.capacity() * 2, this.indices.capacity() + indexCount * 4);
            ByteBuffer newIndices = BufferUtils.createByteBuffer((int)newSize);
            MemoryUtil.memCopy((long)MemoryUtil.memAddress0((ByteBuffer)this.indices), (long)MemoryUtil.memAddress0((ByteBuffer)newIndices), (long)((long)this.indicesCount * 4L));
            this.indices = newIndices;
            this.indicesPointer = MemoryUtil.memAddress0((ByteBuffer)this.indices);
        }
    }

    private void allocateBuffers(int vertexCount, int indexCount) {
        this.vertices = BufferUtils.createByteBuffer((int)(this.primitiveVerticesSize * vertexCount));
        this.verticesPointer = this.verticesPointerStart = MemoryUtil.memAddress0((ByteBuffer)this.vertices);
        this.indices = BufferUtils.createByteBuffer((int)(indexCount * 4));
        this.indicesPointer = MemoryUtil.memAddress0((ByteBuffer)this.indices);
    }

    public void end() {
        if (!this.building) {
            throw new IllegalStateException("Mesh.end() called while not building.");
        }
        this.building = false;
    }

    public boolean isBuilding() {
        return this.building;
    }

    public GpuBuffer getVertexBuffer() {
        this.vertices.limit(this.getVerticesOffset());
        return this.format.uploadImmediateVertexBuffer(this.vertices);
    }

    public GpuBuffer getIndexBuffer() {
        this.indices.limit(this.indicesCount * 4);
        return this.format.uploadImmediateIndexBuffer(this.indices);
    }

    public int getIndicesCount() {
        return this.indicesCount;
    }

    private int getVerticesOffset() {
        return (int)(this.verticesPointer - this.verticesPointerStart);
    }

    private void debugVertexBufferCapacity() {
        if (DEBUG && (this.vertices == null || this.vertexI * this.primitiveVerticesSize >= this.vertices.capacity())) {
            throw new IndexOutOfBoundsException("Vertices written to MeshBuilder without calling 'ensureCapacity()' first!");
        }
    }

    private void debugIndexBufferCapacity() {
        if (DEBUG && (this.indices == null || this.indicesCount * 4 >= this.indices.capacity())) {
            throw new IndexOutOfBoundsException("Indices written to MeshBuilder without calling 'ensureCapacity()' first!");
        }
    }
}
