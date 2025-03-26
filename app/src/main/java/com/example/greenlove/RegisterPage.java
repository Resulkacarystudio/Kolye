package com.example.greenlove;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterPage extends AppCompatActivity {
    private static final String KULLANICI_ADI_PATTERN = "^[a-zA-Z0-9_]+$";
    FrameLayout seffaf;
    private static final Pattern pattern = Pattern.compile(KULLANICI_ADI_PATTERN);
    StringBuilder hataMesaji = new StringBuilder();
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private HashMap<String, Object> mData;
    private FirebaseFirestore mFirebasestore;

    private EditText kullanici_adi, kullanici_sifre,kullanici_sifre_tekrar, kullanici_ad, kullanici_email, kullanici_telefonno;
    private TextView kayitolpage_girisyap_button;
    private Button kayitol_button,google_button;
    private TextView kayitol_kurallar;
    private ProgressBar progressBar;
    ImageView showPasswordIcon;
    String kullaniciAdi;
    String sifre;
    String sifre_tekrar;
    String email;
    String telefonNo;
    Drawable[] drawables;
    Drawable leftDrawable;
    Drawable rightDrawable;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 100; // Google Sign-In için dönüş kodu
    private ActivityResultLauncher<Intent> googleSignInLauncher;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_page);


            //showPasswordIcon = findViewById(R.id.show_password_icon);
            kullanici_adi = findViewById(R.id.kayitol_kullaniciadi);
            kullanici_sifre = findViewById(R.id.kayitol_kullanicisifre);
            kullanici_sifre_tekrar = findViewById(R.id.kullanici_sifre_tekrar);
            kullanici_ad = findViewById(R.id.kayitol_ad);
            kullanici_email = findViewById(R.id.kayitol_email);
            kullanici_telefonno = findViewById(R.id.kayitol_telefon);
            kayitol_button = findViewById(R.id.kayitolpage_kayitbutton);
           // kayitol_kurallar = findViewById(R.id.kayitol_kurallar);
            progressBar = findViewById(R.id.progressBar);
            seffaf = findViewById(R.id.overlay);
            kayitolpage_girisyap_button = findViewById(R.id.kayitolpage_girisyap_button);
            google_button = findViewById(R.id.btn_google);


             drawables = kullanici_sifre.getCompoundDrawables();
             leftDrawable = drawables[0]; // This is the drawableStart (left)
           rightDrawable = drawables[2]; // This is the drawableEnd (right)
        kullanici_sifre.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(this, R.drawable.sifre_asset1), // Sol (left)
                null, // Üst (top)
                ContextCompat.getDrawable(this, R.drawable.eyes_asset2), // Sağ (right)
                null  // Alt (bottom)
        );
/*
        kullanici_sifre_tekrar.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(this, R.drawable.sifre_asset1), // Sol (left)
                null, // Üst (top)
                ContextCompat.getDrawable(this, R.drawable.eyes_asset2), // Sağ (right)
                null  // Alt (bottom)
        );
*/

        Log.d("DrawablesTest", "Left: " + drawables[0] + ", Right: " + drawables[2] );

// ActivityResultLauncher başlat (onCreate içinde doğrudan)
            googleSignInLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                            try {
                                GoogleSignInAccount account = task.getResult(ApiException.class);
                                if (account != null) {
                                    firebaseAuthWithGoogle(account.getIdToken());
                                }
                            } catch (ApiException e) {
                                Log.e("GoogleSignIn", "Google Sign-In Failed: " + e.getMessage());
                                Toast.makeText(this, "Google Sign-In Başarısız: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("GoogleSignIn", "Sign-In Cancelled or Failed");
                            Toast.makeText(this, "Sign-In Cancelled or Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        if (isUserLoggedIn()) {
            // Kullanıcı giriş yapmış, doğrudan MainActivity'e yönlendir
            startActivity(new Intent(RegisterPage.this, baslangic_activity.class));
            finish();
            return;
        }

            // GoogleSignInClient başlat
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id)) // Firebase Web Client ID
                    .requestEmail()
                    .build();
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);





            // Kendi Google Giriş Butonunu Ayarla
           google_button.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   Log.d("GoogleSignIn", "Google Sign-In Button Clicked");

                   progressBar.setVisibility(View.VISIBLE);
                   Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                   googleSignInLauncher.launch(signInIntent);
               }
           });






            kayitolpage_girisyap_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RegisterPage.this,FirstPage.class);
                    startActivity(intent);
                }
            });


            mAuth = FirebaseAuth.getInstance();
            mFirebasestore = FirebaseFirestore.getInstance();

            setFocusChangeListeners();

            kayitol_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleRegistration();
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







// Check if the right drawable exists
            if (rightDrawable != null) {
                kullanici_sifre.setOnTouchListener(new View.OnTouchListener() {
                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        @SuppressLint("ClickableViewAccessibility") int drawableWidth = rightDrawable.getBounds().width(); // Get width of the right drawable
                        int touchX = (int) event.getX();

                        System.out.println("burada");
                        // Check if the touch is within the bounds of the right drawable
                        if (event.getAction() == MotionEvent.ACTION_UP && touchX >= (kullanici_sifre.getWidth() - drawableWidth)) {
                            // Handle the icon touch (e.g., toggle password visibility)
                            togglePasswordVisibility1();
                            return true; // Event handled
                        }
                        return false; // Event not handled, pass it to the next view
                    }
                });
            } else {
                // Handle the case where the right drawable is not set
                // Optionally set the right drawable here if necessary
                System.out.println("burası boş");
            }

            if (rightDrawable != null) {
                kullanici_sifre_tekrar.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int drawableWidth = rightDrawable.getBounds().width(); // Get width of the right drawable
                        int touchX = (int) event.getX();

                        // Check if the touch is within the bounds of the right drawable
                        if (event.getAction() == MotionEvent.ACTION_UP && touchX >= (kullanici_sifre.getWidth() - drawableWidth)) {
                            // Handle the icon touch (e.g., toggle password visibility)

                            togglePasswordVisibility2();
                            return true; // Event handled
                        }
                        return false; // Event not handled, pass it to the next view
                    }
                });
            } else {
                // Handle the case where the right drawable is not set
                // Optionally set the right drawable here if necessary
            }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            Window window = getWindow();
            window.setNavigationBarColor(Color.BLACK); // Navigation bar rengini siyah yap
            return insets;
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("GoogleSignIn", "onActivityResult  girdi"); // Log testi

        if (requestCode == RC_SIGN_IN) {
            Log.d("GoogleSignIn", "onActivityResult Called"); // Log testi
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                if (account != null) {
                    Log.d("GoogleSignIn", "Account Retrieved: " + account.getEmail()); // Log
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Log.e("GoogleSignIn", "Google Sign-In Failed: " + e.getMessage());
                Toast.makeText(this, "Google Sign-In Başarısız: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        // Firebase Authentication ile oturum aç
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String name = firebaseUser.getDisplayName(); // Kullanıcı adı
                            String email = firebaseUser.getEmail(); // Kullanıcı e-posta
                            String uid = firebaseUser.getUid(); // Firebase UID

                            // Kullanıcı bilgilerini Firestore'a kaydet
                            saveBasicUserInfoToDatabase(name, email, uid);
                            SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("kemail1", email);
                            editor.apply();
                            setUserLoggedIn(true);
                            // Kullanıcıyı uygulamanın ana sayfasına yönlendir
                            progressBar.setVisibility(View.GONE);

                            Toast.makeText(this, "Giriş Başarılı. Hoş Geldiniz!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, baslangic_activity.class));
                            finish();
                        }
                    } else {
                        Log.e("FirebaseAuth", "Authentication Failed: " + task.getException().getMessage());
                        Toast.makeText(this, "Google hesabıyla giriş başarısız: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void saveBasicUserInfoToDatabase(String name, String email, String uid) {
        // Kullanıcı bilgilerini bir HashMap'e ekle
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("kullanici_ID", uid);
        userData.put("kullanici_email", email);
        userData.put("kullanici_isim", name);

        // Firestore'a kaydet
        mFirebasestore.collection("Kullanicilar")
                .document(email) // Email'i belge adı olarak kullan
                .set(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Firestore", "Kullanıcı başarıyla Firestore'a kaydedildi.");
                        Toast.makeText(this, "Kayıt başarılı.", Toast.LENGTH_SHORT).show();



                    } else {
                        Log.e("Firestore", "Kayıt başarısız: " + task.getException().getMessage());
                        Toast.makeText(this, "Kayıt başarısız oldu: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Veri yazma hatası: " + e.getMessage());
                    Toast.makeText(this, "Veritabanı hatası: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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


    private void setFocusChangeListeners() {
        setFocusChangeListener(kullanici_adi, R.string.kullanici_adi_kurallar);
        setFocusChangeListener(kullanici_sifre, R.string.kullanici_sifre_kurallar);
        setFocusChangeListener(kullanici_email, R.string.kullanici_email_kurallar);
        setFocusChangeListener(kullanici_telefonno, R.string.kullanici_telefonno_kurallar);
    }

    private void setFocusChangeListener(EditText editText, int rulesResource) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
               // kayitol_kurallar.setTextColor(getResources().getColor(R.color.black));
                //kayitol_kurallar.setText(rulesResource);
            } else {
                //kayitol_kurallar.setText("");
            }
        });
    }

    private void togglePasswordVisibility() {
        boolean isPasswordVisible = (kullanici_sifre.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        if (isPasswordVisible) {
            kullanici_sifre.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
           //// showPasswordIcon.setImageResource(R.drawable.eyes20);
        } else {
            kullanici_sifre.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
          //  showPasswordIcon.setImageResource(R.drawable.eyes21);
        }
        kullanici_sifre.setSelection(kullanici_sifre.length());
    }

    private void handleRegistration() {
        hataMesaji = new StringBuilder("");
         kullaniciAdi = kullanici_adi.getText().toString().trim();
         sifre = kullanici_sifre.getText().toString().trim();
         sifre_tekrar = kullanici_sifre_tekrar.getText().toString().trim();
         email = kullanici_email.getText().toString().trim();
         telefonNo = kullanici_telefonno.getText().toString().trim();

        boolean tumKurallarUygun = tumKurallarUygun(kullaniciAdi, sifre, email, telefonNo);

        if (tumKurallarUygun) {
            progressBar.setVisibility(View.VISIBLE);
            findViewById(R.id.overlay).setVisibility(View.VISIBLE); // Katmanı göst
            mAuth.createUserWithEmailAndPassword(email, sifre)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressBar.setVisibility(View.GONE);
                            findViewById(R.id.overlay).setVisibility(View.GONE); // Katmanı gizle

                            if (task.isSuccessful()) {
                                mUser = mAuth.getCurrentUser();
                                if (mUser != null) {
                                    sendEmailVerification();
                                }
                            } else {
                                Toast.makeText(RegisterPage.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
           // kayitol_kurallar.setText(hataMesaji.toString());
        }
    }

    private void sendEmailVerification() {
        mUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(RegisterPage.this, "Doğrulama e-postası gönderildi. Lütfen e-postanızı kontrol edin.", Toast.LENGTH_LONG).show();
                    saveUserToDatabase();
                } else {
                    Toast.makeText(RegisterPage.this, "E-posta doğrulama gönderilemedi: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void saveUserToDatabase() {
        mData = new HashMap<>();
       // mData.put("kullanici_adi", kullanici_adi.getText().toString());
        //mData.put("kullanici_sifre", kullanici_sifre.getText().toString());
        mData.put("kullanici_isim", kullanici_ad.getText().toString());
        mData.put("kullanici_email", kullanici_email.getText().toString());
        //mData.put("kullanici_telefon", kullanici_telefonno.getText().toString());
        mData.put("kullanici_ID", mUser.getUid());

        mFirebasestore.collection("Kullanicilar").document(kullanici_email.getText().toString())
                .set(mData)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterPage.this, "Kayıt İşlemi Başarılı", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(RegisterPage.this, FirstPage.class));
                    } else {
                        Toast.makeText(RegisterPage.this, "Veritabanına kayıt başarısız oldu: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    public static boolean kullaniciAdiGecerliMi(String kullaniciAdi) {
        Matcher matcher = pattern.matcher(kullaniciAdi);
        return matcher.matches();
    }

    public static  boolean kullaniciSifreAyniMi(String sifre,String sifre_tekrar)
    {
        if(!sifre.equals(sifre_tekrar))
        {
            return  false;
        }
        return true;
    }

    public static boolean sifreKurallaraUygun(String sifre) {
        if (sifre.length() < 8) return false;


        boolean harfVar = false;
        boolean rakamVar = false;

        for (char karakter : sifre.toCharArray()) {
            if (Character.isLetter(karakter)) harfVar = true;
            else if (Character.isDigit(karakter)) rakamVar = true;
            else if (karakter != '_' && karakter != '.') return false;
        }

        for (char c : new char[]{'ı', 'ü', 'ö', 'ğ', 'ç'}) {
            if (sifre.contains(String.valueOf(c))) return false;
        }

        return harfVar && rakamVar;
    }

    public static boolean emailKurallaraUygun(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.split("\\.").length <= 4;
    }

    public static boolean telefonNoKurallaraUygun(String telefonNo) {
        return telefonNo.length() == 11 && telefonNo.charAt(0) == '0' && telefonNo.chars().allMatch(Character::isDigit);
    }
/*
    public boolean tumKurallarUygun(String kullaniciAdi, String sifre, String email, String telefonNo) {
        if (!kullaniciAdiGecerliMi(kullaniciAdi)) hataMesaji.append("Geçersiz Kullanıcı Adı.\n");
        if (!sifreKurallaraUygun(sifre)) hataMesaji.append("Geçersiz Şifre.\n");
        if (!emailKurallaraUygun(email)) hataMesaji.append("Geçersiz E-posta Adresi.\n");
        if (!telefonNoKurallaraUygun(telefonNo)) hataMesaji.append("Geçersiz Telefon Numarası.\n");

        if (hataMesaji.length() == 0) return true;
        else {
            kayitol_kurallar.setTextColor(Color.RED);
            return false;
        }
    }
    */
public boolean tumKurallarUygun(String kullaniciAdi, String sifre, String email, String telefonNo) {
    StringBuilder hataMesaji = new StringBuilder();

    if (!kullaniciAdiGecerliMi(kullaniciAdi))
        hataMesaji.append("Geçersiz Kullanıcı Adı.\n");
    if (!sifreKurallaraUygun(sifre))
        hataMesaji.append("Geçersiz Şifre.\n");
    if (!emailKurallaraUygun(email))
        hataMesaji.append("Geçersiz E-posta Adresi.\n");
    if (!telefonNoKurallaraUygun(telefonNo))
        hataMesaji.append("Geçersiz Telefon Numarası.\n");
    if (!kullaniciSifreAyniMi(sifre,sifre_tekrar))
        hataMesaji.append("Şifreler AynI Olmak Zorunda");

    if (hataMesaji.length() == 0) {
        return true;
    } else {
        // Hataları bir AlertDialog ile göster
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hata Mesajı")
                .setMessage(hataMesaji.toString())
                .setPositiveButton("Tamam", (dialog, which) -> dialog.dismiss())
                .show();

        return false;
    }
}
    // Toggle password visibility
    // Toggle password visibility
    private void togglePasswordVisibility1() {
        if (kullanici_sifre.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            // If password is visible, change it to hidden and update the drawable
            kullanici_sifre.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            kullanici_sifre.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null, getResources().getDrawable(R.drawable.eyes_asset2), null); // Preserve left drawable and update right drawable to hide icon
        } else {
            // If password is hidden, change it to visible and update the drawable
            kullanici_sifre.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            kullanici_sifre.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null, getResources().getDrawable(R.drawable.noteyes_asset2), null); // Preserve left drawable and update right drawable to show icon
        }
        kullanici_sifre.setSelection(kullanici_sifre.length()); // Ensure the cursor stays at the end
    }

    private void togglePasswordVisibility2() {
        if (kullanici_sifre_tekrar.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            // If password is visible, change it to hidden and update the drawable
            kullanici_sifre_tekrar.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            kullanici_sifre_tekrar.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null, getResources().getDrawable(R.drawable.eyes_asset2), null); // Preserve left drawable and update right drawable to hide icon
        } else {
            // If password is hidden, change it to visible and update the drawable
            kullanici_sifre_tekrar.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            kullanici_sifre_tekrar.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null, getResources().getDrawable(R.drawable.noteyes_asset2), null); // Preserve left drawable and update right drawable to show icon
        }
        kullanici_sifre_tekrar.setSelection(kullanici_sifre_tekrar.length()); // Ensure the cursor stays at the end
    }




}
