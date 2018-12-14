package com.suk.smartsms.utils;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.CursorAdapter;

//异步查询处理器
public class MyAsyncQueryHandler extends AsyncQueryHandler {

    private onNotifyAdapterListner listener;

    public MyAsyncQueryHandler(ContentResolver cr) {
        super(cr);
    }

    public void setOnNotifyAdapterListner(onNotifyAdapterListner listner) {
        this.listener = listner;
    }

    //当查询数据库完毕时,系统调用此方法,在主线程调用
    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        super.onQueryComplete(token, cookie, cursor);
        if (listener != null) {
            listener.onPreNotify(token, cookie, cursor);
        }
        if (cookie != null && cookie instanceof CursorAdapter) {
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

    //定义一个通知适配器监听的接口
    public interface onNotifyAdapterListner {
        void onPreNotify(int token, Object cookie, Cursor cursor);

        void onPostNotify(int token, Object cookie, Cursor cursor);
    }

}
