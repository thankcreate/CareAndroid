package com.thankcreate.care;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.thankcreate.care.R;
import com.thankcreate.care.account.AccountActivity;
import com.thankcreate.care.lab.LabActivity;
import com.thankcreate.care.picture.PictureWallActivity;
import com.thankcreate.care.preference.PreferenceActivity;
import com.thankcreate.care.status.StatusTimelineActivity;

import android.os.Bundle;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
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
		
		setNavigateBar();
		setTabs();
	}

	private void setNavigateBar() {
		
//		actionBar.addAction(new Action() {
//            @Override
//            public void performAction(View view) {
//                Toast.makeText(MainActivity.this, "Added action.", Toast.LENGTH_SHORT).show();
//            }
//            @Override
//            public int getDrawable() {
//                return R.drawable.ic_title_share_default;
//            }
//        });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	private void setTabs()
	{
		addTab("主页", R.drawable.tab_home, StatusTimelineActivity.class);
		addTab("图片", R.drawable.tab_picture, PictureWallActivity.class);
		
		addTab("奇怪的地方", R.drawable.tab_microscope, LabActivity.class);
		addTab("帐号", R.drawable.tab_account, AccountActivity.class);
		addTab("设置", R.drawable.tab_settings, PreferenceActivity.class);
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
	}
	
	
    public static Intent createIntent(Context context) {
        Intent i = new Intent(context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return i;
    }
	

}
