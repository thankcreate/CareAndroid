package com.thankcreate.care;

public class AppConstants {

	public static final String PREFERENCES_NAME = "com_thankcreate_care";
	public static final String CACHE_ITEM = "cacheItem";
	
	// SinaWeibo
	public static final String SINAWEIBO_APP_KEY = "892878802";
	public static final String SINAWEIBO_REDIRECT_URL = "http://api.weibo.com/oauth2/default.html";
	
	// Renren
//	public static final String RENREN_APP_ID = "223764";
//	public static final String RENREN_APP_KEY = "c2ec3bd34d834eb48e7d0789c14e0cb5";
//	public static final String RENREN_SECRET_KEY = "a45988af42454c39807f85664a808cc4";
	
	// 这是人人ios版的key其实
	public static final String RENREN_APP_ID = "220565";
	public static final String RENREN_APP_KEY = "fd80adfb79554ad8a8fb46d5a3fde69c";
	public static final String RENREN_SECRET_KEY = "2d853703fc854cdda912f36f3ff69a01";
	public static final String[] RENREN_PERMISSION = {
        "publish_feed",
        "publish_blog", 
        "publish_share",
        "read_user_album", 
        "read_user_status",
        "read_user_photo",
        "read_user_comment",
        "read_user_status",
        "publish_comment",
        "read_user_share",                
        "create_album", 
        "photo_upload" }; 
	
	// Douban	
	public static final String DOUBAN_APP_KEY = "0df3961af03d91e829dac250d0b8b5b8";
	public static final String DOUBAN_SECRET_KEY = "947c7204b935bf14";
	public static final String DOUBAN_REDIRECT_URL = "http://thankcreate.github.com/Care/callback.html";
	public static final String DOUBAN_PERMISSION = "shuo_basic_r,shuo_basic_w,douban_basic_common";

}
