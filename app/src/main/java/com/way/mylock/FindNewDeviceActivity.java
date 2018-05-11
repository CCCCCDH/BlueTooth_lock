package com.way.mylock;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.provider.Settings;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.BroadcastReceiver;
import android.location.LocationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v4.content.ContextCompat;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import com.way.pattern.R;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class FindNewDeviceActivity extends Activity {
    private ListView find_device_listview;
    private BluetoothAdapter localBluetoothAdapter;
    private boolean hasregister=false;
    private DeviceListAdapter mAdapter;
    private ProgressDialog  searchDialog;
    private  ArrayList<Beacon> devices;
    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;
    private List<String> lstDevices = new ArrayList<String>();
    private ArrayAdapter<String> adtDevices;
    static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private Runnable doDiscoveryWork =new Runnable() {
        @Override
        public void run() {
            //开始搜索
            checkPermissions();
//            onPermissionGranted();
//            doDiscovery();
        }
    };

    private void checkPermissions() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, getString(R.string.please_open_blue), Toast.LENGTH_LONG).show();
            return;
        }

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION);
        }
    }
    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }
    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.notifyTitle)
                            .setMessage(R.string.gpsNotifyMsg)
                            .setNegativeButton(R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                            .setPositiveButton(R.string.setting,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
                                        }
                                    })

                            .setCancelable(false)
                            .show();
                } else {
                    doDiscovery();
//                    startScan();
                }
                break;
        }
    }

    public void setPairingDevices() {
        Set<BluetoothDevice> devices=localBluetoothAdapter.getBondedDevices();
        devices.clear();
        mAdapter.notifyDataSetChanged();
        if(devices.size()>0){ //存在已配对过的设备
            for(Iterator<BluetoothDevice> it=devices.iterator();it.hasNext();){
                BluetoothDevice btd=it.next();
                Beacon beacon0 =new Beacon(btd.getName(),btd.getAddress());
                this.devices.add(beacon0);
                mAdapter.notifyDataSetChanged();
            }
        }else{   //不存在已经配对的蓝牙设备
            Toast.makeText(this,"请扫描附近蓝牙设备",Toast.LENGTH_SHORT).show();
        }
    }
    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(localBluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){

            }
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.w("find:----","找到设备");
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 搜索没有配对过的蓝牙设备

                if(device.getBondState()==BluetoothDevice.BOND_NONE) {
                    String str=device.getName()+"\n"+device.getAddress();
                    if(lstDevices.indexOf(str)==-1){
                        lstDevices.add(str);
                        adtDevices.notifyDataSetChanged();
                    }
                }else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
                    device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    switch (device.getBondState()) {
                        case BluetoothDevice.BOND_BONDING:
                            Log.d("BlueToothTestActivity", "正在配对......");
                            break;
                        case BluetoothDevice.BOND_BONDED:

                            break;
                        case BluetoothDevice.BOND_NONE:
                            Log.d("BlueToothTestActivity", "取消配对");
                        default:
                            break;
                    }
                }
            }
            //搜索完成时
            if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                Log.w("finish:----", "搜索完成！");
//                searchDialog.cancel();
                if(find_device_listview.getCount()==0){
                    Toast.makeText(FindNewDeviceActivity.this,"附近没有可用设备",Toast.LENGTH_SHORT).show();
                }else{
//                    Toast.makeText(FindNewDeviceActivity.this,"搜索到"+find_device_listview.getCount()+"台设备",Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_new_device);
        find_device_listview=(ListView)this.findViewById(R.id.find_device_list);
        adtDevices=new ArrayAdapter<String>(FindNewDeviceActivity.this,android.R.layout.simple_expandable_list_item_1,lstDevices);
        find_device_listview.setAdapter(adtDevices);

        devices=new ArrayList<Beacon>();
        //mAdapter=new DeviceListAdapter(FindNewDeviceActivity.this,devices);
        localBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        doDiscoveryWork.run();

        //设置点击事件
        find_device_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(localBluetoothAdapter.isDiscovering()){
                    localBluetoothAdapter.cancelDiscovery();
                }
                String str= lstDevices.get(i);
                String[] values=str.split("\n");
                String address =values[1];
                Log.w("address----",address);
                String name =values[0];
                Log.w("name----", name);
                BluetoothDevice btDev = localBluetoothAdapter.getRemoteDevice(address);
                try {
                    Boolean returnValue = false;
                    if (btDev.getBondState() == BluetoothDevice.BOND_NONE) {
                        //利用反射方法调用BluetoothDevice.createBond(BluetoothDevice remoteDevice);
                        Method createBondMethod = BluetoothDevice.class
                                .getMethod("createBond");
                        Log.d("BlueToothTestActivity", "开始配对");
                        returnValue = (Boolean) createBondMethod.invoke(btDev);
                        if(returnValue) {
//                            Toast.makeText(FindNewDeviceActivity.this, "输入Pin码", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(btDev.getBondState()==BluetoothDevice.BOND_BONDED){
                        Toast.makeText(FindNewDeviceActivity.this,"完成配对",Toast.LENGTH_SHORT).show();
                        setPairingDevices();
                        FindNewDeviceActivity.this.finish();
                    }else{
//                        Toast.makeText(FindNewDeviceActivity.this,"配对失败",Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //注册广播
        if(!hasregister) {
            hasregister = true;
            IntentFilter filterStar = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            IntentFilter changeFilter= new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            IntentFilter filterFinish = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(mReceiver, filterFinish);
            registerReceiver(mReceiver, filterStar);
            registerReceiver(mReceiver, changeFilter);
        }
        //注册的广播在活动结束时需要注销
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //如果蓝牙适配器正在扫描，则停止扫描
        if(localBluetoothAdapter!=null&&localBluetoothAdapter.isDiscovering())
            localBluetoothAdapter.cancelDiscovery();

        //注销广播
        if(hasregister){
            hasregister=false;
            unregisterReceiver(mReceiver);
        }
    }


    void doDiscovery(){
        if(localBluetoothAdapter.isDiscovering()){
            localBluetoothAdapter.cancelDiscovery();
        }
        localBluetoothAdapter.startDiscovery();
    }
}