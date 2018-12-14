package com.suk.smartsms;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.suk.smartsms.utils.MyAsyncQueryHandler;
import com.suk.smartsms.utils.SmsUri;


public class GroupUI extends ListActivity implements OnItemLongClickListener, OnItemClickListener {

    private GroupCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        ListView lv_group_list = getListView();
        mAdapter = new GroupCursorAdapter(this, null);
        lv_group_list.setAdapter(mAdapter);

        prepareData();

        lv_group_list.setOnItemLongClickListener(this);
        lv_group_list.setOnItemClickListener(this);
    }

    private void prepareData() {
        MyAsyncQueryHandler mHandler = new MyAsyncQueryHandler(getContentResolver());
        mHandler.startQuery(0, mAdapter, SmsUri.GROUP_URI, null, null, null, null);

    }

    class GroupCursorAdapter extends CursorAdapter {

        public GroupCursorAdapter(Context context, Cursor c) {
            super(context, c);
            // TODO Auto-generated constructor stub
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return View.inflate(context, R.layout.item_group_list, null);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            String name = cursor.getString(cursor.getColumnIndex("name"));
            TextView tv = (TextView) view.findViewById(R.id.tv_group_list_name);
            tv.setText(name);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_group_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        showCreateGroupDialog();
        return super.onOptionsItemSelected(item);
    }

    private void showCreateGroupDialog() {
        AlertDialog.Builder builder = new Builder(this);
        builder.setTitle("新建群组");
        View v = View.inflate(this, R.layout.create_group, null);

        final AlertDialog ad = builder.create();
        ad.setView(v, 0, 0, 0, 0);
        ad.show();
        LayoutParams lp = ad.getWindow().getAttributes();
        Display dp = getWindowManager().getDefaultDisplay();
        lp.width = (int) (dp.getWidth() * 0.7);
        ad.getWindow().setAttributes(lp);

        //给对话框中的按钮设置侦听
        Button bt_create_group = (Button) v.findViewById(R.id.bt_create_group);
        final EditText et_create_group_name = (EditText) v.findViewById(R.id.et_create_group_name);
        bt_create_group.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String name = et_create_group_name.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(GroupUI.this, "群组名字不能为空", 0).show();
                } else {
                    //创建群组
                    addGroup(name);
                    ad.dismiss();
                }
            }

        });
    }

    //添加群组
    private void addGroup(String name) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        Uri uri = getContentResolver().insert(SmsUri.GROUP_URI, values);
        if (ContentUris.parseId(uri) != -1) {
            Toast.makeText(this, "创建成功", 0).show();
        } else {
            Toast.makeText(this, "创建失败", 0).show();
        }

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                   int position, long id) {
//		Cursor cursor = mAdapter.getCursor();
//		cursor.moveToPosition(position);
        Cursor cursor = (Cursor) mAdapter.getItem(position);
        int groupId = cursor.getInt(0);
        showGroupDialog(groupId);
        return false;
    }

    private void showGroupDialog(final int groupId) {
        AlertDialog.Builder builder = new Builder(this);
        builder.setItems(new String[]{"修改", "删除"}, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0://点击了修改
                        showUpdateDialog(groupId);
                        break;
                    case 1://点击了删除
                        showDeleteDialog(groupId);
                        break;

                }

            }


        });
        builder.show();
    }


    private void showDeleteDialog(final int groupId) {
        AlertDialog.Builder builder = new Builder(this);
        builder.setTitle("删除");
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setMessage("真的要删除吗，再考虑一下");
        builder.setNegativeButton("取消", null);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteGroup(groupId);

            }
        });
        builder.show();

    }

    private void deleteGroup(int groupId) {
        int number = getContentResolver().delete(ContentUris.withAppendedId(SmsUri.GROUP_ID_URI, groupId), null, null);
        if (number > 0) {
            Toast.makeText(this, "删除成功", 0).show();
        } else {
            Toast.makeText(this, "删除失败", 0).show();
        }
    }

    private void showUpdateDialog(final int groupId) {
        AlertDialog.Builder builder = new Builder(this);
        builder.setTitle("修改群组");
        View v = View.inflate(this, R.layout.create_group, null);

        final AlertDialog ad = builder.create();
        ad.setView(v, 0, 0, 0, 0);
        ad.show();
        LayoutParams lp = ad.getWindow().getAttributes();
        Display dp = getWindowManager().getDefaultDisplay();
        lp.width = (int) (dp.getWidth() * 0.7);
        ad.getWindow().setAttributes(lp);

        //给对话框中的按钮设置侦听
        Button bt_create_group = (Button) v.findViewById(R.id.bt_create_group);
        bt_create_group.setText("确认修改");
        final EditText et_create_group_name = (EditText) v.findViewById(R.id.et_create_group_name);
        bt_create_group.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String name = et_create_group_name.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(GroupUI.this, "群组名字不能为空", 0).show();
                } else {
                    //修改群组
                    updateGroup(name, groupId);
                    ad.dismiss();
                }
            }

        });

    }

    private void updateGroup(String name, int groupId) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        int number = getContentResolver().update(SmsUri.GROUP_URI, values, "_id = ?", new String[]{groupId + ""});
        if (number > 0) {
            Toast.makeText(this, "修改成功", 0).show();
        } else {
            Toast.makeText(this, "修改失败", 0).show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {

        Cursor cursor = (Cursor) mAdapter.getItem(position);
        int groupId = cursor.getInt(0);
        String threadIds = getThreadIdsFromGroup(groupId);
        if (TextUtils.isEmpty(threadIds)) {
            Toast.makeText(this, "该群组下没有任何会话", 0).show();
        } else {
            Intent intent = new Intent(this, ConversationUI.class);

            String name = cursor.getString(1);
            intent.putExtra("name", name);
            intent.putExtra("threadIds", threadIds);
            startActivity(intent);
        }
    }

    //通过群组id，查询该群组下的所有会话的id
    private String getThreadIdsFromGroup(int groupId) {
        Cursor cursor = getContentResolver().query(SmsUri.THREAD_GROUP_URI, new String[]{"thread_id"}, "group_id = ?", new String[]{groupId + ""}, null);
        StringBuilder sb = new StringBuilder();
        if (cursor.getCount() > 0) {
            sb.append("(");
            while (cursor.moveToNext()) {
                String threadId = cursor.getString(0);
                sb.append(threadId + ", ");
            }
            sb.replace(sb.lastIndexOf(", "), sb.length(), ")");
            return sb.toString();
        }
        return null;
    }
}
