package net.wurstclient.other_feature;

import java.util.Objects;
import net.wurstclient.Feature;

public abstract class OtherFeature
extends Feature {
    private final String name;
    private final String description;

    public OtherFeature(String name, String description) {
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
        if (name.contains(" ")) {
            throw new IllegalArgumentException("Feature name must not contain spaces: " + name);
        }
    }

    @Override
    public final String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return WURST.translate(this.description, new Object[0]);
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public String getPrimaryAction() {
        return "";
    }

    @Override
    public void doPrimaryAction() {
    }
}
