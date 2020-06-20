package samuelpalmer.sensorautorotation.deviceorientation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventCallback;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.annotation.AnyThread;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.core.content.ContextCompat;
import androidx.core.os.HandlerCompat;
import androidx.test.platform.app.InstrumentationRegistry;

import java.util.Arrays;

class SensorPoller {

    private static String TAG = SensorPoller.class.getSimpleName();

    /**
     * Fetches the current value of the given sensor
     */
    @UiThread
    public static float[] pollSensor(int sensorType) {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        SensorManager sensorManager = ContextCompat.getSystemService(context, SensorManager.class);
        assert sensorManager != null;
        Sensor sensor = sensorManager.getDefaultSensor(sensorType);
        if (sensor == null)
            throw new RuntimeException("Sensor not found");

        SensorListenerThread sensorThread = new SensorListenerThread();
        sensorThread.start();
        Handler sensorHandler = HandlerCompat.createAsync(sensorThread.getLooper());

        boolean registered = sensorManager.registerListener(sensorThread.listener, sensor, SensorManager.SENSOR_DELAY_FASTEST, sensorHandler);
        if (!registered)
            throw new RuntimeException("Failed to register for sensor");

        try {
            sensorThread.join(5000);
            if (sensorThread.isAlive())
                throw new RuntimeException("Sensor thread hasn't died");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        sensorManager.unregisterListener(sensorThread.listener);

        return sensorThread.getValues();
    }

    @WorkerThread
    private static class SensorListenerThread extends HandlerThread {

        private volatile float[] values = null;

        private SensorListenerThread() {
            super("Sensor listener");
        }

        @AnyThread
        private float[] getValues() {
            if (values == null)
                throw new RuntimeException("Haven't got values yet");

            return values;
        }

        private final SensorEventCallback listener = new SensorEventCallback() {

            @Override
            @WorkerThread
            public void onSensorChanged(SensorEvent event) {
                super.onSensorChanged(event);

                if (values == null) {
                    values = event.values.clone();
                    Log.d(TAG, "Got sensor values: " + Arrays.toString(values));
                    quitSafely();
                }
            }

        };

    }

    private SensorPoller() {}

}
