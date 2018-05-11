package com.way;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.way.mylock.Record;
import com.way.pattern.R;
import com.way.sqlite.DBManager;

import java.util.ArrayList;

public class MyRecordFragment extends Fragment {
    private ListView recordListView;
    private Button btClear;
    private RecordArrayAdapter recordArrayAdapter;
    private ArrayList<Record> records;
    private DBManager dbManager;
    private ImageView delete_image;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.w("onCreateView:","-----!");
        View view =inflater.inflate(R.layout.myrecord_layout,container,false);
        recordListView=(ListView)view.findViewById(R.id.open_record_listView);
       // btClear=(Button)view.findViewById(R.id.bt_clear_openRecord);
        delete_image=(ImageView)view.findViewById(R.id.image_delete);
        dbManager=new DBManager(getActivity());
        records=dbManager.searchRecord();
        recordArrayAdapter =new RecordArrayAdapter(getActivity(),records);
        recordListView.setAdapter(recordArrayAdapter);
        delete_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(),"已清除",Toast.LENGTH_SHORT).show();
                dbManager.clearRecord();
                records.clear();
                records.addAll(dbManager.searchRecord());
                recordArrayAdapter.notifyDataSetChanged();

            }
        });
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        Log.w("onAttach:","-----");
        super.onAttach(activity);
    }

    @Override
    public void onPause() {
        Log.w("onPause:", "-----");
        super.onPause();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.w("onHiddenChanged:","-----");
        records.clear();
        records.addAll(dbManager.searchRecord());
        recordArrayAdapter.notifyDataSetChanged();
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onResume() {
        Log.w("onResume:","-----");
        super.onResume();
        recordArrayAdapter.notifyDataSetChanged();
    }
   //自定义的数组适配器
        private class RecordArrayAdapter extends BaseAdapter {
        private ArrayList<Record> records;
        private LayoutInflater inflater;
        public RecordArrayAdapter(Activity activity,ArrayList<Record> records){
            inflater=activity.getLayoutInflater();
            this.records=records;
        }

        @Override
        public int getCount() {
            return records.size();
        }

        @Override
        public Object getItem(int i) {
            return records.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if(view ==null){
                viewHolder=new ViewHolder();
                view =inflater.inflate(R.layout.record_item_layout,null);
                viewHolder.time=(TextView)view.findViewById(R.id.record_time);
                viewHolder.visitor=(TextView)view.findViewById(R.id.record_visitor);
                viewHolder.name=(TextView)view.findViewById(R.id.record_name);
                view.setTag(viewHolder);
            }else{
                viewHolder=(ViewHolder)view.getTag();
            }
            final  Record record =records.get(i);
            final String timeString =record.getTime();
            final String nameString =record.getName();
            final String visitorString =record.getVisitor();
            viewHolder.time.setText(timeString);
            viewHolder.name.setText(nameString);
            viewHolder.visitor.setText(visitorString);
            return view;
        }
        class ViewHolder{
            TextView time;
            TextView name;
            TextView visitor;
        }
    }
}

