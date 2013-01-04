package com.thankcreate.care;

import java.io.File;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;

import com.renren.api.connect.android.Renren;
import com.thankcreate.care.tool.ui.DrawableManager;
import com.thankcreate.care.viewmodel.MainViewModel;
import com.weibo.sdk.android.Weibo;

public class App extends Application {
	
	private static Context context;
	
	private static Weibo sinaWeibo;	
	private static Renren renren;
	public static MainViewModel mainViewModel;
	public static DrawableManager drawableManager = new DrawableManager();
	public static float density = 1;
	public static Boolean memoryCleaned = false;
	
	
	
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
	
	
	/**
	 * 虽说一开始就初始化过，但如果切出去后内存不足，把这个释放了就完蛋了
	 * 所以每次必须用get方法，判断null
	 * @return
	 */
	public static Weibo getSinaWeibo() {		
		if(sinaWeibo == null)
			sinaWeibo = Weibo.getInstance(AppConstants.SINAWEIBO_APP_KEY, AppConstants.SINAWEIBO_REDIRECT_URL);
		return sinaWeibo;
	}

	public static Renren getRenren() {
		if(renren == null)
			renren = new Renren(AppConstants.RENREN_APP_KEY
					, AppConstants.RENREN_SECRET_KEY
					, AppConstants.RENREN_APP_ID
					, getAppContext());
		return renren;
	}

	public static Context getAppContext() {		
        return context;
    }
	
	public static void setAppContext(Context c)
	{
		context = c;
	}
}
