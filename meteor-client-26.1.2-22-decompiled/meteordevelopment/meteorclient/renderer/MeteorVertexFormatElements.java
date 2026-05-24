package meteordevelopment.meteorclient.renderer;

import com.mojang.blaze3d.vertex.VertexFormatElement;

public abstract class MeteorVertexFormatElements {
    public static final VertexFormatElement POS2 = VertexFormatElement.register((int)MeteorVertexFormatElements.getNextVertexFormatElementId(), (int)0, (VertexFormatElement.Type)VertexFormatElement.Type.FLOAT, (boolean)false, (int)2);

    private MeteorVertexFormatElements() {
    }

    private static int getNextVertexFormatElementId() {
        int id = 0;
        while (VertexFormatElement.byId((int)id) != null) {
            if (++id < 32) continue;
            throw new RuntimeException("Too many mods registering VertexFormatElements");
        }
        return id;
    }
}
