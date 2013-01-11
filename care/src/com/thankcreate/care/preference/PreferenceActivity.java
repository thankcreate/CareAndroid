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
import com.thankcreate.care.service.NewsPollingService;
import com.thankcreate.care.tool.misc.MathTool;
import com.thankcreate.care.tool.misc.PreferenceHelper;
import com.umeng.fb.UMFeedbackService;

import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.R.integer;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
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
	private ToggleButton toggleUsePolling;
	private RelativeLayout layoutUsePolling;
	private RelativeLayout layoutGotoSiteLayout;
	private RelativeLayout layoutFeedback;
	private RelativeLayout layoutAboutLayout;

	private int pollingSettingSelected = 0;
	private long miliArray[] = { 10 * 60 * 1000, 20 * 60 * 1000, 30 * 60 * 1000, 60 * 60 * 1000, 0};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preference_preference);
		initActionBar();
		initControl();
		
	}

	private void initPollingSettingSelected() {
    	SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
    	long savedPollingTime = pref.getLong("Global_PollingTime", 30 * 60 * 1000);
    	for(int i = 0; i < miliArray.length; i++)
    	{
    		if(miliArray[i] == savedPollingTime)
    		{
    			pollingSettingSelected = i;
    			return;
    		}
    	}
    	// 如果全都不匹配，设为不轮询
    	pollingSettingSelected = miliArray.length - 1;
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
		return false;
	}

	private void initControl() {
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
				if (isChecked) {
					// 这里不能直接设为true，要等真正把密码设置正确了，才能置为true
					// edit.putString("Global_UsePassword", "True");
					Intent intent = new Intent();
					intent.setClass(PreferenceActivity.this,
							PasswordSetActivity.class);
					startActivity(intent);
				} else
					edit.putString("Global_UsePassword", "False");
				edit.commit();
			}
		});


		// 是否显示转发图
		toggleUseSharedPicture = (ToggleButton) findViewById(R.id.preference_show_forward_pic_toggle);
		toggleUseSharedPicture.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ToggleButton btn = (ToggleButton) v;
				boolean isChecked = btn.isChecked();
				if (isChecked)
					edit.putString("Global_NeedFetchImageInRetweet", "True");
				else
					edit.putString("Global_NeedFetchImageInRetweet", "False");
				edit.commit();
			}
		});
		
		// 推送
		layoutUsePolling = (RelativeLayout) findViewById(R.id.preference_use_polling_layout);
		layoutUsePolling.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				usePollingLayoutClicked();
			}
		});
		toggleUsePolling = (ToggleButton) findViewById(R.id.preference_use_polling_toggle);
		toggleUsePolling.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ToggleButton btn = (ToggleButton) v;
				boolean isChecked = btn.isChecked();
				setPolling(isChecked);
			}
		});

		// 访问网站
		layoutGotoSiteLayout = (RelativeLayout) findViewById(R.id.preference_goto_site);
		layoutGotoSiteLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction("android.intent.action.VIEW");
				Uri content_url = Uri
						.parse("http://thankcreate.github.com/Care/");
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
				intent.setClass(PreferenceActivity.this,
						PreferenceAboutActivity.class);
				startActivity(intent);
			}
		});
	}

	private void initControlContent() {
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
		String userPassword = pref.getString("Global_UsePassword", "False");
		if (userPassword.equalsIgnoreCase("True")) {
			toggleUsePassword.setChecked(true);
		} else {
			toggleUsePassword.setChecked(false);
		}
		
		String usePolling = pref.getString("Global_UsePolling", "True");
		if(usePolling.equalsIgnoreCase("True")) {
			toggleUsePolling.setChecked(true);
		} else {
			toggleUsePolling.setChecked(false);
		}

		String useSharedPicture = pref.getString(
				"Global_NeedFetchImageInRetweet", "True");
		if (useSharedPicture.equalsIgnoreCase("True")) {
			toggleUseSharedPicture.setChecked(true);
		} else {
			toggleUseSharedPicture.setChecked(false);
		}
	}

	// 这个函数是从用户点击toggle的事件中进来的
	private void setPolling(boolean needPolling) {
		PendingIntent alarmSender;
		alarmSender = PendingIntent.getService(this, 0, new Intent(this,
				NewsPollingService.class), 0);
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
		
		Editor editor = pref.edit();
		if (needPolling) {
			editor.putString("Global_UsePolling", "True");
			editor.putLong("Global_PollingTime", AppConstants.DEFAULT_POLLING_INTERVAL);
			editor.commit();
			long interval = AppConstants.DEFAULT_POLLING_INTERVAL;
			long firstTime = SystemClock.elapsedRealtime() + interval;
			am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
	                firstTime , interval, alarmSender);
		} else {
			editor.putString("Global_UsePolling", "False");
			editor.putLong("Global_PollingTime", 0);
			editor.commit();
			am.cancel(alarmSender);
		}
	}
	
	

	private void usePollingLayoutClicked()
	{
		initPollingSettingSelected();
		new AlertDialog.Builder(this)
		.setIcon(R.drawable.thumb_clock)
		.setTitle("设置查询间隔")
		.setSingleChoiceItems(R.array.array_polling_time,
				pollingSettingSelected, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						pollingSettingSelected = whichButton;
					}
				})
		.setPositiveButton(R.string.dialog_confirm,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						setPollingTime(miliArray[pollingSettingSelected]);
					}
				})
		.setNegativeButton(R.string.dialog_cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
					}
				}).create().show();
	}
	
	/**
	 * 0 说明是不轮询
	 */
	private void setPollingTime(Long time)
	{
		PendingIntent alarmSender;
		alarmSender = PendingIntent.getService(this, 0, new Intent(this,
				NewsPollingService.class), 0);
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);		
		Editor editor = pref.edit();
		if(time == 0)
		{
			editor.putString("Global_UsePolling", "False");
			editor.putLong("Global_PollingTime", 0);
			toggleUsePolling.setChecked(false);
			am.cancel(alarmSender);		
		}
		else
		{
			editor.putString("Global_UsePolling", "True");
			editor.putLong("Global_PollingTime", time);
			toggleUsePolling.setChecked(true);
			long firstTime = SystemClock.elapsedRealtime() + time;
			am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
	                firstTime, time, alarmSender);			
		}
		editor.commit();
	}
}
