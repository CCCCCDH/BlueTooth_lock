package com.way.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "bluetooth.db";
    public static final String DB_TABLE_NAME = "devices";
    public static final String DB_TABLE_NAME_RECORD="records";
    private static final int DB_VERSION=1;
    public DBHelper(Context context){
        //Context context, String name, CursorFactory factory, int version
        //factory输入null,使用默认值
        super(context, DB_NAME, null, DB_VERSION);
    }

    //数据第一次创建的时候会调用onCreate
    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建表
        db.execSQL("CREATE TABLE IF NOT EXISTS devices" +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR, address STRING)");

        db.execSQL("CREATE TABLE IF NOT EXISTS records" +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, visitor VARCHAR,name STRING, time STRING)");
        Log.i("SQL", "------create table");
    }

    //数据库第一次创建时onCreate方法会被调用，我们可以执行创建表的语句，当系统发现版本变化之后，会调用onUpgrade方法，我们可以执行修改表结构等语句
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //在表info中增加一列other
        //db.execSQL("ALTER TABLE info ADD COLUMN other STRING");
        Log.i("WIRELESSQA", "update sqlite "+oldVersion+"---->"+newVersion);
    }
}
