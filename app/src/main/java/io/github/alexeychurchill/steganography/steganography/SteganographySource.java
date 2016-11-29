package io.github.alexeychurchill.steganography.steganography;

import android.graphics.Bitmap;

/**
 * Pair Bitmap and Source text
 */

public class SteganographySource {
    private final Bitmap image;
    private final String text;

    public SteganographySource(Bitmap image, String text) {
        this.image = image;
        this.text = text;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getText() {
        return text;
    }
}
