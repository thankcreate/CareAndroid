package com.thankcreate.care;

import com.thankcreate.care.viewmodel.MainViewModel;
import com.weibo.sdk.android.Weibo;

public class App {
	
	private static App app = null;
	
	public Weibo sinaWeibo;
	public MainViewModel mainViewModel;
	
	public static App getInstance()
	{
		if(app == null)
		{
			app = new App();
			app.init();
		}
		return app;
	}
	
	private void init()
	{
		sinaWeibo = Weibo.getInstance(AppConstants.SINAWEIBO_APP_KEY, AppConstants.SINAWEIBO_REDIRECT_URL);
		mainViewModel = new MainViewModel();
	}

}
