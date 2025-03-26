package com.example.greenlove;

import static androidx.core.content.ContextCompat.getSystemService;
import static java.security.AccessController.getContext;
import android.Manifest; // Bu satır önemli

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;


public class MainActivity extends AppCompatActivity {
    public LinearLayout bluetoothLayout;

    public int seciliSekme = 1;
    private boolean isDozeDialogShown = false; // Sınıf içi kontrol değişkeni

    boolean durum = false;
    LinearLayout bottomBar;
    private static final int REQUEST_BLUETOOTH = 1001;
    private static final int REQUEST_NOTIFICATION = 1002;
    private static final int REQUEST_PHOTO_ACCESS = 1003;
    String currentUserId;
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        if (!areAllPermissionsGranted()) {
            // Eksik izin varsa başlangıç aktivitesine yönlendir
            redirectToPermissionsActivity();
            return;
        }
// Status Bar rengi

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);



            FirebaseAuth.AuthStateListener authStateListener = firebaseAuth -> {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // Kullanıcı oturum açmış
                    currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                } else {
                   // redirectToLoginPage();
                }
            };

// Listener'ı ekle
            FirebaseAuth.getInstance().addAuthStateListener(authStateListener);


            Bundle bundle = new Bundle();
            bundle.putString("receiverId", currentUserId); // Kendi kullanıcı ID'nizi kullanın
            sohbetFragment sohbetFragment = new sohbetFragment();
            sohbetFragment.setArguments(bundle);
            bottomBar = findViewById(R.id.bottomBar);
            SharedPreferences sharedPreferences = this.getSharedPreferences("partner_bilgileri", Context.MODE_PRIVATE);


            String isim = sharedPreferences.getString("partner_isim", "");

            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);



            bluetoothLayout = findViewById(R.id.bluetoothLayout);
            final LinearLayout zamanLayout = findViewById(R.id.zamanLayout);
            final LinearLayout sohbetLayout = findViewById(R.id.sohbetLayout);
            final LinearLayout profilLayout = findViewById(R.id.profilLayout);

            final ImageView bluetoothImage = findViewById(R.id.bluetoothImage);
            final ImageView zamanImage = findViewById(R.id.zamanImage);
            final ImageView sohbetImage = findViewById(R.id.sohbetImage);
            final ImageView profilImage = findViewById(R.id.profilImage);

            final TextView bluetoothTxt = findViewById(R.id.bluetoothTxt);
            final TextView zamanTxt = findViewById(R.id.zamanTxt);
            final TextView sohbetTxt = findViewById(R.id.sohbetTxt);
            final TextView profilTxt = findViewById(R.id.profilTxt);

                 //disableDozeMode();




            // SADECE 1 KEZ ÇALIŞMASI GEREKLİ
            if (!durum) {

                getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true).replace(R.id.fragmentContainer, bluetoothFragment.class,null)
                        .commit();

                durum = true;
            }



            //BLUETOOTH SEKMESİ
            bluetoothLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    profilLayout.setClickable(true);
                    profilLayout.setEnabled(true);

                    seciliSekme = 1;

                    if(seciliSekme == 1)
                    {                                    profilLayout.setEnabled(true);

                        profilLayout.setClickable(true);

                        bottomBar.setVisibility(View.VISIBLE);

                        getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true).replace(R.id.fragmentContainer, bluetoothFragment.class,null)
                                .commit();

                        zamanTxt.setVisibility(View.GONE);
                        sohbetTxt.setVisibility(View.GONE);
                        profilTxt.setVisibility(View.GONE);

                        zamanImage.setImageResource(R.drawable.menu_zaman_1);
                        sohbetImage.setImageResource(R.drawable.menu_sohbet_1);
                        profilImage.setImageResource(R.drawable.menu_profil_1);

                        sohbetLayout.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.transparent));
                        zamanLayout.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.transparent));
                        profilLayout.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.transparent));


                        bluetoothTxt.setVisibility(View.VISIBLE);
                        bluetoothImage.setImageResource(R.drawable.bt_menu_1);
                        bluetoothLayout.setBackgroundResource(R.drawable.round_back_bluetooth_100);

                        ScaleAnimation scaleAnimation = new ScaleAnimation(0.8f,1.0f,1f,1f, Animation.RELATIVE_TO_SELF,0.0f,Animation.RELATIVE_TO_SELF,0.0f);
                        scaleAnimation.setDuration(200);
                        scaleAnimation.setFillAfter(true);
                        bluetoothLayout.startAnimation(scaleAnimation);

                        seciliSekme = 1;
                    }
                }
            });

            //ZAMAN SEKMESİ
            zamanLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    seciliSekme = 2;
                    profilLayout.setEnabled(true);

                    profilLayout.setClickable(true);

                    if(seciliSekme == 2)
                    {                                    profilLayout.setEnabled(true);

                        profilLayout.setClickable(true);

                        bottomBar.setVisibility(View.VISIBLE);

                        getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true).replace(R.id.fragmentContainer, zamanFragment.class,null)
                                .commit();
                        bluetoothTxt.setVisibility(View.GONE);
                        sohbetTxt.setVisibility(View.GONE);
                        profilTxt.setVisibility(View.GONE);

                        bluetoothImage.setImageResource(R.drawable.bt_menu_1);
                        sohbetImage.setImageResource(R.drawable.menu_sohbet_1);
                        profilImage.setImageResource(R.drawable.menu_profil_1);

                        bluetoothLayout.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.transparent));
                        sohbetLayout.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.transparent));
                        profilLayout.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.transparent));


                        zamanTxt.setVisibility(View.VISIBLE);
                        zamanImage.setImageResource(R.drawable.menu_zaman_1);
                        zamanLayout.setBackgroundResource(R.drawable.round_back_zaman_100);

                        ScaleAnimation scaleAnimation = new ScaleAnimation(0.8f,1.0f,1f,1f, Animation.RELATIVE_TO_SELF,1.0f,Animation.RELATIVE_TO_SELF,0.0f);
                        scaleAnimation.setDuration(200);
                        scaleAnimation.setFillAfter(true);
                        zamanLayout.startAnimation(scaleAnimation);

                        seciliSekme = 2;
                    }

                }
            });


                //SOHBET SEKMESİ
                sohbetLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        profilLayout.setClickable(true);

                        if (!internetVarMi(MainActivity.this)) {
                            // Eğer internet yoksa, burada istediğiniz işlemi yapabilirsiniz
                            Toast.makeText(MainActivity.this, "İnternet bağlantısı yok, lütfen kontrol edin.", Toast.LENGTH_LONG).show();
                        }else {
                            if (!isim.equals("")) {


                                seciliSekme = 3;
                                profilLayout.setClickable(true);
                                profilLayout.setEnabled(true);

                                if (seciliSekme == 3) {
                                    profilLayout.setEnabled(true);

                                    bottomBar.setVisibility(View.GONE);

                                    getSupportFragmentManager().beginTransaction()
                                            .setReorderingAllowed(true).replace(R.id.fragmentContainer, sohbetFragment.class, null)
                                            .commit();
                                    bluetoothTxt.setVisibility(View.GONE);
                                    zamanTxt.setVisibility(View.GONE);
                                    profilTxt.setVisibility(View.GONE);

                                    bluetoothImage.setImageResource(R.drawable.bt_menu_1);
                                    zamanImage.setImageResource(R.drawable.menu_zaman_1);
                                    profilImage.setImageResource(R.drawable.menu_profil_1);

                                    bluetoothLayout.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.transparent));
                                    zamanLayout.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.transparent));
                                    profilLayout.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.transparent));


                                    sohbetTxt.setVisibility(View.VISIBLE);
                                    sohbetImage.setImageResource(R.drawable.menu_sohbet_1);
                                    sohbetLayout.setBackgroundResource(R.drawable.round_back_sohbet_100);

                                    ScaleAnimation scaleAnimation = new ScaleAnimation(0.8f, 1.0f, 1f, 1f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
                                    scaleAnimation.setDuration(200);
                                    scaleAnimation.setFillAfter(true);
                                    sohbetLayout.startAnimation(scaleAnimation);

                                    seciliSekme = 3;
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "Lütfen Arkadaşınızın E-Mail Adresini Giriniz.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });


                //PROFİL SEKMESİ
                profilLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        profilLayout.setClickable(false);
                        profilLayout.setEnabled(false);

                        if (!internetVarMi(MainActivity.this)) {
                            // Eğer internet yoksa, burada istediğiniz işlemi yapabilirsiniz
                            Toast.makeText(MainActivity.this, "İnternet bağlantısı yok, lütfen kontrol edin.", Toast.LENGTH_LONG).show();
                        }else {
                            if (!isim.equals("")) {


                                seciliSekme = 4;
                                if (seciliSekme == 4) {
                                    bottomBar.setVisibility(View.VISIBLE);
                                    profilLayout.setClickable(false);
                                    profilLayout.setEnabled(false);

                                    getSupportFragmentManager().beginTransaction()
                                            .setReorderingAllowed(true).replace(R.id.fragmentContainer, profilFragment.class, null)
                                            .commit();
                                    bluetoothTxt.setVisibility(View.GONE);
                                    zamanTxt.setVisibility(View.GONE);
                                    sohbetTxt.setVisibility(View.GONE);

                                    bluetoothImage.setImageResource(R.drawable.bt_menu_1);
                                    zamanImage.setImageResource(R.drawable.menu_zaman_1);
                                    sohbetImage.setImageResource(R.drawable.menu_sohbet_1);

                                    bluetoothLayout.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.transparent));
                                    zamanLayout.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.transparent));
                                    sohbetLayout.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.transparent));

                                    profilTxt.setVisibility(View.VISIBLE);
                                    profilImage.setImageResource(R.drawable.menu_profil_1);
                                    profilLayout.setBackgroundResource(R.drawable.round_back_profil_100);

                                    ScaleAnimation scaleAnimation = new ScaleAnimation(0.8f, 1.0f, 1f, 1f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
                                    scaleAnimation.setDuration(200);
                                    scaleAnimation.setFillAfter(true);
                                    profilLayout.startAnimation(scaleAnimation);

                                    seciliSekme = 4;
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "Lütfen Arkadaşınızın E-Mail Adresini Giriniz.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

            SharedPreferences sharedPreferencespartnericin = getSharedPreferences("UserInfo", MODE_PRIVATE);


            SharedPreferences sharedPreferencespartner = getSharedPreferences("partner_bilgileri", Context.MODE_PRIVATE);
            String partnerkullaniciadi = sharedPreferencespartner.getString("partner_kullaniciadi", "Bilinmiyor");
            String partnerisim = sharedPreferencespartner.getString("partner_isim", "Bilinmiyor");
            String partnertelefon = sharedPreferencespartner.getString("partner_telefon", "Bilinmiyor");
            String partneremail = sharedPreferencespartner.getString("partner_email", "Bilinmiyor");
            System.out.println(partneremail+"dsada");

            if(!partneremail.equals("Bilinmiyor"))
            {
                downloadAndSaveProfileImage(partneremail);

            }
internetVarMi(MainActivity.this);
            Window window = getWindow();
            window.setNavigationBarColor(Color.BLACK); // Navigation bar rengini siyah yap
            return insets;
        });
    }


    public void downloadAndSaveProfileImage(String email) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Fotoğrafın yolu
        String filePath = "users/" + email + "/profile_image.jpg";
        StorageReference profileImageRef = storageRef.child(filePath);

        // Fotoğrafı indirme işlemi
        profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            String downloadUrl = uri.toString(); // Fotoğrafın indirilebilir URL'si

            // SharedPreferences'a kaydet
            SharedPreferences sharedPreferences = getSharedPreferences("partnerprofilfoto", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("partnerprofil", downloadUrl);
            editor.apply();

            Log.d("ProfileImage", "Resim indirildi ve SharedPreferences'a kaydedildi: " + downloadUrl);
        }).addOnFailureListener(e -> {
            // Hata durumunda log yaz
            Log.e("ProfileImage", "Fotoğraf indirilemedi: " + e.getMessage());
        });
    }



    @Override
    public void onBackPressed() {
        // Geri tuşuna basıldığında hiçbir şey yapılmayacak
    }
    public boolean internetVarMi(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  // Android 6.0 (API 23) ve üzeri cihazlar için
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (networkCapabilities != null) {
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return true;  // WiFi bağlantısı var
                    } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return true;  // Mobil veri bağlantısı var
                    }
                }
            } else {  // Android 6.0 altı cihazlar için
                android.net.NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnected()) {
                    return true;  // Genel bir bağlantı var
                }
            }
        }

        // İnternet yoksa burada Toast mesajı göster
        Toast.makeText(context, "İnternet bağlantısı yok", Toast.LENGTH_LONG).show();
        return false;  // İnternet bağlantısı yok
    }
    private void redirectToLoginPage() {
        // Context'i kontrol et
        if (this!= null) {
            // Tüm SharedPreferences verilerini sil
            SharedPreferences preferences = this.getSharedPreferences("your_pref_name", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear(); // Tüm verileri temizle
            editor.apply(); // Değişiklikleri kaydet

            // Dosyaları sil
            File dir = this.getFilesDir();
            deleteRecursive(dir); // Dosyaları silme metodu

            // Uygulamayı kapat
            this.finishAffinity(); // Tüm aktiviteleri kapatır ve uygulamayı kapatır

            // Eğer uygulama tamamen kapandıktan sonra tekrar açılması isteniyorsa
            // uygulamanın başlatılması için sistem servisi kullanılabilir:
            Intent intent = new Intent(this, FirstPage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            Log.e("BluetoothFragment", "Context null, yönlendirme yapılamadı.");
        }
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }




    private void disableDozeMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

            // PowerManager null değil ve pil optimizasyonları aktifse
            if (powerManager != null && !powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
                // Eğer diyalog daha önce gösterildiyse tekrar gösterme
                if (isDozeDialogShown) {
                    return;
                }

                isDozeDialogShown = true; // Diyalog gösterildi olarak işaretle
                new android.app.AlertDialog.Builder(this)
                        .setTitle("Pil Optimizasyonu Gerekiyor")
                        .setMessage("Uygulamanın düzgün çalışabilmesi için pil optimizasyonunu kapatmanız gerekiyor. Ayarlara gitmek ister misiniz?")
                        .setPositiveButton("Evet", (dialog, which) -> {
                            // Kullanıcı onay verirse ayarlara yönlendir
                            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        })
                        .setNegativeButton("Hayır", (dialog, which) -> {
                            // Kullanıcı reddederse bir uyarı göster
                            Toast.makeText(this, "Pil optimizasyonu kapatılmadı, uygulama düzgün çalışmayabilir.", Toast.LENGTH_SHORT).show();
                        })
                        .setOnDismissListener(dialog -> isDozeDialogShown = false) // Diyalog kapatıldığında bayrağı sıfırla
                        .setCancelable(false) // Kullanıcı diyalogu kapatamadan cevap vermeli
                        .show();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

      /*  if (requestCode == 101 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

            if (powerManager != null && powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
                // İzin başarıyla verildi, SharedPreferences'a kaydet
                SharedPreferences sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
                sharedPreferences.edit().putBoolean("battery_optimization_ignored", true).apply();
                Toast.makeText(this, "Pil optimizasyonu devre dışı bırakıldı. Teşekkürler!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Pil optimizasyonu ayarını devre dışı bırakmadınız. Uygulamanın performansı etkilenebilir.", Toast.LENGTH_LONG).show();
            }
        }*/
    }

    private boolean areAllPermissionsGranted() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);

        // Bluetooth Connect ve Konum İzni Kontrolü
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        // Pil Optimizasyonu
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
                return false;
            }
        }

        // Bildirim İzni
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        // Fotoğraflar ve Videolar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }


        return true; // Tüm izinler verilmişse
    }

    private void redirectToPermissionsActivity() {
        Intent intent = new Intent(MainActivity.this, baslangic_activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Toast.makeText(this, "Lütfen eksik izinleri tamamlayın.", Toast.LENGTH_SHORT).show();
    }



}
