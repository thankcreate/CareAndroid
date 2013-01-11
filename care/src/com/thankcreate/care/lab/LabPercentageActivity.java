package com.thankcreate.care.lab;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DialRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.DialRenderer.Type;

import com.thankcreate.care.App;
import com.thankcreate.care.R;
import com.thankcreate.care.R.layout;
import com.thankcreate.care.R.menu;
import com.thankcreate.care.tool.misc.MathTool;
import com.thankcreate.care.tool.misc.MiscTool;
import com.thankcreate.care.tool.misc.StringTool;
import com.thankcreate.care.tool.ui.ToastHelper;
import com.thankcreate.care.viewmodel.EntryType;
import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.view.Menu;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LabPercentageActivity extends LabShareActivity {

	private LinearLayout layout;

	private GraphicalView mChartView;	
	
	private ImageView imageViewHerAvatar;
	private TextView textHerName;
	private ImageView imageViewMyAvatar;
	private TextView textMyName;
	private TextView textAward;
	
	int param1;
	String herName;
	String myName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lab_percentage);
		initActionBar();
		initControl();
		analysis();
		MobclickAgent.onEvent(this, "LabPercentageActivity");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_lab_percentage, menu);
		return false;
	}

	
	@Override
	protected void initActionBar() {
		super.initActionBar();
		actionBar.setTitle("姻缘指数");
	}
	
	private void initControl() {
		layout = (LinearLayout) findViewById(R.id.chart);
		imageViewHerAvatar = (ImageView) findViewById(R.id.lab_her_avatar);
		imageViewMyAvatar = (ImageView) findViewById(R.id.lab_my_avatar);
		textHerName = (TextView) findViewById(R.id.lab_her_name);
		textMyName = (TextView) findViewById(R.id.lab_my_name);
		textAward = (TextView) findViewById(R.id.lab_award);
	}
	

	private void analysis() {
		Boolean res = getData();
		if(res)
			refreshChart();
	}
	
	private boolean getData()
	{
		myName= MiscTool.getMyName();
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
		
		textMyName.setText(myName);
		textHerName.setText(herName);
		
		App.getDrawableManager().fetchDrawableOnThread(MiscTool.getHerIconUrl(), imageViewHerAvatar);
		App.getDrawableManager().fetchDrawableOnThread(MiscTool.getMyIconUrl(), imageViewMyAvatar);
		
		int herSig = StringTool.getSig(herName);
		int mySig =  StringTool.getSig(myName);
		
		param1 =  (herSig + mySig) * 575 % 49 + 50;
		textAward.setText(String.valueOf(param1));
        return true;
	}
	
	private void refreshChart() {
		
	    CategorySeries category = new CategorySeries("Weight indic");
	    category.add("Current", param1);
	    DialRenderer renderer = new DialRenderer();
	    renderer.setChartTitleTextSize(20);
	    renderer.setLabelsTextSize(15);
	    renderer.setLegendTextSize(15);
	    renderer.setMargins(new int[] {20, 10, 15, 10});
	    SimpleSeriesRenderer r = new SimpleSeriesRenderer();
	    r.setColor(Color.argb(0xff, 0xf9, 0x39, 0x46));
	    renderer.addSeriesRenderer(r);
	    renderer.setLabelsTextSize(20);
	    renderer.setLabelsColor(Color.argb(0xBB, 0xFF, 0x9D, 0x00));
	    renderer.setShowLabels(true);
	    renderer.setShowLegend(false);
	    renderer.setVisualTypes(new DialRenderer.Type[] {Type.NEEDLE});
	    renderer.setMinValue(0);
	    renderer.setMaxValue(100);   
	    renderer.setPanEnabled(false);

	    mChartView = ChartFactory.getDialChartView(this, category, renderer);
		mChartView
				.setBackgroundResource(R.drawable.bitmap_bkg_tile_picturewallpage);
		layout.setBackgroundResource(R.drawable.bitmap_bkg_tile_picturewallpage);
		layout.addView(mChartView, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
	}
	
	@Override
	protected String getShareTextSinaWeibo() {	
		herName = MiscTool.getHerName(EntryType.SinaWeibo);
		myName = MiscTool.getMyName(EntryType.SinaWeibo);
		String preContentString =  String.format("经某不靠谱的分析仪测算，@%s 与 @%s 的姻缘指数达到惊人的%d。去死去死团众，不管你们信不信，我反正不信了", 
				herName, myName, param1);
		return preContentString;
	}

	@Override
	protected String getShareTextRenren() {
		herName = MiscTool.getHerName(EntryType.Renren);
		String herID = MiscTool.getHerID(EntryType.Renren);
		myName = MiscTool.getMyName(EntryType.Renren);
		String myID = MiscTool.getMyID(EntryType.Renren);
		String preContentString =  String.format("经某不靠谱的分析仪测算，@%s(%s) 与 @%s(%s) 的姻缘指数达到惊人的%d。去死去死团众，不管你们信不信，我反正不信了", 
				herName, herID, myName, myID, param1);
		return preContentString;
	}

	@Override
	protected String getShareTextDouban() {
		herName = MiscTool.getHerName(EntryType.Douban);
		myName = MiscTool.getMyName(EntryType.Douban);
		String preContentString =  String.format("经某不靠谱的分析仪测算，@%s 与 @%s 的姻缘指数达到惊人的%d。去死去死团众，不管你们信不信，我反正不信了", 
				herName, myName, param1);
		return preContentString;
	}



}
