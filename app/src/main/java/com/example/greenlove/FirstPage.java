package com.example.greenlove;

import static com.google.firebase.messaging.Constants.MessageNotificationKeys.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.greenlove.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class FirstPage extends AppCompatActivity {
    Button  first_login_bt,googlegiris_button;
    TextView first_register_bt,sifremiUnuttum;
    EditText first_kullaniciadi, first_kullanicisifre;
    FrameLayout seffaf;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirebaseFirestore;
    private DocumentReference mReference;
    private ProgressBar progressBar;
    Drawable[] drawables1;
    Drawable leftDrawable1;
  Drawable rightDrawable1;

    String txtkullaniciadi, txtsifre;
    ImageView showPasswordIcon;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private FirebaseFirestore mFirestore;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // Sadece Light Mode

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_first_page);

        first_register_bt = findViewById(R.id.first_register_bt);
        first_login_bt = findViewById(R.id.firstPage_login_bt);
        first_kullaniciadi = findViewById(R.id.firstPage_kullaniciadi);
        first_kullanicisifre = findViewById(R.id.firstPage_kullanicisifre);
        progressBar = findViewById(R.id.progressBar);
        // showPasswordIcon = findViewById(R.id.show_password_icon1);
        sifremiUnuttum = findViewById(R.id.sifremiUnuttum);
        seffaf = findViewById(R.id.overlay);
        googlegiris_button = findViewById(R.id.btn_googlegiris);


        // Firebase Auth ve Firestore başlat
        mFirestore = FirebaseFirestore.getInstance();
        // Giriş durumunu kontrol et
        if (isUserLoggedIn()) {
            // Kullanıcı giriş yapmış, doğrudan MainActivity'e yönlendir
            startActivity(new Intent(FirstPage.this, baslangic_activity.class));
            finish();
            return;
        }





        // Google Sign-In seçenekleri
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Web Client ID
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Google Sign-In Launcher
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        try {
                            GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(result.getData())
                                    .getResult(ApiException.class);
                            if (account != null) {
                                firebaseAuthWithGoogle(account.getIdToken());
                            }
                        } catch (ApiException e) {
                            Log.e("GoogleSignIn", "Google Sign-In Failed: " + e.getMessage());
                            Toast.makeText(this, "Google Sign-In Başarısız: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Google Sign-In İptal Edildi", Toast.LENGTH_SHORT).show();
                    }
                });






// Butona tıklama dinleyicisi ekleyin
            googlegiris_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Google giriş işlemini başlat
                    Log.d("GoogleSignIn", "Google Sign-In Button Clicked");

                    progressBar.setVisibility(View.VISIBLE);
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    googleSignInLauncher.launch(signInIntent);
                }
            });

            if (first_kullanicisifre != null) {
                Drawable[] drawables1 = first_kullanicisifre.getCompoundDrawables();
                leftDrawable1 = drawables1[0];
                rightDrawable1 = drawables1[2];
            } else {
                Log.e("FirstPage", "EditText first_kullanicisifre is null");
            }




        sifremiUnuttum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sifremiUnuttum(sifremiUnuttum);
            }
        });

        if (!internetBaglantisiVarMi(getApplicationContext())) {
            Toast.makeText(FirstPage.this, "İnternet Bağlantısını Kontrol Ediniz.", Toast.LENGTH_LONG).show();
        }

        mAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();

        first_register_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FirstPage.this, RegisterPage.class);
                startActivity(intent);
            }
        });



        // Sağdaki Drawable'ın tıklanıp tıklanmadığını kontrol et
        if (rightDrawable1 != null) {
            first_kullanicisifre.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // Tıklama başlangıcını kontrol et
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Log.d("TouchTest", "ACTION_DOWN Detected");
                    }

                    int drawableWidth = rightDrawable1.getBounds().width();  // Sağdaki drawable'ın genişliğini alıyoruz
                    int touchX = (int) event.getX();  // Dokunulan X koordinatını alıyoruz

                    Log.d("TouchTest", "TouchX: " + touchX + ", DrawableWidth: " + drawableWidth);

                    if (event.getAction() == MotionEvent.ACTION_UP && touchX >= (first_kullanicisifre.getWidth() - drawableWidth)) {
                        // Eğer tıklama sağdaki drawable bölgesindeyse
                        Log.d("TouchTest", "Touch event is on the right drawable!");
                        togglePasswordVisibility();  // Şifre görünürlüğünü toggle et
                        return true;  // Olay işlenmiş
                    }

                    return false;  // Olay işlenmemiş, diğer event'lere devam et
                }
            });
        }





        first_login_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtkullaniciadi = first_kullaniciadi.getText().toString();
                txtsifre = first_kullanicisifre.getText().toString();
                if (!TextUtils.isEmpty(txtkullaniciadi) && !TextUtils.isEmpty(txtsifre)) {

                    // Show ProgressBar
                    findViewById(R.id.overlay).setVisibility(View.VISIBLE); // Katmanı gizle

                    progressBar.setVisibility(View.VISIBLE);

                    mAuth.signInWithEmailAndPassword(txtkullaniciadi, txtsifre)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        if (user != null) {
                                            if (user.isEmailVerified()) {
                                                // Kullanıcının e-postası doğrulanmış
                                                Toast.makeText(FirstPage.this, "Giriş Başarılı", Toast.LENGTH_LONG).show();

                                                SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putString("kemail1", first_kullaniciadi.getText().toString());
                                                editor.apply();

                                                // Giriş durumunu kaydet
                                                setUserLoggedIn(true);

                                                Intent intent = new Intent(FirstPage.this, baslangic_activity.class);
                                                startActivity(intent);
                                                finish(); // FirstPage aktivitesini kapat
                                            } else {
                                                // Kullanıcının e-postası doğrulanmamış
                                                mAuth.signOut(); // Kullanıcıyı çıkış yaptır
                                                progressBar.setVisibility(View.GONE); // ProgressBar'ı gizle
                                                findViewById(R.id.overlay).setVisibility(View.GONE); // Katmanı gizle

                                                Toast.makeText(FirstPage.this, "E-posta adresiniz doğrulanmamış. Lütfen doğrulama yapın.", Toast.LENGTH_LONG).show();
                                            }

                                        }
                                    } else {
                                        // Hide ProgressBar
                                       progressBar.setVisibility(View.GONE);
                                        findViewById(R.id.overlay).setVisibility(View.GONE); // Katmanı gizle

                                        Toast.makeText(FirstPage.this, "Giriş Başarısız. Lütfen bilgilerinizi kontrol edin.", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }).addOnFailureListener(FirstPage.this, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Hide ProgressBar
                                    progressBar.setVisibility(View.GONE);
                                    findViewById(R.id.overlay).setVisibility(View.GONE); // Katmanı gizle

                                    Toast.makeText(FirstPage.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                } else {
                    Toast.makeText(FirstPage.this, "Kullanıcı Adı ve Şifre Boş Bırakılamaz", Toast.LENGTH_LONG).show();
                }
            }
        });
       /* showPasswordIcon.setOnClickListener(new View.OnClickListener() {
            boolean isPasswordVisible = false;

            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });


*/
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            Window window = getWindow();
            window.setNavigationBarColor(Color.BLACK); // Navigation bar rengini siyah yap
        return insets;
        });

    }
    // Firebase ile Google kullanıcısını doğrula

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        // Firebase Auth ile giriş
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();

                        if (firebaseUser != null) {
                            String email = firebaseUser.getEmail();
                            String displayName = firebaseUser.getDisplayName();
                            String uid = firebaseUser.getUid();

                            if (email != null) {
                                saveUserToFirestore(email, displayName, uid); // Kullanıcıyı Firestore'a kaydet
                                SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("kemail1", email);
                                editor.apply();
                                setUserLoggedIn(true);

                            } else {
                                Toast.makeText(this, "Google hesabından e-posta alınamadı.", Toast.LENGTH_SHORT).show();
                                signOutImmediately(); // Oturumu kapat
                            }
                        }
                    } else {
                        Log.e("FirebaseAuth", "Authentication Failed: " + task.getException().getMessage());
                        Toast.makeText(this, "Giriş Başarısız. Lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToFirestore(String email, String displayName, String uid) {
        // Kullanıcı bilgilerini bir HashMap'e ekleyin
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("kullanici_ID", uid);
        userData.put("kullanici_email", email);
        userData.put("kullanici_isim", displayName);

        // Firestore'a kaydet
        mFirestore.collection("Kullanicilar")
                .document(email) // Email'i belge adı olarak kullan
                .set(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        progressBar.setVisibility(View.GONE);

                        Toast.makeText(this, "Giriş Başarılı. Hoş Geldiniz!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, baslangic_activity.class)); // Başlangıç sayfasına yönlendirin
                        finish();
                    } else {
                        Log.e("Firestore", "Kullanıcı kaydedilemedi: " + task.getException().getMessage());
                        Toast.makeText(this, "Kullanıcı bilgileri kaydedilemedi.", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void checkIfUserExistsInFirestore(String email) {
        mFirestore.collection("Kullanicilar").document(email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {

                            // Kullanıcı mevcut, giriş başarılı
                            Toast.makeText(this, "Giriş Başarılı. Hoş Geldiniz!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, baslangic_activity.class));
                            finish();
                        } else {
                            // Kullanıcı mevcut değil
                            Toast.makeText(this, "Bu e-posta ile kayıtlı kullanıcı bulunamadı.", Toast.LENGTH_SHORT).show();
                            signOutImmediately(); // Oturumu hemen kapat
                        }
                    } else {
                        Log.e("Firestore", "Kullanıcı kontrolü sırasında hata: " + task.getException().getMessage());
                        Toast.makeText(this, "Bir hata oluştu. Lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show();
                        signOutImmediately(); // Oturumu hemen kapat
                    }
                });
    }

    private void signOutImmediately() {
        mAuth.signOut(); // Firebase Authentication oturumunu kapat
        mGoogleSignInClient.signOut(); // Google oturumunu kapat
    }


    private void signInWithFirebaseAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        Toast.makeText(this, "Giriş Başarılı. Hoş Geldiniz!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, baslangic_activity.class));
                        finish();
                    } else {
                        Log.e("FirebaseAuth", "Authentication Failed: " + task.getException().getMessage());
                        Toast.makeText(this, "Giriş Başarısız. Lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void checkUserInFirestore(String email, String idToken) {
        mFirestore.collection("Kullanicilar").document(email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Kullanıcı mevcut, şimdi Firebase Authentication'da giriş yap
                            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
                            mAuth.signInWithCredential(credential)
                                    .addOnCompleteListener(authTask -> {
                                        if (authTask.isSuccessful()) {
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            Toast.makeText(this, "Hoş Geldiniz: " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(this, baslangic_activity.class));
                                            finish();
                                        } else {
                                            Toast.makeText(this, "Firebase Auth Hatası: " + authTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // Kullanıcı Firestore'da mevcut değil, giriş yapılmasın
                            Toast.makeText(this, "Bu e-posta ile kayıtlı kullanıcı bulunamadı.", Toast.LENGTH_SHORT).show();
                            mGoogleSignInClient.signOut(); // Oturumu kapat
                        }
                    } else {
                        Toast.makeText(this, "Firestore Hatası: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }




    public boolean isUserLoggedIn() {
        SharedPreferences preferences = getSharedPreferences("user_pref", MODE_PRIVATE);
        return preferences.getBoolean("is_logged_in", false);
    }

    private void setUserLoggedIn(boolean loggedIn) {
        SharedPreferences preferences = getSharedPreferences("user_pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("is_logged_in", loggedIn);
        editor.apply();
    }

    public static boolean internetBaglantisiVarMi(Context context) {
        // ConnectivityManager kullanarak sistem servisini alıyoruz
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // Aktif ağ bilgisini alıyoruz
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        // Bağlı olduğumuz ağın bilgisi var mı ve bağlantı sağlanıyor mu kontrol ediyoruz
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }
    public void sifremiUnuttum(View view) {
        Intent intent = new Intent(this, sifremiUnuttum.class);
        startActivity(intent);
    }
    // Şifre görünürlüğünü değiştir
    private void togglePasswordVisibility() {
        if (first_kullanicisifre.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            first_kullanicisifre.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            first_kullanicisifre.setCompoundDrawablesWithIntrinsicBounds(leftDrawable1, null, getResources().getDrawable(R.drawable.eyes_asset2), null); // Göz ikonu
        } else {
            first_kullanicisifre.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            first_kullanicisifre.setCompoundDrawablesWithIntrinsicBounds(leftDrawable1, null, getResources().getDrawable(R.drawable.noteyes_asset1), null); // Göz kapalı ikonu
        }
        first_kullanicisifre.setSelection(first_kullanicisifre.length()); // İmleci sona koyuyoruz
    }
    private void togglePasswordVisibility1() {
        if (first_kullanicisifre.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            // If password is visible, change it to hidden and update the drawable
            first_kullanicisifre.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            Toast.makeText(this,"naber",Toast.LENGTH_LONG).show();
            first_kullanicisifre.setCompoundDrawablesWithIntrinsicBounds(leftDrawable1, null, getResources().getDrawable(R.drawable.eyes_asset2), null); // Preserve left drawable and update right drawable to hide icon
        } else {
            // If password is hidden, change it to visible and update the drawable
            first_kullanicisifre.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            Toast.makeText(this,"naber222",Toast.LENGTH_LONG).show();
            first_kullanicisifre.setCompoundDrawablesWithIntrinsicBounds(leftDrawable1, null, getResources().getDrawable(R.drawable.noteyes_asset2), null); // Preserve left drawable and update right drawable to show icon
        }
        first_kullanicisifre.setSelection(first_kullanicisifre.length()); // Ensure the cursor stays at the end
    }



}
/*rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}


 */