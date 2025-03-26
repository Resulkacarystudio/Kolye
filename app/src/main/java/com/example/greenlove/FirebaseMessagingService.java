package com.example.greenlove;



import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.UUID;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final UUID SERVICE_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E"); // Nordic UART Service UUID
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E"); // TX Characteristic UUID
    private static final String TAG = "FirebaseMessagingServic";
    // private static final UUID SERVICE_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    private static final UUID WRITE_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    private static final UUID NOTIFY_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");

    private BroadcastReceiver bluetoothCommandReceiver;
    private PowerManager.WakeLock wakeLock;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        acquireWakeLock();
        String title = null;
        String body = null;

        // Data yükü
        if (remoteMessage.getData().size() > 0) {
            Log.d("FCM", "Data Yükü: " + remoteMessage.getData());
            String customKey = remoteMessage.getData().get("customKey");
            title = remoteMessage.getData().get("title");
            body = remoteMessage.getData().get("body");
            Log.d("FCM", "Custom Key: " + customKey + ", Başlık: " + title + ", İçerik: " + body);
        }
        sendKToBluetoothDevice312();
        playNotificationSoundAndVibration();

        if (title != null && body != null) {
            createAndShowNotification(title, body);
        }
        releaseWakeLock();
    }


    private void playNotificationSoundAndVibration() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    private void createAndShowNotification(String title, String message) {
        String channelId = "default_channel_id";
        String channelName = "Default Channel";

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Android Oreo (API 26) ve üzeri için bildirim kanalı oluştur
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.logocircle) // Küçük simge
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo_circle)) // Büyük simge
                .setPriority(NotificationCompat.PRIORITY_MAX);


        // Bildirime tıklanınca açılacak aktiviteyi ayarlayın
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                1,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        builder.setContentIntent(pendingIntent);

        // Bildirimi göster
        notificationManager.notify(100, builder.build());
    }


    /*
        private void sendKToBluetoothDevice() {
            SharedPreferences sharedPreferences = getSharedPreferences("BluetoothPreferences", Context.MODE_PRIVATE);
            String deviceAddress = sharedPreferences.getString("connectedDeviceAddress", null);

            if (deviceAddress != null) {
                Intent intent = new Intent(this, BluetoothService.class);
                intent.setAction("SEND_K");
                intent.putExtra("device_address", deviceAddress);
                startService(intent); // BluetoothService'e 'k' gönderilmesi için intent başlat

                Log.d(TAG, "BluetoothService'e 'k' gönderilmesi için intent başlatıldı.");
            } else {
                Log.e(TAG, "Bluetooth cihazı bağlı değil.");
            }
        }
    */



    // Bluetooth'un bağlı olup olmadığını kontrol eden yardımcı metod
    private boolean isBluetoothConnected() {
        SharedPreferences sharedPreferences = getSharedPreferences("BluetoothPreferences", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("isConnected", false); // Bağlantı durumunu kontrol et
    }

    private boolean bledurumu() {
        SharedPreferences sharedPreferences = getSharedPreferences("bledurum", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("bledurumu", false); // Bağlantı durumunu kontrol et
    }



    private void sendKToBluetoothDevice312() {
        BluetoothForegroundService service = BluetoothForegroundService.getInstance();
        System.out.println("girdi1");
        if (service != null) {
            System.out.println("girdi2");
            BluetoothGatt gatt = service.getBluetoothGatt();
            System.out.println("girdi3");
            if (gatt != null) {
                System.out.println("girdi4");
                BluetoothGattService targetService = gatt.getService(SERVICE_UUID);
                System.out.println("girdi5");
                if (targetService != null) {
                    System.out.println("girdi6");
                    BluetoothGattCharacteristic characteristic = targetService.getCharacteristic(CHARACTERISTIC_UUID);
                    System.out.println("girdi7");
                    if (characteristic != null) {
                        System.out.println("girdi8");
                        characteristic.setValue("k");
                        System.out.println("girdi9");
                        boolean success = gatt.writeCharacteristic(characteristic);
                        System.out.println("girdi10");
                        if (success) {
                            Log.d(TAG, "BLE cihazına 'k' harfi başarıyla gönderildi.");
                        } else {
                            Log.e(TAG, "BLE cihazına 'k' harfi gönderilemedi.");
                        }
                    } else {
                        Log.e(TAG, "Karakteristik bulunamadı. UUID'yi kontrol edin.");
                    }
                } else {
                    Log.e(TAG, "Hedef servis bulunamadı. UUID'yi kontrol edin.");
                }
            } else {
                Log.e(TAG, "BluetoothGatt bağlantısı mevcut değil.");
            }
        } else {
            Log.e(TAG, "BluetoothForegroundService çalışmıyor.");
        }
    }


    private boolean getBluetoothConnectionState() {
        SharedPreferences sharedPreferences = getSharedPreferences("BluetoothPreferences", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("isConnected", false); // Varsayılan değer: false
    }

    private void acquireWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakeLockTag");
            wakeLock.acquire(1 * 60 * 1000L /*1 dakika*/); // WakeLock'u 10 dakika tut
            Log.d("WakeLock", "WakeLock alındı.");
        }
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            Log.d("WakeLock", "WakeLock serbest bırakıldı.");
        }
    }
}