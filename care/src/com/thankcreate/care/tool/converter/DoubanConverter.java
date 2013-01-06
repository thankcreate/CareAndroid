package com.thankcreate.care.tool.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.text.Html;

import com.renren.api.connect.android.users.UserInfo;
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

public class DoubanConverter {
	
	public static String largeAvatar = "";
	
	public static CommentViewModel convertCommentToCommon(JSONObject comment)
	{
		if(comment == null)
			return null;
		CommentViewModel model = new CommentViewModel();
		try {
			JSONObject user = comment.optJSONObject("user");
			if(user == null)
				return null;
			
			model.title = user.optString("screen_name");
			model.iconURL = user.optString("small_avatar");
			model.uid = user.optString("id");
			model.doubanUID = user.optString("uid");
			
			model.content = comment.optString("text");
			model.id =  comment.optString("id");
			
			String rawTime = comment.optString("created_at");
			model.time = convertDoubanDateStringToDate(rawTime);
			model.type = EntryType.Douban;
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
			model.avatar = user.optString("small_avatar");
			model.avatar2 = user.optString("large_avatar");
			model.ID = user.optString("id");
			model.firstCharactor = StringTool.converterToFirstSpell(model.name.toLowerCase());
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
			String tp = status.optString("type");
			String title = status.optString("title");
			
			if (title.contains("关注") // 新关注了某个人
					|| title.contains("加入") // 加入小组
					|| title.contains("活动") // 对某活动感兴趣
					|| title.contains("歌曲") // 某2添加了某歌曲
					|| title.contains("试读") // 正在试读
					|| title.contains("豆瓣阅读") // 豆瓣阅读
					|| title.contains("使用") // 开始使用
					|| title.contains("日记")) // 写了日记
			{
				return null;
			}
			
			if(tp.equalsIgnoreCase("collect_book"))
			{
				return convertStatusBook(status, mainViewModel);
			}
			else if(tp.equalsIgnoreCase("collect_movie"))
			{
				return convertStatusMovie(status, mainViewModel);
			}
			else if(tp.equalsIgnoreCase("collect_music"))
			{
				return convertStatusMusic(status, mainViewModel);
			}
			// 豆瓣现在抽风，纯文字状态有时候type是null 真无语>_<
			// Note:豆瓣的所有的转发，外转的type都是text
			else if(tp.equalsIgnoreCase("text") || title.equalsIgnoreCase("说："))
			{
				return convertStatusText(status, mainViewModel);
			}
			return null;
		} catch (Exception e) {
			return null;
		}		
	}
	
	public static ItemViewModel convertStatusBook(JSONObject status, MainViewModel mainViewModel)
	{		
		if(status == null)
			return null;
		try {
			ItemViewModel model = new ItemViewModel();
			JSONObject user = status.optJSONObject("user");
			if(user == null)
				return null;
			model.iconURL = user .optString("small_avatar");			
			//if(StringTool.isNullOrEmpty(largeAvatar))
			{
				largeAvatar = PreferenceHelper.getString("Douban_FollowerAvatar2"); 
			}
			model.largeIconURL = largeAvatar;
			model.title = user.optString("screen_name");
			
			String bookTitle = "";
			JSONArray listAttach = status.optJSONArray("attachments");
			if(listAttach != null)
			{
				int length = listAttach.length();
				for (int i = 0; i < length; i++) {
					JSONObject attach = listAttach.getJSONObject(i);
					String type = attach.optString("type");
					if(type.equalsIgnoreCase("book"))
					{
						bookTitle =  attach.optString("title");
					}		
				}
			}
			String trimStatusTitle = trimMark(status.optString("title"));
			String statusText = status.optString("text");					
			model.content = trimStatusTitle + " “" + bookTitle + "” " + statusText;
			
			String rawTime = status.optString("created_at");
			model.time = convertDoubanDateStringToDate(rawTime);
			model.ID = status.optString("id");
			model.commentCount = status.optString("comments_count");
			model.sharedCount = status.optString("reshared_count");
			model.type = EntryType.Douban;
	        filtPicture(status, model, mainViewModel);
			return model;
		} catch (Exception e) {
			return null;
		}		
	}
	
	public static ItemViewModel convertStatusMovie(JSONObject status, MainViewModel mainViewModel)
	{		
		if(status == null)
			return null;
		try {
			ItemViewModel model = new ItemViewModel();
			JSONObject user = status.optJSONObject("user");
			if(user == null)
				return null;
			model.iconURL = user.optString("small_avatar");			
			//if(StringTool.isNullOrEmpty(largeAvatar))
			{
				largeAvatar = PreferenceHelper.getString("Douban_FollowerAvatar2"); 
			}
			model.largeIconURL = largeAvatar;
			model.title = user.optString("screen_name");
			
			String movieTitle = "";
			JSONArray listAttach = status.optJSONArray("attachments");
			if(listAttach != null)
			{
				int length = listAttach.length();
				for (int i = 0; i < length; i++) {
					JSONObject attach = listAttach.getJSONObject(i);
					String type = attach.optString("type");
					if(type.equalsIgnoreCase("movie"))
					{
						movieTitle =  attach.optString("title");
					}		
				}
			}
			String trimStatusTitle = trimMark(status.optString("title"));
			String statusText = status.optString("text");					
			model.content = trimStatusTitle + " “" + movieTitle + "” " + statusText;
			
			String rawTime = status.optString("created_at");
			model.time = convertDoubanDateStringToDate(rawTime);
			model.ID = status.optString("id");
			model.commentCount = status.optString("comments_count");
			model.sharedCount = status.optString("reshared_count");
			model.type = EntryType.Douban;
	        filtPicture(status, model, mainViewModel);
			return model;
		} catch (Exception e) {
			return null;
		}	
	}
	
	public static ItemViewModel convertStatusMusic(JSONObject status, MainViewModel mainViewModel)
	{		
		if(status == null)
			return null;
		try {
			ItemViewModel model = new ItemViewModel();
			JSONObject user = status.optJSONObject("user");
			if(user == null)
				return null;
			model.iconURL = user.optString("small_avatar");			
			//if(StringTool.isNullOrEmpty(largeAvatar))
			{
				largeAvatar = PreferenceHelper.getString("Douban_FollowerAvatar2"); 
			}
			model.largeIconURL = largeAvatar;
			model.title = user.optString("screen_name");
			
			String musicTitle = "";
			JSONArray listAttach = status.optJSONArray("attachments");
			if(listAttach != null)
			{
				int length = listAttach.length();
				for (int i = 0; i < length; i++) {
					JSONObject attach = listAttach.getJSONObject(i);
					String type = attach.optString("type");
					if(type.equalsIgnoreCase("music"))
					{
						musicTitle =  attach.optString("title");
					}		
				}
			}
			String trimStatusTitle = trimMark(status.optString("title"));
			String statusText = status.optString("text");					
			model.content = trimStatusTitle + " “" + musicTitle + "” " + statusText;
			
			String rawTime = status.optString("created_at");
			model.time = convertDoubanDateStringToDate(rawTime);
			model.ID = status.optString("id");
			model.commentCount = status.optString("comments_count");
			model.sharedCount = status.optString("reshared_count");
			model.type = EntryType.Douban;
	        filtPicture(status, model, mainViewModel);
			return model;
		} catch (Exception e) {
			return null;
		}		
	}
	
	public static ItemViewModel convertStatusText(JSONObject status, MainViewModel mainViewModel)
	{		
		if(status == null)
			return null;
		try {
			ItemViewModel model = new ItemViewModel();
			JSONObject user = status.optJSONObject("user");
			if(user == null)
				return null;
			model.iconURL = user.optString("small_avatar");			
			//if(StringTool.isNullOrEmpty(largeAvatar))
			{
				largeAvatar = PreferenceHelper.getString("Douban_FollowerAvatar2"); 
			}
			model.largeIconURL = largeAvatar;
			model.title = user.optString("screen_name");
			model.content = status.optString("text");	
			String rawTime = status.optString("created_at");
			model.time = convertDoubanDateStringToDate(rawTime);
			model.ID = status.optString("id");
			model.commentCount = status.optString("comments_count");
			model.sharedCount = status.optString("reshared_count");
			model.type = EntryType.Douban;
			
			JSONObject foward = status.optJSONObject("reshared_status");
			if(foward != null)
			{
				ItemViewModel fowardModel = convertStatusToCommon(foward, mainViewModel);
				if(fowardModel == null)				
					return null;
				// 如果是转播的话，把model的text改成“转播”两字，不然空在那里很奇怪
				model.content = "转播";
				model.forwardItem = fowardModel;
			}
			filtPicture(status, model, mainViewModel);
			return model;
		} catch (Exception e) {
			return null;
		}		
	}
	
	public static void filtPicture(JSONObject status, ItemViewModel model, MainViewModel mainViewModel) {
		if (status == null || model == null)
			return;
		try {
			JSONArray listAttach = status.optJSONArray("attachments");
			if(listAttach == null)
				return;
			for(int i = 0; i < listAttach.length(); i++)
			{
				JSONObject attach = listAttach.getJSONObject(i);
				String attachType = attach.optString("type");
				// 其实也没必要这样去一个个的比较类型，wp7版没有这一段,ios版有这一段
//				if(attachType.equalsIgnoreCase("movie")
//					|| attachType.equalsIgnoreCase("music")
//					|| attachType.equalsIgnoreCase("book")
//					|| attachType.equalsIgnoreCase("image"))
				{
					JSONArray listMedia = attach.optJSONArray("media");
					if(listMedia == null)
						return;
					for(int j = 0; j < listMedia.length(); j++)
					{
						JSONObject media = listMedia.getJSONObject(i);
						String mediaType = media.optString("type");
						if(mediaType.equalsIgnoreCase("image"))
						{
							model.imageURL = media.optString("src");
							model.midImageURL = generateDoubanSrc(model.imageURL, "median");
							model.fullImageURL = generateDoubanSrc(model.imageURL, "raw");
							
							PictureItemViewModel picItem = new PictureItemViewModel();
							picItem.smallURL = model.imageURL;
							picItem.middleURL = model.midImageURL;
							picItem.largeURL = model.fullImageURL;
							picItem.ID = model.ID;
							picItem.type = EntryType.Douban;
							picItem.title = model.title;
							picItem.description = model.content;
							picItem.time = model.time;
							mainViewModel.doubanPictureItems.add(picItem);
							break;
						}
					}
					break;
				}
			}
		} catch (Exception e) {
			return;
		}
	}
	
	// 豆瓣虽然只给了一个src,但是它的中图和大图是直接把链接中的small替换成median或raw就行了
	public static String generateDoubanSrc(String input, String dest)
	{
		if(StringTool.isNullOrEmpty(input) || StringTool.isNullOrEmpty(dest))
			return input;
		return input.replace("small", dest);
	}

	 // 因为读过的内容会在Title里写个如下打分字样 ，要把它去掉
    // 读过[score]0[/score]
    public static String trimMark(String input)
    {
    	if(input == null)
    		return "";
    	try {
    		for (int i = 0; i < input.length(); i++)
            {
                if (input.charAt(i) == '[')
                {
                    String result = input.substring(0, i);
                    return result;
                }
            }
            return input;
		} catch (Exception e) {
			return input;
		}
        
    }
    
	
	// 豆瓣的祼格式是这样的
	// 2012-12-07 20:37:36	
	public static Date convertDoubanDateStringToDate(String rawDate)
	{
		if(StringTool.isNullOrEmpty(rawDate))
			return new Date();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

		Date resultDate = null;
		try {
			resultDate = sdf.parse(rawDate);			
		} catch (ParseException e) {
			resultDate = new Date(); 
		}
		return resultDate;		
	}
}
