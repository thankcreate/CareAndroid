package com.thankcreate.care;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class GuideActivity extends BaseActivity {

    private ViewPager viewPager;  
    private ArrayList<View> pageViews;  
    private ViewGroup main, group;  
    private ImageView imageView;  
    private ImageView[] imageViews;
    private int[] imageIDs = {
    		R.drawable.guide_1, 
    		R.drawable.guide_2, 
    		R.drawable.guide_3, 
    		R.drawable.guide_4, 
    		R.drawable.guide_5};
    
    private final int LAST_PAGE_DISPLAY_LENGHT = 1000;
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide);
		initControl();
		
	}

	private void initControl() {
        LayoutInflater inflater = getLayoutInflater();  
        pageViews = new ArrayList<View>();  
        for(int i = 0; i < imageIDs.length; i++)
        {
        	View layoutView = inflater.inflate(R.layout.guide_item, null);
        	ImageView  imageView = (ImageView) layoutView.findViewById(R.id.guide_item_imageview);
        	imageView.setImageResource(imageIDs[i]);
        	pageViews.add(layoutView);
        } 
  
        imageViews = new ImageView[pageViews.size()];  
        //main = (ViewGroup)inflater.inflate(R.layout.activity_guide, null);  
        
        // group是R.layou.main中的负责包裹小圆点的LinearLayout.  
        group = (ViewGroup)findViewById(R.id.guide_viewgroup);
  
        viewPager = (ViewPager)findViewById(R.id.guide_pages);
  
        for (int i = 0; i < pageViews.size(); i++) {  
            imageView = new ImageView(this);  
            imageView.setLayoutParams(new LayoutParams(20,20));  
            imageView.setPadding(20, 0, 20, 0);  
            imageViews[i] = imageView;  
            if (i == 0) {  
                //默认选中第一张图片
                imageViews[i].setBackgroundResource(R.drawable.page_indicator_focused);  
            } else {  
                imageViews[i].setBackgroundResource(R.drawable.page_indicator);  
            }  
            group.addView(imageViews[i]);  
        }  
  
        //setContentView(main);
        viewPager.setAdapter(new GuidePageAdapter());  
        viewPager.setOnPageChangeListener(new GuidePageChangeListener());  
	}

	/** 指引页面Adapter */
    class GuidePageAdapter extends PagerAdapter {  
    	  
        @Override  
        public int getCount() {  
            return pageViews.size();  
        }  
  
        @Override  
        public boolean isViewFromObject(View arg0, Object arg1) {  
            return arg0 == arg1;  
        }  
  
        @Override  
        public int getItemPosition(Object object) {  
            // TODO Auto-generated method stub  
            return super.getItemPosition(object);  
        }  
  
        @Override  
        public void destroyItem(View arg0, int arg1, Object arg2) {  
            // TODO Auto-generated method stub  
            ((ViewPager) arg0).removeView(pageViews.get(arg1));  
        }  
  
        @Override  
        public Object instantiateItem(View arg0, int arg1) {  
            // TODO Auto-generated method stub  
            ((ViewPager) arg0).addView(pageViews.get(arg1));  
            return pageViews.get(arg1);  
        }  
  
        @Override  
        public void restoreState(Parcelable arg0, ClassLoader arg1) {  
            // TODO Auto-generated method stub  
  
        }  
  
        @Override  
        public Parcelable saveState() {  
            // TODO Auto-generated method stub  
            return null;  
        }  
  
        @Override  
        public void startUpdate(View arg0) {  
            // TODO Auto-generated method stub  
  
        }  
  
        @Override  
        public void finishUpdate(View arg0) {  
            // TODO Auto-generated method stub  
  
        }  
    } 
    
    /** 指引页面改监听器 */
    class GuidePageChangeListener implements OnPageChangeListener {  
  
        @Override  
        public void onPageScrollStateChanged(int arg0) {  
            // TODO Auto-generated method stub  
  
        }  
  
        @Override  
        public void onPageScrolled(int arg0, float arg1, int arg2) {  
            // TODO Auto-generated method stub  
  
        }  
  
        @Override  
        public void onPageSelected(int arg0) {  
            for (int i = 0; i < imageViews.length; i++) {  
                imageViews[arg0]  
                        .setBackgroundResource(R.drawable.page_indicator_focused);  
                if (arg0 != i) {  
                    imageViews[i]  
                            .setBackgroundResource(R.drawable.page_indicator);  
                }  
            }
            if(arg0 == imageViews.length - 1)
            {
            	new Handler().postDelayed(new Runnable() {
					
					@Override
					public void run() {
	        			Intent intent = new Intent();
	        			intent.setClass(GuideActivity.this, MainActivity.class);					
	        			startActivity(intent);
					}
				}, LAST_PAGE_DISPLAY_LENGHT);
            }
  
        }  
  
    }  
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_guide, menu);
		return true;
	}

}
