package com.suk.smartsms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.suk.smartsms.utils.Utils;


public class NewMessageUI extends Activity implements OnClickListener {

    private AutoCompleteTextView actvNewmsgAddress;
    private EditText etNewMsgBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newmsg);
        init();
    }

    private void init() {
        actvNewmsgAddress = findViewById(R.id.et_newmsg_address);
        etNewMsgBody = findViewById(R.id.et_newmsg_body);

        NewMsgCursorAdapter mAdapter = new NewMsgCursorAdapter(this, null);
        actvNewmsgAddress.setAdapter(mAdapter);
        mAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            //输入框内容一改变，此方法调用，参数就是输入框的内容
            @Override
            public Cursor runQuery(CharSequence constraint) {
                String[] projection = new String[]{
                        "display_name",
                        "data1",
                        "_id"
                };
                Cursor cursor = getContentResolver().query(Phone.CONTENT_URI, projection, "data1 like ?", new String[]{constraint + "%"}, null);
                //把这个cursor传给了adapter对象
                return cursor;
            }
        });

        Button btNewMsgSend = findViewById(R.id.bt_newmsg_send);
        ImageButton imbtNewMsgSelectContact = findViewById(R.id.imbt_newmsg_select_contact);
        btNewMsgSend.setOnClickListener(this);
        imbtNewMsgSelectContact.setOnClickListener(this);
    }

    static class NewMsgCursorAdapter extends CursorAdapter {
        public NewMsgCursorAdapter(Context context, Cursor c) {
            super(context, c);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return View.inflate(context, R.layout.item_select_contact, null);
        }

        @Override
        public CharSequence convertToString(Cursor cursor) {
            return cursor.getString(1);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            String name = cursor.getString(0);
            String address = cursor.getString(1);
            TextView tvSelectContactName = view.findViewById(R.id.tv_select_contact_name);
            TextView tvSelectContactAddress = view.findViewById(R.id.tv_select_contact_address);
            tvSelectContactAddress.setText(address);
            tvSelectContactName.setText(name);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.bt_newmsg_send:
                String address = actvNewmsgAddress.getText().toString();
                String body = etNewMsgBody.getText().toString();
                Utils.sendMessage(this, body, address);
                finish();
                break;
            case R.id.imbt_newmsg_select_contact:
                //隐式启动选择联系人的activity
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_PICK);
                intent.setData(Uri.parse("content://com.android.contacts/contacts"));
                startActivityForResult(intent, 100);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            //选择联系人activity返回给我们的数据
            Uri uri = data.getData();
            if (uri != null) {
                int id = Utils.getContactInfoByUri(uri, getContentResolver());
                if (id == -1) {
                    Toast.makeText(this, "您选择的联系人木有号码哟", 0).show();
                } else {
                    String address = Utils.getContactAddressById(id, getContentResolver());
                    actvNewmsgAddress.setText(address);
                    etNewMsgBody.requestFocus();
                }
            }
        }
    }
}
