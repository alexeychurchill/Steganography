package io.github.alexeychurchill.steganography.steganography;

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
}
