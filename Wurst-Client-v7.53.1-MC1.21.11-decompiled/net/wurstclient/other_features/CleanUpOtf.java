package net.wurstclient.other_features;

import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.other_feature.OtherFeature;

@SearchTags(value={"Clean Up"})
@DontBlock
public final class CleanUpOtf
extends OtherFeature {
    public CleanUpOtf() {
        super("CleanUp", "Cleans up your server list.\nTo use it, press the 'Clean Up' button on the server selection screen.");
    }
}
