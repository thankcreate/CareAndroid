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
