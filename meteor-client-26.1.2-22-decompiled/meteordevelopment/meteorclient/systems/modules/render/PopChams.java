package meteordevelopment.meteorclient.systems.modules.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.meteorclient.utils.render.WireframeEntityRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class PopChams
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> onlyOne;
    private final Setting<Double> renderTime;
    private final Setting<Double> yModifier;
    private final Setting<Double> scaleModifier;
    private final Setting<Boolean> fadeOut;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final List<GhostPlayer> ghosts;

    public PopChams() {
        super(Categories.Render, "pop-chams", "Renders a ghost where players pop totem.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.onlyOne = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-one")).description("Only allow one ghost per player.")).defaultValue(false)).build());
        this.renderTime = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("render-time")).description("How long the ghost is rendered in seconds.")).defaultValue(1.0).min(0.1).sliderMax(6.0).build());
        this.yModifier = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("y-modifier")).description("How much should the Y position of the ghost change per second.")).defaultValue(0.75).sliderRange(-4.0, 4.0).build());
        this.scaleModifier = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale-modifier")).description("How much should the scale of the ghost change per second.")).defaultValue(-0.25).sliderRange(-4.0, 4.0).build());
        this.fadeOut = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("fade-out")).description("Fades out the color.")).defaultValue(true)).build());
        this.shapeMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.sideColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The side color.")).defaultValue(new SettingColor(255, 255, 255, 25)).build());
        this.lineColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The line color.")).defaultValue(new SettingColor(255, 255, 255, 127)).build());
        this.ghosts = new ArrayList<GhostPlayer>();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void onDeactivate() {
        List<GhostPlayer> list = this.ghosts;
        synchronized (list) {
            this.ghosts.clear();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        Player player;
        Entity entity;
        block10: {
            block9: {
                Packet<?> packet = event.packet;
                if (!(packet instanceof ClientboundEntityEventPacket)) {
                    return;
                }
                ClientboundEntityEventPacket p = (ClientboundEntityEventPacket)packet;
                if (p.getEventId() != 35) {
                    return;
                }
                entity = p.getEntity((Level)this.mc.level);
                if (!(entity instanceof Player)) break block9;
                player = (Player)entity;
                if (entity != this.mc.player) break block10;
            }
            return;
        }
        List<GhostPlayer> list = this.ghosts;
        synchronized (list) {
            if (this.onlyOne.get().booleanValue()) {
                this.ghosts.removeIf(ghostPlayer -> ghostPlayer.uuid.equals(entity.getUUID()));
            }
            this.ghosts.add(new GhostPlayer(this, player));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventHandler
    private void onRender3D(Render3DEvent event) {
        List<GhostPlayer> list = this.ghosts;
        synchronized (list) {
            this.ghosts.removeIf(ghostPlayer -> ghostPlayer.render(event));
        }
    }

    private class GhostPlayer
    extends FakePlayerEntity {
        private final UUID uuid;
        private double timer;
        private double scale;
        final /* synthetic */ PopChams this$0;

        public GhostPlayer(PopChams popChams, Player player) {
            PopChams popChams2 = popChams;
            Objects.requireNonNull(popChams2);
            this.this$0 = popChams2;
            super(player, "ghost", 20.0f, false);
            this.scale = 1.0;
            this.uuid = player.getUUID();
        }

        public boolean render(Render3DEvent event) {
            this.timer += event.frameTime;
            if (this.timer > this.this$0.renderTime.get()) {
                return true;
            }
            this.yOld = this.getY();
            ((IVec3)this.position()).meteor$setY(this.getY() + this.this$0.yModifier.get() * event.frameTime);
            this.scale += this.this$0.scaleModifier.get() * event.frameTime;
            int preSideA = this.this$0.sideColor.get().a;
            int preLineA = this.this$0.lineColor.get().a;
            if (this.this$0.fadeOut.get().booleanValue()) {
                this.this$0.sideColor.get().a = (int)((double)this.this$0.sideColor.get().a * (1.0 - this.timer / this.this$0.renderTime.get()));
                this.this$0.lineColor.get().a = (int)((double)this.this$0.lineColor.get().a * (1.0 - this.timer / this.this$0.renderTime.get()));
            }
            WireframeEntityRenderer.render(event, (Entity)this, this.scale, this.this$0.sideColor.get(), this.this$0.lineColor.get(), this.this$0.shapeMode.get());
            this.this$0.sideColor.get().a = preSideA;
            this.this$0.lineColor.get().a = preLineA;
            return false;
        }
    }
}
