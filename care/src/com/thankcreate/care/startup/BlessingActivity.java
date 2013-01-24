package com.thankcreate.care.startup;

import java.util.List;

import com.thankcreate.care.R;
import com.thankcreate.care.R.layout;
import com.thankcreate.care.R.menu;
import com.thankcreate.care.tool.misc.BlessHelper;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Gallery.LayoutParams;
import android.widget.ViewSwitcher.ViewFactory;

public class BlessingActivity extends Activity implements ViewFactory {

	private ImageSwitcher imageSwitcher;
	
	final private int PER_SHOW_TIME = 7000;
	
	private int mIndex = 0;
	private List<Bitmap> listBitmaps;
	private BlessHelper blessHelper;
	
	private ImageView testView;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blessing);
		initControl();
	}

	private void initControl() {
		imageSwitcher = (ImageSwitcher) findViewById(R.id.blessing_image_switcher);
		imageSwitcher.setFactory(this);
		imageSwitcher.setInAnimation(AnimationUtils.loadAnimation(this,
				R.anim.fade_in));
		
		
		blessHelper = new BlessHelper();
		listBitmaps = blessHelper.getBlessImages();
		BitmapDrawable bd = new BitmapDrawable(getResources(), listBitmaps.get(0));
		imageSwitcher.setImageDrawable(bd);
		mIndex = ++mIndex % listBitmaps.size();
		
		delayRefresh();
	}
	
	private void delayRefresh()
	{
		final Handler handler=new Handler();
		Runnable runnable=new Runnable() {
		    @Override
		    public void run() {				
				BitmapDrawable bd = new BitmapDrawable(getResources(), listBitmaps.get(mIndex));
				imageSwitcher.setImageDrawable(bd);
				mIndex = ++mIndex % listBitmaps.size();			
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

}
