package com.example.greenlove;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.Manifest;

public class profilFragment extends Fragment {
    private static final int REQUEST_CODE_PICK_IMAGE_FOR_POST = 1;
    private Button fotograf_yukle_bt, change_partner_button; // Buton tanımı eklendi
    private ImageView profile_image;
    private TextView kullanici_ismi, k_email, fotografsayisi_tw; // Post sayısını gösterecek TextView
    private ProgressBar progressBar;
    private FirebaseFirestore db, profildb;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private String userId, profil_mail;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<String> postUrls = new ArrayList<>();
    private String kolyearkadasi_isim;
    private String kolyearkadasi_adi;
    private String kolyearkadasi_email;
    private String kolyearkadasi_telefon;
    private String kullaniciAdi;
    private boolean showingPartnerProfile = false;  // İlk açılışta profil sahibinin profili gösterilecek

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int REQUEST_CODE_PICK_IMAGE = 2;
    private static final int REQUEST_CODE_READ_STORAGE_PERMISSION = 1;
    String kullaniciEmailpartner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profil, container, false);

        profile_image = view.findViewById(R.id.profile_image);
        kullanici_ismi = view.findViewById(R.id.username);
        fotograf_yukle_bt = view.findViewById(R.id.fotograf_yukle_bt);
        change_partner_button = view.findViewById(R.id.change_partner_button); // Buton tanımlandı
        fotografsayisi_tw = view.findViewById(R.id.fotografsayisi_tw); // Post sayısı TextView'i
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE); // Başlangıçta gizli
        k_email = view.findViewById(R.id.k_email);
        recyclerView = view.findViewById(R.id.recyclerView);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);

        kolyearkadasi_isim = sharedPreferences.getString("kullaniciIsim", "");
        kolyearkadasi_adi = sharedPreferences.getString("kullaniciAdi", "");
        kolyearkadasi_email = sharedPreferences.getString("kullaniciEmail", "");
        kolyearkadasi_telefon = sharedPreferences.getString("kullaniciTelefon", "");

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        postAdapter = new PostAdapter(getContext(), postUrls, new PostAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // Büyük resmi göster
                Intent intent = new Intent(getContext(), FullscreenImageActivity.class);
                intent.putStringArrayListExtra("image_list", new ArrayList<>(postUrls));
                intent.putExtra("initial_position", position);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(int position) {
                // Veritabanından sil ve listeyi güncelle
                String imageUrl = postUrls.get(position);
                deleteImageFromFirestore(imageUrl, position);
            }
        });
        recyclerView.setAdapter(postAdapter);

        int permission = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    (Activity) getContext(),
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

        SharedPreferences profilsahibi = getContext().getSharedPreferences("ProfilKullanici", MODE_PRIVATE);
        SharedPreferences profilsahibi1 = getContext().getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String kullaniciIsim1 = profilsahibi1.getString("kullaniciIsim", "");
        String kullaniciAdi1 = profilsahibi.getString("kullaniciAdi", "Kullanıcı İsim");
        String profilFotoUrl1 = profilsahibi.getString("profilFotoUrl", null);
        String email1 = profilsahibi.getString("email", "Kullanıcı Email");
        String email2 = profilsahibi.getString("kemail1", "Kullanıcı Email");



        if (profilFotoUrl1 != null && !profilFotoUrl1.isEmpty()) {
            Glide.with(getContext())
                    .load(profilFotoUrl1)
                    .apply(RequestOptions.circleCropTransform())
                    .into(profile_image);
        } else {
            profile_image.setImageResource(R.drawable.logo_circle);
        }
        SharedPreferences sharedPreferences12 = getContext().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);


        String isim = sharedPreferences12.getString("kullaniciIsim", "");

        String adi = sharedPreferences12.getString("kullaniciAdi", "");
        String email = sharedPreferences12.getString("kullaniciEmail", "");
        SharedPreferences sharedPreferences1 = getContext().getSharedPreferences("AppPrefs", MODE_PRIVATE);
        profil_mail = sharedPreferences1.getString("kemail1", "Misafir");

        k_email.setText(profil_mail);
        kullanici_ismi.setText(kullaniciIsim1);
        db = FirebaseFirestore.getInstance();
        profildb = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        String email_arkadasi = sharedPreferences1.getString("kemail1", "");

        // Profil sahibinin verilerini yükle
        loadProfileData(profil_mail);

        // Kolye Arkadaşı Butonuna Tıklayınca Yapılacak İşlemler
        change_partner_button.setOnClickListener(v -> {
            toggleProfile();
        });

        setupPhotoUpload();
        fotograf_yukle_bt.setOnClickListener(v -> {
            if (postUrls.size() >= 12) {
                Toast.makeText(getContext(), "Maksimum 12 fotoğraf yükleyebilirsiniz.", Toast.LENGTH_SHORT).show();
            } else if (showingPartnerProfile) {
                Toast.makeText(getContext(), "Kolye arkadaşının profilinde fotoğraf yükleyemezsiniz.", Toast.LENGTH_SHORT).show();
            } else {
                checkAndRequestPermissionsForPost();
            }
        });


        return view;
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_CODE_READ_STORAGE_PERMISSION);
            }
        } else {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ_STORAGE_PERMISSION);
            }
        }
    }

    private void setupPhotoUpload() {
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showingPartnerProfile) {
                    // Kolye arkadaşının profilindeyken uyarı ver ve resim değiştirmeyi engelle
                    Toast.makeText(getContext(), "Kolye arkadaşının profil resmini değiştiremezsiniz.", Toast.LENGTH_SHORT).show();
                } else {
                    // Kullanıcının kendi profilindeyse resim değiştirme işlemini başlat
                    checkAndRequestPermissions();

                    if (ContextCompat.checkSelfPermission(getContext(), Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                            Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {

                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
                    } else {
                        Toast.makeText(getContext(), "İzin verilmedi. Galeriye erişilemiyor.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK && data != null) {
            Uri selectedImage = data.getData();

            if (requestCode == REQUEST_CODE_PICK_IMAGE_FOR_POST) {
                if (showingPartnerProfile) {
                    uploadPhotoToFirebaseForPost(selectedImage, kolyearkadasi_email); // Kolye arkadaşının postu
                } else {
                    uploadPhotoToFirebaseForPost(selectedImage, profil_mail); // Kendi postu
                }
            } else if (requestCode == REQUEST_CODE_PICK_IMAGE) {
                Log.d("PhotoUpload", "Image selected: " + selectedImage.toString());

                if (selectedImage != null && profil_mail != null) {
                    progressBar.setVisibility(View.VISIBLE);

                    StorageReference ref = storageReference.child("users/" + profil_mail + "/profile_image.jpg");

                    ref.putFile(selectedImage)
                            .addOnSuccessListener(taskSnapshot -> {
                                ref.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String downloadUrl = uri.toString();
                                    updateProfileImageInFirestore(downloadUrl);
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getContext(), "Fotoğraf başarıyla yüklendi", Toast.LENGTH_SHORT).show();


                                    Glide.with(profilFragment.this)
                                            .load(downloadUrl)
                                            .apply(RequestOptions.circleCropTransform())
                                            .into(profile_image);

                                    saveUserInfoInPreferences(kullanici_ismi.getText().toString(), kullaniciAdi, downloadUrl, profil_mail);
                                });
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                Log.e("Firebase Upload", "Fotoğraf yüklenirken hata oluştu", e);
                                Toast.makeText(getContext(), "Fotoğraf yüklenirken hata oluştu", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Log.e("PhotoUpload", "Image not selected or userId is null.");
                    Toast.makeText(getContext(), "Fotoğraf seçilmedi veya kullanıcı ID'si null", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("PhotoUpload", "Image not selected or result code not OK.");
                Toast.makeText(getContext(), "Fotoğraf seçilemedi", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateProfileImageInFirestore(String downloadUrl) {
        if (profil_mail != null) {  // profil_mail kullanarak güncelleme
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("profil_fotografı", downloadUrl);

            db.collection("Kullanicilar").document(profil_mail)
                    .set(updateData, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "Profil fotoğrafı başarıyla güncellendi.");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Profil fotoğrafı güncellenirken hata oluştu", e);
                        Toast.makeText(getContext(), "Profil fotoğrafı kaydedilirken hata oluştu.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e("Firestore", "Profil mail null.");
            Toast.makeText(getContext(), "Profil mail null, Firestore güncelleme yapılamıyor.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_READ_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // İzin verildiğinde fotoğraf seçimini başlatın
                selectPhotoForPost();
            } else {
                Toast.makeText(getContext(), "İzin verilmedi, fotoğraf yükleme yapılamaz.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveUserInfoInPreferences(String kullaniciIsim, String kullaniciAdi, String profilFotoUrl, String email) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("ProfilKullanici", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("kullaniciIsim", kullaniciIsim);
        editor.putString("kullaniciAdi", kullaniciAdi);
        editor.putString("profilFotoUrl", profilFotoUrl);
        editor.putString("email", email);

        editor.apply();
    }

    private void checkAndRequestPermissionsForPost() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_CODE_READ_STORAGE_PERMISSION);
            } else {
                selectPhotoForPost();
            }
        } else {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ_STORAGE_PERMISSION);
            } else {
                selectPhotoForPost();
            }
        }
    }

    private void selectPhotoForPost() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE_FOR_POST);
    }

    private void uploadPhotoToFirebaseForPost(Uri imageUri, String email) {
        if (imageUri != null) {
            progressBar.setVisibility(View.VISIBLE);  // ProgressBar'ı göster

            String folderPath = "images/" + email + "/";
            StorageReference ref = storageReference.child(folderPath + "post_" + System.currentTimeMillis() + ".jpg");

            ref.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                 savePostInFirestore(downloadUrl, email);
                    progressBar.setVisibility(View.GONE);  // ProgressBar'ı gizle
                });
            }).addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);  // ProgressBar'ı gizle
                Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }


    private void savePostInFirestore(String downloadUrl, String email) {
        Map<String, Object> postData = new HashMap<>();
        postData.put("image_url", downloadUrl);
        postData.put("timestamp", System.currentTimeMillis());

        db.collection("Kullanicilar").document(email).collection("Posts").add(postData)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Fotograf Yüklendi", Toast.LENGTH_SHORT).show();
                    postUrls.add(downloadUrl);  // Yeni eklenen postu listeye ekleyin
                    postAdapter.notifyItemInserted(postUrls.size() - 1);  // Adapter'ı bilgilendirin
                    fotografsayisi_tw.setText(postUrls.size() + " Post"); // Post sayısını güncelleyin
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Fotograf Yüklenemedi.", Toast.LENGTH_SHORT).show();
                });
    }


    private void savePostInFirestore(String downloadUrl) {
        Map<String, Object> postData = new HashMap<>();
        postData.put("image_url", downloadUrl);
        postData.put("timestamp", System.currentTimeMillis());

        db.collection("Kullanicilar").document(userId).collection("Posts").add(postData)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Post uploaded successfully.", Toast.LENGTH_SHORT).show();
                    postUrls.add(downloadUrl);  // Yeni eklenen postu listeye ekleyin
                    postAdapter.notifyItemInserted(postUrls.size() - 1);  // Adapter'ı bilgilendirin
                    fotografsayisi_tw.setText(postUrls.size() + " Post"); // Post sayısını güncelleyin
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to upload post to Firestore.", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadPosts() {
        db.collection("Kullanicilar").document(userId).collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        postUrls.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String imageUrl = document.getString("image_url");
                            postUrls.add(imageUrl);
                        }
                        postAdapter.notifyDataSetChanged();
                        fotografsayisi_tw.setText(postUrls.size() + " Post"); // Dinamik post sayısını göster
                    } else {
                        fotografsayisi_tw.setText("0 Post"); // Eğer post yoksa sıfır olarak göster
                        Log.e("Firestore", "Postlar yüklenemedi veya bulunamadı.");
                    }
                });
    }

    public void deleteImageFromFirestore(String imageUrl, int position) {
        // Firestore'dan silme işlemi yap
        // Silme başarılı ise:
        postUrls.remove(position);
        postAdapter.notifyItemRemoved(position);
        fotografsayisi_tw.setText(postUrls.size() + " Post"); // Silindiğinde post sayısını güncelleyin
    }

    private void toggleProfile() {
        if (showingPartnerProfile) {
            // Kullanıcı kendi profiline geçiyor
            loadProfileData(profil_mail);  // Profil sahibinin maili
            change_partner_button.setText("Kolye Arkadaşı");
            showingPartnerProfile = false;
        } else {
            // Kolye arkadaşı profiline geçiyor
            loadPartnerDetails();
            loadProfileData(kullaniciEmailpartner);  // Kolye arkadaşının maili

            change_partner_button.setText("Profil Sahibi");

            showingPartnerProfile = true;
        }
    }
    private void loadPartnerDetails() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("partner_bilgileri", Context.MODE_PRIVATE);

        String kullaniciAdi = sharedPreferences.getString("partner_kullaniciadi", "Bilinmiyor");
        String kullaniciIsim = sharedPreferences.getString("partner_isim", "Bilinmiyor");
         kullaniciEmailpartner = sharedPreferences.getString("partner_email", "Bilinmiyor");
        String kullaniciTelefon = sharedPreferences.getString("partner_telefon", "Bilinmiyor");
        String btID = sharedPreferences.getString("partner_btid", "Bilinmiyor");

    }

    private void loadProfileData(String email) {
        progressBar.setVisibility(View.VISIBLE); // İşlem başında ProgressBar'ı göster

        db.collection("Kullanicilar")
                .whereEqualTo("kullanici_email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userName = document.getString("kullanici_isim");
                            String profileUrl = document.getString("profil_fotografı");

                            // Profil maili güncelleme
                            if ("Profil Sahibi".equals(change_partner_button.getText().toString())) {
                                k_email.setText(kolyearkadasi_email);
                            } else {
                                k_email.setText(profil_mail);
                            }

                            // Kullanıcı ismini güncelle
                            kullanici_ismi.setText(userName);

                            // Profil fotoğrafını yükle
                            if (profileUrl != null && !profileUrl.isEmpty()) {
                                Glide.with(profilFragment.this)
                                        .load(profileUrl)
                                        .apply(RequestOptions.circleCropTransform())
                                        .into(profile_image);
                            } else {
                                Glide.with(profilFragment.this)
                                        .load(R.drawable.logo_circle) // Varsayılan fotoğraf
                                        .apply(RequestOptions.circleCropTransform())
                                        .into(profile_image);
                            }

                            // Kullanıcının postlarını yükle
                            loadPostsForUser(document.getId());
                        }
                    } else {
                        // Kullanıcı bulunamadığında hata mesajı
                        Log.e("Firebase", "Kullanıcı bulunamadı.");
                        Toast.makeText(getContext(), "Kullanıcı profili bulunamadı.", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE); // İşlem tamamlandığı için ProgressBar'ı gizle
                    }
                })
                .addOnFailureListener(e -> {
                    // Firebase sorgusunda hata durumunda
                    Log.e("Firebase", "Profil bilgisi yüklenirken hata oluştu.", e);
                    Toast.makeText(getContext(), "Profil bilgisi yüklenirken hata oluştu.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE); // İşlem tamamlandığı için ProgressBar'ı gizle
                });
    }

    // Kullanıcıya ait postlar yüklendiğinde ProgressBar'ı gizleyin
    private void loadPostsForUser(String userId) {
        db.collection("Kullanicilar").document(userId).collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE); // İşlem tamamlandığı için ProgressBar'ı gizle
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        postUrls.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String imageUrl = document.getString("image_url");
                            postUrls.add(imageUrl);
                        }
                        postAdapter.notifyDataSetChanged();
                        fotografsayisi_tw.setText(postUrls.size() + " Post"); // Post sayısını güncelle
                    } else {
                        fotografsayisi_tw.setText("0 Post"); // Post yoksa sıfır olarak göster
                        Log.e("Firestore", "Postlar yüklenemedi veya bulunamadı.");
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE); // İşlem tamamlandığı için ProgressBar'ı gizle
                    Log.e("Firebase", "Postlar yüklenirken hata oluştu.", e);
                    Toast.makeText(getContext(), "Postlar yüklenirken hata oluştu.", Toast.LENGTH_SHORT).show();
                });
    }

}
