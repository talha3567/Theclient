package meteordevelopment.meteorclient.mixin;

import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.elements.TextHud;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={CrashReport.class})
public abstract class CrashReportMixin {
    @Inject(method={"getDetails(Ljava/lang/StringBuilder;)V"}, at={@At(value="TAIL")})
    private void onAddDetails(StringBuilder builder, CallbackInfo ci) {
        builder.append("\n\n-- Meteor Client --\n\n");
        builder.append("Version: ").append(MeteorClient.VERSION).append("\n");
        if (!MeteorClient.BUILD_NUMBER.isEmpty()) {
            builder.append("Build: ").append(MeteorClient.BUILD_NUMBER).append("\n");
        }
        if (Modules.get() != null) {
            boolean modulesActive = false;
            for (Category category : Modules.loopCategories()) {
                List<Module> modules = Modules.get().getGroup(category);
                boolean categoryActive = false;
                for (Module module : modules) {
                    if (module == null || !module.isActive()) continue;
                    if (!modulesActive) {
                        modulesActive = true;
                        builder.append("\n[[ Active Modules ]]\n");
                    }
                    if (!categoryActive) {
                        categoryActive = true;
                        builder.append("\n[").append(category).append("]:\n");
                    }
                    builder.append(module.name).append("\n");
                }
            }
        }
        if (Hud.get() != null && Hud.get().active) {
            boolean hudActive = false;
            for (HudElement element : Hud.get()) {
                if (element == null || !element.isActive()) continue;
                if (!hudActive) {
                    hudActive = true;
                    builder.append("\n[[ Active Hud Elements ]]\n");
                }
                if (!(element instanceof TextHud)) {
                    builder.append(element.info.name).append("\n");
                    continue;
                }
                TextHud textHud = (TextHud)element;
                builder.append("Text\n{").append(textHud.text.get()).append("}\n");
                if (textHud.shown.get() == TextHud.Shown.Always) continue;
                builder.append("(").append((Object)textHud.shown.get()).append(textHud.condition.get()).append(")\n");
            }
        }
    }
}
