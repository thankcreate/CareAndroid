package com.thankcreate.care.tool.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;

import android.R.bool;
import android.R.integer;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.JsonReader;
import android.util.Log;

import com.dongxuexidu.douban4j.constants.DefaultConfigs;
import com.dongxuexidu.douban4j.utils.HttpManager;
import com.hp.hpl.sparta.xpath.ThisNodeTest;
import com.renren.api.connect.android.AsyncRenren;
import com.renren.api.connect.android.exception.RenrenError;
import com.thankcreate.care.App;
import com.thankcreate.care.AppConstants;
import com.thankcreate.care.tool.converter.DoubanConverter;
import com.thankcreate.care.tool.converter.RenrenConverter;
import com.thankcreate.care.tool.converter.RssConverter;
import com.thankcreate.care.tool.converter.SinaWeiboConverter;
import com.thankcreate.care.tool.misc.MiscTool;
import com.thankcreate.care.tool.misc.PreferenceHelper;
import com.thankcreate.care.tool.misc.StringTool;
import com.thankcreate.care.tool.misc.TaskHelper;
import com.thankcreate.care.tool.misc.TaskHelper.OnTaskCompleteListener;
import com.thankcreate.care.viewmodel.FriendViewModel;
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
	
	public boolean isLoading = false;
	public boolean isComplete = false;
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
		if(!MiscTool.isOnline())
		{
			notifyListeners();
			return;
		}
		
		if(isLoading)
			return;
		isLoading = true;
		App.mainViewModel.isChanged = false;
		taskHelper.clear();
		taskHelper.pushTask("SinaWeibo");
		taskHelper.pushTask("Renren");
		taskHelper.pushTask("Douban");
		taskHelper.pushTask("Rss");
		
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
				0, 0, 30, 1, false, FEATURE.ALL, false, mSinaWeiboUserTimeLineListener);
	}
	
	private RequestListener mSinaWeiboUserTimeLineListener = new RequestListener() {
		

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
		App.mainViewModel.rssItems.clear();
		final String url = PreferenceHelper.getString("RSS_FollowerPath");		
		if(StringTool.isNullOrEmpty(url))
		{
			taskHelper.popTask("Rss");
			return;
		}
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					RSSReader rssReader = new RSSReader();
					RSSFeed rssFeed = rssReader.load(url);
					if(rssFeed.getItems() != null && rssFeed.getItems().size() != 0)
					{
						for(int i = 0; i < rssFeed.getItems().size(); i++)
						{
							RSSItem item = rssFeed.getItems().get(i);
							ItemViewModel model = RssConverter.convertStatusToCommon(item);
							if(model != null)
								App.mainViewModel.rssItems.add(model);
						}
					}			
					taskHelper.popTask();
				} catch (Exception e) {
					ToastHelper.show(">_<  RSS订阅信息获取发生未知错误");
					taskHelper.popTask("Rss");
				}					
			}
		}).start();
	}
	
	private void refreshModelRenren() {
		App.mainViewModel.renrenItems.clear();
		App.mainViewModel.renrenPictureItems.clear();
		// 1.判断是否登陆
		Boolean isLoggin = MiscTool.isRenrenLogin();
		if(!isLoggin)
		{
			taskHelper.popTask("Renren");
			return;			
		}			
		
		// 2.判断是否关注了用户
		String strFollowerID = PreferenceHelper.getString("Renren_FollowerID");
		if(StringTool.isNullOrEmpty(strFollowerID))
		{			
			taskHelper.popTask("Renren");
			return;
		}
		
		// 3.判断是否过期
		long exp = PreferenceHelper.getLong("Renren_ExpirationDate");
		if(exp < System.currentTimeMillis())
		{
			ToastHelper.show(">_<  人人帐号授权已过期，请到帐号页重新登陆以授权");			
			PreferenceHelper.removeRenrenPreference();
			taskHelper.popTask("Renren");			
			return;
		}
		
		AsyncRenren asyncRenren = new AsyncRenren(App.getRenren());
		Bundle bd = new Bundle();	
		bd.putString("method", "feed.get");
		bd.putString("type", "10,30,32");
		bd.putString("uid", strFollowerID);
		bd.putString("count", "30");
		asyncRenren.requestJSON(bd, mRenrenUserTimeLineListener);
		
	}
	
	private com.renren.api.connect.android.RequestListener mRenrenUserTimeLineListener = new com.renren.api.connect.android.RequestListener() {
				
		@Override
		public void onRenrenError(RenrenError renrenError) {
			taskHelper.popTask("Renren");		
			ToastHelper.show(">_<  人人信息获取发生未知错误");
		}
		
		@Override
		public void onFault(Throwable fault) {
			taskHelper.popTask("Renren");		
			ToastHelper.show(">_<  人人信息获取发生未知错误");			
		}
		
		@Override
		public void onComplete(String response) {
			try {				
				JSONArray statuses = new JSONArray(response);
				if(statuses != null)
				{
					for(int i = 0; i < statuses.length(); i++)
					{
						JSONObject ob = statuses.getJSONObject(i);
						ItemViewModel model = RenrenConverter.convertStatusToCommon(ob);
						if(model != null)
						{
							App.mainViewModel.renrenItems.add(model);
						}
					}
				}
				taskHelper.popTask("Renren");
			} catch (Exception e) {
				taskHelper.popTask("Renren");		
				ToastHelper.show(">_<  人人信息获取发生未知错误");
			}
			
		}
	};

	private void refreshModelDouban() {
		App.mainViewModel.doubanItems.clear();
		App.mainViewModel.doubanPictureItems.clear();
		// 1.判断是否登陆
		Boolean isLoggin = MiscTool.isDoubanLogin();
		if(!isLoggin)
		{
			taskHelper.popTask("Douban");
			return;			
		}			
		
		// 2.判断是否关注了用户
		final String strFollowerID = PreferenceHelper.getString("Douban_FollowerID");
		if(StringTool.isNullOrEmpty(strFollowerID))
		{			
			taskHelper.popTask("Douban");
			return;
		}
		
		// 3.判断是否过期
		long exp = PreferenceHelper.getLong("Douban_ExpirationDate");
		if(exp < System.currentTimeMillis())
		{
			ToastHelper.show(">_<  豆瓣帐号授权已过期，请到帐号页重新登陆以授权");			
			PreferenceHelper.removeDoubanPreference();
			taskHelper.popTask("Douban");			
			return;
		}
		
		final String token = PreferenceHelper.getString("Douban_Token");
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpManager httpManager = new HttpManager(token);
					String url = String.format("%s/shuo/v2/statuses/user_timeline/%s", 
							DefaultConfigs.API_URL_PREFIX, strFollowerID);
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("count", "50"));
					String result = httpManager.getResponseString(url, params, true);
					
					JSONArray statuses = new JSONArray(result);
					if(statuses != null)
					{
						for(int i = 0; i < statuses.length(); i++)
						{
							JSONObject ob = statuses.getJSONObject(i);
							ItemViewModel model = DoubanConverter.convertStatusToCommon(ob);
							if(model != null)
							{
								App.mainViewModel.doubanItems.add(model);
							}
						}
					}
					taskHelper.popTask("Douban");
				} catch (Exception e) {
					e.printStackTrace();
					taskHelper.popTask("Douban");		
					ToastHelper.show(">_<  豆瓣信息获取发生未知错误");
				}				
			}
		}).start();
	}

	@Override
	public void onAllTaskComplete() {
		isLoading = false;
		refreshViewItems();
	}
	
	private void refreshViewItems() {
		
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
		
		// 3.存缓存
		try {
			File myDir = App.getAppContext().getCacheDir();		
			File cacheFile = new File(myDir , AppConstants.CACHE_ITEM);
			FileOutputStream fos = new FileOutputStream(cacheFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(App.mainViewModel.items);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		notifyListeners();		
	}
	
	
	private void notifyListeners() {
		for (OnRefreshCompleteListener listener : listListeners) {
			if(listener != null)
				listener.onRefreshComplete();
		}
		isComplete = true;
	}


	public interface OnRefreshCompleteListener
	{
		public void onRefreshComplete();
	}


}
