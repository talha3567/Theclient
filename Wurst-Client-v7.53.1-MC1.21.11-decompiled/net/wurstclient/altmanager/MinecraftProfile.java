package net.wurstclient.altmanager;

import java.util.UUID;

public final class MinecraftProfile {
    private final UUID uuid;
    private final String name;
    private final String mcAccessToken;

    public MinecraftProfile(UUID uuid, String name, String mcAccessToken) {
        this.uuid = uuid;
        this.name = name;
        this.mcAccessToken = mcAccessToken;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public String getAccessToken() {
        return this.mcAccessToken;
    }
}
