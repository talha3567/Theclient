package net.wurstclient.other_features;

import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.other_feature.OtherFeature;

@SearchTags(value={"last server"})
@DontBlock
public final class LastServerOtf
extends OtherFeature {
    public LastServerOtf() {
        super("LastServer", "Wurst adds a \"Last Server\" button to the server selection screen that automatically brings you back to the last server you played on.\n\nUseful when you get kicked and/or have a lot of servers.");
    }
}
