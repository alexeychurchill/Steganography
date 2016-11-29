package io.github.alexeychurchill.steganography.steganography;

import android.graphics.Bitmap;

/**
 * TaskReadyListener listener
 */

public interface TaskReadyListener {
    void onHideTaskReady(Bitmap bitmap);
    void onShowTaskReady(String result);
}
