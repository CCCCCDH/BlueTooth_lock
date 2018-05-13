package com.way.blebluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.os.Build;

import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;

public abstract class BleBlueToothCallBack extends BluetoothGattCallback{
    public abstract void onConnectFail();

    public abstract void WriteFail();

    public abstract void GetMsg(byte[] data);

    public abstract void GetMsgFail();
}
