package com.example.greenlove;

import android.bluetooth.BluetoothGatt;

public class BluetoothManagerSingleton {
    private static BluetoothManagerSingleton instance;
    private BluetoothGatt bluetoothGatt;

    private BluetoothManagerSingleton() {}

    public static synchronized BluetoothManagerSingleton getInstance() {
        if (instance == null) {
            instance = new BluetoothManagerSingleton();
        }
        return instance;
    }

    public void setBluetoothGatt(BluetoothGatt gatt) {
        this.bluetoothGatt = gatt;
    }

    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }
}
