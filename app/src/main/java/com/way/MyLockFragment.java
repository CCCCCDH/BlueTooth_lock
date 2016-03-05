package com.way;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
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
import com.way.mylock.CommunicationActivity;
import com.way.mylock.ConfigActivity;
import com.way.pattern.R;
import com.way.sqlite.DBManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;


/**
 * Created by wise on 2015/10/7.
 */
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

    @Override
    public void onStart() {
        super.onStart();

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
                Intent intent = new Intent(getActivity(), CommunicationActivity.class);
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
                        Log.w("mString in handle ", mString + "is equal " + mString.equals("1"));
                        if(mString.equals("1")) {
                            Log.w("mhandler open class:", "----class:主人");
                            Toast.makeText(context, "开锁成功！", Toast.LENGTH_SHORT).show();
                            //插入一条开锁记录
                            dbManager.insertRecord(1);
                            //当开锁成功时，检查复选框被选中
                        }else if(mString.equals("2")){
                            Log.w("mhandler open class:", "----class:常驻人员");
                            Toast.makeText(context, "开锁成功！", Toast.LENGTH_SHORT).show();
                            //插入一条开锁记录
                            dbManager.insertRecord(2);
                        }else if(mString.equals("3")){
                            Log.w("mhandler open class:", "----class:访客");
                            Toast.makeText(context, "开锁成功！", Toast.LENGTH_SHORT).show();
                            //插入一条开锁记录
                            dbManager.insertRecord(3);
                        }else
                            Toast.makeText(context, "密码错误！", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        Toast.makeText(context, "开锁失败！", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
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
    //定义一个连接线程类
    public class ConnectThread extends Thread {
        private Boolean isConnected = false;
        private BluetoothSocket mSocket;
        private String password;

        public ConnectThread(BluetoothDevice device, final String password) {
            Log.w("CtThread btDevice---", device.getAddress());
            this.password = password;
            Method m;
            try {
                m =device.getClass().getMethod("createInsecureRfcommSocket", new Class[] {int.class});
                mSocket = (BluetoothSocket) m.invoke(device, 1);
            } catch (SecurityException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (NoSuchMethodException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


        }

        public void run() {
            mBluetoothAdapter.cancelDiscovery();
            try {
                mSocket.connect();
                Log.w("--------连接成功！----", "success！！");
                isConnected = true;
            } catch (IOException connectException) {
                //如果不能打开，关闭socket并且退出
                try {
                    Log.w("--------连接失败！----", "btsocket");
                    Message message=new Message();
                    message.what=1;
                    mHandler.sendMessage(message);
                    mSocket.close();
                } catch (IOException s) {
                    return;
                }
            }

            if (isConnected) {
                try {
                    OutputStream outputStream = mSocket.getOutputStream();
                    InputStream inputStream = mSocket.getInputStream();
                    outputStream.write(getHexBytes(password));
                    outputStream.flush();

                     inputStream2String(inputStream);


                } catch (IOException e) {
                }



            if (mSocket != null) {
                try {
                    mSocket.close();
                    Log.w("mSocket has been", "closed!!");
                } catch (IOException e) {
                }
            }
        }
            }


        public  void inputStream2String (InputStream in) throws IOException   {
            Message message =new Message();//创建一个Message
            StringBuffer out = new StringBuffer();
            int count = 0;
            while (count == 0) {
                count = in.available();
            }
            Log.w("inputStream.available", "  " + count);
            byte[]  b = new byte[count];
            in.read(b);
            mString=new String(b) ;
            message.what=0;
            Log.w("mString +","-"+mString+"-");
            mHandler.sendMessage(message);
        }
        private byte[] getHexBytes(String password) {
            int i = 0, n = 0;
            byte[] bos = password.getBytes();
            for (i = 0; i < bos.length; i++) {
                if (bos[i] == 0x0a) n++;
            }
            byte[] bos_new = new byte[bos.length + n];
            n = 0;
            for (i = 0; i < bos.length; i++) { //手机中换行为0a,将其改为0d 0a后再发送
                if (bos[i] == 0x0a) {
                    bos_new[n] = 0x0d;
                    n++;
                    bos_new[n] = 0x0a;
                } else {
                    bos_new[n] = bos[i];
                }
                n++;
            }
            return bos_new;
        }

    }

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

                        BluetoothDevice btDev = mBluetoothAdapter.getRemoteDevice(open_address);
                        progressDialog=ProgressDialog.show(context,"请稍等……","正在开锁",false,true);
                        ConnectThread connectThread = new ConnectThread(btDev, open_password);
                        connectThread.start();
                    }
                }).show();
    }

}