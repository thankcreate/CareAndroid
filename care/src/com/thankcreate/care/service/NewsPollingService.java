package com.thankcreate.care.service;

import java.security.Timestamp;
import java.util.Date;

import com.google.common.base.Objects.ToStringHelper;
import com.thankcreate.care.App;
import com.thankcreate.care.AppConstants;
import com.thankcreate.care.DispatcherActivity;
import com.thankcreate.care.R;
import com.thankcreate.care.tool.misc.MiscTool;
import com.thankcreate.care.tool.misc.PreferenceHelper;
import com.thankcreate.care.tool.ui.RefreshViewerHelper;
import com.thankcreate.care.tool.ui.RefreshViewerHelper.OnRefreshCompleteListener;

import com.thankcreate.care.viewmodel.ItemViewModel;
import com.thankcreate.care.viewmodel.MainViewModel;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.text.format.Time;


public class NewsPollingService extends Service implements OnRefreshCompleteListener {
	
	// 此mainViewModel区分于App.mainViewModel，专门用来给后台刷新
	private MainViewModel mainViewModel;
	private RefreshViewerHelper refreshViewerHelper;

	// 借用R.string.notification_title保证ID的唯一性
	public static final int NOTIFICATION_ID = R.string.notification_title;
	
	private NotificationManager mNotificationManager;

	@Override
	public void onCreate() {
		super.onCreate();
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mainViewModel = new MainViewModel();
		refreshViewerHelper = RefreshViewerHelper.getServiceInstance(mainViewModel);
		refreshViewerHelper.addListenter(this);
		refreshViewerHelper.refreshMainViewModel();		
	}

	@Override
	public void onDestroy() {
		// 解除循环引用
		refreshViewerHelper.removeListenter(this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification() {
		String content = "test1";
		String title = "test2";

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.thumb_notification_logo,
				content, System.currentTimeMillis());
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, DispatcherActivity.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, title, content, contentIntent);


		mNotificationManager.notify(NOTIFICATION_ID, notification);		
		stopSelf();
	}

	/**
	 * 这里的原则是避免多报
	 */
	@Override
	public void onRefreshComplete() {
		if(mainViewModel.items == null || mainViewModel.items.size() == 0)
		{
			stopSelf();
			return;
		}
		SharedPreferences pref = getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND | Context.MODE_MULTI_PROCESS);
		
		Long lastTimeLatestForeground = pref.getLong("Global_LastTimeLatestForegound", -1);		
		Long lastTimeLatestBackground = pref.getLong("Global_LastTimeLatestBackgound", -1);
		if(lastTimeLatestForeground == -1 || lastTimeLatestBackground == -1)
		{
			stopSelf();
			return;
		}
				
		int newsCount = 0;
		for(int i = 0; i < mainViewModel.items.size(); i++)
		{
			ItemViewModel item = mainViewModel.items.get(i);
			if(item == null || item.time == null)
			{
				continue;
			}
			Long itemTime = item.time.getTime();
			if(itemTime > lastTimeLatestForeground && itemTime <= lastTimeLatestBackground)
			{
				++newsCount;
			}
		}
		if(newsCount == 0)
		{
			stopSelf();
			return;
		}
		
		Long lastNotificationTime  = pref.getLong("Global_LastNotifcationTime", -1);
		if(lastNotificationTime >= lastTimeLatestBackground)
		{
			stopSelf();
			return;
		}
		Editor editor = pref.edit();
		editor.putLong("Global_LastNotifcationTime", lastTimeLatestBackground);
		editor.commit();
		
		String content = String.format("%s更新了%d条新鲜事.", MiscTool.getHerName(), newsCount);
		String title = "我只在乎你";

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.thumb_notification_logo,
				content, System.currentTimeMillis());
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, DispatcherActivity.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, title, content, contentIntent);


		mNotificationManager.notify(NOTIFICATION_ID, notification);
		stopSelf();
	}
}
