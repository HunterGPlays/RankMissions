package net.terrocidepvp.rankmissions.utils;

import java.util.ArrayList;
import java.util.List;

public class ColorCodeUtil {
    private static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            // Check for color codes.
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i+1]) > -1) {
                // Replace character.
                b[i] = '§';
                // Make sure the color code is lower case.
                b[i+1] = Character.toLowerCase(b[i+1]);
            }
        }
        return new String(b);
    }

    public static String translate(String string) {
        return translateAlternateColorCodes('&', string);
    }

    public static List<String> translate(List<String> string) {
        ArrayList<String> temp = new ArrayList<>();
        string.forEach(str -> temp.add(translate(str)));
        return temp;
    }
}
