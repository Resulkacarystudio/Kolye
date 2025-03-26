package com.example.greenlove;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BluetoothJobService extends JobService {

    private static final String TAG = "BluetoothJobService";

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Job started: Restarting BluetoothForegroundService.");

        // BluetoothForegroundService'i başlat
        Context context = getApplicationContext();
        Intent serviceIntent = new Intent(context, BluetoothForegroundService.class);
        context.startForegroundService(serviceIntent);

        // İşin tamamlandığını belirt
        jobFinished(params, false);

        return true; // Eğer iş arka planda devam ediyorsa true döndürün
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job stopped.");
        return true; // İşin yeniden planlanmasını sağlamak için true döndür
    }
}
