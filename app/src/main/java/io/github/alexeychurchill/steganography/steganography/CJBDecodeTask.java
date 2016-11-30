package io.github.alexeychurchill.steganography.steganography;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import io.github.alexeychurchill.steganography.activities.ActionActivity;

import static android.content.ContentValues.TAG;

/**
 * Decode async task
 */

public class CJBDecodeTask extends AsyncTask<Bitmap, Double, String> {
    private ProgressListener progressListener;
    private TaskReadyListener taskReadyListener;
    private final int forecastArea;

    public CJBDecodeTask(int forecastArea) {
        this.forecastArea = forecastArea;
    }

    @Override
    protected String doInBackground(Bitmap... bitmaps) {
        if (bitmaps == null) {
            return null;
        }
        if (bitmaps.length < 1) {
            return null;
        }

        Bitmap sourceImage = bitmaps[0];
        StringBuilder outTextBuilder = new StringBuilder();

        int bitNumber = 0;

        long totalPixels = sourceImage.getWidth() * sourceImage.getHeight() * 8;
        long decodedPixels = 0;
        char currentCharacter = '\0';

        for (int y = 0; y < sourceImage.getHeight(); y++) {
            for (int x = 0; x < sourceImage.getWidth(); x++) {

                Log.d(ActionActivity.TAG, "doInBackground: x = " + x + ", y = " + y);
                Log.d(ActionActivity.TAG, "doInBackground: bit = " + bitNumber);

                int probableBlueValue = forecastBlueValue(sourceImage, x, y);
                int realBlueValue = Color.blue(sourceImage.getPixel(x, y));

                Log.d(ActionActivity.TAG, "doInBackground: probable = " + probableBlueValue + ", real = " + realBlueValue);

                if (realBlueValue > probableBlueValue) {
                    currentCharacter = Utils.setNBitValue(currentCharacter, bitNumber % 8, 1);
                } else {
                    currentCharacter = Utils.setNBitValue(currentCharacter, bitNumber % 8, 0);
                }

                bitNumber++;

                if (bitNumber > 7) {
                    if (currentCharacter == '\0') {
                        return outTextBuilder.toString();
                    } else {
                        outTextBuilder.append(currentCharacter);
                    }
                    bitNumber = 0;
                    currentCharacter = '\0';
                }

                // Progress
                decodedPixels++;
                publishProgress(1.0 * decodedPixels / totalPixels);
            }
        }

        return outTextBuilder.toString();
    }

    private int forecastBlueValue(Bitmap sourceImage, int x, int y) {
        int sumBlueValue = 0;
        int pixelCount = 0;

        // By x
        for (int areaNumber = -forecastArea; areaNumber <= forecastArea; areaNumber++) {
            if (areaNumber == 0) {
                continue;
            }
            int neighbourX = x + areaNumber;
            if ((neighbourX >= 0) && (neighbourX < sourceImage.getWidth())) {
                sumBlueValue += Color.blue(sourceImage.getPixel(neighbourX, y));
                pixelCount++;
            }
        }

        //By y
        for (int areaNumber = -forecastArea; areaNumber <= forecastArea; areaNumber++) {
            if (areaNumber == 0) {
                continue;
            }
            int neighbourY = y + areaNumber;
            if ((neighbourY >= 0) && (neighbourY < sourceImage.getHeight())) {
                sumBlueValue += Color.blue(sourceImage.getPixel(x, neighbourY));
                pixelCount++;
            }
        }

        return sumBlueValue / pixelCount;
    }

    private int getBlueValue(Bitmap sourceImage, int x, int y) {
        return Color.blue(sourceImage.getPixel(x, y));
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
