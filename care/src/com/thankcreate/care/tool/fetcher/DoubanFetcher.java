package com.thankcreate.care.tool.fetcher;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.view.View;

import com.dongxuexidu.douban4j.constants.DefaultConfigs;
import com.dongxuexidu.douban4j.utils.HttpManager;
import com.thankcreate.care.App;
import com.thankcreate.care.tool.converter.DoubanConverter;
import com.thankcreate.care.tool.fetcher.BaseFetcher.CommentMan;
import com.thankcreate.care.tool.fetcher.BaseFetcher.FetchCompleteListener;
import com.thankcreate.care.tool.misc.MiscTool;
import com.thankcreate.care.tool.misc.PreferenceHelper;
import com.thankcreate.care.tool.misc.StringTool;
import com.thankcreate.care.tool.misc.TaskHelper;
import com.thankcreate.care.tool.misc.TaskHelper.OnTaskCompleteListener;
import com.thankcreate.care.tool.ui.ToastHelper;
import com.thankcreate.care.viewmodel.CommentViewModel;
import com.thankcreate.care.viewmodel.EntryType;
import com.thankcreate.care.viewmodel.ItemViewModel;

public class DoubanFetcher extends BaseFetcher implements OnTaskCompleteListener{
	private TaskHelper taskHelper;
	List<CommentMan> finalList;
	private String herID;
	private String myID;
	private FetchCompleteListener mFetchCompleteListener;
	private String token = PreferenceHelper.getString("Douban_Token");
	
	@Override
	public void fetch(final FetchCompleteListener listener) {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				if(listener == null)
					return;
				if(taskHelper == null)
					taskHelper = new TaskHelper(DoubanFetcher.this);
				
				finalList = new ArrayList<CommentMan>();
				finalList.clear();
				mFetchCompleteListener = listener;

				herID = MiscTool.getHerID(EntryType.Douban);
				myID = MiscTool.getMyID(EntryType.Douban);
				
				if(StringTool.isNullOrEmpty(herID) || StringTool.isNullOrEmpty(myID))
				{
					listener.fetchComplete(finalList);
					return;
				}
				
				
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							HttpManager httpManager = new HttpManager(token);
							String url = String.format("%s/shuo/v2/statuses/user_timeline/%s", 
									DefaultConfigs.API_URL_PREFIX, herID);
							List<NameValuePair> params = new ArrayList<NameValuePair>();
							params.add(new BasicNameValuePair("count", "30"));
							String result = httpManager.getResponseString(url, params, true);
							
							JSONArray statuses = new JSONArray(result);
							if(statuses != null)
							{
								for(int i = 0; i < statuses.length(); i++)
								{
									JSONObject ob = statuses.getJSONObject(i);
									taskHelper.pushTask();
									handleStatus(ob);
								}
							}							
						} catch (Exception e) {
							listener.fetchComplete(finalList);
							return;
						}				
					}
				}).start();
				
			}
		});
		thread.run();
	}
	
	private void handleStatus(final JSONObject ob) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {					
					HttpManager httpManager = new HttpManager(token);
					String id = ob.optString("id");					
					String url = String.format("%s/shuo/v2/statuses/%s/comments", 
							DefaultConfigs.API_URL_PREFIX, id);									
					String result = httpManager.getResponseString(url, null, true);					
					JSONArray comments = new JSONArray(result);
					if(comments != null)
					{
						for(int i = 0; i < comments.length(); i++)
						{
							JSONObject ob = comments.getJSONObject(i);
							CommentViewModel model = DoubanConverter.convertCommentToCommon(ob);
							if(model != null)
							{
								String userID = model.uid;
								if(!userID.equals(myID) && !userID.equals(herID))
								{
									CommentMan man = new CommentMan();
									man.id = userID;
									man.name = model.title;
									finalList.add(man);
								}
							}							
						}
					}
					taskHelper.popTask();
				} catch (Exception e) {
					taskHelper.popTask();
				}				
			}
		}).start();
	}

	@Override
	public void onAllTaskComplete() {
		if(mFetchCompleteListener != null)
		{
			mFetchCompleteListener.fetchComplete(finalList);
		}
	}
}
