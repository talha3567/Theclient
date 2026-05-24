package meteordevelopment.meteorclient.utils.entity.fakeplayer;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixin.AbstractClientPlayerAccessor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class FakePlayerEntity
extends RemotePlayer {
    public boolean doNotPush;
    public boolean hideWhenInsideCamera;
    public boolean noHit;

    public FakePlayerEntity(Player player, String name, float health, boolean copyInv) {
        super(MeteorClient.mc.level, new GameProfile(UUID.randomUUID(), name));
        this.copyPosition((Entity)player);
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
        this.yHeadRotO = this.yHeadRot = player.yHeadRot;
        this.yBodyRotO = this.yBodyRot = player.yBodyRot;
        this.getAttributes().assignAllValues(player.getAttributes());
        this.setPose(player.getPose());
        if (health <= 20.0f) {
            this.setHealth(health);
        } else {
            this.setHealth(health);
            this.setAbsorptionAmount(health - 20.0f);
        }
        if (copyInv) {
            this.getInventory().replaceWith(player.getInventory());
        }
    }

    public void spawn() {
        this.unsetRemoved();
        MeteorClient.mc.level.addEntity((Entity)this);
    }

    public void despawn() {
        MeteorClient.mc.level.removeEntity(this.getId(), Entity.RemovalReason.DISCARDED);
        this.setRemoved(Entity.RemovalReason.DISCARDED);
    }

    @Nullable
    protected PlayerInfo getPlayerInfo() {
        PlayerInfo entry = super.getPlayerInfo();
        if (entry == null) {
            ((AbstractClientPlayerAccessor)((Object)this)).meteor$setPlayerInfo(MeteorClient.mc.getConnection().getPlayerInfo(MeteorClient.mc.player.getUUID()));
        }
        return entry;
    }
}
