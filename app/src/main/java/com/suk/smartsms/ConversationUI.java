package com.suk.smartsms;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.suk.smartsms.utils.MyAsyncQueryHandler;
import com.suk.smartsms.utils.SmsUri;
import com.suk.smartsms.utils.Utils;

import java.util.HashSet;

public class ConversationUI extends Activity implements OnItemClickListener, OnClickListener, OnItemLongClickListener {

    private ConversationListCursorAdapter mAdapter;
    static final int MENU_ID_SEARCH = 0;
    static final int MENU_ID_EDIT = 1;
    static final int MENU_ID_EDITCANCEL = 2;
    boolean isEditting = false;
    boolean needStop = false;
    HashSet<Integer> selectedConversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        init();
    }

    private void init() {
        selectedConversation = new HashSet<Integer>();

        bt_conversation_newmsg = (Button) findViewById(R.id.bt_conversation_newmsg);
        bt_conversation_select_all = (Button) findViewById(R.id.bt_conversation_select_all);
        bt_conversation_select_cancel = (Button) findViewById(R.id.bt_conversation_select_cancel);
        bt_conversation_deletemsg = (Button) findViewById(R.id.bt_conversation_deletemsg);
        bt_conversation_newmsg.setOnClickListener(this);
        bt_conversation_select_all.setOnClickListener(this);
        bt_conversation_select_cancel.setOnClickListener(this);
        bt_conversation_deletemsg.setOnClickListener(this);


        lv_conversation_list = (ListView) findViewById(R.id.lv_conversation_list);
        mAdapter = new ConversationListCursorAdapter(this, null);
        lv_conversation_list.setAdapter(mAdapter);

        lv_conversation_list.setOnItemClickListener(this);
        lv_conversation_list.setOnItemLongClickListener(this);

        prepareData();

    }

    static final int COLUMN_INDEX_SNIPPET = 0;
    static final int COLUMN_INDEX_THREAD_ID = 1;
    static final int COLUMN_INDEX_MSG_COUNT = 2;
    static final int COLUMN_INDEX_ADDRESS = 3;
    static final int COLUMN_INDEX_DATE = 4;
    private Button bt_conversation_newmsg;
    private Button bt_conversation_select_all;
    private Button bt_conversation_select_cancel;
    private Button bt_conversation_deletemsg;
    private ListView lv_conversation_list;
    private ProgressDialog pd;

    private void prepareData() {
        String[] projection = new String[]{
                "sms.body AS snippet",
                "sms.thread_id AS _id",
                "groups.msg_count AS msg_count",
                "address AS address",
                "date AS date"
        };
        //获取异步查询处理器对象
        MyAsyncQueryHandler mHandler = new MyAsyncQueryHandler(getContentResolver());

        String where = null;

        //判断是否从群组界面跳转过来的
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        if (!TextUtils.isEmpty(name)) {
            setTitle(name);
            where = "thread_id in " + intent.getStringExtra("threadIds");
        }

        //开启子线程查询指定的uri
        mHandler.startQuery(0, mAdapter, SmsUri.CONVERSATION_URI, projection, where, null, "date desc");
    }

    class ConversationListCursorAdapter extends CursorAdapter {

        public ConversationListCursorAdapter(Context context, Cursor c) {
            super(context, c);
            // TODO Auto-generated constructor stub
        }

        //创建view对象，返回的view对象将作为listview的条目
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            ViewHolder mHolder = new ViewHolder();
            View v = View.inflate(context, R.layout.item_conversation_list, null);
            mHolder.cb_conversation_selected = (CheckBox) v.findViewById(R.id.cb_conversation_selected);
            mHolder.iv_conversation_list_photo = (ImageView) v.findViewById(R.id.iv_conversation_list_photo);
            mHolder.tv_conversation_list_name = (TextView) v.findViewById(R.id.tv_conversation_list_name);
            mHolder.tv_conversation_list_date = (TextView) v.findViewById(R.id.tv_conversation_list_date);
            mHolder.tv_conversation_list_body = (TextView) v.findViewById(R.id.tv_conversation_list_body);
            v.setTag(mHolder);
            return v;
        }

        //给view对象里的各个组件设置要显示的内容
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            String address = cursor.getString(COLUMN_INDEX_ADDRESS);
            long date = cursor.getLong(COLUMN_INDEX_DATE);
            String msgCount = cursor.getString(COLUMN_INDEX_MSG_COUNT);
            String body = cursor.getString(COLUMN_INDEX_SNIPPET);
            int thread_id = cursor.getInt(COLUMN_INDEX_THREAD_ID);

            ViewHolder mHolder = (ViewHolder) view.getTag();

            //设置选框是否显示
            if (isEditting) {
                mHolder.cb_conversation_selected.setVisibility(View.VISIBLE);
                mHolder.cb_conversation_selected.setChecked(selectedConversation.contains(thread_id));
            } else {
                mHolder.cb_conversation_selected.setVisibility(View.GONE);
            }

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
                mHolder.tv_conversation_list_name.setText(address + "(" + msgCount + ")");
                mHolder.iv_conversation_list_photo.setImageResource(R.drawable.ic_unknow_contact_picture);
            } else {
                //熟人
                mHolder.tv_conversation_list_name.setText(name + "(" + msgCount + ")");
                Bitmap bm = Utils.getContactPhotoByAddress(address, getContentResolver());
                if (bm == null) {
                    mHolder.iv_conversation_list_photo.setImageResource(R.drawable.ic_contact_picture);
                } else {
                    mHolder.iv_conversation_list_photo.setImageBitmap(bm);
                }

            }

        }

        class ViewHolder {
            CheckBox cb_conversation_selected;
            ImageView iv_conversation_list_photo;
            TextView tv_conversation_list_name;
            TextView tv_conversation_list_date;
            TextView tv_conversation_list_body;
        }
    }

    //此方法只调用一次，菜单创建时调用
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_ID_SEARCH, 0, "搜索");
        menu.add(0, MENU_ID_EDIT, 0, "编辑");
        menu.add(0, MENU_ID_EDITCANCEL, 0, "取消编辑");
        return super.onCreateOptionsMenu(menu);
    }

    //每次菜单显示时调用
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isEditting) {
            menu.findItem(MENU_ID_SEARCH).setVisible(false);
            menu.findItem(MENU_ID_EDIT).setVisible(false);
            menu.findItem(MENU_ID_EDITCANCEL).setVisible(true);
        } else {
            menu.findItem(MENU_ID_SEARCH).setVisible(true);
            menu.findItem(MENU_ID_EDIT).setVisible(true);
            menu.findItem(MENU_ID_EDITCANCEL).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    //option菜单的点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case MENU_ID_SEARCH:
                onSearchRequested();
                break;
            case MENU_ID_EDIT:
                isEditting = true;
                selectedConversation.clear();
                refreshState();
                break;
            case MENU_ID_EDITCANCEL:
                isEditting = false;
                refreshState();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    //根据是否编辑状态决定四个按钮是否显示
    private void refreshState() {
        if (isEditting) {
            bt_conversation_newmsg.setVisibility(View.GONE);
            bt_conversation_select_all.setVisibility(View.VISIBLE);
            bt_conversation_select_cancel.setVisibility(View.VISIBLE);
            bt_conversation_deletemsg.setVisibility(View.VISIBLE);

            bt_conversation_select_cancel.setEnabled(selectedConversation.size() != 0);
            bt_conversation_deletemsg.setEnabled(selectedConversation.size() != 0);
            bt_conversation_select_all.setEnabled(selectedConversation.size() != lv_conversation_list.getCount());
        } else {
            bt_conversation_newmsg.setVisibility(View.VISIBLE);
            bt_conversation_select_all.setVisibility(View.GONE);
            bt_conversation_select_cancel.setVisibility(View.GONE);
            bt_conversation_deletemsg.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        if (isEditting) {
            Cursor cursor = mAdapter.getCursor();
            cursor.moveToPosition(position);
            int thread_id = cursor.getInt(COLUMN_INDEX_THREAD_ID);
            CheckBox cb = (CheckBox) view.findViewById(R.id.cb_conversation_selected);
            if (cb.isChecked()) {
                selectedConversation.remove(thread_id);
                cb.setChecked(false);
            } else {
                selectedConversation.add(thread_id);
                cb.setChecked(true);
            }
            refreshState();
        } else {
            Intent intent = new Intent(this, ConversationDetailUI.class);
            Cursor cursor = mAdapter.getCursor();
            cursor.moveToPosition(position);
            intent.putExtra("address", cursor.getString(COLUMN_INDEX_ADDRESS));
            startActivity(intent);
        }

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.bt_conversation_newmsg:
                Intent intent = new Intent(this, NewMessageUI.class);
                startActivity(intent);
                break;
            case R.id.bt_conversation_select_all:
                //全选，遍历cursor，把所有会话id存入hashset中
                Cursor cursor = mAdapter.getCursor();
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    int thread_id = cursor.getInt(COLUMN_INDEX_THREAD_ID);
                    selectedConversation.add(thread_id);
                }
                mAdapter.notifyDataSetChanged();
                refreshState();
                break;
            case R.id.bt_conversation_select_cancel:
                //取消选择，清空hashset即可
                selectedConversation.clear();
                mAdapter.notifyDataSetChanged();
                refreshState();
                break;
            case R.id.bt_conversation_deletemsg:
                showDeleteDialog();
                break;

        }

    }

    private void showDeleteDialog() {
        AlertDialog.Builder builder = new Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle("删除");
        builder.setMessage("亲，您真的要删除吗么么哒");
        builder.setNegativeButton("取消", null);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                showDeleteProgress();

            }
        });
        builder.show();
    }

    private void showDeleteProgress() {
        pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMax(selectedConversation.size());
        pd.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                isEditting = false;
                refreshState();
            }
        });
        pd.setButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                needStop = true;

            }
        });
        pd.show();
        deleteMsg();
    }

    private void deleteMsg() {
        Thread t = new Thread() {
            @Override
            public void run() {
                for (int thread_id : selectedConversation) {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (needStop) {
                        needStop = false;
                        break;
                    }
                    getContentResolver().delete(SmsUri.SMS_URI, "thread_id = ?", new String[]{thread_id + ""});
                    pd.incrementProgressBy(1);
                }

                pd.dismiss();
            }
        };
        t.start();
    }

    @Override
    public void onBackPressed() {
        if (isEditting) {
            isEditting = false;
            refreshState();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                   int position, long id) {
        Cursor cursor = (Cursor) mAdapter.getItem(position);
        int threadId = cursor.getInt(COLUMN_INDEX_THREAD_ID);
        //先检测一下该会话是否在某个群组中
        String name = getThreadFromThread_Group(threadId);
        if (TextUtils.isEmpty(name)) {
            //弹出群组的选择对话框
            showGroupSelectDialog(threadId);
        } else {
            //吐司弹出该群组的名字
            Toast.makeText(this, "该会话已经存在“" + name + "”中了", 0).show();
        }
        return true;
    }

    private void showGroupSelectDialog(final int threadId) {
        AlertDialog.Builder builder = new Builder(this);
        builder.setTitle("选择要加入的群组");

        Cursor cursor = getContentResolver().query(SmsUri.GROUP_URI, null, null, null, null);
        String[] items = new String[cursor.getCount()];
        final int[] itemsId = new int[cursor.getCount()];

        while (cursor.moveToNext()) {
            //拿到群组的名字
            String name = cursor.getString(1);
            //拿到群组id
            int groupId = cursor.getInt(0);
            items[cursor.getPosition()] = name;
            itemsId[cursor.getPosition()] = groupId;
        }
        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //把会话添加到群组里
                ContentValues values = new ContentValues();
                values.put("thread_id", threadId);
                values.put("group_id", itemsId[which]);
                Uri uri = getContentResolver().insert(SmsUri.THREAD_GROUP_URI, values);
                if (ContentUris.parseId(uri) != -1) {
                    Toast.makeText(ConversationUI.this, "添加成功", 0).show();
                } else {
                    Toast.makeText(ConversationUI.this, "添加 失败", 0).show();
                }

            }
        });
        builder.show();

    }

    private String getThreadFromThread_Group(int threadId) {
        Cursor cursor = getContentResolver().query(SmsUri.THREAD_GROUP_URI, new String[]{"group_id"}, "thread_id = ?", new String[]{threadId + ""}, null);
        if (cursor.moveToFirst()) {
            int groupId = cursor.getInt(0);
            Cursor c = getContentResolver().query(SmsUri.GROUP_URI, new String[]{"name"}, "_id = ?", new String[]{groupId + ""}, null);
            c.moveToFirst();
            String name = c.getString(0);
            return name;
        } else {
            return null;
        }
    }
}
