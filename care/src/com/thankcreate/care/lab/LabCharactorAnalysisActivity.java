package com.thankcreate.care.lab;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer.Orientation;

import com.thankcreate.care.App;
import com.thankcreate.care.R;
import com.thankcreate.care.R.layout;
import com.thankcreate.care.R.menu;
import com.thankcreate.care.tool.misc.MathTool;
import com.thankcreate.care.tool.misc.MiscTool;
import com.thankcreate.care.tool.misc.StringTool;
import com.thankcreate.care.tool.ui.ToastHelper;
import com.thankcreate.care.viewmodel.EntryType;
import com.thankcreate.care.viewmodel.ItemViewModel;
import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.view.Menu;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LabCharactorAnalysisActivity extends LabShareActivity {
	
	private LinearLayout layout;

	private GraphicalView mChartView;	
	private DefaultRenderer mRenderer = new DefaultRenderer();
	private CategorySeries mSeries = new CategorySeries("");
	
	private ImageView imageViewAvatar;
	private TextView textName;
	private TextView textAward;
	
	String herName;
	String avatar;
	
	int param1;
	int param2;
	int param3;
	int param4;
	int param5;
	String mostActiveTime;
	String award;
	int max;
	
	String[] categories = {"萝莉", "女王", "天然呆","吃货", "中二"};	
	int[] colors = {
			Color.argb(0xFF, 0xFF, 0x14, 0x21),			
			Color.argb(0xFF, 0x9A, 0xFF, 0xF3),
			Color.argb(0xFF, 0xFF, 0x6D, 0x4B),			
			Color.argb(0xFF, 0x90, 0xED, 0xB6),
			Color.argb(0xFF, 0xED, 0xFF, 0x4D)};
//	int[] colors = {
//			Color.argb(0xFF, 0xa4, 0xfa, 0xf1),			
//			Color.argb(0xFF, 0xf9, 0x39, 0x46),
//			Color.argb(0xFF, 0x3a, 0x65, 0x54),
//			Color.argb(0xFF, 0x7d, 0x03, 0x1c),
//			Color.argb(0xFF, 0x35, 0x00, 0x0a)};

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lab_charactor_analysis);
		initActionBar();
		initControl();
		analysis();
		MobclickAgent.onEvent(this, "LabCharactorAnalysisActivity");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_lab_charactor_analysis, menu);
		return false;
	}
	
	@Override
	protected void initActionBar() {
		super.initActionBar();
		actionBar.setTitle("性格分析");
	}
	
	private void initControl() {
		layout = (LinearLayout) findViewById(R.id.chart);
		imageViewAvatar = (ImageView) findViewById(R.id.lab_avatar);
		textName = (TextView) findViewById(R.id.lab_name);
		textAward = (TextView) findViewById(R.id.lab_award);
	}
	

	private void analysis() {
		Boolean res = getData();
		if(res)
			refreshChart();
	}
	
	private boolean getData()
	{
		String myName= MiscTool.getMyName();
		herName = MiscTool.getHerName();
		
		if(StringTool.isNullOrEmpty(myName))
		{
			ToastHelper.show("请先至少登陆一个帐户");
			finish();
			return false;
		}
		
		if(StringTool.isNullOrEmpty(herName))
		{
			ToastHelper.show("请先至少关注一个帐户");
			finish();
			return false;
		}
		
		textName.setText(herName);
		App.getDrawableManager().fetchDrawableOnThread(MiscTool.getHerIconUrl(), imageViewAvatar);
		
		int herSig = StringTool.getSig(herName);
		param1 = (int)(herSig * 575 % 50 + 50);
        param2 = (int)(herSig * herSig % 50 + 50);
        param3 = (int)(herSig * 250 % 50 + 50);
        param4 = (int)(herSig * 337 % 50 + 50);
        param5 = (int)(herSig * 702 % 50 + 50);
                
		
		max = MathTool.getMaxValue(new int[]{param1, param2, param3, param4, param5});		
		award = MathTool.getMaxLable(
				new String[]{"极品萝莉", "盖世女王" , "激萌天然呆", "吃货去死去死" ,"中二少年"}, 
				new int[]{param1, param2, param3, param4, param5});
		textAward.setText(award);
		
        return true;
	}
	
	private void refreshChart() {
		
	    mRenderer.setChartTitleTextSize(20);
	    mRenderer.setLabelsTextSize(15);
	    mRenderer.setMargins(new int[] { 5, 20, 5, 20 });
	    mRenderer.setZoomButtonsVisible(false);
	    mRenderer.setStartAngle(90);
	    mRenderer.setClickEnabled(false);
	    mRenderer.setAntialiasing(false);
	    mRenderer.setShowLegend(false);
	    mRenderer.setExternalZoomEnabled(false);
	    
	    
	    mSeries.add("萝莉", param1);
	    mSeries.add("女王", param2);
	    mSeries.add("天然呆", param3);
	    mSeries.add("吃货", param4);
	    mSeries.add("中二", param5);
	    
	    for(int i = 0; i < categories.length; i++)
	    {
	    	SimpleSeriesRenderer seriesRenderer = new SimpleSeriesRenderer();
	    	seriesRenderer.setColor(colors[i]);
	    	mRenderer.addSeriesRenderer(seriesRenderer);
	    }
	    
		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setBackgroundColor(Color.TRANSPARENT);	

	    mChartView = ChartFactory.getPieChartView(this, mSeries, mRenderer);
		mChartView
				.setBackgroundResource(R.drawable.bitmap_bkg_tile_picturewallpage);
		layout.setBackgroundResource(R.drawable.bitmap_bkg_tile_picturewallpage);
		layout.addView(mChartView, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
	}


	@Override
	protected String getShareTextSinaWeibo() {
		int pencentage = max * 100 / (param1 + param2 + param3 + param4);
		herName = MiscTool.getHerName(EntryType.SinaWeibo);
		String preContentString =  String.format("据新一轮民调显示，@%s 的萝莉属性为%d，女王属性为%d，天然呆属性为%d，吃货属性为%d，伪娘属性为%d，获得了成就【%s】", 
				herName, param1, param2, param3, param4, param5, award);
		return preContentString;
	}

	@Override
	protected String getShareTextRenren() {
		int pencentage = max * 100 / (param1 + param2 + param3 + param4);
		herName = MiscTool.getHerName(EntryType.Renren);
		String herID = MiscTool.getHerID(EntryType.Renren);
		String preContentString =  String.format("据新一轮民调显示，@%s(%s) 的萝莉属性为%d，女王属性为%d，天然呆属性为%d，吃货属性为%d，伪娘属性为%d，获得了成就【%s】", 
				herName,herID, param1, param2, param3, param4, param5, award);
		return preContentString;
	}

	@Override
	protected String getShareTextDouban() {
		int pencentage = max * 100 / (param1 + param2 + param3 + param4);
		herName = MiscTool.getHerName(EntryType.Douban);
		String preContentString =  String.format("据新一轮民调显示，@%s 的萝莉属性为%d，女王属性为%d，天然呆属性为%d，吃货属性为%d，伪娘属性为%d，获得了成就【%s】", 
				herName, param1, param2, param3, param4, param5, award);
		return preContentString;
	}



}
