package com.way.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.way.MyRecordFragment;
import com.way.mylock.Beacon;
import com.way.mylock.Record;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DBManager {
    private DBHelper dBhelper;
    private SQLiteDatabase db;
    private Context context;
    public DBManager(Context context){
        this.context=context;
        dBhelper=new DBHelper(context);
        db=dBhelper.getReadableDatabase();
    }

    //返回表里的所有数据
    public ArrayList<Beacon> searchAllData(){
        Log.w("searchAllData","------!!");
        String sql = "SELECT * FROM devices";
        return ExecSQLForAllDevices(sql);
    }
    private ArrayList<Beacon> ExecSQLForAllDevices(String sql) {
        ArrayList<Beacon> list=new ArrayList<Beacon>();
        Cursor c = ExecSQLForCursor(sql);
        while (c.moveToNext()){
            Beacon beacon =new Beacon( c.getString(c.getColumnIndex("name")),
                    c.getString(c.getColumnIndex("address")));
            Log.w("ExecSQlFor",beacon.getName()+"--"+beacon.getAddress());
            list.add(beacon);
        }
        c.close();
        return list;
    }


    public ArrayList<com.way.mylock.Record>searchRecord(){
        Log.w("searchAllRecord","------!!");
        String sql = "SELECT * FROM records";
        ArrayList<com.way.mylock.Record>records =new ArrayList<Record>();
        Cursor c = ExecSQLForCursor(sql);
        while (c.moveToNext()){
            Record record =new Record(c.getString(c.getColumnIndex("visitor")),
                    c.getString(c.getColumnIndex("name")), c.getString(c.getColumnIndex("time")));

            records.add(record);
        }
        c.close();
        return records;
    }

    //根据设备地址检查数据库中的是否已经添加
    public boolean isAdded(String address){
       Cursor c= db.query(DBHelper.DB_TABLE_NAME, null, null, null, null, null, null);
        if(c.moveToFirst()){
          do{
              String addressInDB=c.getString(c.getColumnIndex("address"));
              if(address.equals(addressInDB)){
                  return true;
              }
          }while(c.moveToNext());
       }
        return false;
    }
    //更新名字
    public void updataName(String NewName,String OldName){
        ContentValues values =new ContentValues();
        values.put("name",NewName);
        db.update(DBHelper.DB_TABLE_NAME,values,"name=?",new String[]{OldName});
    }

    //根据设备的蓝牙地址删除一台设备
    public void deleteDevice(String address){
        db.delete(DBHelper.DB_TABLE_NAME,"address=?",new String[]{address});
    }
    //添加一条信息
    public void add(String name ,String address){
        Log.i("SQL","------add data ------");
        ContentValues cv = new ContentValues();
        cv.put("name",name);
        cv.put("address",address);
        db.insert(DBHelper.DB_TABLE_NAME, null, cv);
        Log.i("SQL add data", name +  "/" + address );
    }

    public void insertRecord(int i){

        SharedPreferences sp1 =context.getSharedPreferences("user_information",Context.MODE_PRIVATE);
        //获取当前的时间
        SimpleDateFormat formatter1    =   new    SimpleDateFormat    ("yyyy年MM月dd日 HH:mm:ss");
        Date curDate1    =   new    Date(System.currentTimeMillis());//获取当前时间
        String    str    =    formatter1.format(curDate1);

        Log.w("insertRecord:","-----time:"+str);
        ContentValues cv1 = new ContentValues();
        switch (i){
            case 1:
                cv1.put("visitor","主人");
                break;
            case 2:
                cv1.put("visitor","住户");
                break;
            case 3:
                cv1.put("visitor","访客");
                break;

        }
        cv1.put("name", sp1.getString("name","未填写"));
        cv1.put("time",str );
        db.insert(DBHelper.DB_TABLE_NAME_RECORD,null,cv1);
    }
    public void closeDB() {

        db.close();
    }

    //清除数据库

    public void clearRecord(){
        db.delete("records",null,null);

    }

    private void ExecSQL(String sql) {
        try {
            db.execSQL(sql);
            Log.i("execSql: ", sql);
        } catch (Exception e) {
            Log.e("ExecSQL Exception", e.getMessage());
            e.printStackTrace();
        }
    }

    private Cursor ExecSQLForCursor(String sql) {
        Cursor c = db.rawQuery(sql, null);
        return c;
    }
}
