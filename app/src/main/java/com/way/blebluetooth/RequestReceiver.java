package com.way.blebluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static android.os.Build.VERSION_CODES.ECLAIR;

public class RequestReceiver extends BroadcastReceiver {
    public RequestReceiver(){

    }
    @TargetApi(ECLAIR)
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("android.bluetooth.device.action.PAIRING_REQUEST")) {
            BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            try {
                int mType = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);

                String pin = "666666";
                boolean isSuccess = false;
                switch (mType) {
                    case 0:
                        //当接收到配对请求时自动配对
                        isSuccess = ClsUtils.setPin(btDevice.getClass(), btDevice, pin );
                        break;
                    case 1:
                        int passKey = Integer.parseInt(pin);
                        isSuccess = ClsUtils.setPassKey(btDevice.getClass(), btDevice, passKey);
                        break;
                }
                if (isSuccess) {
                    abortBroadcast();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}