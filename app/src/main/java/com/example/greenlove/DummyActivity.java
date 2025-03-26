package com.example.greenlove;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.companion.CompanionDeviceManager;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

public class DummyActivity extends Activity {
    public static BluetoothForegroundService instance;

    private static final String TAG = "DummyActivity";

    /*

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "DummyActivity started");

        // BluetoothForegroundService'den gelen chooserLauncher intentini çalıştır
        Intent intent = getIntent();
       // instance = this; // Servis örneğini kaydet

        IntentSender sender = intent.getParcelableExtra("chooserLauncher");
        try {
            startIntentSenderForResult(sender, 0, null, 0, 0, 0);
        } catch (Exception e) {
            Log.e(TAG, "Cihaz seçim ekranı başlatılamadı: " + e.getMessage());
        }
    }
    public static BluetoothForegroundService getInstance() {
        if (instance == null) {
            Log.e(TAG, "BluetoothForegroundService instance is null. Ensure the service is running.");
        }
        return instance;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == RESULT_OK && data != null) {
            BluetoothDevice device = data.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE);
            if (device != null) {
                Log.d(TAG, "Eşleşen cihaz: " + device.getName());
                BluetoothForegroundService.getInstance().connectToDevice(device);
            }
        }
        finish();
    }

     */
}
