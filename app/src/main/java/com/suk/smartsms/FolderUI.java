package com.suk.smartsms;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.suk.smartsms.utils.MyAsyncQueryHandler;
import com.suk.smartsms.utils.Utils;

import java.util.HashMap;

public class FolderUI extends ListActivity implements MyAsyncQueryHandler.onNotifyAdapterListner, OnItemClickListener {

    private ListView lv_folder_ui;
    private int[] folderIcons;
    private String[] folderLabels;
    private HashMap<Integer, Integer> folderCount;
    private FolderUIAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        lv_folder_ui = getListView();
        init();
    }

    @SuppressLint("UseSparseArrays")
    private void init() {
        folderIcons = new int[]{
                R.drawable.a_f_inbox,
                R.drawable.a_f_outbox,
                R.drawable.a_f_sent,
                R.drawable.a_f_draft
        };
        folderLabels = new String[]{
                "收件箱",
                "发件箱",
                "已发送",
                "草稿箱"
        };
        folderCount = new HashMap<Integer, Integer>();
        for (int i = 0; i < folderLabels.length; i++) {
            folderCount.put(i, 0);
            //异步查询4个箱子的短信数量
            MyAsyncQueryHandler mHandler = new MyAsyncQueryHandler(getContentResolver());
            mHandler.setOnNotifyAdapterListner(this);
            mHandler.startQuery(i, null, Utils.getTypeUri(i), new String[]{"count(*) AS count"}, null, null, null);
        }

        mAdapter = new FolderUIAdapter();
        lv_folder_ui.setAdapter(mAdapter);
        lv_folder_ui.setOnItemClickListener(this);
    }

    class FolderUIAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return folderIcons.length;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = View.inflate(FolderUI.this, R.layout.item_folder_list, null);
            ImageView iv_folder_list_icon = (ImageView) v.findViewById(R.id.iv_folder_list_icon);
            TextView tv_folder_list_label = (TextView) v.findViewById(R.id.tv_folder_list_label);
            TextView tv_folder_list_count = (TextView) v.findViewById(R.id.tv_folder_list_count);
            iv_folder_list_icon.setImageResource(folderIcons[position]);
            tv_folder_list_label.setText(folderLabels[position]);
            tv_folder_list_count.setText(folderCount.get(position) + "");
            return v;
        }

    }

    @Override
    public void onPreNotify(int token, Object cookie, Cursor cursor) {
        cursor.moveToFirst();
        //拿到某一个箱子的短信数量
        int count = cursor.getInt(0);
        //以token为key，把短信数量存入对应的位置
        folderCount.put(token, count);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPostNotify(int token, Object cookie, Cursor cursor) {
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        Intent intent = new Intent(this, FolerDetailUI.class);
        intent.putExtra("type", position);
        startActivity(intent);

    }
}
