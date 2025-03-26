package com.example.greenlove;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothService extends Service {

    private static final String CHANNEL_ID = "BluetoothServiceChannel"; // Bildirim kanalı ID'si
    private BluetoothAdapter bluetoothAdapter; // Bluetooth Adapter'i
    private BluetoothSocket bluetoothSocket; // Bluetooth soketi
    private InputStream inputStream; // Gelen veriyi okumak için InputStream
    private OutputStream outputStream; // Veriyi göndermek için OutputStream
    private BluetoothDevice bluetoothDevice; // Bluetooth cihazı
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // HC-05 için UUID
    private Handler handler; // Periyodik işlem yapmak için Handler
    private Runnable sendKeepAliveRunnable; // Keep-alive mesajı için Runnable

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel(); // Bildirim kanalı oluşturulur
        handler = new Handler(Looper.getMainLooper()); // Handler ana iş parçacığına bağlanır
        requestIgnoreBatteryOptimizations(); // Battery optimizasyonlarından kaçınma isteği
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // Cihazın Bluetooth adaptörü alınır

        if (intent != null && "SEND_K".equals(intent.getAction())) {
            // 'k' karakterini Bluetooth cihazına gönder
            sendKToBluetoothDevice();
        } else {
            String deviceAddress = intent.getStringExtra("device_address"); // Bluetooth cihaz adresi alınır
            if (deviceAddress != null) {
                bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress); // Cihaza bağlanmak için cihaz adresi kullanılır
                startForeground(1, getNotification()); // Foreground servisi başlatılır (Bildirimle birlikte)

                new Thread(() -> {
                    connectToDevice(); // Cihaza bağlanmayı dener
                    if (inputStream != null) {
                        readData(); // Veri okuma işlemi başlatılır
                    }
                }).start();
            } else {
                Log.e("BluetoothService", "Bluetooth device address is null."); // Eğer cihaz adresi null ise hata
                showToast("Bluetooth cihaz adresi bulunamadı."); // Uyarı göster
                stopSelf(); // Servisi durdur
            }
        }

        return START_NOT_STICKY; // Servis, sistem tarafından yeniden başlatılmasın
    }

    private void connectToDevice() {
        disconnectFromDevice(); // Önceki bağlantıyı kes

        try {
            // Android 12 ve üzeri için Bluetooth izinlerini kontrol et
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    requestBluetoothPermissions(); // İzinler verilmemişse, izinleri iste
                    return;
                }
            } else {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                    requestBluetoothPermissions(); // İzinler Android 12 altı için kontrol edilir
                    return;
                }
            }

            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID); // Bluetooth soketi oluştur
            bluetoothSocket.connect(); // Bağlantı kur
            inputStream = bluetoothSocket.getInputStream(); // Verileri okumak için InputStream al
            outputStream = bluetoothSocket.getOutputStream(); // Verileri göndermek için OutputStream al
            showToast("Bluetooth bağlantısı başarılı."); // Başarı mesajı göster
            saveConnectionStatus(true); // Bağlantı durumunu kaydet
            saveConnectedDeviceAddress(bluetoothDevice.getAddress()); // Bağlı cihazın adresini kaydet
            sendConnectionSuccessBroadcast(); // Başarılı bağlantı yayını gönder
            startKeepAlive(); // Keep-alive mesajlarını başlat
        } catch (IOException e) {
            Log.e("BluetoothService", "Bağlantı Hatası: " + e.getMessage(), e); // Bağlantı hatası logu
            showToast("Bluetooth bağlantısı başarısız."); // Uyarı göster
            sendNotification("Bağlantı Hatası", e.getMessage()); // Bildirim gönder
            saveConnectionStatus(false); // Bağlantı durumu başarısız olarak kaydedilir
            stopSelf(); // Servis durdurulur
        }
    }

    private void requestBluetoothPermissions() {
        // Bluetooth izinleri için Broadcast gönder
        Intent intent = new Intent("com.example.greenlove.REQUEST_BLUETOOTH_PERMISSION");
        sendBroadcast(intent);
    }

    private void startKeepAlive() {
        sendKeepAliveRunnable = new Runnable() {
            @Override
            public void run() {
                sendKeepAliveMessage(); // Keep-alive mesajı gönder
                handler.postDelayed(this, 5000); // Her 5 saniyede bir Keep-alive mesajı gönder
            }
        };
        handler.post(sendKeepAliveRunnable); // İş parçacığı başlat
    }

    private void stopKeepAlive() {
        if (sendKeepAliveRunnable != null) {
            handler.removeCallbacks(sendKeepAliveRunnable); // Keep-alive mesajlarını durdur
        }
    }

    private void sendKeepAliveMessage() {
        if (outputStream != null) {
            try {
                outputStream.write("KEEP_ALIVE".getBytes()); // Keep-alive mesajını gönder
                Log.d("BluetoothService", "Keep-alive message sent"); // Log kaydı
            } catch (IOException e) {
                Log.e("BluetoothService", "Keep-alive message send error: " + e.getMessage(), e); // Hata logu
                reconnectToDevice(); // Hata durumunda yeniden bağlan
            }
        }
    }

    private void reconnectToDevice() {
        disconnectFromDevice(); // Bağlantıyı kes
        connectToDevice(); // Yeniden bağlan
    }

    private void saveConnectedDeviceAddress(String address) {
        // Bağlı cihazın adresini SharedPreferences'a kaydet
        SharedPreferences sharedPreferences = getSharedPreferences("BluetoothPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("connectedDeviceAddress", address);
        editor.apply();
    }

    private void saveConnectionStatus(boolean isConnected) {
        // Bağlantı durumunu SharedPreferences'a kaydet
        SharedPreferences sharedPreferences = getSharedPreferences("BluetoothPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isConnected", isConnected);
        editor.apply();
    }

    private void sendConnectionSuccessBroadcast() {
        // Başarılı bağlantı durumu için Broadcast gönder
        Intent intent = new Intent("com.example.greenlove.BLUETOOTH_CONNECTED");
        sendBroadcast(intent);
    }

    private void disconnectFromDevice() {
        stopKeepAlive(); // Keep-alive mesajlarını durdur
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close(); // Bluetooth bağlantısını kapat
                bluetoothSocket = null;
                inputStream = null;
                outputStream = null;
                showToast("Önceki Bluetooth bağlantısı kapatıldı."); // Uyarı göster
                saveConnectionStatus(false); // Bağlantı durumu kaydedilir
            } catch (IOException e) {
                Log.e("BluetoothService", "Bağlantı kesilme hatası: " + e.getMessage(), e); // Hata logu
                sendNotification("Bağlantı Kesilme Hatası", e.getMessage()); // Bildirim gönder
            }
        }
    }

    private void readData() {
        byte[] buffer = new byte[1024]; // Verileri depolamak için buffer
        int bytes;

        while (true) {
            try {
                bytes = inputStream.read(buffer); // Veri oku
                if (bytes > 0) {
                    String message = "Kolye Arkadaşın Seni Düşünüyor"; // Sabit mesaj
                    sendFcmNotification(message); // FCM bildirimi gönder
                    showToast("Bildirim Gönderildi"); // Uyarı göster
                }
            } catch (IOException e) {
                Log.e("BluetoothService", "Veri okuma hatası: " + e.getMessage(), e); // Hata logu
                showToast("Bluetooth bağlantısı koptu."); // Uyarı göster
                sendNotification("Veri Okuma Hatası", e.getMessage()); // Bildirim gönder
                reconnectToDevice(); // Bağlantıyı yeniden dene
                break;
            }
        }
    }

    private void sendFcmNotification(String data) {
        // Firebase Cloud Messaging bildirimi gönder
        SharedPreferences sharedPreferences = getSharedPreferences("BluetoothPreferences", Context.MODE_PRIVATE);
        String title = sharedPreferences.getString("title", "Default Title");
        String message = data;
        String userToken = getSavedText();

        FcmNotificationsSender notificationsSender = new FcmNotificationsSender(userToken, getApplicationContext(), null);
        notificationsSender.SendNotifications(title, message);
    }

    private Notification getNotification() {
        // Foreground servis için bildirim oluştur
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Bluetooth Service")
                .setContentText("Bluetooth cihazına bağlı")
                .setSmallIcon(R.drawable.menu_zaman_1)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void createNotificationChannel() {
        // Bildirim kanalı oluştur
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Bluetooth Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void requestIgnoreBatteryOptimizations() {
        // Battery optimizasyonlarından kaçınma isteği
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            String packageName = getPackageName();
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent); // Kullanıcıyı battery optimizasyonlarını kapatması için yönlendir
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Bu servis bağlı bir servis değildir
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnectFromDevice(); // Servis sonlandığında Bluetooth bağlantısını kes
    }

    private void showToast(final String message) {
        // Toast mesajı göstermek için Handler kullan
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(BluetoothService.this, message, Toast.LENGTH_SHORT).show());
    }

    private void sendKToBluetoothDevice() {
        // Bluetooth cihazına 'k' karakteri gönder
        if (outputStream != null) {
            try {
                outputStream.write('k');
            } catch (IOException e) {
                Log.e("BluetoothService", "Veri gönderme hatası: " + e.getMessage(), e); // Hata logu
                showToast("Kolyeye Bildirim Gönderilmedi."); // Uyarı göster
                sendNotification("Veri Gönderme Hatası", e.getMessage()); // Bildirim gönder
            }
        } else {
            showToast("Bluetooth bağlantısı yok."); // Bağlantı yoksa uyarı göster
        }
    }

    private static final String SHARED_PREFS = "sharedPrefs"; // SharedPreferences anahtarları
    private static final String TEXT_KEY = "textKey";

    public String getSavedText() {
        // Firebase abone ol ve kaydedilmiş text'i al
        FirebaseMessaging.getInstance().subscribeToTopic("all");

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        return sharedPreferences.getString(TEXT_KEY, "Varsayılan değer");
    }

    private void sendNotification(String title, String message) {
        // Hata bildirimi gönder
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.menu_zaman_1)
                .build();
        notificationManager.notify(2, notification);
    }
}
