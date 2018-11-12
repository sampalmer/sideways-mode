// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package samuelpalmer.facedetectiontester.java;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.ToggleButton;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;
import com.google.android.gms.common.annotation.KeepName;
import samuelpalmer.facedetectiontester.R;
import samuelpalmer.facedetectiontester.common.CameraSource;
import samuelpalmer.facedetectiontester.common.CameraSourcePreview;
import samuelpalmer.facedetectiontester.common.GraphicOverlay;
import samuelpalmer.facedetectiontester.java.facedetection.FaceDetectionProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Demo app showing the various features of ML Kit for Firebase. This class is used to
 * set up continuous frame processing on frames from a camera source.
 */
@KeepName
public final class LivePreviewActivity extends AppCompatActivity
        implements OnRequestPermissionsResultCallback,
        CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "LivePreviewActivity";
    private static final int PERMISSION_REQUESTS = 1;

    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    private SeekBar screenBrightnessSlider;
    private SeekBar screenOverlaySlider;
    private SeekBar isoSlider;
    private SeekBar exposureCompensationSlider;
    private SeekBar exposureTimeSlider;
    private SeekBar autoLevelAmountSlider;
    private WindowManager windowManager;
    private View overlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_live_preview);

        preview = findViewById(R.id.firePreview);
        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }
        graphicOverlay = findViewById(R.id.fireFaceOverlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }

        ToggleButton facingSwitch = findViewById(R.id.facingSwitch);
        facingSwitch.setOnCheckedChangeListener(this);

        windowManager = ContextCompat.getSystemService(this, WindowManager.class);

        overlay = new View(this);
        overlay.setBackgroundColor(Color.WHITE);

        WindowManager.LayoutParams overlayParams = new WindowManager.LayoutParams();
        overlayParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        overlayParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        overlayParams.format = PixelFormat.TRANSLUCENT;
        overlayParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        overlayParams.height = WindowManager.LayoutParams.MATCH_PARENT;

        windowManager.addView(overlay, overlayParams);

        screenBrightnessSlider = findViewById(R.id.screenBrightness);
        screenOverlaySlider = findViewById(R.id.screenOverlay);
        isoSlider = findViewById(R.id.iso);
        exposureCompensationSlider = findViewById(R.id.exposureCompensation);
        exposureTimeSlider = findViewById(R.id.exposureTime);
        autoLevelAmountSlider = findViewById(R.id.autoLevelAmount);

        screenBrightnessSlider.setMax(Integer.MAX_VALUE);
        screenBrightnessSlider.setOnSeekBarChangeListener(new SimpleSeekChangeListener(progress -> setScreenBrightness((float) progress / Integer.MAX_VALUE)));
        setScreenBrightness(0.5f);
        screenBrightnessSlider.setProgress(screenBrightnessSlider.getMax() / 2);

        screenOverlaySlider.setMax(Integer.MAX_VALUE);
        screenOverlaySlider.setOnSeekBarChangeListener(new SimpleSeekChangeListener(progress -> overlay.setAlpha((float) progress / Integer.MAX_VALUE)));
        overlay.setAlpha(0);
        screenOverlaySlider.setProgress(0);

        isoSlider.setOnSeekBarChangeListener(new SimpleSeekChangeListener(progress -> cameraSource.setIso(cameraSource.getSupportedIsos()[progress])));
        exposureCompensationSlider.setOnSeekBarChangeListener(new SimpleSeekChangeListener(progress -> cameraSource.setExposureCompensation(progress + cameraSource.getMinExposureCompensation())));
        exposureTimeSlider.setOnSeekBarChangeListener(new SimpleSeekChangeListener(progress -> cameraSource.setExposureTime(progress == 0 ? null : (progress - 1) / 1000000.0 + cameraSource.getMinExposureTime())));

        createCameraSource();

        autoLevelAmountSlider.setMax(Integer.MAX_VALUE);
        autoLevelAmountSlider.setOnSeekBarChangeListener(new SimpleSeekChangeListener(progress -> cameraSource.setAutoLevelAmount((float) progress / Integer.MAX_VALUE)));
        cameraSource.setAutoLevelAmount(0.5f);
        autoLevelAmountSlider.setProgress(autoLevelAmountSlider.getMax() / 2);

        if (allPermissionsGranted()) {
            startCameraSource();
        } else {
            getRuntimePermissions();
        }
    }

    private void setScreenBrightness(float screenBrightness) {
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.screenBrightness = screenBrightness;
        window.setAttributes(attributes);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "Set facing");
        if (cameraSource != null) {
            if (!isChecked) {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
            } else {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
            }
        }
        preview.stop();

        if (allPermissionsGranted())
            startCameraSource();
    }

    private void createCameraSource() {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
            cameraSource.setOnStateChangeListener(cameraStateChangeListener);
        }

        Log.i(TAG, "Using Face Detector Processor");
        cameraSource.setMachineLearningFrameProcessor(new FaceDetectionProcessor());
    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource.setOnStateChangeListener(null);
                cameraSource = null;
            }
        }
    }

    private final CameraSource.StateChangeListener cameraStateChangeListener = new CameraSource.StateChangeListener() {
        @Override
        public void onStateChanged() {
            if (cameraSource.isStarted()) {
                String[] supportedIsos = cameraSource.getSupportedIsos();

                if (supportedIsos == null) {
                    isoSlider.setEnabled(false);
                }
                else {
                    isoSlider.setEnabled(true);

                    isoSlider.setMax(supportedIsos.length - 1);

                    int isoIndex = Arrays.asList(supportedIsos).indexOf(cameraSource.getIso());
                    if (isoIndex == -1)
                        throw new RuntimeException("Camera's ISO value is not in the supported list: " + cameraSource.getIso());

                    isoSlider.setProgress(isoIndex);
                }

                int maxExposureCompensation = cameraSource.getMaxExposureCompensation();
                int minExposureCompensation = cameraSource.getMinExposureCompensation();

                if (minExposureCompensation == 0 && maxExposureCompensation == 0) {
                    exposureCompensationSlider.setEnabled(false);
                }
                else {
                    exposureCompensationSlider.setEnabled(true);
                    exposureCompensationSlider.setMax(maxExposureCompensation - minExposureCompensation);
                    exposureCompensationSlider.setProgress(cameraSource.getExposureCompensation() - minExposureCompensation);
                }

                Double maxExposureTime = cameraSource.getMaxExposureTime();
                Double minExposureTime = cameraSource.getMinExposureTime();

                if (maxExposureTime == null || minExposureTime == null) {
                    exposureTimeSlider.setEnabled(false);
                }
                else {
                    exposureTimeSlider.setEnabled(true);
                    exposureTimeSlider.setMax((int) ((maxExposureTime - minExposureTime) * 1000000 + 1));

                    Double exposureTime = cameraSource.getExposureTime();
                    exposureTimeSlider.setProgress(exposureTime == null ? 0 : (int) (exposureTime * 1000000 + 1));
                }
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        if (allPermissionsGranted())
            startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }

        windowManager.removeView(overlay);
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "Permission granted!");
        if (allPermissionsGranted()) {
            createCameraSource();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }
}
