package com.thankcreate.care;


import com.markupartist.android.widget.ActionBar;
import com.thankcreate.care.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.AbstractAction;
import com.markupartist.android.widget.ActionBar.IntentAction;
/**
 * @author Adil Soomro
 *
 */
public class ArrowsActivity extends Activity {
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.arrowspage);
        
        final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        //actionBar.setHomeAction(new IntentAction(this, MainActivity.createIntent(this), R.drawable.tab_account));
        //actionBar.setHomeLogo(R.drawable.tab_account);
        actionBar.setTitle("我只在乎你");
//        actionBar.addActionRight(new IntentAction(this, new Intent(this, MainActivity.class), R.drawable.tab_picture));
        actionBar.addActionLeft(new IntentAction(this, new Intent(this, MainActivity.class), R.drawable.tab_home));
//        actionBar.SetTitleLogo( R.drawable.tab_account);	
        actionBar.setProgressBarVisibility(View.VISIBLE);
    }
}