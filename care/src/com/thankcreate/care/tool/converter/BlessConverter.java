package com.thankcreate.care.tool.converter;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.thankcreate.care.tool.misc.StringTool;
import com.thankcreate.care.viewmodel.BlessItemViewModel;

public class BlessConverter {
	public static BlessItemViewModel convertToViewModel(JSONObject ob)
	{
		if(ob == null)
			return null;
		BlessItemViewModel model = new BlessItemViewModel();
		try {
			model.title = ob.optString("name");
			if(StringTool.isNullOrEmpty(model.title))
				model.title = "匿名";
			
			model.content = ob.optString("content");
			
			String rawTimeStr = ob.optString("time");
			// Unix-like的timestamp和java的timestamp差了个1000倍数
			Long rawTimeLong = Long.parseLong(rawTimeStr) * 1000;
			model.time = new Date(rawTimeLong);
			
		} catch (Exception e) {
			model = null;
		}
		return model;
	}
}
