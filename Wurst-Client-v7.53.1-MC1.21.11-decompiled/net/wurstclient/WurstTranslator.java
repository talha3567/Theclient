package net.wurstclient;

import com.google.common.collect.Lists;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.class_1074;
import net.minecraft.class_1078;
import net.minecraft.class_2477;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import net.minecraft.class_4013;
import net.minecraft.class_9226;
import net.wurstclient.WurstClient;

public class WurstTranslator
implements class_4013 {
    private final WurstClient wurst = WurstClient.INSTANCE;
    private class_1078 mcEnglish;
    private Map<String, String> currentLangStrings = Map.of();
    private Map<String, String> englishOnlyStrings = Map.of();

    public void method_14491(class_3300 manager) {
        this.mcEnglish = class_1078.method_4675((class_3300)manager, Lists.newArrayList("en_us"), (boolean)false);
        HashMap currentLangStrings = new HashMap();
        this.loadTranslations(manager, this.getCurrentLangCodes(), currentLangStrings::put);
        this.currentLangStrings = Collections.unmodifiableMap(currentLangStrings);
        HashMap englishOnlyStrings = new HashMap();
        this.loadTranslations(manager, List.of("en_us"), englishOnlyStrings::put);
        this.englishOnlyStrings = Collections.unmodifiableMap(englishOnlyStrings);
    }

    public String translate(String key, Object ... args) {
        if (this.isForcedEnglish()) {
            return this.translateEnglish(key, args);
        }
        String string = this.currentLangStrings.get(key);
        if (string != null) {
            try {
                return String.format(string, args);
            }
            catch (IllegalFormatException e) {
                return key;
            }
        }
        return this.translateMc(key, args);
    }

    public String translateEnglish(String key, Object ... args) {
        String string = this.englishOnlyStrings.get(key);
        if (string == null) {
            string = this.mcEnglish.method_48307(key);
        }
        try {
            return String.format(string, args);
        }
        catch (IllegalFormatException e) {
            return key;
        }
    }

    public String translateMc(String key, Object ... args) {
        if (class_1074.method_4663((String)key)) {
            return class_1074.method_4662((String)key, (Object[])args);
        }
        return key;
    }

    public String translateMcEnglish(String key, Object ... args) {
        try {
            return String.format(this.mcEnglish.method_48307(key), args);
        }
        catch (IllegalFormatException e) {
            return key;
        }
    }

    public boolean isForcedEnglish() {
        return this.wurst.getOtfs().translationsOtf.getForceEnglish().isChecked();
    }

    public class_1078 getMcEnglish() {
        return this.mcEnglish;
    }

    public Map<String, String> getMinecraftsCurrentLanguage() {
        return this.currentLangStrings;
    }

    public Map<String, String> getWurstsCurrentLanguage() {
        return this.isForcedEnglish() ? this.englishOnlyStrings : this.getMinecraftsCurrentLanguage();
    }

    private ArrayList<String> getCurrentLangCodes() {
        String mainLangCode = class_310.method_1551().method_1526().method_4669().toLowerCase();
        ArrayList<String> langCodes = new ArrayList<String>();
        langCodes.add("en_us");
        if (!"en_us".equals(mainLangCode)) {
            langCodes.add(mainLangCode);
        }
        return langCodes;
    }

    private void loadTranslations(class_3300 manager, Iterable<String> langCodes, BiConsumer<String, String> entryConsumer) {
        for (String langCode : langCodes) {
            String langFilePath = "translations/" + langCode + ".json";
            class_2960 langId = class_2960.method_60655((String)"wurst", (String)langFilePath);
            for (class_3298 resource : manager.method_14489(langId)) {
                try {
                    InputStream stream = resource.method_14482();
                    try {
                        if (!this.isBuiltInWurstResourcePack(resource)) continue;
                        class_2477.method_29425((InputStream)stream, entryConsumer);
                    }
                    finally {
                        if (stream == null) continue;
                        stream.close();
                    }
                }
                catch (JsonParseException | IOException e) {
                    System.out.println("Failed to load Wurst translations for " + langCode);
                    e.printStackTrace();
                }
                catch (Exception e) {
                    System.out.println("Unexpected exception while loading Wurst translations for " + langCode);
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean isBuiltInWurstResourcePack(class_3298 resource) {
        class_9226 knownPack = Optional.ofNullable(resource).flatMap(class_3298::method_56936).orElse(null);
        if (knownPack == null) {
            return false;
        }
        return ("fabric".equals(knownPack.comp_2336()) || "vanilla".equals(knownPack.comp_2336())) && "wurst".equals(knownPack.comp_2337());
    }
}
