package com.thankcreate.care.preference;

import com.hp.hpl.sparta.xpath.BooleanExpr;
import com.markupartist.android.widget.ActionBar;
import com.thankcreate.care.App;
import com.thankcreate.care.AppConstants;
import com.thankcreate.care.BaseActivity;
import com.thankcreate.care.R;
import com.thankcreate.care.R.layout;
import com.thankcreate.care.R.menu;
import com.thankcreate.care.account.AccountActivity;
import com.thankcreate.care.password.PasswordActivity;
import com.thankcreate.care.password.PasswordSetActivity;
import com.thankcreate.care.rss.RssSetActivity;
import com.thankcreate.care.tool.misc.PreferenceHelper;
import com.umeng.fb.UMFeedbackService;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

public class PreferenceActivity extends BaseActivity {
	
	private ActionBar actionBar;
	private ToggleButton toggleUsePassword;
	private ToggleButton toggleUseSharedPicture;
	private RelativeLayout layoutGotoSiteLayout;
	private RelativeLayout layoutFeedback;
	private RelativeLayout layoutAboutLayout;		

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preference_preference);
		initActionBar();
		initControl();
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		initControlContent();
	}



	private void initActionBar() {
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("设置");
		actionBar.SetTitleLogo(R.drawable.tab_settings);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_preference, menu);
		return true;
	}
	
	private void initControl()
	{
		// 启动密码
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
		final Editor edit = pref.edit();
		toggleUsePassword = (ToggleButton) findViewById(R.id.preference_user_password_toggle);
		toggleUsePassword.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ToggleButton btn = (ToggleButton) v;
				boolean isChecked = btn.isChecked();
				if(isChecked)
				{					
					// 这里不能直接设为true，要等真正把密码设置正确了，才能置为true
					//edit.putString("Global_UsePassword", "True");
					Intent intent = new Intent();
					intent.setClass(PreferenceActivity.this, PasswordSetActivity.class);					
					startActivity(intent);
				}
				else
					edit.putString("Global_UsePassword", "False");
				edit.commit();
			}
		});

		
		// 是否显示转发图
		// TODO
		toggleUseSharedPicture = (ToggleButton) findViewById(R.id.preference_show_forward_pic_toggle);
		toggleUseSharedPicture.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ToggleButton btn = (ToggleButton) v;
				boolean isChecked = btn.isChecked();
				if(isChecked)
					edit.putString("Global_NeedFetchImageInRetweet", "True");
				else
					edit.putString("Global_NeedFetchImageInRetweet", "False");
				edit.commit();
			}
		});

		
		// 访问网站
		layoutGotoSiteLayout = (RelativeLayout) findViewById(R.id.preference_goto_site);
		layoutGotoSiteLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();        
				intent.setAction("android.intent.action.VIEW");    
				Uri content_url = Uri.parse("http://thankcreate.github.com/Care/");   
				intent.setData(content_url);  
				startActivity(intent);
			}
		});
		
		// 意见反馈
		layoutFeedback = (RelativeLayout) findViewById(R.id.preference_feedback);
		layoutFeedback.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				UMFeedbackService.openUmengFeedbackSDK(PreferenceActivity.this);
			}
		});
		
		// 关于
		layoutAboutLayout = (RelativeLayout) findViewById(R.id.preference_about);
		layoutAboutLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(PreferenceActivity.this, PreferenceAboutActivity.class);					
				startActivity(intent);
			}
		});
	}
	
	private void initControlContent()
	{
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
		String userPassword = pref.getString("Global_UsePassword", "False");		
		if(userPassword.equalsIgnoreCase("True"))
		{
			toggleUsePassword.setChecked(true);
		}
		else 
		{
			toggleUsePassword.setChecked(false);
		}
		
		String useSharedPicture = pref.getString("Global_NeedFetchImageInRetweet", "True");
		if(useSharedPicture.equalsIgnoreCase("True"))
		{
			toggleUseSharedPicture.setChecked(true);
		}
		else 
		{
			toggleUseSharedPicture.setChecked(false);
		}
	}
}
