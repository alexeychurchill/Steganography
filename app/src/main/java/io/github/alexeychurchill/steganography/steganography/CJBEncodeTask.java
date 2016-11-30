package io.github.alexeychurchill.steganography.steganography;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;

/**
 * CJB encode task
 *
 * USES BLUE CHANNEL!
 *
 *
 */

public class CJBEncodeTask extends AsyncTask <SteganographySource, Double, Bitmap> {
    private ProgressListener progressListener;
    private TaskReadyListener taskReadyListener;
    private final double bitEnergy;

    public CJBEncodeTask(double bitEnergy) {
        this.bitEnergy = bitEnergy;
    }

    @Override
    protected Bitmap doInBackground(SteganographySource... steganographySources) {
        if (steganographySources == null) {
            return null;
        }
        if (steganographySources.length < 1) {
            return null;
        }
        SteganographySource source = steganographySources[0];

        String sourceText = source.getText();
        Bitmap sourceImage = source.getImage();

        if ((sourceText == null) || (sourceImage == null)) {
            return null;
        }

        Bitmap outImage = sourceImage.copy(Bitmap.Config.ARGB_8888, true);

        long totalPixels = sourceText.length() * 8; // 1 bit per pixel
        long encodedPixels = 0;

        int x = 0, y = 0;

        for (char character : sourceText.toCharArray()) {
            for (int charBitNumber = 0; charBitNumber < 8; charBitNumber++) {
                int color = sourceImage.getPixel(x, y);
                int colorBrightness = Utils.getBrightness(color);
                int blueValue = Color.blue(color);

                int bitValue = Utils.getNBitValue(character, charBitNumber);

                if (bitValue == 1) {
                    blueValue += (int) (bitEnergy * colorBrightness);
                } else {
                    blueValue -= (int) (bitEnergy * colorBrightness);
                }

                color = Color.rgb(
                        Color.red(color), Color.green(color), blueValue
                );

                outImage.setPixel(x, y, color);

                // Another pixel
                x++;
                if (x >= sourceImage.getWidth()) {
                    x = 0;
                    y++;
                }

                // Progress
                encodedPixels++;
                double progress = 1.0 * encodedPixels / totalPixels;
                publishProgress(progress);
            }
        }

        return outImage;
    }

    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
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
