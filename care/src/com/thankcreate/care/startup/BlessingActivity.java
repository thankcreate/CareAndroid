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
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Gallery.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;

public class BlessingActivity extends Activity {

	
	/**
	 * 注意，PER_SHOW_TIME - MIX_SHOW_TIME是渐隐的时间
	 * 改了任何一个，都要去anim里把它改掉
	 */
	final private int PER_SHOW_TIME = 8000; // 每张图显示的总时间
	final private int MIX_SHOW_TIME = 2000; // 两张图一起显示的时间(通过alpha混合在一起)
	final private int SLIDE_TIME = 300;
	
	private int mBkgIndex = 0;
	private int mItemIndex = 0;
	private List<Drawable> listDrawables;
	List<BlessItemViewModel> listItems;
	private BlessHelper blessHelper;
	
	private ImageView image1;
	private ImageView image2;
	
	private int activeFlag;
	
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
		layoutEnter.setAnimation(makeSliderLayoutInitAnimation());
		imageSlider = (ImageView) findViewById(R.id.blessing_enter_slider);
		
		textName = (TextView) findViewById(R.id.blessing_item_name);
		textContent = (TextView) findViewById(R.id.blessing_item_content);
		
		image1 = (ImageView) findViewById(R.id.blessing_image1);
		image2 = (ImageView) findViewById(R.id.blessing_image2);
		adjustImageView(image1);
		adjustImageView(image2);
		
		// 从缓存中加载图片和祝福项
		blessHelper = new BlessHelper();
		listDrawables = blessHelper.getBlessImages(this);	
		listItems = blessHelper.getCachedBlessPassedItem();
		
		
		refreshItemText();
		
		image1.setImageDrawable(listDrawables.get(0));
		image1.setAnimation(makeImageAnimation());
		activeFlag = 1;
		
		mBkgIndex = ++mBkgIndex % listDrawables.size();
		
		delayRefresh();
	}
	
	private Animation makeImageAnimation()
	{	
		AnimationSet res = new AnimationSet(true);
		
		DisplayMetrics metric = new DisplayMetrics();		
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		int adjustedHeight = (int) (metric.heightPixels * 1.2);
		int adjustedWidth = (int) (metric.widthPixels * 1.2);
		
		TranslateAnimation translate = new TranslateAnimation(
				(int) (adjustedWidth * 0.16), (int) (adjustedWidth * 0.17), 
				(int) (adjustedHeight * 0.16), (int) (adjustedHeight * 0.17));
		translate.setDuration(12000);
		
		ScaleAnimation scale = new ScaleAnimation((float)1.0, (float)0.87, (float)1.0, (float)0.87);
		scale.setDuration(12000);
		
		AlphaAnimation alphaStart = new AlphaAnimation(0, 1);
		alphaStart.setDuration(MIX_SHOW_TIME);
		
		AlphaAnimation alphaEnd = new AlphaAnimation(1, 0);
		alphaEnd.setStartOffset(PER_SHOW_TIME - MIX_SHOW_TIME);
		alphaEnd.setDuration(MIX_SHOW_TIME);
		
		res.addAnimation(translate);
		res.addAnimation(scale);
		res.addAnimation(alphaStart);
		res.addAnimation(alphaEnd);
		
		res.setFillAfter(true);
		return res;
	}
	
	private Animation makeTextAnimation()
	{
		Animation res =  new AlphaAnimation(0,1);
		res.setFillAfter(true);
		res.setDuration(MIX_SHOW_TIME / 2);		
		return res;
	}
	
	private Animation makeSliderLayoutInitAnimation()
	{
		Animation res =  new AlphaAnimation(0,1);
		res.setFillAfter(true);
		res.setDuration(1000);		
		return res;
	}
	
	private void refreshItemText()
	{
		BlessItemViewModel item = listItems.get(mItemIndex);
		textContent.setText(item.content);
		textContent.setAnimation(makeTextAnimation());
		textName.setText("— " + item.title);
		textName.setAnimation(makeTextAnimation());
		mItemIndex = ++mItemIndex % listItems.size();
	}
	
	private void delayRefresh()
	{
		final Handler handler=new Handler();
		Runnable runnable=new Runnable() {
		    @Override
		    public void run() {		
		    	if(activeFlag == 1)
		    	{
		    		activeFlag = 2;		    		
		    		image2.setImageDrawable(listDrawables.get(mBkgIndex));
		    		image2.setAnimation(makeImageAnimation());
		    	}
		    	else {
		    		activeFlag = 1;
		    		image1.setImageDrawable(listDrawables.get(mBkgIndex));
		    		image1.setAnimation(makeImageAnimation());
				}
				mBkgIndex = ++mBkgIndex % listDrawables.size();
				refreshItemText();
				handler.postDelayed(this, PER_SHOW_TIME - MIX_SHOW_TIME );
			}		
		};
		handler.postDelayed(runnable, PER_SHOW_TIME - MIX_SHOW_TIME);
	}

	public void adjustImageView(ImageView i) {
		
		i.setBackgroundColor(0x00000000);
		i.setScaleType(ImageView.ScaleType.CENTER_CROP);
		
		DisplayMetrics metric = new DisplayMetrics();		
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		int adjustedHeight = (int) (metric.heightPixels * 1.2);
		int adjustedWidth = (int) (metric.widthPixels * 1.2);		
		
		RelativeLayout.LayoutParams  params = new RelativeLayout.LayoutParams(adjustedWidth, adjustedHeight);		
		params.leftMargin = (int) (0 - (metric.widthPixels * 0.2));
		params.topMargin = (int) (0 - (metric.heightPixels * 0.2));
		
		i.setLayoutParams(params);	
		
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
