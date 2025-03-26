package com.example.greenlove;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

public class FullscreenImageActivity extends AppCompatActivity {
    private List<String> imageList; // Görüntülerin URL'lerini tutan liste
    private int initialPosition; // Başlangıç pozisyonu, yani hangi resmin başlangıçta görüntüleneceği
    private Button deleteButton, backButton; // Silme ve geri butonları
    private ViewPager2 viewPager; // Resimleri kaydırarak görüntülemek için ViewPager2
    private ImagePagerAdapter adapter; // Resimleri göstermek için kullanılan adapter
    private ProgressBar progressBar; // ProgressBar tanımı
    String email1; // Kullanıcının email adresi
    String profil_mail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);

        // ProgressBar'ı tanımla
        progressBar = findViewById(R.id.progressBar);

        // Profil bilgilerini almak için SharedPreferences kullanılıyor
        SharedPreferences profilsahibi = getSharedPreferences("ProfilKullanici", MODE_PRIVATE);
        email1 = profilsahibi.getString("email", "Kullanıcı Email"); // Kaydedilmiş email adresini alıyoruz
        SharedPreferences sharedPreferences1 = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        profil_mail = sharedPreferences1.getString("kemail1", "Misafir");

        viewPager = findViewById(R.id.view_pager); // XML'den ViewPager2 bileşenini buluyoruz
        deleteButton = findViewById(R.id.delete_button); // Silme butonunu buluyoruz
        backButton = findViewById(R.id.back_button); // Geri butonunu buluyoruz

        imageList = getIntent().getStringArrayListExtra("image_list"); // Görüntü URL'lerini içeren listeyi alıyoruz
        initialPosition = getIntent().getIntExtra("initial_position", 0); // Başlangıçta gösterilecek pozisyonu alıyoruz

        if (imageList != null && !imageList.isEmpty()) {
            // Eğer resim listesi boş değilse adapter oluşturuluyor ve ViewPager'e atanıyor
            adapter = new ImagePagerAdapter(this, imageList);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(initialPosition, false); // Belirtilen pozisyondan başlıyor

            // Silme butonuna tıklanınca `deleteImage()` metodunu çalıştırıyoruz
            deleteButton.setOnClickListener(view -> deleteImage(viewPager.getCurrentItem())); // Hangi pozisyonda olduğumuzu alıyoruz

            // Geri butonuna tıklanınca aktiviteyi kapatıyoruz
            backButton.setOnClickListener(v -> finish());
        } else {
            // Eğer liste boşsa aktiviteyi kapatıyoruz
            finish();
        }
    }

    // Silme işlemi için kullanılan metod
    private void deleteImage(int position) {
        String imageUrl = imageList.get(position); // O anki resmin URL'sini alıyoruz

        // Silme işlemi başladığında ProgressBar'ı görünür yap
        progressBar.setVisibility(View.VISIBLE);

        // Firestore'dan silme işlemi
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Kullanicilar").document(profil_mail).collection("Posts")
                .whereEqualTo("image_url", imageUrl) // Firestore'da bu URL'ye sahip belgeyi buluyoruz
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Eğer belge bulunduysa, silme işlemini başlatıyoruz
                        queryDocumentSnapshots.getDocuments().get(0).getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Firebase Storage'dan resmi de siliyoruz
                                    FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl).delete()
                                            .addOnSuccessListener(bVoid -> {
                                                // Eğer her şey yolunda gittiyse, kullanıcı arayüzünden resmi kaldırıyoruz
                                                imageList.remove(position); // Listedeki resmi kaldırıyoruz
                                                adapter.notifyItemRemoved(position); // Adapter'ı bilgilendiriyoruz
                                                adapter.notifyItemRangeChanged(position, imageList.size()); // Kalan elemanların pozisyonlarını güncelliyoruz
                                                Toast.makeText(FullscreenImageActivity.this, "Fotoğraf Başarıyla Silindi.", Toast.LENGTH_SHORT).show();

                                                // Eğer liste boşsa, aktiviteyi kapatıyoruz
                                                if (imageList.isEmpty()) {
                                                    finish();
                                                }

                                                // Silme işlemi başarıyla tamamlandığında ProgressBar'ı gizle
                                                progressBar.setVisibility(View.GONE);
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(FullscreenImageActivity.this, "Fotoğraf Silinemedi.", Toast.LENGTH_SHORT).show();
                                                // Hata durumunda ProgressBar'ı gizle
                                                progressBar.setVisibility(View.GONE);
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(FullscreenImageActivity.this, "Fotoğraf Silinemedi.", Toast.LENGTH_SHORT).show();
                                    // Hata durumunda ProgressBar'ı gizle
                                    progressBar.setVisibility(View.GONE);
                                });
                    } else {
                        Toast.makeText(FullscreenImageActivity.this, "Kolye Arkadaşınızın Fotoğraflarını Silemezsiniz.", Toast.LENGTH_SHORT).show();
                        // Hata durumunda ProgressBar'ı gizle
                        progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FullscreenImageActivity.this, "Fotoğraf Alınamadı.", Toast.LENGTH_SHORT).show();
                    // Hata durumunda ProgressBar'ı gizle
                    progressBar.setVisibility(View.GONE);
                });
    }
}
