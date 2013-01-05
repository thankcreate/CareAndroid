package com.thankcreate.care.status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.dongxuexidu.douban4j.constants.DefaultConfigs;
import com.dongxuexidu.douban4j.utils.HttpManager;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.renren.api.connect.android.AsyncRenren;
import com.renren.api.connect.android.exception.RenrenError;
import com.thankcreate.care.App;
import com.thankcreate.care.AppConstants;
import com.thankcreate.care.BaseActivity;
import com.thankcreate.care.R;
import com.thankcreate.care.R.layout;
import com.thankcreate.care.R.menu;
import com.thankcreate.care.control.BackKeyLinearLayout;
import com.thankcreate.care.tool.converter.DoubanConverter;
import com.thankcreate.care.tool.converter.RenrenConverter;
import com.thankcreate.care.tool.misc.MiscTool;
import com.thankcreate.care.tool.misc.PreferenceHelper;
import com.thankcreate.care.tool.misc.StringTool;
import com.thankcreate.care.tool.ui.ToastHelper;
import com.thankcreate.care.viewmodel.CommentViewModel;
import com.thankcreate.care.viewmodel.EntryType;
import com.thankcreate.care.viewmodel.ItemViewModel;
import com.thankcreate.care.viewmodel.RenrenType;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.CommentsAPI;
import com.weibo.sdk.android.net.RequestListener;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class StatusAddCommentActivity extends BaseActivity {

	private ActionBar actionBar;
	private EditText textInput;	
	private TextView textCount;
	
	private ItemViewModel itemViewModel;
	private CommentViewModel commentViewModel;
	private BackKeyLinearLayout backKeyLinearLayout;
	
	private int maxCount = 140;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_status_add_comment);
		initActionBar();
		initControl();
		parseIntent();
		changeUIByType();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_status_add_comment, menu);
		return true;
	}

	private void initActionBar() {
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("评论");
		actionBar.addActionRight(new Action() {			
			@Override
			public void performAction(View view) {
				sendClick();
				
			}			

			@Override
			public int getDrawable() {
				return R.drawable.thumb_send;				
			}
		});
		addActionBarBackButton(actionBar);
	}
	
	private void initControl(){
		backKeyLinearLayout = (BackKeyLinearLayout) findViewById(R.id.comment_add_root);
		backKeyLinearLayout.setActivity(this);
		textInput = (EditText) findViewById(R.id.comment_add_input);
		textInput.addTextChangedListener(mOnTextChanged);		
		textCount =  (TextView) findViewById(R.id.comment_add_count_left);		
	}
	
	private void parseIntent()
	{
		Intent it=this.getIntent();
		itemViewModel = (ItemViewModel) it.getSerializableExtra("itemViewModel");
		commentViewModel = (CommentViewModel) it.getSerializableExtra("commentViewModel");
		if(itemViewModel == null)
			finish();
	}
	

	private void changeUIByType() {
		int type = itemViewModel.type;
		if(type == EntryType.SinaWeibo)
		{
			maxCount = 140;
		}
		// 人人在回复时最长也是140，只是发表新状态时可以到280
		else if (type == EntryType.Renren)
		{ 
			maxCount = 140;
		}
		else if (type == EntryType.Douban)
		{ 
			maxCount = 140;
		}
		textCount.setText(String.valueOf(maxCount));	
		InputFilter[] FilterArray = new InputFilter[1];
		FilterArray[0] = new InputFilter.LengthFilter(maxCount);
		textInput.setFilters(FilterArray);
		
        // m_commentViewModel不为空说明是对评论本身的评论
        // 各平台目前对评论的回复其实和对状态本身的回复没有本质区别
        // 都是调用一样的接口，但是前面加上了一对“对XX说”之类的文字		
		if (commentViewModel != null)
        {
			String preContentString = "";
            if (type == EntryType.SinaWeibo)
            {
            	preContentString =  String.format("回复@%s: ", commentViewModel.title);
            }
            else if (type == EntryType.Renren)
            {
            	preContentString =  String.format("回复%s: ", commentViewModel.title);            	
            }
            else if (type == EntryType.Douban)
            {
            	preContentString =  String.format("@%s: ", commentViewModel.doubanUID);            	
            }
            textInput.setText(preContentString);    
            textInput.setSelection(preContentString.length());
        }
	}
	
	private TextWatcher mOnTextChanged = new TextWatcher(){

		@Override
		public void afterTextChanged(Editable s) {
			int length = s.toString().length();
			textCount.setText(String.valueOf(maxCount - length));
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			
		}
	};
	
	private void sendClick() {
		String commentText = textInput.getText().toString();
		if(StringTool.isNullOrEmpty(commentText))
		{
			ToastHelper.show("只有智商超过250才能看见大人写的字么？",true);
			return;
		}
		if(commentText.length() > maxCount)
		{
			ToastHelper.show("内容超长勒喵~",true);
		}
		
		if(itemViewModel == null)
			return;
		
		int type = itemViewModel.type;
		if(type == EntryType.SinaWeibo)
		{
			sinaWeiboSend();
		}
		else if (type == EntryType.Renren)
		{ 
			renrenSend();
		}
		else if (type == EntryType.Douban)
		{
			doubanSend();
		}
	}

	private void sinaWeiboSend()
	{
		String commentText = textInput.getText().toString();
		Oauth2AccessToken oa = MiscTool.getOauth2AccessToken();
		CommentsAPI commentsAPI  = new CommentsAPI(oa);
		commentsAPI.create(commentText, Long.valueOf(itemViewModel.ID), false, mSinaWeiboSendCommentRequestListner);		
		actionBar.getProgressBar().post(new Runnable() {			
			@Override
			public void run() {
				actionBar.setProgressBarVisibility(View.VISIBLE);				
			}
		});
		
	}
	
	private RequestListener mSinaWeiboSendCommentRequestListner = new RequestListener(){

		@Override
		public void onComplete(String arg0) {
			ToastHelper.show("发送成功", true);	
			actionBar.getProgressBar().post(new Runnable() {			
				@Override
				public void run() {					
					actionBar.setProgressBarVisibility(View.GONE);				
				}
			});
			finish();
		}

		@Override
		public void onError(WeiboException arg0) {
			ToastHelper.show(">_< 由于未知原因，发送失败，请保持网络畅通", true);
			actionBar.getProgressBar().post(new Runnable() {			
				@Override
				public void run() {					
					actionBar.setProgressBarVisibility(View.GONE);				
				}
			});
		}

		@Override
		public void onIOException(IOException arg0) {	
			ToastHelper.show(">_< 由于未知原因，发送失败，请保持网络畅通", true);			
			actionBar.getProgressBar().post(new Runnable() {			
				@Override
				public void run() {					
					actionBar.setProgressBarVisibility(View.GONE);				
				}
			});
		}
	};
	
	private void renrenSend()
	{
		String commentText = textInput.getText().toString();
		
		AsyncRenren asyncRenren = new AsyncRenren(App.getRenren());
		Bundle bd = new Bundle();	
		int renrenType = itemViewModel.renrenFeedType;
		// 对普通状态的评论
		if(renrenType == RenrenType.TextStatus)
		{
			bd.putString("method", "status.addComment");
			bd.putString("status_id", itemViewModel.ID);
			bd.putString("owner_id", itemViewModel.ownerID);
			bd.putString("content", commentText);
		}
		// 对原创上传照片的评论
		else if(renrenType == RenrenType.UploadPhoto)
		{
			bd.putString("method", "photos.addComment");
			bd.putString("pid", itemViewModel.ID);
			bd.putString("uid", itemViewModel.ownerID);
			bd.putString("content", commentText);
		}
		// 对照片分享的评论
		else if(renrenType == RenrenType.SharePhoto)
		{
			bd.putString("method", "share.addComment");
			bd.putString("share_id", itemViewModel.ID);
			bd.putString("user_id", itemViewModel.ownerID);
			bd.putString("content", commentText);
		}
		asyncRenren.requestJSON(bd, mRenrenSendCommentRequestListener);
		
	}
	
	private com.renren.api.connect.android.RequestListener mRenrenSendCommentRequestListener = new com.renren.api.connect.android.RequestListener() {
				
		@Override
		public void onRenrenError(RenrenError renrenError) {
			ToastHelper.show(">_< 由于未知原因，发送失败，请保持网络畅通", true);
			actionBar.getProgressBar().post(new Runnable() {			
				@Override
				public void run() {					
					actionBar.setProgressBarVisibility(View.GONE);				
				}
			});
		}
		
		@Override
		public void onFault(Throwable fault) {
			ToastHelper.show(">_< 由于未知原因，发送失败，请保持网络畅通", true);
			actionBar.getProgressBar().post(new Runnable() {			
				@Override
				public void run() {					
					actionBar.setProgressBarVisibility(View.GONE);				
				}
			});
		}
		
		@Override
		public void onComplete(String response) {
			try {
				JSONObject result = new JSONObject(response);
				String flag = result.optString("result","0");
				if(flag.equals("1"))
				{
					ToastHelper.show("发送成功", true);	
					actionBar.getProgressBar().post(new Runnable() {			
						@Override
						public void run() {					
							actionBar.setProgressBarVisibility(View.GONE);				
						}
					});
					finish();
				}
				else {
					ToastHelper.show(">_< 由于未知原因，发送失败，请保持网络畅通", true);
					actionBar.getProgressBar().post(new Runnable() {			
						@Override
						public void run() {					
							actionBar.setProgressBarVisibility(View.GONE);				
						}
					});
				}
			} catch (Exception e) {
				ToastHelper.show(">_< 由于未知原因，发送失败，请保持网络畅通", true);
				actionBar.getProgressBar().post(new Runnable() {			
					@Override
					public void run() {					
						actionBar.setProgressBarVisibility(View.GONE);				
					}
				});
			}
			
		}
	};
	
	private void doubanSend()
	{
		final String commentText = textInput.getText().toString();
		final String token = PreferenceHelper.getString("Douban_Token");
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {					
					HttpManager httpManager = new HttpManager(token);
					String url = String.format("%s/shuo/v2/statuses/%s/comments", 
							DefaultConfigs.API_URL_PREFIX, itemViewModel.ID);
					Map<String, String> params = new HashMap<String, String>();					
					params.put("text", commentText);
					params.put("source", AppConstants.DOUBAN_SECRET_KEY);					
					String result = httpManager.postEncodedEntry(url, params, true);
					ToastHelper.show("发送成功", true);	
					actionBar.getProgressBar().post(new Runnable() {			
						@Override
						public void run() {					
							actionBar.setProgressBarVisibility(View.GONE);				
						}
					});
					finish();
					
				} catch (Exception e) {
					ToastHelper.show(">_< 由于未知原因，发送失败，请保持网络畅通", true);
					actionBar.getProgressBar().post(new Runnable() {			
						@Override
						public void run() {					
							actionBar.setProgressBarVisibility(View.GONE);				
						}
					});
				}				
			}
		}).start();
	}
}
