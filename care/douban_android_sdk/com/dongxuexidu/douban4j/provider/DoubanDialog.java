package com.dongxuexidu.douban4j.provider;

import java.io.IOException;
import java.io.InputStream;

import com.dongxuexidu.douban4j.constants.DefaultConfigs;
import com.dongxuexidu.douban4j.model.app.AccessToken;
import com.dongxuexidu.douban4j.model.app.DoubanException;
import com.dongxuexidu.douban4j.utils.DoubanAuthListener;
import com.thankcreate.care.AppConstants;
import com.thankcreate.care.R;
import com.weibo.sdk.android.util.Utility;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnKeyListener;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * 好吧，这个类其实是在山寨新浪微博的sdk 
 * @author ThankCreate
 *
 */
public class DoubanDialog extends Dialog {
	
	static  FrameLayout.LayoutParams FILL = new FrameLayout.LayoutParams(
			ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
	private String mUrl;
	private DoubanAuthListener mListener;
	private ProgressDialog mSpinner;
	private WebView mWebView;
	private RelativeLayout webViewContainer;
	private RelativeLayout mContent;

	private static int theme = android.R.style.Theme_Translucent_NoTitleBar;
	
	public DoubanDialog(Context context, DoubanAuthListener listener) {
		super(context, theme);
		mListener = listener;
		mUrl = String.format("%s?client_id=%s&response_type=code&redirect_uri=%s&display=mobile&scope=%s"
				,DefaultConfigs.AUTH_URL
				,DefaultConfigs.API_KEY
				,DefaultConfigs.ACCESS_TOKEN_REDIRECT_URL
				,AppConstants.DOUBAN_PERMISSION);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSpinner = new ProgressDialog(getContext());
		mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mSpinner.setMessage("Loading...");
		mSpinner.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				onBack();
				return false;
			}

		});
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFeatureDrawableAlpha(Window.FEATURE_OPTIONS_PANEL, 0);  
		mContent = new RelativeLayout(getContext());
		setUpWebView();

		addContentView(mContent, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
	}
	
	private void setUpWebView() {
		webViewContainer = new RelativeLayout(getContext());
		mWebView = new WebView(getContext());
		mWebView.setVerticalScrollBarEnabled(false);
		mWebView.setHorizontalScrollBarEnabled(false);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.setWebViewClient(new DoubanWebViewClient());
		mWebView.loadUrl(mUrl);
		mWebView.setLayoutParams(FILL);
		mWebView.setVisibility(View.INVISIBLE);
		
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		
		RelativeLayout.LayoutParams lp0 = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT);
		
        mContent.setBackgroundColor(Color.TRANSPARENT);

        webViewContainer.setBackgroundResource(R.drawable.dialog_bg);
        
       
		
        webViewContainer.addView(mWebView,lp0);
		webViewContainer.setGravity(Gravity.CENTER);
		
		
	    Resources resources = getContext().getResources();
	    lp.leftMargin=resources.getDimensionPixelSize(R.dimen.dialog_left_margin);
	    lp.rightMargin=resources.getDimensionPixelSize(R.dimen.dialog_right_margin);
	    lp.topMargin=resources.getDimensionPixelSize(R.dimen.dialog_top_margin);
	    lp.bottomMargin=resources.getDimensionPixelSize(R.dimen.dialog_bottom_margin);
		
        mContent.addView(webViewContainer, lp);
	}
	
	private void handleRedirectUrl(WebView view, String url) {
		Bundle values = Utility.parseUrl(url);

		String error = values.getString("error");
		String error_code = values.getString("error_code");
		String code = values.getString("code");

		if (error == null && error_code == null && !TextUtils.isEmpty(code)) {
			handleCode(code);
		} else if (error.equals("access_denied")) {
			// 用户或授权服务器拒绝授予数据访问权限
			mListener.onCancel();
			dismiss();
		} else {
			if(error_code==null){
				mListener.onError("");
				dismiss();
			}
			else{
				mListener.onError(error_code);
				dismiss();
			}
			
		}
	}
	
	private void handleCode(final String code) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				OAuthDoubanProvider provider = new OAuthDoubanProvider();
				AccessToken token;
				try {
					token = provider.tradeAccessTokenWithCode(code);
					mListener.onComplete(token);
				} catch (DoubanException e) {
					mListener.onError("");					
				}
				DoubanDialog.this.dismiss();
			}
		}).start();
	}
	
	private class DoubanWebViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {			
			 if (url.startsWith("sms:")) {  //针对webview里的短信注册流程，需要在此单独处理sms协议
	                Intent sendIntent = new Intent(Intent.ACTION_VIEW);  
	                sendIntent.putExtra("address", url.replace("sms:", ""));  
	                sendIntent.setType("vnd.android-dir/mms-sms");  
	                DoubanDialog.this.getContext().startActivity(sendIntent);  
	                return true;  
	            }  
			return super.shouldOverrideUrlLoading(view, url);
		}

		@Override
		public void onReceivedError(WebView view, int errorCode, String description,
				String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			mListener.onError("");
			DoubanDialog.this.dismiss();
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {			
			if (url.startsWith(DefaultConfigs.ACCESS_TOKEN_REDIRECT_URL)) {
				handleRedirectUrl(view, url);
				view.stopLoading();
				DoubanDialog.this.dismiss();
				return;
			}
			super.onPageStarted(view, url, favicon);
			mSpinner.show();
		}

		@Override
		public void onPageFinished(WebView view, String url) {			
			super.onPageFinished(view, url);
			if (mSpinner.isShowing()) {
				mSpinner.dismiss();
			}
			mWebView.setVisibility(View.VISIBLE);
		}

		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			handler.proceed();
		}

	}
	
	protected void onBack() {
		try {
			mSpinner.dismiss();
			if (null != mWebView) {
				mWebView.stopLoading();
				mWebView.destroy();
			}
		} catch (Exception e) {
		}
		dismiss();
	}

}
