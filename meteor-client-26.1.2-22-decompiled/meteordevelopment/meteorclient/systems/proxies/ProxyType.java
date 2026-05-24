package meteordevelopment.meteorclient.systems.proxies;

import org.jetbrains.annotations.Nullable;

public enum ProxyType {
    Socks4,
    Socks5;


    @Nullable
    public static ProxyType parse(String group) {
        for (ProxyType type : ProxyType.values()) {
            if (!type.name().equalsIgnoreCase(group)) continue;
            return type;
        }
        return null;
    }
}
