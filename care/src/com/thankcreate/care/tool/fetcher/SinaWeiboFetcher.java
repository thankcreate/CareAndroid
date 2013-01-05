package com.thankcreate.care.tool.fetcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.thankcreate.care.App;
import com.thankcreate.care.tool.converter.SinaWeiboConverter;
import com.thankcreate.care.tool.misc.*;
import com.thankcreate.care.tool.misc.TaskHelper.OnTaskCompleteListener;
import com.thankcreate.care.tool.ui.ToastHelper;
import com.thankcreate.care.viewmodel.CommentViewModel;
import com.thankcreate.care.viewmodel.EntryType;
import com.thankcreate.care.viewmodel.ItemViewModel;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.CommentsAPI;
import com.weibo.sdk.android.api.WeiboAPI.AUTHOR_FILTER;
import com.weibo.sdk.android.net.RequestListener;

/**
 *  注意，当前这个类里有循环引用，注意去掉
 * @author ThankCreate
 *
 */
public class SinaWeiboFetcher extends BaseFetcher implements OnTaskCompleteListener {

	private TaskHelper taskHelper;
	List<CommentMan> finalList;
	private String herID;
	private String myID;
	private FetchCompleteListener mFetchCompleteListener;
	
	@Override
	public void fetch(final FetchCompleteListener listener) {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				if(listener == null)
					return;
				if(taskHelper == null)
					taskHelper = new TaskHelper(SinaWeiboFetcher.this);
				
				finalList = new ArrayList<CommentMan>();
				finalList.clear();
				mFetchCompleteListener = listener;
				if(App.mainViewModel.sinaWeiboItems == null || App.mainViewModel.sinaWeiboItems.size() == 0)
				{
					listener.fetchComplete(finalList);
					return;
				}
				 
				herID = MiscTool.getHerID(EntryType.SinaWeibo);
				myID = MiscTool.getMyID(EntryType.SinaWeibo);
				
				if(StringTool.isNullOrEmpty(herID) || StringTool.isNullOrEmpty(myID))
				{
					listener.fetchComplete(finalList);
					return;
				}
				
				int length = App.mainViewModel.sinaWeiboItems.size();
				if(length > MAX_FETCHE_COUNT)
					length = MAX_FETCHE_COUNT;
				for (int i = 0; i < length; i++) {
					ItemViewModel item = App.mainViewModel.sinaWeiboItems.get(i);
					taskHelper.pushTask();
					fetchSingleStatusComment(item.ID);
				}
			}
		});
		thread.run();
	}

	private void fetchSingleStatusComment(String id)
	{
		if(!MiscTool.isSinaWeiboLogin())
		{
			taskHelper.popTask();
			return;
		}
		
		try {
			Oauth2AccessToken oa = MiscTool.getOauth2AccessToken();
			CommentsAPI commentsAPI = new CommentsAPI(oa);
			commentsAPI.show(Long.parseLong(id), 0, 0, 50, 1, AUTHOR_FILTER.ALL , mCommentRequestListener);
		} catch (Exception e) {
			taskHelper.popTask();
			return;
		}
	}
	
	private RequestListener mCommentRequestListener = new RequestListener() {
		

		@Override
		public void onComplete(String arg0) {
			try
			{
				JSONObject root = new JSONObject(arg0);
				JSONArray listComments = root.optJSONArray("comments");
				if(listComments == null || listComments.length() == 0)
					return;
				for(int i = 0; i < listComments.length(); i++)
				{
					JSONObject status = listComments.getJSONObject(i);
					CommentViewModel model = SinaWeiboConverter.convertCommentToCommon(status);
					if(model != null)
					{
						// 要去掉她自己啊！！！！你个2货
						if(model.uid.compareTo(myID) != 0 && model.uid.compareTo(herID) != 0 )
						{
							CommentMan man = new CommentMan();
							man.id = model.uid;
							man.name = model.title;
							finalList.add(man);
						}
					}
				}
			} 
			catch (Exception e) 
			{	
				e.printStackTrace();
			}
			finally
			{
				taskHelper.popTask();
			}
		}
		
		@Override
		public void onIOException(IOException arg0) {
			taskHelper.popTask();
			
		}
		
		@Override
		public void onError(WeiboException arg0) {
			taskHelper.popTask();
		}
	};
	
	@Override
	public void onAllTaskComplete() {
		if(mFetchCompleteListener != null)
		{
			mFetchCompleteListener.fetchComplete(finalList);
		}
	}
	
	
}
