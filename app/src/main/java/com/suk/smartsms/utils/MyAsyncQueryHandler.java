package com.suk.smartsms.utils;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.CursorAdapter;

/**
 * 异步查询处理器
 *
 * @author Administrator
 */
public class MyAsyncQueryHandler extends AsyncQueryHandler {

    private NotifyAdapterListener listener;

    public MyAsyncQueryHandler(ContentResolver cr) {
        super(cr);
    }

    public void setOnNotifyAdapterListner(NotifyAdapterListener listner) {
        this.listener = listner;
    }

    /**
     * 当查询数据库完毕时,系统调用此方法,在主线程调用
     */
    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        super.onQueryComplete(token, cookie, cursor);
        if (listener != null) {
            listener.onPreNotify(token, cookie, cursor);
        }
        if (cookie instanceof CursorAdapter) {
            //将传过来的adapter赋值给新的adapter
            CursorAdapter mAdapter = (CursorAdapter) cookie;
            //把最新查询到的内容设置给cursoradapter
            mAdapter.changeCursor(cursor);
        } else {
            Utils.printCursor(cursor);
        }
        if (listener != null) {
            listener.onPostNotify(token, cookie, cursor);
        }

    }

    /**
     * 定义一个通知适配器监听的接口
     */
    public interface NotifyAdapterListener {
        /**
         * 通知前
         *
         * @param token  令牌
         * @param cookie 缓存
         * @param cursor Cursor
         */
        void onPreNotify(int token, Object cookie, Cursor cursor);

        /**
         * 通知
         *
         * @param token  令牌
         * @param cookie 缓存
         * @param cursor Cursor
         */
        void onPostNotify(int token, Object cookie, Cursor cursor);
    }

}
