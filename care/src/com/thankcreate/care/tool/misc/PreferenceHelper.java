package com.thankcreate.care.tool.misc;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.thankcreate.care.App;
import com.thankcreate.care.AppConstants;
import com.thankcreate.care.account.AccountActivity;

public class PreferenceHelper {
	
    public static void removeSinaWeiboPreference()
    {
    	SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
    	Editor editor = pref.edit();    	
        editor.remove("SinaWeibo_NickName");
    	editor.remove("SinaWeibo_ID");
    	editor.remove("SinaWeibo_FollowerID");
        editor.remove("SinaWeibo_FollowerNickName");
        editor.remove("SinaWeibo_Token");
        editor.remove("SinaWeibo_ExpirationDate");
        editor.remove("SinaWeibo_RecentCount");
        editor.remove("SinaWeibo_Avatar");
        editor.remove("SinaWeibo_FollowerAvatar");
        editor.remove("SinaWeibo_FollowerAvatar2");
        editor.commit();
    }
    
    public static void removeRenrenPreference()
    {
    	SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
    	Editor editor = pref.edit();    	
        editor.remove("Renren_NickName");
    	editor.remove("Renren_ID");
    	editor.remove("Renren_FollowerID");
        editor.remove("Renren_FollowerNickName");
        editor.remove("Renren_Token");
        editor.remove("Renren_ExpirationDate");
        editor.remove("Renren_RecentCount");
        editor.remove("Renren_Avatar");
        editor.remove("Renren_FollowerAvatar");
        editor.remove("Renren_FollowerAvatar2");
        editor.commit();
    }
    
    public static void removeDoubanPreference()
    {
    	SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
    	Editor editor = pref.edit();    	
        editor.remove("Douban_NickName");
    	editor.remove("Douban_ID");
    	editor.remove("Douban_FollowerID");
        editor.remove("Douban_FollowerNickName");
        editor.remove("Douban_Token");
        editor.remove("Douban_ExpirationDate");
        editor.remove("Douban_RecentCount");
        editor.remove("Douban_Avatar");
        editor.remove("Douban_FollowerAvatar");
        editor.remove("Douban_FollowerAvatar2");
        editor.commit();
    }
    
    public static void removeRssPreference()
    {
    	SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
    	Editor editor = pref.edit();    	
        editor.remove("RSS_FollowerSiteTitle");
    	editor.remove("RSS_FollowerPath");
    	editor.remove("RSS_FollowerDescription");
        editor.commit();
    }
    /**
     *  
     * @param key
     * @return "" if not found
     */
    public static String getString(String key)
    {
    	SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
    	return pref.getString(key, "");
    }
    
    public static String getString(String key, String defStr)
    {
    	SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
    	return pref.getString(key, defStr);
    }
    
    
    /**
     * 
     * @param key
     * @return -1 if not found
     */
    public static Long getLong(String key)
    {
    	SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
    	return pref.getLong(key, -1);
    }
    
    public static Long getLong(String key, Long defLong)
    {
    	SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
    	return pref.getLong(key, defLong);
    }
}
