package com.example.greenlove;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class sohbetFragment extends Fragment {
    private RecyclerView recyclerViewMessages;
    LinearLayout topbanner;
    private EditText editTextMessage, editTextReceiverId;
    private ImageButton buttonSend, buttonShareId, buttonSaveReceiverId;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private Set<String> messageIdSet; // Eklenen mesajların ID'lerini takip etmek için
    private FirebaseDatabase database;
    private DatabaseReference messagesRef;

    private String currentUserId;
    private String receiverId;
    private ImageView profile_imagesohbet;
    TextView bannertext;
    private static final int MESSAGE_LIMIT = 10; // Bir seferde kaç mesaj yükleneceğini belirler
    private boolean isLoading = false; // Yeni mesajların yüklenip yüklenmediğini kontrol eder

    private static final String PREFS_NAME = "MyPrefs";
    private static final String RECEIVER_ID_KEY = "receiverId";
    private FirebaseFirestore db, profildb;

    ImageView profil_fotografi;
    ImageButton backbutton;
    CardView messageInputCard;
    NestedScrollView scrollView;
    String sohbetemail;
    String kolyearkadasi_email;
    View view1;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sohbet, container, false);

        profile_imagesohbet = view.findViewById(R.id.profile_image);
        topbanner = view.findViewById(R.id.topBanner);
        recyclerViewMessages = view.findViewById(R.id.recyclerViewMessages);
        editTextMessage = view.findViewById(R.id.editTextMessage);
        editTextReceiverId = view.findViewById(R.id.editTextReceiverId);
        buttonSend = view.findViewById(R.id.buttonSend);
        buttonShareId = view.findViewById(R.id.buttonShareId);
        buttonSaveReceiverId = view.findViewById(R.id.buttonSaveReceiverId);
        messageList = new ArrayList<>();
        messageIdSet = new HashSet<>(); // Mesaj ID'lerini takip etmek için set
        bannertext = view.findViewById(R.id.bannerText);
        profil_fotografi = view.findViewById(R.id.profile_image);
        backbutton = view.findViewById(R.id.backButton);
        messageInputCard = view.findViewById(R.id.messageInputCard);

        database = FirebaseDatabase.getInstance();
        messagesRef = database.getReference("messages");

        // MainActivity'yi referans al
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences1 = getContext().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);


        String kolyearkadasi_isim = sharedPreferences1.getString("kullaniciIsim", "");
        bannertext.setText(kolyearkadasi_isim);
        String kolyearkadasi_adi = sharedPreferences1.getString("kullaniciAdi", "");
        kolyearkadasi_email = sharedPreferences1.getString("kullaniciEmail", "");
        String kolyearkadasi_telefon = sharedPreferences1.getString("kullaniciTelefon", "");
        loadProfileData(kolyearkadasi_email);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Glide.with(this)
                .load(R.drawable.logocircle) // Yüklemek istediğiniz resim kaynağı
                .apply(RequestOptions.circleCropTransform()) // Yuvarlak yap
                .into(profil_fotografi); // Hedef ImageView
        loadProfileData(kolyearkadasi_email);
        // Klavye açıldığında veya kapandığında gerçekleşecek işlemler
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float density = displayMetrics.density; // Ekran yoğunluğunu al (dpi)
        // Klavye açıldığında veya kapandığında gerçekleşecek işlemler
        view1 = getActivity().getWindow().getDecorView().getRootView();
        view1.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override

            public void onGlobalLayout() {
                // Fragment bağlamını kontrol et
                if (getContext() == null) {
                    return; // Bağlam yoksa çık
                }
                Rect r = new Rect();
                view1.getWindowVisibleDisplayFrame(r);
                int screenHeight = view1.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                // Alt navigasyon çubuğu olup olmadığını kontrol et
                int navBarHeight = getNavigationBarHeight();

                // Klavye açıldığında
                if (keypadHeight > screenHeight * 0.15) {
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) messageInputCard.getLayoutParams();

                    // Burada öneri çubuğu yüksekliği hesaplanıyor
                    int suggestionBarHeight = getSuggestionBarHeight();

                    if (127< navBarHeight ) {
                        // Alt navigasyon çubuğu varsa
                        params.bottomMargin =  keypadHeight - navBarHeight;
                    } else {
                        // Alt navigasyon çubuğu yoksa
                        params.bottomMargin = keypadHeight ; // Klavyenin boyutunu ve öneri çubuğunu ekle
                    }

                    messageInputCard.setLayoutParams(params);

                    // Mesaj listesini kaydır
                    recyclerViewMessages.postDelayed(() -> {
                        if (messageAdapter != null && messageAdapter.getItemCount() > 0) {
                            recyclerViewMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
                        }
                    }, 100);
                } else {
                    // Klavye kapandığında
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) messageInputCard.getLayoutParams();
                    params.bottomMargin = 0; // Alt boşluğu sıfırla
                    messageInputCard.setLayoutParams(params);
                }
            }
        });



        // ReceiverId'yi SharedPreferences'dan al
        SharedPreferences preferences = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        receiverId = preferences.getString(RECEIVER_ID_KEY, currentUserId); // Eğer yoksa kendi ID'nizi kullanın

        if (receiverId == null) {
            Log.e("sohbetFragment", "Receiver ID is null");
            Toast.makeText(getContext(), "Receiver ID is not provided", Toast.LENGTH_LONG).show();


            return view;
        }

        editTextReceiverId.setText(receiverId);


        messageAdapter = new MessageAdapter(messageList, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(messageAdapter);
        loadStoredUserData();
        updateCurrentUserSohbetID(receiverId);
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        sohbetemail = sharedPreferences.getString("kullaniciEmail", "Varsayılan İsim");
        kolyeArkadasiBilgileri(sohbetemail);


        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(editTextMessage.getText().toString().trim());
            }
        });

        buttonShareId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareId();
            }
        });

        buttonSaveReceiverId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveReceiverId();
            }
        });

        // Yalnızca yeni mesajları ekleyin
        setupMessageListener();

        // Sonsuz kaydırma mantığını ekleyin
        recyclerViewMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading && linearLayoutManager != null && linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                    // Eski mesajları yükleyin
                    loadMoreMessages();
                    isLoading = true;
                }
            }
        });

        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sabit bir fragmente dönmek için
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragmentContainer, new bluetoothFragment());  // Belirli bir fragment
                transaction.commit();
                MainActivity activity = (MainActivity) getActivity();
                activity.seciliSekme = 1;
                activity.bottomBar.setVisibility(View.VISIBLE);

            }
        });

        editTextMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    recyclerViewMessages.post(new Runnable() {
                        @Override
                        public void run() {
                            if (messageList != null && !messageList.isEmpty()) {  // Mesaj listesinin boş olup olmadığını kontrol et
                                recyclerViewMessages.smoothScrollToPosition(messageList.size() - 1);
                            }
                        }
                    });
                }
            }
        });


        return view;
    }

    private void sendMessage(String text) {
        if (!text.isEmpty()) {
            String senderId = currentUserId;
            String messageId = messagesRef.push().getKey();
            Message message = new Message(messageId, text, senderId, editTextReceiverId.getText().toString(), System.currentTimeMillis());
            messagesRef.child(messageId).setValue(message, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.e("SendMessage", "Failed to send message", databaseError.toException());
                        Toast.makeText(getContext(), "Mesaj gönderilemedi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        //    Toast.makeText(getContext(), "Mesaj gönderildi", Toast.LENGTH_SHORT).show();
                        editTextMessage.setText("");
                        // Yeni mesaj gönderildikten sonra en aşağıya kaydır
                        recyclerViewMessages.scrollToPosition(messageList.size() - 1);
                    }
                }
            });
        } else {
            Log.e("SendMessage", "Message text is empty");
        }
    }


    private void shareId() {
        String shareText = "My ID: " + currentUserId;
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.setType("text/plain");
        startActivity(Intent.createChooser(shareIntent, "Share ID via"));
    }

    private void saveReceiverId() {
        receiverId = editTextReceiverId.getText().toString().trim();
        if (!receiverId.isEmpty()) {
            // SharedPreferences'a kaydet
            SharedPreferences preferences = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(RECEIVER_ID_KEY, receiverId);
            editor.apply();
            Toast.makeText(getContext(), "Receiver ID saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Receiver ID is empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupMessageListener() {
        Query messageQuery = messagesRef.orderByChild("timestamp").limitToLast(MESSAGE_LIMIT);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                Message message = dataSnapshot.getValue(Message.class);
                if (message != null && (message.getSenderId().equals(currentUserId) || message.getReceiverId().equals(currentUserId))) {
                    if (!messageIdSet.contains(message.getId())) {
                        messageList.add(message);
                        messageIdSet.add(message.getId());
                        messageAdapter.notifyItemInserted(messageList.size() - 1);
                        recyclerViewMessages.scrollToPosition(messageList.size() - 1);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Mesaj güncellenirse yapılacak işlemler
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // Mesaj silinirse yapılacak işlemler
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Mesaj taşınırsa yapılacak işlemler
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MessageListener", "Failed to listen for messages", databaseError.toException());
            }
        });
    }

    private void loadMoreMessages() {
        if (messageList.size() > 0) {
            // İlk olarak en eski mesajı alarak o mesajdan önceki mesajları sorgulayın
            Message lastMessage = messageList.get(0);
            Query messageQuery = messagesRef.orderByChild("timestamp")
                    .endAt(lastMessage.getTimestamp() - 1)
                    .limitToLast(MESSAGE_LIMIT); // Belirli bir limitte mesaj çekin

            messageQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<Message> newMessages = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Message message = snapshot.getValue(Message.class);
                        if (message != null && (message.getSenderId().equals(currentUserId) || message.getReceiverId().equals(currentUserId))) {
                            if (!messageIdSet.contains(message.getId())) {
                                newMessages.add(message);
                                messageIdSet.add(message.getId());
                            }
                        }
                    }

                    // Eski mesajları başa ekleyin
                    messageList.addAll(0, newMessages);
                    messageAdapter.notifyItemRangeInserted(0, newMessages.size());

                    // Eski pozisyona kaydırın
                    recyclerViewMessages.scrollToPosition(newMessages.size());

                    isLoading = false;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("LoadMoreMessages", "Failed to load messages", databaseError.toException());
                    isLoading = false;
                }
            });
        }
    }

    private void updateCurrentUserSohbetID(String newSohbetID) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("Kullanicilar").document(userEmail);

            // Güncelleme işlemi
            docRef.update("sohbetID", newSohbetID)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Firestore", "sohbetID başarıyla güncellendi!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("Firestore", "sohbetID güncellemesi başarısız oldu", e);
                            Toast.makeText(getContext(), "sohbetID güncellemesi başarısız oldu.", Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            // Kullanıcı oturum açmamış, oturum açma ekranına yönlendirebilirsiniz
            Toast.makeText(getContext(), "Kullanıcı oturumu açmamış.", Toast.LENGTH_LONG).show();
        }
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
                        Log.d("Firestore", "DocumentSnapshot data: " + document.getData());

                        // Yalnızca gerekli alanları çekme
                        String kullaniciIsimValue = document.getString("kullanici_isim");
                        String sohbetIDValue = document.getString("sohbetID");

                        // Verileri SharedPreferences'a kaydetme
                        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("kullanici_isim", kullaniciIsimValue);
                        editor.putString("sohbetID", sohbetIDValue);
                        editor.apply();

                    } else {
                        Log.d("Firestore", "No such document");
                        Toast.makeText(getContext(), "Belirtilen e-posta ile eşleşen bir kullanıcı bulunamadı.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d("Firestore", "get failed with ", task.getException());
                    Toast.makeText(getContext(), "Kullanıcı bilgileri çekilemedi.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void loadStoredUserData() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String kullaniciIsimValue = sharedPreferences.getString("kullanici_isim", " ");
        String sohbetIDValue = sharedPreferences.getString("sohbetID", "Varsayılan Sohbet ID");

        // Verileri UI'a set etme (eğer UI bileşenleri varsa)
        // editTextMessage.setText(kullaniciIsimValue);
        editTextMessage.setHint("Sohbet Başlat");

        editTextReceiverId.setText(sohbetIDValue);
        kullaniciIsimValue = kullaniciIsimValue.substring(0, 1).toUpperCase() + kullaniciIsimValue.substring(1).toLowerCase();

        // bannertext.setText(kullaniciIsimValue);
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Burada context erişimini kullanabilirsiniz.
    }

    private void loadProfileData(String email) {
        // Belirtilen e-mail'e göre sorgu yapıyoruz
        db.collection("Kullanicilar")
                .whereEqualTo("kullanici_email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userName = document.getString("kullanici_isim");
                            String profileUrl = document.getString("profil_fotografı");


                            if (profileUrl != null && !profileUrl.isEmpty()) {
                                Glide.with(sohbetFragment.this)
                                        .load(profileUrl)
                                        .apply(RequestOptions.circleCropTransform())
                                        .into(profile_imagesohbet);
                            }


                        }
                    } else {
                        Log.e("Firebase", "Kullanıcı bulunamadı.");
                    }
                });
    }

    // Navigation bar yüksekliğini almak için yardımcı fonksiyon
    private int getNavigationBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    // Alt navigasyon çubuğunun yüksekliğini almak için bir yöntem
    private int getNavigationBarHeight() {
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        return resourceId > 0 ? getResources().getDimensionPixelSize(resourceId) : 0;
    }

    // Öneri çubuğu yüksekliğini almak için bir yöntem
    // Öneri çubuğu yüksekliğini almak için bir yöntem
    private int getSuggestionBarHeight() {
        // Öneri çubuğunun yüksekliğini 48dp olarak sabit belirle
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
    }
}