package meteordevelopment.meteorclient.systems.waypoints;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.waypoints.Waypoint;
import meteordevelopment.meteorclient.systems.waypoints.events.WaypointAddedEvent;
import meteordevelopment.meteorclient.systems.waypoints.events.WaypointRemovedEvent;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.files.StreamUtils;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.Dimension;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang3.Strings;
import org.jetbrains.annotations.NotNull;

public class Waypoints
extends System<Waypoints>
implements Iterable<Waypoint> {
    private static final String PNG = ".png";
    public static final String[] BUILTIN_ICONS = new String[]{"square", "circle", "triangle", "star", "diamond", "skull"};
    public final Map<String, AbstractTexture> icons = new ConcurrentHashMap<String, AbstractTexture>();
    private final List<Waypoint> waypoints = new CopyOnWriteArrayList<Waypoint>();

    public Waypoints() {
        super(null);
    }

    public static Waypoints get() {
        return Systems.get(Waypoints.class);
    }

    @Override
    public void init() {
        File iconsFolder = new File(new File(MeteorClient.FOLDER, "waypoints"), "icons");
        iconsFolder.mkdirs();
        for (String builtinIcon : BUILTIN_ICONS) {
            File iconFile = new File(iconsFolder, builtinIcon + PNG);
            if (iconFile.exists()) continue;
            this.copyIcon(iconFile);
        }
        File[] files = iconsFolder.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (!file.getName().endsWith(PNG)) continue;
            try (FileInputStream inputStream = new FileInputStream(file);){
                String name = Strings.CS.removeEnd(file.getName(), (CharSequence)PNG);
                DynamicTexture texture = new DynamicTexture(() -> name, NativeImage.read((InputStream)inputStream));
                this.icons.put(name, (AbstractTexture)texture);
            }
            catch (Exception e) {
                MeteorClient.LOG.error("Failed to read a waypoint icon", e);
            }
        }
    }

    public boolean add(Waypoint waypoint) {
        if (this.waypoints.contains(waypoint)) {
            this.save();
            return true;
        }
        this.waypoints.add(waypoint);
        this.save();
        MeteorClient.EVENT_BUS.post(new WaypointAddedEvent(waypoint));
        return false;
    }

    public boolean remove(Waypoint waypoint) {
        boolean removed = this.waypoints.remove(waypoint);
        if (removed) {
            this.save();
            MeteorClient.EVENT_BUS.post(new WaypointRemovedEvent(waypoint));
        }
        return removed;
    }

    public void removeAll(Collection<Waypoint> c) {
        boolean removed = this.waypoints.removeAll(c);
        if (removed) {
            this.save();
        }
    }

    public Waypoint get(String name) {
        for (Waypoint waypoint : this.waypoints) {
            if (!waypoint.name.get().equalsIgnoreCase(name)) continue;
            return waypoint;
        }
        return null;
    }

    @EventHandler
    private void onGameJoined(GameJoinedEvent event) {
        this.load();
    }

    @EventHandler(priority=-200)
    private void onGameDisconnected(GameLeftEvent event) {
        this.waypoints.clear();
    }

    public static boolean checkDimension(Waypoint waypoint) {
        Dimension waypointDim;
        Dimension playerDim = PlayerUtils.getDimension();
        if (playerDim == (waypointDim = waypoint.dimension.get())) {
            return true;
        }
        if (!waypoint.opposite.get().booleanValue()) {
            return false;
        }
        boolean playerOpp = playerDim == Dimension.Overworld || playerDim == Dimension.Nether;
        boolean waypointOpp = waypointDim == Dimension.Overworld || waypointDim == Dimension.Nether;
        return playerOpp && waypointOpp;
    }

    @Override
    public File getFile() {
        if (!Utils.canUpdate()) {
            return null;
        }
        return new File(new File(MeteorClient.FOLDER, "waypoints"), Utils.getFileWorldName() + ".nbt");
    }

    public boolean isEmpty() {
        return this.waypoints.isEmpty();
    }

    @Override
    @NotNull
    public Iterator<Waypoint> iterator() {
        return new WaypointIterator(this);
    }

    private void copyIcon(File file) {
        String path = "/assets/meteor-client/textures/icons/waypoints/" + file.getName();
        InputStream in = Waypoints.class.getResourceAsStream(path);
        if (in == null) {
            MeteorClient.LOG.error("Failed to read a resource: {}", (Object)path);
            return;
        }
        StreamUtils.copy(in, file);
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.put("waypoints", (Tag)NbtUtils.listToTag(this.waypoints));
        return tag;
    }

    @Override
    public Waypoints fromTag(CompoundTag tag) {
        this.waypoints.clear();
        for (Tag waypointTag : tag.getListOrEmpty("waypoints")) {
            this.waypoints.add(new Waypoint(waypointTag));
        }
        return this;
    }

    private final class WaypointIterator
    implements Iterator<Waypoint> {
        private final Iterator<Waypoint> it;
        final /* synthetic */ Waypoints this$0;

        private WaypointIterator(Waypoints waypoints) {
            Waypoints waypoints2 = waypoints;
            Objects.requireNonNull(waypoints2);
            this.this$0 = waypoints2;
            this.it = this.this$0.waypoints.iterator();
        }

        @Override
        public boolean hasNext() {
            return this.it.hasNext();
        }

        @Override
        public Waypoint next() {
            return this.it.next();
        }

        @Override
        public void remove() {
            this.it.remove();
            this.this$0.save();
        }
    }
}
