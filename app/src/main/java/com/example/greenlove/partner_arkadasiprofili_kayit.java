package com.example.greenlove;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.checkerframework.checker.nullness.qual.NonNull;

public class partner_arkadasiprofili_kayit extends AppCompatActivity {


    private  ImageView partner_image;
    private EditText emailEditText;
    private Button fetchButton;
    private TextView partnerName, partnerUsername, partnerEmail, partnerPhone, partnerBtId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partner_arkadasiprofili_kayit);

        // XML elemanlarını tanımlıyoruz
        emailEditText = findViewById(R.id.emailEditText);
        fetchButton = findViewById(R.id.fetchButton);
        partnerName = findViewById(R.id.partner_name);
        partnerUsername = findViewById(R.id.partner_username);
        partnerEmail = findViewById(R.id.partner_email);
        partnerPhone = findViewById(R.id.partner_phone);
        partnerBtId = findViewById(R.id.partner_btid);
        partner_image = findViewById(R.id.partner_image);


        // Buton tıklama işlemi
        fetchButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Lütfen bir email adresi girin", Toast.LENGTH_SHORT).show();
                return;
            }

            fetchPartnerData(email);
        });

        // Kaydedilmiş partner bilgilerini göster
        loadPartnerDetails();
    }

    private void fetchPartnerData(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Kullanicilar").document(email);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Kullanıcı bilgilerini al
                        String kullaniciAdi = document.getString("kullanici_adi");
                        String kullaniciIsim = document.getString("kullanici_isim");
                        String kullaniciEmail = document.getString("kullanici_email");
                        String kullaniciTelefon = document.getString("kullanici_telefon");
                        String btID = document.getString("btID");

                        getUserProfileImage(kullaniciEmail,partner_image);

                        // Kullanıcı bilgilerini UI'da göster
                        displayPartnerDetails(kullaniciAdi, kullaniciIsim, kullaniciEmail, kullaniciTelefon, btID);

                        // Kullanıcı bilgilerini SharedPreferences ile kaydet
                        saveUserDetailsSeparately(kullaniciAdi, kullaniciIsim, kullaniciEmail, kullaniciTelefon, btID);

                        Log.d("Firestore", "Kullanıcı bilgileri başarıyla çekildi ve kaydedildi.");
                    } else {
                        Log.d("Firestore", "Belirtilen e-posta ile eşleşen bir kullanıcı bulunamadı.");
                        Toast.makeText(partner_arkadasiprofili_kayit.this, "Kullanıcı bulunamadı.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e("Firestore", "Kullanıcı bilgileri çekilemedi.", task.getException());
                    Toast.makeText(partner_arkadasiprofili_kayit.this, "Kullanıcı bilgileri çekilemedi.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void saveUserDetailsSeparately(String kullaniciAdi, String kullaniciIsim, String kullaniciEmail, String kullaniciTelefon, String btID) {
        SharedPreferences sharedPreferences = getSharedPreferences("partner_bilgileri", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("partner_kullaniciadi", kullaniciAdi);
        editor.putString("partner_isim", kullaniciIsim);
        editor.putString("partner_email", kullaniciEmail);
        editor.putString("partner_telefon", kullaniciTelefon);
        editor.putString("partner_btid", btID);
        editor.apply();

        Log.d("SharedPreferences", "Kullanıcı bilgileri ayrı ayrı kaydedildi.");
    }

    private void loadPartnerDetails() {
        SharedPreferences sharedPreferences = getSharedPreferences("partner_bilgileri", Context.MODE_PRIVATE);

        String kullaniciAdi = sharedPreferences.getString("partner_kullaniciadi", "Bilinmiyor");
        String kullaniciIsim = sharedPreferences.getString("partner_isim", "Bilinmiyor");
        String kullaniciEmail = sharedPreferences.getString("partner_email", "Bilinmiyor");
        String kullaniciTelefon = sharedPreferences.getString("partner_telefon", "Bilinmiyor");
        String btID = sharedPreferences.getString("partner_btid", "Bilinmiyor");

        displayPartnerDetails(kullaniciAdi, kullaniciIsim, kullaniciEmail, kullaniciTelefon, btID);
    }

    private void displayPartnerDetails(String kullaniciAdi, String kullaniciIsim, String kullaniciEmail, String kullaniciTelefon, String btID) {
        partnerName.setText("Adı: " + kullaniciIsim);
        partnerUsername.setText("Kullanıcı Adı: " + kullaniciAdi);
        partnerEmail.setText("Email: " + kullaniciEmail);
        partnerPhone.setText("Telefon: " + kullaniciTelefon);
        partnerBtId.setText("BT ID: " + btID);
        getUserProfileImage(kullaniciEmail,partner_image);

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
                ImageView partner_image = view.findViewById(R.id.partner_image);
                Glide.with(view)
                        .load(uri)
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.profilfotokullanici) // Yüklenirken gösterilecek yer tutucu
                                .circleCrop()) // Resmi yuvarlak yap
                        .into(partner_image);



            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@androidx.annotation.NonNull Exception e) {
                // Hata durumunda varsayılan resmi göster
                ImageView kolyearkadasi_profilimage = view.findViewById(R.id.partner_image);
                kolyearkadasi_profilimage.setImageResource(R.drawable.logo_circle);
                Log.w("Storage", "Resim yüklenemedi: " + e.getMessage());
            }
        });
    }
}
