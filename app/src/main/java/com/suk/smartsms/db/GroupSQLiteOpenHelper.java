package com.suk.smartsms.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class GroupSQLiteOpenHelper extends SQLiteOpenHelper {

    private static GroupSQLiteOpenHelper mInstance = null;
    private static String DB_NAME = "itheima.db";
    private static int DB_VERSION = 1;

    public static GroupSQLiteOpenHelper getInstance(Context context) {
        if (mInstance == null) {
            synchronized (GroupSQLiteOpenHelper.class) {
                if (mInstance == null)
                    mInstance = new GroupSQLiteOpenHelper(context, DB_NAME, null, DB_VERSION);
            }
        }
        return mInstance;
    }

    private GroupSQLiteOpenHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建表
        db.execSQL("create table groups(_id integer primary key autoincrement, name varchar(20))");
        db.execSQL("create table thread_group(_id integer primary key autoincrement,thread_id integer,group_id integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
