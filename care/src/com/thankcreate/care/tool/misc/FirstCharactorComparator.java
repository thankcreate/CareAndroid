package com.thankcreate.care.tool.misc;


import java.text.Collator;
import java.util.Comparator;



import com.thankcreate.care.viewmodel.FriendViewModel;

public class FirstCharactorComparator implements Comparator<FriendViewModel> {
	
	@Override
	public int compare(FriendViewModel lhs, FriendViewModel rhs) {
		String key1= lhs.firstCharactor;
		String key2= rhs.firstCharactor;
		return key1.compareTo(key2);
	}
}
