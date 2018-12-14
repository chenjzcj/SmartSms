package com.suk.smartsms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.suk.smartsms.utils.MyAsyncQueryHandler;
import com.suk.smartsms.utils.Utils;

import java.util.HashMap;

public class FolerDetailUI extends Activity implements OnClickListener, MyAsyncQueryHandler.onNotifyAdapterListner {

    private int type;
    private FolderDetailCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_detail);
        Intent intent = getIntent();
        type = intent.getIntExtra("type", -1);

        initTitle();
        init();
    }

    private void init() {
        dateMap = new HashMap<Integer, String>();
        smsMap = new HashMap<Integer, Integer>();

        Button bt_folder_detail_newmsg = (Button) findViewById(R.id.bt_folder_detail_newmsg);
        bt_folder_detail_newmsg.setOnClickListener(this);

        ListView lv_folder_detail_list = (ListView) findViewById(R.id.lv_folder_detail_list);
        mAdapter = new FolderDetailCursorAdapter(this, null);
        lv_folder_detail_list.setAdapter(mAdapter);
        prepareData();
    }

    static final int COLUMN_INDEX_ID = 0;
    static final int COLUMN_INDEX_BODY = 1;
    static final int COLUMN_INDEX_DATE = 2;
    static final int COLUMN_INDEX_ADDRESS = 3;
    private HashMap<Integer, String> dateMap;
    private HashMap<Integer, Integer> smsMap;

    private void prepareData() {
        String[] projection = new String[]{
                "_id",
                "body",
                "date",
                "address"
        };
        MyAsyncQueryHandler mHandler = new MyAsyncQueryHandler(getContentResolver());
        mHandler.setOnNotifyAdapterListner(this);
        mHandler.startQuery(0, mAdapter, Utils.getTypeUri(type), projection, null, null, "date desc");
    }

    class FolderDetailCursorAdapter extends CursorAdapter {

        public FolderDetailCursorAdapter(Context context, Cursor c) {
            super(context, c);
            // TODO Auto-generated constructor stub
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return dateMap.size() + smsMap.size();
        }

        @Override
        protected void onContentChanged() {
            // TODO Auto-generated method stub
            super.onContentChanged();
            Cursor cursor = getCursor();
            //在根据Cursor的内容初始化两个map之前，先把cursor的指针复位
            cursor.moveToPosition(-1);
            dateMap.clear();
            smsMap.clear();
            prepareCursor(cursor);

        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (dateMap.containsKey(position)) {
                String date = dateMap.get(position);
                TextView tv = new TextView(FolerDetailUI.this);
                tv.setText(date);
                tv.setTextSize(18);
                tv.setGravity(Gravity.CENTER_HORIZONTAL);
                tv.setBackgroundResource(android.R.color.darker_gray);
                return tv;
            }
            int index = smsMap.get(position);
            Cursor cursor = getCursor();
            cursor.moveToPosition(index);

            View v;
            //如果缓存不存在，那么会去newView，如果缓存存在，就使用缓存
            //如果缓存存在，但是是一个textView，那么使用缓存的话，是取不到viewholder的
            if (convertView == null || convertView instanceof TextView) {
                v = newView(FolerDetailUI.this, cursor, parent);
            } else {
                v = convertView;
            }
            bindView(v, FolerDetailUI.this, cursor);
            return v;
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
            ViewHolder mHolder = (ViewHolder) view.getTag();

            String body = cursor.getString(COLUMN_INDEX_BODY);
            String address = cursor.getString(COLUMN_INDEX_ADDRESS);
            long date = cursor.getLong(COLUMN_INDEX_DATE);

            mHolder.tv_conversation_list_body.setText(body);
            String dateStr = null;
            if (DateUtils.isToday(date)) {
                dateStr = DateFormat.getTimeFormat(context).format(date);
            } else {
                dateStr = DateFormat.getDateFormat(context).format(date);
            }
            mHolder.tv_conversation_list_date.setText(dateStr);
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
                    //说明没存头像
                    mHolder.iv_conversation_list_photo.setImageResource(R.drawable.ic_contact_picture);
                } else {
                    //存的有头像
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

    private void initTitle() {
        switch (type) {
            case 0:
                setTitle("收件箱");
                break;
            case 1:
                setTitle("发件箱");
                break;
            case 2:
                setTitle("已发送");
                break;
            case 3:
                setTitle("草稿箱");
                break;

        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, NewMessageUI.class);
        startActivity(intent);

    }

    @Override
    public void onPreNotify(int token, Object cookie, Cursor cursor) {
        prepareCursor(cursor);

    }

    private void prepareCursor(Cursor cursor) {
        //对cursor做处理，分离出dateMap和smsMap两个集合
        int listViewIndex = 0;
        while (cursor.moveToNext()) {
            long date = cursor.getLong(COLUMN_INDEX_DATE);
            String dateStr = DateFormat.getDateFormat(this).format(date);
            //如果map里不包含这条短信的时间，就存进去
            if (!dateMap.containsValue(dateStr)) {
                dateMap.put(listViewIndex, dateStr);
                listViewIndex++;
            }
            smsMap.put(listViewIndex, cursor.getPosition());
            listViewIndex++;
        }
    }

    @Override
    public void onPostNotify(int token, Object cookie, Cursor cursor) {

    }

}
