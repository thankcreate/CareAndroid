package com.thankcreate.care.lab;

import com.markupartist.android.widget.ActionBar;
import com.thankcreate.care.R;
import com.thankcreate.care.R.layout;
import com.thankcreate.care.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class LabActivity extends Activity {
	private ActionBar actionBar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lab_lab);
		initActionBar();
	}
	
	private void initActionBar()
	{
		actionBar  = (ActionBar) findViewById(R.id.actionbar);     
        actionBar.setTitle("奇怪的地方");    
        actionBar.SetTitleLogo(R.drawable.tab_microscope);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_lab, menu);
		return true;
	}

}
