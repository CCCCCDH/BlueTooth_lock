package com.way.blebluetooth;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    private LastState lastState;
    public Boolean isdiscoverTimeOut = false;
    public Boolean isNotifyTimeOut = false;
    private static Boolean needParing=false;
    enum LastState {
        CONNECT_CONNECTING,
        CONNECT_CONNECTED,
        CONNECT_FAILURE,
        CONNECT_DISCONNECT
    }
    public String mMsg;

    private static final String UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    mGatt.discoverServices();
                    isdiscoverTimeOut=true;
                    Message time1Msg = new Message();
                    time1Msg.what=3;
                    handler.sendMessageDelayed(time1Msg,1200);
                    break;
                case 1:
                    isdiscoverTimeOut=false;
                    Notify();
                    isNotifyTimeOut=true;
                    Message time2Msg = new Message();
                    time2Msg.what=4;
                    handler.sendMessageDelayed(time2Msg,1200);
                    break;
                case 2:
                    isNotifyTimeOut=false;
                    SendMsg(mMsg);
                case 3:
                        if(isdiscoverTimeOut)
                        {
                            isdiscoverTimeOut = false;
                            mBleCallBack.onConnectFail();
                            DisConnect();
                            refreshDeviceCache();
                            DisConnect();
                            try {
                                ClsUtils.removeBond(mGatt.getDevice().getClass(),mGatt.getDevice());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
//                            GattClose();
                        }
                        break;
                case 4:
                    if(isNotifyTimeOut)
                    {
                        isNotifyTimeOut = false;
                        mBleCallBack.onConnectFail();
                        DisConnect();
                        refreshDeviceCache();
                        DisConnect();
                        try {
                            ClsUtils.removeBond(mGatt.getDevice().getClass(),mGatt.getDevice());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
//                        GattClose();
                    }
                    break;
                default:
                    break;
            }
        };
    };
    private BluetoothGattCallback connectGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            mGatt = gatt;
            Log.i("连接状态改变成功", String.valueOf(status));
            Log.i("连接状态改变成功", String.valueOf(newState));
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (mBleCallBack != null) {
                    Log.w("onConnectionStateChange", "连接成功");

                    if (mDevice.getBondState() == BluetoothDevice.BOND_NONE && needParing) {
                        try {
                            ClsUtils.Paring(context);
                            ClsUtils.createBond(mGatt.getDevice().getClass(),mGatt.getDevice());
                        }catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
//                    while (mDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
//                    }
                    Log.w("BOND_BONDED", "成功配对");
                    handler.sendEmptyMessageDelayed(0,500);
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                    if (lastState == LastState.CONNECT_CONNECTING) {
                        Log.w("onConnectionStateChange", "连接失败");
                        mBleCallBack.onConnectFail();
                        DisConnect();
                        refreshDeviceCache();
                        DisConnect();
                        GattClose();

                    } else if (lastState == LastState.CONNECT_CONNECTED) {
                        lastState = LastState.CONNECT_DISCONNECT;
                        DisConnect();
                        refreshDeviceCache();
                        DisConnect();
                        GattClose();
                        Log.i("onConnectionStateChange", "mGatt断开成功");
                    }
                }
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("onServicesDiscovered", "获取服务成功");
//                mBleCallBack.onConnectSuccess();
                handler.sendEmptyMessage(1);
            } else {
                Log.w("onServicesDiscovered", "获取服务失败");
                mBleCallBack.onConnectFail();
                DisConnect();
                refreshDeviceCache();
                DisConnect();
            }
        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
                Log.i("onCharacteristicChanged", "订阅收到消息成功");
                mBleCallBack.GetMsg(characteristic.getValue());
                DisConnect();
                refreshDeviceCache();
                DisConnect();
        }
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.i("onDescriptorWrite", "成功收到设置描述操作返回消息");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                handler.sendEmptyMessage(2);
            } else {
                mBleCallBack.GetMsgFail();
                DisConnect();
                refreshDeviceCache();
                DisConnect();
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
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.i("onDescriptorRead成功", String.valueOf(status));
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

    public synchronized void Connect(String mac, String mMsg, BleBlueToothCallBack cGattCallback){
        mBleCallBack = cGattCallback;
        this.mMsg = mMsg;
        lastState = LastState.CONNECT_CONNECTING;
        Log.w("gattConnect", "--------开始！----");
        mDevice = mBluetoothAdapter.getRemoteDevice(mac);
        mGatt = mDevice.connectGatt(context,
                false, connectGattCallback, TRANSPORT_LE);
        if (mGatt==null){
            DisConnect();
            refreshDeviceCache();
            DisConnect();
            lastState = LastState.CONNECT_FAILURE;
        }
    }

    public void SendMsg(String mMsg){
        byte[] sendMsg = MsgUtils.hexStringToBytes(mMsg);
        mGattService = mGatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
        mCharacteristic = mGattService.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
        mCharacteristic.setValue(sendMsg);
        mGatt.writeCharacteristic(mCharacteristic);
    }
    public  void Notify(){
        mGattService = mGatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
        if(mGattService == null)
        {
            if (mBleCallBack != null)
                mBleCallBack.GetMsgFail();
            return ;
        }

        mCharacteristic = mGattService.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
        if (mGatt == null || mCharacteristic == null) {
            if (mBleCallBack != null)
                mBleCallBack.GetMsgFail();
            return ;
        }

        boolean success1 = mGatt.setCharacteristicNotification(mCharacteristic, true);
        if (!success1) {
            if (mBleCallBack != null)
                mBleCallBack.GetMsgFail();
            return ;
        }

        BluetoothGattDescriptor mDescriptor = mCharacteristic.getDescriptor(UUID.fromString(UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR));
        if (mDescriptor == null) {
            if (mBleCallBack != null)
                mBleCallBack.GetMsgFail();
            return ;
        } else {
            mDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            boolean success2 = mGatt.writeDescriptor(mDescriptor);
            if (!success2) {
                if (mBleCallBack != null)
                    mBleCallBack.GetMsgFail();
                return ;
            }
        }
    }
    public void StopNotify(){
        if(mGatt == null)
        {
            return;
        }
        mGattService = mGatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
        if(mGattService == null)
        {
            return;
        }
        mCharacteristic = mGattService.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
        if(mCharacteristic == null)
        {
            return;
        }
        boolean success1 = mGatt.setCharacteristicNotification(mCharacteristic, false);
        if (!success1) {
            return ;
        }
        BluetoothGattDescriptor mDescriptor = mCharacteristic.getDescriptor(UUID.fromString(UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR));
        if (mDescriptor == null) {
            return ;
        } else {
            mDescriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            boolean success2 = mGatt.writeDescriptor(mDescriptor);
            if (!success2) {
                return ;
            }
        }

    }
    public synchronized void DisConnect(){
        if(mGatt!=null){
            mGatt.disconnect();
            Log.i("gattDisConnect", "--------调用1断开连接成功！----");
        }
        Log.i("gattDisConnect", "--------调用2断开连接成功！----");
    }
    public synchronized void GattClose(){
        if(mGatt!=null){
            mGatt.close();
            Log.i("gattDisConnect", "--------调用1断开gatt成功！----");
        }
        Log.i("gattDisConnect", "--------调用2断开gatt成功！----");
    }
    private synchronized void refreshDeviceCache() {
        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");
            if (refresh != null && mGatt != null) {
                boolean success = (Boolean) refresh.invoke(mGatt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}



