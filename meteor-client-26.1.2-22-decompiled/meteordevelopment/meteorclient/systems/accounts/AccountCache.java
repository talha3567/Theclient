package meteordevelopment.meteorclient.systems.accounts;

import com.mojang.util.UndashedUuid;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.misc.NbtException;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.render.PlayerHeadTexture;
import meteordevelopment.meteorclient.utils.render.PlayerHeadUtils;
import net.minecraft.nbt.CompoundTag;

public class AccountCache
implements ISerializable<AccountCache> {
    public String username = "";
    public String uuid = "";
    private PlayerHeadTexture headTexture;
    private volatile boolean loadingHead;

    public PlayerHeadTexture getHeadTexture() {
        return this.headTexture != null ? this.headTexture : PlayerHeadUtils.STEVE_HEAD;
    }

    public void loadHead() {
        this.loadHead(null);
    }

    public void loadHead(Runnable callback) {
        if (this.headTexture != null || this.uuid == null || this.uuid.isBlank()) {
            if (callback != null) {
                MeteorClient.mc.execute(callback);
            }
            return;
        }
        if (this.loadingHead) {
            return;
        }
        this.loadingHead = true;
        MeteorExecutor.execute(() -> {
            byte[] head = PlayerHeadUtils.fetchHead(UndashedUuid.fromStringLenient((String)this.uuid));
            MeteorClient.mc.execute(() -> {
                if (head != null) {
                    this.headTexture = new PlayerHeadTexture(head, true);
                }
                this.loadingHead = false;
                if (callback != null) {
                    callback.run();
                }
            });
        });
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("username", this.username);
        tag.putString("uuid", this.uuid);
        return tag;
    }

    @Override
    public AccountCache fromTag(CompoundTag tag) {
        if (tag.getString("username").isEmpty() || tag.getString("uuid").isEmpty()) {
            throw new NbtException();
        }
        this.username = (String)tag.getString("username").get();
        this.uuid = (String)tag.getString("uuid").get();
        this.loadHead();
        return this;
    }
}
