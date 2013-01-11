package com.thankcreate.care.lab;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import org.achartengine.chart.PointStyle;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.thankcreate.care.App;
import com.thankcreate.care.BaseActivity;
import com.thankcreate.care.R;
import com.thankcreate.care.status.StatusPostActivity;
import com.thankcreate.care.status.StatusDetailActivity;
import com.thankcreate.care.tool.misc.DateTool;
import com.thankcreate.care.tool.misc.MiscTool;
import com.thankcreate.care.tool.misc.StringTool;
import com.thankcreate.care.tool.ui.ToastHelper;
import com.thankcreate.care.viewmodel.EntryType;
import com.umeng.analytics.MobclickAgent;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.api.StatusesAPI;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * 这个类主要是用来实现所有奇怪的地方页面共用的actionbar上的分享功能
 * @author ThankCreate
 */
public abstract class LabShareActivity extends BaseActivity {

	protected ActionBar actionBar;
	
	// 在alert框中选中的那个发布源的索引值
	private int sourceSelected = 0;
	private int[] typeArray = {EntryType.SinaWeibo, EntryType.Renren, EntryType.Douban};
	private String screenshotURL;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int tp = MiscTool.getFirstFoundLoginType();
				
		for(int i = 0; i< typeArray.length; i++)
		{
			if(typeArray[i] == tp)
				sourceSelected = i;
		}
	}
	
	/**
	 * 子类的initActionBar方法必须先调用此父类方法
	 */
	protected void initActionBar() {
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.addActionRight(new Action() {
			@Override
			public void performAction(View view) {
				shareClicked();
				MobclickAgent.onEvent(LabShareActivity.this, "LabShareClick");
			}
			
			@Override
			public int getDrawable() {
				return R.drawable.thumb_share;
			}
		});
		addActionBarBackButton(actionBar);
	}
	
	
	protected String getNameForShort(String rawName) {
		try {
			if(StringTool.isNullOrEmpty(rawName))
				return "";
			if(rawName.length() <= 7)
				return rawName;
			return rawName.substring(0,7) + "...";	
		} catch (Exception e) {
			return "";
		}
		
	}
	
	private void takeScreenShot()
	{
		Bitmap bitmap;
		View v1 = actionBar.getRootView();
		v1.setDrawingCacheEnabled(true);
		bitmap = Bitmap.createBitmap(v1.getDrawingCache());
		v1.setDrawingCacheEnabled(false);

		Date date = new Date();
		String sigDate = DateTool.getDateSig(date);		
		File myDir = App.getAppContext().getCacheDir();		
		File imageFile = new File(myDir , sigDate + ".png");
		
		screenshotURL = imageFile.toString();
		try {
			FileOutputStream fout = new FileOutputStream(imageFile);
		    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fout);
		    fout.flush();
		    fout.close();
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		    screenshotURL = "";
		} catch (IOException e) {
		    e.printStackTrace();
		    screenshotURL = "";
		}
	}
	
	private void shareClicked()
	{
		preShare();
		takeScreenShot();
		 new AlertDialog.Builder(this)
	     .setIcon(R.drawable.thumb_share)
	     .setTitle("选择发布源")
	     .setSingleChoiceItems(R.array.array_post_source_choose, sourceSelected, new DialogInterface.OnClickListener() {
	         public void onClick(DialogInterface dialog, int whichButton) {
	        	 sourceSelected = whichButton;
	         }
	     })
	     .setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
	         public void onClick(DialogInterface dialog, int whichButton) {            	 
	        	 if(sourceSelected == 0)
	        		 shareToSinaWeibo();
	        	 else if(sourceSelected == 1)
	        		 shareToRenren();
	        	 else if(sourceSelected == 2)
	        		 shareToDouban();
	         }
	     })
	     .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
	         public void onClick(DialogInterface dialog, int whichButton) {
	        	 Log.i("btnIndex", String.valueOf(whichButton));
	         }
	     })
	    .create().show();
	}
	
	private void shareToSinaWeibo()
	{
		if(!MiscTool.isAuthValid(EntryType.SinaWeibo))
		{
			ToastHelper.show("新浪微博尚未登陆，或者登陆已过期~");
			return;
		}
		gotoStatusPostPage(EntryType.SinaWeibo);
	}
	
	private void shareToRenren()
	{
		if(!MiscTool.isAuthValid(EntryType.Renren))
		{
			ToastHelper.show("人人帐号尚未登陆，或者登陆已过期~");
			return;
		}
		gotoStatusPostPage(EntryType.Renren);
	}
	
	private void shareToDouban()
	{
		if(!MiscTool.isAuthValid(EntryType.Douban))
		{
			ToastHelper.show("豆瓣帐号尚未登陆，或者登陆已过期~");
			return;
		}
		gotoStatusPostPage(EntryType.Douban);
	}
	
	private void gotoStatusPostPage(int type)
	{
		Intent intent = new Intent();
		intent.setClass(this, StatusPostActivity.class);
		intent.putExtra("type", type);
		intent.putExtra("preContent", getShareText(type));
		intent.putExtra("imageURL", screenshotURL);
		startActivity(intent); 
	}
	
	protected XYMultipleSeriesRenderer buildRenderer(int[] colors, PointStyle[] styles) {
	    XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
	    setRenderer(renderer, colors, styles);
	    return renderer;
	  }

	  protected void setRenderer(XYMultipleSeriesRenderer renderer, int[] colors, PointStyle[] styles) {
	    renderer.setAxisTitleTextSize(16);
	    renderer.setChartTitleTextSize(20);
	    renderer.setLabelsTextSize(15);
	    renderer.setLegendTextSize(15);
	    renderer.setPointSize(5f);
	    renderer.setMargins(new int[] { 20, 30, 15, 20 });
	    int length = colors.length;
	    for (int i = 0; i < length; i++) {
	      XYSeriesRenderer r = new XYSeriesRenderer();
	      r.setColor(colors[i]);
	      r.setPointStyle(styles[i]);
	      renderer.addSeriesRenderer(r);
	    }
	  }
	
	protected XYMultipleSeriesRenderer buildBarRenderer(int[] colors) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		renderer.setAxisTitleTextSize(20);
		renderer.setChartTitleTextSize(20);
		renderer.setLabelsTextSize(20);
		renderer.setLegendTextSize(20);	
		
		int length = colors.length;
		for (int i = 0; i < length; i++) {
			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
			r.setColor(colors[i]);			
			renderer.addSeriesRenderer(r);
		}
		return renderer;
	}

	protected void setChartSettings(XYMultipleSeriesRenderer renderer,
			String title, String xTitle, String yTitle, double xMin,
			double xMax, double yMin, double yMax, int axesColor,
			int labelsColor) {
		renderer.setChartTitle(title);
		renderer.setXTitle(xTitle);
		renderer.setYTitle(yTitle);
		renderer.setXAxisMin(xMin);
		renderer.setXAxisMax(xMax);
		renderer.setYAxisMin(yMin);
		renderer.setYAxisMax(yMax);
		renderer.setAxesColor(axesColor);
		renderer.setLabelsColor(labelsColor);
	}

	protected XYMultipleSeriesDataset buildBarDataset(String[] titles,
			List<double[]> values) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		int length = titles.length;
		for (int i = 0; i < length; i++) {
			CategorySeries series = new CategorySeries(titles[i]);
			double[] v = values.get(i);
			int seriesLength = v.length;
			for (int k = 0; k < seriesLength; k++) {
				series.add(v[k]);
			}
			dataset.addSeries(series.toXYSeries());
		}
		return dataset;
	}

	private String getShareText(int type)
	{
        if (type == EntryType.SinaWeibo)
        {
        	return getShareTextSinaWeibo();
        } 
        else if (type == EntryType.Renren)
        {
        	return getShareTextRenren();
        }
        else if (type == EntryType.Douban)
        {
        	return getShareTextDouban();
        } 
        return "";
	}
	
	
	protected void preShare() {
		// default do nothing
	}
	
	protected abstract String getShareTextSinaWeibo();
	protected abstract String getShareTextRenren();
	protected abstract String getShareTextDouban();

}
