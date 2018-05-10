package com.way.mylock;

import java.math.BigInteger;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.bluetooth.BluetoothGatt;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup;

import com.way.pattern.R;
import com.way.sqlite.DBManager;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import com.clj.fastble.data.BleDevice;

/**
 * Created by wise on 2015/10/20.
 */
public class ConfigActivity extends Activity {
    private ListView configListView;
    private TextView configDeviceName;
    private SimpleAdapter mAdapter;
//    private int msgTypeTag=0;
    private String mString="";
//    private String mUserPwd="";
//    private String mLoginPwd="";
    private String string_userClass="";
    private BluetoothAdapter mBluetoothAdapter =BluetoothAdapter.getDefaultAdapter();
    private String[] listContext =new String[]{"修改设备名称","修改密码","分享密钥","删除设备"};
    private int[] imageID =new int[]{R.drawable.ic_config_name,R.drawable.ic_congif_password,
    R.drawable.ic_config_share,R.drawable.ic_config_delete};
    private String configedName=""; //被管理设备的名称
    private String configedAddress=""; //被管理设备的地址
//    private  static Boolean isGetMsgOver;
    private ProgressDialog progressDialog;
    private static android.os.Handler mHandler;
    private BleManager mBleManager;
    private DBManager dbManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config_layout);
        configDeviceName=(TextView)findViewById(R.id.config_deviceName);
        configListView=(ListView)findViewById(R.id.config_listView);
        //获取上一个活动中传递过来的自定义名称
        Intent intent=getIntent();
        configedName=intent.getStringExtra("configName");
        configedAddress=intent.getStringExtra("configAddress");
        configDeviceName.setText(configedName);
        mBleManager = BleManager.getInstance();
        mBleManager.init(this.getApplication());
        mBleManager
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setOperateTimeout(5000);
        //打开数据库
        dbManager =new DBManager(ConfigActivity.this);
        //handler
        mHandler=new android.os.Handler(){
            public void handleMessage(Message msg){
                progressDialog.dismiss();
                switch (msg.what){
                    case 0:
                        if(mString.length()==1){
                            if(mString.equals("8")) {
                                Toast.makeText(ConfigActivity.this, "密码错误！", Toast.LENGTH_SHORT).show();
                                break;
                            }else if(mString.equals("1")){
                                Toast.makeText(ConfigActivity.this, "修改主人密码成功！", Toast.LENGTH_SHORT).show();
                            }else if(mString.equals("2")){
                                Toast.makeText(ConfigActivity.this, "修改住户密码成功！", Toast.LENGTH_SHORT).show();
                            }else if(mString.equals("3")){
                                Toast.makeText(ConfigActivity.this, "修改访客密码成功！", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(ConfigActivity.this, "收到未知消息！", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }else if(mString.length()==13){
                            LayoutInflater inflater3 =LayoutInflater.from(ConfigActivity.this);
                            final View view3 =inflater3.inflate(R.layout.config_get_password_result, null);

                            final AlertDialog.Builder builder3 =new AlertDialog.Builder(ConfigActivity.this);

                            TextView editText_Userword=(TextView)view3.findViewById(R.id.config_GetPwd_Password_User);
                            editText_Userword.setText(mString.substring(1,7));
                            TextView editText_Loginword=(TextView)view3.findViewById(R.id.config_GetPwd_Password_Login);
                            editText_Loginword.setText(mString.substring(7,13));

                            builder3.setView(view3).
                                    setTitle("分享秘钥成功").
                                    setIcon(R.drawable.ic_config_share).
                                    setNegativeButton("关闭",null).show();
                            break;
                        }else{
                            Toast.makeText(ConfigActivity.this, "获得秘钥出错！", Toast.LENGTH_SHORT).show();
                        }

                    case 1:
                         Toast.makeText(ConfigActivity.this,mString,Toast.LENGTH_SHORT).show();
                        //在结尾把Socket关闭
//                        try{
//                            mSocket.close();
//                            Log.w("mHandler----","mSocket has been closed");
//                        }   catch (Exception e){
//                        }
                        break;
                }
                clean();
            }
        };
        //创建一个Map
        List<Map<String,Object>>listItems =new ArrayList<Map<String, Object>>();
        //往list_item里加入要素
        for(int i=0;i<listContext.length;i++){
            Map<String ,Object> listItem=new HashMap<String, Object>();
            listItem.put("image",imageID[i]);
            listItem.put("list_content",listContext[i]);
            //把每一个listitem加入到listitems里面
            listItems.add(listItem);
        }
        //设置simpleAdapter

        mAdapter=new SimpleAdapter(ConfigActivity.this,listItems,R.layout.config_list_ltem,
                new String[]{"image","list_content"},new int[]{R.id.config_imageView,R.id.config_nameView});

        configListView.setAdapter(mAdapter);
        configListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    //点击修改设备名称
                    case 0:
                        AlertDialog.Builder builder0 =new AlertDialog.Builder(ConfigActivity.this);
                        LayoutInflater inflater0 =LayoutInflater.from(ConfigActivity.this);
                        final View view0 =inflater0.inflate(R.layout.config_change_name, null);
                        builder0.setView(view0).
                                setTitle("修改设备名称").
                                setIcon(R.drawable.ic_config_name).
                                setNegativeButton("取消",null).
                                setPositiveButton("完成", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        final String newName=((EditText)view0.findViewById(R.id.config_changedName)).
                                                getText().toString();
                                        //对数据库进行操作
                                        dbManager.searchAllData();
                                        dbManager.updataName(newName, configedName);
                                        configDeviceName.setText(newName);
                                        dbManager.closeDB();
                                        Toast.makeText(ConfigActivity.this,"修改完成",Toast.LENGTH_SHORT).show();
                                    }
                                }).show();
                        break;
                    //点击修改密码
                    case 1:
                        LayoutInflater inflater1 =LayoutInflater.from(ConfigActivity.this);
                        final View view1 =inflater1.inflate(R.layout.config_change_password_new, null);

                        final RadioGroup  user_class=(RadioGroup)view1.findViewById(R.id.config_rg);
                        user_class.check(R.id.config_class_host);
                        user_class.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                                switch (i) {
                                    case R.id.config_class_host:
                                        string_userClass = "01";
                                        break;
                                    case R.id.config_class_live:
                                        string_userClass = "02";
                                        break;
                                    case R.id.config_class_visitor:
                                        string_userClass = "03";
                                        break;
                                }
                            }
                        });

                        final AlertDialog.Builder builder1 =new AlertDialog.Builder(ConfigActivity.this);
                        builder1.setView(view1).
                                setTitle("修改设备密码").
                                setIcon(R.drawable.ic_congif_password).
                                setNegativeButton("取消",null).
                                setPositiveButton("完成", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if(string_userClass.equals(""))
                                        {
                                            string_userClass = "01";
                                        }
                                        EditText masterPasswordText = (EditText)view1.findViewById(R.id.config_changePassword_master);
                                        EditText newPasswordText = (EditText)view1.findViewById(R.id.config_changePassword_new1);
                                        EditText newPassword2Text = (EditText)view1.findViewById(R.id.config_changePassword_new2);

                                        String masterPassword =masterPasswordText.getText().toString();
                                        String newPassword =newPasswordText.getText().toString();
                                        String newPassword2 =newPassword2Text.getText().toString();
                                        if(masterPassword.length()!=6||newPassword.length()!=6||newPassword2.length()!=6)
                                        {
                                            Toast.makeText(ConfigActivity.this,"请输入6位数密码",Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        if(!newPassword.equals(newPassword2))
                                        {
                                            Toast.makeText(ConfigActivity.this,"两次密码输入不一致",Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        checkPassword(masterPassword, newPassword);
                                        progressDialog=ProgressDialog.show(ConfigActivity.this,"正在检验","请保持在设备附近",false,true);
                                    }
                                }).show();
                    break;
                    //分享密码
                    case 2:
                        LayoutInflater inflater2 =LayoutInflater.from(ConfigActivity.this);
                        final View view2 =inflater2.inflate(R.layout.config_get_password, null);

                        final AlertDialog.Builder builder2 =new AlertDialog.Builder(ConfigActivity.this);
                        builder2.setView(view2).
                                setTitle("获取住户和访客密码").
                                setIcon(R.drawable.ic_config_share).
                                setNegativeButton("取消",null).
                                setPositiveButton("完成", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        final String masterPassword =((EditText)view2.findViewById(R.id.config_GetPwd_Master_Password)).
                                                getText().toString();
                                        if(masterPassword.length()!=6)
                                        {
                                            Toast.makeText(ConfigActivity.this,"请输入6位数密码",Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        GetPwd(masterPassword);
                                        progressDialog=ProgressDialog.show(ConfigActivity.this,"正在检验","请保持在设备附近",false,true);
                                    }
                                }).show();
                        break;
                    //删除设备
                    case 3:
                        AlertDialog.Builder builder =new AlertDialog.Builder(ConfigActivity.this);
                        builder.setTitle("删除设备").
                                setMessage("您确认删除设备吗？").
                                setNegativeButton("取消",null).
                                setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dbManager.deleteDevice(configedAddress);
                                        Toast.makeText(ConfigActivity.this,"删除成功！",Toast.LENGTH_SHORT).show();
                                        ConfigActivity.this.finish();
                                    }
                                }).show();
                }
            }
        });
        }

    private void checkPassword(String msterPassword,String newPassword) {
        mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice bluetoothDevice=mBluetoothAdapter.getRemoteDevice(configedAddress);
        CheckPasswordThread thread = new CheckPasswordThread(bluetoothDevice,msterPassword,newPassword);
        thread.start();
//        return isSame;
    }

    public String toHex(String arg) {//将6位string转换成12位hex string
        return String.format("%06x", new BigInteger(1, arg.getBytes(/*YOUR_CHARSET?*/)));
    }

    private class CheckPasswordThread extends Thread {
        private  Message message =new Message();
        private Boolean isConnect=false;
        private BleDevice mBleDevice;
        private String password;
        public CheckPasswordThread(BluetoothDevice device,String masterPassword,String newPassword) {
            Method m;
            this.password=("02"+toHex(masterPassword)+string_userClass+toHex(newPassword)+"23");
            mBleDevice = mBleManager.convertBleDevice(device);
        }

            public void run(){
                mBluetoothAdapter.cancelDiscovery();
                connect(mBleDevice);

        }
        private void connect(final BleDevice mBleDevice) {
            mBleManager.connect(mBleDevice.getMac(), new BleGattCallback() {
                @Override
                public void onStartConnect() {
                    // 开始连接
                }

                @Override
                public void onConnectFail(BleDevice bleDevice, BleException exception) {
                    Log.w("--------连接失败！----", "btsocket");
                    Message message = new Message();
                    message.what = 1;
                    mString="连接失败！";
                    mHandler.sendMessage(message);
                }

                @Override
                public void onConnectSuccess(final BleDevice bleDevice,final BluetoothGatt gatt, int status) {
                    mBleManager.notify(
                            bleDevice,
                            "0000ffe0-0000-1000-8000-00805f9b34fb",
                            "0000ffe1-0000-1000-8000-00805f9b34fb",
                            new BleNotifyCallback() {

                                @Override
                                public void onNotifySuccess() {
                                }

                                @Override
                                public void onNotifyFailure(final BleException exception) {
                                    Log.w("--------接收消息失败！----", "btsocket");
                                    StopNotify(bleDevice);
                                    Message message = new Message();
                                    message.what = 1;
                                    mString="接收门锁消息失败！";
                                    mHandler.sendMessage(message);
                                }

                                @Override
                                public void onCharacteristicChanged(byte[] data) {
                                    try{
                                        inputStreamString(data);
                                        StopNotify(bleDevice);
                                    }
                                    catch (IOException e) {
                                    }
                                }
                            });

                    mBleManager.write(
                            bleDevice,
                            "0000ffe0-0000-1000-8000-00805f9b34fb",
                            "0000ffe1-0000-1000-8000-00805f9b34fb",
                            HexUtil.hexStringToBytes(password),
                            new BleWriteCallback() {

                                @Override
                                public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {

                                }

                                @Override
                                public void onWriteFailure(final BleException exception) {
                                    StopNotify(bleDevice);
                                    Log.w("--------发送消息失败！----", "btsocket");
                                    Message message = new Message();
                                    message.what = 1;
                                    mString="发送修改指令失败！";
                                    mHandler.sendMessage(message);
                                }
                            });
                }

                @Override
                public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                }
            });
        }
    }

    private void GetPwd(String msterPassword) {
        mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice bluetoothDevice=mBluetoothAdapter.getRemoteDevice(configedAddress);
        GetPwdThread thread = new GetPwdThread(bluetoothDevice,msterPassword);
        thread.start();
    }

    private class GetPwdThread extends Thread {
        private  Message message =new Message();
        private Boolean isConnect=false;
        private BleDevice mBleDevice;
        private String password;
        public GetPwdThread(BluetoothDevice device,String masterPassword) {
            Method m;
            this.password=("03"+toHex(masterPassword)+"23");
            mBleDevice = mBleManager.convertBleDevice(device);
        }

        public void run(){
            mBluetoothAdapter.cancelDiscovery();
            connect(mBleDevice);
        }
        private void connect(final BleDevice mBleDevice) {
            mBleManager.connect(mBleDevice.getMac(), new BleGattCallback() {
                @Override
                public void onStartConnect() {
                    // 开始连接
                }

                @Override
                public void onConnectFail(BleDevice bleDevice, BleException exception) {
                    Log.w("--------连接失败！----", "btsocket");
                    Message message = new Message();
                    message.what = 1;
                    mString="连接失败！";
                    mHandler.sendMessage(message);
                }

                @Override
                public void onConnectSuccess(final BleDevice bleDevice,final BluetoothGatt gatt, int status) {
//                    isGetMsgOver=false;
                    mBleManager.notify(
                            bleDevice,
                            "0000ffe0-0000-1000-8000-00805f9b34fb",
                            "0000ffe1-0000-1000-8000-00805f9b34fb",
                            new BleNotifyCallback() {

                                @Override
                                public void onNotifySuccess() {
                                }

                                @Override
                                public void onNotifyFailure(final BleException exception) {
                                    Log.w("--------接收消息失败！----", "btsocket");
                                    Message message = new Message();
                                    message.what = 1;
                                    mString="接收门锁消息失败！";
                                    mHandler.sendMessage(message);
                                }

                                @Override
                                public void onCharacteristicChanged(byte[] data) {
                                    try{
                                        inputStreamString(data);
                                        StopNotify(bleDevice);
                                    }
                                    catch (IOException e) {
                                    }
                                }
                            });

                    mBleManager.write(
                            bleDevice,
                            "0000ffe0-0000-1000-8000-00805f9b34fb",
                            "0000ffe1-0000-1000-8000-00805f9b34fb",
                            HexUtil.hexStringToBytes(password),
                            new BleWriteCallback() {

                                @Override
                                public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {

                                }

                                @Override
                                public void onWriteFailure(final BleException exception) {
                                    StopNotify(bleDevice);
                                    Log.w("--------发送消息失败！----", "btsocket");
                                    Message message = new Message();
                                    message.what = 1;
                                    mString="发送查看指令失败！";
                                    mHandler.sendMessage(message);
                                }
                            });
                }

                @Override
                public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                }
            });
        }
    }

    public void StopNotify(BleDevice bleDevice){
        mBleManager.stopNotify(
                bleDevice,
                "0000ffe0-0000-1000-8000-00805f9b34fb",
                "0000ffe1-0000-1000-8000-00805f9b34fb");
    }

    public void clean( ){
//        isGetMsgOver=false;
        mString="";
//        mLoginPwd="";
//        mUserPwd="";
    }

    public  void inputStreamString (byte[]  b) throws IOException   {
        Log.w("inputStream.available", "  " + b.length);
        mString=new String(b) ;
//        Toast.makeText(ConfigActivity.this,mString,Toast.LENGTH_SHORT).show();
        Message message =new Message();//创建一个Message
        message.what=0;
        Log.w("mString +","-"+mString+"-");
        mHandler.sendMessage(message);
    }
}
