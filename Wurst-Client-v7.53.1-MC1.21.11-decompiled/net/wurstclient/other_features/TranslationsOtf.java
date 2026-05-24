package net.wurstclient.other_features;

import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.other_feature.OtherFeature;
import net.wurstclient.settings.CheckboxSetting;

@SearchTags(value={"languages", "localizations", "localisations", "internationalization", "internationalisation", "i18n", "sprachen", "\u00fcbersetzungen", "force english"})
@DontBlock
public final class TranslationsOtf
extends OtherFeature {
    private final CheckboxSetting forceEnglish = new CheckboxSetting("Force English", "Displays the Wurst Client in English, even if Minecraft is set to a different language.", true);

    public TranslationsOtf() {
        super("Translations", "Allows text in Wurst to be displayed in other languages than English. It will use the same language that Minecraft is set to.\n\nThis is an experimental feature!");
        this.addSetting(this.forceEnglish);
    }

    public CheckboxSetting getForceEnglish() {
        return this.forceEnglish;
    }
}
