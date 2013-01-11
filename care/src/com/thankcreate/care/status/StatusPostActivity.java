package com.thankcreate.care.status;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.dongxuexidu.douban4j.constants.DefaultConfigs;
import com.dongxuexidu.douban4j.utils.HttpManager;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.renren.api.connect.android.AsyncRenren;
import com.renren.api.connect.android.Util;
import com.renren.api.connect.android.common.AbstractRequestListener;
import com.renren.api.connect.android.exception.RenrenError;
import com.renren.api.connect.android.photos.PhotoUploadRequestParam;
import com.renren.api.connect.android.photos.PhotoUploadResponseBean;
import com.renren.api.connect.android.status.StatusSetRequestParam;
import com.renren.api.connect.android.status.StatusSetResponseBean;
import com.thankcreate.care.App;
import com.thankcreate.care.AppConstants;
import com.thankcreate.care.BaseActivity;
import com.thankcreate.care.R;
import com.thankcreate.care.R.layout;
import com.thankcreate.care.R.menu;
import com.thankcreate.care.control.BackKeyLinearLayout;
import com.thankcreate.care.tool.converter.DoubanConverter;
import com.thankcreate.care.tool.misc.MiscTool;
import com.thankcreate.care.tool.misc.PreferenceHelper;
import com.thankcreate.care.tool.misc.StringTool;
import com.thankcreate.care.tool.misc.UploadTool;
import com.thankcreate.care.tool.ui.ToastHelper;
import com.thankcreate.care.viewmodel.CommentViewModel;
import com.thankcreate.care.viewmodel.EntryType;
import com.thankcreate.care.viewmodel.ItemViewModel;
import com.umeng.analytics.MobclickAgent;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.CommentsAPI;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.net.RequestListener;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class StatusPostActivity extends BaseActivity {

	private ActionBar actionBar;
	private EditText textInput;	
	private TextView textCount;
	private ImageView imageViewThumb;
			
	private BackKeyLinearLayout backKeyLinearLayout;
	
	private int maxCount = 140;
	private int type = EntryType.NotSet;
	private String preContent;
	private String imageURL;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_status_post);
		initActionBar();
		initControl();
		parseIntent();
		changeUIByType();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		return false;
	}

	private void initActionBar() {
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("发布");
		actionBar.addActionRight(new Action() {			
			@Override
			public void performAction(View view) {
				sendClick();
				MobclickAgent.onEvent(StatusPostActivity.this, "PostNew");
			}			

			@Override
			public int getDrawable() {
				return R.drawable.thumb_send;				
			}
		});
		addActionBarBackButton(actionBar);
	}
	
	private void initControl(){
		backKeyLinearLayout = (BackKeyLinearLayout) findViewById(R.id.post_root);
		backKeyLinearLayout.setActivity(this);
		textInput = (EditText) findViewById(R.id.status_post_input);
		textInput.addTextChangedListener(mOnTextChanged);		
		textCount =  (TextView) findViewById(R.id.status_post_count_left);
		imageViewThumb = (ImageView) findViewById(R.id.status_post_thumb_image_view);
	}
	
	private void parseIntent()
	{
		Intent it= this.getIntent();
		type = it.getIntExtra("type", EntryType.NotSet);		
		preContent = it.getStringExtra("preContent");
		if(preContent == null)
			preContent = "";
		imageURL = it.getStringExtra("imageURL");
		
		if(type == EntryType.NotSet)
		{
			finish();			
		}
	}
	

	private void changeUIByType() {		
		if(type == EntryType.SinaWeibo)
		{
			maxCount = 140;
		}
		// 人人在回复时最长也是140，只是发表新状态时可以到280
		else if (type == EntryType.Renren)
		{ 
			maxCount = 280;
		}
		else if (type == EntryType.Douban)
		{ 
			maxCount = 140;
		}
		else
		{
			finish();
			return;
		}
		textCount.setText(String.valueOf(maxCount));	
		InputFilter[] FilterArray = new InputFilter[1];
		FilterArray[0] = new InputFilter.LengthFilter(maxCount);
		textInput.setFilters(FilterArray);
		

        textInput.setText(preContent);    
        textInput.setSelection(preContent.length());
        
        if(StringTool.isNullOrEmpty(imageURL))
        {
        	imageViewThumb.setVisibility(View.GONE);
        	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        	
        }
        else
        {
        	imageViewThumb.setVisibility(View.VISIBLE);
        	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        	try {
        		File imgFile = new  File(imageURL);
        		if(imgFile.exists()){
            	    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            	    imageViewThumb.setImageBitmap(myBitmap);
            	}
			} catch (Exception e) {
				imageViewThumb.setVisibility(View.GONE);
				imageURL = "";
			}
        	
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
		new AlertDialog.Builder(this)
        .setIcon(R.drawable.thumb_send)
        .setTitle("确认发送吗？点击确认将发布您的状态")
        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	sendInternal();
            }
        })
        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        })
        .create().show();
	}
	
	private void sendInternal()
	{
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
		String postText = textInput.getText().toString();
		Oauth2AccessToken oa = MiscTool.getOauth2AccessToken();
		StatusesAPI statusesAPI = new StatusesAPI(oa);
		if(StringTool.isNullOrEmpty(imageURL))
		{
			statusesAPI.update(postText, null, null, mSinaWeiboPostStatusRequestListner);
			actionBar.getProgressBar().post(new Runnable() {			
				@Override
				public void run() {
					actionBar.setProgressBarVisibility(View.VISIBLE);				
				}
			});
		}
		else 
		{			
			statusesAPI.upload(postText, imageURL, null, null, mSinaWeiboPostStatusRequestListner);				
			actionBar.getProgressBar().post(new Runnable() {			
				@Override
				public void run() {
					actionBar.setProgressBarVisibility(View.VISIBLE);				
				}
			});
		}
	}
	
	private RequestListener mSinaWeiboPostStatusRequestListner = new RequestListener(){

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
		String postText = textInput.getText().toString();
		AsyncRenren asyncRenren = new AsyncRenren(App.getRenren());
		if(StringTool.isNullOrEmpty(imageURL))
		{
			StatusSetRequestParam param = new StatusSetRequestParam(postText);
			asyncRenren.publishStatus(param, mRenrenPostStatusRequestListner, true);			
		}
		else
		{
			File file = new File(imageURL);
			PhotoUploadRequestParam param = new PhotoUploadRequestParam(file);
			param.setCaption(postText);
			asyncRenren.publishPhoto(param, mRenrenUploadPhotoRequestListner);
		}

		actionBar.getProgressBar().post(new Runnable() {			
			@Override
			public void run() {
				actionBar.setProgressBarVisibility(View.VISIBLE);				
			}
		});
	}
	
	private AbstractRequestListener<StatusSetResponseBean> mRenrenPostStatusRequestListner = new AbstractRequestListener<StatusSetResponseBean> (){

		@Override
		public void onComplete(StatusSetResponseBean bean) {
			if(bean.getResult() == 1)
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
			else 
			{
				ToastHelper.show(">_< 由于未知原因，发送失败，请保持网络畅通", true);
				actionBar.getProgressBar().post(new Runnable() {			
					@Override
					public void run() {					
						actionBar.setProgressBarVisibility(View.GONE);				
					}
				});
			}
		}

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
	};
	
	private AbstractRequestListener<PhotoUploadResponseBean> mRenrenUploadPhotoRequestListner =  new AbstractRequestListener<PhotoUploadResponseBean>() {

		@Override
		public void onComplete(PhotoUploadResponseBean bean) {
			if(bean != null && !StringTool.isNullOrEmpty(bean.getSrc()))
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
			else 
			{
				ToastHelper.show(">_< 由于未知原因，发送失败，请保持网络畅通", true);
				actionBar.getProgressBar().post(new Runnable() {			
					@Override
					public void run() {					
						actionBar.setProgressBarVisibility(View.GONE);				
					}
				});
			}
			
		}

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
	};
	
	

	private void doubanSend()
	{
		final String postText = textInput.getText().toString();
		final String token = PreferenceHelper.getString("Douban_Token");
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String url = String.format("%s/shuo/v2/statuses/",
							DefaultConfigs.API_URL_PREFIX);
					if(StringTool.isNullOrEmpty(imageURL))
					{
						HttpManager httpManager = new HttpManager(token);						
						Map<String, String> params = new HashMap<String, String>();
						params.put("text", postText);
						params.put("source", AppConstants.DOUBAN_SECRET_KEY);
						httpManager.postEncodedEntry(url, params, true);
					}
					else
					{
						byte[] bytes = Util.fileToByteArray(new File(imageURL));
						
						File imageFile = new File(imageURL);
						String name = imageFile.getName();
						Bundle bundle = new Bundle();
						bundle.putString("text", postText);					
						bundle.putString("source", AppConstants.DOUBAN_SECRET_KEY);
						HttpURLConnection conn = UploadTool.doubanSendFormdata(url,
								token, bundle, name, bytes);
						
						StringBuilder sb = new StringBuilder();
						InputStream in = conn.getInputStream();
				        BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
				        for (String line = r.readLine(); line != null; line = r.readLine()) {
				            sb.append(line);
				        }
				        in.close();
				        String res = sb.toString();
					}

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
