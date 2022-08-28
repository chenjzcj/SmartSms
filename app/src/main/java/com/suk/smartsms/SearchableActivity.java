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
        ListView lvSearchList = getListView();
        mAdapter = new SearchCursorAdapter(this, null);
        lvSearchList.setAdapter(mAdapter);
    }

    class SearchCursorAdapter extends CursorAdapter {

        public SearchCursorAdapter(Context context, Cursor c) {
            super(context, c);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            ViewHolder mHolder = new ViewHolder();
            View v = View.inflate(context, R.layout.item_conversation_list, null);
            mHolder.ivConversationListPhoto = v.findViewById(R.id.iv_conversation_list_photo);
            mHolder.tvConversationListName = v.findViewById(R.id.tv_conversation_list_name);
            mHolder.tvConversationListDate = v.findViewById(R.id.tv_conversation_list_date);
            mHolder.tvConversationListBody = v.findViewById(R.id.tv_conversation_list_body);
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
            mHolder.tvConversationListBody.setText(body);
            //设置会话条目的时间
            String dateStr;
            if (DateUtils.isToday(date)) {
                dateStr = DateFormat.getTimeFormat(context).format(date);
            } else {
                dateStr = DateFormat.getDateFormat(context).format(date);
            }
            mHolder.tvConversationListDate.setText(dateStr);

            //设置会话列表的姓名
            String name = Utils.getContactNameByAddress(address, getContentResolver());
            if (TextUtils.isEmpty(name)) {
                //陌生人
                mHolder.tvConversationListName.setText(address);
                mHolder.ivConversationListPhoto.setImageResource(R.drawable.ic_unknow_contact_picture);
            } else {
                //熟人
                mHolder.tvConversationListName.setText(name);
                Bitmap bm = Utils.getContactPhotoByAddress(address, getContentResolver());
                if (bm == null) {
                    mHolder.ivConversationListPhoto.setImageResource(R.drawable.ic_contact_picture);
                } else {
                    mHolder.ivConversationListPhoto.setImageBitmap(bm);
                }
            }
        }

        class ViewHolder {
            ImageView ivConversationListPhoto;
            TextView tvConversationListName;
            TextView tvConversationListDate;
            TextView tvConversationListBody;
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
