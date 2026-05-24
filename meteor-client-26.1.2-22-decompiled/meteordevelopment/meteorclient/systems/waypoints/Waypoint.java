package meteordevelopment.meteorclient.systems.waypoints;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.settings.BlockPosSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ProvidedStringSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.waypoints.Waypoints;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.Dimension;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class Waypoint
implements ISerializable<Waypoint> {
    public final Settings settings = new Settings();
    private final SettingGroup sgVisual = this.settings.createGroup("Visual");
    private final SettingGroup sgPosition = this.settings.createGroup("Position");
    public Setting<String> name = this.sgVisual.add(((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("name")).description("The name of the waypoint.")).defaultValue("Home")).build());
    public Setting<String> icon = this.sgVisual.add(((ProvidedStringSetting.Builder)((ProvidedStringSetting.Builder)((ProvidedStringSetting.Builder)((ProvidedStringSetting.Builder)new ProvidedStringSetting.Builder().name("icon")).description("The icon of the waypoint.")).defaultValue("Square")).supplier(() -> Waypoints.BUILTIN_ICONS).onChanged(string -> this.validateIcon())).build());
    public Setting<SettingColor> color;
    public Setting<Boolean> visible;
    public Setting<Integer> maxVisible;
    public Setting<Double> scale;
    public Setting<BlockPos> pos;
    public Setting<Dimension> dimension;
    public Setting<Boolean> opposite;
    public Setting<NearAction> actionWhenNear;
    public Setting<Integer> actionWhenNearDistance;
    public final UUID uuid;
    public final long createdAt;
    final int waypointActionCooldown = 1000;

    private Waypoint() {
        this.color = this.sgVisual.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("color")).description("The color of the waypoint.")).defaultValue(MeteorClient.ADDON.color.toSetting()).build());
        this.visible = this.sgVisual.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("visible")).description("Whether to show the waypoint.")).defaultValue(true)).build());
        this.maxVisible = this.sgVisual.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("max-visible-distance")).description("How far away to render the waypoint.")).defaultValue(5000)).build());
        this.scale = this.sgVisual.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("The scale of the waypoint.")).defaultValue(1.5).build());
        this.pos = this.sgPosition.add(((BlockPosSetting.Builder)((BlockPosSetting.Builder)((BlockPosSetting.Builder)new BlockPosSetting.Builder().name("location")).description("The location of the waypoint.")).defaultValue(BlockPos.ZERO)).build());
        this.dimension = this.sgPosition.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("dimension")).description("Which dimension the waypoint is in.")).defaultValue(Dimension.Overworld)).build());
        this.opposite = this.sgPosition.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("opposite-dimension")).description("Whether to show the waypoint in the opposite dimension.")).defaultValue(true)).visible(() -> this.dimension.get() != Dimension.End)).build());
        this.actionWhenNear = this.sgPosition.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("action-when-near")).description("Action to be performed when the player is near.")).defaultValue(NearAction.Disabled)).build());
        this.actionWhenNearDistance = this.sgPosition.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("action-when-near-distance")).description("How close (in blocks) the player has to be for the near action to be performed.")).defaultValue(8)).sliderRange(0, 32).visible(() -> this.actionWhenNear.get() != NearAction.Disabled)).build());
        this.waypointActionCooldown = 1000;
        this.uuid = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
    }

    public Waypoint(Tag tag) {
        this.color = this.sgVisual.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("color")).description("The color of the waypoint.")).defaultValue(MeteorClient.ADDON.color.toSetting()).build());
        this.visible = this.sgVisual.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("visible")).description("Whether to show the waypoint.")).defaultValue(true)).build());
        this.maxVisible = this.sgVisual.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("max-visible-distance")).description("How far away to render the waypoint.")).defaultValue(5000)).build());
        this.scale = this.sgVisual.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("The scale of the waypoint.")).defaultValue(1.5).build());
        this.pos = this.sgPosition.add(((BlockPosSetting.Builder)((BlockPosSetting.Builder)((BlockPosSetting.Builder)new BlockPosSetting.Builder().name("location")).description("The location of the waypoint.")).defaultValue(BlockPos.ZERO)).build());
        this.dimension = this.sgPosition.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("dimension")).description("Which dimension the waypoint is in.")).defaultValue(Dimension.Overworld)).build());
        this.opposite = this.sgPosition.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("opposite-dimension")).description("Whether to show the waypoint in the opposite dimension.")).defaultValue(true)).visible(() -> this.dimension.get() != Dimension.End)).build());
        this.actionWhenNear = this.sgPosition.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("action-when-near")).description("Action to be performed when the player is near.")).defaultValue(NearAction.Disabled)).build());
        this.actionWhenNearDistance = this.sgPosition.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("action-when-near-distance")).description("How close (in blocks) the player has to be for the near action to be performed.")).defaultValue(8)).sliderRange(0, 32).visible(() -> this.actionWhenNear.get() != NearAction.Disabled)).build());
        this.waypointActionCooldown = 1000;
        CompoundTag nbt = (CompoundTag)tag;
        this.uuid = nbt.read("uuid", UUIDUtil.CODEC).orElse(UUID.randomUUID());
        this.createdAt = System.currentTimeMillis();
        this.fromTag(nbt);
    }

    public void renderIcon(double x, double y, double a, double size) {
        AbstractTexture texture = Waypoints.get().icons.get(this.icon.get());
        if (texture == null) {
            return;
        }
        int preA = this.color.get().a;
        this.color.get().a = (int)((double)this.color.get().a * a);
        Renderer2D.TEXTURE.begin();
        Renderer2D.TEXTURE.texQuad(x, y, size, size, this.color.get());
        Renderer2D.TEXTURE.render(texture.getTextureView(), texture.getSampler());
        this.color.get().a = preA;
    }

    public BlockPos getPos() {
        Dimension dim = this.dimension.get();
        BlockPos pos = this.pos.get();
        Dimension currentDim = PlayerUtils.getDimension();
        if (dim == currentDim || dim.equals((Object)Dimension.End)) {
            return this.pos.get();
        }
        return switch (dim) {
            case Dimension.Overworld -> new BlockPos(pos.getX() / 8, pos.getY(), pos.getZ() / 8);
            case Dimension.Nether -> new BlockPos(pos.getX() * 8, pos.getY(), pos.getZ() * 8);
            default -> null;
        };
    }

    public boolean actionWhenNearCheck(int distance) {
        if (System.currentTimeMillis() - this.createdAt < 1000L) {
            return false;
        }
        return this.actionWhenNearDistance.get() >= distance;
    }

    private void validateIcon() {
        Map<String, AbstractTexture> icons = Waypoints.get().icons;
        AbstractTexture texture = icons.get(this.icon.get());
        if (texture == null && !icons.isEmpty()) {
            this.icon.set(icons.keySet().iterator().next());
        }
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.store("uuid", UUIDUtil.CODEC, (Object)this.uuid);
        tag.put("settings", (Tag)this.settings.toTag());
        return tag;
    }

    @Override
    public Waypoint fromTag(CompoundTag tag) {
        if (tag.contains("settings")) {
            this.settings.fromTag(tag.getCompoundOrEmpty("settings"));
        }
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Waypoint waypoint = (Waypoint)o;
        return Objects.equals(this.uuid, waypoint.uuid);
    }

    public int hashCode() {
        return Objects.hashCode(this.uuid);
    }

    public String toString() {
        return this.name.get();
    }

    public static enum NearAction {
        Disabled,
        Hide,
        Delete;

    }

    public static class Builder {
        private String name = "";
        private String icon = "";
        private BlockPos pos = BlockPos.ZERO;
        private Dimension dimension = Dimension.Overworld;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public Builder pos(BlockPos pos) {
            this.pos = pos;
            return this;
        }

        public Builder dimension(Dimension dimension) {
            this.dimension = dimension;
            return this;
        }

        public Waypoint build() {
            Waypoint waypoint = new Waypoint();
            if (!this.name.equals(waypoint.name.getDefaultValue())) {
                waypoint.name.set(this.name);
            }
            if (!this.icon.equals(waypoint.icon.getDefaultValue())) {
                waypoint.icon.set(this.icon);
            }
            if (!this.pos.equals((Object)waypoint.pos.getDefaultValue())) {
                waypoint.pos.set(this.pos);
            }
            if (!this.dimension.equals((Object)waypoint.dimension.getDefaultValue())) {
                waypoint.dimension.set(this.dimension);
            }
            return waypoint;
        }
    }
}
