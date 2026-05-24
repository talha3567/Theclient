package meteordevelopment.meteorclient.systems.modules.misc;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.lang.runtime.SwitchBootstraps;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.entity.EntityRemovedEvent;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ArrayListDeque;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Notifier
extends Module {
    private final SettingGroup sgTotemPops;
    private final SettingGroup sgVisualRange;
    private final SettingGroup sgPearl;
    private final SettingGroup sgJoinsLeaves;
    private final Setting<Boolean> totemPops;
    private final Setting<Boolean> totemsDistanceCheck;
    private final Setting<Integer> totemsDistance;
    private final Setting<Boolean> totemsIgnoreOwn;
    private final Setting<Boolean> totemsIgnoreFriends;
    private final Setting<Boolean> totemsIgnoreOthers;
    private final Setting<Boolean> visualRange;
    private final Setting<Event> event;
    private final Setting<Set<EntityType<?>>> entities;
    private final Setting<Boolean> visualRangeIgnoreFriends;
    private final Setting<Boolean> visualRangeIgnoreFakes;
    private final Setting<Boolean> visualMakeSound;
    private final Setting<Boolean> pearl;
    private final Setting<Boolean> pearlIgnoreOwn;
    private final Setting<Boolean> pearlIgnoreFriends;
    private final Setting<JoinLeaveModes> joinsLeavesMode;
    private final Setting<Integer> notificationDelay;
    private final Setting<Boolean> simpleNotifications;
    private int timer;
    private boolean loginPacket;
    private final Object2IntMap<UUID> totemPopMap;
    private final Object2IntMap<UUID> chatIdMap;
    private final Map<Integer, Vec3> pearlStartPosMap;
    private final ArrayListDeque<MutableComponent> messageQueue;
    private final Random random;

    public Notifier() {
        super(Categories.Misc, "notifier", "Notifies you of different events.");
        this.sgTotemPops = this.settings.createGroup("Totem Pops");
        this.sgVisualRange = this.settings.createGroup("Visual Range");
        this.sgPearl = this.settings.createGroup("Pearl");
        this.sgJoinsLeaves = this.settings.createGroup("Joins/Leaves");
        this.totemPops = this.sgTotemPops.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("totem-pops")).description("Notifies you when a player pops a totem.")).defaultValue(true)).build());
        this.totemsDistanceCheck = this.sgTotemPops.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("distance-check")).description("Limits the distance in which the pops are recognized.")).defaultValue(false)).visible(this.totemPops::get)).build());
        this.totemsDistance = this.sgTotemPops.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("player-radius")).description("The radius in which to log totem pops.")).defaultValue(30)).sliderRange(1, 50).range(1, 100).visible(() -> this.totemPops.get() != false && this.totemsDistanceCheck.get() != false)).build());
        this.totemsIgnoreOwn = this.sgTotemPops.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-own")).description("Ignores your own totem pops.")).defaultValue(false)).build());
        this.totemsIgnoreFriends = this.sgTotemPops.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-friends")).description("Ignores friends totem pops.")).defaultValue(false)).build());
        this.totemsIgnoreOthers = this.sgTotemPops.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-others")).description("Ignores other players totem pops.")).defaultValue(false)).build());
        this.visualRange = this.sgVisualRange.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("visual-range")).description("Notifies you when an entity enters your render distance.")).defaultValue(false)).build());
        this.event = this.sgVisualRange.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("event")).description("When to log the entities.")).defaultValue(Event.Both)).build());
        this.entities = this.sgVisualRange.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entities")).description("Which entities to notify about.")).defaultValue(EntityType.PLAYER).build());
        this.visualRangeIgnoreFriends = this.sgVisualRange.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-friends")).description("Ignores friends.")).defaultValue(true)).build());
        this.visualRangeIgnoreFakes = this.sgVisualRange.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-fake-players")).description("Ignores fake players.")).defaultValue(true)).build());
        this.visualMakeSound = this.sgVisualRange.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sound")).description("Emits a sound effect on enter / leave")).defaultValue(true)).build());
        this.pearl = this.sgPearl.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pearl")).description("Notifies you when a player is teleported using an ender pearl.")).defaultValue(true)).build());
        this.pearlIgnoreOwn = this.sgPearl.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-own")).description("Ignores your own pearls.")).defaultValue(false)).build());
        this.pearlIgnoreFriends = this.sgPearl.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-friends")).description("Ignores friends pearls.")).defaultValue(false)).build());
        this.joinsLeavesMode = this.sgJoinsLeaves.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("player-joins-leaves")).description("How to handle player join/leave notifications.")).defaultValue(JoinLeaveModes.None)).build());
        this.notificationDelay = this.sgJoinsLeaves.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("notification-delay")).description("How long to wait in ticks before posting the next join/leave notification in your chat.")).range(0, 1000).sliderRange(0, 100).defaultValue(0)).build());
        this.simpleNotifications = this.sgJoinsLeaves.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("simple-notifications")).description("Display join/leave notifications without a prefix, to reduce chat clutter.")).defaultValue(true)).build());
        this.loginPacket = true;
        this.totemPopMap = new Object2IntOpenHashMap();
        this.chatIdMap = new Object2IntOpenHashMap();
        this.pearlStartPosMap = new HashMap<Integer, Vec3>();
        this.messageQueue = new ArrayListDeque();
        this.random = new Random();
    }

    @EventHandler
    private void onEntityAdded(EntityAddedEvent event) {
        Entity entity;
        if (!event.entity.getUUID().equals(this.mc.player.getUUID()) && this.entities.get().contains(event.entity.getType()) && this.visualRange.get().booleanValue() && this.event.get() != Event.Despawn) {
            entity = event.entity;
            if (entity instanceof Player) {
                Player player = (Player)entity;
                if (!(this.visualRangeIgnoreFriends.get().booleanValue() && Friends.get().isFriend(player) || this.visualRangeIgnoreFakes.get().booleanValue() && event.entity instanceof FakePlayerEntity)) {
                    ChatUtils.sendMsg(event.entity.getId() + 100, ChatFormatting.GRAY, "(highlight)%s(default) has entered your visual range!", event.entity.getName().getString());
                    if (this.visualMakeSound.get().booleanValue()) {
                        this.mc.level.playSound((Entity)this.mc.player, (Entity)this.mc.player, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.AMBIENT, 3.0f, 1.0f);
                    }
                }
            } else {
                MutableComponent text = Component.literal((String)event.entity.getType().getDescription().getString()).withStyle(ChatFormatting.WHITE);
                text.append((Component)Component.literal((String)" has spawned at ").withStyle(ChatFormatting.GRAY));
                text.append((Component)ChatUtils.formatCoords(event.entity.position()));
                text.append((Component)Component.literal((String)".").withStyle(ChatFormatting.GRAY));
                this.info((Component)text);
            }
        }
        if (this.pearl.get().booleanValue() && (entity = event.entity) instanceof ThrownEnderpearl) {
            ThrownEnderpearl pearlEntity = (ThrownEnderpearl)entity;
            this.pearlStartPosMap.put(pearlEntity.getId(), new Vec3(pearlEntity.getX(), pearlEntity.getY(), pearlEntity.getZ()));
        }
    }

    @EventHandler
    private void onEntityRemoved(EntityRemovedEvent event) {
        Entity e;
        int i;
        if (!event.entity.getUUID().equals(this.mc.player.getUUID()) && this.entities.get().contains(event.entity.getType()) && this.visualRange.get().booleanValue() && this.event.get() != Event.Spawn) {
            Entity entity = event.entity;
            if (entity instanceof Player) {
                Player player = (Player)entity;
                if (!(this.visualRangeIgnoreFriends.get().booleanValue() && Friends.get().isFriend(player) || this.visualRangeIgnoreFakes.get().booleanValue() && event.entity instanceof FakePlayerEntity)) {
                    ChatUtils.sendMsg(event.entity.getId() + 100, ChatFormatting.GRAY, "(highlight)%s(default) has left your visual range!", event.entity.getName().getString());
                    if (this.visualMakeSound.get().booleanValue()) {
                        this.mc.level.playSound((Entity)this.mc.player, (Entity)this.mc.player, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.AMBIENT, 3.0f, 1.0f);
                    }
                }
            } else {
                MutableComponent text = Component.literal((String)event.entity.getType().getDescription().getString()).withStyle(ChatFormatting.WHITE);
                text.append((Component)Component.literal((String)" has despawned at ").withStyle(ChatFormatting.GRAY));
                text.append((Component)ChatUtils.formatCoords(event.entity.position()));
                text.append((Component)Component.literal((String)".").withStyle(ChatFormatting.GRAY));
                this.info((Component)text);
            }
        }
        if (this.pearl.get().booleanValue() && this.pearlStartPosMap.containsKey(i = (e = event.entity).getId())) {
            Entity entity;
            ThrownEnderpearl pearl = (ThrownEnderpearl)e;
            if (pearl.getOwner() != null && (entity = pearl.getOwner()) instanceof Player) {
                Player p = (Player)entity;
                double d = this.pearlStartPosMap.get(i).distanceTo(e.position());
                if (!(Friends.get().isFriend(p) && this.pearlIgnoreFriends.get().booleanValue() || p.equals((Object)this.mc.player) && this.pearlIgnoreOwn.get().booleanValue())) {
                    this.info("(highlight)%s's(default) pearl landed at %d, %d, %d (highlight)(%.1fm away, travelled %.1fm)(default).", pearl.getOwner().getName().getString(), pearl.blockPosition().getX(), pearl.blockPosition().getY(), pearl.blockPosition().getZ(), Float.valueOf(pearl.distanceTo((Entity)this.mc.player)), d);
                }
            }
            this.pearlStartPosMap.remove(i);
        }
    }

    @Override
    public void onActivate() {
        this.totemPopMap.clear();
        this.chatIdMap.clear();
        this.pearlStartPosMap.clear();
    }

    @Override
    public void onDeactivate() {
        this.timer = 0;
        this.messageQueue.clear();
    }

    @EventHandler
    private void onGameJoin(GameJoinedEvent event) {
        this.timer = 0;
        this.totemPopMap.clear();
        this.chatIdMap.clear();
        this.messageQueue.clear();
        this.pearlStartPosMap.clear();
    }

    @EventHandler
    private void onGameLeave(GameLeftEvent event) {
        this.loginPacket = true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        Packet<?> packet = event.packet;
        Objects.requireNonNull(packet);
        Packet<?> packet2 = packet;
        int n = 0;
        block8: while (true) {
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ClientboundPlayerInfoUpdatePacket.class, ClientboundPlayerInfoRemovePacket.class, ClientboundEntityEventPacket.class}, packet2, n)) {
                case 0: {
                    ClientboundPlayerInfoUpdatePacket packet3 = (ClientboundPlayerInfoUpdatePacket)packet2;
                    if (!this.joinsLeavesMode.get().equals((Object)JoinLeaveModes.Both) && !this.joinsLeavesMode.get().equals((Object)JoinLeaveModes.Joins)) {
                        n = 1;
                        continue block8;
                    }
                    if (this.loginPacket) {
                        this.loginPacket = false;
                        return;
                    }
                    if (!packet3.actions().contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER)) break block8;
                    this.createJoinNotifications(packet3);
                    break block8;
                }
                case 1: {
                    ClientboundPlayerInfoRemovePacket packet4 = (ClientboundPlayerInfoRemovePacket)packet2;
                    if (!this.joinsLeavesMode.get().equals((Object)JoinLeaveModes.Both) && !this.joinsLeavesMode.get().equals((Object)JoinLeaveModes.Leaves)) {
                        n = 2;
                        continue block8;
                    }
                    this.createLeaveNotification(packet4);
                    break block8;
                }
                case 2: {
                    Object2IntMap<UUID> object2IntMap;
                    ClientboundEntityEventPacket packet5 = (ClientboundEntityEventPacket)packet2;
                    if (!this.totemPops.get().booleanValue() || packet5.getEventId() != 35 || !((object2IntMap = packet5.getEntity((Level)this.mc.level)) instanceof Player)) {
                        n = 3;
                        continue block8;
                    }
                    Player entity = (Player)object2IntMap;
                    if (entity.equals((Object)this.mc.player) && this.totemsIgnoreOwn.get() != false || Friends.get().isFriend(entity) && this.totemsIgnoreOthers.get() != false || !Friends.get().isFriend(entity) && this.totemsIgnoreFriends.get().booleanValue()) {
                        return;
                    }
                    object2IntMap = this.totemPopMap;
                    synchronized (object2IntMap) {
                        int pops = this.totemPopMap.getOrDefault((Object)entity.getUUID(), 0);
                        this.totemPopMap.put((Object)entity.getUUID(), ++pops);
                        double distance = PlayerUtils.distanceTo((Entity)entity);
                        if (this.totemsDistanceCheck.get().booleanValue() && distance > (double)this.totemsDistance.get().intValue()) {
                            return;
                        }
                        ChatUtils.sendMsg(this.getChatId((Entity)entity), ChatFormatting.GRAY, "(highlight)%s (default)popped (highlight)%d (default)%s.", entity.getName().getString(), pops, pops == 1 ? "totem" : "totems");
                        break block8;
                    }
                }
            }
            break;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.joinsLeavesMode.get() != JoinLeaveModes.None) {
            ++this.timer;
            while (this.timer >= this.notificationDelay.get() && !this.messageQueue.isEmpty()) {
                this.timer = 0;
                if (this.simpleNotifications.get().booleanValue()) {
                    this.mc.player.sendSystemMessage((Component)this.messageQueue.removeFirst());
                    continue;
                }
                ChatUtils.sendMsg((Component)this.messageQueue.removeFirst());
            }
        }
        if (!this.totemPops.get().booleanValue()) {
            return;
        }
        Object2IntMap<UUID> object2IntMap = this.totemPopMap;
        synchronized (object2IntMap) {
            for (Player player : this.mc.level.players()) {
                if (!this.totemPopMap.containsKey((Object)player.getUUID()) || player.deathTime <= 0 && !(player.getHealth() <= 0.0f)) continue;
                int pops = this.totemPopMap.removeInt((Object)player.getUUID());
                ChatUtils.sendMsg(this.getChatId((Entity)player), ChatFormatting.GRAY, "(highlight)%s (default)died after popping (highlight)%d (default)%s.", player.getName().getString(), pops, pops == 1 ? "totem" : "totems");
                this.chatIdMap.removeInt((Object)player.getUUID());
            }
        }
    }

    private int getChatId(Entity entity) {
        return this.chatIdMap.computeIfAbsent((Object)entity.getUUID(), object -> this.random.nextInt());
    }

    private void createJoinNotifications(ClientboundPlayerInfoUpdatePacket packet) {
        for (ClientboundPlayerInfoUpdatePacket.Entry entry : packet.newEntries()) {
            if (entry.profile() == null) continue;
            if (this.simpleNotifications.get().booleanValue()) {
                this.messageQueue.addLast((Object)Component.literal((String)(String.valueOf(ChatFormatting.GRAY) + "[" + String.valueOf(ChatFormatting.GREEN) + "+" + String.valueOf(ChatFormatting.GRAY) + "] " + entry.profile().name())));
                continue;
            }
            this.messageQueue.addLast((Object)Component.literal((String)(String.valueOf(ChatFormatting.WHITE) + entry.profile().name() + String.valueOf(ChatFormatting.GRAY) + " joined.")));
        }
    }

    private void createLeaveNotification(ClientboundPlayerInfoRemovePacket packet) {
        if (this.mc.getConnection() == null) {
            return;
        }
        for (UUID id : packet.profileIds()) {
            PlayerInfo toRemove = this.mc.getConnection().getPlayerInfo(id);
            if (toRemove == null) continue;
            if (this.simpleNotifications.get().booleanValue()) {
                this.messageQueue.addLast((Object)Component.literal((String)(String.valueOf(ChatFormatting.GRAY) + "[" + String.valueOf(ChatFormatting.RED) + "-" + String.valueOf(ChatFormatting.GRAY) + "] " + toRemove.getProfile().name())));
                continue;
            }
            this.messageQueue.addLast((Object)Component.literal((String)(String.valueOf(ChatFormatting.WHITE) + toRemove.getProfile().name() + String.valueOf(ChatFormatting.GRAY) + " left.")));
        }
    }

    public static enum Event {
        Spawn,
        Despawn,
        Both;

    }

    public static enum JoinLeaveModes {
        None,
        Joins,
        Leaves,
        Both;

    }
}
