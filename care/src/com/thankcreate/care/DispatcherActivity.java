package com.thankcreate.care;

import com.thankcreate.care.password.PasswordActivity;
import com.thankcreate.care.tool.misc.PreferenceHelper;
import com.thankcreate.care.tool.misc.StringTool;
import com.thankcreate.care.tool.ui.RefreshViewerHelper;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.NotificationType;
import com.umeng.fb.UMFeedbackService;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;

public class DispatcherActivity extends BaseActivity {

	private final int SPLASH_DISPLAY_LENGHT = 2000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dispatcher);
		// 友盟的错误统计
		MobclickAgent.onError(this);
		// 友盟用户反馈
		UMFeedbackService.enableNewReplyNotification(this, NotificationType.NotificationBar);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_dispatcher, menu);
		return true;
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
		
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
		final Editor editor = pref.edit();
		final String firstLaunch = pref.getString("Global_FirstLauch", "");
		final String usePassword = pref.getString("Global_UsePassword", "False");
		
		if(!StringTool.isNullOrEmpty(firstLaunch))
		{
			RefreshViewerHelper.getInstance().refreshMainViewModel();
		}
		
		new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
            	if(StringTool.isNullOrEmpty(firstLaunch))
        		{
        			editor.putString("Global_FirstLauch", "WhatEver");
        			editor.commit();
        			Intent intent = new Intent();
        			intent.setClass(DispatcherActivity.this, GuideActivity.class);					
        			startActivity(intent);
        		}
        		else if (usePassword.equalsIgnoreCase("True"))
        		{
        			Intent intent = new Intent();
        			intent.setClass(DispatcherActivity.this, PasswordActivity.class);					
        			startActivity(intent);
        		}
        		else {
        			Intent intent = new Intent();
        			intent.setClass(DispatcherActivity.this, MainActivity.class);					
        			startActivity(intent);
        		}
            }
        }, SPLASH_DISPLAY_LENGHT);
		
		
	}
}
