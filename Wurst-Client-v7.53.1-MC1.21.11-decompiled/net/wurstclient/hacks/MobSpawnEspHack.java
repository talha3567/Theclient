package net.wurstclient.hacks;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.awt.Color;
import java.util.Map;
import net.minecraft.class_1299;
import net.minecraft.class_1317;
import net.minecraft.class_1921;
import net.minecraft.class_1923;
import net.minecraft.class_1944;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_290;
import net.minecraft.class_4538;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.WurstRenderLayers;
import net.wurstclient.events.PacketInputListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.hacks.mobspawnesp.HitboxCheckSetting;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ChunkAreaSetting;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.EasyVertexBuffer;
import net.wurstclient.util.RegionPos;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.chunk.ChunkSearcher;
import net.wurstclient.util.chunk.ChunkVertexBufferCoordinator;

@SearchTags(value={"mob spawn esp", "LightLevelESP", "light level esp", "LightLevelOverlay", "light level overlay"})
public final class MobSpawnEspHack
extends Hack
implements UpdateListener,
RenderListener {
    private final ChunkAreaSetting drawDistance = new ChunkAreaSetting("Draw distance", "", ChunkAreaSetting.ChunkArea.A9);
    private final ColorSetting nightColor = new ColorSetting("Night color", "description.wurst.setting.mobspawnesp.night_color", Color.YELLOW);
    private final ColorSetting dayColor = new ColorSetting("Day color", "description.wurst.setting.mobspawnesp.day_color", Color.RED);
    private final SliderSetting opacity = new SliderSetting("Opacity", 0.5, 0.0, 1.0, 0.01, SliderSetting.ValueDisplay.PERCENTAGE);
    private final CheckboxSetting depthTest = new CheckboxSetting("Depth test", true);
    private final HitboxCheckSetting hitboxCheck = new HitboxCheckSetting();
    private final ChunkVertexBufferCoordinator coordinator = new ChunkVertexBufferCoordinator(this::isSpawnable, VertexFormat.class_5596.field_27377, class_290.field_63455, this::buildBuffer, this.drawDistance);
    private int cachedDayColor;
    private int cachedNightColor;

    public MobSpawnEspHack() {
        super("MobSpawnESP");
        this.setCategory(Category.RENDER);
        this.addSetting(this.drawDistance);
        this.addSetting(this.nightColor);
        this.addSetting(this.dayColor);
        this.addSetting(this.opacity);
        this.addSetting(this.depthTest);
        this.addSetting(this.hitboxCheck);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(PacketInputListener.class, this.coordinator);
        EVENTS.add(RenderListener.class, this);
        this.cachedDayColor = this.dayColor.getColorI();
        this.cachedNightColor = this.nightColor.getColorI();
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(PacketInputListener.class, this.coordinator);
        EVENTS.remove(RenderListener.class, this);
        this.coordinator.reset();
    }

    @Override
    public void onUpdate() {
        if (this.dayColor.getColorI() != this.cachedDayColor || this.nightColor.getColorI() != this.cachedNightColor) {
            this.cachedDayColor = this.dayColor.getColorI();
            this.cachedNightColor = this.nightColor.getColorI();
            this.coordinator.reset();
        }
        this.coordinator.update();
    }

    @Override
    public void onRender(class_4587 matrixStack, float partialTicks) {
        class_1921 layer = WurstRenderLayers.getLines(this.depthTest.isChecked());
        for (Map.Entry<class_1923, EasyVertexBuffer> entry : this.coordinator.getBuffers()) {
            RegionPos region = RegionPos.of(entry.getKey());
            matrixStack.method_22903();
            RenderUtils.applyRegionalRenderOffset(matrixStack, region);
            entry.getValue().draw(matrixStack, layer, 1.0f, 1.0f, 1.0f, this.opacity.getValueF());
            matrixStack.method_22909();
        }
    }

    private boolean isSpawnable(class_2338 pos, class_2680 state) {
        if (!class_1317.method_56558((class_1299)class_1299.field_6046, (class_4538)MobSpawnEspHack.MC.field_1687, (class_2338)pos)) {
            return false;
        }
        if (!this.hitboxCheck.isSpaceEmpty(pos)) {
            return false;
        }
        return MobSpawnEspHack.MC.field_1687.method_8314(class_1944.field_9282, pos) < 1;
    }

    private void buildBuffer(class_4588 buffer, ChunkSearcher searcher, Iterable<ChunkSearcher.Result> results) {
        RegionPos region = RegionPos.of(searcher.getPos());
        for (ChunkSearcher.Result result : results) {
            if (searcher.isInterrupted()) {
                return;
            }
            this.drawCross(buffer, result.pos(), region);
        }
    }

    private void drawCross(class_4588 buffer, class_2338 pos, RegionPos region) {
        float x1 = pos.method_10263() - region.x();
        float x2 = x1 + 1.0f;
        float y = (float)pos.method_10264() + 0.01f;
        float z1 = pos.method_10260() - region.z();
        float z2 = z1 + 1.0f;
        int color = MobSpawnEspHack.MC.field_1687.method_8314(class_1944.field_9284, pos) < 8 ? this.cachedDayColor : this.cachedNightColor;
        RenderUtils.drawLine(buffer, x1, y, z1, x2, y, z2, color);
        RenderUtils.drawLine(buffer, x2, y, z1, x1, y, z2, color);
    }
}
