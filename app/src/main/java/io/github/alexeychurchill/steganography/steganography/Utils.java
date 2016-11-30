package io.github.alexeychurchill.steganography.steganography;

import android.graphics.Color;

/**
 * Additional utils
 */

public final class Utils {
    public static boolean isContainsNonAscii(String input) {
        for (char character : input.toCharArray()) {
            if (((int) character) > 255) {
                return true;
            }
        }
        return false;
    }

    /*
    * Max size - 8 bit
    * */
    public static int getNBitValue(char character, int n) {
        int intChar = (int) character;
        if (n > 7) {
            return 0;
        }
        int mask = 1 << n;
        return (intChar & mask) >> n;
    }

    /*
    * Max size - 8 bit
    * */
    public static char setNBitValue(char character, int n, int value) {
        int intChar = (int) character;
        if (n > 7) {
            return character;
        }
        if (value > 0) {
            intChar = intChar | (1 << n);
        } else {
            intChar = intChar & (255 ^ (1 << n));
        }
        return (char) intChar;
    }

    public static int getBrightness(int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        return (int) (0.3 * red + 0.59 * green + 0.11 * blue);
    }
}
