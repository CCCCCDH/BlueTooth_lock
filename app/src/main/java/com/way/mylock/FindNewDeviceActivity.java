package com.way.mylock;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.way.MainActivity;
import com.way.pattern.R;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by wise on 2015/10/13.
 */
public class FindNewDeviceActivity extends Activity {
    private ListView find_device_listview;
    private BluetoothAdapter localBluetoothAdapter;
    private boolean hasregister=false;
    private DeviceListAdapter mAdapter;
    private ProgressDialog  searchDialog;
    private  ArrayList<Beacon> devices;
    private List<String> lstDevices = new ArrayList<String>();
    private ArrayAdapter<String> adtDevices;
//    static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private Runnable doDiscoveryWork =new Runnable() {
        @Override
        public void run() {
            //开始搜索
            doDiscovery();
        }
    };


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
            Toast.makeText(this,"不存在已配对设备",Toast.LENGTH_SHORT).show();
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
                    //Beacon beacon = new Beacon(device.getName(), device.getAddress());
                    String str=device.getName()+"\n"+device.getAddress();
                    if(lstDevices.indexOf(str)==-1){
                        Log.w("address:----",device.getAddress());
                        Log.w("name:----",device.getName());
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
                    Toast.makeText(FindNewDeviceActivity.this,"搜索到"+find_device_listview.getCount()+"台设备",Toast.LENGTH_SHORT).show();
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


        //find_device_listview.setAdapter(mAdapter);
        //实例化数组

//        searchDialog = new ProgressDialog(FindNewDeviceActivity.this);
//        searchDialog.setMessage("正在搜索，请稍等……");
//        searchDialog.setCancelable(false);
//        searchDialog.show();
//        Log.w("start:----","开始搜索");

        devices=new ArrayList<Beacon>();
        mAdapter=new DeviceListAdapter(FindNewDeviceActivity.this,devices);
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
                        if(returnValue)
                            Toast.makeText(FindNewDeviceActivity.this,"输入Pin码",Toast.LENGTH_SHORT).show();

                    }
                    if(btDev.getBondState()==BluetoothDevice.BOND_BONDED){
                        Toast.makeText(FindNewDeviceActivity.this,"完成配对",Toast.LENGTH_SHORT).show();
                        setPairingDevices();
                        FindNewDeviceActivity.this.finish();
                    }else{
                        Toast.makeText(FindNewDeviceActivity.this,"未配对",Toast.LENGTH_SHORT).show();
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
