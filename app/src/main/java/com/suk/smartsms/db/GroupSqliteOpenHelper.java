package com.suk.smartsms.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * GroupSQLiteOpenHelper
 *
 * @author Administrator
 */
public class GroupSqliteOpenHelper extends SQLiteOpenHelper {

    private static GroupSqliteOpenHelper mInstance = null;
    private static final String DB_NAME = "itheima.db";
    private static final int DB_VERSION = 1;

    public static GroupSqliteOpenHelper getInstance(Context context) {
        if (mInstance == null) {
            synchronized (GroupSqliteOpenHelper.class) {
                if (mInstance == null) {
                    mInstance = new GroupSqliteOpenHelper(context, DB_NAME, null, DB_VERSION);
                }
            }
        }
        return mInstance;
    }

    private GroupSqliteOpenHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /**
     * 创建表
     *
     * @param db SQLiteDatabase
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table groups(_id integer primary key autoincrement, name varchar(20))");
        db.execSQL("create table thread_group(_id integer primary key autoincrement,thread_id integer,group_id integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
