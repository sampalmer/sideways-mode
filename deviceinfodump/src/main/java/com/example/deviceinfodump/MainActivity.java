package com.example.deviceinfodump;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.PackageInfoCompat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String NOTIFICATION_CHANNEL_ID = "notifications";
    private static final String PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NotificationManager notificationManager = ContextCompat.getSystemService(this, NotificationManager.class);
        assert notificationManager != null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (ContextCompat.checkSelfPermission(this, PERMISSION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{PERMISSION}, 0);
        else
            dumpNotificationLayout(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (permissions.length == 1 && permissions[0].equals(PERMISSION))
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                dumpNotificationLayout(this);
            else
                finish();
    }

    private void dumpNotificationLayout(Context context) {
        String deviceInfo = getDeviceInfo();

        ClipboardManager clipboardManager = ContextCompat.getSystemService(context, ClipboardManager.class);
        assert clipboardManager != null;
        clipboardManager.setPrimaryClip(ClipData.newPlainText("Device info", deviceInfo));
        String outFilePath = FileUtils.writeToDownloads(context, deviceInfo, "device_info.txt", "text/plain");
        Toast.makeText(context, "Device info dumped to clipboard and " + outFilePath, Toast.LENGTH_LONG).show();
    }

    private String getDeviceInfo() {
        StringBuilder wip = new StringBuilder();

        Field[] buildFields = Build.class.getFields();
        for (Field field : buildFields) {
            if (Modifier.isStatic(field.getModifiers())) {
                Object value;
                try {
                    value = field.get(null);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                wip
                        .append("Build.")
                        .append(field.getName())
                        .append(": ")
                        .append(value)
                        .append("\n");
            }
        }

        Field[] buildVersionFields = Build.VERSION.class.getFields();
        for (Field field : buildVersionFields) {
            if (Modifier.isStatic(field.getModifiers())) {
                Object value;
                try {
                    value = field.get(null);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                wip
                        .append("Build.VERSION.")
                        .append(field.getName())
                        .append(": ")
                        .append(value)
                        .append("\n");
            }
        }

        Properties systemProperties = System.getProperties();
        Set<String> propertyNames = systemProperties.stringPropertyNames();
        for (String propertyName : propertyNames) {
            String propertyValue = systemProperties.getProperty(propertyName);
            wip
                    .append("System.getProperty(\"")
                    .append(propertyName)
                    .append("\"): ")
                    .append(propertyValue)
                    .append("\n");
        }

        FeatureInfo[] features = getPackageManager().getSystemAvailableFeatures();
        for (FeatureInfo feature : features) {
            wip
                    .append(feature)
                    .append("\n");
        }

        List<PackageInfo> installedPackages = getPackageManager().getInstalledPackages(0);
        for (PackageInfo installedPackage : installedPackages) {
            wip
                    .append("Package: ")
                    .append(installedPackage.packageName)
                    .append("/")
                    .append(installedPackage.versionName)
                    .append("/")
                    .append(PackageInfoCompat.getLongVersionCode(installedPackage))
                    .append("\n")
            ;
        }

        List<ApplicationInfo> installedApps = getPackageManager().getInstalledApplications(0);
        for (ApplicationInfo installedApp : installedApps) {
            wip
                    .append("App: ")
                    .append(installedApp.packageName)
                    .append("/")
                    .append(installedApp.loadLabel(getPackageManager()))
                    .append("\n")
            ;
        }

        return wip.toString();
    }

}
