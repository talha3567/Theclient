package net.wurstclient.altmanager;

import com.google.gson.JsonObject;
import net.wurstclient.altmanager.CrackedAlt;
import net.wurstclient.altmanager.LoginException;

public abstract class Alt {
    private boolean favorite;

    public Alt(boolean favorite) {
        this.favorite = favorite;
    }

    public abstract void login() throws LoginException;

    public abstract void exportAsJson(JsonObject var1);

    public abstract String exportAsTXT();

    public abstract String getName();

    public abstract String getDisplayName();

    public final boolean isCracked() {
        return this instanceof CrackedAlt;
    }

    public final boolean isCheckedPremium() {
        return !this.isCracked() && !this.getName().isEmpty();
    }

    public final boolean isUncheckedPremium() {
        return !this.isCracked() && this.getName().isEmpty();
    }

    public final boolean isFavorite() {
        return this.favorite;
    }

    public final void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public final String toString() {
        return this.getDisplayName();
    }
}
