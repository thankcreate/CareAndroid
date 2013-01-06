package com.thankcreate.care.viewmodel;

import java.io.Serializable;
import java.util.Date;


public class ChatItemViewModel implements Serializable{
	
	public static final int TYPE_HER = 1;
	public static final int TYPE_ME = 2;
	
	public String title;
	public String content;
	public String iconURL;
	public Date time;	
	public int type;
}
