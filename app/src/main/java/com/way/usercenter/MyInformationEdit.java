package com.way.usercenter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.way.pattern.R;

/**
 * Created by wise on 2015/10/8.
 */
public class MyInformationEdit extends Activity {
    private EditText name;
    private EditText phone;
    private EditText address;
    private RadioGroup user_class;
    private String string_userClass;
    private Button bt_finish;
    private RadioButton radioButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.userenter_myinformation_edit);

        //实例化

        name=(EditText)findViewById(R.id.userCenter_informationEdit_name);
        phone=(EditText)findViewById(R.id.userCenter_informationEdit_phone);
        address=(EditText)findViewById(R.id.userCenter_informationEdit_address);
        user_class=(RadioGroup)findViewById(R.id.rg);
        bt_finish=(Button)findViewById(R.id.userCenter_information_btFinish);
        user_class.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.class_host:
                        string_userClass = "主人";
                        break;
                    case R.id.class_live:
                        string_userClass = "常驻者";
                        break;
                    case R.id.class_visitor:
                        string_userClass = "访客";
                        break;
                }
            }
        });

       //设置上次编写的内容
        setContent();


        bt_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取sharedpreferences
                SharedPreferences sp = MyInformationEdit.this.getSharedPreferences("user_information", MODE_PRIVATE);
                //存入数据
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("name", name.getText().toString());
                editor.putString("phone", phone.getText().toString());
                editor.putString("address", address.getText().toString());
                editor.putString("class", string_userClass);
                editor.commit();
                //将填写的数据返回到information活动里
                Intent intent =getIntent();
                intent.putExtra("name", name.getText().toString());
                intent.putExtra("phone",phone.getText().toString());
                intent.putExtra("address",address.getText().toString());
                intent.putExtra("class",string_userClass);

                MyInformationEdit.this.setResult(2, intent);
                MyInformationEdit.this.finish();
            }
        });


    }

    private void setContent() {
        int id=0;
        SharedPreferences sp =getSharedPreferences("user_information", MODE_PRIVATE);
        String name0 =sp.getString("name", "1");
        String  phone0=sp.getString("phone","1");
        String address0=sp.getString("address","1");
        String userClass0=sp.getString("class","1");
        if(!name0.equals("1"))
          name.setText(name0);
        if(!phone0.equals("1"))
            phone.setText(phone0);
        if(!address0.equals("1"))
            address.setText(address0);
        if(!userClass0.equals("1")){
            if(userClass0.equals("主人"))
                id=1;
            else if(userClass0.equals("常驻者"))
                id=2;
            else if(userClass0.equals("访客"))
                    id=3;

            switch (id){
                case 1 :
                    radioButton=(RadioButton)this.findViewById(R.id.class_host);
                    break;
                case 2 :
                    radioButton=(RadioButton)this.findViewById(R.id.class_live);
                    break;
                case 3 :
                    radioButton=(RadioButton)this.findViewById(R.id.class_visitor);
                    break;
            }
            if(id!=0)
               radioButton.setChecked(true);
        }

    }
}
