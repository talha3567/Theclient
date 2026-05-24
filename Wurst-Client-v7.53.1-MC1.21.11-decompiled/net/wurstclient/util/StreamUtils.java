package net.wurstclient.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public final class StreamUtils
extends Enum<StreamUtils> {
    private static final /* synthetic */ StreamUtils[] $VALUES;

    public static StreamUtils[] values() {
        return (StreamUtils[])$VALUES.clone();
    }

    public static StreamUtils valueOf(String name) {
        return Enum.valueOf(StreamUtils.class, name);
    }

    public static ArrayList<String> readAllLines(InputStream input) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(input));){
            String line;
            ArrayList<String> lines = new ArrayList<String>();
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            ArrayList<String> arrayList = lines;
            return arrayList;
        }
    }

    private static /* synthetic */ StreamUtils[] $values() {
        return new StreamUtils[0];
    }

    static {
        $VALUES = StreamUtils.$values();
    }
}
