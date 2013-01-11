package com.thankcreate.care.tool.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import android.text.Html;

import com.renren.api.connect.android.friends.FriendsGetFriendsResponseBean.Friend;
import com.thankcreate.care.App;
import com.thankcreate.care.tool.misc.PreferenceHelper;
import com.thankcreate.care.tool.misc.StringTool;
import com.thankcreate.care.viewmodel.CommentViewModel;
import com.thankcreate.care.viewmodel.EntryType;
import com.thankcreate.care.viewmodel.FriendViewModel;
import com.thankcreate.care.viewmodel.ItemViewModel;
import com.thankcreate.care.viewmodel.MainViewModel;
import com.thankcreate.care.viewmodel.PictureItemViewModel;
import com.thankcreate.care.viewmodel.RenrenType;

public class RenrenConverter {
	
    public static String FeedTypeTextStatus = "10";
    public static String FeedTypeUploadPhoto = "30";
    public static String FeedTypeSharePhoto = "32";
    
    
    public static CommentViewModel convertCommentToCommon(JSONObject comment, int renrenType)
	{
		if(comment == null)
			return null;
		CommentViewModel model = new CommentViewModel();
		try {
			if(renrenType == RenrenType.TextStatus)
			{
				model.title = comment.optString("name");
				model.iconURL = comment.optString("tinyurl");
				model.content = comment.optString("text");
				model.id = comment.optString("comment_id");
				String rawTime = comment.optString("time");
				model.time = convertRenrenDateStringToDate(rawTime);
			}
			else if(renrenType == RenrenType.UploadPhoto)
			{
				model.title = comment.optString("name");
				model.iconURL = comment.optString("headurl");
				model.content = comment.optString("text");
				model.id = comment.optString("comment_id");
				String rawTime = comment.optString("time");
				model.time = convertRenrenDateStringToDate(rawTime);
			}
			else if(renrenType == RenrenType.SharePhoto)
			{
				model.title = comment.optString("name");
				model.iconURL = comment.optString("headurl");
				model.content = comment.optString("content");
				model.id = comment.optString("id");
				String rawTime = comment.optString("time");
				model.time = convertRenrenDateStringToDate(rawTime);
			}			
		} catch (Exception e) {
			return null;
		}
		return model;
	}
    
	public static FriendViewModel convertFriendToCommon(Friend friend) {
		if(friend == null)
			return null;
		FriendViewModel model = new FriendViewModel();
		try {			
			model.name = friend.getName();
			model.description = "";
			model.avatar = friend.getHeadurl();
			model.avatar2 = friend.getHeadurl();
			model.ID = String.valueOf(friend.getUid());
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
		try {
			String tp = status.optString("feed_type");
			if(tp.equalsIgnoreCase(FeedTypeTextStatus))
			{
				return convertTextStatus(status, mainViewModel);
			}
			else if(tp.equalsIgnoreCase(FeedTypeUploadPhoto))
			{
				return convertUploadPhoto(status, mainViewModel);
			}
			else if(tp.equalsIgnoreCase(FeedTypeSharePhoto))
			{
				return convertSharePhoto(status, mainViewModel);
			}
			return null;
		} catch (Exception e) {
			return null;
		}		
	}
	
	public static ItemViewModel convertTextStatus(JSONObject status, MainViewModel mainViewModel)
	{		
		if(status == null)
			return null;
		try {
			ItemViewModel model = new ItemViewModel();
			model.iconURL = status.optString("headurl");
			model.largeIconURL = PreferenceHelper.getString("Renren_FollowerAvatar2", model.iconURL);
			
			model.title = status.optString("name");

			String rawPrefix = status.optString("prefix");
			model.content = Html.fromHtml(rawPrefix).toString();  
	        
	        String plainTime = status.optString("update_time");
	        model.time = convertRenrenDateStringToDate(plainTime);
	        
	        model.type = EntryType.Renren;
	        model.ID = status.optString("source_id");
	        model.ownerID = status.optString("actor_id");
	        model.renrenFeedType = RenrenType.TextStatus;
	        
	        JSONObject comments = status.optJSONObject("comments");
	        if(comments != null)
	        {
	        	model.commentCount = comments.optString("count");
	        }
	        model.sharedCount = "0";
	        
	        JSONArray attachments = status.optJSONArray("attachment");
	        if(attachments != null)
	        {
	        	for(int i = 0; i < attachments.length(); i++)
	        	{
	        		JSONObject attach = attachments.getJSONObject(i);
	        		String attachType = attach.optString("media_type");
	        		if(attachType.equalsIgnoreCase("status"))
	        		{
	        			model.forwardItem = new ItemViewModel();
	        			model.forwardItem.title = attach.optString("owner_name");
	        			model.forwardItem.content =  Html.fromHtml(attach.optString("content")).toString();
	        		}
	        	}
	        }
	        
			return model;
		} catch (Exception e) {
			return null;
		}		
	}
	
	
	public static ItemViewModel convertUploadPhoto(JSONObject status, MainViewModel mainViewModel)
	{
		if(status == null)
			return null;
		try {
			ItemViewModel model = new ItemViewModel();
			model.iconURL = status.optString("headurl");
			model.largeIconURL = PreferenceHelper.getString("Renren_FollowerAvatar2", model.iconURL);

			model.title = status.optString("name");
			String plainTime = status.optString("update_time");
			model.time = convertRenrenDateStringToDate(plainTime);

			model.type = EntryType.Renren;
			model.ownerID = status.optString("actor_id");
			model.renrenFeedType = RenrenType.UploadPhoto;
			JSONObject comments = status.optJSONObject("comments");
			if (comments != null) {
				model.commentCount = comments.optString("count");
			}
			model.sharedCount = "0";
			
			JSONArray attachments = status.optJSONArray("attachment");
	        if(attachments != null)
	        {
	        	for(int i = 0; i < attachments.length(); i++)
	        	{
	        		JSONObject attach = attachments.getJSONObject(i);
	        		String attachType = attach.optString("media_type");
	        		if(attachType.equalsIgnoreCase("photo"))
	        		{
	        			model.content =  Html.fromHtml(attach.optString("content")).toString();
	        			model.imageURL = attach.optString("src");
	        			model.midImageURL = attach.optString("src");
	        			model.fullImageURL = attach.optString("raw_src");	        			
	        			model.ID = attach.optString("media_id");
	        			
	        			 PictureItemViewModel pic =  new PictureItemViewModel();
	                     pic.smallURL = model.imageURL;
	                     pic.middleURL = model.midImageURL;
	                     pic.largeURL = model.fullImageURL;
	                     pic.ID = model.ID;
	                     pic.title = model.title;
	                     pic.description = model.content;
	                     pic.time = model.time;
	                     pic.type = EntryType.Renren;
	                     mainViewModel.renrenPictureItems.add(pic);
	        		}
	        	}
	        }
			return model;			
		} catch (Exception e) {
			return null;
		}		
	}
	
	public static ItemViewModel convertSharePhoto(JSONObject status, MainViewModel mainViewModel)
	{
		if(status == null)
			return null;
		try {			
			ItemViewModel model = new ItemViewModel();
			model.iconURL = status.optString("headurl");			
			model.largeIconURL = PreferenceHelper.getString("Renren_FollowerAvatar2", model.iconURL);

			model.title = status.optString("name");
			model.content =  Html.fromHtml(status.optString("message")).toString();
			String plainTime = status.optString("update_time");
			model.time = convertRenrenDateStringToDate(plainTime);

			model.type = EntryType.Renren;
			model.ID = status.optString("source_id");
			model.ownerID = status.optString("actor_id");
			model.renrenFeedType = RenrenType.SharePhoto;
			JSONObject comments = status.optJSONObject("comments");
			if (comments != null) {
				model.commentCount = comments.optString("count");
			}
			model.sharedCount = "0";
			
			JSONArray attachments = status.optJSONArray("attachment");
	        if(attachments != null)
	        {
	        	for(int i = 0; i < attachments.length(); i++)
	        	{
	        		JSONObject attach = attachments.getJSONObject(i);
	        		String attachType = attach.optString("media_type");
	        		if(attachType.equalsIgnoreCase("photo"))
	        		{
	        			model.forwardItem = new ItemViewModel();
	        			model.forwardItem.title = attach.optString("owner_name");
	        			model.forwardItem.content =  Html.fromHtml(status.optString("description")).toString();
	        				        			
	        			model.forwardItem.imageURL = attach.optString("src");
	        			model.forwardItem.midImageURL = attach.optString("src");
	        			model.forwardItem.fullImageURL = attach.optString("raw_src");  
	        			
	        			String useFowardPictureString = PreferenceHelper.getString("Global_NeedFetchImageInRetweet", "True");
	        			if(useFowardPictureString.equalsIgnoreCase("True"))
	        			{
	        				PictureItemViewModel pic =  new PictureItemViewModel();
		                     pic.smallURL = model.forwardItem.imageURL;
		                     pic.middleURL = model.forwardItem.midImageURL;
		                     pic.largeURL = model.forwardItem.fullImageURL;
		                     pic.ID = model.ID;
		                     pic.title = model.title;
		                     pic.description = model.content;
		                     pic.time = model.time;
		                     pic.type = EntryType.Renren;
		                     mainViewModel.renrenPictureItems.add(pic);
	        			}
	        		}
	        	}
	        }
			return model;
		} catch (Exception e) {
			return null;
		}		
	}
	
	// 人人的祼格式是这样的
	// 2012-12-07 20:37:36	
	// SimpleDateFormat必须做缓存，否则你会死得很惨
	// 这玩意儿不做缓存的话几乎在虚拟机上跑不动
	public static SimpleDateFormat sdf;
	public static Date convertRenrenDateStringToDate(String rawDate)
	{
		if(StringTool.isNullOrEmpty(rawDate))
			return new Date();
		
		if(sdf == null)
			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

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
