package com.suk.smartsms.provider;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.suk.smartsms.utils.SmsUri;

/**
 * @author Administrator
 */
public class MySuggestionProvider extends SearchRecentSuggestionsProvider {

    public final static String AUTHORITY = "com.itheima.searchprovider";
    public final static int MODE = DATABASE_MODE_QUERIES | DATABASE_MODE_2LINES;

    public MySuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }

    private final String[] mProjection = new String[]{
            "_id",
            "address",
            "body"
    };
    private final String[] columnNames = new String[]{
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
            //系统会拿着跟这个字段对应的数据，作为query的参数跳转到SearchableActivity
            SearchManager.SUGGEST_COLUMN_QUERY
    };

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        //这个参数就是搜索框输入的内容
        Context context = getContext();
        if (context == null) {
            return null;
        }
        ContentResolver resolver = context.getContentResolver();
        if (resolver == null) {
            return null;
        }
        Cursor cursor = resolver.query(SmsUri.SMS_URI, mProjection, "body like ?", new String[]{"%" + selectionArgs[0] + "%"}, null);
        if (cursor == null) {
            return null;
        }
        MatrixCursor mc = new MatrixCursor(columnNames);
        try {
            //把cursor的内容复制到mc对象里
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String[] columnValues = new String[columnNames.length];
                    columnValues[0] = cursor.getString(0);
                    columnValues[1] = cursor.getString(1);
                    columnValues[2] = cursor.getString(2);
                    columnValues[3] = cursor.getString(2);
                    mc.addRow(columnValues);
                }
            }
        } finally {
            cursor.close();
        }
        return mc;
    }
}
