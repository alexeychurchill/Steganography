package io.github.alexeychurchill.steganography.steganography;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

/**
 * LSB unhider
 */

public class LSBDecodeTask extends AsyncTask<Bitmap, Double, String> {
    private ProgressListener progressListener;
    private TaskReadyListener taskReadyListener;

    @Override
    protected String doInBackground(Bitmap... params) {
        if (params == null) {
            return null;
        }
        if (params.length < 1) {
            return null;
        }
        Bitmap inputBitmap = params[0];
        if (inputBitmap == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        int sourceWidth = inputBitmap.getWidth();
        int sourceHeight = inputBitmap.getWidth();
        long totalPixels = sourceWidth * sourceHeight;
        long decodedPixels = 0;

        for (int y = 0; y < sourceHeight; y++) {
            for (int x = 0; x < sourceWidth; x++) {
                int color = inputBitmap.getPixel(x, y);
                int red = Color.red(color);
                int green = Color.green(color);
                int blue = Color.blue(color);
                red = (red & 0x7) << 5;
                green = (green & 0x3) << 3;
                blue = blue & 0x7;
                char character = (char) (red | green | blue);
                // Message read
                if (character == '\0') {
                    return builder.toString();
                }
                builder.append(character);
                decodedPixels++;
                publishProgress(1.0 * decodedPixels / totalPixels);
            }
        }

        return builder.toString();
    }

    public void setProgressListener(ProgressListener listener) {
        this.progressListener = listener;
    }

    public void setTaskReadyListener(TaskReadyListener taskReadyListener) {
        this.taskReadyListener = taskReadyListener;
    }

    @Override
    protected void onProgressUpdate(Double... values) {
        super.onProgressUpdate(values);
        if (values.length < 1) {
            return;
        }
        double value = values[0];
        if (progressListener != null) {
            progressListener.onProgressChanged(value);
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (taskReadyListener != null) {
            taskReadyListener.onShowTaskReady(s);
        }
    }
}
