package meteordevelopment.meteorclient.gui.screens.settings.base;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.screens.settings.base.CollectionListSettingScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.IdentifierException;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public abstract class DynamicRegistryListSettingScreen<T>
extends CollectionListSettingScreen<ResourceKey<T>> {
    protected final ResourceKey<Registry<T>> registryKey;

    public DynamicRegistryListSettingScreen(GuiTheme theme, String title, Setting<?> setting, Collection<ResourceKey<T>> collection, ResourceKey<Registry<T>> registryKey) {
        super(theme, title, setting, collection, DynamicRegistryListSettingScreen.createUniverse(collection, registryKey));
        this.registryKey = registryKey;
    }

    private static <T> Iterable<ResourceKey<T>> createUniverse(Collection<ResourceKey<T>> collection, ResourceKey<Registry<T>> registryKey) {
        ReferenceOpenHashSet set = new ReferenceOpenHashSet(collection);
        Optional.ofNullable(Minecraft.getInstance().getConnection()).map(networkHandler -> networkHandler.registryAccess()).orElseGet(VanillaRegistries::createLookup).lookup(registryKey).ifPresent(arg_0 -> DynamicRegistryListSettingScreen.lambda$createUniverse$1((Set)set, arg_0));
        return set;
    }

    @Override
    protected void postWidgets(WTable left, WTable right) {
        if (!left.cells.isEmpty()) {
            left.add(this.theme.horizontalSeparator()).expandX();
            left.row();
        }
        WHorizontalList manualEntry = left.add(this.theme.horizontalList()).expandX().widget();
        WTextBox textBox = manualEntry.add(this.theme.textBox("minecraft:")).expandX().minWidth(120.0).widget();
        manualEntry.add(this.theme.plus()).expandCellX().right().widget().action = () -> {
            String entry = textBox.get().trim();
            try {
                Identifier id = entry.contains(":") ? Identifier.parse((String)entry) : Identifier.withDefaultNamespace((String)entry);
                this.addValue(ResourceKey.create(this.registryKey, (Identifier)id));
            }
            catch (IdentifierException identifierException) {
                // empty catch block
            }
        };
    }

    private static /* synthetic */ void lambda$createUniverse$1(Set set, HolderLookup.RegistryLookup registry) {
        registry.listElementIds().forEach(set::add);
    }
}
