package com.example.greenlove;

import static com.example.greenlove.bluetoothFragment.SHARED_PREFS;
import static com.example.greenlove.bluetoothFragment.TEXT_KEY;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothForegroundService extends Service {
    public static BluetoothForegroundService instance;
    // Bluetooth ile ilgili nesneler
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattService bluetoothService;
    private BluetoothGattCharacteristic bluetoothCharacteristic;
    private PowerManager.WakeLock wakeLock;
    private static final UUID SERVICE_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E"); // Nordic UART Service UUID
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E"); // TX Characteristic UUID
    private static final String TAG = "BLEForegroundService";
    private static final String CHANNEL_ID = "BLEForegroundServiceChannel";
    private static final String TARGET_DEVICE_NAME = "GreenLove12345"; // Hedef cihaz adı
    private Handler periodicHandler;
    private Runnable periodicRunnable;
    String btIDValue;
    private BluetoothLeScanner bluetoothLeScanner;

    private Handler scanTimeoutHandler;
    private boolean isDeviceConnected = false;
    String savedEmail;
    String bleadres;

    private BroadcastReceiver bluetoothCommandReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        requestBatteryOptimizationExemption();
        createNotificationChannel();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU)
        { // Android 14 ve üzeri
            startForeground(1, createNotification("Bluetooth cihazları taranıyor...", true), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 ve 13 arası
            startForeground(1, createNotification("Bluetooth cihazları taranıyor...", true));

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Android 8.0 ve 9.0
            startForeground(1, createNotification("Bluetooth cihazları taranıyor...", true));
        }
        else
        { // Android 6 ve 7
            startForeground(1, createNotification("Bluetooth cihazları taranıyor...", true));
        }

        Log.d(TAG, "Service created and running in foreground.");

        // Bluetooth durum değişikliği dinleyicisini ekleyin
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothStateReceiver, filter);
    }





    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started. Starting BLE scan...");

        startBleScan();
        //startSendingDataPeriodically();
        System.out.println("girdim");

        return START_STICKY;
    }

    private void startBleScan() {
        if (!isLocationEnabled()) {
            Log.e(TAG, "Konum servisleri kapalı. Tarama yapılamaz.");
            updateNotification("Konum servisleri kapalı, lütfen açın.", false);
            return;
        }

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth etkin değil. Tarama yapılamaz.");
            updateNotification("Cihazınız bağlı değil.", false);
            return;
        }

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (bluetoothLeScanner == null) {
            Log.e(TAG, "BluetoothLeScanner null. Tarama yapılamaz.");
            updateNotification("Cihazınız bağlı değil.", false);
            return;
        }

        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .build();

        List<ScanFilter> scanFilters = new ArrayList<>();
        scanFilters.add(new ScanFilter.Builder().setDeviceName(TARGET_DEVICE_NAME).build());

        bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback);
        updateNotification("Cihaz taranıyor...", false); // Tarama devam ederken bildirim göster
        Log.d(TAG, "BLE tarama başlatıldı.");
    }


    private void handleBluetoothStateChange(int state) {
        switch (state) {
            case BluetoothAdapter.STATE_OFF:
                Log.d(TAG, "Bluetooth kapalı.");
                updateNotification("Bluetooth kapalı. Lütfen açın.", false);
                break;

            case BluetoothAdapter.STATE_ON:
                Log.d(TAG, "Bluetooth açık. Tarama başlatılıyor...");
                startBleScan(); // Bluetooth açıldığında taramayı başlat
                break;

            default:
                Log.d(TAG, "Bluetooth durumu değişti: " + state);
                break;
        }
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            BluetoothDevice device = result.getDevice();
            String deviceName = device.getName();

            Log.d(TAG, "Device found: " + deviceName);

            if (deviceName != null && deviceName.equals(TARGET_DEVICE_NAME)) {
                Log.d(TAG, "Target device found. Connecting...");
                bluetoothLeScanner.stopScan(scanCallback);
                updateNotification("Cihaz bağlanıyor...", false); // Bağlantı kurulmaya çalışıldığını göster
                connectToDevice(device);
            }
        }

    };

    private void connectToDevice(BluetoothDevice device) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 ve üstü
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Bluetooth connect permission not granted.");
                return;
            }
        }

        bluetoothGatt = device.connectGatt(this, true, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE);
        Log.d(TAG, "Connecting to device...");
    }

    public final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Connection failed with status: " + status);
                reconnectToDevice();
                updateNotification("Cihaz bağlantısı başarısız.", false);
                return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Device connected.");
                isDeviceConnected = true;
                updateNotification("Cihaz bağlı.", true); // Bağlantı sağlandığında bildirim güncelle
                saveBluetoothConnectionState(true);
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Device disconnected.");
                isDeviceConnected = false;

                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                    updateNotification("Cihaz taranıyor...", false); // Bluetooth açıkken tarama başlatılıyor
                    reconnectToDevice();
                } else {
                    updateNotification("Cihazınız bağlı değil.", false); // Bluetooth kapalıysa bu mesaj gösterilir
                }
            }
        }





        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Services discovered");
                // Karakteristikleri burada ayarlayın
                enableNotifications(gatt);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            String value = characteristic.getStringValue(0); // Gelen veriyi al
            Log.d(TAG, "Characteristic changed: " + value);

            if ("bildirim".equals(value)) {
                //  showToast("Bildirim alındı!");
                String message = "Kolye Arkadaşın Seni Düşünüyor22"; // Sabit mesaj
                sendFcmNotification(message); // FCM bildirimi gönder
                showToast("Bildirim Gönderildi"); // Uyarı göster
            }
            else
            {

            }
        }
    };


    private void acquireWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            if (wakeLock == null || !wakeLock.isHeld()) {
                wakeLock = powerManager.newWakeLock(
                        PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE,
                        "BLEService::WakeLock"
                );
                wakeLock.acquire(10 * 60 * 1000L /*10 dakika*/);
                Log.d(TAG, "WakeLock acquired.");
            }
        }
    }

    private void reconnectToDevice() {
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }

        Log.d(TAG, "Cihaz yeniden taranıyor...");
        updateNotification("Cihaz taranıyor...", false); // Yeniden tarama durumunda bildirim güncelle
        new Handler(Looper.getMainLooper()).postDelayed(this::startBleScan, 1000); // 1 saniye gecikme ile taramayı başlat
    }


    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            Log.d(TAG, "WakeLock released.");
        }
    }

    private void requestBatteryOptimizationExemption() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    private void updateNotification(String message, boolean connected) {
        Notification notification = createNotification(message, connected);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(1, notification);
        }
    }


    private Notification createNotification(String contentText, boolean connected) {
        int icon = connected ? android.R.drawable.presence_online : android.R.drawable.presence_offline;
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("BLE Bağlantı Durumu")
                .setContentText(contentText)
                .setSmallIcon(icon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "BLE Foreground Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (bluetoothStateReceiver != null) {
            unregisterReceiver(bluetoothStateReceiver); // Alıcıyı kaldır
        }

        if (bluetoothGatt != null) {
            bluetoothGatt.close();
        }
        if (bluetoothLeScanner != null) {
            bluetoothLeScanner.stopScan(scanCallback);
        }
        if (scanTimeoutHandler != null) {
            scanTimeoutHandler.removeCallbacksAndMessages(null);
        }
        Log.d(TAG, "Service destroyed and resources cleaned up.");
    }

    private void enableNotifications(BluetoothGatt gatt) {
        for (BluetoothGattService service : gatt.getServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    gatt.setCharacteristicNotification(characteristic, true);
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                    if (descriptor != null) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                    }
                }
            }
        }
    }
    private void showToast(String message) {
        Handler handler = new Handler(getMainLooper());
        handler.post(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    private void sendFcmNotification(String data) {
        // Firebase Cloud Messaging bildirimi gönder
        SharedPreferences sharedPreferences = getSharedPreferences("BluetoothPreferences", Context.MODE_PRIVATE);
        String title = sharedPreferences.getString("title", "Default Title");
        String message = data;
        String userToken = getSavedText();
        SharedPreferences sharedPreferences20 = getApplicationContext().getSharedPreferences("partner_bilgileri", Context.MODE_PRIVATE);
        savedEmail = sharedPreferences20.getString("partner_email", null); // Varsayılan değer olarak null
        bleadres = getBLEAddress();

        loadBtIDValue();
        FcmNotificationsSender notificationsSender = new FcmNotificationsSender(bleadres, getApplicationContext(), null);
        //  notificationsSender.SendNotifications(title, message);

        notificationsSender.SendNotifications("GreenLove", "Partneriniz Sizi Düşünüyor...");    }

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

    private void loadBtIDValue() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        btIDValue = sharedPreferences.getString(TEXT_KEY, null);
    }

    public String getBLEAddress() {
        SharedPreferences sharedPreferencesble = getApplicationContext().getSharedPreferences("partner_bilgileri", Context.MODE_PRIVATE);
        return sharedPreferencesble.getString("partner_btid", null);
    }
    public static BluetoothForegroundService getInstance() {
        return instance;
    }

    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }

    public boolean sendKToBluetoothDevice(String value) {
        // Mevcut servis örneğini alın
        if (BluetoothForegroundService.instance == null) {
            Log.e(TAG, "BluetoothForegroundService örneği mevcut değil. Servis başlatılmış mı kontrol edin.");
            return false;
        }

        // Mevcut BluetoothGatt bağlantısını alın
        BluetoothGatt gatt = BluetoothForegroundService.instance.getBluetoothGatt();
        if (gatt == null) {
            Log.e(TAG, "BluetoothGatt bağlantısı mevcut değil. Bağlantının aktif olduğunu doğrulayın.");
            return false;
        }

        // Hedef hizmeti alın
        BluetoothGattService targetService = gatt.getService(SERVICE_UUID);
        if (targetService == null) {
            Log.e(TAG, "Hedef servis bulunamadı. UUID'yi kontrol edin.");
            return false;
        }

        // Hedef karakteristiği alın
        BluetoothGattCharacteristic characteristic = targetService.getCharacteristic(CHARACTERISTIC_UUID);
        if (characteristic == null) {
            Log.e(TAG, "Hedef karakteristik bulunamadı. UUID'yi kontrol edin.");
            return false;
        }

        // Değeri karakteristiğe ayarla ve yazma işlemini gerçekleştir
        characteristic.setValue(value);
        boolean success = gatt.writeCharacteristic(characteristic);

        if (success) {
            Log.d(TAG, "BLE cihazına '" + value + "' harfi başarıyla gönderildi.");
        } else {
            Log.e(TAG, "BLE cihazına '" + value + "' harfi gönderilemedi.");
        }

        return success;
    }

    private void saveBluetoothConnectionState(boolean isConnected) {
        SharedPreferences sharedPreferences = getSharedPreferences("BluetoothPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isConnected", isConnected);
        editor.apply();
    }

    public void setBluetoothGatt(BluetoothGatt gatt) {
        this.bluetoothGatt = gatt;
    }
    private void sendKeepAliveSignal() {
        if (bluetoothGatt != null && bluetoothCharacteristic != null) {
            bluetoothCharacteristic.setValue("keep-alive"); // Örnek bir veri
            bluetoothGatt.writeCharacteristic(bluetoothCharacteristic);
            Log.d(TAG, "Keep-alive mesajı gönderildi.");
        }
    }
    private Handler keepAliveHandler = new Handler();
    private Runnable keepAliveRunnable = new Runnable() {
        @Override
        public void run() {
            sendKeepAliveSignal();
            keepAliveHandler.postDelayed(this, 1000 * 60); // Her dakika bir kez gönder
        }
    };

    // Keep-alive başlat
    private void startKeepAlive() {
        keepAliveHandler.post(keepAliveRunnable);
    }

    // Keep-alive durdur
    private void stopKeepAlive() {
        keepAliveHandler.removeCallbacks(keepAliveRunnable);
    }

    public void startSendingDataPeriodically() {
        // Handler oluştur
        periodicHandler = new Handler();

        // Runnable tanımla
        periodicRunnable = new Runnable() {
            @Override
            public void run() {
                // Veriyi gönder
                boolean success = sendKToBluetoothDevice("c");
                if (success) {
                    Log.d(TAG, "Veri başarıyla gönderildi.");
                } else {
                    Log.e(TAG, "Veri gönderilemedi.");
                }

                // 20 saniye sonra tekrar çalıştır
                periodicHandler.postDelayed(this, 20000); // 20000 ms = 20 saniye
            }
        };

        // İlk çalıştırma
        periodicHandler.post(periodicRunnable);
    }

    public void stopSendingDataPeriodically() {
        if (periodicHandler != null && periodicRunnable != null) {
            periodicHandler.removeCallbacks(periodicRunnable);
            Log.d(TAG, "Periyodik veri gönderimi durduruldu.");
        }
    }

    private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                handleBluetoothStateChange(state);
            }
        }
    };

}
