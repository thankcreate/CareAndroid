package com.thankcreate.care;

import com.thankcreate.care.password.PasswordActivity;
import com.thankcreate.care.service.NewsPollingService;
import com.thankcreate.care.tool.misc.PreferenceHelper;
import com.thankcreate.care.tool.misc.StringTool;
import com.thankcreate.care.tool.ui.RefreshViewerHelper;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.NotificationType;
import com.umeng.fb.UMFeedbackService;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
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
			RefreshViewerHelper.getAppInstance().refreshMainViewModel();
		}
		
		new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
            	// 第一次启动
            	if(StringTool.isNullOrEmpty(firstLaunch))
        		{
            		// 开启后台轮询
            		PendingIntent alarmSender;
            		alarmSender = PendingIntent.getService(DispatcherActivity.this, 0, new Intent(DispatcherActivity.this,
            				NewsPollingService.class), 0);
            		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            		SharedPreferences pref = App.getAppContext().getSharedPreferences(
            				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);            		
            		Editor editor = pref.edit();
        			editor.putString("Global_UsePolling", "True");
        			editor.putLong("Global_PollingTime", AppConstants.DEFAULT_POLLING_INTERVAL);
        			editor.commit();
        			long interval = AppConstants.DEFAULT_POLLING_INTERVAL;
        			long firstTime = SystemClock.elapsedRealtime() + interval;
        			am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
        	                firstTime , interval, alarmSender);
        		
            		
            		// 跳转到引导页
        			editor.putString("Global_FirstLauch", "WhatEver");
        			editor.commit();
        			Intent intent = new Intent();
        			intent.setClass(DispatcherActivity.this, GuideActivity.class);					
        			startActivity(intent);
        		}
            	// 不是第一次启动，且设置了密码
        		else if (usePassword.equalsIgnoreCase("True"))
        		{
        			Intent intent = new Intent();
        			intent.setClass(DispatcherActivity.this, PasswordActivity.class);					
        			startActivity(intent);
        		}
            	// 不是第一次启动，且没有设置密码
        		else {
        			Intent intent = new Intent();
        			intent.setClass(DispatcherActivity.this, MainActivity.class);					
        			startActivity(intent);
        		}
            }
        }, SPLASH_DISPLAY_LENGHT);
		
		
	}
}
