package com.way.blebluetooth;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothGattService;

import android.os.SystemClock;
import android.util.Log;

import android.content.Context;
import java.util.UUID;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

public class BleBlueToothManager {
    private Application context;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mDevice;
    private BluetoothGatt mGatt;
    private BluetoothGattService mGattService;
    private BluetoothGattCharacteristic mCharacteristic;
    private BleBlueToothCallBack mBleCallBack;
    private String msg;

    private static final String UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";

    private BluetoothGattCallback connectGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            mGatt = gatt;
            Log.w("onConnectionStateChange", "status11111");
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if(mBleCallBack!=null) {
                    Log.w("onConnectionStateChange", "连接成功");
                    mGatt.discoverServices();
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if(mBleCallBack!=null) {
                    Log.w("onConnectionStateChange", "连接失败");
                    mBleCallBack.onConnectFail();
                }
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("onServicesDiscovered", "获取服务成功");
//                mBleCallBack.onConnectSuccess();
                SystemClock.sleep(100);
                Notify();
            } else {
                Log.w("onServicesDiscovered", "获取服务失败");
                mBleCallBack.onConnectFail();
            }
        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
//            if (characteristic.getUuid().toString().equalsIgnoreCase(gatt.getDevice().getUuids().toString())) {
                //收到消息 characteristic.getValue()
                Log.i("onCharacteristicChanged", "订阅收到消息");
                mBleCallBack.GetMsg(characteristic.getValue());
//            }
        }
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.i("onDescriptorWrite", "成功收到设置描述操作返回消息");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                SendMsg(msg);
            } else {
                mBleCallBack.GetMsgFail();
            }
        }
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.i("onCharacteristicWrite", "成功收到写操作返回消息");
            if (status == BluetoothGatt.GATT_SUCCESS) {
            } else {
                mBleCallBack.WriteFail();
            }
        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.i("onCharacteristicRead", "成功收到读取返回消息");
        }
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.i("onCharacteristicRead", "成功收到Rssi消息");
        }
        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.i("onCharacteristicRead", "成功收到传输最大单位改变消息");
        }
    };

    public void init(Application app) {
        if (context == null && app != null) {
            context = app;
            mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager != null)
                mBluetoothAdapter = mBluetoothManager.getAdapter();
        }
    }

    public synchronized void Connect(String mac, String msg, BleBlueToothCallBack cGattCallback){
        mBleCallBack = cGattCallback;
        this.msg = msg;
        Log.w("gattConnect", "--------开始！----");
        mDevice = mBluetoothAdapter.getRemoteDevice(mac);
        mGatt = mDevice.connectGatt(context,
                false, connectGattCallback, TRANSPORT_LE);
    }

    public void SendMsg(String msg){
        byte[] sendMsg = MsgUtils.hexStringToBytes(msg);
        mGattService = mGatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
        mCharacteristic = mGattService.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
        mCharacteristic.setValue(sendMsg);
        mGatt.writeCharacteristic(mCharacteristic);
    }
    public void Notify(){
        mGattService = mGatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
        mCharacteristic = mGattService.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
        mGatt.setCharacteristicNotification(mCharacteristic, true);
        BluetoothGattDescriptor mDescriptor = mCharacteristic.getDescriptor(UUID.fromString(UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR));
        mDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mGatt.writeDescriptor(mDescriptor);
    }
    public void StopNotify(){
        mGattService = mGatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
        mCharacteristic = mGattService.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
        mGatt.setCharacteristicNotification(mCharacteristic, false);
        BluetoothGattDescriptor mDescriptor = mCharacteristic.getDescriptor(UUID.fromString(UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR));
        mDescriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        mGatt.writeDescriptor(mDescriptor);
    }
    public void DisConnect(){
        mGatt.disconnect();
        mGatt.close();
    }

}



