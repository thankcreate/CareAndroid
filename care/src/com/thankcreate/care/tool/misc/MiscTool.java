package com.thankcreate.care.tool.misc;

import java.net.ContentHandler;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.thankcreate.care.App;
import com.thankcreate.care.AppConstants;
import com.thankcreate.care.account.AccountActivity;
import com.thankcreate.care.viewmodel.EntryType;
import com.weibo.sdk.android.Oauth2AccessToken;

public class MiscTool {

	public static Boolean isSinaWeiboLogin() {
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
		String token = pref.getString("SinaWeibo_Token", "");
		return !StringTool.isNullOrEmpty(token);
	}

	public static Boolean isRenrenLogin() {
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
		String token = pref.getString("Renren_Token", "");
		return !StringTool.isNullOrEmpty(token);
	}

	public static Boolean isDoubanLogin() {
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
		String token = pref.getString("Douban_Token", "");
		return !StringTool.isNullOrEmpty(token);
	}
	
	public static Boolean isAuthValid(int type){
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
		if(type == EntryType.SinaWeibo)
		{
			String token = pref.getString("SinaWeibo_Token", "");
			if(StringTool.isNullOrEmpty(token))
				return false;
			Long exp = pref.getLong("SinaWeibo_ExpirationDate", -1);			
			if(exp == -1 || exp <  System.currentTimeMillis())
				return false;
			return true;
		}		
		else if(type == EntryType.Renren)
		{
			String token = pref.getString("Renren_Token", "");
			if(StringTool.isNullOrEmpty(token))
				return false;
			Long exp = pref.getLong("Renren_ExpirationDate", -1);			
			if(exp == -1 || exp <  System.currentTimeMillis())
				return false;
			return true;
		}
		else if(type == EntryType.Douban)
		{
			String token = pref.getString("Douban_Token", "");
			if(StringTool.isNullOrEmpty(token))
				return false;
			Long exp = pref.getLong("Douban_ExpirationDate", -1);			
			if(exp == -1 || exp <  System.currentTimeMillis())
				return false;
			return true;
		}
		return false;
	}

	public static String getMyID(int type) {
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
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
	
	public static String getHerID(int type) {
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
		String id;
		switch (type) {
		case EntryType.SinaWeibo:
			id = pref.getString("SinaWeibo_FollowerID", "");
			break;
		case EntryType.Renren:
			id = pref.getString("Renren_FollowerID", "");
			break;
		case EntryType.Douban:
			id = pref.getString("Douban_FollowerID", "");
			break;
		default:
			id = "";
			break;
		}
		return id;
	}
	
	public static String getHerName(int type) {
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
		String name;
		switch (type) {
		case EntryType.SinaWeibo:
			name = pref.getString("SinaWeibo_FollowerNickName", "");
			break;
		case EntryType.Renren:
			name = pref.getString("Renren_FollowerNickName", "");
			break;
		case EntryType.Douban:
			name = pref.getString("Douban_FollowerNickName", "");
			break;
		default:
			name = "";
			break;
		}
		return name;
	}
	
	

	/**
	 * 依SinaWeibo, Renren, Douban的顺序查出第一个可用的已登陆帐号
	 * 
	 * @return 如果没有任何登陆，返回EntryType.NotSet
	 */
	public static int getFirstFoundLoginType() {
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
		String token = pref.getString("SinaWeibo_Token", "");
		if (!StringTool.isNullOrEmpty(token)) {
			return EntryType.SinaWeibo;
		}

		token = pref.getString("Renren_Token", "");
		if (!StringTool.isNullOrEmpty(token)) {
			return EntryType.Renren;
		}

		token = pref.getString("Douban_Token", "");
		if (!StringTool.isNullOrEmpty(token)) {
			return EntryType.Douban;
		}

		return EntryType.NotSet;
	}

	public static String getMyName(int type)
	{
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
		String name;
		switch (type) {
		case EntryType.SinaWeibo:
			name = pref.getString("SinaWeibo_NickName", "");
			break;
		case EntryType.Renren:
			name = pref.getString("Renren_NickName", "");
			break;
		case EntryType.Douban:
			name = pref.getString("Douban_NickName", "");
			break;
		default:
			name = "";
			break;
		}
		return name;
	}
	
	public static String getMyName() {
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
		String name = pref.getString("SinaWeibo_NickName", "");
		if (!StringTool.isNullOrEmpty(name))
			return name;

		name = pref.getString("Renren_NickName", "");
		if (!StringTool.isNullOrEmpty(name))
			return name;

		name = pref.getString("Douban_NickName", "");
		if (!StringTool.isNullOrEmpty(name))
			return name;

		return "";
	}

	public static String getHerName() {
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
		String name = pref.getString("SinaWeibo_FollowerNickName", "");
		if (!StringTool.isNullOrEmpty(name))
			return name;

		name = pref.getString("Renren_FollowerNickName", "");
		if (!StringTool.isNullOrEmpty(name))
			return name;

		name = pref.getString("Douban_FollowerNickName", "");
		if (!StringTool.isNullOrEmpty(name))
			return name;

		return "";
	}


	
	public static String getMyIconUrl() {
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
		String herIcon = pref.getString("SinaWeibo_Avatar", "");
		if (!StringTool.isNullOrEmpty(herIcon)) {
			return herIcon;
		}

		herIcon = pref.getString("Renren_Avatar", "");
		if (!StringTool.isNullOrEmpty(herIcon)) {
			return herIcon;
		}

		herIcon = pref.getString("Douban_Avatar", "");
		if (!StringTool.isNullOrEmpty(herIcon)) {
			return herIcon;
		}
		
		return "";
	}
	
	public static String getHerIconUrl(int type) {
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
		String herIcon = "";
		if(type == EntryType.SinaWeibo)
		{
			herIcon = pref.getString("SinaWeibo_FollowerAvatar2", "");
			if (!StringTool.isNullOrEmpty(herIcon)) {
				return herIcon;
			}
			

			herIcon = pref.getString("SinaWeibo_FollowerAvatar", "");
			if (!StringTool.isNullOrEmpty(herIcon)) {
				return herIcon;
			}
		}
		else if(type == EntryType.Renren)
		{
			herIcon = pref.getString("Renren_FollowerAvatar2", "");
			if (!StringTool.isNullOrEmpty(herIcon)) {
				return herIcon;
			}
			
			herIcon = pref.getString("Renren_FollowerAvatar", "");
			if (!StringTool.isNullOrEmpty(herIcon)) {
				return herIcon;
			}
		}
		else if(type == EntryType.Douban)
		{
			herIcon = pref.getString("Douban_FollowerAvatar2", "");
			if (!StringTool.isNullOrEmpty(herIcon)) {
				return herIcon;
			}

			herIcon = pref.getString("Douban_FollowerAvatar", "");
			if (!StringTool.isNullOrEmpty(herIcon)) {
				return herIcon;
			}
		}
		return "";
	}
	
	public static String getHerIconUrl() {
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
		String herIcon = pref.getString("SinaWeibo_FollowerAvatar2", "");
		if (!StringTool.isNullOrEmpty(herIcon)) {
			return herIcon;
		}

		herIcon = pref.getString("Renren_FollowerAvatar2", "");
		if (!StringTool.isNullOrEmpty(herIcon)) {
			return herIcon;
		}

		herIcon = pref.getString("Douban_FollowerAvatar2", "");
		if (!StringTool.isNullOrEmpty(herIcon)) {
			return herIcon;
		}

		herIcon = pref.getString("SinaWeibo_FollowerAvatar", "");
		if (!StringTool.isNullOrEmpty(herIcon)) {
			return herIcon;
		}

		herIcon = pref.getString("Renren_FollowerAvatar", "");
		if (!StringTool.isNullOrEmpty(herIcon)) {
			return herIcon;
		}

		herIcon = pref.getString("Douban_FollowerAvatar", "");
		if (!StringTool.isNullOrEmpty(herIcon)) {
			return herIcon;
		}
		return "";
	}

	public static Oauth2AccessToken getOauth2AccessToken() {
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
		String token = pref.getString("SinaWeibo_Token", "");
		long exp = pref.getLong("SinaWeibo_ExpirationDate", -1);
		if (token.equalsIgnoreCase("") || exp == -1) {
			return null;
		} else {
			Oauth2AccessToken oa = new Oauth2AccessToken();
			oa.setExpiresTime(exp);
			oa.setToken(token);
			return oa;
		}
	}
	
	public static boolean isOnline() {
	    ConnectivityManager cm =
	        (ConnectivityManager) App.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
}
