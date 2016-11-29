package io.github.alexeychurchill.steganography.steganography;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;

/**
 * Least significant bit encoding task
 */

public class LSBEncodeTask extends AsyncTask<SteganographySource, Double, Bitmap> {
    private ProgressListener progressListener;
    private TaskReadyListener taskReadyListener;

    @Override
    protected Bitmap doInBackground(SteganographySource... steganographySources) {
        if (steganographySources == null) {
            return null;
        }
        if (steganographySources.length < 1) {
            return null;
        }

        Bitmap inputBitmap = steganographySources[0].getImage();

        if (inputBitmap == null) {
            return null;
        }

        int sourceWidth = inputBitmap.getWidth();
        int sourceHeight = inputBitmap.getWidth();
        long totalPixels = sourceWidth * sourceHeight;
        long encodedPixels = 0;
//        Bitmap outputBitmap = Bitmap
//                .createBitmap(sourceWidth, sourceHeight, Bitmap.Config.ARGB_8888);
        Bitmap outputBitmap = inputBitmap.copy(Bitmap.Config.ARGB_8888, true);
        String source = steganographySources[0].getSource();

        if (source == null) {
            return null;
        }

        int x = 0, y = 0;

        for (char character : source.toCharArray()) {
            int color = outputBitmap.getPixel(x, y);
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);
            // RGB888 -> RGB565
            red /= 8;
            green /= 4;
            blue /= 8;
            // Character components of color
            int charRed = (((int) character) & 0xE0) >> 5;
            int charGreen = (((int) character) & 0x18) >> 3;
            int charBlue = (((int) character) & 0x7);
            // Colors transforming
            red = (red << 3) & charRed;
            green = (green << 2) & charGreen;
            blue = (blue << 3) & charBlue;
            color = Color.rgb(red, green, blue);
            outputBitmap.setPixel(x, y, color);
            // Image pixel incrementation
            if (x >= sourceWidth) {
                x = 0;
                y++;
            }
            // Progress
            encodedPixels++;
            double progress = 1.0 * encodedPixels / totalPixels;
            publishProgress(progress);
        }
        return outputBitmap;
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
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (taskReadyListener != null) {
            taskReadyListener.onHideTaskReady(bitmap);
        }
    }
}
