package com.example.greenlove;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class zamanFragment extends Fragment {

    private ProgressBar pgbar;
    private TextView progressTxt;
    private TextView gsaat, gdakika, gsaniye;
    private TextView gecenYilTxt, gecenAyTxt, gecenGunTxt;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Calendar selectedDateTime;
    private SharedPreferences sharedPreferences;
    private TextView gun_txt;
    private SeekBar seekBar;
    private boolean isShowingTemporaryTarget = false;

    Button selectDateButton;
    String stageName;
    TextView step_6;
    long elapsedSeconds ;
    long elapsedMinutes;
    long elapsedHours;
    long elapsedDays;


    private static final String PREFS_NAME = "com.example.greenlove.preferences";
    private static final String KEY_YEAR = "selected_year";
    private static final String KEY_MONTH = "selected_month";
    private static final String KEY_DAY = "selected_day";
    private static final String KEY_HOUR = "selected_hour";
    private static final String KEY_MINUTE = "selected_minute";
    private static final String KEY_SECOND = "selected_second";
    private static final String KEY_SAVED_TIME = "saved_time";
    private static final String KEY_TARGET_HOURS = "target_hours";
    private int visualProgress = 0;  // Görsel olarak gösterilecek ilerleme



    private static final int DEFAULT_TARGET_HOURS = 1; // Varsayılan hedef: 1 saat
    private int visualSeconds = 0;  // Görsel olarak gösterilecek saniye


    private boolean isManualUpdate = false; // Manuel güncellemeyi kontrol eden bayrak
    private boolean thumbVisible = true; // İşaretçi görünürlük kontrolü için

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_zaman, container, false);

        pgbar = view.findViewById(R.id.pgbar);
        progressTxt = view.findViewById(R.id.progress_txt);
        gsaat = view.findViewById(R.id.gsaat);
        gdakika = view.findViewById(R.id.gdakika);
        gsaniye = view.findViewById(R.id.gsaniye);
        gecenYilTxt = view.findViewById(R.id.geecenYil_txt);
        gecenAyTxt = view.findViewById(R.id.gecenAy_txt);
        gecenGunTxt = view.findViewById(R.id.gecenGun_txt);
        gun_txt = view.findViewById(R.id.gun_txt);
        seekBar = view.findViewById(R.id.seekBar);
        step_6 = view.findViewById(R.id.step_6);

        // Kullanıcı dokunmasını engellemek için touch listener ekleyin
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // true dönerse touch event'ini engeller
                return true;
            }
        });

    selectDateButton = view.findViewById(R.id.selectDateButton);
        Button button2 = view.findViewById(R.id.button2);

        sharedPreferences = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        loadSavedDateTime(); // Önceden kaydedilmiş tarihi yükle

        selectDateButton.setOnClickListener(v -> showDateTimePickerDialog());
        button2.setOnClickListener(this::showPopupMenu);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateProgress(); // Fragment görünür olduğunda güncellemeyi başlat
        toggleSeekBarThumbVisibilityOnce(); // SeekBar işaretçisini gizle/göster
    }

    private void showDateTimePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year1, month1, dayOfMonth) -> {
            // Seçilen tarihi kontrol et
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(year1, month1, dayOfMonth);

            // Eğer seçilen tarih bugünden ileri bir tarih ise uyarı göster
            if (selectedCalendar.after(Calendar.getInstance())) {
                Toast.makeText(getContext(), "İleri bir tarih seçemezsiniz", Toast.LENGTH_SHORT).show();
            } else {
                // Seçilen tarihi kaydet
                selectedDateTime = Calendar.getInstance();
                selectedDateTime.set(year1, month1, dayOfMonth);

                // Tarih seçildikten sonra saat, dakika ve saniyeyi seç
                showTimePickerDialog();
            }
        }, year, month, day);
        datePickerDialog.show();
    }



    private void showTimePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view, hourOfDay, minute1) -> {
            if (selectedDateTime != null) {
                selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedDateTime.set(Calendar.MINUTE, minute1);
                showSecondPickerDialog(); // Saniye seçimini göster
            }
        }, hour, minute, true);
        timePickerDialog.show();
    }

    private void showSecondPickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int second = calendar.get(Calendar.SECOND);

        NumberPicker secondPicker = new NumberPicker(getContext());
        secondPicker.setMinValue(0);
        secondPicker.setMaxValue(59);
        secondPicker.setValue(second);

        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Saniye Seçin")
                .setView(secondPicker)
                .setPositiveButton("Tamam", (dialog, which) -> {
                    if (selectedDateTime != null) {
                        selectedDateTime.set(Calendar.SECOND, secondPicker.getValue());
                        saveSelectedDateTime(); // Seçilen tarihi kaydet
                        resetProgressBarAndTextViews(); // Eski değerleri sıfırla
                        updateProgress(); // Progress bar'ı güncelleyin
                    }
                })
                .setNegativeButton("İptal", null)
                .show();
    }

    private void saveSelectedDateTime() {
        if (selectedDateTime != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(KEY_YEAR, selectedDateTime.get(Calendar.YEAR));
            editor.putInt(KEY_MONTH, selectedDateTime.get(Calendar.MONTH));
            editor.putInt(KEY_DAY, selectedDateTime.get(Calendar.DAY_OF_MONTH));
            editor.putInt(KEY_HOUR, selectedDateTime.get(Calendar.HOUR_OF_DAY));
            editor.putInt(KEY_MINUTE, selectedDateTime.get(Calendar.MINUTE));
            editor.putInt(KEY_SECOND, selectedDateTime.get(Calendar.SECOND));
            editor.apply(); // Değişiklikleri kaydet

            Toast.makeText(getContext(), "Tarih ve saat kaydedildi", Toast.LENGTH_SHORT).show();

            // Tarih değiştiği için, hedef saati sıfırla ve durumu güncelle
            sharedPreferences.edit().putInt(KEY_TARGET_HOURS, DEFAULT_TARGET_HOURS).apply();

            // İlerleme çubuğunu ve diğer metin alanlarını sıfırla
            resetProgressBarAndTextViews();

            // Yeni hedef ve tarih ile ilerleme durumunu güncelle
            updateProgress();
        }
    }


    private void loadSavedDateTime() {
        // Daha önce kaydedilmiş tarih ve saat bilgilerini `SharedPreferences`'tan yükle
        int year = sharedPreferences.getInt(KEY_YEAR, -1);
        int month = sharedPreferences.getInt(KEY_MONTH, -1);
        int day = sharedPreferences.getInt(KEY_DAY, -1);
        int hour = sharedPreferences.getInt(KEY_HOUR, -1);
        int minute = sharedPreferences.getInt(KEY_MINUTE, -1);
        int second = sharedPreferences.getInt(KEY_SECOND, 0); // Varsayılan olarak 0

        if (year != -1 && month != -1 && day != -1 && hour != -1 && minute != -1) {
            // Kaydedilen tarih ve saat bilgileri mevcutsa
            selectedDateTime = Calendar.getInstance();
            selectedDateTime.set(year, month, day, hour, minute, second);

            // Seçilen tarihi butona yaz
            selectDateButton.setText(String.format(Locale.getDefault(), "Sevgili Olduğunuz Tarih: %02d/%02d/%d", day, month +1, year));

            // İlerleme çubuğunu ve diğer alanları güncelle
            updateProgress();
        } else {
            resetProgressBarAndTextViews(); // Eğer tarih yoksa sıfırla
        }
    }
    private void showPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(getContext(), v);
        popupMenu.inflate(R.menu.popup_menu);

        popupMenu.setOnMenuItemClickListener(item -> {
            int targetHours;

            // Seçime göre hedef saatlerini geçici olarak ayarlıyoruz
            if (item.getItemId() == R.id.one_hour) {
                targetHours = 1;
            } else if (item.getItemId() == R.id.one_day) {
                targetHours = 24;
            } else if (item.getItemId() == R.id.three_days) {
                targetHours = 3 * 24;
            } else if (item.getItemId() == R.id.one_week) {
                targetHours = 7 * 24;
            } else if (item.getItemId() == R.id.two_weeks) {
                targetHours = 14 * 24;
            } else if (item.getItemId() == R.id.one_month) {
                targetHours = 30 * 24;
            } else if (item.getItemId() == R.id.three_months) {
                targetHours = 90 * 24;
            } else if (item.getItemId() == R.id.six_months) {
                targetHours = 180 * 24;
            } else if (item.getItemId() == R.id.one_year) {
                targetHours = 365 * 24;
            } else if (item.getItemId() == R.id.two_years) {
                targetHours = 2 * 365 * 24;
            } else if (item.getItemId() == R.id.four_years) {
                targetHours = 4 * 365 * 24;
            } else if (item.getItemId() == R.id.five_years) {
                targetHours = 5 * 365 * 24;
            } else if (item.getItemId() == R.id.ten_years) {
                targetHours = 10 * 365 * 24;
            } else {
                targetHours = -1;
            }

            // Eğer geçerli bir hedef seçildiyse
            if (targetHours != -1) {
                // Önce eski gecikmeleri kaldır
                handler.removeCallbacksAndMessages(null);

                // Geçici hedef göster, ama sayaç arka planda devam etsin
                updateProgressForTemporaryTarget(targetHours);

                // Her saniyede güncel duruma geri dönmesin, sadece görseli güncelleyelim
                handler.postDelayed(this::resetToSavedProgress, 5000);  // 10 saniye sonra eski duruma dön
            }

            return true;
        });

        popupMenu.show();
    }


    private void resetToSavedProgress() {
        isShowingTemporaryTarget = false;  // Geçici gösterimi sonlandır
        updateProgress();  // Gerçek duruma geri dön
    }





    private void updateProgress() {
        if (isShowingTemporaryTarget) {
            return;  // Geçici hedef gösteriliyorken normal sayaç güncellemesi yapılmasın
        }

        if (selectedDateTime == null) {
            Toast.makeText(getContext(), "Lütfen bir tarih ve saat seçin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kayıtlı hedef saati al
        int targetHours = sharedPreferences.getInt(KEY_TARGET_HOURS, DEFAULT_TARGET_HOURS);
        long targetSeconds = targetHours * 60 * 60;

        // Başlangıç zamanı ve geçen süreyi hesapla
        long startTimeMillis = selectedDateTime.getTimeInMillis();
        long currentTimeMillis = System.currentTimeMillis();
        long elapsedMillis = currentTimeMillis - startTimeMillis;

        if (elapsedMillis < 0) {
            elapsedMillis = 0;
        }

       elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis);
       elapsedMinutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis);
       elapsedHours = TimeUnit.MILLISECONDS.toHours(elapsedMillis);
       elapsedDays = TimeUnit.MILLISECONDS.toDays(elapsedMillis);

        int progress;
        if (elapsedSeconds >= targetSeconds) {
            progress = 100;

            // Aşama değiştiğinde yeni hedef saati hesapla
            int nextTargetHours = getNextTargetHours(targetHours);

            // Yeni hedefi kaydet
            sharedPreferences.edit()
                    .putInt(KEY_TARGET_HOURS, nextTargetHours)
                    .apply();

            targetHours = nextTargetHours; // Hedef saat güncellendi ve kaydedildi
        } else {
            progress = (int) ((double) elapsedSeconds / targetSeconds * 100);
        }

        // ProgressBar'ı güncelle
        pgbar.setProgress(progress);

        // Aşama adını dinamik olarak belirle
        stageName = getStageName(targetHours);
        String progressText = String.format(Locale.getDefault(), "%%%d\nSeçili Durum: %s", progress, stageName);

        SpannableString spannableString = new SpannableString(progressText);
        spannableString.setSpan(new android.text.style.RelativeSizeSpan(2f), 0, String.valueOf(progress).length() + 1, 0);
        spannableString.setSpan(new android.text.style.RelativeSizeSpan(1f), String.valueOf(progress).length() + 1, progressText.length(), 0);

        progressTxt.setText(spannableString);

        // Geçen süreyi UI'da göster
        gsaat.setText(String.format(Locale.getDefault(), "SAAT\n%d", elapsedHours % 24));
        gdakika.setText(String.format(Locale.getDefault(), "DAKİKA\n%d", elapsedMinutes % 60));
        gsaniye.setText(String.format(Locale.getDefault(), "SANİYE\n%d", elapsedSeconds % 60));

        int years = (int) (elapsedDays / 365);
        int months = (int) ((elapsedDays % 365) / 30);
        int days = (int) (elapsedDays % 365 % 30);
        updateSeekBarStep(elapsedDays);

        toggleSeekBarThumbVisibilityOnce();
        gun_txt.setText(String.format(Locale.getDefault(), "GÜN %d", elapsedDays));
        gecenYilTxt.setText(String.format(Locale.getDefault(), "YIL\n%d", years));
        gecenAyTxt.setText(String.format(Locale.getDefault(), "AY\n%d", months));
        gecenGunTxt.setText(String.format(Locale.getDefault(), "GÜN\n%d", days));

        // Normal sayaç güncellemesi her saniyede bir devam edecek
        handler.postDelayed(this::updateProgress, 1000);
    }



    private int getNextTargetHours(int currentTargetHours) {
        // Hedef saatlerine göre bir sonraki hedefi belirle
        switch (currentTargetHours) {
            case 1:
                return 24; // 1 saatten sonra 1 gün
            case 24:
                return 3 * 24; // 1 günden sonra 3 gün
            case 3 * 24:
                return 7 * 24; // 3 günden sonra 1 hafta
            case 7 * 24:
                return 14 * 24; // 1 haftadan sonra 2 hafta
            case 14 * 24:
                return 30 * 24; // 2 haftadan sonra 1 ay
            case 30 * 24:
                return 90 * 24; // 1 aydan sonra 3 ay
            case 90 * 24:
                return 180 * 24; // 3 aydan sonra 6 ay
            case 180 * 24:
                return 365 * 24; // 6 aydan sonra 1 yıl
            case 365 * 24:
                return 2 * 365 * 24; // 1 yıldan sonra 2 yıl
            case 2 * 365 * 24:
                return 4 * 365 * 24; // 2 yıldan sonra 4 yıl
            case 4 * 365 * 24:
                return 5 * 365 * 24; // 4 yıldan sonra 5 yıl
            case 5 * 365 * 24:
                return 10 * 365 * 24; // 5 yıldan sonra 10 yıl
            case 10 * 365 * 24:
                return 20 * 365 * 24; // 4 yıldan sonra 5 yıl
            default:
                return DEFAULT_TARGET_HOURS; // Diğer durumlarda varsayılan hedef
        }
    }

    private void resetProgressBarAndTextViews() {
        // Progress bar ve text view'ları sıfırla
        pgbar.setProgress(0);
        progressTxt.setText("0%");

        gsaat.setText("SAAT\n0");
        gdakika.setText("DAKİKA\n0");
        gsaniye.setText("SANİYE\n0");

        gecenYilTxt.setText("YIL\n0");
        gecenAyTxt.setText("AY\n0");
        gecenGunTxt.setText("GÜN\n0");
    }

    private void updateSeekBarStep(long elapsedDays) {
        int newSeekBarStep = 0;



        if(elapsedDays>365*10)
        {
            step_6.setText("20 YIL");
            newSeekBarStep = 5; // 5 yıl geçtiyse
        }
        if(elapsedDays<365*10)
        {
            step_6.setText("10 YIL");
            newSeekBarStep = 5; // 5 yıl geçtiyse
        }
        if (elapsedDays < 365 * 5) {
            newSeekBarStep = 5; // 5 yıl geçtiyse
            step_6.setText("5 YIL");
        }  if (elapsedDays < 365) {
            newSeekBarStep = 4; // 1 yıl geçtiyse
        } if (elapsedDays < 30) {
            newSeekBarStep = 3; // 1 ay geçtiyse
        }  if (elapsedDays < 7) {
            newSeekBarStep = 2; // 1 hafta geçtiyse
        }  if (elapsedDays < 1) {
            newSeekBarStep = 1; // 1 gün geçtiyse
        }

        seekBar.setProgress(newSeekBarStep); // SeekBar'ı güncelle
    }

    private void toggleSeekBarThumbVisibilityOnce() {
        seekBar.getThumb().setAlpha(255); // İşaretçiyi göster

        handler.postDelayed(() -> {
            seekBar.getThumb().setAlpha(0); // İşaretçiyi tekrar gizle
        }, 500); // İşaretçiyi 0.5 saniye sonra gizle
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }



    private void updateProgressForTemporaryTarget(int targetHours) {
        long startTimeMillis = selectedDateTime.getTimeInMillis();
        long currentTimeMillis = System.currentTimeMillis();
        long elapsedMillis = currentTimeMillis - startTimeMillis;

        if (elapsedMillis < 0) {
            elapsedMillis = 0;
        }

        long elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis);
        long targetSeconds = targetHours * 60 * 60;

        visualSeconds = (int) elapsedSeconds;  // Görsel olarak başladığımız nokta

        // Geçici ilerlemeyi her saniye artır
        handler.post(new Runnable() {
            @Override
            public void run() {
                visualSeconds++;
                int tempProgress = (int) ((double) visualSeconds / targetSeconds * 100);
                if (tempProgress > 100) {
                    tempProgress = 100;
                }

                // Geçici "Seçili Durum" adını al
                String tempStageName = getStageName(targetHours);

                // Görsel sayaç ve progress bar'ı geçici olarak güncelle
                pgbar.setProgress(tempProgress);
                String progressText = String.format(Locale.getDefault(), "%%%d\nGeçici Durum: %s", tempProgress, tempStageName);

                SpannableString spannableString = new SpannableString(progressText);
                spannableString.setSpan(new android.text.style.RelativeSizeSpan(2f), 0, String.valueOf(tempProgress).length() + 1, 0);
                spannableString.setSpan(new android.text.style.RelativeSizeSpan(1f), String.valueOf(tempProgress).length() + 1, progressText.length(), 0);

                progressTxt.setText(spannableString);

                updateVisualTimer(visualSeconds, targetHours); // Metin alanlarını güncelle

                // 10 saniye boyunca geçici olarak göster, ardından dur
                if (visualSeconds < elapsedSeconds + 10) {
                    handler.postDelayed(this, 1000); // 1 saniye gecikmeli tekrar çalış
                } else {
                    // 10 saniye sonunda gerçek duruma geri dön
                    resetToSavedProgress();
                }
            }
        });
    }

    // Hedef saate göre aşama adını döndüren yardımcı metod
    private String getStageName(int targetHours) {
        switch (targetHours) {
            case 1:
                return "1 SAAT";
            case 24:
                return "1 GÜN";
            case 3 * 24:
                return "3 GÜN";
            case 7 * 24:
                return "1 HAFTA";
            case 14 * 24:
                return "2 HAFTA";
            case 30 * 24:
                return "1 AY";
            case 90 * 24:
                return "3 AY";
            case 180 * 24:
                return "6 AY";
            case 365 * 24:
                return "1 YIL";
            case 2 * 365 * 24:
                return "2 YIL";
            case 4 * 365 * 24:
                return "4 YIL";
            case 5 * 365 * 24:
                return "5 YIL";
            case 10 * 365 * 24:
                return "10 YIL";
            case 20 * 365 * 24:
                return "20 YIL";
            default:
                return "";
        }
    }
    private void updateVisualTimer(int visualSeconds, int targetHours) {
        long targetSeconds = targetHours * 60 * 60;

        int progress = (int) ((double) visualSeconds / targetSeconds * 100);
        if (progress > 100) {
            progress = 100;  // %100'ü aşmasın
        }

        // Görsel zamanlayıcıyı UI'da güncelle
        long visualMinutes = visualSeconds / 60;
        long visualHours = visualSeconds / (60 * 60);
        long visualDays = visualSeconds / (60 * 60 * 24);

        updateSeekBarStep(elapsedDays);

        toggleSeekBarThumbVisibilityOnce();
        gsaat.setText(String.format(Locale.getDefault(), "SAAT\n%d", visualHours % 24));
        gdakika.setText(String.format(Locale.getDefault(), "DAKİKA\n%d", visualMinutes % 60));
        gsaniye.setText(String.format(Locale.getDefault(), "SANİYE\n%d", visualSeconds % 60));

        int years = (int) (visualDays / 365);
        int months = (int) ((visualDays % 365) / 30);
        int days = (int) (visualDays % 365 % 30);

        gun_txt.setText(String.format(Locale.getDefault(), "GÜN %d", visualDays));
        gecenYilTxt.setText(String.format(Locale.getDefault(), "YIL\n%d", years));
        gecenAyTxt.setText(String.format(Locale.getDefault(), "AY\n%d", months));
        gecenGunTxt.setText(String.format(Locale.getDefault(), "GÜN\n%d", days));
    }
}
