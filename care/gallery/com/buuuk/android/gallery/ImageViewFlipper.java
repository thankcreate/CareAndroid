package com.buuuk.android.gallery;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.buuuk.android.ui.touch.TouchActivity;
import com.buuuk.android.ui.touch.WrapMotionEvent;
import com.buuuk.android.util.FileUtils;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.thankcreate.care.App;
import com.thankcreate.care.R;
import com.thankcreate.care.tool.misc.DateTool;
import com.thankcreate.care.tool.ui.DrawableManager;
import com.thankcreate.care.tool.ui.DrawableManager.FetchDrawableCompleteListener;
import com.thankcreate.care.tool.ui.ToastHelper;
import com.thankcreate.care.viewmodel.EntryType;
import com.thankcreate.care.viewmodel.ItemViewModel;
import com.thankcreate.care.viewmodel.PictureItemViewModel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

@SuppressLint("SdCardPath")
public class ImageViewFlipper extends TouchActivity {
	
	private static final int EXIT = 0;
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private static final String DIRECTORY = "/sdcard/";
	private static final String DATA_DIRECTORY = "/sdcard/.ImageViewFlipper/";
	private static final String DATA_FILE = "/sdcard/.ImageViewFlipper/imagelist.dat";
	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;
	private Animation slideLeftIn;
	private Animation slideLeftOut;
	private Animation slideRightIn;
	private Animation slideRightOut;
	private ViewFlipper viewFlipper;
	private int currentView = 0;
	List<String> ImageList = new ArrayList();
	private int currentIndex = 0;
	private int maxIndex = 0;
	private ImageView currentImageView = null;
	
	private float mMinZoomScale=1;
	
	private DrawableManager drawableManager = new DrawableManager();
	FileOutputStream output = null;
	OutputStreamWriter writer = null;
	
	// Add by ThankCreate
	private ActionBar actionBar;
	private LinearLayout layoutProgress;	
	private LinearLayout layoutDescription;
	private TextView textDescription;
	private TextView textFrom;
	private TextView textTime;
	
	private Boolean isGallery = true;
	private String singleSrc = "";
	
	protected void onSaveInstanceState(Bundle icicle) {
		super.onSaveInstanceState(icicle);
		icicle.putSerializable("currentGalleryIndex",currentIndex);		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	

		
		setContentView(R.layout.activity_picture_detail);
		final ImageView iv = (ImageView) findViewById(R.id.zero);
		
		Log.i("count1", String.valueOf(App.mainViewModel.pictureItems.size()));
		Log.i("ImageListcount1", String.valueOf(ImageList.size()));
		if(App.mainViewModel.pictureItems == null || App.mainViewModel.pictureItems.size() == 0)
			finish();
		
		for (PictureItemViewModel pic : App.mainViewModel.pictureItems) {
			// 新浪微博的largeURL实在太大了，这里只用mid就行了
			if(pic.type == EntryType.SinaWeibo)
			{
				ImageList.add(pic.middleURL);	
			}
			// 人人
			else if(pic.type == EntryType.Renren)
			{
				ImageList.add(pic.largeURL);	
			}
			// 豆瓣
			else if(pic.type == EntryType.Douban)
			{
				ImageList.add(pic.middleURL);
			}
					
		}
		
		singleSrc = this.getIntent().getStringExtra("src");
		if(singleSrc == null) {
			isGallery = true;
			currentIndex = this.getIntent().getIntExtra("index", 0);
		}
		else { 
			isGallery = false;
		}
		
		if(savedInstanceState != null)
		{
			currentIndex = savedInstanceState.getInt("currentGalleryIndex", currentIndex);
		}
		
		initActionBar();
		initControl();
		
		maxIndex = ImageList.size() - 1;
		
		Log.v("currentIndex", "Index: "+currentIndex);

		viewFlipper = (ViewFlipper) findViewById(R.id.flipper);

		slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
		slideLeftOut = AnimationUtils
				.loadAnimation(this, R.anim.slide_left_out);
		slideRightIn = AnimationUtils
				.loadAnimation(this, R.anim.slide_right_in);
		slideRightOut = AnimationUtils.loadAnimation(this,
				R.anim.slide_right_out);

		viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this,
				android.R.anim.fade_in));
		viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,
				android.R.anim.fade_out));
		
		
		// 每次加载前都给tag设成url值，方便在返回时确定是这个图片是当前正在显示的那个，
		//iv.setTag(ImageList.get(currentIndex));
		Log.i("count2", String.valueOf(App.mainViewModel.pictureItems.size()));
		Log.i("ImageListcount2", String.valueOf(ImageList.size()));
		String src = "";
		if(isGallery)
			src = ImageList.get(currentIndex);
		else
			src = singleSrc;
		currentImageView = iv;
		drawableManager.fetchDrawableOnThreadWithCallback(src, iv, new FetchDrawableCompleteListener(){

			public void fetchComplete(Bitmap d) {
				layoutProgress.setVisibility(View.GONE);
				iv.setImageBitmap(d);
				resetImage(iv,iv.getDrawable());
			}
			
		});
		
		System.gc();
		
		gestureDetector = new GestureDetector(new MyGestureDetector());
		gestureListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (gestureDetector.onTouchEvent(event)) {
					return true;
				}
				return false;
			}
		};
		
	}
	private void initActionBar()
	{		
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("照片详情");
		actionBar.addActionRight(new Action() {
			
			@Override
			public void performAction(View view) {
				
				BitmapDrawable drawable = (BitmapDrawable) currentImageView.getDrawable();
				// 可能这个时候还没有加载完，所以要检测一下null
				if(drawable == null)
					return;
				Bitmap bmp = drawable.getBitmap();
				
				// 保存到相机文件夹
				File myDir = new File("/sdcard/DCIM/Camera/");
				myDir.mkdirs();

				Date date = new Date();
				String sigDate = DateTool.getDateSig(date);
				
				String fname = sigDate + ".jpg";
				File file = new File(myDir, fname);				
				
				try {
				       FileOutputStream out = new FileOutputStream(file);
				       bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
				} catch (Exception e) {
				       ToastHelper.show(">_< 由于未知原因，图片保存失败");
				}
			       ToastHelper.show("^_^ 图片已保存至" + file.toString(), true);
			}
			
			@Override
			public int getDrawable() {
				return R.drawable.thumb_save;
			}
		});
		addActionBarBackButton(actionBar);
	}
	
	/**
	 * initControl之前必须确保存intent已经解析过了
	 */
	private void initControl()
	{
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("照片详情");
		actionBar.SetTitleLogo(R.drawable.tab_picture);
		layoutProgress = (LinearLayout) findViewById(R.id.picture_detail_progress_layout);
		layoutProgress.setVisibility(View.VISIBLE);
		
		layoutDescription = (LinearLayout) findViewById(R.id.picture_detail_description_layout);
		textDescription = (TextView) findViewById(R.id.picture_detail_description_text);		
		textFrom = (TextView) findViewById(R.id.picture_detail_from_text);				
		textTime = (TextView) findViewById(R.id.picture_detail_time_text);
		
		if(isGallery)
			refreshDescriptionPart(currentIndex);
		else
			layoutDescription.setVisibility(View.GONE);
	}
	
	private void refreshDescriptionPart(int index)
	{
		try {
			PictureItemViewModel item = App.mainViewModel.pictureItems.get(currentIndex);
			textDescription.setText(item.description);
			textFrom.setText(EntryType.StringValueOf(item.type));
			textTime.setText(DateTool.convertDateToStringInShow(item.time));
		} catch (Exception e) {
			finish();
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		int NONE = Menu.NONE;
		menu.add(NONE, EXIT, NONE, "Exit");
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case EXIT:
			quit();
			break;
		}

		return super.onOptionsItemSelected(item);
	}
	
	// TODO
	protected void onPause() {
		super.onPause();

//		SharedPreferences indexPrefs = getSharedPreferences("currentIndex",
//				MODE_PRIVATE);
		
//		SharedPreferences.Editor indexEditor = indexPrefs.edit();
//		indexEditor.putInt("currentIndex", currentIndex);
//		indexEditor.commit();
	}
	
	protected void onResume() {
		super.onResume();
//		SharedPreferences indexPrefs = getSharedPreferences("currentIndex",
//				MODE_PRIVATE);
//		if (indexPrefs.contains("currentIndex")) {
//			currentIndex = indexPrefs.getInt("currentIndex", 0);
//		}	
	}

	class MyGestureDetector extends SimpleOnGestureListener {
		private int toggleCount = 0; 
		@Override
		public boolean onDoubleTap(final MotionEvent e){
			
			
	    	ImageView view = (ImageView)findViewById(R.id.zero);
			switch(currentView){
			case 0: view = (ImageView)findViewById(R.id.zero); break;
			case 1: view = (ImageView)findViewById(R.id.one); break;
			case 2:view = (ImageView)findViewById(R.id.two); break;				
			}
			 
			resetImage(view,view.getDrawable());
			return true;
		}
		
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			if(isGallery)
			{
				int state = layoutDescription.getVisibility();
				if(state == View.GONE)
					layoutDescription.setVisibility(View.VISIBLE);
				else
					layoutDescription.setVisibility(View.GONE);
			}			
			return super.onSingleTapUp(e);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			try {
				if(!isGallery)
					return false;
				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
					return false;
				// right to left swipe
				if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					viewFlipper.setInAnimation(slideLeftIn);
					viewFlipper.setOutAnimation(slideLeftOut);

					if (currentIndex == maxIndex) {
						currentIndex = 0;
					} else {
						currentIndex = currentIndex + 1;
					}
					
					// 清缓存，只保留左1张，当前1张，后1张
					if(currentIndex > 1)
					{
						for(int i = currentIndex - 2; i >= 0; --i)
						{
							drawableManager.removeFromCache(ImageList.get(i));
						}
					}
					System.gc();
					
					final ImageView iv;				
					
					if (currentView == 0) {
						currentView = 1;
						iv = (ImageView) findViewById(R.id.one);

						
					} else if (currentView == 1) {
						currentView = 2;
						 iv = (ImageView) findViewById(R.id.two);
						 
					
					} else {
						currentView = 0;
						iv = (ImageView) findViewById(R.id.zero);
					}					
					iv.setImageDrawable(null);
					currentImageView = iv;
					layoutProgress.setVisibility(View.VISIBLE);
					refreshDescriptionPart(currentIndex);
					drawableManager.fetchDrawableOnThreadWithCallback(ImageList.get(currentIndex), iv,new FetchDrawableCompleteListener() {
						
						@Override
						public void fetchComplete(Bitmap d) {
							layoutProgress.setVisibility(View.GONE);
							iv.setImageBitmap(d);
							System.gc();
							resetImage(iv,iv.getDrawable());	
							Log.v("ImageViewFlipper", "Current View: " + currentView);
							
						}
					});		
					viewFlipper.showNext();
					
					// 预加载后一张
					if(currentIndex < maxIndex)
					{
						drawableManager.fetchDrawableOnThreadWithDoNothing(ImageList.get(currentIndex + 1));
					}
					return true;
				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					viewFlipper.setInAnimation(slideRightIn);
					viewFlipper.setOutAnimation(slideRightOut);
					
					
					
					if (currentIndex == 0) {
						currentIndex = maxIndex;
					} else {
						currentIndex = currentIndex - 1;
					}
					
					// 清缓存，只保留左1张，当前1张，后1张
					if(currentIndex < maxIndex - 1)
					{
						for(int i = currentIndex + 2; i <= maxIndex; i++)
						{
							drawableManager.removeFromCache(ImageList.get(i));
						}						
					}
					System.gc();
					
					
					final ImageView iv;

					if (currentView == 0) {
						currentView = 2;
						iv = (ImageView) findViewById(R.id.two);						
					} else if (currentView == 2) {
						currentView = 1;
						iv = (ImageView) findViewById(R.id.one);						
					} else {
						currentView = 0;
						iv = (ImageView) findViewById(R.id.zero);						
					}
					currentImageView = iv;
					iv.setImageDrawable(null);
					layoutProgress.setVisibility(View.VISIBLE);
					refreshDescriptionPart(currentIndex);
					drawableManager.fetchDrawableOnThreadWithCallback(ImageList.get(currentIndex), iv,new FetchDrawableCompleteListener() {
						
						@Override
						public void fetchComplete(Bitmap d) {
							layoutProgress.setVisibility(View.GONE);
							iv.setImageBitmap(d);
							System.gc();
							
							resetImage(iv,iv.getDrawable());	
							Log.v("ImageViewFlipper", "Current View: " + currentView);
						}
					});
					viewFlipper.showPrevious();
					
					// 预加载前一张
					if(currentIndex > 0)
					{
						drawableManager.fetchDrawableOnThreadWithDoNothing(ImageList.get(currentIndex - 1));
					}
					return true;
				}
			} catch (Exception e) {
				// nothing
			}
			return false;
		}
		
	}
	
	@Override
	public void resetImage(ImageView iv, Drawable draw) {
		try {
			Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
			int rotation = display.getRotation();
			
			int orientation = 0;
			if( rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180)
				orientation = 0;
			else
				orientation = 1;
			
	        matrix = new Matrix();
	        matrix.setTranslate(1f, 1f);
	        float scale = 1;
	        
	        int controlHeight = viewFlipper.getHeight();
	        
	        mMinZoomScale = 1;
	        if( orientation==0 
	        		//&& (float)draw.getIntrinsicWidth() >  (float)getWindowManager().getDefaultDisplay().getWidth()
	        		) {
	        	
	        	scale = (float)(getWindowManager().getDefaultDisplay().getWidth() + 2)/(float)draw.getIntrinsicWidth();        	
	        	mMinZoomScale = scale;
	        	matrix.postScale(scale,scale);
	        
	        	iv.setImageMatrix(matrix);
	        }else if( orientation==1 
	        		//&& (float)draw.getIntrinsicHeight() >  (float)getWindowManager().getDefaultDisplay().getHeight()
	        		){
	        	//scale = (float)getWindowManager().getDefaultDisplay().getHeight()/(float)draw.getIntrinsicHeight();
	        	scale = (float)controlHeight/(float)draw.getIntrinsicHeight();
	        	mMinZoomScale = scale;
	        	matrix.postScale(scale,scale);
	        
	        	iv.setImageMatrix(matrix);
	        }
	        	
	        	
//	        float transX = (float) getWindowManager().getDefaultDisplay().getWidth()/2
//	                                - (float)(draw.getIntrinsicWidth()*scale)/2
//	                                ;
	        float transX = -1;
	        
	        float transY = (float)-1;
	        //float y1 = (float)getWindowManager().getDefaultDisplay().getHeight()/2;
	        float y1 = (float)controlHeight/2;
	        float y2 = (float)(draw.getIntrinsicHeight()*scale)/2;
	        
	        if(y1 > y2)  
	        	transY = y1 - y2;
	        
	        matrix.postTranslate(transX,transY);
	        iv.setImageMatrix(matrix);
		} catch (Exception e) {
			// 让你妹的报异常!
		}
	
	}
	
	
	@Override
	public float getMinZoomScale(){
		return mMinZoomScale;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent rawEvent) {
		if(gestureDetector.onTouchEvent(rawEvent))
			return true;
			
		
		ImageView view = (ImageView)findViewById(R.id.zero);
		switch(currentView){
		case 0: view = (ImageView)findViewById(R.id.zero); break;
		case 1: view = (ImageView)findViewById(R.id.one); break;
		case 2:view = (ImageView)findViewById(R.id.two); break;				
		}	
		onTouchEvented(view, rawEvent);
		
		return true;
	}
	
 
	public void quit() {
//		SharedPreferences indexPrefs = getSharedPreferences("currentIndex",
//				MODE_PRIVATE);
//		
//		SharedPreferences.Editor indexEditor = indexPrefs.edit();
//		indexEditor.putInt("currentIndex", 0);
//		indexEditor.commit();
		
		File settings = new File(DATA_FILE);
		settings.delete();
		finish();
		int pid = android.os.Process.myPid();
		android.os.Process.killProcess(pid);
		System.exit(0);
	}


}