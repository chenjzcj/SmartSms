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


public class NewMessageUI extends Activity implements OnClickListener{

	private AutoCompleteTextView actv_newmsg_address;
	private EditText et_newmsg_body;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_newmsg);
		init();
	}

	private void init(){
		actv_newmsg_address = (AutoCompleteTextView) findViewById(R.id.et_newmsg_address);
		et_newmsg_body = (EditText) findViewById(R.id.et_newmsg_body);

		NewMsgCursorAdapter mAdapter = new NewMsgCursorAdapter(this, null);
		actv_newmsg_address.setAdapter(mAdapter);
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

		Button bt_newmsg_send = (Button) findViewById(R.id.bt_newmsg_send);
		ImageButton imbt_newmsg_select_contact = (ImageButton) findViewById(R.id.imbt_newmsg_select_contact);
		bt_newmsg_send.setOnClickListener(this);
		imbt_newmsg_select_contact.setOnClickListener(this);
	}

	class NewMsgCursorAdapter extends CursorAdapter{

		public NewMsgCursorAdapter(Context context, Cursor c) {
			super(context, c);
			// TODO Auto-generated constructor stub
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View v = View.inflate(context, R.layout.item_select_contact, null);
			return v;
		}

		@Override
		public CharSequence convertToString(Cursor cursor) {
			// TODO Auto-generated method stub
			return cursor.getString(1);
		}
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			String name = cursor.getString(0);
			String address = cursor.getString(1);

			TextView tv_select_contact_name = (TextView) view.findViewById(R.id.tv_select_contact_name);
			TextView tv_select_contact_address = (TextView) view.findViewById(R.id.tv_select_contact_address);
			tv_select_contact_address.setText(address);
			tv_select_contact_name.setText(name);

		}

	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
			case R.id.bt_newmsg_send:
				String address= actv_newmsg_address.getText().toString();
				String body = et_newmsg_body.getText().toString();
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
		}

	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 100){
			//选择联系人activity返回给我们的数据
			Uri uri = data.getData();
			if(uri != null){
				int id = Utils.getContactInfoByUri(uri, getContentResolver());
				if(id == -1){
					Toast.makeText(this, "您选择的联系人木有号码哟", 0).show();
				}
				else{
					String address = Utils.getContactAddressById(id, getContentResolver());
					actv_newmsg_address.setText(address);
					et_newmsg_body.requestFocus();
				}
			}
		}
	}
}
