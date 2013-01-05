package com.thankcreate.care.tool.converter;

import org.json.JSONObject;
import org.mcsoxford.rss.RSSItem;

import android.text.Html;

import com.thankcreate.care.tool.misc.StringTool;
import com.thankcreate.care.viewmodel.EntryType;
import com.thankcreate.care.viewmodel.ItemViewModel;

public class RssConverter {
	public static ItemViewModel convertStatusToCommon(RSSItem item)
	{
		if(item == null)
			return null;
		try {
			ItemViewModel model = new ItemViewModel();
			model.title = item.getTitle();
			
			String possibleContent = "";
			if(!StringTool.isNullOrEmpty(item.getContent()))
			{
				possibleContent = item.getContent();
			}
			else if(!StringTool.isNullOrEmpty(item.getDescription()))
			{
				possibleContent = item.getDescription();
			}
			model.content = getFirst50(possibleContent);
			model.rssSummary = possibleContent;
			model.originalURL = item.getLink().toString();
			model.type = EntryType.Rss;
			model.time = item.getPubDate();			
			return model;
		} catch (Exception e) {
			return null;
		}
	}
	
	public static String getFirst50(String input)
	{	
		if(StringTool.isNullOrEmpty(input))
			return "";
		String noTagContent = Html.fromHtml(input).toString();
		if(noTagContent.length() < 85)
			return noTagContent;
		
		String cut = noTagContent.substring(0, 85) + "...";
		return cut;
		
	}
}
