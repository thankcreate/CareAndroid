package com.thankcreate.care.tool.converter;

import org.json.JSONException;
import org.json.JSONObject;

import com.thankcreate.care.tool.StringTool;
import com.thankcreate.care.viewmodel.FriendViewModel;

public class SinaWeiboConverter {
	public static FriendViewModel convertFriendToCommon(JSONObject user) {
		FriendViewModel model = new FriendViewModel();
		try {			
			model.name = user.getString("screen_name");
			model.description = user.getString("description");
			model.avatar = user.getString("profile_image_url");
			model.avatar2 = user.getString("avatar_large");
			model.ID = user.getString("id");
			model.firstCharactor =StringTool.converterToFirstSpell(model.name.toLowerCase());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return model;
	}
}
