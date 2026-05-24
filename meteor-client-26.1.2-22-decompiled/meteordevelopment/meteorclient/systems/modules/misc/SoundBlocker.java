package meteordevelopment.meteorclient.systems.modules.misc;

import java.util.List;
import meteordevelopment.meteorclient.events.world.PlaySoundEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.SoundEventListSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public class SoundBlocker
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<List<SoundEvent>> sounds;

    public SoundBlocker() {
        super(Categories.Misc, "sound-blocker", "Cancels out selected sounds.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sounds = this.sgGeneral.add(((SoundEventListSetting.Builder)((SoundEventListSetting.Builder)new SoundEventListSetting.Builder().name("sounds")).description("Sounds to block.")).build());
    }

    @EventHandler
    private void onPlaySound(PlaySoundEvent event) {
        for (SoundEvent sound : this.sounds.get()) {
            if (!sound.location().equals((Object)event.sound.getIdentifier())) continue;
            event.cancel();
            break;
        }
    }

    public boolean shouldBlock(SoundInstance soundInstance) {
        return this.isActive() && this.sounds.get().contains(Setting.parseId(BuiltInRegistries.SOUND_EVENT, soundInstance.getIdentifier().getPath()));
    }
}
