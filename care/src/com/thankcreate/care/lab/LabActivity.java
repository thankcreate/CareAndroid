package com.thankcreate.care.lab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.youmi.android.AdView;

import com.buuuk.android.gallery.ImageViewFlipper;
import com.markupartist.android.widget.ActionBar;
import com.thankcreate.care.App;
import com.thankcreate.care.BaseActivity;
import com.thankcreate.care.R;
import com.thankcreate.care.R.layout;
import com.thankcreate.care.R.menu;
import com.thankcreate.care.password.PasswordSetActivity;
import com.thankcreate.care.picture.PictureWallActivity;
import com.thankcreate.care.preference.PreferenceActivity;

import android.os.Bundle;
import android.app.Activity;
import android.app.LauncherActivity.ListItem;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class LabActivity extends BaseActivity {
	private ActionBar actionBar;
	private GridView gridView;	
	private Class[] activities;
	private ImageView[] imageViews = {null, null, null, null, null, null};	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lab_lab);
		initActionBar();
		initControl();
		
	}
	
	private void initActionBar()
	{
		actionBar  = (ActionBar) findViewById(R.id.actionbar);     
        actionBar.setTitle("奇怪的地方");    
        actionBar.SetTitleLogo(R.drawable.tab_microscope);
	}
	
	private void initControl()
	{
		activities = new Class[]{
				LabTimelineActivity.class,
				LabCharactorAnalysisActivity.class,
				LabPercentageActivity.class,
				LabEnemyActivity.class,
				LabCatActivity.class,
				LabSmartChatActivity.class
			};
		imageViews[0] = (ImageView) findViewById(R.id.lab_item_timeline);
		imageViews[1] = (ImageView) findViewById(R.id.lab_item_charactor_analysis);
		imageViews[2] = (ImageView) findViewById(R.id.lab_item_percentage);
		imageViews[3] = (ImageView) findViewById(R.id.lab_item_enemy);
		imageViews[4] = (ImageView) findViewById(R.id.lab_item_cat);
		imageViews[5] = (ImageView) findViewById(R.id.lab_item_smart_chat);
		for(int i = 0; i < activities.length; i++)
		{
			imageViews[i].setOnClickListener(new LabItemOnClickListner(i));
		}
		
		LinearLayout adViewLayout = (LinearLayout) findViewById(R.id.lab_ad);
		adViewLayout.addView(new AdView(this),
		new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
		LinearLayout.LayoutParams.WRAP_CONTENT));
	}
	
	class LabItemOnClickListner implements OnClickListener{

		int index;
		
		public LabItemOnClickListner(int index) {
			super();
			this.index = index;
		}

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(LabActivity.this, activities[index]);					
			startActivity(intent);
		}
		
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_lab, menu);
		return true;
	}	
}
