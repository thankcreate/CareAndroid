package com.thankcreate.care;

import java.util.ArrayList;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.thankcreate.care.viewmodel.ItemViewModel;
import com.thankcreate.care.viewmodel.MainViewModel;
import com.thankcreate.care.viewmodel.PictureItemViewModel;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class BaseActivity extends Activity {
	
	protected void onSaveInstanceState(Bundle icicle) {
		super.onSaveInstanceState(icicle);
		icicle.putSerializable("cacheMainViewModel", App.mainViewModel);
		icicle.putBoolean("memoryCleaned", true);
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			App.mainViewModel = (MainViewModel) savedInstanceState
					.getSerializable("cacheMainViewModel");
			App.memoryCleaned = savedInstanceState.getBoolean("memoryCleaned", false);
			App.setAppContext(getApplicationContext());
		}
	}
	
	protected void addActionBarBackButton(ActionBar actionBar) {
		actionBar.addActionLeft(new Action() {
			@Override
			public void performAction(View view) {
				finish();
			}
			
			@Override
			public int getDrawable() {
				return R.drawable.thumb_back;
			}
		});
	}
	
	// 友盟的API规定所有Activity必须在onResume里和onPause里都写上下面的东西
	protected void onResume() {
	    super.onResume();
	    MobclickAgent.onResume(this);
	}
	protected void onPause() {
	    super.onPause();
	    MobclickAgent.onPause(this);
	}
}
