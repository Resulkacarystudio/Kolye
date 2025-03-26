package com.example.greenlove;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class sifremiUnuttum extends AppCompatActivity {
    EditText sifremiUnuttumEdit;
    Button sifremiUnuttumButton;
    String emailAddress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sifremi_unuttum);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            sifremiUnuttumEdit = findViewById(R.id.sifremiUnuttumEdit);
            sifremiUnuttumButton = findViewById(R.id.sifremiUnuttumButton);
            FirebaseAuth auth = FirebaseAuth.getInstance();

            sifremiUnuttumButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    emailAddress = sifremiUnuttumEdit.getText().toString();

                    auth.sendPasswordResetEmail(emailAddress)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Şifre sıfırlama e-postası gönderildi
                                        Toast.makeText(getApplication(), "Şifre sıfırlama bağlantısı e-posta ile gönderildi.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Hata meydana geldi
                                        Toast.makeText(getApplication(), "Bir hata oluştu, lütfen e-posta adresinizi kontrol edin.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            });


            return insets;
        });
    }
}