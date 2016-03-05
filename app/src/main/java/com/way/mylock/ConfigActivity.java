package com.way.mylock;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.way.MainActivity;
import com.way.MyLockFragment;
import com.way.pattern.R;
import com.way.sqlite.DBManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;

/**
 * Created by wise on 2015/10/20.
 */
public class ConfigActivity extends Activity {
    private ListView configListView;
    private TextView configDeviceName;
    private SimpleAdapter mAdapter;
    private String[] listContext =new String[]{"修改设备名称","修改密码","分享密钥","删除设备"};
    private int[] imageID =new int[]{R.drawable.ic_config_name,R.drawable.ic_congif_password,
    R.drawable.ic_config_share,R.drawable.ic_config_delete};
    private String configedName; //被管理设备的名称
    private String configedAddress; //被管理设备的地址
    private  String newPassword=null; //用户输入的新密码
    private Boolean isSame=false; //修改密码时 检验密码的是否一致 默认不一致
    private ProgressDialog progressDialog;
    private static android.os.Handler mHandler;
    private BluetoothSocket mSocket;
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

        //打开数据库
        dbManager =new DBManager(ConfigActivity.this);
        //handler
        mHandler=new android.os.Handler(){
            public void handleMessage(Message msg){
                progressDialog.dismiss();
                switch (msg.what){
                    case 0:
                        Log.w("handler---","get the message");
                        final AlertDialog.Builder builder =new AlertDialog.Builder(ConfigActivity.this);
                        LayoutInflater inflater =LayoutInflater.from(ConfigActivity.this);
                        final View view =inflater.inflate(R.layout.config_change_password_new, null);
                        builder.setView(view).setIcon(R.drawable.ic_congif_password).
                                setTitle("输入新密码").
                                setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //取消时要记得把socket关闭
                                       try{
                                           mSocket.close();
                                           Log.w("mHandler----","mSocket has been closed");
                                       }   catch (Exception e){

                                       }
                                    }
                                }).
                        setPositiveButton("完成", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String newPassword1=((EditText)view.findViewById(R.id.config_changePassword_new1)).
                                        getText().toString();
                                String newPassword2=((EditText)view.findViewById(R.id.config_changePassword_new2)).
                                        getText().toString();
                                if(newPassword1.equals(newPassword2)){
                                    newPassword=newPassword1;
                                    progressDialog=ProgressDialog.show(ConfigActivity.this,"正在修改","请保持在设备附近……",false,true);
                                    sendNewPassword(newPassword);
                                }else {
                                    Toast.makeText(ConfigActivity.this,"两次密码不一致，请重新输入",Toast.LENGTH_SHORT).show();
                                    builder.show();
                                }
                            }
                        }).show();
                        break;
                    case 1:
                         Toast.makeText(ConfigActivity.this,"密码错误！",Toast.LENGTH_SHORT).show();
                        //在结尾把Socket关闭
                        try{
                            mSocket.close();
                            Log.w("mHandler----","mSocket has been closed");
                        }   catch (Exception e){
                        }
                        break;
                }
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
                        final AlertDialog.Builder builder1 =new AlertDialog.Builder(ConfigActivity.this);
                        LayoutInflater inflater1 =LayoutInflater.from(ConfigActivity.this);
                        final View view1 =inflater1.inflate(R.layout.config_change_password, null);
                        builder1.setView(view1).
                                setTitle("修改设备密码").
                                setIcon(R.drawable.ic_congif_password).
                                setNegativeButton("取消",null).
                                setPositiveButton("完成", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        final String oldPassword =((EditText)view1.findViewById(R.id.config_oldPassword)).
                                                getText().toString();
                                        checkPassword(oldPassword);
                                        progressDialog=ProgressDialog.show(ConfigActivity.this,"正在检验","请保持在设备附近",false,true);
                                    }
                                }).show();
                    break;
                    //分享密码
                    case 2:
                        Toast.makeText(ConfigActivity.this,"分享密码",Toast.LENGTH_SHORT).show();
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

    private Boolean checkPassword(String password) {
        BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice bluetoothDevice=bluetoothAdapter.getRemoteDevice(configedAddress);
        CheckPasswordThread thread = new CheckPasswordThread(bluetoothDevice,password);
        thread.start();
        return isSame;
    }

    private class CheckPasswordThread extends Thread {
        private  Message message =new Message();
        private Boolean isConnect=false;
        private String password;
        public CheckPasswordThread(BluetoothDevice device,String password) {
            Log.w("CtThread  password---", "is" + password);
            Method m;
            this.password = password;
            try {
                m = device.getClass().getMethod("createInsecureRfcommSocket", new Class[]{int.class});
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

            public void run(){
            try {
                mSocket.connect();
                isConnect=true;
                Log.w("---config---连接成功！----", "success！！");
            } catch (IOException connectException) {
                //如果不能打开，关闭socket并且退出
                try {
                    Log.w("---config---连接失败！----", "btsocket");
                    mSocket.close();
                } catch (IOException s) {
                }
              }
                if(isConnect) {
                    Log.w("-----config---connect","star");
                    try {
                        OutputStream outputStream = mSocket.getOutputStream();
                        InputStream inputStream = mSocket.getInputStream();
                        outputStream.write(getHexBytes(password));
                        outputStream.flush();
                        //把输出流转化为字符串，并检查密码是否一致，改变isSame
                          inputStream2String(inputStream);
                    } catch (IOException e) {
                        Log.w("outPut wrong",e);
                    }
                }

                //如果密码正确
          if(isSame){
              Log.w("---thread----","password is same！");
              message.what=0;
              mHandler.sendMessage(message);
        }else{
              //密码不正确
              message.what=1;
              mHandler.sendMessage(message);
          }

        }

    }
    //线程类中发送密码的方法
    private void sendNewPassword(String newPassword) {
        Log.w("method----","send new password");
        try{
            InputStream in =mSocket.getInputStream();
            OutputStream out =mSocket.getOutputStream();

            out.write(getHexBytes("*" + newPassword));
            out.flush();

            String result =getReturnString(in);
            if(result.equals("9")){
                Toast.makeText(ConfigActivity.this,"修改成功!",Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                mSocket.close();
            }else {
                Toast.makeText(ConfigActivity.this, "修改失败!", Toast.LENGTH_SHORT).show();
                mSocket.close();
            }
        }catch (IOException e){

        }

    }

    private String getReturnString(InputStream in) throws IOException{
        StringBuffer out = new StringBuffer();
        int count = 0;
        while (count == 0) {
            Log.w("get Return count!!","------");
            count = in.available();
        }
        Log.w("return inputStream", "  " + count);
        byte[]  b = new byte[count];
        in.read(b);
        String mString=new String(b) ;
        Log.w("return mString","-"+mString+"-");
        return mString;
    }

    public  void inputStream2String (InputStream in) throws IOException   {
        StringBuffer out = new StringBuffer();
        int count = 0;
        while (count == 0) {
            Log.w("get count!!","------");
            count = in.available();
        }
        Log.w("inputStream.available", "  " + count);
        byte[]  b = new byte[count];
        in.read(b);
        String mString=new String(b) ;
        //如果密码是一致的
        if(mString.equals("1")){
            isSame=true;
        }else{
            isSame=false;
        }
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
