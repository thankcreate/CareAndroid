package com.thankcreate.care;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.thankcreate.care.R;
import com.thankcreate.care.account.AccountActivity;
import com.thankcreate.care.lab.LabActivity;
import com.thankcreate.care.picture.PictureWallActivity;
import com.thankcreate.care.preference.PreferenceActivity;
import com.thankcreate.care.status.StatusTimelineActivity;
import com.thankcreate.care.tool.misc.BlessHelper;
import com.thankcreate.care.tool.misc.StringTool;
import com.thankcreate.care.tool.ui.RefreshViewerHelper;
import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setTabs();
		setDensity();
		
		fetchBlessing();
	}

	BlessHelper blessHelper;
	private void fetchBlessing() {		
		blessHelper = new BlessHelper();
		blessHelper.cacheBlessImages();
		blessHelper.cacheBlessPassedItem();
	}

	protected void onResume() {
	    super.onResume();
	    // 父就别加了，否则统计有误，这统计很有问题
	    //MobclickAgent.onResume(this);
	}
	protected void onPause() {
	    super.onPause();
	    //MobclickAgent.onPause(this);
	}
	
	private void setDensity() {
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		App.density = metric.density;
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return false;
	}
	
	private void setTabs()
	{
		addTab("主页", R.drawable.tab_home_selector, StatusTimelineActivity.class);
		addTab("图片", R.drawable.tab_picture_selector, PictureWallActivity.class);
		addTabMiddle("实验室", R.drawable.tab_microscope_selector, LabActivity.class);
		addTab("帐号", R.drawable.tab_account_selector, AccountActivity.class);
		addTab("设置", R.drawable.tab_settings_selector, PreferenceActivity.class);
		
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
		final Editor editor = pref.edit();
		final String firstInMain = pref.getString("Global_FirstInMainActivity", "");		
		getTabWidget().setBackgroundResource(R.color.transparent);
		getTabHost().setBackgroundResource(R.color.transparent);
		// 如果是第一次进入，先进帐号设置页
		if(StringTool.isNullOrEmpty(firstInMain))
		{
			editor.putString("Global_FirstInMainActivity", "WhatEver");
			editor.commit();	
			getTabHost().setCurrentTab(3);
		}
		else
		{
			getTabHost().setCurrentTab(0);
		}
		
		getTabWidget().setDrawingCacheBackgroundColor(0);		
	}
	
	private void addTab(String labelId, int drawableId, Class<?> c)
	{
		TabHost tabHost = getTabHost();
		Intent intent = new Intent(this, c);
		TabHost.TabSpec spec = tabHost.newTabSpec("tab" + labelId);	
		
		View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, getTabWidget(), false);
		TextView title = (TextView) tabIndicator.findViewById(R.id.title);
		title.setText(labelId);
		ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
		icon.setImageResource(drawableId);
		
		spec.setIndicator(tabIndicator);
		spec.setContent(intent);
		
		tabHost.addTab(spec);		
		tabHost.setBackgroundResource(R.color.transparent);
	}
	
	private void addTabMiddle(String labelId, int drawableId, Class<?> c)
	{
		TabHost tabHost = getTabHost();
		Intent intent = new Intent(this, c);
		TabHost.TabSpec spec = tabHost.newTabSpec("tab" + labelId);	
		
		View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator_middle, getTabWidget(), false);
		
		ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
		icon.setImageResource(drawableId);
		
		spec.setIndicator(tabIndicator);
		spec.setContent(intent);
		
		tabHost.addTab(spec);
		tabHost.setBackgroundResource(R.color.transparent);		
	}
	
	
    public static Intent createIntent(Context context) {
        Intent i = new Intent(context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return i;
    }
	

}
