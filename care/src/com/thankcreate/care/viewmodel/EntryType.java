package com.thankcreate.care.viewmodel;



public class EntryType {
	public static final int NotSet = 0;
	public static final int SinaWeibo = 1;
	public static final int Renren = 2;
	public static final int Douban = 3;	
	
	public static String StringValueOf(int type)
	{
		if(type == SinaWeibo)
		{
			return "来自新浪微博";
		}
		else if(type == Renren)
		{
			return "来自人人";
		}
		else if(type == Douban)
		{
			return "来自豆瓣";
		}
		else
		{
			return "来自火星";
		}
	}
}
