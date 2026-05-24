package net.wurstclient.keybinds;

import java.util.Objects;
import net.minecraft.class_3675;

public class Keybind
implements Comparable<Keybind> {
    private final String key;
    private final String commands;

    public Keybind(String key, String commands) {
        this.key = Objects.requireNonNull(key);
        this.commands = Objects.requireNonNull(commands);
    }

    public String getKey() {
        return this.key;
    }

    public String getCommands() {
        return this.commands;
    }

    @Override
    public int compareTo(Keybind o) {
        return this.key.compareToIgnoreCase(o.key);
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Keybind)) {
            return false;
        }
        Keybind otherKeybind = (Keybind)obj;
        return this.key.equalsIgnoreCase(otherKeybind.key);
    }

    public int hashCode() {
        return this.key.hashCode();
    }

    public String toString() {
        return Keybind.getDisplayKey(this.key) + " -> " + this.commands;
    }

    public static String getDisplayKey(String key) {
        try {
            return class_3675.method_15981((String)key).method_27445().getString();
        }
        catch (IllegalArgumentException e) {
            return key;
        }
    }
}
