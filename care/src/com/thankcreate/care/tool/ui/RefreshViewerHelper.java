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
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.JsonReader;
import android.util.Log;

import com.dongxuexidu.douban4j.constants.DefaultConfigs;
import com.dongxuexidu.douban4j.utils.HttpManager;
import com.hp.hpl.sparta.xpath.ThisNodeTest;
import com.renren.api.connect.android.AsyncRenren;
import com.renren.api.connect.android.exception.RenrenError;
import com.thankcreate.care.App;
import com.thankcreate.care.AppConstants;
import com.thankcreate.care.service.NewsPollingService;
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

/**
 * 由于安卓版“我只在乎你”可以实现推送机制，这里的RefreshViewerHelper分为两类
 * 一类是供前台程序运行的RefreshViewerHelper，它直接与App的MainViewModel绑定
 * 另一类是供后台Service运行的RefreshViewerHelper，它刷新的是后台Service的那个临时MainViewModel
 * 今后，安卓版所有的刷新操作，都不能直接拿App的MainViewModel，而是要一步步传参传过去
 * 注意，此类中所有的涉及UI的操作，都应该判断mType的状态，只有前台RefreshViewerHelper才能触发Toast
 * 
 * @author ThankCreate
 */
public class RefreshViewerHelper implements OnTaskCompleteListener {

	private static final int FORGRAOUND = 1;
	private static final int BACKGROUND = 2;
	private int mType;
	private TaskHelper taskHelper;

	public boolean isLoading = false;
	public boolean isComplete = false;

	private MainViewModel mainViewModel;
	private List<OnRefreshCompleteListener> listListeners = new ArrayList<RefreshViewerHelper.OnRefreshCompleteListener>();
	private static RefreshViewerHelper sAppRefreshViewerHelper = null;
	private static RefreshViewerHelper sServiceRefreshViewerHelper = null;

	public static RefreshViewerHelper getAppInstance() {
		if (sAppRefreshViewerHelper == null) {
			sAppRefreshViewerHelper = new RefreshViewerHelper();
			sAppRefreshViewerHelper.mType = FORGRAOUND;
			sAppRefreshViewerHelper.mainViewModel = App.mainViewModel;
		}
		return sAppRefreshViewerHelper;
	}

	public static RefreshViewerHelper getServiceInstance(
			MainViewModel mainViewModel) {
		if (sServiceRefreshViewerHelper == null) {
			sServiceRefreshViewerHelper = new RefreshViewerHelper();
			sServiceRefreshViewerHelper.mType = BACKGROUND;
		}
		sServiceRefreshViewerHelper.mainViewModel = mainViewModel;
		return sServiceRefreshViewerHelper;
	}

	private RefreshViewerHelper() {
		super();
		taskHelper = new TaskHelper(this);
	}

	public void addListenter(OnRefreshCompleteListener listener) {
		listListeners.add(listener);
	}

	public void removeListenter(OnRefreshCompleteListener listener) {
		listListeners.remove(listener);
	}

	public void refreshMainViewModel() {
		if (!MiscTool.isOnline()) {
			notifyListeners();
			return;
		}

		if (isLoading)
			return;
		isLoading = true;
		mainViewModel.isChanged = false;
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
		mainViewModel.sinaWeiboItems.clear();
		mainViewModel.sinaWeiboPictureItems.clear();
		// 1.判断是否登陆
		Boolean isLoggin = MiscTool.isSinaWeiboLogin();
		if (!isLoggin) {
			taskHelper.popTask("SinaWeibo");
			return;
		}

		// 2.判断是否关注了用户
		String strFollowerID = PreferenceHelper
				.getString("SinaWeibo_FollowerID");
		if (StringTool.isNullOrEmpty(strFollowerID)) {
			taskHelper.popTask("SinaWeibo");
			return;
		}

		// 3.判断是否过期
		long exp = PreferenceHelper.getLong("SinaWeibo_ExpirationDate");
		if (exp < System.currentTimeMillis()) {
			if (mType == FORGRAOUND) {
				ToastHelper.show(">_<  新浪微博授权已过期，请到帐号页重新登陆以授权");
				PreferenceHelper.removeSinaWeiboPreference();
			}
			taskHelper.popTask("SinaWeibo");
			return;
		}

		Oauth2AccessToken oa = MiscTool.getOauth2AccessToken();
		if (oa == null)
			return;

		StatusesAPI statusesAPI = new StatusesAPI(oa);
		// 新浪微博最多一次加载100
		statusesAPI.userTimeline(Long.parseLong(strFollowerID), 0, 0, 80, 1,
				false, FEATURE.ALL, false, mSinaWeiboUserTimeLineListener);
	}

	private RequestListener mSinaWeiboUserTimeLineListener = new RequestListener() {

		@Override
		public void onComplete(String arg0) {
			try {
				JSONObject root = new JSONObject(arg0);
				JSONArray listStatus = root.optJSONArray("statuses");
				if (listStatus == null || listStatus.length() == 0)
					return;
				for (int i = 0; i < listStatus.length(); i++) {
					JSONObject status = listStatus.getJSONObject(i);
					ItemViewModel model = SinaWeiboConverter
							.convertStatusToCommon(status, mainViewModel);
					if (model != null) {
						mainViewModel.sinaWeiboItems.add(model);
					}
				}
			} catch (Exception e) {
				if (mType == FORGRAOUND) {
					ToastHelper.show(">_<  新浪微博信息获取发生未知错误");
				}
				e.printStackTrace();
			} finally {
				taskHelper.popTask("SinaWeibo");
			}

		}

		@Override
		public void onIOException(IOException arg0) {
			taskHelper.popTask("SinaWeibo");
			if (mType == FORGRAOUND) {
				ToastHelper.show(">_<  新浪微博信息获取发生未知错误");
			}
		}

		@Override
		public void onError(WeiboException arg0) {
			// TODO: 验证statusCode是否有用
			taskHelper.popTask("SinaWeibo");
			if (mType == FORGRAOUND) {
				if (arg0.getStatusCode() == 21327) {
					ToastHelper.show(">_<  新浪微博授权已过期，请到帐号页重新登陆以授权");
				} else {
					ToastHelper.show(">_<  新浪微博信息获取发生未知错误");
				}
			}
		}
	};

	private void refreshModelRssFeed() {
		mainViewModel.rssItems.clear();
		final String url = PreferenceHelper.getString("RSS_FollowerPath");
		if (StringTool.isNullOrEmpty(url)) {
			taskHelper.popTask("Rss");
			return;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					RSSReader rssReader = new RSSReader();
					RSSFeed rssFeed = rssReader.load(url);
					if (rssFeed.getItems() != null
							&& rssFeed.getItems().size() != 0) {
						for (int i = 0; i < rssFeed.getItems().size(); i++) {
							RSSItem item = rssFeed.getItems().get(i);
							ItemViewModel model = RssConverter
									.convertStatusToCommon(item);
							if (model != null)
								mainViewModel.rssItems.add(model);
						}
					}
					taskHelper.popTask();
				} catch (Exception e) {
					if (mType == FORGRAOUND) {
						ToastHelper.show(">_<  RSS订阅信息获取发生未知错误");
					}
					taskHelper.popTask("Rss");
				}
			}
		}).start();
	}

	private void refreshModelRenren() {
		mainViewModel.renrenItems.clear();
		mainViewModel.renrenPictureItems.clear();
		// 1.判断是否登陆
		Boolean isLoggin = MiscTool.isRenrenLogin();
		if (!isLoggin) {
			taskHelper.popTask("Renren");
			return;
		}

		// 2.判断是否关注了用户
		String strFollowerID = PreferenceHelper.getString("Renren_FollowerID");
		if (StringTool.isNullOrEmpty(strFollowerID)) {
			taskHelper.popTask("Renren");
			return;
		}

		// 3.判断是否过期
		long exp = PreferenceHelper.getLong("Renren_ExpirationDate");
		if (exp < System.currentTimeMillis()) {
			if (mType == FORGRAOUND) {
				ToastHelper.show(">_<  人人帐号授权已过期，请到帐号页重新登陆以授权");
				PreferenceHelper.removeRenrenPreference();
			}
			taskHelper.popTask("Renren");
			return;
		}

		AsyncRenren asyncRenren = new AsyncRenren(App.getRenren());
		Bundle bd = new Bundle();
		bd.putString("method", "feed.get");
		bd.putString("type", "10,30,32");
		bd.putString("uid", strFollowerID);
		bd.putString("count", "50"); // 人人最多就为50
		asyncRenren.requestJSON(bd, mRenrenUserTimeLineListener);

	}

	private com.renren.api.connect.android.RequestListener mRenrenUserTimeLineListener = new com.renren.api.connect.android.RequestListener() {

		@Override
		public void onRenrenError(RenrenError renrenError) {
			taskHelper.popTask("Renren");
			if (mType == FORGRAOUND) {
				ToastHelper.show(">_<  人人信息获取发生未知错误");
			}
		}

		@Override
		public void onFault(Throwable fault) {
			taskHelper.popTask("Renren");
			if (mType == FORGRAOUND) {
				ToastHelper.show(">_<  人人信息获取发生未知错误");
			}
		}

		@Override
		public void onComplete(String response) {
			try {
				JSONArray statuses = new JSONArray(response);
				if (statuses != null) {
					for (int i = 0; i < statuses.length(); i++) {
						JSONObject ob = statuses.getJSONObject(i);
						ItemViewModel model = RenrenConverter
								.convertStatusToCommon(ob, mainViewModel);
						if (model != null) {
							mainViewModel.renrenItems.add(model);
						}
					}
				}
				taskHelper.popTask("Renren");
			} catch (Exception e) {
				taskHelper.popTask("Renren");
				if (mType == FORGRAOUND) {
					ToastHelper.show(">_<  人人信息获取发生未知错误");
				}
			}

		}
	};

	private void refreshModelDouban() {
		mainViewModel.doubanItems.clear();
		mainViewModel.doubanPictureItems.clear();
		// 1.判断是否登陆
		Boolean isLoggin = MiscTool.isDoubanLogin();
		if (!isLoggin) {
			taskHelper.popTask("Douban");
			return;
		}

		// 2.判断是否关注了用户
		final String strFollowerID = PreferenceHelper
				.getString("Douban_FollowerID");
		if (StringTool.isNullOrEmpty(strFollowerID)) {
			taskHelper.popTask("Douban");
			return;
		}

		// 3.判断是否过期
		long exp = PreferenceHelper.getLong("Douban_ExpirationDate");
		if (exp < System.currentTimeMillis()) {
			if (mType == FORGRAOUND) {
				ToastHelper.show(">_<  豆瓣帐号授权已过期，请到帐号页重新登陆以授权");
				PreferenceHelper.removeDoubanPreference();
			}
			taskHelper.popTask("Douban");
			return;
		}

		final String token = PreferenceHelper.getString("Douban_Token");
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpManager httpManager = new HttpManager(token);
					String url = String.format(
							"%s/shuo/v2/statuses/user_timeline/%s",
							DefaultConfigs.API_URL_PREFIX, strFollowerID);
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("count", "100")); // 豆瓣最大一次加载200
					String result = httpManager.getResponseString(url, params,
							true);

					JSONArray statuses = new JSONArray(result);
					if (statuses != null) {
						for (int i = 0; i < statuses.length(); i++) {
							JSONObject ob = statuses.getJSONObject(i);
							ItemViewModel model = DoubanConverter
									.convertStatusToCommon(ob, mainViewModel);
							if (model != null) {
								mainViewModel.doubanItems.add(model);
							}
						}
					}
					taskHelper.popTask("Douban");
				} catch (Exception e) {
					e.printStackTrace();
					taskHelper.popTask("Douban");
					if (mType == FORGRAOUND) {
						ToastHelper.show(">_<  豆瓣信息获取发生未知错误");
					}
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

		mainViewModel.items.clear();
		mainViewModel.listItems.clear();
		mainViewModel.pictureItems.clear();
		mainViewModel.listPictureItems.clear();

		// 1.状态部分
		mainViewModel.listItems.addAll(mainViewModel.sinaWeiboItems);
		mainViewModel.listItems.addAll(mainViewModel.renrenItems);
		mainViewModel.listItems.addAll(mainViewModel.doubanItems);
		mainViewModel.listItems.addAll(mainViewModel.rssItems);
		Collections.sort(mainViewModel.listItems,
				new Comparator<ItemViewModel>() {
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
		mainViewModel.items.addAll(mainViewModel.listItems);


		// 2.图片部分
		mainViewModel.listPictureItems
				.addAll(mainViewModel.sinaWeiboPictureItems);
		mainViewModel.listPictureItems.addAll(mainViewModel.renrenPictureItems);
		mainViewModel.listPictureItems.addAll(mainViewModel.doubanPictureItems);
		Collections.sort(mainViewModel.listPictureItems,
				new Comparator<PictureItemViewModel>() {
					@Override
					public int compare(PictureItemViewModel lhs,
							PictureItemViewModel rhs) {
						try {
							int result = lhs.time.compareTo(rhs.time);
							return -result;
						} catch (Exception e) {
							return 0;
						}
					}
				});
		mainViewModel.pictureItems.addAll(mainViewModel.listPictureItems);

		// 3.存缓存
		try {
			File myDir = App.getAppContext().getFilesDir();
			File cacheFile = new File(myDir, AppConstants.CACHE_ITEM);
			FileOutputStream fos = new FileOutputStream(cacheFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(mainViewModel.items);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 4.后台轮询服务相关
		
		// 4.1
		// 如果是FORGROUND，清掉Notification
		if (mType == FORGRAOUND) {
			try {
				NotificationManager mNotificationManager = (NotificationManager) App
						.getAppContext().getSystemService(
								Context.NOTIFICATION_SERVICE);
				mNotificationManager.cancel(NewsPollingService.NOTIFICATION_ID);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// 4.2找到第一个time非空的项，更新最新消息的参数项，方便后台service判断是否有更新
		// 前台refresh和后台refresh的最新item时间点是分别保存的		
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND | Context.MODE_MULTI_PROCESS);
		for (int i = 0; i < mainViewModel.items.size(); i++) {
			ItemViewModel item = mainViewModel.items.get(i);
			if (item != null && item.time != null) {
				if (mType == FORGRAOUND) {
					Long lastTime = pref.getLong("Global_LastTimeLatestForegound", -1);
					if (item.time.getTime() > lastTime) {
						Editor editor = pref.edit();												
						editor.putLong("Global_LastTimeLatestForegound",
								item.time.getTime());
						editor.commit();
					}
				}
				else {
					Long lastTime = pref.getLong("Global_LastTimeLatestBackgound", -1);
					if (item.time.getTime() > lastTime) {
						Editor editor = pref.edit();
						editor.putLong("Global_LastTimeLatestBackgound",
								item.time.getTime());
						editor.commit();
					}
				}
				break;
			}
		}
		notifyListeners();
	}

	private void notifyListeners() {
		for (OnRefreshCompleteListener listener : listListeners) {
			if (listener != null)
				listener.onRefreshComplete();
		}
		isComplete = true;
	}

	public interface OnRefreshCompleteListener {
		public void onRefreshComplete();
	}

}
