package net.wurstclient.settings;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import net.wurstclient.settings.SliderSetting;

public final class RoundingPrecisionSetting
extends SliderSetting {
    private static final DecimalFormatSymbols SYMBOLS = new DecimalFormatSymbols(Locale.ENGLISH);
    private final DecimalFormat[] FORMATS;

    public RoundingPrecisionSetting(String name, String description, int value, int min, int max) {
        super(name, description, (double)value, (double)min, (double)max, 1.0, SliderSetting.ValueDisplay.ROUNDING_PRECISION);
        if (min < 0) {
            throw new IllegalArgumentException("min must be greater than or equal to 0");
        }
        this.FORMATS = new DecimalFormat[max + 1];
    }

    public DecimalFormat getFormat() {
        int value = this.getValueI();
        if (this.FORMATS[value] == null) {
            Object pattern = "0";
            if (value > 0) {
                pattern = (String)pattern + "." + "#".repeat(value);
            }
            this.FORMATS[value] = new DecimalFormat((String)pattern, SYMBOLS);
        }
        return this.FORMATS[value];
    }

    public String format(double value) {
        return this.getFormat().format(value);
    }
}
