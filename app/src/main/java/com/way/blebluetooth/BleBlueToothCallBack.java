package com.way.blebluetooth;

import android.bluetooth.BluetoothGattCallback;

public abstract class BleBlueToothCallBack extends BluetoothGattCallback{
    public abstract void onConnectFail();

    public abstract void WriteFail();

    public abstract void GetMsg(byte[] data);

    public abstract void GetMsgFail();
}
