package com.way;

import android.app.ListFragment;



import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.way.pattern.CreateGesturePasswordActivity;
import com.way.pattern.R;
import com.way.usercenter.MyInformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wise on 2015/10/7.
 */
public class UserCenterFragment extends ListFragment {

    private String TAG=UserCenterFragment.class.getName();
    private SimpleAdapter mAdapter;

    private String[] list_content=new String[]{"我的信息","修改图形锁","帮助"};
    private int[] imageID=new int[]{R.drawable.ic_usercenter_user,R.drawable.ic_usercenter_lock,R.drawable.ic_usercenter_help};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.user_layout,container,false);

        Log.i("TAG","----------onCreateView");
        return view;

    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //创建Map
        List<Map<String,Object>> listItems =new ArrayList<Map<String, Object>>();


        //往list_item里加入要素
        for(int i=0;i<list_content.length;i++){
            Map<String ,Object> listItem=new HashMap<String, Object>();
            listItem.put("image",imageID[i]);
            listItem.put("list_content",list_content[i]);
            //把每一个listitem加入到listitems里面
          listItems.add(listItem);
        }

        //创建一个simpleAdapter
        mAdapter =new SimpleAdapter(getActivity(),listItems,R.layout.usercenter_list_item,
                new String[]{"image","list_content"},new int[]{R.id.usercenter_listItem_imageView,R.id.usercenter_listItem_textView});

        setListAdapter(mAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        switch (position){
            case 0:
                Intent intent0 =new Intent(this.getActivity(), MyInformation.class);
                startActivity(intent0);
                break;
            case 1:
                Intent intent1 =new Intent(this.getActivity(), CreateGesturePasswordActivity.class);
                startActivity(intent1);
                break;
            case 2:
                Log.e("TAG","this is the NO.3");
                break;
        }

    }
}
