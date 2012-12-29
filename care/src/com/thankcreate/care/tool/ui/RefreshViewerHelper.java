package com.thankcreate.care.tool.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Looper;
import android.util.JsonReader;

import com.thankcreate.care.App;
import com.thankcreate.care.tool.converter.SinaWeiboConverter;
import com.thankcreate.care.tool.misc.MiscTool;
import com.thankcreate.care.tool.misc.PreferenceHelper;
import com.thankcreate.care.tool.misc.StringTool;
import com.thankcreate.care.tool.misc.TaskHelper;
import com.thankcreate.care.tool.misc.TaskHelper.OnTaskCompleteListener;
import com.thankcreate.care.viewmodel.ItemViewModel;
import com.thankcreate.care.viewmodel.MainViewModel;
import com.thankcreate.care.viewmodel.PictureItemViewModel;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.api.WeiboAPI.FEATURE;
import com.weibo.sdk.android.net.RequestListener;

public class RefreshViewerHelper implements OnTaskCompleteListener{
	
	private TaskHelper taskHelper;
	
	private List<OnRefreshCompleteListener> listListeners = new ArrayList<RefreshViewerHelper.OnRefreshCompleteListener>();
	
	
	private static RefreshViewerHelper sRefreshViewerHelper = null; 
	
	public static  RefreshViewerHelper getInstance()
	{
		if(sRefreshViewerHelper == null)
			sRefreshViewerHelper = new RefreshViewerHelper();
		return sRefreshViewerHelper;
	}
	
	private RefreshViewerHelper() {
		super();
		taskHelper = new TaskHelper(this);		
	}
	
	public void addListenter(OnRefreshCompleteListener listener)
	{
		listListeners.add(listener);
	}


	public void refreshMainViewModel()
	{
		App.mainViewModel.isChanged = false;
		taskHelper.clear();
		taskHelper.pushTask("SinaWeibo");
		
        // 1.Weibo
        refreshModelSinaWeibo();
        // 2.Rss
        refreshModelRssFeed();
        // 3.Renren
        refreshModelRenren();
        // 4.Douban 
        refreshModelDouban();
	}


	private void refreshModelSinaWeibo() {
		App.mainViewModel.sinaWeiboItems.clear();
		App.mainViewModel.sinaWeiboPictureItems.clear();
		// 1.判断是否登陆
		Boolean isLoggin = MiscTool.isSinaWeiboLogin();
		if(!isLoggin)
		{
			taskHelper.popTask("SinaWeibo");
			return;			
		}			
		
		// 2.判断是否关注了用户
		String strFollowerID = PreferenceHelper.getString("SinaWeibo_FollowerID");
		if(StringTool.isNullOrEmpty(strFollowerID))
		{			
			taskHelper.popTask("SinaWeibo");
			return;
		}
		
		// 3.判断是否过期
		long exp = PreferenceHelper.getLong("SinaWeibo_ExpirationDate");
		if(exp < System.currentTimeMillis())
		{
			ToastHelper.show(">_<  新浪微博授权已过期，请到帐号页重新登陆以授权");			
			PreferenceHelper.removeSinaWeiboPreference();
			taskHelper.popTask("SinaWeibo");			
			return;
		}
				
		Oauth2AccessToken oa = MiscTool.getOauth2AccessToken();
		if(oa == null)
			return;
		
		StatusesAPI statusesAPI = new StatusesAPI(oa);
		statusesAPI.userTimeline(Long.parseLong(strFollowerID),
				0, 0, 30, 1, false, FEATURE.ALL, false, userTimeLineListener);
	}
	
	private RequestListener userTimeLineListener = new RequestListener() {
		

		@Override
		public void onComplete(String arg0) {
			try
			{
				JSONObject root = new JSONObject(arg0);
				JSONArray listStatus = root.optJSONArray("statuses");
				if(listStatus == null || listStatus.length() == 0)
					return;
				for(int i = 0; i < listStatus.length(); i++)
				{
					JSONObject status = listStatus.getJSONObject(i);
					ItemViewModel model = SinaWeiboConverter.convertStatusToCommon(status);
					if(model != null)
					{
						App.mainViewModel.sinaWeiboItems.add(model);
					}
				}
			} 
			catch (Exception e) 
			{				
				ToastHelper.show(">_<  新浪微博信息获取发生未知错误");
				e.printStackTrace();
			}
			finally
			{
				taskHelper.popTask("SinaWeibo");	
			}
			
		}
		
		@Override
		public void onIOException(IOException arg0) {
			taskHelper.popTask("SinaWeibo");		
			ToastHelper.show(">_<  新浪微博信息获取发生未知错误");
		}
		
		@Override
		public void onError(WeiboException arg0) {
			// TODO: 验证statusCode是否有用
			taskHelper.popTask("SinaWeibo");		
			if(arg0.getStatusCode() == 21327) {
				ToastHelper.show(">_<  新浪微博授权已过期，请到帐号页重新登陆以授权");
			}
			else {
				ToastHelper.show(">_<  新浪微博信息获取发生未知错误");
			}
		}
	};


	private void refreshModelRssFeed() {
		// TODO Auto-generated method stub
		
	}
	
	private void refreshModelRenren() {
		// TODO Auto-generated method stub
		
	}

	private void refreshModelDouban() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onAllTaskComplete() {
		refreshViewItems();
	}
	
	public void refreshViewItems() {
		
		App.mainViewModel.items.clear();
		App.mainViewModel.listItems.clear();
		App.mainViewModel.pictureItems.clear();
		App.mainViewModel.listPictureItems.clear();
		
		// 1.状态部分
		App.mainViewModel.listItems.addAll(App.mainViewModel.sinaWeiboItems);
		App.mainViewModel.listItems.addAll(App.mainViewModel.renrenItems);
		App.mainViewModel.listItems.addAll(App.mainViewModel.doubanItems);
		App.mainViewModel.listItems.addAll(App.mainViewModel.rssItems);
		Collections.sort(App.mainViewModel.listItems, new Comparator<ItemViewModel>() {
			@Override
			public int compare(ItemViewModel lhs, ItemViewModel rhs) {
				try {
					int result = lhs.time.compareTo(rhs.time);	
					return -result;
				} catch (Exception e) {
					return 0;
				}
			}
		});
		App.mainViewModel.items.addAll(App.mainViewModel.listItems);
		
		// 2.图片部分
		App.mainViewModel.listPictureItems.addAll(App.mainViewModel.sinaWeiboPictureItems);
		App.mainViewModel.listPictureItems.addAll(App.mainViewModel.renrenPictureItems);
		App.mainViewModel.listPictureItems.addAll(App.mainViewModel.doubanPictureItems);
		Collections.sort(App.mainViewModel.listPictureItems, new Comparator<PictureItemViewModel>() {
			@Override
			public int compare(PictureItemViewModel lhs, PictureItemViewModel rhs) {
				try {
					int result = lhs.time.compareTo(rhs.time);	
					return -result;
				} catch (Exception e) {
					return 0;
				}
			}
		});
		App.mainViewModel.pictureItems.addAll(App.mainViewModel.listPictureItems);
		
		notifyListeners();
	}
	
	
	private void notifyListeners() {
		for (OnRefreshCompleteListener listener : listListeners) {
			if(listener != null)
				listener.onRefreshComplete();
		}
	}


	public interface OnRefreshCompleteListener
	{
		public void onRefreshComplete();
	}


}
