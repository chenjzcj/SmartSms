package com.suk.smartsms.utils;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsManager;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * @author Administrator
 */
public class Utils {

    /**
     * 打印光标
     *
     * @param cursor 光标
     */
    public static void printCursor(Cursor cursor) {
        while (cursor.moveToNext()) {
            int count = cursor.getColumnCount();
            for (int i = 0; i < count; i++) {
                System.out.println(cursor.getPosition() + "行：" + cursor.getColumnName(i) + ":" + cursor.getString(i));
            }
        }
    }

    /**
     * 根据号码获取联系人姓名
     */
    public static String getContactNameByAddress(String address, ContentResolver cr) {
        Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
        Cursor cursor = cr.query(lookupUri, new String[]{"display_name"}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String name = null;
        try {
            if (cursor.moveToFirst()) {
                name = cursor.getString(0);
            }
        } finally {
            cursor.close();
        }

        return name;

    }

    /**
     * 根据号码获取头像
     */
    public static Bitmap getContactPhotoByAddress(String address, ContentResolver cr) {
        Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
        Cursor cursor = cr.query(lookupUri, new String[]{"_id"}, null, null, null);
        if (cursor == null) {
            return null;
        }
        Bitmap bm;
        try {
            int id = 0;
            if (cursor.moveToFirst()) {
                id = cursor.getInt(0);
            }

            Uri uri = ContentUris.withAppendedId(Contacts.CONTENT_URI, id);
            InputStream is = Contacts.openContactPhotoInputStream(cr, uri);
            bm = BitmapFactory.decodeStream(is);
        } finally {
            cursor.close();
        }

        return bm;
    }

    /**
     * 发送短信
     */
    public static void sendMessage(Context context, String body, String address) {
        SmsManager sm = SmsManager.getDefault();
        ArrayList<String> smss = sm.divideMessage(body);
        // 定义广播需要的action
        Intent intent = new Intent("com.itheima.sendsms");
        // 生成作为广播的pendingtent
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 10, intent, PendingIntent.FLAG_ONE_SHOT);
        for (String text : smss) {
            sm.sendTextMessage(address, null, text, pIntent, null);
        }
        // 把发送的短信存入数据库
        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put("body", body);
        values.put("address", address);
        values.put("type", 2);
        cr.insert(SmsUri.SMS_URI, values);
    }

    public static int getContactInfoByUri(Uri uri, ContentResolver cr) {
        Cursor cursor = cr.query(uri, new String[]{"_id", "has_phone_number"}, null, null, null);
        if (cursor == null) {
            return -1;
        }
        try {
            cursor.moveToFirst();
            int hasPhone = cursor.getInt(1);
            if (hasPhone == 1) {
                return cursor.getInt(0);
            } else {
                return -1;
            }
        } finally {
            cursor.close();
        }
    }

    /**
     * 通过联系人的id获取号码
     */
    public static String getContactAddressById(int id, ContentResolver cr) {
        Cursor cursor = cr.query(Phone.CONTENT_URI, new String[]{"data1"}, "contact_id = ?", new String[]{id + ""}, null);
        if (cursor == null) {
            return null;
        }
        try {
            cursor.moveToFirst();
            return cursor.getString(0);
        } finally {
            cursor.close();
        }
    }

    public static Uri getTypeUri(int index) {
        switch (index) {
            case 0:
                return SmsUri.SMS_INBOX_URI;
            case 1:
                return SmsUri.SMS_OUTBOX_URI;
            case 2:
                return SmsUri.SMS_SENT_URI;
            case 3:
                return SmsUri.SMS_DRAFT_URI;
            default:
                break;

        }
        return null;
    }
}
