package net.wurstclient.hacks.chattranslator;

import net.minecraft.class_2561;
import net.wurstclient.settings.EnumSetting;

public final class LanguageSetting
extends EnumSetting<Language> {
    private LanguageSetting(String name, String description, Language[] values, Language selected) {
        super(name, description, (Enum[])values, (Enum)selected);
    }

    public static LanguageSetting withAutoDetect(String name, String description, Language selected) {
        return new LanguageSetting(name, description, Language.values(), selected);
    }

    public static LanguageSetting withAutoDetect(String name, Language selected) {
        return new LanguageSetting(name, "", Language.values(), selected);
    }

    public static LanguageSetting withoutAutoDetect(String name, String description, Language selected) {
        Language[] values = Language.valuesWithoutAutoDetect();
        return new LanguageSetting(name, description, values, selected);
    }

    public static LanguageSetting withoutAutoDetect(String name, Language selected) {
        Language[] values = Language.valuesWithoutAutoDetect();
        return new LanguageSetting(name, "", values, selected);
    }

    public static enum Language {
        AUTO_DETECT("Detect Language", "auto"),
        AFRIKAANS("Afrikaans", "af"),
        ARABIC("Arabic", "ar"),
        CZECH("Czech", "cs"),
        CHINESE_SIMPLIFIED("Chinese (simplified)", "zh-CN"),
        CHINESE_TRADITIONAL("Chinese (traditional)", "zh-TW"),
        DANISH("Danish", "da"),
        DUTCH("Dutch", "nl"),
        ENGLISH("English", "en"),
        FINNISH("Finnish", "fi"),
        FRENCH("French", "fr"),
        GERMAN("Deutsch!", "de"),
        GREEK("Greek", "el"),
        HINDI("Hindi", "hi"),
        ITALIAN("Italian", "it"),
        JAPANESE("Japanese", "ja"),
        KOREAN("Korean", "ko"),
        NORWEGIAN("Norwegian", "no"),
        POLISH("Polish", "pl"),
        PORTUGUESE("Portugese", "pt"),
        RUSSIAN("Russian", "ru"),
        SPANISH("Spanish", "es"),
        SWAHILI("Swahili", "sw"),
        SWEDISH("Swedish", "sv"),
        TURKISH("Turkish", "tr");

        private final String name;
        private final String value;
        private final String prefix;

        private Language(String name, String value) {
            this.name = name;
            this.value = value;
            this.prefix = "\u00a7a[\u00a7b" + name + "\u00a7a]:\u00a7r ";
        }

        public String getValue() {
            return this.value;
        }

        public String getPrefix() {
            return this.prefix;
        }

        public class_2561 prefixText(String s) {
            return class_2561.method_43470((String)(this.prefix + s));
        }

        public String toString() {
            return this.name;
        }

        private static Language[] valuesWithoutAutoDetect() {
            Language[] allValues = Language.values();
            Language[] valuesWithoutAuto = new Language[allValues.length - 1];
            System.arraycopy(allValues, 1, valuesWithoutAuto, 0, valuesWithoutAuto.length);
            return valuesWithoutAuto;
        }
    }
}
