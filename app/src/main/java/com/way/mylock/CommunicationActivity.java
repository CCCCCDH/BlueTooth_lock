package com.way.mylock;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.way.MainActivity;
import com.way.MyLockFragment;
import com.way.pattern.R;
import com.way.sqlite.DBManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by wise on 2015/10/9.
 */
public class CommunicationActivity extends Activity  implements DeviceListAdapter.Callbacks {
    private ListView devices_listView;
    private DeviceListAdapter mAdapter;
    private ArrayList<Beacon> ArrayList_devices;
    private Button bt_addDevice;
    private BluetoothAdapter localBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
    private DBManager dbManager;
    private Boolean hasAdd=false;

    @Override
    public void setPairingDevices() {
        ArrayList_devices.clear();
        //让adapter获得以匹配的设备
        setPairingDevice();
        mAdapter.notifyDataSetChanged();
        Toast.makeText(this,"已取消配对！",Toast.LENGTH_SHORT).show();
        CommunicationActivity.this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.communicate_layout);

        //找到listview
        devices_listView = (ListView) this.findViewById(R.id.devices_list);
        ArrayList_devices=new ArrayList<Beacon>();
        mAdapter = new DeviceListAdapter(CommunicationActivity.this,ArrayList_devices);
        devices_listView.setAdapter(mAdapter);
        //添加新的bluetooth
        bt_addDevice=(Button)this.findViewById(R.id.bt_addNewBluetooth);
        bt_addDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Intent intent =new Intent(CommunicationActivity.this,FindNewDeviceActivity.class);
                startActivity(intent);
            }
        });
        //获取可配对的蓝牙设备

        setPairingDevice();


        //item的点击事件
        devices_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.w("list_item","Click!!!");
                final Beacon beacon0 =ArrayList_devices.get(i);
               //创建对话框
                LayoutInflater layoutInflater=LayoutInflater.from(CommunicationActivity.this);
                View myDialogView =layoutInflater.inflate(R.layout.lock_build_dialog, null);
                final EditText custom_name=(EditText)myDialogView.findViewById(R.id.custom_edit_name);
                TextView address0=(TextView)myDialogView.findViewById(R.id.custom_address);
                address0.setText(beacon0.getAddress());

                //打开数据库
                dbManager = new DBManager(CommunicationActivity.this);
                if(hasAdd=dbManager.isAdded(beacon0.getAddress())){
                    Toast.makeText(CommunicationActivity.this,"该设备已添加！请勿重复添加",Toast.LENGTH_SHORT).show();
                }
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(CommunicationActivity.this).setTitle("用户自定义").
                        setIcon(R.drawable.ic_usercenter_help).
                        setView(myDialogView).
                        //完成按钮
                        setPositiveButton("完成", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                beacon0.name = custom_name.getText().toString();
                                    Intent intent = getIntent();
                                    intent.putExtra("name", beacon0.name);
                                    intent.putExtra("address", beacon0.address);
                                    CommunicationActivity.this.setResult(1, intent);
                                    CommunicationActivity.this.finish();
                                }
                        }).
                        setNegativeButton("取消",null);
                //如果没有被添加，就显示对话框
                if(!hasAdd){
                    alertDialog.show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w("Communication--:", "onResume!!");
        setPairingDevice();
        mAdapter.notifyDataSetChanged();
    }

    private void setPairingDevice() {
        Set<BluetoothDevice> devices=localBluetoothAdapter.getBondedDevices();
        if(devices.size()>0){ //存在已配对过的设备
            ArrayList_devices.clear();
            for(Iterator<BluetoothDevice> it=devices.iterator();it.hasNext();){
                BluetoothDevice btd=it.next();
                Beacon beacon0 =new Beacon(btd.getName(),btd.getAddress());
                ArrayList_devices.add(beacon0);
            }
        }else{   //不存在已经配对的蓝牙设备
            Toast.makeText(this,"不存在已配对设备",Toast.LENGTH_SHORT).show();
        }

    }
}
