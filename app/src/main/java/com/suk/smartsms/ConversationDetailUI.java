package com.suk.smartsms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.suk.smartsms.utils.MyAsyncQueryHandler;
import com.suk.smartsms.utils.SmsUri;
import com.suk.smartsms.utils.Utils;


public class ConversationDetailUI extends Activity implements OnClickListener, MyAsyncQueryHandler.onNotifyAdapterListner {

    private String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_detail);

        Intent intent = getIntent();
        address = intent.getStringExtra("address");
        initTitle();
        init();
    }

    static final int COLUMN_INDEX_ID = 0;
    static final int COLUMN_INDEX_BODY = 1;
    static final int COLUMN_INDEX_DATE = 2;
    static final int COLUMN_INDEX_TYPE = 3;
    private EditText et_conversation_detail_body;
    private ListView lv_conversation_detail_list;

    private void init() {
        Button bt_conversation_detail_back = (Button) findViewById(R.id.bt_conversation_detail_back);
        Button bt_conversation_detail_send = (Button) findViewById(R.id.bt_conversation_detail_send);
        bt_conversation_detail_back.setOnClickListener(this);
        bt_conversation_detail_send.setOnClickListener(this);

        et_conversation_detail_body = (EditText) findViewById(R.id.et_conversation_detail_body);

        String[] projection = new String[]{
                "_id",
                "body",
                "date",
                "type"
        };
        lv_conversation_detail_list = (ListView) findViewById(R.id.lv_conversation_detail_list);
        ConversationDetailCursorAdapter mAdapter = new ConversationDetailCursorAdapter(this, null);
        lv_conversation_detail_list.setAdapter(mAdapter);
        //查询会话里的短信
        MyAsyncQueryHandler mHandler = new MyAsyncQueryHandler(getContentResolver());
        mHandler.setOnNotifyAdapterListner(this);
        mHandler.startQuery(0, mAdapter, SmsUri.SMS_URI, projection, "address = ?", new String[]{address}, "date");
    }

    class ConversationDetailCursorAdapter extends CursorAdapter {

        public ConversationDetailCursorAdapter(Context context, Cursor c) {
            super(context, c);
            // TODO Auto-generated constructor stub
        }

        //cursor模型层数据改变了，此方法调用
        @Override
        protected void onContentChanged() {
            // TODO Auto-generated method stub
            super.onContentChanged();
            //listview滑动到最底部
            lv_conversation_detail_list.setSelection(getCount() - 1);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = View.inflate(context, R.layout.item_conversation_detail, null);
            ViewHolder mHolder = new ViewHolder();
            mHolder.tl_conversation_detail_receive = (TableLayout) v.findViewById(R.id.tl_conversation_detail_receive);
            mHolder.tv_conversation_detail_receive_body = (TextView) v.findViewById(R.id.tv_conversation_detail_receive_body);
            mHolder.tv_conversation_detail_receive_date = (TextView) v.findViewById(R.id.tv_conversation_detail_receive_date);
            mHolder.tl_conversation_detail_send = (TableLayout) v.findViewById(R.id.tl_conversation_detail_send);
            mHolder.tv_conversation_detail_send_body = (TextView) v.findViewById(R.id.tv_conversation_detail_send_body);
            mHolder.tv_conversation_detail_send_date = (TextView) v.findViewById(R.id.tv_conversation_detail_send_date);
            v.setTag(mHolder);
            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder mHolder = (ViewHolder) view.getTag();
            String body = cursor.getString(COLUMN_INDEX_BODY);
            long date = cursor.getLong(COLUMN_INDEX_DATE);
            int type = cursor.getInt(COLUMN_INDEX_TYPE);

            String dateStr = null;
            if (DateUtils.isToday(date)) {
                dateStr = DateFormat.getTimeFormat(context).format(date);
            } else {
                dateStr = DateFormat.getDateFormat(context).format(date);
            }

            //判断一下短信的类型
            if (type == 1) {
                //接收的短信
                mHolder.tl_conversation_detail_receive.setVisibility(View.VISIBLE);
                mHolder.tl_conversation_detail_send.setVisibility(View.GONE);
                mHolder.tv_conversation_detail_receive_body.setText(body);
                mHolder.tv_conversation_detail_receive_date.setText(dateStr);
            } else {
                //发送的短信
                mHolder.tl_conversation_detail_receive.setVisibility(View.GONE);
                mHolder.tl_conversation_detail_send.setVisibility(View.VISIBLE);
                mHolder.tv_conversation_detail_send_body.setText(body);
                mHolder.tv_conversation_detail_send_date.setText(dateStr);
            }

        }

        class ViewHolder {
            TableLayout tl_conversation_detail_receive;
            TextView tv_conversation_detail_receive_body;
            TextView tv_conversation_detail_receive_date;
            TableLayout tl_conversation_detail_send;
            TextView tv_conversation_detail_send_body;
            TextView tv_conversation_detail_send_date;
        }
    }

    private void initTitle() {
        TextView tv_conversation_detail_title = (TextView) findViewById(R.id.tv_conversation_detail_title);
        String name = Utils.getContactNameByAddress(address, getContentResolver());
        if (TextUtils.isEmpty(name)) {
            //陌生人
            tv_conversation_detail_title.setText(address);
        } else {
            //熟人
            tv_conversation_detail_title.setText(name);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.bt_conversation_detail_back:
                finish();
                break;
            case R.id.bt_conversation_detail_send:
                String body = et_conversation_detail_body.getText().toString();
                if (TextUtils.isEmpty(body)) {
                    Toast.makeText(this, "短信内容为空", 0).show();
                    break;
                }
                Utils.sendMessage(this, body, address);
                et_conversation_detail_body.setText("");

                //隐藏输入法
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                break;

        }

    }

    @Override
    public void onPreNotify(int token, Object cookie, Cursor cursor) {
    }

    @Override
    public void onPostNotify(int token, Object cookie, Cursor cursor) {
        lv_conversation_detail_list.setSelection(cursor.getCount() - 1);

    }
}
