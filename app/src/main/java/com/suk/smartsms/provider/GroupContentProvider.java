package com.suk.smartsms.provider;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.suk.smartsms.db.GroupSqliteOpenHelper;

public class GroupContentProvider extends ContentProvider {

    GroupSqliteOpenHelper mOpenHelper;
    static final String AUTHORITY = "com.itheima.groupprovider";
    static final Uri NOTIFY_URI = Uri.parse("content://aa.bb.cc");
    static UriMatcher um = new UriMatcher(UriMatcher.NO_MATCH);
    static final int GROUP_CODE = 0;
    static final int GROUP_ID_CODE = 1;
    static final int THREAD_GROUP_CODE = 2;

    static {
        um.addURI(AUTHORITY, "groups", GROUP_CODE);
        um.addURI(AUTHORITY, "groups/#", GROUP_ID_CODE);
        um.addURI(AUTHORITY, "thread_group", THREAD_GROUP_CODE);
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = GroupSqliteOpenHelper.getInstance(getContext());
        return false;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (um.match(uri)) {
            case GROUP_CODE:
                Cursor cursor = db.query("groups", projection, selection, selectionArgs, null, null, sortOrder);
                //对cursor做设置，使其可以收到内容提供者发出的通知
                cursor.setNotificationUri(getContext().getContentResolver(), NOTIFY_URI);
                return cursor;
            case THREAD_GROUP_CODE:
                cursor = db.query("thread_group", projection, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), NOTIFY_URI);
                return cursor;
            default:
                throw new IllegalArgumentException("无效的uri：" + uri);
        }
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (um.match(uri)) {
            case GROUP_CODE:
                long id = db.insert("groups", null, values);
                getContext().getContentResolver().notifyChange(NOTIFY_URI, null);
                return ContentUris.withAppendedId(uri, id);
            case THREAD_GROUP_CODE:
                id = db.insert("thread_group", null, values);
                getContext().getContentResolver().notifyChange(NOTIFY_URI, null);
                return ContentUris.withAppendedId(uri, id);
            default:
                throw new IllegalArgumentException("无效的uri：" + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        switch (um.match(uri)) {
            case GROUP_ID_CODE:
                long id = ContentUris.parseId(uri);
                SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                int number = db.delete("groups", "_id = ?", new String[]{id + ""});
                db.delete("thread_group", "group_id = ?", new String[]{id + ""});
                getContext().getContentResolver().notifyChange(NOTIFY_URI, null);
                return number;
            default:
                throw new IllegalArgumentException("无效的uri：" + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        switch (um.match(uri)) {
            case GROUP_CODE:
                SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                int number = db.update("groups", values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(NOTIFY_URI, null);
                return number;
            default:
                throw new IllegalArgumentException("无效的uri：" + uri);
        }
    }

}
