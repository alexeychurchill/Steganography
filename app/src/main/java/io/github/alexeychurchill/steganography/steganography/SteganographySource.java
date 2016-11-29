package io.github.alexeychurchill.steganography.steganography;

import android.graphics.Bitmap;

/**
 * Pair Bitmap and Source text
 */

public class SteganographySource {
    private final Bitmap image;
    private final String source;

    public SteganographySource(Bitmap image, String source) {
        this.image = image;
        this.source = source;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getSource() {
        return source;
    }
}
