package net.wurstclient.other_features;

import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.other_feature.OtherFeature;

@SearchTags(value={"Server Finder"})
@DontBlock
public final class ServerFinderOtf
extends OtherFeature {
    public ServerFinderOtf() {
        super("ServerFinder", "Allows you to find easy-to-grief Minecraft servers quickly and easily. To use it, press the 'Server Finder' button on the server selection screen.");
    }
}
