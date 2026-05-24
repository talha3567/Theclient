package meteordevelopment.meteorclient.systems.modules.render;

import java.lang.reflect.Type;
import java.lang.runtime.SwitchBootstraps;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.network.Http;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.UniquelyIdentifyable;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

public class EntityOwner
extends Module {
    private static final Color BACKGROUND = new Color(0, 0, 0, 75);
    private static final Color TEXT = new Color(255, 255, 255);
    private final SettingGroup sgGeneral;
    private final Setting<Double> scale;
    private final Vector3d pos;
    private final Map<UUID, String> uuidToName;

    public EntityOwner() {
        super(Categories.Render, "entity-owner", "Displays the name of the player who owns the entity you're looking at.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.scale = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("The scale of the text.")).defaultValue(1.0).min(0.0).build());
        this.pos = new Vector3d();
        this.uuidToName = new HashMap<UUID, String>();
    }

    @Override
    public void onDeactivate() {
        this.uuidToName.clear();
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        block4: for (Entity entity : this.mc.level.entitiesForRendering()) {
            EntityReference owner;
            Entity entity2;
            Objects.requireNonNull(entity);
            int n = 0;
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{TamableAnimal.class, ThrownEnderpearl.class}, (Entity)entity2, n)) {
                case 0: {
                    TamableAnimal tameable = (TamableAnimal)entity2;
                    owner = tameable.getOwnerReference();
                    break;
                }
                case 1: {
                    ThrownEnderpearl pearl = (ThrownEnderpearl)entity2;
                    owner = EntityReference.of((UniquelyIdentifyable)((LivingEntity)pearl.getOwner()));
                    break;
                }
                default: {
                    continue block4;
                }
            }
            if (owner == null) continue;
            Utils.set(this.pos, entity, event.tickDelta);
            this.pos.add(0.0, (double)entity.getEyeHeight(entity.getPose()) + 0.75, 0.0);
            if (!NametagUtils.to2D(this.pos, this.scale.get())) continue;
            this.renderNametag(this.getOwnerName((EntityReference<LivingEntity>)owner));
        }
    }

    private void renderNametag(String name) {
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(this.pos);
        text.beginBig();
        double w = text.getWidth(name);
        double x = -w / 2.0;
        double y = -text.getHeight();
        Renderer2D.COLOR.begin();
        Renderer2D.COLOR.quad(x - 1.0, y - 1.0, w + 2.0, text.getHeight() + 2.0, BACKGROUND);
        Renderer2D.COLOR.render();
        text.render(name, x, y, TEXT);
        text.end();
        NametagUtils.end();
    }

    private String getOwnerName(EntityReference<LivingEntity> owner) {
        @Nullable LivingEntity ownerEntity = (LivingEntity)EntityReference.get(owner, (Level)this.mc.level, LivingEntity.class);
        if (ownerEntity instanceof Player) {
            Player playerEntity = (Player)ownerEntity;
            return playerEntity.getName().getString();
        }
        UUID uuid = owner.getUUID();
        String name = this.uuidToName.get(uuid);
        if (name != null) {
            return name;
        }
        MeteorExecutor.execute(() -> {
            if (this.isActive()) {
                @Nullable ProfileResponse res = (ProfileResponse)Http.get("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString().replace("-", "")).sendJson((Type)((Object)ProfileResponse.class));
                if (this.isActive()) {
                    if (res == null) {
                        this.uuidToName.put(uuid, "Failed to get name");
                    } else {
                        this.uuidToName.put(uuid, res.name);
                    }
                }
            }
        });
        name = "Retrieving";
        this.uuidToName.put(uuid, name);
        return name;
    }

    private static class ProfileResponse {
        public String name;

        private ProfileResponse() {
        }
    }
}
