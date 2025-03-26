package com.example.greenlove;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.VIBRATOR_SERVICE;


import static androidx.core.content.ContextCompat.getSystemService;
import static androidx.core.content.ContextCompat.startForegroundService;
import static androidx.core.content.PermissionChecker.checkSelfPermission;
import static com.google.common.reflect.Reflection.getPackageName;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.companion.AssociationRequest;
import android.companion.BluetoothDeviceFilter;
import android.companion.CompanionDeviceManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.InputType;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class bluetoothFragment extends Fragment {
    private ActivityResultLauncher<IntentSenderRequest> intentSenderLauncher;



    private static final int REQUEST_SELECT_DEVICE = 1; // Kullanıcı seçiminden gelen sonuç için kod
    private CompanionDeviceManager deviceManager; // CDM yönetimi için
    private static final String TAG = "BluetoothFragment";
    ImageView user_image,partner_image;
    View ayrac;

    private static final int REQUEST_PERMISSION_CODE = 1000;
    private static final int REQUEST_NOTIFICATION_PERMISSIONS = 2000;
    private static final int REQUEST_BACKGROUND_LOCATION_PERMISSION = 2000;
    private static final int REQUEST_LOCATION_PERMISSIONS = 2000;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean isScanning = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    TextView profilisim,profilemail;
    ImageView profilfoto;
    private BLEService bleService;
    private boolean isServiceBound = false;

    private List<BluetoothDevice> deviceList = new ArrayList<>();
    private ArrayAdapter<String> deviceAdapter;
    private ListView listView;
    private ActivityResultLauncher<Intent> selectDeviceLauncher;
    private boolean isProfileLoaded = false; // Profilin yüklenip yüklenmediğini kontrol eder


    String btIDValue;
    private ImageView bluetoothfr_logo;

    private GridLayout gridLayout;
    private FrameLayout hucre1, hucre3, hucre2, hucre4;
    Button kullanicisilbutton;

    CardView kolyearkadasi_bilgilericard;
    String isim;
    TextView kolyearkadasitelefon_Text;
    private LinearLayout token_layout, token_layout2, kullanici_idlayout, bluetooth_layout, kullanici_id_ici, kolyearkadasi_layout, kolye_arkadasi_ekrani;
    private EditText titleid, mesid, tokenid;
    private TextView tokenid2, kullanici_idtxt, kullanici_arkadasi_tw;
    String kullaniciAdiValue;
    TextView kullanici_ismi,partner_ismi;
    String kullaniciIsimValue;
    String kullaniciEmailValue;
    TableRow kolyearkadasi_tablo;
    TableRow kolyearkadasibutton_table;
    String kullaniciTelefonValue;
    LinearLayout kolyearkadasiekle_layout;
    LinearLayout kolyeyebaglan,titresimgonder;
    File localFile;
    Button bir;

    private static final int REQUEST_ENABLE_BT = 1;
    private boolean durum = false;
    private static final int REQUEST_PERMISSION_BT = 2;
    private static final int REQUEST_PERMISSION_NOTIFICATION = 3; // Bildirim izni

    private TextView bluetoothacikkapalı_txt;
    private Button kolyearkadasi_bt;
    private LinearLayout fulllayout;
    private TextView bluetoothdurum_Txt;
    private TextView bluetoothbaglicihaz_txt, kullanici_id, kolyearkadasi_isim_text, kolyearkadasi_email_text;

    private CustomArrayAdapter arrayAdapter; // Custom adapter kullanma
    private PowerManager.WakeLock wakeLock;

    LinearLayout otorumlayout;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ProgressBar progressBar;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 100;

    private Button buttonYak, bildirimGonder_bt, kullanici_id_bt, kullaniciidkaydet_bt, kullanici_arkadasi_bt, kolyearkadasi_eklebtn, kolyearkadasi_ekleTXT;
    private TableLayout kullaniciBilgileriLayout;
    private TextView kullaniciIsim, kullaniciAdi, kullaniciEmail, kullaniciTelefon;
    private static final String CHANNEL_ID = "MyChannelId";
    private static final String CHANNEL_NAME = "MyChannelName";
    private static final int NOTIFICATION_ID = 1;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String TEXT_KEY = "textKey";
    private boolean isReceiverRegistered = false; // Receiver'in kayıt durumu
    CardView kullanici_bilgileri_card;
    private static final int REQUEST_PERMISSIONS = 1001; // İzinler için bir benzersiz tanımlayıcı
  /*  private static final Intent[] POWERMANAGER_INTENTS = {
            new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
            new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            new Intent().setComponent(new ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
            new Intent().setComponent(new ComponentName("com.htc.pitroad", "com.htc.pitroad.landingpage.activity.LandingPageActivity")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity"))
    };
*/


    ImageView BluetoothOnnOff;
    private ObjectAnimator rotateAnimator;
    TextView oturumisim, oturumkullaniciadi, oturumemail, oturumtelefon;

    private FirebaseAuth mAuth;

    TextView bluetooth_durumu_layout;
    private FirebaseUser currentUser;
    ImageView kolyearkadasi_profilimage;
    EditText kolye_Arkadasi_ed;
    ImageView kullanici_arkadasi_image;
    LinearLayout kolye_arkadasi_lineer;
    LinearLayout partner_bilgileri_cardview;
    Button kolyearkadasi_eklebt;
    LinearLayout bluetooth_kapali_layout,profile_git_Linear;
    ImageView center_logo, kullanici_profil_image;
    SharedPreferences sharedPreferences20;
    TextView kullaniciisim_txt, kullaniciemail_txt;
    private String baglanti_durumu, bagli_cihaz;
    ProgressBar batteryProgress;
    LinearLayout main_sagpartner;
    Button btnPin25, btnPin27, btnPin31;



    private FirebaseAuth auth;
    LottieAnimationView animationView;
    BluetoothDevice selectedDevice;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluetooth, container, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 10001);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1001);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE
                );
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 ve üzeri
            if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.BLUETOOTH_SCAN}, 10000);

            }
        }


        // requestAllPermissions();
        bir = view.findViewById(R.id.bir);
        btnPin25 = view.findViewById(R.id.btn_pin_25);
        btnPin27 = view.findViewById(R.id.btn_pin_27);
        btnPin31 = view.findViewById(R.id.btn_pin_31);
        sharedPreferences20 = requireActivity().getSharedPreferences("KolyeAppPrefs", Context.MODE_PRIVATE);
        String kayitliKolyeArkadasiEmail = sharedPreferences20.getString("btemail", "E-posta bulunamadı");

        SharedPreferences sharedPreferencespartner = requireContext().getSharedPreferences("partner_bilgileri", Context.MODE_PRIVATE);
        String partnerkullaniciadi = sharedPreferencespartner.getString("partner_kullaniciadi", "Bilinmiyor");
        String partnerisim = sharedPreferencespartner.getString("partner_isim", "Bilinmiyor");
        String partnertelefon = sharedPreferencespartner.getString("partner_telefon", "Bilinmiyor");
        String partneremail = sharedPreferencespartner.getString("partner_email", "Bilinmiyor");

        kolyeArkadasiBilgileri(partneremail);
        btIDValue = getBLEAddress();


        animationView = view.findViewById(R.id.lottieAnimationView);
        animationView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        // SharedPreferences ile durum kontrolü
        SharedPreferences sharedPreferencesvar = getContext().getSharedPreferences("AutoStartPrefs", Context.MODE_PRIVATE);
        boolean isDialogShown = sharedPreferencesvar.getBoolean("isDialogShown", false);

// PowerManager kontrolü
        PowerManager powerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);

     /*   for (Intent intent : POWERMANAGER_INTENTS) {
            if (powerManager.isIgnoringBatteryOptimizations(getContext().getPackageName())) {
                Log.d("AutoStartCheck", "Otomatik başlatma etkin.");
            } else {
                Log.d("AutoStartCheck", "Otomatik başlatma devre dışı.");

                // Daha önce diyalog gösterilmediyse
                if (!isDialogShown && getContext().getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                    // Kullanıcıdan izin vermesi için diyalog göster
                    new AlertDialog.Builder(getContext())
                            .setTitle("İzin Gerekli")
                            .setMessage("Uygulamanızın arka planda çalışması için ayar yapmanız gerekiyor. Devam etmek istiyor musunuz?")
                            .setPositiveButton("Evet", (dialog, which) -> {
                                // Ayar sayfasını aç
                                startActivity(intent);
                            })
                            .setNegativeButton("Hayır", (dialog, which) -> {
                                // Kullanıcı "Hayır" dediğinde durumu kaydet
                                SharedPreferences.Editor editor = sharedPreferencesvar.edit();
                                editor.putBoolean("isDialogShown", true);
                                editor.apply();
                            })
                            .show();
                    break;
                }
            }
        }
*/LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

       // if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
         //   Toast.makeText(getContext(), "Konum servisini açın!", Toast.LENGTH_LONG).show();
           // startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        //}

        // openAppSettings();
///burası resull
        fulllayout = view.findViewById(R.id.fulllayout);
        // ProgressBar ve TextView'leri tanımlayın
        ProgressBar circularProgress = view.findViewById(R.id.circular_progress);
        TextView progressText = view.findViewById(R.id.progress_text);
        TextView storageInfoText = view.findViewById(R.id.storage_info_text);
        kolyearkadasitelefon_Text = view.findViewById(R.id.kolyearkadasitelefon_Text);
        ayrac = view.findViewById(R.id.main_ayrac);
        partner_bilgileri_cardview = view.findViewById(R.id.partner_bilgileri_cardview);
        kolyeyebaglan = view.findViewById(R.id.kolyeye_baglan_cardview);
        titresimgonder = view.findViewById(R.id.titresim_gonder);
        kullanici_ismi = view.findViewById(R.id.kullanici_ismi);
        partner_ismi = view.findViewById(R.id.partner_ismi);
        profilemail = view.findViewById(R.id.profilemail);
        profilfoto = view.findViewById(R.id.profilfoto);
        profilisim = view.findViewById(R.id.profilisim);
        kullanicisilbutton = view.findViewById(R.id.kullanicisilbutton);
        oturumisim = view.findViewById(R.id.oturumisim);
        oturumemail = view.findViewById(R.id.oturumemail);
        oturumtelefon = view.findViewById(R.id.oturumtelefon);
        oturumkullaniciadi = view.findViewById(R.id.oturumkullaniciadi);
        otorumlayout = view.findViewById(R.id.otorumlayout);
        hucre4 = view.findViewById(R.id.hucre_4);
        kolyearkadasi_tablo = view.findViewById(R.id.kolyearkadasi_table);
        kolyearkadasibutton_table = view.findViewById(R.id.kolyearkadasibutton_table);
        kolyearkadasiekle_layout = view.findViewById(R.id.kolyearkadasiekle_layout);
        user_image = view.findViewById(R.id.user_image);
        partner_image = view.findViewById(R.id.partner_image);
        kullaniciemail_txt = view.findViewById(R.id.kullaniciemail_txt);
        kullaniciisim_txt = view.findViewById(R.id.kullaniciisim_txt);
        kullanici_profil_image = view.findViewById(R.id.kullanici_profil_image);
        kullanici_bilgileri_card = view.findViewById(R.id.kullanici_bilgileri_card);
        kolyearkadasi_ekleTXT = view.findViewById(R.id.kolyearkadasi_ekleTXT);
        kolyearkadasi_eklebtn = view.findViewById(R.id.kolyearkadasi_eklebtn);
        kolyearkadasi_bilgilericard = view.findViewById(R.id.kolyearkadasi_bilgilericard);
        kolyearkadasi_profilimage = view.findViewById(R.id.kolyearkadasi_profil_image);
        kolyearkadasi_email_text = view.findViewById(R.id.kolyekarkadasiemail_text);
        kolyearkadasi_isim_text = view.findViewById(R.id.kolyearkadasiisim_text);
        center_logo = view.findViewById(R.id.center_logo);
        bluetooth_kapali_layout = view.findViewById(R.id.bluetooth_kapali_layout);
        kullaniciBilgileriLayout = view.findViewById(R.id.kullanici_bilgileri_layout);
        kullaniciIsim = view.findViewById(R.id.kullanici_isim);
        kullaniciAdi = view.findViewById(R.id.kullanici_adi);
        kullaniciEmail = view.findViewById(R.id.kullanici_email);
        kullaniciTelefon = view.findViewById(R.id.kullanici_telefon);
        bluetooth_durumu_layout = view.findViewById(R.id.bluetooth_durumu_layout);
        kolyearkadasi_eklebt = view.findViewById(R.id.kolyearkadasi_eklebt);
        // kolye_arkadasi_lineer = view.findViewById(R.id.kolye_arkadasi_lineer);
        profile_git_Linear = view.findViewById(R.id.profile_git_Linear);

        kullanici_arkadasi_image = view.findViewById(R.id.kullanici_arkadasi_image);
        kolye_Arkadasi_ed = view.findViewById(R.id.kullanici_arkadasi_ed);
        kullanici_arkadasi_bt = view.findViewById(R.id.kolye_arkadasi_bt);
        kullanici_arkadasi_tw = view.findViewById(R.id.kullanici_arkadasi_tw);
        BluetoothOnnOff = view.findViewById(R.id.BluetoothOnnOff);
        kolyearkadasi_layout = view.findViewById(R.id.kolye_arkadasi_layout);
        kolyearkadasi_bt = view.findViewById(R.id.kolye_arkadasi_bt);
        titleid = view.findViewById(R.id.titleid);
        mesid = view.findViewById(R.id.mesid);
        tokenid = view.findViewById(R.id.tokenid);
        bluetoothacikkapalı_txt = view.findViewById(R.id.bluetooth_button);
        listView = view.findViewById(R.id.liste);
        bildirimGonder_bt = view.findViewById(R.id.bildirimGonder_bt);
        bluetoothbaglicihaz_txt = view.findViewById(R.id.bluetoothdurum_txt);
        gridLayout = view.findViewById(R.id.gridLayout);
        hucre1 = view.findViewById(R.id.hucre_1);
        hucre2 = view.findViewById(R.id.hucre_2);
        hucre3 = view.findViewById(R.id.hucre_3);
        kullanici_id = view.findViewById(R.id.kullanici_id);
        token_layout = view.findViewById(R.id.toket_layout);
        kullanici_id_bt = view.findViewById(R.id.kullanici_id_bt);
        token_layout2 = view.findViewById(R.id.toket_layout2);
        tokenid2 = view.findViewById(R.id.tokenid2);
        kullaniciidkaydet_bt = view.findViewById(R.id.kullanici_idkaydet_bt);
        progressBar = view.findViewById(R.id.progressBar);
        kullanici_idlayout = view.findViewById(R.id.kullanici_layout);
        kullanici_idtxt = view.findViewById(R.id.kullanici_idtxt);
        bluetoothfr_logo = view.findViewById(R.id.imageView);
        bluetooth_layout = view.findViewById(R.id.bluetooth_layout1);
        kullanici_id_ici = view.findViewById(R.id.kullanici_id_ic);
        batteryProgress = view.findViewById(R.id.battery_progress);
        main_sagpartner = view.findViewById(R.id.main_sagpartner);

        kolyearkadasi_eklebtn.setVisibility(View.GONE);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //  bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        // İzin talebini kontrol et ve göster
        //  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        //     showBatteryOptimizationDialog();
        // }


        bir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intentbt = new Intent(getContext(),bticin.class);
                startActivity(intentbt);

                }

        });

        /*
        btnPin25.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothForegroundService service = BluetoothForegroundService.getInstance();
              service.sendCommandToNrf("1");

            }

        });
        btnPin27.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothForegroundService service = BluetoothForegroundService.getInstance();
                if (service != null) { // Servis mevcut mu kontrol et
                    boolean success = service.sendCommandToNrf("2");
                    if (success) {
                        Log.d("BLE", "Komut başarıyla gönderildi.");
                    } else {
                        Log.e("BLE", "Komut gönderilemedi.");
                    }
                } else {
                    Log.e("BLE", "BluetoothForegroundService çalışmıyor veya null.");
                }
            }
        });

        btnPin31.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothForegroundService service = BluetoothForegroundService.getInstance();
                boolean success = service.sendCommandToNrf("3");
                if (success) {
                    Log.d("BLE", "Komut başarıyla gönderildi.");
                } else {
                    Log.e("BLE", "Komut gönderilemedi.");
                }
            }

        });
*/

// Şarj seviyesi animasyonu (örneğin, %70'e kadar dolum)
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(batteryProgress, "progress", 0, 70);
        progressAnimator.setDuration(2000); // Animasyon süresi (ms)
        progressAnimator.setInterpolator(new DecelerateInterpolator()); // Yavaşlayan animasyon efekti
        progressAnimator.start();
        // ProgressBar değerlerini ayarlayın
        int progressValue = 99; // Yüzde olarak progress değeri
        double currentStorage = 1.37; // GB olarak mevcut depolama
        double maxStorage = 12.63; // GB olarak maksimum depolama

        circularProgress.setProgress(progressValue); // ProgressBar yüzdesi
        progressText.setText(progressValue + "%"); // Yüzde texti
        storageInfoText.setText(String.format("%.2fGB/%.2fGB", currentStorage, maxStorage)); // Depolama bilgisi

        partner_bilgileri_cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentpartnerbilgileri = new Intent(getContext(),partner_arkadasiprofili_kayit.class);
                startActivity(intentpartnerbilgileri);
            }
        });
        kolyeyebaglan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14 ve üzeri
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE) == PackageManager.PERMISSION_GRANTED) {
                        Intent intent = new Intent(requireContext(), BluetoothForegroundService.class);
                        ContextCompat.startForegroundService(requireContext(), intent);
                    } else {
                        Toast.makeText(requireContext(), "Gerekli izinler verilmedi!", Toast.LENGTH_SHORT).show();
                    }

                }
                else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        Intent intent = new Intent(requireContext(), BluetoothForegroundService.class);
                        ContextCompat.startForegroundService(requireContext(), intent);

                    } else {
                        Toast.makeText(requireContext(), "Bluetooth bağlantı izni verilmedi!", Toast.LENGTH_SHORT).show();
                    }
                }
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Android 8.0 ve üzeri
                    Intent intent = new Intent(requireContext(), BluetoothForegroundService.class);
                    requireContext().startForegroundService(intent);
                } else { // Android 7.1 ve altı
                    Intent intent = new Intent(requireContext(), BluetoothForegroundService.class);
                    requireContext().startService(intent);
                }


            }
        });
        // Butona tıklama dinleyicisi
        BluetoothOnnOff.setOnClickListener(v -> {
            if (bluetoothAdapter == null) {
                Toast.makeText(getContext(), "Bluetooth desteklenmiyor", Toast.LENGTH_SHORT).show();
                return;
            }

            // if (bluetoothAdapter.isEnabled()) {
            // Bluetooth açık, kullanıcı kapatmak istiyorsa
            bluetoothAdapter.disable(); // (Sadece eski Android sürümleri için çalışır)
            Toast.makeText(getContext(), "Bluetooth kapatıldı", Toast.LENGTH_SHORT).show();
            animationView.pauseAnimation();

            //   } else {
            // Bluetooth kapalı, açmak için izin iste
            // Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //  startActivityForResult(enableBtIntent, 1);
            Toast.makeText(getContext(), "Bluetooth açılıyor", Toast.LENGTH_SHORT).show();
            animationView.playAnimation();

            //  }

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Pil Optimizasyonu Ayarı");
            builder.setMessage("Lütfen uygulamanın doğru çalışması için güç yönetimi ayarlarından 'Otomatik Kapatma'dan hariç tutunuz.");
            builder.setPositiveButton("Tamam", (dialog, which) -> requestMiuiPowerManagerPermission());
            builder.setNegativeButton("İptal", null);
            builder.show();


            // Durumu kontrol edip simgeyi güncelle
            updateBluetoothIcon();
        });

        SharedPreferences sharedPreferencesmevcut = getContext().getSharedPreferences("kullanicibilgileri", Context.MODE_PRIVATE);
        SharedPreferences.Editor editormevcut = sharedPreferencesmevcut.edit();

        // Mevcut değerleri SharedPreferences'ten al
        String mevcutKullaniciIsim = sharedPreferencesmevcut.getString("kullanici_isim", "");
        String mevcutKullaniciEmail = sharedPreferencesmevcut.getString("kullanici_email", "");
        profilisim.setText(mevcutKullaniciIsim);
        profilemail.setText(mevcutKullaniciEmail);
      /*  profile_git_Linear.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                // FragmentManager ve FragmentTransaction ile geçiş işlemi
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

                // Geçiş animasyonları ekle
                transaction.setCustomAnimations(
                        R.drawable.enter_from_right,  // Yeni fragment giriş animasyonu
                        R.drawable.exit_to_left,      // Eski fragment çıkış animasyonu
                        R.drawable.enter_from_left,   // Geri dönüşte eski fragment giriş animasyonu
                        R.drawable.exit_to_right      // Geri dönüşte yeni fragment çıkış animasyonu
                );

                // Yeni Fragment oluştur
                profilFragment newFragment = new profilFragment();

                // Yeni Fragment'i fragment_container içine yerleştir
                transaction.replace(R.id.fragmentContainer, newFragment);

                // Geri tuşuna basıldığında önceki fragmente dönmek için
                transaction.addToBackStack(null);

                // Geçişi tamamla
                transaction.commit();
            }
        });

*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 ve üzeri
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_SCAN
                        },
                        REQUEST_BLUETOOTH_PERMISSIONS);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13 ve üzeri
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.POST_NOTIFICATIONS},
                            REQUEST_NOTIFICATION_PERMISSIONS);
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 ve üzeri
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        REQUEST_BACKGROUND_LOCATION_PERMISSION);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0 ve üzeri
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSIONS);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 ve üzeri
            if (requireContext().checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    requireContext().checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_CONNECT
                        },
                        1001 // İzin isteği için bir kod
                );
            }
        } else {
            if (requireContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1002
                );
            }
        }


        deviceAdapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.WHITE); // Yazı rengini beyaz yap
                return view;
            }
        };
        listView.setAdapter(deviceAdapter);

/*
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            selectedDevice = deviceList.get(position);

            if (selectedDevice != null) {
                // Bağlı cihaz bilgilerini SharedPreferences'te sakla
                SharedPreferences sharedPreferences = requireContext().getSharedPreferences("BluetoothPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("connectedDeviceName1", selectedDevice.getName());
                editor.apply();
                String connectedDeviceName1 = sharedPreferences.getString("connectedDeviceName1", "Bağlı Cihaz Yok");

                Intent serviceIntent = new Intent(getContext(), BluetoothForegroundService.class);
                serviceIntent.putExtra("device_address", selectedDevice.getAddress());

                // Servisi başlat
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    bluetoothbaglicihaz_txt.setText(connectedDeviceName1);
                    requireContext().startForegroundService(serviceIntent);
                    // SharedPreferences tanımlama ve editör oluşturma
                    SharedPreferences sharedPreferencesble = requireContext().getSharedPreferences("bledurum", Context.MODE_PRIVATE);

                    SharedPreferences.Editor editorble = sharedPreferencesble.edit();

                    editorble.putBoolean("bledurumu", true);
                    editorble.apply();


// Değişiklikleri kaydetme
                    editor.apply(); // apply() asenkron çalışır, commit() ise eşzamanlı çalışır.

                } else {
                    bluetoothbaglicihaz_txt.setText(connectedDeviceName1);


                    requireContext().startService(serviceIntent);
                }
            } else {
                Toast.makeText(getContext(), "Cihaz seçilemedi!", Toast.LENGTH_SHORT).show();
                SharedPreferences sharedPreferencesble = requireContext().getSharedPreferences("bledurum", Context.MODE_PRIVATE);

                SharedPreferences.Editor editorble = sharedPreferencesble.edit();
                editorble.putBoolean("bledurumu", false);
                editorble.apply();
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(requireActivity(),
                        new String[] {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                        REQUEST_BLUETOOTH_PERMISSIONS);
            }
        }


        // Intent serviceIntent = new Intent(getContext(), BLEService.class);
        //requireActivity().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
*/

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        requestNotificationPermission();
        auth = FirebaseAuth.getInstance();
        loadBtIDValue();

        partnerbilgilerinicek();

        // Oturum açan kullanıcının verilerini almak
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String userEmail = currentUser.getEmail();
            Log.d("User ID", userId);
            System.out.println(userEmail);
        } else {
            SharedPreferences preferences = getActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("is_logged_in", false);
            editor.apply();
            SharedPreferences sharedPreferences20 = getActivity().getSharedPreferences("your_pref_name", Context.MODE_PRIVATE);
            SharedPreferences.Editor prefsEditor = sharedPreferences20.edit(); // Değişken ismini değiştirdik
            prefsEditor.remove("btemail");
            prefsEditor.apply();
            // SharedPreferences'ı al
            SharedPreferences sharedPreferences33 = getContext().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
            SharedPreferences.Editor prefsEditor1 = sharedPreferences33.edit();

            // Tüm verileri sil
            prefsEditor1.clear(); // Tüm verileri temizler
            prefsEditor1.apply(); // Değişiklikleri uygula

            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("YourAppNamePrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor4 = sharedPreferences.edit();

            // "btIDKey" anahtarındaki veriyi sil
            editor4.remove("btIDKey");
            editor4.apply(); // Değişiklikleri uygula

            SharedPreferences sharedPreferences44 = requireActivity().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor44 = sharedPreferences44.edit();

// "btemail" anahtarını ve diğer anahtarları sil
            editor44.remove("btemail");
            editor44.remove(TEXT_KEY);
            SharedPreferences sh = getContext().getSharedPreferences("gizli", MODE_PRIVATE);
            SharedPreferences.Editor edit = sh.edit();
            edit.putBoolean("gizlimi", true);
            edit.apply();
// Değişiklikleri uygula
            editor.apply();

            SharedPreferences sharedPreferences66 = requireActivity().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

            // Önce mevcut değeri oku
            btIDValue = sharedPreferences66.getString(TEXT_KEY, null);

            // Şimdi anahtarı sil
            SharedPreferences.Editor editor66 = sharedPreferences66.edit();
            editor66.remove(TEXT_KEY);
            editor66.apply(); // Değişiklikleri uygula

            Intent intent = new Intent(getContext(), FirstPage.class);
            startActivity(intent);
            // redirectToLoginPage();
            // Toast.makeText(getContext(), "Kullanıcı oturumu açmamış.", Toast.LENGTH_LONG).show();
        }


        //SharedPreferences sh = getContext().getSharedPreferences("gizli", MODE_PRIVATE);

      //  boolean gizliMi = sh.getBoolean("gizlimi", true); // Varsayılan değer olarak true verilebilir

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("partner_bilgileri", Context.MODE_PRIVATE);


        String isimpartner = sharedPreferences.getString("partner_isim", "");

        if (isimpartner.equals(""))
        {
            main_sagpartner.setVisibility(View.GONE);
            ayrac.setVisibility(View.GONE);

        }
        else
        {
            main_sagpartner.setVisibility(View.VISIBLE);
            ayrac.setVisibility(View.VISIBLE);

        }


        SharedPreferences sharedPreferences33 = getContext().getSharedPreferences("UserInfo", MODE_PRIVATE);
        // Verileri al ve EditText'lere aktar
        isim = sharedPreferences33.getString("kullaniciIsim", "");
        String adi = sharedPreferences33.getString("kullaniciAdi", "");
        String email = sharedPreferences33.getString("kullaniciEmail", "");
        String telefon = sharedPreferences33.getString("kullaniciTelefon", "");

        if (!adi.isEmpty() && !email.isEmpty()) {
            kolyearkadasi_ekleTXT.setVisibility(View.VISIBLE);
        }
        loadText();
        hucre1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    requireContext().startForegroundService(new Intent(requireContext(), BluetoothForegroundService.class));
                } else {
                    requireContext().startService(new Intent(requireContext(), BluetoothForegroundService.class));
                }



                // Tıklandığında rengi değiştir
                hucre1.setBackgroundResource(R.drawable.frame_4);
                kullanici_bilgileri_card.setVisibility(View.GONE);
                String pairedDevice = getPairedDevice();
                // Toast.makeText(getContext(), "Eşleşmiş Cihaz: " + pairedDevice, Toast.LENGTH_LONG).show();

                //  listView.setVisibility(View.VISIBLE);
                otorumlayout.setVisibility(View.GONE);
                // Rengi eski haline döndürmek için bir delay koy
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hucre1.setBackgroundResource(R.drawable.frame_3);
                    }
                }, 1000); // 1000 milisaniye = 1 saniye

                center_logo.setVisibility(View.GONE);
                //   kolye_arkadasi_lineer.setVisibility(View.GONE);
                bluetooth_layout.setVisibility(View.VISIBLE);
                gridLayout.setVisibility(View.GONE);
                bluetoothacikkapalı_txt.setVisibility(View.VISIBLE);
                bluetoothbaglicihaz_txt.setVisibility(View.VISIBLE);
                // kolyearkadasi_eklebt.setVisibility(View.VISIBLE);


            }
        });

        titresimgonder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tokenarkadas = sadeceBTID(partneremail);
                loadText();

                tokenid.setText(btIDValue);
                System.out.println("bulan" + "           " + tokenid.getText().toString() + "\nbulan          " + btIDValue + "\n" + tokenarkadas + loadText());
                System.out.println("bak" + loadText());
                System.out.println("tokenid:" + tokenid);
                System.out.println("btıdvalue:" + btIDValue);
                System.out.println("tokenarkadas:" + tokenarkadas);
                System.out.println("loadtext:" + loadText());
                FcmNotificationsSender notificationsSender = new FcmNotificationsSender(btIDValue, getContext(), getActivity());
                notificationsSender.SendNotifications("GreenLove", "Partneriniz Sizi Düşünüyor...");
                // Titreşim
               // Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
                //if (vibrator != null && vibrator.hasVibrator()) {
                  //  vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)); // 300ms titreşim
                //}

                // Layout Titreşim Animasyonu
                ObjectAnimator shakeAnimator = ObjectAnimator.ofFloat(fulllayout, "translationX", 0, 25, -25, 15, -15, 6, -6, 0);
                shakeAnimator.setDuration(500); // 500ms sürer
                shakeAnimator.start();

            }
        });
        // Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        //  intent.setData(Uri.parse("package:" + getPackageName("com.example.greenlove")));
        // startActivity(intent);

        hucre2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences sh = getContext().getSharedPreferences("gizli", MODE_PRIVATE);

                boolean gizliMi = sh.getBoolean("gizlimi", true); // Varsayılan değer olarak true verilebilir



                if (gizliMi) {
                    // gizliMi true ise yapılacak işlemler
                    Toast.makeText(getContext(), "Lütfen Arkadaşınızın Email Adresini Giriniz.", Toast.LENGTH_SHORT).show();
                } else {
                    // Veriyi çekmek için
                    SharedPreferences sharedPreferences = requireContext().getSharedPreferences("YourAppNamePrefs", Context.MODE_PRIVATE);

                    // btIDValue'yu SharedPreferences'ten çekiyoruz
                    String btIDValueKaydet = sharedPreferences.getString("btIDKey", null); // Kaydedilen veriyi al, eğer yoksa null döner

                    // Titreme efekti oluştur
                    ObjectAnimator shake = ObjectAnimator.ofPropertyValuesHolder(
                            gridLayout,
                            PropertyValuesHolder.ofFloat("translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0)
                    );
                    shake.setDuration(500); // Animasyon süresi (milisaniye)
                    shake.start();
                    otorumlayout.setVisibility(View.GONE);
                    // Tıklandığında rengi değiştir
                    String kayitliKolyeArkadasiEmail = sharedPreferences20.getString("btemail", "E-posta bulunamadı");
                    kullanici_bilgileri_card.setVisibility(View.GONE);

                    // hucre2.setBackgroundResource(R.drawable.frame_2);

                    // Rengi eski haline döndürmek için bir delay koy
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //  hucre2.setBackgroundResource(R.drawable.frame_2); // Orijinal renge geri döndür
                        }
                    }, 1000); // 1000 milisaniye = 1 saniye

                    // Diğer işlemler
                    loadText();
                    String tokenarkadas = sadeceBTID(partneremail);
                    tokenid.setText(btIDValue);
                    System.out.println("bulan" + "           " + tokenid.getText().toString() + "\nbulan          " + btIDValue + "\n" + tokenarkadas + loadText());
                    System.out.println("bak" + loadText());
                    System.out.println("tokenid:" + tokenid);
                    System.out.println("btıdvalue:" + btIDValue);
                    System.out.println("tokenarkadas:" + tokenarkadas);
                    System.out.println("loadtext:" + loadText());
                    FcmNotificationsSender notificationsSender = new FcmNotificationsSender(btIDValue, getContext(), getActivity());
                    notificationsSender.SendNotifications("GreenLove", "Partneriniz Sizi Düşünüyor...");

                }


            }


        });

        hucre3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tıklandığında rengi değiştir
                hucre2.setBackgroundResource(R.drawable.frame_4);
                otorumlayout.setVisibility(View.GONE);
                // Rengi eski haline döndürmek için bir delay koy
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hucre2.setBackgroundResource(R.drawable.frame_3);
                    }
                }, 1000); // 1000 milisaniye = 1 saniye

                kullanici_bilgileri_card.setVisibility(View.GONE);
                center_logo.setVisibility(View.GONE);
                gridLayout.setVisibility(View.GONE);
                kullanici_id.setVisibility(View.GONE);
                token_layout.setVisibility(View.GONE);
                token_layout2.setVisibility(View.GONE);
                tokenid2.setVisibility(View.GONE);
                tokenid.setVisibility(View.VISIBLE);
                kullanici_id_ici.setVisibility(View.VISIBLE);
                bluetooth_kapali_layout.setVisibility(View.GONE);
                listView.setVisibility(View.GONE);

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserInfo", MODE_PRIVATE);


                String isim = sharedPreferences.getString("kullaniciIsim", "");

                String adi = sharedPreferences.getString("kullaniciAdi", "");
                String email = sharedPreferences.getString("kullaniciEmail", "");
                String telefon = sharedPreferences.getString("kullaniciTelefon", "");
                if (!isim.equals("")) {
                    kolyearkadasi_bt.setVisibility(View.GONE);
                    kolye_Arkadasi_ed.setVisibility(View.GONE);
                    kolyearkadasi_tablo.setVisibility(View.GONE);
                    kolyearkadasibutton_table.setVisibility(View.GONE);

                } else {
                    kolyearkadasi_bt.setVisibility(View.VISIBLE);
                    kolye_Arkadasi_ed.setVisibility(View.VISIBLE);
                    kolyearkadasibutton_table.setVisibility(View.VISIBLE);
                }


                if (!isim.isEmpty() || !adi.isEmpty() || !email.isEmpty() || !telefon.isEmpty()) {
                    kullaniciBilgileriLayout.setVisibility(View.VISIBLE);
                    kolyearkadasi_bilgilericard.setVisibility(View.VISIBLE);
                    kullanici_arkadasi_tw.setText("Kolye Arkadaşınızın Bilgileri");
                    kullanici_arkadasi_tw.setVisibility(View.GONE);
                    //  kullanici_arkadasi_image.setVisibility(View.VISIBLE);
                    getUserProfileImage(email, kolyearkadasi_profilimage);

                    if (!isim.isEmpty()) {
                        kullaniciIsim.setText(isim);
                        kolyearkadasi_isim_text.setText(isim);
                    }

                    if (!adi.isEmpty()) {
                        kullaniciAdi.setText(adi);

                    }

                    if (!email.isEmpty()) {
                        kullaniciEmail.setText(email);
                        kolyearkadasi_email_text.setText(email);
                    }

                    if (!telefon.isEmpty()) {
                        kullaniciTelefon.setText(telefon);
                    }
                }


            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        getActivity(),
                        new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.BLUETOOTH_SCAN,
                                android.Manifest.permission.BLUETOOTH_CONNECT
                        },
                        1 // İsteğe özel bir kod
                );
            }
        }


        hucre4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //  checkPermissions();
                center_logo.setVisibility(View.GONE);
                gridLayout.setVisibility(View.GONE);
                kullanici_id.setVisibility(View.GONE);
                token_layout.setVisibility(View.GONE);
                token_layout2.setVisibility(View.GONE);
                tokenid2.setVisibility(View.GONE);
                tokenid.setVisibility(View.GONE);
                kullanici_id_ici.setVisibility(View.GONE);
                bluetooth_kapali_layout.setVisibility(View.GONE);
                listView.setVisibility(View.GONE);
                otorumlayout.setVisibility(View.VISIBLE);
                kullanici_bilgileri_card.setVisibility(View.VISIBLE);


            }
        });

         localFile = new File(getContext().getCacheDir(), email + "_profile_image.jpg");

       /* if (localFile.exists()) {
            ImageView userImage = view.findViewById(R.id.profilfoto);
            Glide.with(getContext())
                    .load(localFile)
                    .apply(new RequestOptions()
                            .circleCrop()
                            .placeholder(R.drawable.profilfoto) // Yer tutucu
                            .circleCrop()) // Yuvarlak yap
                    .into(userImage);
        }
        else
        {

        }
*/


        SharedPreferences sharedPreferencesoturum = getContext().getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String profil_mail = sharedPreferencesoturum.getString("kemail1", "Misafir");
        oturumkullanici(profil_mail, kullanici_profil_image);
        oturumkullanici(profil_mail, profilfoto);
        getUserDataByEmail(profil_mail);

        ImageView kolyearkadasi_profilimage = view.findViewById(R.id.kullanici_profil_image);




        kullanicisilbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                promptForPasswordAndDeleteUser(profil_mail);


            }
        });

        kullanici_arkadasi_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!kolye_Arkadasi_ed.getText().toString().isEmpty()) {
                    String kolye_arkadasi_email = kolye_Arkadasi_ed.getText().toString();
                    kolyeArkadasiBilgileri(kolye_arkadasi_email);
                    kolyearkadasi_eklebtn.setVisibility(View.VISIBLE);

                    // Değeri kaydetme işlemi (örneğin, bir e-posta adresi kaydetmek):
                    SharedPreferences.Editor editor = sharedPreferences20.edit();
                    editor.putString("btemail", kolye_arkadasi_email); // Değeri kaydettik
                    editor.apply(); // Değişiklikleri uyguladık


                } else {
                    Toast.makeText(getActivity(), "Lütfen Arkadaşınızın E-mail Adresini Giriniz.", Toast.LENGTH_LONG).show();
                }
            }
        });

        kullanici_id_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareText();
            }
        });

        kullaniciidkaydet_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveText();
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_CONNECT
                        },
                        REQUEST_PERMISSION_CODE);

            }
        }

        kolyearkadasi_eklebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kolyearkadasi_bt.setVisibility(View.GONE);
                kolye_Arkadasi_ed.setVisibility(View.GONE);
                kolyearkadasibutton_table.setVisibility(View.GONE);
                saveText();
                loadText();
                boolean durum = true;
                kolyearkadasiekle_layout.setVisibility(View.GONE);
                kolyearkadasi_ekleTXT.setVisibility(View.VISIBLE);
                SharedPreferences sh = getContext().getSharedPreferences("gizli", MODE_PRIVATE);
                SharedPreferences.Editor edit = sh.edit();
                edit.putBoolean("gizlimi", false);
                edit.apply();
                // SharedPreferences'e verileri kaydet
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserInfo", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("kullaniciIsim", kullaniciIsimValue);
                editor.putString("kullaniciAdi", kullaniciAdiValue);
                editor.putString("kullaniciEmail", kullaniciEmailValue);
                editor.putString("kullaniciTelefon", kullaniciTelefonValue);
                editor.apply();

                // Verileri al ve EditText'lere aktar
                isim = sharedPreferences.getString("kullaniciIsim", "");
                String adi = sharedPreferences.getString("kullaniciAdi", "");
                String email = sharedPreferences.getString("kullaniciEmail", "");
                String telefon = sharedPreferences.getString("kullaniciTelefon", "");

                kullaniciIsim.setText(isim);
                kullaniciAdi.setText(adi);
                kullaniciEmail.setText(email);
                kullaniciTelefon.setText(telefon);
                kolyearkadasi_eklebtn.setVisibility(View.GONE);

            }
        });

        // Set text color to white
        titleid.setTextColor(Color.WHITE);
        mesid.setTextColor(Color.WHITE);
        tokenid.setTextColor(Color.WHITE);


        FirebaseMessaging.getInstance().subscribeToTopic("all");

        // Kullanıcının token'ını almak ve Firestore'a kaydetmek
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        // Yeni FCM token alındı
                        String token = task.getResult();
                        // Tokenı Firestore'daki btID alanına kaydet
                        updateCurrentUserBtID(token);
                    }
                });


        // Bildirim gönderme butonu
        bildirimGonder_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleid.getText().toString();
                String message = mesid.getText().toString();

                if (btIDValue != null && !btIDValue.isEmpty()) {
                    FcmNotificationsSender notificationsSender = new FcmNotificationsSender(btIDValue, getContext(), getActivity());
                    notificationsSender.SendNotifications(title, message);
                } else {
                    Toast.makeText(getContext(), "Kolye arkadaşınızın tokenı alınamadı.", Toast.LENGTH_LONG).show();
                }
            }
        });
        ImageView BluetoothOnnOff = view.findViewById(R.id.BluetoothOnnOff);

        animationView.setOnClickListener(v -> {
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                showBluetoothDisableDialog();
            } else {
                Toast.makeText(getContext(), "Bluetooth zaten kapalı.", Toast.LENGTH_SHORT).show();
            }
        });

      //  loadPartnerProfileImage(getContext(),kolyearkadasi_profilimage);

       // IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
     //   requireActivity().registerReceiver(bluetoothStateReceiver, filter);


        return view;
    }

    private void updateBluetoothIcon() {
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            animationView.setVisibility(View.VISIBLE);
            BluetoothOnnOff.setVisibility(View.GONE);
            animationView.playAnimation();

        } else {

            bluetoothbaglicihaz_txt.setText("Kolye Bağlı Değil");
            BluetoothOnnOff.setVisibility(View.VISIBLE);
            animationView.pauseAnimation();
            animationView.setVisibility(View.GONE);
        }
    }

    private boolean isConnectedToBluetoothDevice() {
        if (bluetoothAdapter != null) {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices != null && !pairedDevices.isEmpty()) {
                for (BluetoothDevice device : pairedDevices) {
                    int connectionState = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.GATT);
                    if (connectionState == BluetoothProfile.STATE_CONNECTED) {
                        Log.d("Bluetooth", "Cihaza bağlı: " + device.getName());
                        return true;
                    }
                }
            }
        }
        Log.d("Bluetooth", "Hiçbir cihaza bağlı değil.");
        return false;
    }


    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("BluetoothPrefs", Context.MODE_PRIVATE);
        String connectedDeviceName1 = sharedPreferences.getString("connectedDeviceName1", "Bağlı Cihaz Yok");
        // İzin durumu kontrolü
        //   if (!checkBatteryOptimizationPermission()) {
        //   requestAllPermissions();
        //  }

        if (!connectedDeviceName1.equals("Bağlı Cihaz Yok")) {
            bluetoothbaglicihaz_txt.setText(connectedDeviceName1);
        } else {
            bluetoothbaglicihaz_txt.setText("Bağlı cihaz bulunamadı");
        }


//      if (bluetoothAdapter.isEnabled()) {
        animationView.playAnimation();
        BluetoothOnnOff.setVisibility(View.GONE);
        animationView.setVisibility(View.VISIBLE);
        listView.setVisibility(View.VISIBLE);
        bluetoothacikkapalı_txt.setText("Bluetooth Açık");


        // startScanning();
        //   } else {
        bluetoothacikkapalı_txt.setText("Bluetooth Kapalı");
        bluetoothbaglicihaz_txt.setText("Kolye Bağlı Değil");
        //stopScanning();
        listView.setVisibility(View.GONE);
        BluetoothOnnOff.setVisibility(View.VISIBLE);
        animationView.setVisibility(View.GONE);
        animationView.pauseAnimation();
        SharedPreferences sharedPreferencesble = requireContext().getSharedPreferences("bledurum", Context.MODE_PRIVATE);

        SharedPreferences.Editor editorble = sharedPreferencesble.edit();

        editorble.putBoolean("bledurumu", false);
        editorble.apply();


        //  }

        // İlk durum kontrolü
        updateBluetoothIcon();
        checkBluetoothPermissions();
        // requestBatteryOptimizationPermission();
        //acquireWakeLock();
        saveTitle();
        // requestBackgroundPermissionIfNeeded(); // Yalnızca arka planda çalışır
    }

    private void requestBackgroundPermissionIfNeeded() {
        if (!isAppInForeground()) { // Uygulama arka planda çalışıyorsa
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 ve üstü için
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                            REQUEST_PERMISSION_CODE);
                }
            }
        }
    }

    public void loadPartnerProfileImage(Context context, ImageView imageView) {
        // SharedPreferences'tan URL'yi al
        SharedPreferences sharedPreferences = context.getSharedPreferences("partnerprofilfoto", Context.MODE_PRIVATE);
        String cachedUrl = sharedPreferences.getString("cached_partnerprofil", null); // Daha önce yüklenen URL
        String partnerProfileUrl = sharedPreferences.getString("partnerprofil", null); // Yeni URL

        if (partnerProfileUrl != null && !partnerProfileUrl.equals(cachedUrl)) {
            // URL farklıysa veya cachedUrl yoksa yeni resmi yükle
            Glide.with(context)
                    .load(partnerProfileUrl)
                    .apply(RequestOptions.circleCropTransform()
                            .signature(new ObjectKey(System.currentTimeMillis()))) // Her yükleme için benzersiz anahtar
                    .into(imageView);

            // Yeni URL'yi cached olarak kaydet
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("cached_partnerprofil", partnerProfileUrl);
            editor.apply();

            Log.d("PartnerProfileImage", "Yeni resim yüklendi ve cache güncellendi.");
        } else if (partnerProfileUrl != null) {
            // URL aynıysa resmi yeniden yükle (ama önbellekten)
            Glide.with(context)
                    .load(partnerProfileUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageView);

            Log.d("PartnerProfileImage", "Resim zaten güncel, önbellekten yüklendi.");
        } else {
            // URL yoksa varsayılan resmi göster
            imageView.setImageResource(R.drawable.profilfotokullanici);
            Log.d("PartnerProfileImage", "Varsayılan resim yüklendi.");
        }
    }



    private boolean isAppInForeground() {
        ActivityManager activityManager = (ActivityManager) requireContext().getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) return false;

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) return false;

        final String packageName = requireContext().getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                    appProcess.processName.equals(packageName)) {
                return true; // Uygulama ön planda
            }
        }
        return false; // Uygulama arka planda
    }

    private void checkBluetoothPermissions() {
        // Android 12 ve üzeri için (BLUETOOTH_CONNECT ve BLUETOOTH_SCAN izinleri gereklidir)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

                // Kullanıcıdan izin iste
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_SCAN
                        },
                        REQUEST_BLUETOOTH_PERMISSIONS);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0 (API 23) ve üzeri için (BLUETOOTH ve BLUETOOTH_ADMIN izinleri gereklidir)
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {

                // Kullanıcıdan izin iste
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{
                                Manifest.permission.BLUETOOTH,
                                Manifest.permission.BLUETOOTH_ADMIN
                        },
                        REQUEST_BLUETOOTH_PERMISSIONS);
            }
        } else {
            Log.d("BluetoothPermissions", "Bluetooth izinleri Android 6.0'dan önce otomatik olarak verilir.");
        }
    }

    private void acquireWakeLock() {
        PowerManager powerManager = (PowerManager) requireContext().getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::BLEWakeLock");
            //  wakeLock.acquire(10 * 60 * 1000L /*10 dakika*/);
            wakeLock.acquire(); // Süresiz WakeLock

        }
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }


    private void requestBackgroundPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1002);
            }
        }
    }

    private void checkAndRequestBatteryOptimization() {
        PowerManager powerManager = (PowerManager) requireContext().getSystemService(Context.POWER_SERVICE);

        if (powerManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Pil optimizasyonlarını atlayıp atlamadığını kontrol et
            if (!powerManager.isIgnoringBatteryOptimizations(requireContext().getPackageName())) {
                // Kullanıcıyı pil optimizasyonu ayarına yönlendirmek için bir intent oluştur
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                startActivity(intent);
            } else {
                Log.d("BatteryOptimization", "Battery optimization is already ignored.");
            }
        } else {
            Log.w("BatteryOptimization", "PowerManager is null or API level is below 23.");
        }
    }


    @Override
    public void onPause() {
        super.onPause();

    }

    private void saveTitle() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("BluetoothPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("title", titleid.getText().toString());
        editor.apply();
    }


    private void sendFcmNotification(String data) {
        String title = titleid.getText().toString();
        String message = data;
        FcmNotificationsSender notificationsSender = new FcmNotificationsSender("/topics/all", getContext(), getActivity());
        notificationsSender.SendNotifications(title, message);
    }


    private void shareText() {
        String textToShare = tokenid2.getText().toString();
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, textToShare);
        shareIntent.setType("text/plain");

        Intent chooser = Intent.createChooser(shareIntent, "Metni Paylaş");
        if (shareIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(chooser);
        }
    }

    private void saveText() {
        String textToSave = btIDValue; // Arkadaşınızın FCM tokenı
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String kayitliKolyeArkadasiEmail = sharedPreferences20.getString("btemail", "E-posta bulunamadı");
        System.out.println("kolye arasi" + kayitliKolyeArkadasiEmail);
        SharedPreferences sharedPreferences44 = requireContext().getSharedPreferences("partner_bilgileri", Context.MODE_PRIVATE);
        String partneremail = sharedPreferences44.getString("partner_email", "Bilinmiyor");
        sadeceBTID(partneremail);
        editor.putString(TEXT_KEY, textToSave);
        editor.apply();
        Toast.makeText(getContext(), "Kayıt Başarılı", Toast.LENGTH_LONG).show();

        tokenid.setText(textToSave);
    }

    private String loadText() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String savedText = sharedPreferences.getString(TEXT_KEY, "Lütfen Kolye Eşinizin Kolye Şifresini Girip Kaydet Butonuna Basınız.");
        tokenid.setText(btIDValue);
        System.out.println("bu" + savedText + "buuu" + btIDValue);
        System.out.println("bu" + savedText + "buuu" + btIDValue);
        return btIDValue;

    }


    private void stopBluetoothAnimation() {
        if (rotateAnimator != null) {
            rotateAnimator.end();  // Animasyonu durdur
        }
    }

    private void updateCurrentUserBtID(String newBtID) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("Kullanicilar").document(userEmail);

            // Güncelleme işlemi
            docRef.update("btID", newBtID)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Firestore", "btID başarıyla güncellendi!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("Firestore", "btID güncellemesi başarısız oldu", e);
                            Toast.makeText(getContext(), "btID güncellemesi başarısız oldu.", Toast.LENGTH_LONG).show();
                        }
                    });
        } else {

            // Toast.makeText(getContext(), "Kullanıcı oturumu açmamış.", Toast.LENGTH_LONG).show();
        }
    }

    private String sadeceBTID(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Kullanicilar").document(email);


// Değ
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Arkadaşınızın bilgilerini alın

                        btIDValue = document.getString("btID"); // Arkadaşınızın FCM tokenı
                        // Veriyi kaydetmek için
                        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("btid", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        String btIDValueKAYDET = btIDValue; // Bu değişkenin gerçek değerini burada saklayacaksın
                        editor.putString("btIDKey", btIDValueKAYDET);

                        editor.apply(); // Asenkron olarak kaydet (performans için tercih edilir)

                        System.out.println("kolyebt" + btIDValue);
                        System.out.println("bune" + btIDValue);
                        tokenid.setText(btIDValue);
                        // saveBtIDValue();


                    }
                } else {
                    Log.d("Firestore", "Kullanıcı bilgileri çekilemedi.", task.getException());
                    Toast.makeText(getContext(), "Kullanıcı bilgileri çekilemedi.", Toast.LENGTH_LONG).show();
                }
            }
        });


        return btIDValue;
    }


    private void kolyeArkadasiBilgileri(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Kullanicilar").document(email);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Arkadaşınızın bilgilerini alın
                        kullaniciAdiValue = document.getString("kullanici_adi");
                        kullaniciIsimValue = document.getString("kullanici_isim");
                        kullaniciEmailValue = document.getString("kullanici_email");
                        kullaniciTelefonValue = telefonnogizle(document.getString("kullanici_telefon"));
                        btIDValue = document.getString("btID"); // Arkadaşınızın FCM tokenı
                        if (getActivity() != null) {
                            saveBLEAddress(btIDValue);
                        } else {
                            Log.e("kolyeArkadasiBilgileri", "Activity null, saveBLEAddress çağrılmadı.");
                        }

                        // Bilgileri arayüzde gösterin
                        kullaniciIsim.setText(kullaniciIsimValue);
                        kullaniciAdi.setText(kullaniciAdiValue);
                        kullaniciEmail.setText(kullaniciEmailValue);
                        kullaniciTelefon.setText(kullaniciTelefonValue);
                        getUserProfileImage(kullaniciEmailValue, kolyearkadasi_profilimage);
                        partner_ismi.setText(kullaniciIsimValue);

                        kolyearkadasitelefon_Text.setText(kullaniciTelefonValue);
                        kolyearkadasi_email_text.setText(kullaniciEmailValue);
                        kolyearkadasi_isim_text.setText(kullaniciIsimValue);

                        // Gerekli görünümleri görünür yapın
                        kullanici_arkadasi_tw.setVisibility(View.GONE);
                        //   kullanici_arkadasi_image.setVisibility(View.VISIBLE);
                        kullaniciBilgileriLayout.setVisibility(View.VISIBLE);
                        kolyearkadasi_bilgilericard.setVisibility(View.VISIBLE);
                        //  kolyearkadasi_bt.setVisibility(View.VISIBLE);
                        // kolyearkadasi_eklebt.setVisibility(View.VISIBLE);

                        // btIDValue'yu kaydedin (eğer gerekliyse)
                        // saveBtIDValue();
                    } else {
                        Log.d("Firestore", "Belirtilen e-posta ile eşleşen bir kullanıcı bulunamadı.");
                        //  Toast.makeText(getContext(), "Belirtilen e-posta ile eşleşen bir kullanıcı bulunamadı.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d("Firestore", "Kullanıcı bilgileri çekilemedi.", task.getException());
                    Toast.makeText(getContext(), "Kullanıcı bilgileri çekilemedi.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private String telefonnogizle(String phoneNumber) {
        if (phoneNumber != null && phoneNumber.length() > 2) {
            int length = phoneNumber.length();
            String maskedNumber = phoneNumber.substring(0, length - 2).replaceAll(".", "*");
            maskedNumber += phoneNumber.substring(length - 2);
            return maskedNumber;
        }
        return phoneNumber;
    }

    // Token'ı Firestore'a kaydeden metod
    private void saveTokenToFirestore(String token) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            db.collection("Users").document(currentUser.getUid())
                    .update("fcmToken", token)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("FCM", "Token başarıyla kaydedildi!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("FCM", "Token kaydedilirken bir hata oluştu", e);
                        }
                    });
        }

    }

    // Karşı tarafa bildirim gönderen metod
    private void sendNotificationToUser(String targetUserId, String title, String body) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(targetUserId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String targetFcmToken = document.getString("fcmToken");
                                if (targetFcmToken != null) {
                                    FcmNotificationsSender notificationsSender = new FcmNotificationsSender(targetFcmToken, getContext(), getActivity());
                                    notificationsSender.SendNotifications(title, body);
                                }
                            } else {
                                Log.d("FCM", "Hedef kullanıcının token'ı bulunamadı.");
                            }
                        } else {
                            Log.d("FCM", "Veritabanından token alınamadı.", task.getException());
                        }
                    }
                });
    }

    private void saveBtIDValue() {
        String kayitliKolyeArkadasiEmail = sharedPreferences20.getString("btemail", "E-posta bulunamadı");
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        SharedPreferences sharedPreferences44 = requireContext().getSharedPreferences("partner_bilgileri", Context.MODE_PRIVATE);
        String partneremail = sharedPreferences44.getString("partner_email", "Bilinmiyor");

        sadeceBTID(partneremail);
        editor.putString(TEXT_KEY, btIDValue);
        editor.apply();
    }

    private void loadBtIDValue() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        btIDValue = sharedPreferences.getString(TEXT_KEY, null);
    }

    // Bildirim iznini talep etmek için (Android 13 ve üzeri)
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 ve sonrası
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Bildirim izni talep et
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_PERMISSION_NOTIFICATION);
            }
        }
    }

    public void getUserProfileImage(String email, View view) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Kullanıcının e-posta adresine göre profil resminin yolu
        String filePath = "users/" + email + "/profile_image.jpg"; // E-posta adresini doğrudan kullanıyoruz
        StorageReference profileImageRef = storageRef.child(filePath);


        // Resmi yükle
        profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // URL alındı, resmi Glide ile yükle
                ImageView kolyearkadasi_profilimage = view.findViewById(R.id.kolyearkadasi_profil_image);
                Glide.with(view)
                        .load(uri)
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.profilfotokullanici) // Yüklenirken gösterilecek yer tutucu
                                .circleCrop()) // Resmi yuvarlak yap
                        .into(kolyearkadasi_profilimage);

                Glide.with(view)
                        .load(uri)
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.profilfotokullanici) // Yüklenirken gösterilecek yer tutucu
                                .circleCrop()) // Resmi yuvarlak yap
                        .into(partner_image);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Hata durumunda varsayılan resmi göster
                ImageView kolyearkadasi_profilimage = view.findViewById(R.id.kolyearkadasi_profil_image);
                kolyearkadasi_profilimage.setImageResource(R.drawable.profilfotokullanici);
                Log.w("Storage", "Resim yüklenemedi: " + e.getMessage());
            }
        });
    }

    public void oturumkullanici(String email, View view) {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        SharedPreferences profilsahibi = getContext().getSharedPreferences("ProfilKullanici", MODE_PRIVATE);
        String profilFotoUrl1 = profilsahibi.getString("profilFotoUrl", null);
        // Kullanıcının e-posta adresine göre profil resminin yolu
        String filePath = "users/" + email + "/profile_image.jpg"; // E-posta adresini doğrudan kullanıyoruz
        StorageReference profileImageRef = storageRef.child(filePath);

        if (profilFotoUrl1 != null && !profilFotoUrl1.isEmpty()) {
            Glide.with(getContext())
                    .load(profilFotoUrl1)
                    .apply(RequestOptions.circleCropTransform())
                    .into(profilfoto);
            return;
        } else {
            profilfoto.setImageResource(R.drawable.profilfotokullanici);

        }


        // Resmi yükle
        profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // URL alındı, resmi Glide ile yükle


//                File localFile = new File(getContext().getCacheDir(), email + "_profile_image.jpg");


               /* ImageView kolyearkadasi_profilimage = view.findViewById(R.id.kullanici_profil_image);
                Glide.with(view)
                        .load(uri)
                        .apply(new RequestOptions()
                                .circleCrop()
                                .placeholder(R.drawable.profilfotokullanici) // Yüklenirken gösterilecek yer tutucu
                                .circleCrop()) // Resmi yuvarlak yap
                        .into(kullanici_profil_image);

                Glide.with(view)
                        .load(uri)
                        .apply(new RequestOptions()
                                .circleCrop()
                                .placeholder(R.drawable.profilfotokullanici) // Yüklenirken gösterilecek yer tutucu
                                .circleCrop()) // Resmi yuvarlak yap
                        .into(kullanici_profil_image);



                Glide.with(view)
                        .load(uri)
                        .apply(new RequestOptions()
                                .circleCrop()
                                .placeholder(R.drawable.profilfotokullanici) // Yüklenirken gösterilecek yer tutucu
                                .circleCrop()) // Resmi yuvarlak yap
                        .into(profilfoto);

                Glide.with(view)
                        .load(uri)
                        .apply(new RequestOptions()
                                .circleCrop()
                                .placeholder(R.drawable.profilfotokullanici) // Yüklenirken gösterilecek yer tutucu
                                .circleCrop()) // Resmi yuvarlak yap
                        .into(user_image);

                downloadAndSaveImage(uri, localFile, getContext());
*/


            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Hata durumunda varsayılan resmi göster
                // ImageView kolyearkadasi_profilimage = view.findViewById(R.id.kullanici_profil_image);
                //  kolyearkadasi_profilimage.setImageResource(R.drawable.logo_circle);
                //  Log.w("Storage", "Resim yüklenemedi: " + e.getMessage());
            }
        });
    }


    private void downloadAndSaveImage(Uri uri, File localFile, Context context) {
        new Thread(() -> {
            try {
                // URL'den resmi indir
                InputStream inputStream = new URL(uri.toString()).openStream();
                OutputStream outputStream = new FileOutputStream(localFile);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();
                inputStream.close();

                // İndirme tamamlandıktan sonra log ile bildir
                Log.d("Storage", "Resim başarıyla kaydedildi: " + localFile.getAbsolutePath());
            } catch (Exception e) {
                Log.e("Storage", "Resim kaydedilemedi: " + e.getMessage());
            }
        }).start();
    }
    public void getUserDataByEmail(String userEmail) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
// SharedPreferences'i metot içinde tanımla
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("kullanicibilgileri", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Mevcut değerleri SharedPreferences'ten al
        String mevcutKullaniciIsim = sharedPreferences.getString("kullanici_isim", "");
        String mevcutKullaniciEmail = sharedPreferences.getString("kullanici_email", "");
        String durum = sharedPreferences.getString("durum", "ilk");
        String activtydurumu = sharedPreferences.getString("durum1", "ilk");
        // Firestore sorgusu: kullanıcı e-posta adresine göre filtrele
        db.collection("Kullanicilar")
                .whereEqualTo("kullanici_email", userEmail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Belgeleri al ve kullanıcı bilgilerini işleyin
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Kullanıcı bilgilerini al
                                String kullaniciAdimetot = document.getString("kullanici_adi");
                                String kullaniciIsimmetot = document.getString("kullanici_isim");
                                String kullaniciemailmetot = document.getString("kullanici_email");
                                String kullaniciTelefonmetot = document.getString("kullanici_telefon");
                                String kullaniciSifremetot = document.getString("kullanici_sifre");
                                String sohbetIDmetot = document.getString("sohbetID");



                            if (durum.equals("ilk")) {
                                editor.putString("kullanici_isim", kullaniciIsimmetot); // Yeni değeri kaydet
                                editor.putString("kullanici_email", kullaniciemailmetot); // Yeni değeri kaydet
                                editor.putString("durum", "son");
                                System.out.println("girmedi");
                                profilemail.setText(kullaniciemailmetot);
                                profilisim.setText(kullaniciIsimmetot);
                                editor.apply();
                            }


                                //profilemail.setText(kullaniciemailmetot);
                                //profilisim.setText(kullaniciIsimmetot);
                                oturumisim.setText(kullaniciIsimmetot);
                                oturumkullaniciadi.setText(kullaniciAdimetot);
                                oturumemail.setText(kullaniciemailmetot);
                                oturumtelefon.setText(kullaniciTelefonmetot);
                                kullaniciisim_txt.setText(kullaniciIsimmetot);
                                kullaniciemail_txt.setText(kullaniciemailmetot);
                                kullanici_ismi.setText(kullaniciIsimmetot);


                            }
                        } else {
                            // Eğer sorgu başarısız olursa
                            Log.w("FirebaseData", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void promptForPasswordAndDeleteUser(String emailToDelete) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Şifre Girişi");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("Sil", (dialog, which) -> {
            String password = input.getText().toString();
            deleteUserAndFiles(emailToDelete, password);
        });
        builder.setNegativeButton("İptal", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public void deleteUserAndFiles(String emailToDelete, String password) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Öncelikle oturum açmalısınız.", Toast.LENGTH_LONG).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(emailToDelete, password);
        auth.getCurrentUser().reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    // Kimlik doğrulama başarılı, dosyaları ve Firestore kaydını sil
                    deleteUserFilesAndFirestore(emailToDelete);
                    deleteAllPostsFromFirestore(emailToDelete);
                    clearAppFiles();
                    clearSharedPreferences();
                    Toast.makeText(getActivity(), "Kullanıcı başarıyla silindi.", Toast.LENGTH_SHORT).show();
                    redirectToLoginPage();
                })
                .addOnFailureListener(e -> {
                    Log.w("AuthDelete", "Yeniden kimlik doğrulama hatası: ");
                    Toast.makeText(getContext(), "Yeniden kimlik doğrulama hatası: ", Toast.LENGTH_SHORT).show();
                });
    }

    private void clearAppFiles() {
        // Uygulama dosyalarını temizle
        File dir = getContext().getFilesDir();
        if (dir != null && dir.isDirectory()) {
            deleteDir(dir); // Tüm dosyaları sil
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    deleteDir(new File(dir, child));
                }
            }
        }
        return dir.delete(); // Dizini sil
    }

    // SharedPreferences'ı temizleyen metot
    private void clearSharedPreferences() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("your_pref_name", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Tüm verileri temizler
        editor.apply(); // Değişiklikleri uygula
        Log.d("SharedPreferences", "Tüm SharedPreferences verileri temizlendi.");
    }

    private void redirectToLoginPage() {
        // Context'i kontrol et
        if (getActivity() != null) {
            // Tüm SharedPreferences verilerini sil
            SharedPreferences preferences = getActivity().getSharedPreferences("your_pref_name", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear(); // Tüm verileri temizle
            editor.apply(); // Değişiklikleri kaydet

            // Dosyaları sil
            File dir = getActivity().getFilesDir();
            deleteRecursive(dir); // Dosyaları silme metodu

            // Uygulamayı kapat
            getActivity().finishAffinity(); // Tüm aktiviteleri kapatır ve uygulamayı kapatır

            // Eğer uygulama tamamen kapandıktan sonra tekrar açılması isteniyorsa
            // uygulamanın başlatılması için sistem servisi kullanılabilir:
            Intent intent = new Intent(getActivity(), FirstPage.class);
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


    private void deleteUserFilesAndFirestore(String emailToDelete) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Kullanıcının dosyalarını sil
        StorageReference userFolderRef = storageRef.child("users/" + emailToDelete);
        StorageReference imagesFolderRef = storageRef.child("images/" + emailToDelete);

        // Kullanıcı dosyalarını sil
        deleteFilesInFolder(userFolderRef, () -> {
            // Resim dosyalarını sil
            deleteFilesInFolder(imagesFolderRef, () -> {
                // Dosyalar silindikten sonra Firestore'dan kullanıcıyı sil
                db.collection("Kullanicilar").document(emailToDelete).delete()
                        .addOnSuccessListener(aVoid -> {
                            Log.d("FirestoreDelete", emailToDelete + " belgesi başarıyla Firestore'dan silindi.");
                            //  Toast.makeText(getContext(), emailToDelete + " başarıyla silindi.", Toast.LENGTH_SHORT).show();

                            // Kullanıcının postlarını sil
                            deleteUserPosts(emailToDelete, () -> deleteAuthUser(emailToDelete));
                        })
                        .addOnFailureListener(e -> {
                            Log.w("FirestoreDelete", "Belge silinirken hata oluştu: " + e.getMessage());
                            // Toast.makeText(getContext(), "Belge silinirken hata: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            });
        });
    }

    private void deleteUserPosts(String emailToDelete, Runnable onComplete) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Posts").whereEqualTo("email", emailToDelete).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("FirestoreDelete", "Silinecek post yok.");
                        onComplete.run();
                        return;
                    }
                    List<Task<Void>> postDeleteTasks = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        postDeleteTasks.add(document.getReference().delete()
                                .addOnSuccessListener(aVoid1 -> {
                                    Log.d("FirestoreDelete", document.getId() + " belgesi başarıyla silindi.");
                                })
                                .addOnFailureListener(e -> {
                                    Log.w("FirestoreDelete", "Post silinirken hata oluştu: " + e.getMessage());
                                    //      Toast.makeText(getContext(), "Post silinirken hata: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }));
                    }

                    // Tüm post silme işlemleri tamamlandığında onComplete'i çağır
                    Tasks.whenAllSuccess(postDeleteTasks).addOnSuccessListener(aVoid2 -> {
                        Log.d("FirestoreDelete", "Tüm postlar başarıyla silindi.");
                        onComplete.run();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.w("FirestoreDelete", "Postlar silinirken hata oluştu: " + e.getMessage());
                    // Toast.makeText(getContext(), "Postlar silinirken hata: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void deleteAuthUser(String emailToDelete) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.getCurrentUser().delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("AuthDelete", emailToDelete + " kullanıcısı başarıyla silindi.");

                })
                .addOnFailureListener(e -> {
                    Log.w("AuthDelete", "Kullanıcı silinirken hata oluştu: " + e.getMessage());
                    // Toast.makeText(getContext(), "Kullanıcı silinirken hata: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void deleteFilesInFolder(StorageReference folderRef, Runnable onComplete) {
        folderRef.listAll().addOnSuccessListener(listResult -> {
            if (listResult.getItems().isEmpty()) {
                // Klasörde dosya yoksa, tamamla
                onComplete.run();
                return;
            }

            // Dosyaları sil
            List<Task<Void>> deleteTasks = new ArrayList<>();
            for (StorageReference item : listResult.getItems()) {
                deleteTasks.add(item.delete().addOnSuccessListener(aVoid -> {
                    Log.d("DeleteUserFiles", item.getName() + " başarıyla silindi.");
                }).addOnFailureListener(e -> {
                    Log.w("DeleteUserFiles", "Silme hatası: " + e.getMessage());
                    Toast.makeText(getContext(), "Silme hatası: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }));
            }

            // Tüm silme işlemleri tamamlandığında onComplete'i çağır
            Tasks.whenAllSuccess(deleteTasks).addOnSuccessListener(aVoid -> {
                Log.d("DeleteUserFiles", "Tüm dosyalar başarıyla silindi.");
                onComplete.run();
            }).addOnFailureListener(e -> {
                Log.w("DeleteUserFiles", "Dosyalar silinirken hata oluştu: " + e.getMessage());
                Toast.makeText(getContext(), "Dosyalar silinirken hata: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }).addOnFailureListener(e -> {
            Log.w("DeleteUserFiles", "Klasör listelenemedi: " + e.getMessage());
            Toast.makeText(getContext(), "Klasör listelenemedi: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void deleteAllPostsFromFirestore(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Kullanicilar")
                .document(email)
                .collection("Posts")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            // Her belgeyi sil
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        // Her silme işlemi başarılı olduğunda buraya gelir
                                        //    Toast.makeText(getContext(), "Post silindi.", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Silme işlemi başarısız olursa buraya gelir
                                        Toast.makeText(getContext(), "Post silinirken hata oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }

                    } else {
                        Toast.makeText(getContext(), "Postlar alınamadı: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BLEService.LocalBinder binder = (BLEService.LocalBinder) service;
            bleService = binder.getService();
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // SharedPreferences'e "Bağlı Cihaz Yok" olarak güncelle
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("BluetoothPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("connectedDeviceName1", "Bağlı Cihaz Yok"); // Yeni varsayılan değer
            editor.apply();
            bleService = null;
            isServiceBound = false;

        }
    };

    private void toggleScanning() {
        if (isScanning) {
            stopScanning();
        } else {
            startScanning();
        }
    }

    private void startScanning() {
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            if (bluetoothLeScanner != null) {
                isScanning = true;
                deviceList.clear();
                deviceAdapter.clear();
                bluetoothLeScanner.startScan(scanCallback);
                Log.d(TAG, "Bluetooth tarama başlatıldı.");
            } else {
                Log.w(TAG, "BluetoothLeScanner null, tarama başlatılamadı.");
            }
        } else {
            Log.w(TAG, "Bluetooth etkin değil, tarama başlatılamıyor.");
            Toast.makeText(getContext(), "Bluetooth kapalı, tarama yapılamaz.", Toast.LENGTH_SHORT).show();
        }
    }


    private void stopScanning() {
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled() && bluetoothLeScanner != null) {
            isScanning = false;
            bluetoothLeScanner.stopScan(scanCallback);
        } else {
            Log.w(TAG, "Bluetooth zaten kapalı veya tarama yapmıyor.");
        }
    }


    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (!deviceList.contains(device)) {
                deviceList.add(device);
                deviceAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    };


    private void connectToDevice(BluetoothDevice device) {
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            try {
                device.createBond(); // Pairing işlemini başlatır
                Toast.makeText(getContext(), "Bağlanılıyor: " + device.getName(), Toast.LENGTH_SHORT).show();

                // Servis ile bağlantıyı başlat
                //  startForegroundServiceWithDevice(device);
            } catch (Exception e) {
                Log.e("ConnectToDevice", "Cihaza bağlanma hatası: " + e.getMessage());
                Toast.makeText(getContext(), "Cihaza bağlanırken hata oluştu.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Bluetooth etkin değil.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isServiceBound) {
          //  requireActivity().unbindService(serviceConnection);
          //  requireActivity().unregisterReceiver(bluetoothStateReceiver);


        }

    }


    private boolean arePermissionsGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 ve üzeri için BLE izinleri
            return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6 ve üzeri için konum izni
            return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android 6 öncesi için izin gerekmez
            return true;
        }
    }

    private void requestPermissions1() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 ve üzeri
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    REQUEST_PERMISSIONS);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6 ve üzeri
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    REQUEST_PERMISSIONS);
        }
    }








  /*  private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "Bluetooth Açık");
                        bluetoothacikkapalı_txt.setText("Bluetooth Açık");
                        BluetoothOnnOff.setVisibility(View.GONE);
                        animationView.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.VISIBLE);
                        animationView.playAnimation(); // Animasyonu başlat


                        // startScanning();


                        break;
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "Bluetooth Kapalı");
                        bluetoothacikkapalı_txt.setText("Bluetooth Kapalı");
                        bluetoothbaglicihaz_txt.setText("Kolye Bağlı Değil");
                        // stopScanning();
                        SharedPreferences sharedPreferencesble = requireContext().getSharedPreferences("bledurum", Context.MODE_PRIVATE);

                        SharedPreferences.Editor editorble = sharedPreferencesble.edit();

                        editorble.putBoolean("bledurumu", false);
                        editorble.apply();
                        listView.setVisibility(View.GONE);
                        animationView.pauseAnimation(); // Animasyonu durdur
                        BluetoothOnnOff.setVisibility(View.VISIBLE);
                        animationView.setVisibility(View.GONE);
                        break;
                }
            }
        }
    };
*/

    private void showBluetoothDisableDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Bluetooth'u Kapatmak mı İstiyorsunuz?");
        builder.setMessage("Bluetooth'u kapatmak istediğinizden emin misiniz?");

        builder.setPositiveButton("Evet", (dialog, which) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10 ve üzeri
                Toast.makeText(getContext(), "Bluetooth'u kapatmak için ayarlara yönlendiriliyorsunuz.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intent);
            } else {
                // Android 9 ve öncesi
                if (bluetoothAdapter != null) {
                    bluetoothAdapter.disable(); // Bluetooth'u kapat
                    Toast.makeText(getContext(), "Bluetooth kapatıldı.", Toast.LENGTH_SHORT).show();
                    animationView.pauseAnimation(); // Animasyonu durdur
                    animationView.pauseAnimation(); // Animasyonu durdur
                    animationView.setVisibility(View.GONE);
                    SharedPreferences sharedPreferencesble = requireContext().getSharedPreferences("bledurum", Context.MODE_PRIVATE);

                    SharedPreferences.Editor editorble = sharedPreferencesble.edit();

                    editorble.putBoolean("bledurumu", false);
                    editorble.apply();
                    BluetoothOnnOff.setVisibility(View.VISIBLE);
                    bluetoothbaglicihaz_txt.setText("Kolye Bağlı Değil");
                } else {
                    Toast.makeText(getContext(), "Bluetooth desteklenmiyor.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Hayır", (dialog, which) -> {
            dialog.dismiss(); // Diyaloğu kapat
        });

        builder.create().show();
    }


    /*  private void showBatteryOptimizationDialog() {
          AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
          builder.setTitle("Pil Optimizasyonu Gerekli")
                  .setMessage("Bu uygulama, sürekli çalışabilmesi için pil optimizasyonlarından muaf tutulmalıdır. Lütfen gerekli izni verin.")
                  .setPositiveButton("Tamam", (dialog, which) -> {
                      requestBatteryOptimizationPermission(); // Kullanıcıyı ayar sayfasına yönlendir
                  })
                  .setNegativeButton("İptal", (dialog, which) -> dialog.dismiss())
                  .show();
      }
  */
    private void redirectToBatterySettings() {
        Intent intent = new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(), "Pil ayarlarına erişilemiyor.", Toast.LENGTH_SHORT).show();
        }
    }

    // BLE adresini kaydet
    public void saveBLEAddress(String btIDValue) {
        SharedPreferences sharedPreferencesble = getContext().getApplicationContext().getSharedPreferences("partner_bilgileri", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorble = sharedPreferencesble.edit();
        editorble.putString("partner_btid", btIDValue);
        editorble.apply();
        Log.d("SharedPreferences", "BLE adresi başarıyla kaydedildi: " + btIDValue);
    }

    // BLE adresini çek
    public String getBLEAddress() {
        SharedPreferences sharedPreferencesble = getContext().getSharedPreferences("partner_bilgileri", Context.MODE_PRIVATE);
        return sharedPreferencesble.getString("partner_btid", null);
    }

    private void requestMiuiPowerManagerPermission() {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.powercenter.PowerSettings"
            ));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("PowerManager", "Miui power manager ayarlarına erişim başarısız", e);
        }
    }











    private void startForegroundServiceWithDevice(BluetoothDevice device) {
        Intent serviceIntent = new Intent(requireContext(), BluetoothForegroundService.class);
        serviceIntent.putExtra("device_address", device.getAddress());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(serviceIntent);
        } else {
            requireContext().startService(serviceIntent);
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //requestAllPermissions();

    }


    public void onDeviceFound(IntentSender chooserLauncher) {
        IntentSenderRequest request = new IntentSenderRequest.Builder(chooserLauncher).build();
        intentSenderLauncher.launch(request);
    }


    private String getPairedDevice() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("BluetoothPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("connectedDevice", "Eşleşmiş cihaz yok");
    }
    private void requestBatteryOptimizationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) requireContext().getSystemService(Context.POWER_SERVICE);

            if (powerManager != null && !powerManager.isIgnoringBatteryOptimizations(requireContext().getPackageName())) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                startActivity(intent);
            } else {
                Log.d("Fragment", "Pil optimizasyonu zaten devre dışı bırakılmış.");
            }
        }
    }

    private void requestDozeModePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) requireContext().getSystemService(Context.POWER_SERVICE);

            if (powerManager != null && !powerManager.isIgnoringBatteryOptimizations(requireContext().getPackageName())) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                startActivity(intent);
            } else {
                Log.d("Fragment", "Doze modu zaten devre dışı bırakılmış.");
            }
        }
    }



    private boolean checkBatteryOptimizationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) requireContext().getSystemService(Context.POWER_SERVICE);
            return powerManager != null && powerManager.isIgnoringBatteryOptimizations(requireContext().getPackageName());
        }
        return true; // Eski Android sürümleri için izin kontrolüne gerek yok
    }
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 ve sonrası için izinler
            if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        android.Manifest.permission.BLUETOOTH_SCAN,
                        android.Manifest.permission.BLUETOOTH_CONNECT,
                }, 1);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0+ için izinler
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                }, 1);
            }
        }
    }

    private int checkSelfPermission(String bluetoothScan) {
        return 1;
    }
    private void requestLocationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 (API 29) ve üzeri
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            }, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Android 6.0 - 9 (API 23-28)
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }



    private void requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) requireContext().getSystemService(Context.POWER_SERVICE);
            if (!powerManager.isIgnoringBatteryOptimizations(requireContext().getPackageName())) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Pil optimizasyonu zaten devre dışı", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void checkBluetoothStatus() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(requireContext(), "Bluetooth desteklenmiyor!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            // Bluetooth açık değilse kullanıcıdan açmasını iste
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 102); // 102 bir istek kodudur.
        } else {
            Toast.makeText(requireContext(), "Bluetooth zaten açık!", Toast.LENGTH_SHORT).show();
        }
    }


    private void requestAllPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Konum izni kontrolü
            if (requireContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                checkBackgroundLocationPermission();
            }
        } else {
            checkBluetoothStatus();
        }
    }

    private void checkBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (requireContext().checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 102);
            } else {
                checkBluetoothStatus();
            }
        } else {
            checkBluetoothStatus();
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Hassas konum izni verildi!", Toast.LENGTH_SHORT).show();
                checkBackgroundLocationPermission();
            } else {
                Toast.makeText(requireContext(), "Hassas konum izni gerekli!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 102) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Arka plan konum izni verildi!", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(requireContext(), "Arka plan konum izni gerekli!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 102) { // Bluetooth izin isteği kodu
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(requireContext(), "Bluetooth açıldı!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Bluetooth açma isteği reddedildi!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void partnerbilgilerinicek() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("partner_bilgileri", Context.MODE_PRIVATE);

        String kullaniciAdi = sharedPreferences.getString("partner_kullaniciadi", "Bilinmiyor");
        String kullaniciIsim = sharedPreferences.getString("partner_isim", "Bilinmiyor");
        String kullaniciEmail = sharedPreferences.getString("partner_email", "Bilinmiyor");
        String kullaniciTelefon = sharedPreferences.getString("partner_telefon", "Bilinmiyor");
        String btID = sharedPreferences.getString("partner_btid", "Bilinmiyor");

        Log.d("SharedPreferences", "Kullanıcı Adı: " + kullaniciAdi);
        Log.d("SharedPreferences", "Kullanıcı İsim: " + kullaniciIsim);
        Log.d("SharedPreferences", "Kullanıcı Email: " + kullaniciEmail);
        Log.d("SharedPreferences", "Kullanıcı Telefon: " + kullaniciTelefon);
        Log.d("SharedPreferences", "BT ID: " + btID);
    }


}


