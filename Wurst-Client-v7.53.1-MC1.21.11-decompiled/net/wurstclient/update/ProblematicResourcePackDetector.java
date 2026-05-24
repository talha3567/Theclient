package net.wurstclient.update;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import net.minecraft.class_3262;
import net.minecraft.class_3288;
import net.minecraft.class_7367;
import net.wurstclient.WurstClient;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.StreamUtils;

public final class ProblematicResourcePackDetector
implements UpdateListener {
    private static final String WARNING_MESSAGE = "VanillaTweaks \"Twinkling Stars\" pack detected. This resource pack is known to cause problems with Wurst!";
    private boolean running;

    public void start() {
        if (this.running) {
            return;
        }
        WurstClient.INSTANCE.getEventManager().add(UpdateListener.class, this);
        this.running = true;
    }

    @Override
    public void onUpdate() {
        if (WurstClient.INSTANCE.isEnabled() && this.isTwinklingStarsInstalled()) {
            ChatUtils.warning(WARNING_MESSAGE);
        }
        WurstClient.INSTANCE.getEventManager().remove(UpdateListener.class, this);
        this.running = false;
    }

    private boolean isTwinklingStarsInstalled() {
        Collection enabledProfiles = WurstClient.MC.method_1520().method_14444();
        for (class_3288 profile : enabledProfiles) {
            class_3262 pack;
            if (!this.isVanillaTweaks(profile) || !this.containsTwinklingStars(pack = profile.method_14458())) continue;
            return true;
        }
        return false;
    }

    private boolean isVanillaTweaks(class_3288 profile) {
        return profile.method_14459().getString().contains("Vanilla Tweaks");
    }

    private boolean containsTwinklingStars(class_3262 pack) {
        try {
            class_7367 supplier = pack.method_14410(new String[]{"Selected Packs.txt"});
            if (supplier == null) {
                return false;
            }
            ArrayList<String> lines = StreamUtils.readAllLines((InputStream)supplier.get());
            return lines.stream().anyMatch(line -> line.contains("TwinklingStars"));
        }
        catch (IOException | IllegalArgumentException e) {
            return false;
        }
    }
}
