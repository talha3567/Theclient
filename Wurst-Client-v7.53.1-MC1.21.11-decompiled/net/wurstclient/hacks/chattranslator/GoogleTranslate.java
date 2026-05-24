package net.wurstclient.hacks.chattranslator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringEscapeUtils;

public final class GoogleTranslate
extends Enum<GoogleTranslate> {
    private static final HashMap<Character, String> simplifyMap;
    private static final /* synthetic */ GoogleTranslate[] $VALUES;

    public static GoogleTranslate[] values() {
        return (GoogleTranslate[])$VALUES.clone();
    }

    public static GoogleTranslate valueOf(String name) {
        return Enum.valueOf(GoogleTranslate.class, name);
    }

    public static String translate(String text, String langFrom, String langTo) {
        String html = GoogleTranslate.getHTML(text, langFrom, langTo);
        String translated = GoogleTranslate.parseHTML(html);
        if (GoogleTranslate.simplify(text).equals(GoogleTranslate.simplify(translated))) {
            return null;
        }
        return translated;
    }

    private static String getHTML(String text, String langFrom, String langTo) {
        String string;
        URL url = GoogleTranslate.createURL(text, langFrom, langTo);
        URLConnection connection = GoogleTranslate.setupConnection(url);
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        try {
            String line;
            StringBuilder html = new StringBuilder();
            while ((line = br.readLine()) != null) {
                html.append(line + "\n");
            }
            string = html.toString();
        }
        catch (Throwable throwable) {
            try {
                try {
                    br.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException e) {
                return null;
            }
        }
        br.close();
        return string;
    }

    private static URL createURL(String text, String langFrom, String langTo) {
        try {
            String encodedText = URLEncoder.encode(text.trim(), "UTF-8");
            String urlString = String.format("https://translate.google.com/m?hl=en&sl=%s&tl=%s&ie=UTF-8&prev=_m&q=%s", langFrom, langTo, encodedText);
            return URI.create(urlString).toURL();
        }
        catch (UnsupportedEncodingException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static URLConnection setupConnection(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(5000);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        return connection;
    }

    private static String parseHTML(String html) {
        String regex = "class=\"result-container\">([^<]*)<\\/div>";
        Pattern pattern = Pattern.compile(regex, 8);
        Matcher matcher = pattern.matcher(html);
        if (!matcher.find()) {
            return null;
        }
        String match = matcher.group(1);
        if (match == null || match.isEmpty()) {
            return null;
        }
        return StringEscapeUtils.unescapeHtml4((String)match);
    }

    private static String simplify(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toLowerCase().toCharArray()) {
            sb.append(simplifyMap.getOrDefault(Character.valueOf(c), String.valueOf(c)));
        }
        return sb.toString();
    }

    private static /* synthetic */ GoogleTranslate[] $values() {
        return new GoogleTranslate[0];
    }

    static {
        $VALUES = GoogleTranslate.$values();
        simplifyMap = new HashMap();
        simplifyMap.put(Character.valueOf(' '), "");
        simplifyMap.put(Character.valueOf('\r'), "");
        simplifyMap.put(Character.valueOf('\n'), "");
        simplifyMap.put(Character.valueOf('\t'), "");
        simplifyMap.put(Character.valueOf('\u00e4'), "a");
        simplifyMap.put(Character.valueOf('\u00f6'), "o");
        simplifyMap.put(Character.valueOf('\u00fc'), "u");
        simplifyMap.put(Character.valueOf('\u00e1'), "a");
        simplifyMap.put(Character.valueOf('\u00e9'), "e");
        simplifyMap.put(Character.valueOf('\u00ed'), "i");
        simplifyMap.put(Character.valueOf('\u00f3'), "o");
        simplifyMap.put(Character.valueOf('\u00fa'), "u");
        simplifyMap.put(Character.valueOf('\u00e0'), "a");
        simplifyMap.put(Character.valueOf('\u00e8'), "e");
        simplifyMap.put(Character.valueOf('\u00ec'), "i");
        simplifyMap.put(Character.valueOf('\u00f2'), "o");
        simplifyMap.put(Character.valueOf('\u00f9'), "u");
        simplifyMap.put(Character.valueOf('\u00e2'), "a");
        simplifyMap.put(Character.valueOf('\u00ea'), "e");
        simplifyMap.put(Character.valueOf('\u00ee'), "i");
        simplifyMap.put(Character.valueOf('\u00f4'), "o");
        simplifyMap.put(Character.valueOf('\u00fb'), "u");
        simplifyMap.put(Character.valueOf('\u00e3'), "a");
        simplifyMap.put(Character.valueOf('\u00f5'), "o");
        simplifyMap.put(Character.valueOf('\u00f1'), "n");
        simplifyMap.put(Character.valueOf('\u00e7'), "c");
    }
}
