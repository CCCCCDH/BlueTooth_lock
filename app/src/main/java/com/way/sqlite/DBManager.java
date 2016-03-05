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

/**
 *DBManager是建立在DBHelper之上，封装了常用的业务方法
 * @author bixiaopeng 2013-2-16 下午3:06:26
 */
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
        switch (i){
            case 1:
                Log.w("insert record:", "-----主人");
                SharedPreferences sp1 =context.getSharedPreferences("user_information",Context.MODE_PRIVATE);
                //获取当前的时间
                SimpleDateFormat formatter1    =   new    SimpleDateFormat    ("yyyy年MM月dd日 HH:mm:ss");
                Date curDate1    =   new    Date(System.currentTimeMillis());//获取当前时间
                String    str1    =    formatter1.format(curDate1);

                Log.w("insertRecord:","-----time:"+str1);
                ContentValues cv1 = new ContentValues();
                cv1.put("visitor","主人");
                cv1.put("name", sp1.getString("name","未填写"));
                cv1.put("time",str1 );
                db.insert(DBHelper.DB_TABLE_NAME_RECORD,null,cv1);
                break;
            case 2:
                Log.w("insert record:", "-----常驻人员");
                SharedPreferences sp2 =context.getSharedPreferences("user_information",Context.MODE_PRIVATE);
                //获取当前的时间
                SimpleDateFormat formatter2    =   new   SimpleDateFormat    ("yyyy年MM月dd日 HH:mm:ss");
                Date curDate2    =   new    Date(System.currentTimeMillis());//获取当前时间
                String    str2    =    formatter2.format(curDate2);

                Log.w("insertRecord:","-----time:"+str2);
                ContentValues cv2 = new ContentValues();
                cv2.put("visitor","常驻人员");
                cv2.put("name", sp2.getString("name","未填写"));
                cv2.put("time",str2 );
                db.insert(DBHelper.DB_TABLE_NAME_RECORD,null,cv2);
                break;
            case 3:
                Log.w("insert record:", "-----访客");
                SharedPreferences sp3 =context.getSharedPreferences("user_information",Context.MODE_PRIVATE);
                //获取当前的时间
                SimpleDateFormat formatter3    =   new    SimpleDateFormat    ("yyyy年MM月dd日 HH:mm:ss");
                Date curDate3    =   new    Date(System.currentTimeMillis());//获取当前时间
                String    str3    =    formatter3.format(curDate3);

                Log.w("insertRecord:","-----time:"+str3);
                ContentValues cv3 = new ContentValues();
                cv3.put("visitor","访客");
                cv3.put("name", sp3.getString("name","未填写"));
                cv3.put("time",str3 );
                db.insert(DBHelper.DB_TABLE_NAME_RECORD,null,cv3);
                break;
        }
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

    /**
     * 执行SQL，返回一个游标
     *
     * @param sql
     * @return
     */
    private Cursor ExecSQLForCursor(String sql) {
        Cursor c = db.rawQuery(sql, null);
        return c;
    }


}
