package com.thankcreate.care.service;

import com.thankcreate.care.App;
import com.thankcreate.care.AppConstants;
import com.thankcreate.care.tool.ui.ToastHelper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

public class CareStartupReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		PendingIntent alarmSender;
		alarmSender = PendingIntent.getService(context, 0, new Intent(context,
				NewsPollingService.class), 0);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
		String usePolling = pref.getString("Global_UsePolling", "True");
		if(usePolling.equalsIgnoreCase("True"))
		{			
			long interval = pref.getLong("Global_PollingTime", AppConstants.DEFAULT_POLLING_INTERVAL);			
			long firstTime = SystemClock.elapsedRealtime() + interval;
			am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
	                firstTime , interval, alarmSender);
		}
	}

}
