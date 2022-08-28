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


/**
 * @author Administrator
 */
public class ConversationDetailUI extends Activity implements OnClickListener, MyAsyncQueryHandler.NotifyAdapterListener {

    private String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
    private EditText etConversationDetailBody;
    private ListView lvConversationDetailList;

    private void init() {
        Button btConversationDetailBack = findViewById(R.id.bt_conversation_detail_back);
        Button btConversationDetailSend = findViewById(R.id.bt_conversation_detail_send);
        btConversationDetailBack.setOnClickListener(this);
        btConversationDetailSend.setOnClickListener(this);
        etConversationDetailBody = findViewById(R.id.et_conversation_detail_body);
        String[] projection = new String[]{"_id", "body", "date", "type"};
        lvConversationDetailList = findViewById(R.id.lv_conversation_detail_list);
        ConversationDetailCursorAdapter mAdapter = new ConversationDetailCursorAdapter(this, null);
        lvConversationDetailList.setAdapter(mAdapter);
        //查询会话里的短信
        MyAsyncQueryHandler mHandler = new MyAsyncQueryHandler(getContentResolver());
        mHandler.setOnNotifyAdapterListner(this);
        mHandler.startQuery(0, mAdapter, SmsUri.SMS_URI, projection, "address = ?", new String[]{address}, "date");
    }

    class ConversationDetailCursorAdapter extends CursorAdapter {
        public ConversationDetailCursorAdapter(Context context, Cursor c) {
            super(context, c);
        }

        //cursor模型层数据改变了，此方法调用
        @Override
        protected void onContentChanged() {
            super.onContentChanged();
            //listview滑动到最底部
            lvConversationDetailList.setSelection(getCount() - 1);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = View.inflate(context, R.layout.item_conversation_detail, null);
            ViewHolder mHolder = new ViewHolder();
            mHolder.tlConversationDetailReceive = v.findViewById(R.id.tl_conversation_detail_receive);
            mHolder.tvConversationDetailReceiveBody = v.findViewById(R.id.tv_conversation_detail_receive_body);
            mHolder.tvConversationDetailReceiveDate = v.findViewById(R.id.tv_conversation_detail_receive_date);
            mHolder.tlConversationDetailSend = v.findViewById(R.id.tl_conversation_detail_send);
            mHolder.tvConversationDetailSendBody = v.findViewById(R.id.tv_conversation_detail_send_body);
            mHolder.tvConversationDetailSendDate = v.findViewById(R.id.tv_conversation_detail_send_date);
            v.setTag(mHolder);
            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder mHolder = (ViewHolder) view.getTag();
            String body = cursor.getString(COLUMN_INDEX_BODY);
            long date = cursor.getLong(COLUMN_INDEX_DATE);
            int type = cursor.getInt(COLUMN_INDEX_TYPE);
            String dateStr;
            if (DateUtils.isToday(date)) {
                dateStr = DateFormat.getTimeFormat(context).format(date);
            } else {
                dateStr = DateFormat.getDateFormat(context).format(date);
            }
            //判断一下短信的类型
            if (type == 1) {
                //接收的短信
                mHolder.tlConversationDetailReceive.setVisibility(View.VISIBLE);
                mHolder.tlConversationDetailSend.setVisibility(View.GONE);
                mHolder.tvConversationDetailReceiveBody.setText(body);
                mHolder.tvConversationDetailReceiveDate.setText(dateStr);
            } else {
                //发送的短信
                mHolder.tlConversationDetailReceive.setVisibility(View.GONE);
                mHolder.tlConversationDetailSend.setVisibility(View.VISIBLE);
                mHolder.tvConversationDetailSendBody.setText(body);
                mHolder.tvConversationDetailSendDate.setText(dateStr);
            }
        }

        class ViewHolder {
            TableLayout tlConversationDetailReceive;
            TextView tvConversationDetailReceiveBody;
            TextView tvConversationDetailReceiveDate;
            TableLayout tlConversationDetailSend;
            TextView tvConversationDetailSendBody;
            TextView tvConversationDetailSendDate;
        }
    }

    private void initTitle() {
        TextView tvConversationDetailTitle = findViewById(R.id.tv_conversation_detail_title);
        String name = Utils.getContactNameByAddress(address, getContentResolver());
        if (TextUtils.isEmpty(name)) {
            //陌生人
            tvConversationDetailTitle.setText(address);
        } else {
            //熟人
            tvConversationDetailTitle.setText(name);
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
                String body = etConversationDetailBody.getText().toString();
                if (TextUtils.isEmpty(body)) {
                    Toast.makeText(this, "短信内容为空", 0).show();
                    break;
                }
                Utils.sendMessage(this, body, address);
                etConversationDetailBody.setText("");
                //隐藏输入法
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPreNotify(int token, Object cookie, Cursor cursor) {
    }

    @Override
    public void onPostNotify(int token, Object cookie, Cursor cursor) {
        lvConversationDetailList.setSelection(cursor.getCount() - 1);
    }
}
