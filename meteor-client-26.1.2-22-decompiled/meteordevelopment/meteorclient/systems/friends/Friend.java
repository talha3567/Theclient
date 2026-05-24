package meteordevelopment.meteorclient.systems.friends;

import com.mojang.util.UndashedUuid;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.network.FailedHttpResponse;
import meteordevelopment.meteorclient.utils.network.Http;
import meteordevelopment.meteorclient.utils.render.PlayerHeadTexture;
import meteordevelopment.meteorclient.utils.render.PlayerHeadUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class Friend
implements ISerializable<Friend>,
Comparable<Friend> {
    public volatile String name;
    @Nullable
    private volatile UUID id;
    @Nullable
    private volatile PlayerHeadTexture headTexture;
    private volatile boolean updating;

    public Friend(String name, @Nullable UUID id) {
        this.name = name;
        this.id = id;
        this.headTexture = null;
    }

    public Friend(Player player) {
        this(player.getName().getString(), player.getUUID());
    }

    public Friend(String name) {
        this(name, null);
    }

    public String getName() {
        return this.name;
    }

    public PlayerHeadTexture getHead() {
        return this.headTexture != null ? this.headTexture : PlayerHeadUtils.STEVE_HEAD;
    }

    public void updateInfo() {
        this.updating = true;
        HttpResponse res = null;
        if (this.id != null) {
            res = Http.get("https://sessionserver.mojang.com/session/minecraft/profile/" + UndashedUuid.toString((UUID)this.id)).exceptionHandler(exception -> MeteorClient.LOG.error("Error while trying to connect session server for friend '{}'", (Object)this.name)).sendJsonResponse((Type)((Object)APIResponse.class));
        }
        if (res == null || res.statusCode() != 200) {
            res = Http.get("https://api.mojang.com/users/profiles/minecraft/" + this.name).exceptionHandler(exception -> MeteorClient.LOG.error("Error while trying to update info for friend '{}'", (Object)this.name)).sendJsonResponse((Type)((Object)APIResponse.class));
        }
        if (res != null && res.statusCode() == 200) {
            this.name = ((APIResponse)res.body()).name;
            this.id = UndashedUuid.fromStringLenient((String)((APIResponse)res.body()).id);
            byte[] head = PlayerHeadUtils.fetchHead(this.id);
            MeteorClient.mc.execute(() -> {
                if (head != null) {
                    this.headTexture = new PlayerHeadTexture(head, true);
                }
            });
        } else if (!(res instanceof FailedHttpResponse)) {
            this.id = null;
        }
        this.updating = false;
    }

    public boolean headTextureNeedsUpdate() {
        return !this.updating && this.headTexture == null;
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", this.name);
        if (this.id != null) {
            tag.putString("id", UndashedUuid.toString((UUID)this.id));
        }
        return tag;
    }

    @Override
    public Friend fromTag(CompoundTag tag) {
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Friend friend = (Friend)o;
        return Objects.equals(this.name, friend.name);
    }

    public int hashCode() {
        return Objects.hash(this.name);
    }

    @Override
    public int compareTo(@NotNull Friend friend) {
        return this.name.compareToIgnoreCase(friend.name);
    }

    private static class APIResponse {
        String name;
        String id;

        private APIResponse() {
        }
    }
}
