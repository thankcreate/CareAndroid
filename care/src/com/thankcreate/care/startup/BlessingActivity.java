package com.thankcreate.care.startup;

import java.util.Date;
import java.util.List;
import java.util.Random;

import com.thankcreate.care.App;
import com.thankcreate.care.AppConstants;
import com.thankcreate.care.DispatcherActivity;
import com.thankcreate.care.MainActivity;
import com.thankcreate.care.R;
import com.thankcreate.care.R.layout;
import com.thankcreate.care.R.menu;
import com.thankcreate.care.password.PasswordActivity;
import com.thankcreate.care.tool.misc.BlessHelper;
import com.thankcreate.care.viewmodel.BlessItemViewModel;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Gallery.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;

public class BlessingActivity extends Activity implements ViewFactory {

	private ImageSwitcher imageSwitcher;
	
	final private int PER_SHOW_TIME = 7000;
	final private int SLIDE_TIME = 2000;
	
	private int mBkgIndex = 0;
	private int mItemIndex = 0;
	private List<Drawable> listDrawables;
	List<BlessItemViewModel> listItems;
	private BlessHelper blessHelper;
	
	
	private RelativeLayout layoutEnter;
	private ImageView imageSlider;
	private TextView textName;
	private TextView textContent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blessing);
		initControl();
	}

	private void initControl() {
		layoutEnter = (RelativeLayout) findViewById(R.id.blessing_enter_layout);
		layoutEnter.setOnClickListener(mOnClickListener);
		imageSlider = (ImageView) findViewById(R.id.blessing_enter_slider);
		
		imageSwitcher = (ImageSwitcher) findViewById(R.id.blessing_image_switcher);
		imageSwitcher.setFactory(this);
		
		Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		fadeInAnimation.setFillAfter(true);
		
		imageSwitcher.setInAnimation(fadeInAnimation);		
		
		
		textName = (TextView) findViewById(R.id.blessing_item_name);
		textContent = (TextView) findViewById(R.id.blessing_item_content);
		
		
		// 从缓存中加载图片和祝福项
		blessHelper = new BlessHelper();
		listDrawables = blessHelper.getBlessImages(this);	
		listItems = blessHelper.getCachedBlessPassedItem();
		
		refreshItemText();
		imageSwitcher.setImageDrawable(listDrawables.get(0));
		mBkgIndex = ++mBkgIndex % listDrawables.size();
		
		delayRefresh();
	}
	
	private void refreshItemText()
	{
		BlessItemViewModel item = listItems.get(mItemIndex);
		textContent.setText(item.content);
		textName.setText("— " + item.title);
		mItemIndex = ++mItemIndex % listItems.size();
	}
	
	private void delayRefresh()
	{
		final Handler handler=new Handler();
		Runnable runnable=new Runnable() {
		    @Override
		    public void run() {		
				imageSwitcher.setImageDrawable(listDrawables.get(mBkgIndex));
				mBkgIndex = ++mBkgIndex % listDrawables.size();
				refreshItemText();
				handler.postDelayed(this, PER_SHOW_TIME);
			}		
		};
		handler.postDelayed(runnable, PER_SHOW_TIME);
	}

	@Override
	public View makeView() {
		ImageView i = new ImageView(this);
		i.setBackgroundColor(0xFF000000);
		i.setScaleType(ImageView.ScaleType.CENTER_CROP);
		
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		int adjustedHeight = (int) (metric.heightPixels * 1.2);
		int adjustedWidth = (int) (metric.widthPixels * 1.2);		
		
		ImageSwitcher.LayoutParams params = new ImageSwitcher.LayoutParams(
				adjustedWidth, adjustedHeight);
		params.leftMargin = (int) (0 - (metric.widthPixels * 0.2));
		params.topMargin = (int) (0 - (metric.heightPixels * 0.2));
		
		i.setLayoutParams(params);
		return i;
	}
	
	private OnClickListener mOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// 做animation
			DisplayMetrics metric = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metric);
			int xDelta = (int) (metric.density * (80 - 30));
			TranslateAnimation slideAnimation = new TranslateAnimation(0, xDelta, 0, 0);
			slideAnimation.setDuration(SLIDE_TIME);
			slideAnimation.setFillAfter(true);
			imageSlider.setAnimation(slideAnimation);
			
			
			
			
			AlphaAnimation test = new AlphaAnimation(1, 0);
			test.setDuration(SLIDE_TIME);
			slideAnimation.setFillAfter(true);	
			
			imageSlider.setAnimation(test);
			
			// 延时做跳转
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {					
					SharedPreferences pref = App.getAppContext().getSharedPreferences(
							AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
					final String usePassword = pref.getString("Global_UsePassword", "False");
					if (usePassword.equalsIgnoreCase("True"))
		    		{
		    			Intent intent = new Intent();
		    			intent.setClass(BlessingActivity.this, PasswordActivity.class);					
		    			startActivity(intent);
		    		}
		        	// 不是第一次启动，且没有设置密码
		    		else {
		    			Intent intent = new Intent();
		    			intent.setClass(BlessingActivity.this, MainActivity.class);					
		    			startActivity(intent);
		    		}
				}
			}, SLIDE_TIME);
		}
	};

}
