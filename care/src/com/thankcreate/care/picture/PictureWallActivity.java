package com.thankcreate.care.picture;

import com.markupartist.android.widget.ActionBar;
import com.thankcreate.care.R;
import com.thankcreate.care.R.layout;
import com.thankcreate.care.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class PictureWallActivity extends Activity {
	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture_picture_wall);
		initActionBar();
	}

	private void initActionBar() {
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("图片");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_picture_wall, menu);
		return true;
	}

}
