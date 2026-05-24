package net.wurstclient.altmanager;

import com.google.gson.JsonObject;
import java.util.Objects;
import net.wurstclient.WurstClient;
import net.wurstclient.altmanager.Alt;
import net.wurstclient.altmanager.LoginException;
import net.wurstclient.altmanager.MicrosoftLoginManager;

public final class MojangAlt
extends Alt {
    private final String email;
    private final String password;
    private String name = "";

    public MojangAlt(String email, String password) {
        this(email, password, "", false);
    }

    public MojangAlt(String email, String password, String name, boolean favorite) {
        super(favorite);
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException();
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.email = email;
        this.password = password;
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public void login() throws LoginException {
        MicrosoftLoginManager.login(this.email, this.password);
        this.name = this.getNameFromSession();
    }

    private String getNameFromSession() {
        String name = WurstClient.MC.method_1548().method_1676();
        if (name == null || name.isEmpty()) {
            throw new RuntimeException("Login returned " + (name == null ? "null" : "empty") + " username. This shouldn't be possible!");
        }
        return name;
    }

    @Override
    public void exportAsJson(JsonObject json) {
        JsonObject jsonAlt = new JsonObject();
        jsonAlt.addProperty("password", this.password);
        jsonAlt.addProperty("name", this.name);
        jsonAlt.addProperty("starred", this.isFavorite());
        json.add(this.email, jsonAlt);
    }

    @Override
    public String exportAsTXT() {
        return this.email + ":" + this.password;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDisplayName() {
        return this.name.isEmpty() ? this.email : this.name;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPassword() {
        return this.password;
    }

    public int hashCode() {
        return Objects.hash(this.email);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MojangAlt)) {
            return false;
        }
        MojangAlt other = (MojangAlt)obj;
        return Objects.equals(this.email, other.email);
    }
}
