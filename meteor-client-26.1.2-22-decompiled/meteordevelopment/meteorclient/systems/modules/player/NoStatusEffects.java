package meteordevelopment.meteorclient.systems.modules.player;

import java.util.List;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StatusEffectListSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

public class NoStatusEffects
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<List<MobEffect>> blockedEffects;

    public NoStatusEffects() {
        super(Categories.Player, "no-status-effects", "Blocks specified status effects.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.blockedEffects = this.sgGeneral.add(((StatusEffectListSetting.Builder)((StatusEffectListSetting.Builder)new StatusEffectListSetting.Builder().name("blocked-effects")).description("Effects to block.")).defaultValue((MobEffect)MobEffects.LEVITATION.value(), (MobEffect)MobEffects.JUMP_BOOST.value(), (MobEffect)MobEffects.SLOW_FALLING.value(), (MobEffect)MobEffects.DOLPHINS_GRACE.value()).build());
    }

    public boolean shouldBlock(MobEffect effect) {
        return this.isActive() && this.blockedEffects.get().contains(effect);
    }
}
