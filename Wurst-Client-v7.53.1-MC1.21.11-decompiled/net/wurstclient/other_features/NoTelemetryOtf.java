package net.wurstclient.other_features;

import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.other_feature.OtherFeature;
import net.wurstclient.settings.CheckboxSetting;

@DontBlock
@SearchTags(value={"privacy", "data", "tracking", "snooper", "spyware"})
public final class NoTelemetryOtf
extends OtherFeature {
    private final CheckboxSetting disableTelemetry = new CheckboxSetting("Disable telemetry", true);

    public NoTelemetryOtf() {
        super("NoTelemetry", "Disables the \"required\" telemetry that Mojang introduced in 22w46a. Turns out it's not so required after all.");
        this.addSetting(this.disableTelemetry);
    }

    @Override
    public boolean isEnabled() {
        return this.disableTelemetry.isChecked();
    }

    @Override
    public String getPrimaryAction() {
        return this.isEnabled() ? "Re-enable Telemetry" : "Disable Telemetry";
    }

    @Override
    public void doPrimaryAction() {
        this.disableTelemetry.setChecked(!this.disableTelemetry.isChecked());
    }
}
