package com.example.greenlove;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FcmNotificationsSender {

    String userFcmToken;
    Context mContext;
    Activity mActivity;
    private RequestQueue requestQueue;
    private final String postUrl = "https://fcm.googleapis.com/v1/projects/kolye-15638/messages:send"; // V1 URL

    public FcmNotificationsSender(String userFcmToken, Context mContext, Activity mActivity) {

        this.userFcmToken = userFcmToken;
        this.mContext = mContext;
        this.mActivity = mActivity;

        // RequestQueue'yu başlat
        requestQueue = Volley.newRequestQueue(mActivity != null ? mActivity : mContext.getApplicationContext());
    }

    // Firebase Admin SDK ile access token alma yöntemi
    private String getAccessToken() {
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(mContext.getAssets().open("kolye-15638-firebase-adminsdk-wnimk-2ee33f20c5.json"))
                    .createScoped("https://www.googleapis.com/auth/cloud-platform");
            credentials.refreshIfExpired();
            return credentials.getAccessToken().getTokenValue();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // FCM ile bildirim gönderme işlemi
    public void SendNotifications(String title, String body) {
        JSONObject mainObj = new JSONObject();
        try {
            JSONObject messageObj = new JSONObject();
            messageObj.put("token", userFcmToken);

            // Data yükü
            JSONObject dataObject = new JSONObject();
            dataObject.put("title", title);
            dataObject.put("body", body);
            dataObject.put("customKey", "customValue");
            dataObject.put("extraInfo", "Kolye bilgisi");

            // Mesaj yapısına data yükünü ekle
            messageObj.put("data", dataObject);

            // Android için konfigürasyon
            JSONObject androidConfig = new JSONObject();
            androidConfig.put("priority", "high");

            // Android yapılandırmasını mesaj yapısına ekle
            messageObj.put("android", androidConfig);

            // Ana nesneye mesaj yapısını ekle
            mainObj.put("message", messageObj);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, mainObj,
                    response -> Log.d("FCM", "Bildirim başarıyla gönderildi."),
                    error -> Log.e("FCM", "Bildirim gönderilirken hata oluştu: " + error.getMessage())) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + getAccessToken());
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            requestQueue.add(request);
        } catch (JSONException e) {
            Log.e("FCM", "JSON oluşturulurken hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }





    /* public void SendNotifications(String title, String body) {
        JSONObject mainObj = new JSONObject();
        try {
            JSONObject messageObj = new JSONObject();
            messageObj.put("token", userFcmToken); // Kullanıcı FCM Token'ını ekle

            System.out.println("butoken"+ userFcmToken);
            // Bildirim payload'ı
            JSONObject notificationObject = new JSONObject();
            notificationObject.put("title", title);
            notificationObject.put("body", body);

            // Mesaj yapısına bildirim ekle
            messageObj.put("notification", notificationObject);
            mainObj.put("message", messageObj);

            // FCM POST isteği
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, mainObj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("FCM", "Bildirim başarıyla gönderildi: " + response.toString());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("FCM", "Bildirim gönderilirken bir hata oluştu: " + error.getMessage());
                    if (error.networkResponse != null) {
                        Log.e("FCM", "Status code: " + error.networkResponse.statusCode);
                        Log.e("FCM", "Response data: " + new String(error.networkResponse.data));
                    }
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");

                    // OAuth2 token'ı header'a ekle
                    String accessToken = getAccessToken();
                    header.put("Authorization", "Bearer " + accessToken);
                    return header;
                }
            };

            // İstek için yeniden deneme politikası
            request.setRetryPolicy(new DefaultRetryPolicy(
                    1000, // 1 saniye zaman aşımı
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES, // Varsayılan yeniden deneme sayısı
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT // Geri çekilme çarpanı
            ));

            // İsteği başlat
            requestQueue.add(request);
        } catch (JSONException e) {
            Log.e("FCM", "JSON oluşturulurken bir hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }*/

    // Kullanıcının token'ı üzerinden bildirim gönderme
    public void sendNotificationToUser(String targetUserId, String title, String body) {
        // Veritabanından hedef kullanıcının token'ını alıyoruz
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(targetUserId) // Kullanıcının UID'siyle token'ı alıyoruz
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String targetFcmToken = document.getString("fcmToken");
                                // Token ile bildirim gönderme işlemi
                                FcmNotificationsSender notificationsSender = new FcmNotificationsSender(targetFcmToken, mContext, mActivity);
                                notificationsSender.SendNotifications(title, body);
                            } else {
                                Log.d("FCM", "Hedef kullanıcı token'ı bulunamadı");
                            }
                        } else {
                            Log.d("FCM", "Veritabanından token alınamadı", task.getException());
                        }
                    }
                });
    }
}
