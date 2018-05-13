package com.way;

import java.math.BigInteger;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.way.mylock.Beacon;
import com.way.blebluetooth.BleBlueToothManager;
import com.way.blebluetooth.BleBlueToothCallBack;
//import com.way.mylock.CommunicationActivity;
import com.way.mylock.FindNewDeviceActivity;
import com.way.mylock.ConfigActivity;
import com.way.pattern.R;
import com.way.sqlite.DBManager;
import java.io.IOException;
import java.util.ArrayList;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import com.clj.fastble.data.BleDevice;

public class MyLockFragment extends Fragment  {
    private  static Handler mHandler;
    private final int  REQUEST_CODE_CONFIG=11;
    private BluetoothAdapter mBluetoothAdapter =BluetoothAdapter.getDefaultAdapter();
    private Button bt_addDevice;
    private final  int REQUEST_COMMUNICATE=1;
    private ListView custom_devices_listView;
    private CustomDeviceAdapter customDeviceAdapter;
    private DBManager dbManager;
    private static Context context; //!!!!!!!把这个context设正static 否则getActivity返回为空
    public EditText editText_password;
    private String open_password; //用于开锁的密码
    private  static String mString;
    private  static ProgressDialog progressDialog;
    private  static CheckBox rememberPass; //是否保存了密码
    private  static SharedPreferences pre;//用来读取保存的密码
    private  static SharedPreferences.Editor editor;
    private  static Boolean isRemember;
    private BleManager mBleManager;
    @Override
    public void onStart() {
        super.onStart();
//        mBleManager = BleManager.getInstance();
//        mBleManager.init(this.getActivity().getApplication());
//        mBleManager
//                .enableLog(true)
//                .setReConnectCount(0, 0)
//                .setOperateTimeout(5000);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.w("onCreateView", "----------");
        View view = inflater.inflate(R.layout.mylock_layout, container, false);
        if(context.equals(null)){
            Log.w("!!!!!!!!!!!!","!!!!!!");
        }
        bt_addDevice = (Button) view.findViewById(R.id.bt_addDevices);
        custom_devices_listView = (ListView) view.findViewById(R.id.myLock_listView);
        if(custom_devices_listView==null){
            Log.w("LIST","is NULL !!!!!");
        }else{
            Log.w("LIST","is  NOT NULL !!!!!");
        }

        dbManager=new DBManager(getActivity());
        customDeviceAdapter=new CustomDeviceAdapter(getActivity(),dbManager.searchAllData());
        if(!customDeviceAdapter.isEmpty()){
            Log.w("onCreate","adapter is not null");
            custom_devices_listView.setAdapter(customDeviceAdapter);
        }else{
            Log.w("onCreate", "adapter is null!!!");
        }

        bt_addDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FindNewDeviceActivity.class);
//                Intent intent = new Intent(getActivity(), CommunicationActivity.class);
                startActivityForResult(intent, 3);
            }
        });
        //如果蓝牙没有打开则打开蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            //请求用户开启
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_COMMUNICATE);
        }
        return view;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w("onCreate", "----------");

        //获取活动
        context = getActivity();


        //获取sharePreference
        pre = context.getSharedPreferences("rememberPassword",Context.MODE_PRIVATE);
        editor=pre.edit();
        //接受消息的handler
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                progressDialog.dismiss();
                switch (msg.what) {
                    case 0:
                        if(mString.equals("8")) {
                            Toast.makeText(context, "密码错误！", Toast.LENGTH_SHORT).show();
                            break;
                        }else if(mString.equals("1")||mString.equals("2")||mString.equals("3")){
                            Log.w("mString in handle ", mString + "is equal " + mString.equals("1"));
                            Toast.makeText(context, "开锁成功！", Toast.LENGTH_SHORT).show();
                            dbManager.insertRecord(Integer.parseInt(mString));
                        }else{
                            Toast.makeText(context, "未知消息！", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 1:
                        Toast.makeText(context, "开锁失败！", Toast.LENGTH_SHORT).show();
                        break;
                }
                clean();
            }
        };
    }

    public void clean( ){
//        isGetMsgOver=false;
        mString="";
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.w("onResume", "-----!!");
        customDeviceAdapter=new CustomDeviceAdapter(getActivity(),dbManager.searchAllData());
        if(customDeviceAdapter.isEmpty())
         Log.w("onResume data","is null");
        Log.w("onResume data","is NOT null");
            customDeviceAdapter.notifyDataSetChanged();
        custom_devices_listView.setAdapter(customDeviceAdapter);
        }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(requestCode==3&&resultCode==1){
        Log.w("for result 方法成功", "!!");
            Bundle  data =intent.getExtras();
            dbManager.add(data.getString("name"),data.getString("address"));
        }
    }
    @Override
     public void onDestroy() {
        super.onDestroy();
        dbManager.closeDB();//关闭数据库
    }
    public String toHex(String arg) {//将6位string转换成12位hex string
        return String.format("%06x", new BigInteger(1, arg.getBytes(/*YOUR_CHARSET?*/)));
    }
    public  void inputStreamString (byte[]  b) throws IOException   {
        Message message =new Message();//创建一个Message
        Log.w("inputStream.available", "  " + b.length);
        mString=new String(b);
        message.what=0;
        Log.w("mString +","-"+mString+"-");
        mHandler.sendMessage(message);
    }
    public void OpenDoor(String mac,String pwd){
        final BleBlueToothManager mBleManager = new BleBlueToothManager();
        mBleManager.init(this.getActivity().getApplication());
        final String msg = ("01"+toHex(pwd)+"23");
        mBleManager.Connect(mac, msg, new BleBlueToothCallBack(){
            @Override
            public void onConnectFail() {
                Log.w("gattConnect", "--------连接失败！----");
                Message message = new Message();
                message.what = 1;
                mString="连接失败！";
                mHandler.sendMessage(message);
            }

            @Override
            public void WriteFail() {
                Log.w("gattWrite", "--------发送指令失败！----");
                Message message = new Message();
                message.what = 1;
                mString="发送指令失败！";
                mHandler.sendMessage(message);
            }

            @Override
            public void GetMsg(byte[] data) {
                try{
                    inputStreamString(data);
                    mBleManager.DisConnect();
                }
                catch (IOException e) {
                }
            }

            @Override
            public void GetMsgFail() {
                Log.w("gattNotify", "--------订阅消息失败！----");
                Message message = new Message();
                message.what = 1;
                mString="接收门锁消息失败！";
                mHandler.sendMessage(message);
                mBleManager.DisConnect();
            }
        });
    }
//    //定义一个连接线程类
//    public class ConnectThread extends Thread {
//        private BleDevice mBleDevice;
//        private String password;
//
//        public ConnectThread(BluetoothDevice device, final String pwd) {
//            Log.w("CtThread btDevice---", device.getAddress());
//            this.password=("01"+toHex(pwd)+"23");
//            mBleDevice = mBleManager.convertBleDevice(device);
//        }
//        public void run() {
//            mBluetoothAdapter.cancelDiscovery();
//            connect(mBleDevice);
//        }
//
//        private void connect(final BleDevice mBleDevice) {
//
//            mBleManager.connect(mBleDevice.getMac(), new BleGattCallback() {
//                @Override
//                public void onStartConnect() {
//                }
//
//                @Override
//                public void onConnectFail(BleDevice bleDevice, BleException exception) {
//                    Log.w("--------连接失败！----", "btsocket");
//                    Message message = new Message();
//                    message.what = 1;
//                    mString="连接失败！";
//                    mHandler.sendMessage(message);
////                    mBleManager.disconnectAllDevice();
//                }
//
//                @Override
//                public void onConnectSuccess(final BleDevice bleDevice,final BluetoothGatt gatt, int status) {
//                    mBleManager.notify(
//                    bleDevice,
//                    "0000ffe0-0000-1000-8000-00805f9b34fb",
//                    "0000ffe1-0000-1000-8000-00805f9b34fb",
//                    new BleNotifyCallback() {
//
//                        @Override
//                        public void onNotifySuccess() {
//                        }
//
//                        @Override
//                        public void onNotifyFailure(final BleException exception) {
//                            Log.w("--------接收消息失败！----", "btsocket");
////                            StopNotify(bleDevice);
//                            Message message = new Message();
//                            message.what = 1;
//                            mString="接收门锁消息失败！";
//                            mHandler.sendMessage(message);
//                            mBleManager.disconnect(bleDevice);
////                            mBleManager.getBluetoothGatt(mBleDevice).close();
//                        }
//
//                        @Override
//                        public void onCharacteristicChanged(byte[] data) {
//                            try{
//                                inputStreamString(data);
////                                StopNotify(bleDevice);
//                                mBleManager.disconnect(bleDevice);
////                                mBleManager.getBluetoothGatt(mBleDevice).close();
//                            }
//                            catch (IOException e) {
//                            }
//                        }
//                    });
//
//                    mBleManager.write(
//                        bleDevice,
//                        "0000ffe0-0000-1000-8000-00805f9b34fb",
//                        "0000ffe1-0000-1000-8000-00805f9b34fb",
//                            HexUtil.hexStringToBytes(password),//HexUtil.hexStringToBytes(password)
//                        new BleWriteCallback() {
//
//                            @Override
//                            public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
//
//                            }
//
//                            @Override
//                            public void onWriteFailure(final BleException exception) {
////                                StopNotify(bleDevice);
//                                Log.w("--------发送消息失败！----", "btsocket");
//                                Message message = new Message();
//                                message.what = 1;
//                                mString="发送指令失败！";
//                                mHandler.sendMessage(message);
//                                mBleManager.disconnect(bleDevice);
////                                mBleManager.getBluetoothGatt(mBleDevice).close();
//                            }
//                        });
//                }
//
//                @Override
//                public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
//                }
//            });
//        }
//
//
//        public void StopNotify(BleDevice bleDevice){
//            mBleManager.stopNotify(
//                    bleDevice,
//                    "0000ffe0-0000-1000-8000-00805f9b34fb",
//                    "0000ffe1-0000-1000-8000-00805f9b34fb");
//        }

//    }
    //自定义的adapter
    public class CustomDeviceAdapter extends BaseAdapter {
        private ArrayList<Beacon> devices;
        private LayoutInflater inflater;
        private Context context;
        public CustomDeviceAdapter(Activity activity ,ArrayList<Beacon> devices){
            inflater=activity.getLayoutInflater();
            context=activity;
            this.devices=devices;
        }

        public ArrayList<Beacon> addDevice(Beacon device){
            devices.add(device);
            return devices;
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int i) {
            return devices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            final int count=i;
            if(view ==null){
                viewHolder=new ViewHolder();
                view =inflater.inflate(R.layout.mylock_custom_listitem_layout,null);
                viewHolder.deviceName=(TextView)view.findViewById(R.id.listItem_custom_name);
                viewHolder.deviceAddress=(TextView)view.findViewById(R.id.listItem_custom_address);
                viewHolder.bt_open=(Button)view.findViewById(R.id.bt_openLock);
                viewHolder.bt_config=(Button)view.findViewById(R.id.bt_config);
                view.setTag(viewHolder);
            }else{
                viewHolder=(ViewHolder)view.getTag();
            }

            final Beacon device =devices.get(i);
            final String deviceName3=device.getName();
            if(deviceName3!=null&&deviceName3.length()>0)
                viewHolder.deviceName.setText(deviceName3);
            else
                viewHolder.deviceName.setText("Unknown device");
            //回调
            viewHolder.bt_config.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    starConfig(device.getName(), device.getAddress());
                }
            });
            viewHolder.bt_open.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openLock(count, device.getAddress());
                    Log.w("openLock---", "" + count + "  address" + device.getAddress());
                }
            });
            viewHolder.deviceAddress.setText(device.getAddress());
            return view;
        }
        class ViewHolder{
            TextView deviceName;
            TextView deviceAddress;
            Button bt_open;
            Button bt_config;
        }

    }

    private void starConfig(String deviceName,String deviceAddress) {
        Log.w("config----", "has Clicked!!");
        Log.w("context----", "has added!!");
        Intent intent =new Intent(context,ConfigActivity.class);
        intent.putExtra("configName",deviceName);
        intent.putExtra("configAddress",deviceAddress);
        startActivityForResult(intent, REQUEST_CODE_CONFIG);
    }

    private void openLock(int i, final String open_address) {
        final int count =i;
        //之前是否点击了保存密码，默认没有
        isRemember =pre.getBoolean("remember_password"+count,false);
        Log.w("bt_open  "+count, "----"+isRemember);
        Log.w("openLock information:","address:"+open_address);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater =LayoutInflater.from(context);
        View view0 =inflater.inflate(R.layout.lock_password_dialog, null);
        editText_password=(EditText)view0.findViewById(R.id.lock_password_dialog);
        rememberPass=(CheckBox)view0.findViewById(R.id.remember_pass);

        //如果保存了密码
        if(isRemember) {
            Log.w("openLock isRemember :","YES"+" count:"+count);
            open_password = pre.getString("password"+count, "");
            editText_password.setText(open_password);
            rememberPass.setChecked(true);
        }

        builder.setView(view0).
                setTitle("请输入蓝牙锁密码").
                setIcon(R.drawable.ic_openlock).
                setNegativeButton("取消", null).
                setPositiveButton("发送", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        open_password = editText_password.getText().toString();
                        if(open_password.length()!=6)
                        {
                            Toast.makeText(context, "请输入6位数密码", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if(rememberPass.isChecked()){
                            Log.w("isChecked---","YES  "+count);
                            editor=pre.edit();
                            editor.putString("password"+count,open_password);
                            editor.putBoolean("remember_password" + count, true);
                        }else{
                            Log.w("isChecked--","NOT  "+count);
                            editor.putBoolean("remember_password" + count, false);
                        }
                        editor.commit();

//                        BluetoothDevice btDev = mBluetoothAdapter.getRemoteDevice(open_address);
//                        final ConnectThread connectThread = new ConnectThread(btDev, open_password);
//                        connectThread.start();
                        OpenDoor(open_address,open_password);
                        progressDialog=ProgressDialog.show(context,"请稍等……","正在开锁",false,true);
                    }
                }).show();
    }
}