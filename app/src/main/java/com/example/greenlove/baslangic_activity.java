package com.example.greenlove;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Window;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class baslangic_activity extends AppCompatActivity {

    private Button buttonIlerle;
    private Switch switchBluetooth;
    private Switch switchBatteryOptimization;
    private Switch switchDozeMode;
    private Switch switchNotification;
    private Switch switchPhotoAccess;

    private PowerManager powerManager;

    private static final int REQUEST_BLUETOOTH = 1001;
    private static final int REQUEST_NOTIFICATION = 1002;
    private static final int REQUEST_PHOTO_ACCESS = 1003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baslangic);

        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        buttonIlerle = findViewById(R.id.buttonilerle);
        switchBluetooth = findViewById(R.id.switchBluetooth);
        switchBatteryOptimization = findViewById(R.id.switchBatteryOptimization);
        switchDozeMode = findViewById(R.id.switchDoze);
        switchNotification = findViewById(R.id.switchNotification);
        switchPhotoAccess = findViewById(R.id.switchFilePermission);

        updateSwitchStates();
        setSwitchListeners();
        if (areAllPermissionsGranted()) {
            Intent intent = new Intent(baslangic_activity.this, MainActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Lütfen tüm izinleri verin.", Toast.LENGTH_LONG).show();
        }
        buttonIlerle.setOnClickListener(v -> {
            if (areAllPermissionsGranted()) {
                Intent intent = new Intent(baslangic_activity.this, MainActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Lütfen tüm izinleri verin.", Toast.LENGTH_LONG).show();
            }
        });
        Window window = getWindow();
        window.setNavigationBarColor(Color.BLACK); // Navigation bar rengini siyah yap
    }

    private void updateSwitchStates() {
        // Bluetooth izinleri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+ için izinler
            boolean bluetoothConnectGranted = checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
            boolean bluetoothScanGranted = checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
            switchBluetooth.setChecked(bluetoothConnectGranted && bluetoothScanGranted);
        } else { // Android 12'den önce
            boolean locationPermissionGranted = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            switchBluetooth.setChecked(locationPermissionGranted);
        }


    // Pil optimizasyonu
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Android 6+
            switchBatteryOptimization.setChecked(powerManager.isIgnoringBatteryOptimizations(getPackageName()));
        } else {
            switchBatteryOptimization.setChecked(true);
            switchBatteryOptimization.setEnabled(false);
        }

        // Doze modu
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Android 6+
            switchDozeMode.setChecked(!powerManager.isDeviceIdleMode());
        } else {
            switchDozeMode.setChecked(true);
            switchDozeMode.setEnabled(false);
        }

        // Bildirim izni
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            switchNotification.setChecked(checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED);
        } else {
            switchNotification.setChecked(true);
            switchNotification.setEnabled(false);
        }

        // Fotoğraflar ve videolar erişim izni
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            switchPhotoAccess.setChecked(checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED);
        } else {
            switchPhotoAccess.setChecked(true);
            switchPhotoAccess.setEnabled(false);
        }
    }

    private void setSwitchListeners() {
        // Bluetooth izinleri
        switchBluetooth.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                handleBluetoothPermissions();
            }
        });

        // Pil optimizasyonu
        switchBatteryOptimization.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                handleBatteryOptimization();
            }
        });

        // Doze modu
        switchDozeMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                handleBackgroundExecutionPermission();
            }
        });

        // Bildirim izni
        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                handleNotificationPermission();
            }
        });

        // Fotoğraflar ve videolar erişim izni
        switchPhotoAccess.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                handlePhotoAccessPermission();
            }
        });
    }

    private void handleBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            requestPermissions(
                    new String[]{
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN
                    },
                    REQUEST_BLUETOOTH
            );
        } else { // Android 6 - 11
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_BLUETOOTH
            );
        }
    }


    private void handleBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Pil optimizasyon ayarlarına erişilemiyor.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleBackgroundExecutionPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Arka plan çalışma izin ayarlarına erişilemiyor.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Uygulama zaten pil optimizasyonundan muaf.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION);
        } else {
            Toast.makeText(this, "Bildirim izinleri bu sürümde gerekmiyor.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePhotoAccessPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_PHOTO_ACCESS);
        } else {
            Toast.makeText(this, "Fotoğraf erişim izinleri bu sürümde gerekmiyor.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean areAllPermissionsGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
                return false;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        updateSwitchStates();
    }
}
