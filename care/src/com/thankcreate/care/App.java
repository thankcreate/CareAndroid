package com.thankcreate.care;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;

import com.thankcreate.care.tool.ui.DrawableManager;
import com.thankcreate.care.viewmodel.MainViewModel;
import com.weibo.sdk.android.Weibo;

public class App  extends Application {
	
	private static Context context;
	
	public static Weibo sinaWeibo;
	public static MainViewModel mainViewModel;
	public static DrawableManager drawableManager = new DrawableManager();
	public static float density = 1;
	public void onCreate(){
        super.onCreate();
        init();
    }
	
	private void init()
	{
		context = getApplicationContext();
		sinaWeibo = Weibo.getInstance(AppConstants.SINAWEIBO_APP_KEY, AppConstants.SINAWEIBO_REDIRECT_URL);
		mainViewModel = new MainViewModel();
		
		
	}
	
	public static Context getAppContext() {
        return context;
    }
}
