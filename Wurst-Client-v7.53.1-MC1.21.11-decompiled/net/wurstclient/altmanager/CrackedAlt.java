package net.wurstclient.altmanager;

import com.google.gson.JsonObject;
import java.util.Objects;
import net.wurstclient.altmanager.Alt;
import net.wurstclient.altmanager.LoginManager;

public final class CrackedAlt
extends Alt {
    private final String name;

    public CrackedAlt(String name) {
        this(name, false);
    }

    public CrackedAlt(String name, boolean favorite) {
        super(favorite);
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.name = name;
    }

    @Override
    public void login() {
        LoginManager.changeCrackedName(this.name);
    }

    @Override
    public void exportAsJson(JsonObject json) {
        JsonObject jsonAlt = new JsonObject();
        jsonAlt.addProperty("starred", this.isFavorite());
        json.add(this.name, jsonAlt);
    }

    @Override
    public String exportAsTXT() {
        return this.name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDisplayName() {
        return this.name;
    }

    public int hashCode() {
        return Objects.hash(this.name);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CrackedAlt)) {
            return false;
        }
        CrackedAlt other = (CrackedAlt)obj;
        return Objects.equals(this.name, other.name);
    }
}
