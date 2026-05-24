package net.wurstclient.other_features;

import net.minecraft.class_156;
import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.other_feature.OtherFeature;
import net.wurstclient.update.Version;

@SearchTags(value={"change log", "wurst update", "release notes", "what's new", "what is new", "new features", "recently added features"})
@DontBlock
public final class ChangelogOtf
extends OtherFeature {
    public ChangelogOtf() {
        super("Changelog", "Opens the changelog in your browser.");
    }

    @Override
    public String getPrimaryAction() {
        return "View Changelog";
    }

    @Override
    public void doPrimaryAction() {
        String link = new Version("7.53.1").getChangelogLink() + "?utm_source=Wurst+Client&utm_medium=ChangelogOtf&utm_content=View+Changelog";
        class_156.method_668().method_670(link);
    }
}
