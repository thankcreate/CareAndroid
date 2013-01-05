package com.thankcreate.care.rss;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.thankcreate.care.BaseActivity;
import com.thankcreate.care.R;
import com.thankcreate.care.R.layout;
import com.thankcreate.care.R.menu;
import com.thankcreate.care.status.StatusAddCommentActivity;
import com.thankcreate.care.status.StatusDetailActivity;
import com.thankcreate.care.viewmodel.ItemViewModel;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

public class RssDetailActivity extends BaseActivity {

	private ActionBar actionBar;
	private WebView webView;
	private ItemViewModel itemViewModel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rss_detail);
		parseIntent();
		initActionBar();
		initControl();
		initControlContent();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_rss_detail, menu);
		return true;
	}
	
	private void parseIntent()
	{
		Intent it= this.getIntent();		
		itemViewModel =(ItemViewModel) it.getSerializableExtra("itemViewModel");
		if(itemViewModel == null)
			finish();
	}

	private void initActionBar() {
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("详情");
		actionBar.addActionRight(new Action() {			
			@Override
			public void performAction(View view) {				
				Intent intent = new Intent();        
				intent.setAction("android.intent.action.VIEW");    
				Uri content_url = Uri.parse(itemViewModel.originalURL);   
				intent.setData(content_url);  
				startActivity(intent);
			}
			
			@Override
			public int getDrawable() {
				return R.drawable.thumb_ie;				
			}
		});
		addActionBarBackButton(actionBar);
	}
	
	private void initControl() {
		webView = (WebView) findViewById(R.id.rss_webview);
	}

	
	private void initControlContent(){
		webView.getSettings().setDefaultTextEncodingName("utf-8") ;
		webView.loadDataWithBaseURL(itemViewModel.originalURL, itemViewModel.rssSummary, "text/html", "utf-8", null);		
	}
}
