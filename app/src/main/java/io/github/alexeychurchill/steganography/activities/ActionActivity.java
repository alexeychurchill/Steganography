package io.github.alexeychurchill.steganography.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Locale;

import io.github.alexeychurchill.steganography.R;
import io.github.alexeychurchill.steganography.steganography.LSBDecodeTask;
import io.github.alexeychurchill.steganography.steganography.LSBEncodeTask;
import io.github.alexeychurchill.steganography.steganography.ProgressListener;
import io.github.alexeychurchill.steganography.steganography.SteganographySource;
import io.github.alexeychurchill.steganography.steganography.TaskReadyListener;
import io.github.alexeychurchill.steganography.steganography.Utils;

public class ActionActivity extends AppCompatActivity implements View.OnClickListener,
        ProgressListener, TaskReadyListener {
    private static final int REQUEST_CODE_GET_IMAGE = 1;
    private static final String TAG = "ActionActivity";
    private ProgressBar pbWorkProgress;
    private TextView tvImageStatus;
    private Switch switchDirection;
    private RadioGroup rgMethod;
    private RadioButton rbLsb;
    private RadioButton rbCjb;
    private EditText etText;
    private Button btnProceed;
    private Button btnLoadImage;

    private Bitmap mSourceBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action);
        initWidgets();
        pbWorkProgress.setProgress(0);
    }

    private void initWidgets() {
        pbWorkProgress = ((ProgressBar) findViewById(R.id.pbWorkProgress));
        btnLoadImage = ((Button) findViewById(R.id.btnLoadImage));
        if (btnLoadImage != null) {
            btnLoadImage.setOnClickListener(this);
        }
        tvImageStatus = ((TextView) findViewById(R.id.tvImageStatus));
        switchDirection = ((Switch) findViewById(R.id.switchDirection));
        rgMethod = ((RadioGroup) findViewById(R.id.rgMethod));
        rbLsb = ((RadioButton) findViewById(R.id.rbLsb));
        rbCjb = ((RadioButton) findViewById(R.id.rbCjb));
        etText = ((EditText) findViewById(R.id.etText));
        btnProceed = ((Button) findViewById(R.id.btnProceed));
        if (btnProceed != null) {
            btnProceed.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnLoadImage:
                callLoadImage();
                break;
            case R.id.btnProceed:
                proceed();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == REQUEST_CODE_GET_IMAGE) && (resultCode == RESULT_OK)) {
            if (data == null) {
                return;
            }

            tvImageStatus.setText(R.string.text_no_image);

            Uri imageUri = data.getData();
            try {
                mSourceBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            } catch (IOException e) {
                Toast.makeText(this, R.string.text_toast_image_io_exception, Toast.LENGTH_LONG)
                        .show();
                return;
            }

            tvImageStatus.setText(
                    "Image: "
                    .concat(String.valueOf(mSourceBitmap.getWidth()))
                    .concat(" x ")
                    .concat(String.valueOf(mSourceBitmap.getHeight()))
            );

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void callLoadImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE_GET_IMAGE);
    }

    private void proceed() {
        if (mSourceBitmap == null) {
            Toast.makeText(this, R.string.text_toast_no_image, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        // Direction
        boolean showHiddenText = switchDirection.isChecked();

        // LSB
        if (rbLsb.isChecked()) {
            if (showHiddenText) {
                showLsb();
            } else {
                SteganographySource steganographySource = new SteganographySource(
                        mSourceBitmap, etText.getText().toString().concat("\0")
                );
                hideLsb(steganographySource);
            }
        }

        if (rbCjb.isChecked()) {
            Toast.makeText(this, R.string.text_toast_will_be_implemented_soon, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        setControlsEnabled(false);
    }

    private void hideLsb(SteganographySource source) {
        // Common text source check
        if (!sourceTextIsOk(source)) {
            return;
        }

        // Length check, specific for LSB
        long pixels = source.getImage().getWidth() * source.getImage().getHeight();
        if (pixels < source.getText().length()) {
            Toast.makeText(this, R.string.text_toast_string_limit_exceeded, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        LSBEncodeTask lsbEncodeTask = new LSBEncodeTask();
        lsbEncodeTask.setProgressListener(this);
        lsbEncodeTask.setTaskReadyListener(this);
        lsbEncodeTask.execute(source);
    }

    private boolean sourceTextIsOk(SteganographySource source) {
        if (source.getText().isEmpty()) {
            Toast.makeText(this, R.string.text_toast_string_empty, Toast.LENGTH_SHORT)
                    .show();
            return false;
        }

        // non-ascii check
        if (Utils.isContainsNonAscii(source.getText())) {
            Toast.makeText(this, R.string.text_toast_contains_non_ascii, Toast.LENGTH_SHORT)
                    .show();
            return false;
        }
        return true;
    }


    private void showLsb() {
        LSBDecodeTask lsbDecodeTask = new LSBDecodeTask();
        lsbDecodeTask.setProgressListener(this);
        lsbDecodeTask.setTaskReadyListener(this);
        lsbDecodeTask.execute(mSourceBitmap);
    }

    @Override
    public void onProgressChanged(double progress) {
        pbWorkProgress.setProgress((int) (100 * progress));
    }

    @Override
    public void onHideTaskReady(Bitmap bitmap) {
        if (bitmap == null) {
            Toast.makeText(this, R.string.text_toast_smth_gone_wrong, Toast.LENGTH_SHORT)
                    .show();
        } else {
            File dcimDirectoryFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            // Creating dir if necessary
            File stenoDir = new File(dcimDirectoryFile, "Steganography/Hidden/");
            if (!stenoDir.exists()) {
                if (!stenoDir.mkdirs()) {
                    Toast.makeText(this, R.string.text_toast_dir_creation_fail, Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
            }
            // Saving file
            String outFileName = "Steganography/Hidden/".concat(
                    String.format(Locale.getDefault(), "%1$tT-%1$tF", Calendar.getInstance())
            ).concat(".png");
            File bitmapFile = new File(dcimDirectoryFile, outFileName);
            OutputStream outputStream;
            try {
                outputStream = new FileOutputStream(bitmapFile);
                // Saving as PNG
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            } catch (FileNotFoundException e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT)
                        .show();
            }
        }
        setControlsEnabled(true);
        Toast.makeText(this, R.string.text_toast_finished, Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public void onShowTaskReady(String result) {
        if (result == null) {
            Toast.makeText(this, R.string.text_toast_smth_gone_wrong, Toast.LENGTH_SHORT)
                    .show();
        } else {
            etText.setText(result);
        }
        setControlsEnabled(true);
    }

    private void setControlsEnabled(boolean enabled) {
        btnProceed.setEnabled(enabled);
        btnLoadImage.setEnabled(enabled);
        switchDirection.setEnabled(enabled);
        rgMethod.setEnabled(enabled);
        etText.setEnabled(enabled);
    }
}
