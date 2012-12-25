package com.thankcreate.care.viewmodel;

public class FriendViewModel {
	public String name;
	public String ID;
	public String description;	
	public String avatar;
	public String avatar2;
	
	// 首字母要提前在converter里算出来，在这里存住，以免重复计算
	public String firstCharactor;
}
