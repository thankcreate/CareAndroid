package com.thankcreate.care.lab;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer.Orientation;
import org.achartengine.renderer.XYSeriesRenderer;

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
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Menu;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LabTimelineActivity extends LabShareActivity {

	private LinearLayout layout;

	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	private GraphicalView mChartView;
	
	private ImageView imageViewAvatar;
	private TextView textName;
	private TextView textAward;
	
	String herName;
	String avatar;
	
	int param1;
	int param2;
	int param3;
	int param4;
	String mostActiveTime;
	String award;
	int max;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lab_timeline);
		initActionBar();
		initControl();
		analysis();
		MobclickAgent.onEvent(this, "LabTimelineActivity");
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_lab_timeline, menu);
		return false;
	}

	@Override
	protected void initActionBar() {
		super.initActionBar();
		actionBar.setTitle("发贴时段");
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
		
		for(ItemViewModel item : App.mainViewModel.items)
        {
			Calendar calendar = GregorianCalendar.getInstance(); 
			calendar.setTime(item.time);
			int hour = calendar.get(Calendar.HOUR_OF_DAY);            
            if (hour >= 8 && hour < 12)
            {
                param1++;
            }
            else if (hour >= 12 && hour < 18)
            {
                param2++;
            }
            else if (hour >= 18 && hour < 24)
            {
                param3++;
            }
            else if (hour >= 0 && hour < 8)
            {
                param4++;
            }                     
        }
		max = MathTool.getMaxValue(new int[]{param1, param2, param3, param4});
		mostActiveTime = MathTool.getMaxLable(
				new String[]{"上午", "下午" , "晚上", "凌晨"}, 
				new int[]{param1, param2, param3, param4});
		award = MathTool.getMaxLable(
				new String[]{"正常得近乎无聊", "睡完午觉就无所事事的家伙" , "月色下的吟游者", "程序员"}, 
				new int[]{param1, param2, param3, param4});
		textAward.setText(award);
		
        return true;
	}
	
	private void refreshChart() {
//		String[] titles = new String[] { "发贴数" };
//		List<double[]> values = new ArrayList<double[]>();
//		values.add(new double[] { param1, param2, param3, param4 });
//
//		int[] colors = new int[] {Color.argb(0xBB, 0xFF, 0x9D, 0x00) };
//
//		XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
//		
//		setChartSettings(renderer, " ",
//				"时间段", "发贴数", 0.5, 4.5, 0, max + 5, Color.GRAY,
//				Color.LTGRAY);
//		renderer.setXLabels(1);
//		renderer.setYLabels(4);
//		
//		renderer.setBarSpacing(0.5f);		
//		renderer.setYLabelsAlign(Align.LEFT);
//		renderer.addXTextLabel(1, "上午");
//		renderer.addXTextLabel(2, "下午");
//		renderer.addXTextLabel(3, "晚上");
//		renderer.addXTextLabel(4, "凌晨");
//		renderer.setOrientation(Orientation.HORIZONTAL);
//		renderer.setClickEnabled(false);
//		renderer.setZoomEnabled(false,false);
//		renderer.setPanEnabled(false,false);
//		renderer.setAntialiasing(false);
//		renderer.setShowLegend(false);
//		// top left bottom right
//		renderer.setMargins(new int[] { 5, 20, 5, 20 });
//		
//
//		int length = renderer.getSeriesRendererCount();
//		for (int i = 0; i < length; i++) {
//			SimpleSeriesRenderer seriesRenderer = renderer
//					.getSeriesRendererAt(i);
//			seriesRenderer.setDisplayChartValues(true);
//		}
//		renderer.setApplyBackgroundColor(true);
//		renderer.setBackgroundColor(Color.TRANSPARENT);
//		renderer.setMarginsColor(Color.argb(0x00, 0x01, 0x01, 0x01));
//		mChartView = ChartFactory.getBarChartView(this,
//				buildBarDataset(titles, values), renderer, Type.DEFAULT);
//
//		mChartView
//				.setBackgroundResource(R.drawable.bitmap_bkg_tile_picturewallpage);
//		layout.setBackgroundResource(R.drawable.bitmap_bkg_tile_picturewallpage);
//		layout.addView(mChartView, new LayoutParams(LayoutParams.MATCH_PARENT,
//				LayoutParams.MATCH_PARENT));
		
		String[] titles = new String[] { "发贴数" };
		List<double[]> values = new ArrayList<double[]>();
		values.add(new double[] { param1, param2, param3, param4 });

		int[] colors = new int[] {Color.argb(0xBB, 0xFF, 0x9D, 0x00) };
		PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE, PointStyle.DIAMOND };
		XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
		
		setChartSettings(renderer, " ",
		"时间段", "发贴数", 0.5, 4.5, 0, max + 5, Color.GRAY,
		Color.LTGRAY);
		renderer.setXLabels(1);
		renderer.setYLabels(4);
		
		renderer.setBarSpacing(0.5f);		
		renderer.setYLabelsAlign(Align.LEFT);
		renderer.addXTextLabel(1, "上午");
		renderer.addXTextLabel(2, "下午");
		renderer.addXTextLabel(3, "晚上");		
		renderer.addXTextLabel(4, "凌晨");
		renderer.setOrientation(Orientation.HORIZONTAL);
		renderer.setClickEnabled(false);
		renderer.setZoomEnabled(false,false);
		renderer.setPanEnabled(false,false);
		renderer.setAntialiasing(false);
		renderer.setShowLegend(false);		
		renderer.setShowGrid(true);		
		renderer.setPointSize(10);		
		// top left bottom right
		renderer.setMargins(new int[] { 5, 20, 5, 20 });
		

		int length = renderer.getSeriesRendererCount();
		for (int i = 0; i < length; i++) {
			 XYSeriesRenderer r = (XYSeriesRenderer) renderer.getSeriesRendererAt(i);
		      r.setLineWidth(5);
		      r.setFillPoints(true);
		}
		renderer.setApplyBackgroundColor(true);
		renderer.setBackgroundColor(Color.TRANSPARENT);
		renderer.setMarginsColor(Color.argb(0x00, 0x01, 0x01, 0x01));
		mChartView = ChartFactory.getLineChartView(this,
				buildBarDataset(titles, values), renderer);

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
		String preContentString =  String.format("据消息人士透露，@%s 最活跃的时间是在%s，此段时间中的发贴量占全部发贴的%d%%, 获得了成就【%s】", 
				herName, mostActiveTime, pencentage,  award);
		return preContentString;
	}


	@Override
	protected String getShareTextRenren() {
		int pencentage = max * 100 / (param1 + param2 + param3 + param4);
		herName = MiscTool.getHerName(EntryType.Renren);
		String herID = MiscTool.getHerID(EntryType.Renren);
		String preContentString =  String.format("据消息人士透露，@%s(%s) 最活跃的时间是在%s，此段时间中的发贴量占全部发贴的%d%%, 获得了成就【%s】", 
				herName, herID, mostActiveTime, pencentage,  award);
		return preContentString;
	}


	@Override
	protected String getShareTextDouban() {
		int pencentage = max * 100 / (param1 + param2 + param3 + param4);
		herName = MiscTool.getHerName(EntryType.Douban);
		String preContentString =  String.format("据消息人士透露，@%s 最活跃的时间是在%s，此段时间中的发贴量占全部发贴的%d%%, 获得了成就【%s】", 
				herName, mostActiveTime, pencentage,  award);
		return preContentString;
	}

}
