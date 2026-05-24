package net.wurstclient.util;

import net.minecraft.class_310;
import net.minecraft.class_412;
import net.minecraft.class_437;
import net.minecraft.class_500;
import net.minecraft.class_639;
import net.minecraft.class_642;
import net.wurstclient.WurstClient;

public final class LastServerRememberer
extends Enum<LastServerRememberer> {
    private static class_642 lastServer;
    private static final /* synthetic */ LastServerRememberer[] $VALUES;

    public static LastServerRememberer[] values() {
        return (LastServerRememberer[])$VALUES.clone();
    }

    public static LastServerRememberer valueOf(String name) {
        return Enum.valueOf(LastServerRememberer.class, name);
    }

    public static class_642 getLastServer() {
        return lastServer;
    }

    public static void setLastServer(class_642 server) {
        lastServer = server;
    }

    public static void joinLastServer(class_500 mpScreen) {
        if (lastServer == null) {
            return;
        }
        mpScreen.method_2548(lastServer);
    }

    public static void reconnect(class_437 prevScreen) {
        if (lastServer == null) {
            return;
        }
        class_412.method_36877((class_437)prevScreen, (class_310)WurstClient.MC, (class_639)class_639.method_2950((String)LastServerRememberer.lastServer.field_3761), (class_642)lastServer, (boolean)false, null);
    }

    private static /* synthetic */ LastServerRememberer[] $values() {
        return new LastServerRememberer[0];
    }

    static {
        $VALUES = LastServerRememberer.$values();
    }
}
