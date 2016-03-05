package com.way.usercenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.way.pattern.R;

/**
 * Created by wise on 2015/10/8.
 */
public class MyInformation extends Activity{
    private TextView textView_name;
    private TextView textView_phone;
    private TextView textView_address;
    private TextView textView_userClass;
    private Button bt_Edit;
    private String name ;
    private String phone;
    private String address;
    private String userClass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.userenter_myinformation);

        //初始化控件
        textView_name=(TextView)findViewById(R.id.userCenter_information_name);
        textView_phone=(TextView)findViewById(R.id.userCenter_information_phone);
        textView_address=(TextView)findViewById(R.id.userCenter_information_address);
        textView_userClass=(TextView)findViewById(R.id.userCenter_information_userClass);
        bt_Edit=(Button)findViewById(R.id.userCenter_information_btEdit);
        //为编辑按钮添加点击事件
        bt_Edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(MyInformation.this,MyInformationEdit.class);
                startActivityForResult(intent,0);
            }
        });

        //从sharedPreferences中读取数据
        SharedPreferences sp=getSharedPreferences("user_information", MODE_PRIVATE);
        name=sp.getString("name","");
        phone=sp.getString("phone","");
        address=sp.getString("address","");
        userClass=sp.getString("class","");

        //添加至textview
        textView_name.setText(name);
        textView_phone.setText(phone);
        textView_address.setText(address);
        textView_userClass.setText(userClass);
    }
        //接受从Edit活动中传递回来的数据
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(resultCode==2&&requestCode==0){
            Bundle data =intent.getExtras();
            textView_name.setText(data.getString("name"));
            textView_phone.setText(data.getString("phone"));
            textView_address.setText(data.getString("address"));
            textView_userClass.setText(data.getString("class"));
        }
    }
}
