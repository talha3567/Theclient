package meteordevelopment.meteorclient.systems.modules.combat;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Objects;
import java.util.Set;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AutoReconnect;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class AutoLog
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgEntities;
    private final Setting<Integer> health;
    private final Setting<Boolean> smart;
    private final Setting<Integer> totemPops;
    private final Setting<Boolean> onlyTrusted;
    private final Setting<Boolean> instantDeath;
    private final Setting<Boolean> smartToggle;
    private final Setting<Boolean> toggleOff;
    private final Setting<Boolean> toggleAutoReconnect;
    private final Setting<Set<EntityType<?>>> entities;
    private final Setting<Boolean> useTotalCount;
    private final Setting<Integer> combinedEntityThreshold;
    private final Setting<Integer> individualEntityThreshold;
    private final Setting<Integer> range;
    private final Object2IntMap<EntityType<?>> entityCounts;
    private int pops;
    private final StaticListener staticListener;

    public AutoLog() {
        super(Categories.Combat, "auto-log", "Automatically disconnects you when certain requirements are met.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgEntities = this.settings.createGroup("Entities");
        this.health = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("health")).description("Automatically disconnects when health is lower or equal to this value. Set to 0 to disable.")).defaultValue(6)).range(0, 19).sliderMax(19).build());
        this.smart = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("predict-incoming-damage")).description("Disconnects when it detects you're about to take enough damage to set you under the 'health' setting.")).defaultValue(true)).build());
        this.totemPops = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("totem-pops")).description("Disconnects when you have popped this many totems. Set to 0 to disable.")).defaultValue(0)).min(0).build());
        this.onlyTrusted = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-trusted")).description("Disconnects when a player not on your friends list appears in render distance.")).defaultValue(false)).build());
        this.instantDeath = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("32K")).description("Disconnects when a player near you can instantly kill you.")).defaultValue(false)).build());
        this.smartToggle = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("smart-toggle")).description("Disables Auto Log after a low-health logout. WILL re-enable once you heal.")).defaultValue(false)).build());
        this.toggleOff = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("toggle-off")).description("Disables Auto Log after usage.")).defaultValue(true)).build());
        this.toggleAutoReconnect = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("toggle-auto-reconnect")).description("Whether to disable Auto Reconnect after a logout.")).defaultValue(true)).build());
        this.entities = this.sgEntities.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entities")).description("Disconnects when a specified entity is present within a specified range.")).defaultValue(EntityType.END_CRYSTAL).build());
        this.useTotalCount = this.sgEntities.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("use-total-count")).description("Toggle between counting the total number of all selected entities or each entity individually.")).defaultValue(true)).visible(() -> !this.entities.get().isEmpty())).build());
        this.combinedEntityThreshold = this.sgEntities.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("combined-entity-threshold")).description("The minimum total number of selected entities that must be near you before disconnection occurs.")).defaultValue(10)).min(1).sliderMax(32).visible(() -> this.useTotalCount.get() != false && !this.entities.get().isEmpty())).build());
        this.individualEntityThreshold = this.sgEntities.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("individual-entity-threshold")).description("The minimum number of entities individually that must be near you before disconnection occurs.")).defaultValue(2)).min(1).sliderMax(16).visible(() -> this.useTotalCount.get() == false && !this.entities.get().isEmpty())).build());
        this.range = this.sgEntities.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("range")).description("How close an entity has to be to you before you disconnect.")).defaultValue(5)).min(1).sliderMax(16).visible(() -> !this.entities.get().isEmpty())).build());
        this.entityCounts = new Object2IntOpenHashMap();
        this.staticListener = new StaticListener(this);
    }

    @Override
    public void onActivate() {
        this.pops = 0;
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        Packet<?> packet = event.packet;
        if (!(packet instanceof ClientboundEntityEventPacket)) {
            return;
        }
        ClientboundEntityEventPacket p = (ClientboundEntityEventPacket)packet;
        if (p.getEventId() != 35) {
            return;
        }
        Entity entity = p.getEntity((Level)this.mc.level);
        if (entity == null || !entity.equals((Object)this.mc.player)) {
            return;
        }
        ++this.pops;
        if (this.totemPops.get() > 0 && this.pops >= this.totemPops.get()) {
            this.disconnect("Popped " + this.pops + " totems.");
            if (this.toggleOff.get().booleanValue()) {
                this.toggle();
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        float playerHealth = this.mc.player.getHealth();
        if (playerHealth <= 0.0f) {
            this.toggle();
            return;
        }
        if (playerHealth <= (float)this.health.get().intValue()) {
            this.disconnect("Health was lower than " + String.valueOf(this.health.get()) + ".");
            if (this.smartToggle.get().booleanValue()) {
                if (this.isActive()) {
                    this.toggle();
                }
                this.enableHealthListener();
            } else if (this.toggleOff.get().booleanValue()) {
                this.toggle();
            }
            return;
        }
        if (this.smart.get().booleanValue() && playerHealth + this.mc.player.getAbsorptionAmount() - PlayerUtils.possibleHealthReductions() < (float)this.health.get().intValue()) {
            this.disconnect("Health was going to be lower than " + String.valueOf(this.health.get()) + ".");
            if (this.toggleOff.get().booleanValue()) {
                this.toggle();
            }
            return;
        }
        if (!this.onlyTrusted.get().booleanValue() && !this.instantDeath.get().booleanValue() && this.entities.get().isEmpty()) {
            return;
        }
        for (Entity entity : this.mc.level.entitiesForRendering()) {
            Player player;
            if (!(entity instanceof Player) || (player = (Player)entity).getUUID() == this.mc.player.getUUID()) continue;
            if (this.onlyTrusted.get().booleanValue() && player != this.mc.player && !Friends.get().isFriend(player)) {
                this.disconnect((Component)Component.literal((String)("Non-trusted player '" + String.valueOf(ChatFormatting.RED) + player.getName().getString() + String.valueOf(ChatFormatting.WHITE) + "' appeared in your render distance.")));
                if (this.toggleOff.get().booleanValue()) {
                    this.toggle();
                }
                return;
            }
            if (!this.instantDeath.get().booleanValue() || !PlayerUtils.isWithin(entity, 8.0) || !(DamageUtils.getAttackDamage((LivingEntity)player, (Entity)this.mc.player) > playerHealth + this.mc.player.getAbsorptionAmount())) continue;
            this.disconnect("Anti-32k measures.");
            if (this.toggleOff.get().booleanValue()) {
                this.toggle();
            }
            return;
        }
        if (!this.entities.get().isEmpty()) {
            int totalEntities = 0;
            this.entityCounts.clear();
            for (Entity entity : this.mc.level.entitiesForRendering()) {
                if (!PlayerUtils.isWithin(entity, (double)this.range.get().intValue()) || !this.entities.get().contains(entity.getType())) continue;
                ++totalEntities;
                if (this.useTotalCount.get().booleanValue()) continue;
                this.entityCounts.put((Object)entity.getType(), this.entityCounts.getOrDefault((Object)entity.getType(), 0) + 1);
            }
            if (this.useTotalCount.get().booleanValue() && totalEntities >= this.combinedEntityThreshold.get()) {
                this.disconnect("Total number of selected entities within range exceeded the limit.");
                if (this.toggleOff.get().booleanValue()) {
                    this.toggle();
                }
            } else if (!this.useTotalCount.get().booleanValue()) {
                for (Object2IntMap.Entry entry : this.entityCounts.object2IntEntrySet()) {
                    if (entry.getIntValue() < this.individualEntityThreshold.get()) continue;
                    this.disconnect("Number of " + ((EntityType)entry.getKey()).getDescription().getString() + " within range exceeded the limit.");
                    if (this.toggleOff.get().booleanValue()) {
                        this.toggle();
                    }
                    return;
                }
            }
        }
    }

    private void disconnect(String reason) {
        this.disconnect((Component)Component.literal((String)reason));
    }

    private void disconnect(Component reason) {
        MutableComponent text = Component.literal((String)"[AutoLog] ");
        text.append(reason);
        AutoReconnect autoReconnect = Modules.get().get(AutoReconnect.class);
        if (autoReconnect.isActive() && this.toggleAutoReconnect.get().booleanValue()) {
            text.append((Component)Component.literal((String)"\n\nINFO - AutoReconnect was disabled").withColor(-8355712));
            autoReconnect.toggle();
        }
        this.mc.player.connection.handleDisconnect(new ClientboundDisconnectPacket((Component)text));
    }

    private void enableHealthListener() {
        MeteorClient.EVENT_BUS.subscribe(this.staticListener);
    }

    private void disableHealthListener() {
        MeteorClient.EVENT_BUS.unsubscribe(this.staticListener);
    }

    private class StaticListener {
        final /* synthetic */ AutoLog this$0;

        private StaticListener(AutoLog autoLog) {
            AutoLog autoLog2 = autoLog;
            Objects.requireNonNull(autoLog2);
            this.this$0 = autoLog2;
        }

        @EventHandler
        private void healthListener(TickEvent.Post event) {
            if (this.this$0.isActive()) {
                this.this$0.disableHealthListener();
            } else if (Utils.canUpdate() && !((AutoLog)this.this$0).mc.player.isDeadOrDying() && ((AutoLog)this.this$0).mc.player.getHealth() > (float)this.this$0.health.get().intValue()) {
                this.this$0.info("Player health greater than minimum, re-enabling module.", new Object[0]);
                this.this$0.toggle();
                this.this$0.disableHealthListener();
            }
        }
    }
}
