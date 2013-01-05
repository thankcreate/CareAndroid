package com.thankcreate.care.tool.ui;

import com.thankcreate.care.App;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

/**
 * 记得要在UI线程show
 * @author ThankCreate
 *
 */
public class ToastHelper {
	
	public static void show(final String content, final Boolean isTop)
	{
		if(content == null)
			return;	
		
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				innerShow(content, isTop);		
			}
		});
	}	

	
	public static void show(final String content)
	{
		if(content == null)
			return;	
		
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				innerShow(content, false);		
			}
		});
	}	

	
	public static void innerShow(String content, Boolean isTop)
	{		
		Toast t = Toast.makeText(App.getAppContext(),content, Toast.LENGTH_SHORT);
		if(isTop){
			t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, (int)(60 * App.density));
		}
		t.show();
	}
}
