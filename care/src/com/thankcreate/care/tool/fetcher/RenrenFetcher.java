package com.thankcreate.care.tool.fetcher;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;

import com.renren.api.connect.android.AsyncRenren;
import com.renren.api.connect.android.exception.RenrenError;
import com.thankcreate.care.App;
import com.thankcreate.care.tool.converter.RenrenConverter;
import com.thankcreate.care.tool.fetcher.BaseFetcher.CommentMan;
import com.thankcreate.care.tool.fetcher.BaseFetcher.FetchCompleteListener;
import com.thankcreate.care.tool.misc.MiscTool;
import com.thankcreate.care.tool.misc.StringTool;
import com.thankcreate.care.tool.misc.TaskHelper;
import com.thankcreate.care.tool.ui.ToastHelper;
import com.thankcreate.care.viewmodel.EntryType;
import com.thankcreate.care.viewmodel.ItemViewModel;

public class RenrenFetcher extends BaseFetcher {

	List<CommentMan> finalList;
	private String herID;
	private String myID;
	private FetchCompleteListener mFetchCompleteListener;
	
	@Override
	public void fetch(FetchCompleteListener listener) {
		if(listener == null)
			return;
		
		finalList = new ArrayList<CommentMan>();
		finalList.clear();
		mFetchCompleteListener = listener;
		
		herID = MiscTool.getHerID(EntryType.Renren);
		myID = MiscTool.getMyID(EntryType.Renren);
		
		if(StringTool.isNullOrEmpty(herID) || StringTool.isNullOrEmpty(myID))
		{
			listener.fetchComplete(finalList);
			return;
		}
		
		AsyncRenren asyncRenren = new AsyncRenren(App.getRenren());
		Bundle bd = new Bundle();	
		bd.putString("method", "feed.get");
		bd.putString("type", "10,30,32");
		bd.putString("uid", herID);
		bd.putString("count", "50");
		asyncRenren.requestJSON(bd, mRenrenUserTimeLineListener);
	}
	
	private com.renren.api.connect.android.RequestListener mRenrenUserTimeLineListener = new com.renren.api.connect.android.RequestListener() {
		
		@Override
		public void onRenrenError(RenrenError renrenError) {
			if(mFetchCompleteListener != null)
			{
				mFetchCompleteListener.fetchComplete(finalList);
			}
		}
		
		@Override
		public void onFault(Throwable fault) {	
			if(mFetchCompleteListener != null)
			{
				mFetchCompleteListener.fetchComplete(finalList);
			}
		}
		
		@Override
		public void onComplete(String response) {
			try {				
				JSONArray statuses = new JSONArray(response);
				if(statuses != null)
				{
					for(int i = 0; i < statuses.length(); i++)
					{
						JSONObject ob = statuses.getJSONObject(i);
						handleStatus(ob);
					}
				}
				if(mFetchCompleteListener != null)
				{
					mFetchCompleteListener.fetchComplete(finalList);
				}
			} catch (Exception e) {
				if(mFetchCompleteListener != null)
				{
					mFetchCompleteListener.fetchComplete(finalList);
				}
			}
		}
	};
	
	private void handleStatus(JSONObject status)
	{
		if(status == null)
			return;
		JSONObject commentsWrapper = status.optJSONObject("comments");
		if(commentsWrapper == null)
			return;
		JSONArray commentArray = commentsWrapper.optJSONArray("comment");
		if(commentArray == null || commentArray.length() == 0)
			return;
		for(int i = 0; i < commentArray.length(); ++i)
		{
			JSONObject comment = commentArray.optJSONObject(i);
			if(comment == null)
				return;
			String id = comment.optString("uid");
			String name = comment.optString("name");
			if(!id.equals(myID) && !id.equals(herID))
			{
				CommentMan man = new CommentMan();
				man.id = id;
				man.name = name;
				finalList.add(man);
			}
		}
	}

}
