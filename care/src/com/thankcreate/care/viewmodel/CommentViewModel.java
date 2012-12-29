package com.thankcreate.care.viewmodel;

import java.io.Serializable;
import java.util.Date;

public class CommentViewModel implements Serializable{
	public String iconURL;
	public String content;
	public String title;
	public String id;
	
	// 豆瓣用户两个ID标识，一个数字ID，一个字符串的ID
	public String uid;
	public String doubanUID;
	public int type;
	
	public Date time;
	
}
