package com.thankcreate.care.viewmodel;

import java.io.Serializable;
import java.util.Date;

import com.thankcreate.care.tool.misc.StringTool;

public class ItemViewModel implements Serializable{
		
	public String iconURL;
	public String largeIconURL;
	
	public String imageURL;
	public String midImageURL;
	public String fullImageURL;	
	
	public Date time;
	public int type;	
	public int renrenFeedType;
	
	public String content;
	public String rssSummary;
	public String title;
	public String originalURL;
	public String description;
	public String ownerID;
	public String ID;	
	public String commentCount;
	public String sharedCount;
	
	public ItemViewModel forwardItem;
		
	public String getContentWithTitle()
	{
		String prefix = (title == null)? "" : title;
		String inner =  (content == null)? "" : content;
		return prefix + ": " + inner;
	}
	
	public String getCommentCount()
	{
		String result = StringTool.isNullOrEmpty(commentCount)? "0" : commentCount;
		return result;
	}
}
