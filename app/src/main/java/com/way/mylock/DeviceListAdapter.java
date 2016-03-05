package com.way.mylock;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.way.pattern.R;

import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.security.auth.callback.Callback;

/**
 * Created by wise on 2015/10/8.
 */
public class DeviceListAdapter extends BaseAdapter {
    private Callbacks mCallback;
    private BluetoothAdapter mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
    private ArrayList<Beacon> devices;
    private LayoutInflater inflater;
    private Activity context;
    public  interface Callbacks{
        public void  setPairingDevices();
    }
    public DeviceListAdapter(Activity activity,ArrayList<Beacon> devices){
        super();
        context=activity;
        this.devices=devices ;
        inflater=context.getLayoutInflater();
        mCallback=(Callbacks)activity;
    }



    public DeviceListAdapter(Activity activity){
        super();
        context=activity;
        inflater=context.getLayoutInflater();
    }

    //添加一个设备
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
    public View getView(int i, View view, ViewGroup viewGroup) {
        final ViewHolder viewHolder;
        final int position=i;
        //如果view没有内容，则进行填充
        if (view == null) {
            viewHolder=new ViewHolder();
            view = inflater.inflate(R.layout.mylock_listitem_layout, null);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
            viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
            viewHolder.bt_cancel=(Button)view.findViewById(R.id.bt_cancel_pair);
            view.setTag(viewHolder);
        }else{
            viewHolder=(ViewHolder)view.getTag();
        }

        final Beacon  device=devices.get(i);
        final String deviceName=device.getName();
        if(deviceName!=null&&deviceName.length()>0)
            viewHolder.deviceName.setText(deviceName);
        else
            viewHolder.deviceName.setText("Unknown device");
           viewHolder.deviceAddress.setText(device.getAddress());

           viewHolder.bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothDevice device0=mBluetoothAdapter.getRemoteDevice(device.getAddress());
                try{
                    Boolean returnValue = false;
                    Method m=device0.getClass().getMethod("removeBond");
                    returnValue = (Boolean) m.invoke(device0);
                    if(returnValue)
                       Log.w("取消配对！！！！","!!!");
                }catch (Exception e){
                    e.printStackTrace();
                }
                mCallback.setPairingDevices();
            }
        });
        return view;

    }
    class ViewHolder{
        TextView deviceName;
        TextView deviceAddress;
        Button bt_cancel;
    }
}
