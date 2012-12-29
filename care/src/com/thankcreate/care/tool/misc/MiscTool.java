package com.thankcreate.care.tool.misc;

import java.net.ContentHandler;

import android.content.Context;
import android.content.SharedPreferences;

import com.thankcreate.care.App;
import com.thankcreate.care.AppConstants;
import com.thankcreate.care.account.AccountActivity;
import com.thankcreate.care.viewmodel.EntryType;
import com.weibo.sdk.android.Oauth2AccessToken;

public class MiscTool {
	

	
	public static Boolean isSinaWeiboLogin()
	{
		SharedPreferences pref = App.getAppContext().getSharedPreferences(AppConstants.PREFERENCES_NAME,
						Context.MODE_APPEND);
		String token = pref.getString("SinaWeibo_Token", "");
		return !StringTool.isNullOrEmpty(token);
	}
	
	
	public static String getCurrentAccountID(int type)
	{
		SharedPreferences pref = App.getAppContext().getSharedPreferences(AppConstants.PREFERENCES_NAME,
				Context.MODE_APPEND);
		String id;
		switch (type) {
		case EntryType.SinaWeibo:
			id = pref.getString("SinaWeibo_ID", "");
			break;
		case EntryType.Renren:
			id = pref.getString("Renren_ID", "");
			break;
		case EntryType.Douban:
			id = pref.getString("Douban_ID", "");
			break;		
		default:
			id = "";
			break;
		}
		return id;
	}

	public static Oauth2AccessToken getOauth2AccessToken() 
	{
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
		String token = pref.getString("SinaWeibo_Token", "");
		long exp = pref.getLong("SinaWeibo_ExpirationDate", -1);
		if(token.equalsIgnoreCase("") || exp == -1)
		{
			return null;
		}
		else
		{
			Oauth2AccessToken oa = new Oauth2AccessToken();
			oa.setExpiresTime(exp);
			oa.setToken(token);
			return oa;
		}
	}
}
