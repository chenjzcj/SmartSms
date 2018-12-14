package com.suk.smartsms;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;


public class MainActivity extends TabActivity implements OnClickListener{

	private TabHost mHost;
	View v_slide;
	int basicWidth;
	int currentX;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		init();
	}

	private void init(){
		mHost = (TabHost) findViewById(android.R.id.tabhost);
		addTab("conversation", "会话", R.drawable.tab_conversation, new Intent(this, ConversationUI.class));
		addTab("folder", "文件夹", R.drawable.tab_folder, new Intent(this, FolderUI.class));
		addTab("group", "群组", R.drawable.tab_group, new Intent(this, GroupUI.class));

		final LinearLayout ll_conversation = (LinearLayout) findViewById(R.id.ll_conversation);
		final LinearLayout ll_folder = (LinearLayout) findViewById(R.id.ll_folder);
		final LinearLayout ll_group = (LinearLayout) findViewById(R.id.ll_group);

		ll_conversation.setOnClickListener(this);
		ll_folder.setOnClickListener(this);
		ll_group.setOnClickListener(this);

		//获取视图树观察者，并设置全局布局侦听
		ll_conversation.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				ll_conversation.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				int width = ll_conversation.getWidth();
				int height = ll_conversation.getHeight();
				v_slide = findViewById(R.id.v_slide);
				LayoutParams lp = (LayoutParams) v_slide.getLayoutParams();
				lp.width = width;
				lp.height = height;
				//获取组件和父元素之间的左边距
				lp.leftMargin = ll_conversation.getLeft();

				basicWidth = findViewById(R.id.rl_conversation).getWidth();
			}
		});


	}

	private void addTab(String tag, String label, int iconId, Intent intent){
		//创建选项卡
		TabSpec tabSpec = mHost.newTabSpec(tag);
		tabSpec.setIndicator(label, getResources().getDrawable(iconId));
		//设置选项卡所属的内容
		tabSpec.setContent(intent);
		//把选项卡添加至TabWidget
		mHost.addTab(tabSpec);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
			case R.id.ll_conversation:
				mHost.setCurrentTabByTag("conversation");
				startTranslateAnimation(currentX, 0);
				currentX = 0;
				break;
			case R.id.ll_folder:
				mHost.setCurrentTabByTag("folder");
				startTranslateAnimation(currentX, basicWidth);
				currentX = basicWidth;
				break;
			case R.id.ll_group:
				mHost.setCurrentTabByTag("group");
				startTranslateAnimation(currentX, basicWidth * 2);
				currentX = basicWidth * 2;
				break;

		}

	}

	private void startTranslateAnimation(int fromXDelta, int toXDelta){
		//定义位移动画
		TranslateAnimation ta = new TranslateAnimation(fromXDelta, toXDelta, 0, 0);
		//设置动画持续时间
		ta.setDuration(500);
		//设置动画移动完毕后停留在该位置
		ta.setFillAfter(true);
		v_slide.startAnimation(ta);
	}

}
