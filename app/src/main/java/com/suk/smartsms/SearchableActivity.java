package com.suk.smartsms;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.suk.smartsms.utils.MyAsyncQueryHandler;
import com.suk.smartsms.utils.SmsUri;
import com.suk.smartsms.utils.Utils;


public class SearchableActivity extends ListActivity {

    private SearchCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        init();
        //获取到启动这个activity的intent
        Intent intent = getIntent();
        //判断获取到的intent的action是不是为ACTION_SEARCH
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            //取出搜索框里输入的数据
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }
    }

    private void init() {
        ListView lv_search_list = getListView();
        mAdapter = new SearchCursorAdapter(this, null);
        lv_search_list.setAdapter(mAdapter);

    }

    class SearchCursorAdapter extends CursorAdapter {

        public SearchCursorAdapter(Context context, Cursor c) {
            super(context, c);
            // TODO Auto-generated constructor stub
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            ViewHolder mHolder = new ViewHolder();
            View v = View.inflate(context, R.layout.item_conversation_list, null);
            mHolder.iv_conversation_list_photo = (ImageView) v.findViewById(R.id.iv_conversation_list_photo);
            mHolder.tv_conversation_list_name = (TextView) v.findViewById(R.id.tv_conversation_list_name);
            mHolder.tv_conversation_list_date = (TextView) v.findViewById(R.id.tv_conversation_list_date);
            mHolder.tv_conversation_list_body = (TextView) v.findViewById(R.id.tv_conversation_list_body);
            v.setTag(mHolder);
            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            String address = cursor.getString(COLUMN_INDEX_ADDRESS);
            long date = cursor.getLong(COLUMN_INDEX_DATE);
            String body = cursor.getString(COLUMN_INDEX_BODY);

            ViewHolder mHolder = (ViewHolder) view.getTag();


            //设置会话条目的body
            mHolder.tv_conversation_list_body.setText(body);

            //设置会话条目的时间
            String dateStr = null;
            if (DateUtils.isToday(date)) {
                dateStr = DateFormat.getTimeFormat(context).format(date);
            } else {
                dateStr = DateFormat.getDateFormat(context).format(date);
            }
            mHolder.tv_conversation_list_date.setText(dateStr);

            //设置会话列表的姓名
            String name = Utils.getContactNameByAddress(address, getContentResolver());
            if (TextUtils.isEmpty(name)) {
                //陌生人
                mHolder.tv_conversation_list_name.setText(address);
                mHolder.iv_conversation_list_photo.setImageResource(R.drawable.ic_unknow_contact_picture);
            } else {
                //熟人
                mHolder.tv_conversation_list_name.setText(name);
                Bitmap bm = Utils.getContactPhotoByAddress(address, getContentResolver());
                if (bm == null) {
                    mHolder.iv_conversation_list_photo.setImageResource(R.drawable.ic_contact_picture);
                } else {
                    mHolder.iv_conversation_list_photo.setImageBitmap(bm);
                }

            }

        }

        class ViewHolder {
            ImageView iv_conversation_list_photo;
            TextView tv_conversation_list_name;
            TextView tv_conversation_list_date;
            TextView tv_conversation_list_body;
        }
    }

    static final int COLUMN_INDEX_ID = 0;
    static final int COLUMN_INDEX_BODY = 1;
    static final int COLUMN_INDEX_DATE = 2;
    static final int COLUMN_INDEX_ADDRESS = 3;

    private void doMySearch(String query) {
        String[] projection = new String[]{
                "_id",
                "body",
                "date",
                "address"
        };
        MyAsyncQueryHandler mHandler = new MyAsyncQueryHandler(getContentResolver());
        mHandler.startQuery(0, mAdapter, SmsUri.SMS_URI, projection, "body like ?", new String[]{"%" + query + "%"}, null);
    }
}
