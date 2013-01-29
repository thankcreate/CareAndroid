package com.thankcreate.care.lab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer.Orientation;

import com.markupartist.android.widget.ActionBar.Action;
import com.thankcreate.care.App;
import com.thankcreate.care.AppConstants;
import com.thankcreate.care.R;
import com.thankcreate.care.R.layout;
import com.thankcreate.care.R.menu;
import com.thankcreate.care.tool.fetcher.BaseFetcher;
import com.thankcreate.care.tool.fetcher.BaseFetcher.CommentMan;
import com.thankcreate.care.tool.fetcher.BaseFetcher.FetchCompleteListener;
import com.thankcreate.care.tool.fetcher.DoubanFetcher;
import com.thankcreate.care.tool.fetcher.RenrenFetcher;
import com.thankcreate.care.tool.fetcher.SinaWeiboFetcher;
import com.thankcreate.care.tool.misc.MathTool;
import com.thankcreate.care.tool.misc.MiscTool;
import com.thankcreate.care.tool.misc.StringTool;
import com.thankcreate.care.tool.ui.ToastHelper;
import com.thankcreate.care.viewmodel.EntryType;
import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LabEnemyActivity extends LabShareActivity implements FetchCompleteListener {

	
	private LinearLayout layout;
	private LinearLayout progressLinearLayout;
	private GraphicalView mChartView;	
	
	private ImageView imageViewAvatar;
	private TextView textName;
	private TextView textEnemy1;
	private TextView textEnemy2;
	private TextView textEnemy3;
	
	private BaseFetcher mFetcher;
	
	String herName;
	String avatar;
	
	int param1;
	int param2;
	int param3;
	int max;
	
	String name1;
	String name2;
	String name3;
	
	String id1;
	String id2;
	String id3;
	
	int mType = EntryType.NotSet;
	int analyseSourceSelected;
	
	List<CommentMan> mListMan; // 用来存回调的返回，所以这里不用初始化
	Map<String, String> mMapNameToID = new HashMap<String, String>();
	Map<String, Integer> mMapMan = new HashMap<String, Integer>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lab_enemy);
		initActionBar();
		initControl();	
		MobclickAgent.onEvent(this, "LabEnemyActivity");
	}
	
	
	/**
	 * 真奇怪，我这里都换到postResume里做刷新了
	 * 而且refresh里面已经跳到后台线程里了
	 * 但是打个潜在情敌页还是要卡半天
	 * why?
	 */
	@Override
	protected void onPostResume() {
		super.onPostResume();
		refresh();
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_lab_enemy, menu);
		return false;
	}
	
	@Override
	protected void initActionBar() {
		super.initActionBar();
		actionBar.setTitle("潜在情敌");
		actionBar.addActionRight(new Action() {
			
			@Override
			public void performAction(View view) {
				configClicked();
			}
			
			@Override
			public int getDrawable() {
				return R.drawable.thumb_data_setting;
			}
		});
	}
	
	protected void configClicked() {		
		 new AlertDialog.Builder(this)
	     .setIcon(R.drawable.thumb_share)
	     .setTitle("选择数据源")
	     .setSingleChoiceItems(R.array.array_post_source_choose, analyseSourceSelected, new DialogInterface.OnClickListener() {
	         public void onClick(DialogInterface dialog, int whichButton) {
	        	 analyseSourceSelected = whichButton;
	         }
	     })
	     .setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
	         public void onClick(DialogInterface dialog, int whichButton) {            	 
	        	 if(analyseSourceSelected == 0)
	        		 sinaWeiboAnalysis();
	        	 else if(analyseSourceSelected == 1)
	        		 renrenAnalysis();
	        	 else if(analyseSourceSelected == 2)
	        		 doubanAnalysis();
	         }
	     })
	     .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
	         public void onClick(DialogInterface dialog, int whichButton) {
	        	 Log.i("btnIndex", String.valueOf(whichButton));
	         }
	     })
	    .create().show();		
	}

	private void doubanAnalysis() {
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
		if(!MiscTool.isAuthValid(EntryType.Douban) 
				|| StringTool.isNullOrEmpty(pref.getString("Douban_FollowerID", "")))
		{
			ToastHelper.show("豆瓣尚未登陆或没有指定关注人");
			return;
		}
		mType = EntryType.Douban;
		refresh();
	}

	private void renrenAnalysis() {
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
		if(!MiscTool.isAuthValid(EntryType.Renren) 
				|| StringTool.isNullOrEmpty(pref.getString("Renren_FollowerID", "")))
		{
			ToastHelper.show("人人尚未登陆或没有指定关注人");
			return;
		}
		mType = EntryType.Renren;
		refresh();
	}

	private void sinaWeiboAnalysis() {
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
		if(!MiscTool.isAuthValid(EntryType.SinaWeibo) 
				|| StringTool.isNullOrEmpty(pref.getString("SinaWeibo_FollowerID", "")))
		{
			ToastHelper.show("新浪微博尚未登陆或没有指定关注人");
			return;
		}
		mType = EntryType.SinaWeibo;
		refresh();
	}

	private void initControl() {
		layout = (LinearLayout) findViewById(R.id.chart);
		progressLinearLayout = (LinearLayout) findViewById(R.id.progess);
		imageViewAvatar = (ImageView) findViewById(R.id.lab_avatar);
		textName = (TextView) findViewById(R.id.lab_name);
		textEnemy1 = (TextView) findViewById(R.id.lab_enemy1);
		textEnemy2 = (TextView) findViewById(R.id.lab_enemy2);
		textEnemy3 = (TextView) findViewById(R.id.lab_enemy3);
	}
	

	private void analysis() {
		Boolean res = getData();
		if(res)
		{
			layout.post(new Runnable() {
				
				@Override
				public void run() {
					refreshChart();
				}
			});
			
		}
			
	}
	
	private Boolean getData() {
		if (mListMan == null || mListMan.size() == 0)
			return false;
		Boolean res = convertListToMap();
		if(!res)
			return false;
        res = getTop3();
        return res;
	}

	/**
	 * 不硬编码不舒服斯基
	 * @return
	 */
	private Boolean getTop3() {
		if (mMapMan == null || mMapMan.size() == 0)
			return false;
		
		ArrayList<Entry<String,Integer>> listMapMan = new ArrayList<Entry<String,Integer>>(mMapMan.entrySet());    
		Collections.sort(listMapMan, new Comparator<Map.Entry<String, Integer>>() {    
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {    
                return (o2.getValue() - o1.getValue());    
            }    
        });  
		
		if(listMapMan.size() >= 3)
		{
			name1 = listMapMan.get(0).getKey();
			name2 = listMapMan.get(1).getKey();
			name3 = listMapMan.get(2).getKey();
			
			id1 = mMapNameToID.get(name1);
			id2 = mMapNameToID.get(name2);
			id3 = mMapNameToID.get(name3);
			
			param1 = listMapMan.get(0).getValue();
			param2 = listMapMan.get(1).getValue();
			param3 = listMapMan.get(2).getValue();
		}
		
		if(listMapMan.size() == 2)
		{
			name1 = listMapMan.get(0).getKey();
			name2 = listMapMan.get(1).getKey();			
			
			id1 = mMapNameToID.get(name1);
			id2 = mMapNameToID.get(name2);			
			
			param1 = listMapMan.get(0).getValue();
			param2 = listMapMan.get(1).getValue();			
		}
		
		if(listMapMan.size() == 1)
		{
			name1 = listMapMan.get(0).getKey();
			
			id1 = mMapNameToID.get(name1);
			
			param1 = listMapMan.get(0).getValue();
		}
		
		max = param1;
		textEnemy1.post(new Runnable() {
			@Override
			public void run() {
				textEnemy1.setText(name1);
			}
		});
		textEnemy2.post(new Runnable() {
			@Override
			public void run() {
				textEnemy2.setText(name2);
			}
		});
		textEnemy3.post(new Runnable() {
			@Override
			public void run() {
				textEnemy3.setText(name3);
			}
		});
		return true;
	}

	private Boolean convertListToMap() {
		try {
			if (mListMan == null || mListMan.size() == 0)
				return false;
			for (CommentMan man : mListMan) {
				if(mMapMan.containsKey(man.name))
				{
					Integer count = mMapMan.get(man.name);
					mMapMan.put(man.name, ++count);					
				}
				else
				{
					mMapMan.put(man.name, 1);
				}
				
				if(!mMapNameToID.containsKey(man.name))
				{
					mMapNameToID.put(man.name, man.id);
				}
			}
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	/**
	 * 这里可能是从非UI线程进来的
	 */
	private void refresh()
	{
		String myName= MiscTool.getMyName();
		herName = MiscTool.getHerName();
		layout.post(new Runnable() {
			
			@Override
			public void run() {
				layout.removeAllViews();
				textEnemy1.setText("分析中...");
				textEnemy2.setText("分析中...");
				textEnemy3.setText("分析中...");
			}
		});
		
		if(StringTool.isNullOrEmpty(myName))
		{
			ToastHelper.show("请先至少登陆一个帐户");
			finish();
			return ;
		}
		
		if(StringTool.isNullOrEmpty(herName))
		{
			ToastHelper.show("请先至少关注一个帐户");
			finish();
			return ;
		}
		
        name1 = "";
        name2 = "";
        name3 = "";
        param1 = 0;
        param2 = 0;
        param3 = 0;
        id1 = "";
        id2 = "";
        id3 = "";
        mMapMan.clear();
        mMapNameToID.clear();
        
        if(mType == EntryType.NotSet)
        	mType = getDefaultFetcheType();
        
        if(mType == EntryType.SinaWeibo)
        {
        	mType = EntryType.SinaWeibo;
			analyseSourceSelected = 0;
			mFetcher = new SinaWeiboFetcher();
			textName.setText(MiscTool.getHerName(EntryType.SinaWeibo));
			App.getDrawableManager().fetchDrawableOnThread(MiscTool.getHerIconUrl(EntryType.SinaWeibo), imageViewAvatar);
        }
        else if(mType == EntryType.Renren)
        {
			mType = EntryType.Renren;
			analyseSourceSelected = 1;
			mFetcher = new RenrenFetcher();
			textName.setText(MiscTool.getHerName(EntryType.Renren));
			App.getDrawableManager().fetchDrawableOnThread(MiscTool.getHerIconUrl(EntryType.Renren), imageViewAvatar);
        }
        else if(mType == EntryType.Douban)
        {
			mType = EntryType.Douban;
			analyseSourceSelected = 2;
			mFetcher = new DoubanFetcher();
			textName.setText(MiscTool.getHerName(EntryType.Douban));
			App.getDrawableManager().fetchDrawableOnThread(MiscTool.getHerIconUrl(EntryType.Douban), imageViewAvatar);
        }     
		
		if(mFetcher != null)
		{
			actionBar.post(new Runnable() {
				
				@Override
				public void run() {
					progressLinearLayout.setVisibility(View.VISIBLE);
					actionBar.setProgressBarVisibility(View.VISIBLE);
				}
			});
			mFetcher.fetch(this);
		}   
		
	}
		
	private void refreshChart()
	{
		String[] titles = new String[] { "" };
		List<double[]> values = new ArrayList<double[]>();
		values.add(new double[] { param2, param1, param3 });

		int[] colors = new int[] {Color.argb(0xBB, 0xFF, 0x9D, 0x00) };

		XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
		
		setChartSettings(renderer, " ",
		"", "情敌指数", 0.5, 3.5, 0, max + 3, Color.GRAY,
		Color.LTGRAY);
		renderer.setXLabels(1);
		renderer.setYLabels(4);
		
		renderer.setBarSpacing(0.5f);		
		renderer.setYLabelsAlign(Align.LEFT);
		renderer.addXTextLabel(1, getNameForShort(name2));
		renderer.addXTextLabel(2, getNameForShort(name1));
		renderer.addXTextLabel(3, getNameForShort(name3));	
		renderer.setOrientation(Orientation.HORIZONTAL);
		renderer.setClickEnabled(false);
		renderer.setZoomEnabled(false,false);
		renderer.setPanEnabled(false,false);
		renderer.setAntialiasing(false);
		renderer.setShowLegend(false);
		// top left bottom right
		renderer.setMargins(new int[] { 5, 20, 5, 20 });
		

		int length = renderer.getSeriesRendererCount();
		for (int i = 0; i < length; i++) {
			SimpleSeriesRenderer seriesRenderer = renderer
					.getSeriesRendererAt(i);
			seriesRenderer.setDisplayChartValues(true);
		}
		renderer.setApplyBackgroundColor(true);
		renderer.setBackgroundColor(Color.TRANSPARENT);
		renderer.setMarginsColor(Color.argb(0x00, 0x01, 0x01, 0x01));
		mChartView = ChartFactory.getBarChartView(this,
				buildBarDataset(titles, values), renderer, Type.DEFAULT);

		mChartView
				.setBackgroundResource(R.drawable.bitmap_bkg_tile_timeline);
		layout.setBackgroundResource(R.drawable.bitmap_bkg_tile_timeline);	
		layout.removeAllViews();
		layout.addView(mChartView, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
	}
	
	private int getDefaultFetcheType()
	{
		BaseFetcher fetcher = null;
		
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
		if(MiscTool.isAuthValid(EntryType.SinaWeibo) 
				&& !StringTool.isNullOrEmpty(pref.getString("SinaWeibo_FollowerID", "")))
		{
			mType = EntryType.SinaWeibo;
			analyseSourceSelected = 0;
			
		}
		else if(MiscTool.isAuthValid(EntryType.Renren)
				&& !StringTool.isNullOrEmpty(pref.getString("Renren_FollowerID", "")))
		{
			mType = EntryType.Renren;
			analyseSourceSelected = 1;
			
		}
		else if(MiscTool.isAuthValid(EntryType.Douban)
				&& !StringTool.isNullOrEmpty(pref.getString("Douban_FollowerID", "")))
		{
			mType = EntryType.Douban;
			analyseSourceSelected = 2;
			
		}
		return mType;
	}
	
	@Override
	public void fetchComplete(List<CommentMan> list) {
		actionBar.post(new Runnable() {
			
			@Override
			public void run() {
				progressLinearLayout.setVisibility(View.GONE);
				actionBar.setProgressBarVisibility(View.GONE);
			}
		});
		
		if (list == null || list.size() == 0)
		{			
			ToastHelper.show(">_< 抓取数据不成失败，请确保网络连接正常~", true);
			return;
		}
			
		mListMan = list;
		analysis();
	}
	

	
	@Override
	protected String getShareTextSinaWeibo() {
		herName = MiscTool.getHerName(EntryType.SinaWeibo);
		String preContentString =  String.format("收取了可观小的小费后，酒馆老板小声道：看在你对@%s 一片痴情的份上，我可以告诉你@%s 似乎在做些小动作，而@%s 更值得你注意，当然了，你的头号情敌非@%s 莫属~~", 
				herName, name3, name2, name1);
		return preContentString;
	}

	@Override
	protected String getShareTextRenren() {
		herName = MiscTool.getHerName(EntryType.Renren);
		String herID = MiscTool.getHerID(EntryType.Renren);
		String preContentString =  String.format("收取了可观小的小费后，酒馆老板小声道：看在你对@%s(%s) 一片痴情的份上，我可以告诉你@%s(%s) 似乎在做些小动作，而@%s(%s) 更值得你注意，当然了，你的头号情敌非@%s(%s) 莫属~~", 
				herName, herID, name3, id3, name2, id2, name1, id1);
		return preContentString;
	}

	@Override
	protected String getShareTextDouban() {
		herName = MiscTool.getHerName(EntryType.Douban);
		String preContentString =  String.format("收取了可观小的小费后，酒馆老板小声道：看在你对@%s 一片痴情的份上，我可以告诉你@%s 似乎在做些小动作，而@%s 更值得你注意，当然了，你的头号情敌非@%s 莫属~~", 
				herName, name3, name2, name1);
		return preContentString;
	}
}
