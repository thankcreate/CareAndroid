package com.thankcreate.care.tool;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.thankcreate.care.AppConstants;
import com.thankcreate.care.account.AccountActivity;

public class PreferenceHelper {
	
    public static void removeSinaWeiboPreference(Context context)
    {
    	SharedPreferences pref = context.getSharedPreferences(
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
    
}
