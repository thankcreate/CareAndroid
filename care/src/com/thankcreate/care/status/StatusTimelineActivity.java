package com.thankcreate.care.status;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.thankcreate.care.R;
import com.thankcreate.care.R.drawable;
import com.thankcreate.care.R.layout;
import com.thankcreate.care.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;

public class StatusTimelineActivity extends Activity {
	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_status_timeline);
		initActionBar();
	}

	private void initActionBar() {
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("我只在乎你");
		actionBar.addActionLeft(new Action() {
			
			@Override
			public void performAction(View view) {				
				
			}
			
			@Override
			public int getDrawable() {				
				return drawable.thumb_write_new;
			}
		});

		actionBar.addActionRight(new Action() {

			@Override
			public void performAction(View view) {

			}

			@Override
			public int getDrawable() {
				return drawable.thumb_refresh;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_status_timeline, menu);
		return true;
	}

}
