package com.thankcreate.care.tool.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.R.string;

import com.thankcreate.care.App;
import com.thankcreate.care.tool.misc.PreferenceHelper;
import com.thankcreate.care.tool.misc.StringTool;
import com.thankcreate.care.viewmodel.CommentViewModel;
import com.thankcreate.care.viewmodel.EntryType;
import com.thankcreate.care.viewmodel.FriendViewModel;
import com.thankcreate.care.viewmodel.ItemViewModel;
import com.thankcreate.care.viewmodel.MainViewModel;
import com.thankcreate.care.viewmodel.PictureItemViewModel;

public class SinaWeiboConverter {
	
	public static CommentViewModel convertCommentToCommon(JSONObject comment)
	{
		if(comment == null)
			return null;
		CommentViewModel model = new CommentViewModel();
		try {
			model.content = comment.optString("text");
			model.id = comment.optString("id");
			
			JSONObject user = comment.optJSONObject("user");
			if(user == null)
				return null;
			
			model.iconURL = user.optString("profile_image_url");
			model.uid = user.optString("id");
			model.title = user.optString("name");
			model.type = EntryType.SinaWeibo;
			
			String rawTime = comment.optString("created_at");
			model.time = convertSinaWeiboDateStringToDate(rawTime);
		} catch (Exception e) {
			return null;
		}
		return model;
	}
	
	public static FriendViewModel convertFriendToCommon(JSONObject user) {
		if(user == null)
			return null;
		FriendViewModel model = new FriendViewModel();
		try {			
			model.name = user.optString("screen_name");
			model.description = user.optString("description");
			model.avatar = user.optString("profile_image_url");
			model.avatar2 = user.optString("avatar_large");
			model.ID = user.optString("id");
			model.firstCharactor =StringTool.converterToFirstSpell(model.name.toLowerCase());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return model;
	}
	
	public static ItemViewModel convertStatusToCommon(JSONObject status, MainViewModel mainViewModel)
	{
		if(status == null)
			return null;
		
		ItemViewModel model = new ItemViewModel();
		try {
			convertPictureToCommon(status, mainViewModel);
			JSONObject user = status.optJSONObject("user");
			if(user == null)
				return null;
			
			model.iconURL = user.optString("profile_image_url");
			model.largeIconURL = user.optString("avatar_large");
			model.title = user.optString("name");
			model.content  = status.optString("text");
			model.imageURL  = status.optString("thumbnail_pic");
			model.midImageURL  = status.optString("bmiddle_pic");
			model.fullImageURL  = status.optString("original_pic");
			
			String rawTimeString = status.optString("created_at");
			model.time  = convertSinaWeiboDateStringToDate(rawTimeString);
			model.ID  = status.optString("id");
			model.type  = EntryType.SinaWeibo;
			model.sharedCount  = status.optString("reposts_count");
			model.commentCount  = status.optString("comments_count");
			
			
			JSONObject forward = status.optJSONObject("retweeted_status");
			if(forward != null)
			{
				model.forwardItem = new ItemViewModel();
				JSONObject forwardUser = forward.optJSONObject("user");
				if(forwardUser == null)
					return null;
				
				model.forwardItem.iconURL = forwardUser.optString("profile_image_url");
				model.forwardItem.largeIconURL = forwardUser.optString("avatar_large");
				model.forwardItem.title = forwardUser.optString("name");
				
				model.forwardItem.content  = forward.optString("text");
				model.forwardItem.imageURL  = forward.optString("thumbnail_pic");
				model.forwardItem.midImageURL  = forward.optString("bmiddle_pic");
				model.forwardItem.fullImageURL  = forward.optString("original_pic");
				
				String forwardRawTimeString = forward.optString("created_at");
				model.forwardItem.time  = convertSinaWeiboDateStringToDate(forwardRawTimeString);
				model.forwardItem.ID  = forward.optString("id");
				model.forwardItem.type  = EntryType.SinaWeibo;
				model.forwardItem.sharedCount  = forward.optString("reposts_count");
				model.forwardItem.commentCount  = forward.optString("comments_count");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return model;
	}
	
	public static void convertPictureToCommon(JSONObject status, MainViewModel mainViewModel)
	{
		if(status == null)
			return;
		PictureItemViewModel model = new PictureItemViewModel();		
		try {
			String useFowardPictureString = PreferenceHelper.getString("Global_NeedFetchImageInRetweet", "True");
			if(useFowardPictureString.equalsIgnoreCase("True"))
			{
				JSONObject forward = status.optJSONObject("retweeted_status");
				if(forward != null)
				{
					convertPictureToCommon(forward, mainViewModel);
				}
			}
			model.smallURL = status.optString("thumbnail_pic");
			model.middleURL = status.optString("bmiddle_pic");
			model.largeURL = status.optString("original_pic");
			model.ID = status.optString("id");
			model.type = EntryType.SinaWeibo;
			model.description = status.optString("text");
			String rawTimeString = status.optString("created_at");
			model.time  = convertSinaWeiboDateStringToDate(rawTimeString);
			
			JSONObject user = status.optJSONObject("user");
			if(user != null)
			{
				model.title = user.optString("name");
			}
			if(!StringTool.isNullOrEmpty(model.smallURL))
			{
				App.mainViewModel.sinaWeiboPictureItems.add(model);
			}
				
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	// 新浪的祼格式是这样的
	// Fri Oct 05 11:38:16 +0800 2012
	public static Date convertSinaWeiboDateStringToDate(String rawDate)
	{
		if(StringTool.isNullOrEmpty(rawDate))
			return new Date();
		
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy", Locale.ENGLISH);

		Date resultDate = null;
		try {
			resultDate = sdf.parse(rawDate);			
		} catch (ParseException e) {
			resultDate = new Date(); 
		} finally {
			return resultDate;
		}
	}
}
