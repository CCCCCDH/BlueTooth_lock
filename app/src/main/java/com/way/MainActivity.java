package com.way;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.way.pattern.R;

/**
 * Created by wise on 2015/10/7.
 */
public class MainActivity extends Activity implements View.OnClickListener{

    //找到三个tab
    private MyRecordFragment myRecordFragment;
    private UserCenterFragment userCenterFragment;
    private MyLockFragment myLockFragment;

    //底部的三个按钮

    private LinearLayout mTabBt_mylock;
    private LinearLayout mTabBt_myrecord;
    private LinearLayout mTabBt_user;

    //用于对fragment进行管理

    private FragmentManager fragmentManager;

    private long exitTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_activity_layout);


        fragmentManager=getFragmentManager();
        //实例化控件

        mTabBt_mylock=(LinearLayout)findViewById(R.id.id_tab_bottom_lock);
        mTabBt_myrecord=(LinearLayout)findViewById(R.id.id_tab_bottom_record);
        mTabBt_user=(LinearLayout)findViewById(R.id.id_tab_bottom_user);

        mTabBt_mylock.setOnClickListener(this);
        mTabBt_myrecord.setOnClickListener(this);
        mTabBt_user.setOnClickListener(this);

        //设置初试的tab为0
        setTabSelection(0);

//        Intent intent0=getIntent();
//        Beacon beacon=(Beacon)intent0.getSerializableExtra("Beacon");



    }

    //设置点击事件

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.id_tab_bottom_lock:
                setTabSelection(0);
                break;
            case R.id.id_tab_bottom_record:
                setTabSelection(1);
                break;
            case R.id.id_tab_bottom_user:
                setTabSelection(2);
                break;
        }
    }

    private void setTabSelection(int index) {
        //重置所有按钮
        resetBtn();
        // 开启一个Fragment事务
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
        hideFragments(transaction);

        switch (index){
            case 0:
                //当点击了tab 按钮之后，改变图片和文字的颜色
                ((ImageButton) mTabBt_mylock.findViewById(R.id.btn_tab_bottom_lock))
                        .setImageResource(R.drawable.ic_lock_focused);
                if(myLockFragment==null) {
                    Log.w("myLockFragment:","   ==null");
                    // 如果MessageFragment为空，则创建一个并添加到界面上
                    myLockFragment = new MyLockFragment();
                    transaction.add(R.id.id_content, myLockFragment);
                }
                    else{//如果不为空，直接显示
                    Log.w("myLockFragment:","   Not null");
                    transaction.show(myLockFragment);
                    }
                break;

            case 1:
                ((ImageButton) mTabBt_myrecord.findViewById(R.id.btn_tab_bottom_record))
                        .setImageResource(R.drawable.ic_search_focused1);
                if(myRecordFragment==null) {
                    Log.w("myRecordFragment:","   ==null");
                    // 如果MessageFragment为空，则创建一个并添加到界面上
                    myRecordFragment = new MyRecordFragment();
                    transaction.add(R.id.id_content, myRecordFragment);
                }
                else{//如果不为空，直接显示
                    Log.w("myRecordFragment:","   Not null");
                    transaction.show(myRecordFragment);
                }
                break;

            case 2:
                ((ImageButton) mTabBt_user.findViewById(R.id.btn_tab_bottom_user))
                        .setImageResource(R.drawable.ic_settings_focused);
                if(userCenterFragment==null) {
                    Log.w("myCenterFragment:","   ==null");
                    // 如果MessageFragment为空，则创建一个并添加到界面上
                    userCenterFragment = new UserCenterFragment();
                    transaction.add(R.id.id_content, userCenterFragment);
                }
                else{//如果不为空，直接显示
                    Log.w("myCenterFragment:","    Not null");
                    transaction.show(userCenterFragment);
                }
                break;

                }
       // transaction.addToBackStack(null);
        transaction.commit();
        }


    /**
     * 将所有的Fragment都置为隐藏状态。
     *
     * @param transaction
     *            用于对Fragment执行操作的事务
     */
    private void hideFragments(FragmentTransaction transaction) {
        if (myLockFragment != null)
        {
            transaction.hide(myLockFragment);
        }

        if (myRecordFragment != null)
        {
            transaction.hide(myRecordFragment);
        }

        if (userCenterFragment!= null)
        {
            transaction.hide(userCenterFragment);
        }
    }


    //重置按钮的目的是清除按钮的选中状态
    private void resetBtn() {
        ((ImageButton) mTabBt_mylock.findViewById(R.id.btn_tab_bottom_lock))
                .setImageResource(R.drawable.ic_lock_normal);
        ((ImageButton) mTabBt_myrecord.findViewById(R.id.btn_tab_bottom_record))
                .setImageResource(R.drawable.ic_search_normal);
        ((ImageButton) mTabBt_user.findViewById(R.id.btn_tab_bottom_user))
                .setImageResource(R.drawable.ic_settings_normal);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
    public void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
            System.exit(0);
        }
    }

    }

