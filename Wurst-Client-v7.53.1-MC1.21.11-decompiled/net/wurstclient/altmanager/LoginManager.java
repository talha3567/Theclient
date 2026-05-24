package net.wurstclient.altmanager;

import java.util.Optional;
import net.minecraft.class_320;
import net.minecraft.class_4844;
import net.wurstclient.WurstClient;

public final class LoginManager
extends Enum<LoginManager> {
    private static final /* synthetic */ LoginManager[] $VALUES;

    public static LoginManager[] values() {
        return (LoginManager[])$VALUES.clone();
    }

    public static LoginManager valueOf(String name) {
        return Enum.valueOf(LoginManager.class, name);
    }

    public static void changeCrackedName(String newName) {
        class_320 session = new class_320(newName, class_4844.method_43344((String)newName), "", Optional.empty(), Optional.empty());
        WurstClient.IMC.setWurstSession(session);
    }

    private static /* synthetic */ LoginManager[] $values() {
        return new LoginManager[0];
    }

    static {
        $VALUES = LoginManager.$values();
    }
}
