package com.thankcreate.care.rss;

import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReaderException;

import com.markupartist.android.widget.ActionBar;
import com.thankcreate.care.App;
import com.thankcreate.care.AppConstants;
import com.thankcreate.care.BaseActivity;
import com.thankcreate.care.R;
import com.thankcreate.care.R.layout;
import com.thankcreate.care.R.menu;
import com.thankcreate.care.tool.misc.PreferenceHelper;
import com.thankcreate.care.tool.misc.StringTool;
import com.thankcreate.care.tool.ui.ToastHelper;

import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnKeyListener;
import android.content.SharedPreferences.Editor;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RssSetActivity extends BaseActivity {

	private ActionBar actionBar;
	private RelativeLayout layoutConfirm;
	private EditText textInput;
	private TextView textSiteName;
	private TextView textDescription;
	
	private ProgressDialog mSpinner;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rss_set);
		initActionBar();
		initControl();
		initInformation();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_rss_set, menu);
		return true;
	}
	
	private void initActionBar()
	{		
		actionBar = (ActionBar) findViewById(R.id.actionbar);
        actionBar.setTitle("RSS订阅");       
        actionBar.SetTitleLogo(R.drawable.tab_account);
        addActionBarBackButton(actionBar);
	}
	
	private void initControl()
	{
		layoutConfirm = (RelativeLayout) findViewById(R.id.rss_confirm);
		layoutConfirm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				confirmClicked();
			}
		});
		textInput = (EditText) findViewById(R.id.rss_input);
		textSiteName = (TextView) findViewById(R.id.rss_title);
		textDescription = (TextView) findViewById(R.id.rss_description);
		
		mSpinner = new ProgressDialog(RssSetActivity.this);
		mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mSpinner.setMessage("Loading...");
		mSpinner.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				mSpinner.dismiss();
				return false;
			}

		});
	}
	
	private void initInformation()
	{
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
    	Editor editor = pref.edit();    	
    	String url = pref.getString("RSS_FollowerPath", "");
    	String title = pref.getString("RSS_FollowerSiteTitle", "");
    	String description= pref.getString("RSS_FollowerDescription", "");
    	textInput.setText(url);
    	textInput.setSelection(0);
    	textInput.clearFocus();
    	textSiteName.setText(title);
    	textDescription.setText(description);
	}
	
	private void confirmClicked()
	{
		InputMethodManager imm = (InputMethodManager)getSystemService(
			      Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(textInput.getWindowToken(), 0);
		
		String url = textInput.getText().toString();
		if(StringTool.isNullOrEmpty(url))
		{
			ToastHelper.show("据说要智商超过250才能看到大王您写的字么？", true);
			return;
		}
		final String fullURL;
		if(!url.startsWith("http"))
		{
			fullURL = "http://" + url;
		}
		else {
			fullURL = url;
		}
		mSpinner.show();
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				RSSReader rssReader = new RSSReader();				
				try {
					final RSSFeed rssFeed = rssReader.load(fullURL);
					// 即使输入的是一个非rss的地址，有时也会不报异常，所以要判断getItems有没有值
					if(rssFeed.getItems() == null || rssFeed.getItems().size() == 0)
					{
						ToastHelper.show("输入地址无效，或者当前网络连接不可用", true);
						actionBar.post(new Runnable() {					
							@Override
							public void run() {
								mSpinner.dismiss();						
							}
						});
						return;
					}
					textDescription.post(new Runnable() {
						
						@Override
						public void run() {
							textDescription.setText(rssFeed.getDescription());
							textSiteName.setText(rssFeed.getTitle());
							textInput.setText(fullURL);
						}
					});
					SharedPreferences pref = App.getAppContext().getSharedPreferences(
							AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
			    	Editor editor = pref.edit();    	
			    	editor.putString("RSS_FollowerPath", fullURL);
			    	editor.putString("RSS_FollowerSiteTitle", rssFeed.getTitle());
			    	editor.putString("RSS_FollowerDescription", rssFeed.getDescription());
			    	editor.commit();
			    	App.mainViewModel.isChanged = true;
				} catch (Exception e) {
					ToastHelper.show("输入地址无效，或者当前网络连接不可用", true);
				}				
				actionBar.post(new Runnable() {					
					@Override
					public void run() {
						mSpinner.dismiss();						
					}
				});
			}
		}).start();
		
	}

}
