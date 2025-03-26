package com.example.greenlove;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BluetoothViewModel extends ViewModel {
    private final MutableLiveData<String> bluetoothStatus = new MutableLiveData<>();

    public LiveData<String> getBluetoothStatus() {
        return bluetoothStatus;
    }

    public void setBluetoothStatus(String status) {
        bluetoothStatus.setValue(status);
    }
}
