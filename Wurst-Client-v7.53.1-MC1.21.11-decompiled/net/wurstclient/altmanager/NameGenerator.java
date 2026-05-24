package net.wurstclient.altmanager;

import java.util.Random;

public final class NameGenerator {
    private static final Random random = new Random();

    public static String generateName() {
        boolean leet;
        Object name = "";
        int nameLength = (int)Math.round(Math.random() * 4.0) + 5;
        String vowels = "aeiouy";
        String consonants = "bcdfghklmnprstvwz";
        int usedConsonants = 0;
        int usedVowels = 0;
        String lastLetter = "blah";
        for (int i = 0; i < nameLength; ++i) {
            String nextLetter = lastLetter;
            if ((random.nextBoolean() || usedConsonants == 1) && usedVowels < 2) {
                while (nextLetter.equals(lastLetter)) {
                    letterIndex = (int)(Math.random() * (double)vowels.length() - 1.0);
                    nextLetter = vowels.substring(letterIndex, letterIndex + 1);
                }
                usedConsonants = 0;
                ++usedVowels;
            } else {
                while (nextLetter.equals(lastLetter)) {
                    letterIndex = (int)(Math.random() * (double)consonants.length() - 1.0);
                    nextLetter = consonants.substring(letterIndex, letterIndex + 1);
                }
                ++usedConsonants;
                usedVowels = 0;
            }
            lastLetter = nextLetter;
            name = ((String)name).concat(nextLetter);
        }
        int capitalMode = (int)Math.round(Math.random() * 2.0);
        if (capitalMode == 1) {
            name = ((String)name).substring(0, 1).toUpperCase() + ((String)name).substring(1);
        } else if (capitalMode == 2) {
            for (int i = 0; i < nameLength; ++i) {
                if ((int)Math.round(Math.random() * 3.0) != 1) continue;
                name = ((String)name).substring(0, i) + ((String)name).substring(i, i + 1).toUpperCase() + (i == nameLength ? "" : ((String)name).substring(i + 1));
            }
        }
        int numberLength = (int)Math.round(Math.random() * 3.0) + 1;
        int numberMode = (int)Math.round(Math.random() * 3.0);
        boolean number = random.nextBoolean();
        if (number) {
            if (numberLength == 1) {
                nextNumber = (int)Math.round(Math.random() * 9.0);
                name = ((String)name).concat(Integer.toString(nextNumber));
            } else if (numberMode == 0) {
                nextNumber = (int)(Math.round(Math.random() * 8.0) + 1L);
                for (i = 0; i < numberLength; ++i) {
                    name = ((String)name).concat(Integer.toString(nextNumber));
                }
            } else if (numberMode == 1) {
                nextNumber = (int)(Math.round(Math.random() * 8.0) + 1L);
                name = ((String)name).concat(Integer.toString(nextNumber));
                for (i = 1; i < numberLength; ++i) {
                    name = ((String)name).concat("0");
                }
            } else if (numberMode == 2) {
                nextNumber = (int)(Math.round(Math.random() * 8.0) + 1L);
                name = ((String)name).concat(Integer.toString(nextNumber));
                for (i = 0; i < numberLength; ++i) {
                    nextNumber = (int)Math.round(Math.random() * 9.0);
                    name = ((String)name).concat(Integer.toString(nextNumber));
                }
            } else if (numberMode == 3) {
                nextNumber = 99999;
                while (Integer.toString(nextNumber).length() != numberLength) {
                    nextNumber = (int)(Math.round(Math.random() * 12.0) + 1L);
                    nextNumber = (int)Math.pow(2.0, nextNumber);
                }
                name = ((String)name).concat(Integer.toString(nextNumber));
            }
        }
        boolean bl = leet = !number && random.nextBoolean();
        if (leet) {
            Object oldName = name;
            while (((String)name).equals(oldName)) {
                int leetMode = (int)Math.round(Math.random() * 7.0);
                if (leetMode == 0) {
                    name = ((String)name).replace("a", "4");
                    name = ((String)name).replace("A", "4");
                }
                if (leetMode == 1) {
                    name = ((String)name).replace("e", "3");
                    name = ((String)name).replace("E", "3");
                }
                if (leetMode == 2) {
                    name = ((String)name).replace("g", "6");
                    name = ((String)name).replace("G", "6");
                }
                if (leetMode == 3) {
                    name = ((String)name).replace("h", "4");
                    name = ((String)name).replace("H", "4");
                }
                if (leetMode == 4) {
                    name = ((String)name).replace("i", "1");
                    name = ((String)name).replace("I", "1");
                }
                if (leetMode == 5) {
                    name = ((String)name).replace("o", "0");
                    name = ((String)name).replace("O", "0");
                }
                if (leetMode == 6) {
                    name = ((String)name).replace("s", "5");
                    name = ((String)name).replace("S", "5");
                }
                if (leetMode != 7) continue;
                name = ((String)name).replace("l", "7");
                name = ((String)name).replace("L", "7");
            }
        }
        int special = (int)Math.round(Math.random() * 8.0);
        switch (special) {
            case 3: {
                name = "xX".concat((String)name).concat("Xx");
                break;
            }
            case 4: {
                name = ((String)name).concat("LP");
                break;
            }
            case 5: {
                name = ((String)name).concat("HD");
                break;
            }
        }
        return name;
    }
}
