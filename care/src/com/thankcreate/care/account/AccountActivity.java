package com.thankcreate.care.account;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.dongxuexidu.douban4j.constants.DefaultConfigs;
import com.dongxuexidu.douban4j.model.app.AccessToken;
import com.dongxuexidu.douban4j.model.app.DoubanException;
import com.dongxuexidu.douban4j.model.user.DoubanUserObj;
import com.dongxuexidu.douban4j.provider.DoubanDialog;
import com.dongxuexidu.douban4j.service.DoubanUserService;
import com.dongxuexidu.douban4j.utils.DoubanAuthListener;
import com.dongxuexidu.douban4j.utils.HttpManager;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.IntentAction;
import com.renren.api.connect.android.AsyncRenren;
import com.renren.api.connect.android.Renren;
import com.renren.api.connect.android.Util;
import com.renren.api.connect.android.common.AbstractRequestListener;
import com.renren.api.connect.android.exception.RenrenAuthError;
import com.renren.api.connect.android.exception.RenrenError;
import com.renren.api.connect.android.users.UserInfo;
import com.renren.api.connect.android.users.UsersGetInfoRequestParam;
import com.renren.api.connect.android.users.UsersGetInfoResponseBean;
import com.renren.api.connect.android.view.RenrenAuthListener;
import com.thankcreate.care.App;
import com.thankcreate.care.AppConstants;
import com.thankcreate.care.BaseActivity;
import com.thankcreate.care.MainActivity;
import com.thankcreate.care.R;
import com.thankcreate.care.R.drawable;
import com.thankcreate.care.R.id;
import com.thankcreate.care.R.layout;
import com.thankcreate.care.R.menu;
import com.thankcreate.care.rss.RssSetActivity;
import com.thankcreate.care.tool.misc.MiscTool;
import com.thankcreate.care.tool.misc.PreferenceHelper;
import com.thankcreate.care.tool.misc.StringTool;
import com.thankcreate.care.tool.ui.ListViewTool;
import com.thankcreate.care.tool.ui.ToastHelper;
import com.thankcreate.care.viewmodel.EntryType;
import com.thankcreate.care.viewmodel.SimpleTableModel;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;


import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.api.UsersAPI;
import com.weibo.sdk.android.api.WeiboAPI.FEATURE;
import com.weibo.sdk.android.net.RequestListener;
import com.weibo.sdk.android.sso.SsoHandler;

import android.os.Bundle;
import android.os.StrictMode;
import android.R.bool;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class AccountActivity extends BaseActivity {

	
	private ScrollView scrollRoot;
	private ActionBar actionBar;
	private ListView sinaWeiboListView;
	private ListView renrenListView;
	private ListView doubanListView;
	private ListView rssListView;
	private AccountGroupAdapter sinaWeiboAdapter = null;
	private AccountGroupAdapter renrenAdapter = null;
	private AccountGroupAdapter doubanAdapter = null;
	private AccountGroupAdapter rssAdapter = null;
	private SsoHandler msinaWeiboSsoHandler;
		
	private ProgressDialog mSpinner;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account_account);
		
		initControlBind();
		initActionBar();		
	}
	
	
	
	@Override
	protected void onResume() { 
		super.onResume();
		initSinaWeibo();
		initRenren();
		initDouban();
		initRss();
	}



	private void initControlBind()
	{
		scrollRoot = (ScrollView) findViewById(R.id.status_detail_scroll_root);	
		sinaWeiboListView = (ListView) findViewById(R.id.account_list_sinaweibo);
		renrenListView = (ListView) findViewById(R.id.account_list_renren);
		doubanListView = (ListView) findViewById(R.id.account_list_douban);
		rssListView = (ListView) findViewById(R.id.account_list_rss);
		
		
		mSpinner = new ProgressDialog(this);
		mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mSpinner.setMessage("Loading...");
	}
	
	private void initActionBar()
	{	
		actionBar = (ActionBar) findViewById(R.id.actionbar);
        actionBar.setTitle("帐号");       
        actionBar.SetTitleLogo(R.drawable.tab_account);
	}
	
	

	private void initSinaWeibo()
	{
		SharedPreferences pref = AccountActivity.this
				.getSharedPreferences(AppConstants.PREFERENCES_NAME,
						Context.MODE_APPEND);
		String myName = pref.getString("SinaWeibo_NickName", "未登陆");	
		String herName = pref.getString("SinaWeibo_FollowerNickName", "未指定");
		
		
		SimpleTableModel model1 = new SimpleTableModel();
		model1.prefix = "登陆帐号:";
		model1.value = myName;
		SimpleTableModel model2 = new SimpleTableModel();
		model2.prefix = "关注帐号:";
		model2.value = herName;
		SimpleTableModel model3 = new SimpleTableModel();
		
		sinaWeiboAdapter = new AccountGroupAdapter(getApplicationContext());
		sinaWeiboAdapter.addItem(model1);
		sinaWeiboAdapter.addItem(model2);
		sinaWeiboAdapter.addItem(model3);
		sinaWeiboListView.setAdapter(sinaWeiboAdapter);
		ListViewTool.setListViewHeightBasedOnChildren(sinaWeiboListView, 0 , true);
		sinaWeiboListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long id) {
				// 登陆
				if(position == 0) {
					MiscTool.clearCookie(AccountActivity.this);
					try {
						Class sso = Class.forName("com.weibo.sdk.android.sso.SsoHandler");
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					
					Activity activity = AccountActivity.this;
					msinaWeiboSsoHandler = new SsoHandler(AccountActivity.this, App
							.getSinaWeibo());
					msinaWeiboSsoHandler.authorize(mWeiboAuthListener);
				}
				// 指定关注人
				else if (position == 1) {
					if(MiscTool.isSinaWeiboLogin()) {
						Intent intent = new Intent();
						intent.setClass(AccountActivity.this, AccountSelectFreindActivity.class);
						intent.putExtra("type", EntryType.SinaWeibo);						
						startActivity(intent);
					}
					else {
						Builder alertDialog = new  AlertDialog.Builder(AccountActivity.this);
						alertDialog.setTitle(">_<");
						alertDialog.setMessage("还没有登陆怎么指定关注的人说~");
						alertDialog.setPositiveButton("朕知道了" ,  null);
						alertDialog.show();
					}
					
				}
				// 退出
				else if (position == 2) {
					new AlertDialog.Builder(AccountActivity.this)
			        .setIcon(R.drawable.tab_account)
			        .setTitle("确认退出登陆？")
			        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int whichButton) {
							PreferenceHelper.removeSinaWeiboPreference();
							initSinaWeibo();
							App.mainViewModel.isChanged = true;
			            }
			        })
			        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int whichButton) {
			            }
			        })
			        .create().show();
				}
			}
			
		});
	}
		
	private void initRenren()
	{
		SharedPreferences pref = AccountActivity.this
				.getSharedPreferences(AppConstants.PREFERENCES_NAME,
						Context.MODE_APPEND);
		String myName = pref.getString("Renren_NickName", "未登陆");	
		String herName = pref.getString("Renren_FollowerNickName", "未指定");
		
		
		SimpleTableModel model1 = new SimpleTableModel();
		model1.prefix = "登陆帐号:";
		model1.value = myName;
		SimpleTableModel model2 = new SimpleTableModel();
		model2.prefix = "关注帐号:";
		model2.value = herName;
		SimpleTableModel model3 = new SimpleTableModel();
		
		renrenAdapter = new AccountGroupAdapter(getApplicationContext());
		renrenAdapter.addItem(model1);
		renrenAdapter.addItem(model2);
		renrenAdapter.addItem(model3);
		renrenListView.setAdapter(renrenAdapter);
		ListViewTool.setListViewHeightBasedOnChildren(renrenListView, 0 , true);
		renrenListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long id) {
				// 登陆
				if(position == 0) {
					// 人人如果已经登陆了一个帐号，再想换个号登陆，如果不logout，则总是原帐号
					// 这应该是个api的bug
					MiscTool.clearCookie(AccountActivity.this);	
	            	PreferenceHelper.removeRenrenPreference();				
					App.getRenren().logout(AccountActivity.this);
					initRenren();
					App.mainViewModel.isChanged = true;
					
					Activity activity = AccountActivity.this;
					App.getRenren().authorize(AccountActivity.this, AppConstants.RENREN_PERMISSION, mRenrenAuthListener);
				}
				// 指定关注人
				else if (position == 1) {
					
					if(MiscTool.isRenrenLogin()) {
						Intent intent = new Intent();
						intent.setClass(AccountActivity.this, AccountSelectFreindActivity.class);
						intent.putExtra("type", EntryType.Renren);
						startActivity(intent);
					}
					else {
						Builder alertDialog = new  AlertDialog.Builder(AccountActivity.this);
						alertDialog.setTitle(">_<");
						alertDialog.setMessage("还没有登陆怎么指定关注的人说~");
						alertDialog.setPositiveButton("朕知道了" ,  null);
						alertDialog.show();
					}
				}
				// 退出
				else if (position == 2) {
					new AlertDialog.Builder(AccountActivity.this)
			        .setIcon(R.drawable.tab_account)
			        .setTitle("确认退出登陆？")
			        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int whichButton) {
			            	PreferenceHelper.removeRenrenPreference();				
							App.getRenren().logout(AccountActivity.this);
							initRenren();
							App.mainViewModel.isChanged = true;
			            }
			        })
			        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int whichButton) {
			            }
			        })
			        .create().show();
				
				}
			}
			
		});
	}
	
	private void initDouban()
	{
		SharedPreferences pref = AccountActivity.this
				.getSharedPreferences(AppConstants.PREFERENCES_NAME,
						Context.MODE_APPEND);
		String myName = pref.getString("Douban_NickName", "未登陆");	
		String herName = pref.getString("Douban_FollowerNickName", "未指定");
		
		
		SimpleTableModel model1 = new SimpleTableModel();
		model1.prefix = "登陆帐号:";
		model1.value = myName;
		SimpleTableModel model2 = new SimpleTableModel();
		model2.prefix = "关注帐号:";
		model2.value = herName;
		SimpleTableModel model3 = new SimpleTableModel();
		
		doubanAdapter = new AccountGroupAdapter(getApplicationContext());
		doubanAdapter.addItem(model1);
		doubanAdapter.addItem(model2);
		doubanAdapter.addItem(model3);
		doubanListView.setAdapter(doubanAdapter);
		ListViewTool.setListViewHeightBasedOnChildren(doubanListView, 0 , true);
		doubanListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long id) {
				// 登陆
				if(position == 0) {
					DoubanDialog dlg = new DoubanDialog(AccountActivity.this,  mDoubanAuthListener);
					dlg.show();
				}
				// 指定关注人
				else if (position == 1) {
					if(MiscTool.isDoubanLogin()) {
						Intent intent = new Intent();
						intent.setClass(AccountActivity.this, AccountSelectFreindActivity.class);
						intent.putExtra("type", EntryType.Douban);
						startActivity(intent);
					}
					else {
						Builder alertDialog = new  AlertDialog.Builder(AccountActivity.this);
						alertDialog.setTitle(">_<");
						alertDialog.setMessage("还没有登陆怎么指定关注的人说~");
						alertDialog.setPositiveButton("朕知道了" ,  null);
						alertDialog.show();
					}
				}
				// 退出
				else if (position == 2) {
					new AlertDialog.Builder(AccountActivity.this)
			        .setIcon(R.drawable.tab_account)
			        .setTitle("确认退出登陆？")
			        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int whichButton) {
							PreferenceHelper.removeDoubanPreference();
							initDouban();
							App.mainViewModel.isChanged = true;
			            }
			        })
			        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int whichButton) {
			            }
			        })
			        .create().show();
				}
			}
		});
	}
	
	private void initRss()
	{
		SharedPreferences pref = AccountActivity.this
				.getSharedPreferences(AppConstants.PREFERENCES_NAME,
						Context.MODE_APPEND);
		String siteTitle = pref.getString("RSS_FollowerSiteTitle", "未订阅");
		
		SimpleTableModel model1 = new SimpleTableModel();
		model1.prefix = "当前订阅:";
		model1.value = siteTitle;
		SimpleTableModel model2 = new SimpleTableModel();
		model2.value = "取消订阅";		
		
		rssAdapter = new AccountGroupAdapter(getApplicationContext());
		rssAdapter.addItem(model1);
		rssAdapter.addItem(model2);		
		rssListView.setAdapter(rssAdapter);
		ListViewTool.setListViewHeightBasedOnChildren(rssListView, 0 , true);
		rssListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long id) {
				// 登陆
				if(position == 0) {
					Intent intent = new Intent();
					intent.setClass(AccountActivity.this, RssSetActivity.class);					
					startActivity(intent);
				}
				// 退出
				else if (position == 1) {
					PreferenceHelper.removeRssPreference();
					initRss();
					App.mainViewModel.isChanged = true;
				}
			}
		});
	}
	
	private void showProgress(final boolean isShow)
	{
		if(actionBar == null || mSpinner == null)
			return;
		actionBar.post(new Runnable() {
			@Override
			public void run() {
				if(isShow)
					mSpinner.show();
				else
					mSpinner.dismiss();
			}
		});
	}
	
	
	
	private WeiboAuthListener mWeiboAuthListener = new WeiboAuthListener (){
        @Override
        public void onComplete(Bundle values) {
        	try {
    			String id = values.getString("uid");
    			String token = values.getString("access_token");
    			String expires_in = values.getString("expires_in");
    			long exp = System.currentTimeMillis() + Long.parseLong(expires_in)
    					* 1000;
    			

    			SharedPreferences pref = AccountActivity.this.getSharedPreferences(
    					AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
    			Editor editor = pref.edit();			
    			editor.putString("SinaWeibo_ID", id);
    			editor.putString("SinaWeibo_Token", token);
    			editor.putLong("SinaWeibo_ExpirationDate", exp);
    			editor.commit();

        		Oauth2AccessToken oauth2AccessToken = new Oauth2AccessToken();
        		oauth2AccessToken.setToken(token);
        		oauth2AccessToken.setExpiresTime(exp);    		
        		
        		UsersAPI usersAPI=new UsersAPI(oauth2AccessToken);
        		long lID = Long.parseLong(id);
        		usersAPI.show(lID, mSinaWeiboShowRequestListener);
        		showProgress(true);
			} catch (Exception e) {
				ToastHelper.show( "授权过程中发生未知错误，请确保网络通畅");  
				PreferenceHelper.removeSinaWeiboPreference();
			}

        }
        @Override
        public void onError(WeiboDialogError e) {        	
        	ToastHelper.show( "授权过程中发生未知错误，请确保网络通畅");           
        }

        @Override
        public void onCancel() {
            
        }

        @Override
        public void onWeiboException(WeiboException e) {
        	ToastHelper.show( "授权过程中发生未知错误，请确保网络通畅");
        }
    };
    
    private RequestListener mSinaWeiboShowRequestListener = new RequestListener()
	{

		@Override
		public void onComplete(String arg0) {
			try {
				JSONObject jsonObject = new JSONObject(arg0);
				String name = jsonObject.getString("screen_name");
				String avatar = jsonObject.getString("profile_image_url");

				SharedPreferences pref = AccountActivity.this
						.getSharedPreferences(AppConstants.PREFERENCES_NAME,
								Context.MODE_APPEND);
				Editor editor = pref.edit();
				editor.putString("SinaWeibo_NickName", name);
				editor.putString("SinaWeibo_Avatar", avatar);
				editor.commit();
				
				sinaWeiboListView.post(new Runnable() {					
					@Override
					public void run() {						
						initSinaWeibo();
					}
				});
				showProgress(false);
			} catch (JSONException e) {
				showProgress(false);
				ToastHelper.show( "授权过程中发生未知错误，请确保网络通畅");
				PreferenceHelper.removeSinaWeiboPreference();				
			}		
			
		}

		@Override
		public void onError(WeiboException arg0) {
			showProgress(false);
			ToastHelper.show( "授权过程中发生未知错误，请确保网络通畅");
			PreferenceHelper.removeSinaWeiboPreference();
		}

		@Override
		public void onIOException(IOException arg0) {
			showProgress(false);
			ToastHelper.show( "授权过程中发生未知错误，请确保网络通畅");
			PreferenceHelper.removeSinaWeiboPreference();
		}	
	};
	
	
	private RenrenAuthListener mRenrenAuthListener = new RenrenAuthListener() {
		
		@Override
		public void onRenrenAuthError(RenrenAuthError renrenAuthError) {
			ToastHelper.show( "授权过程中发生未知错误，请确保网络通畅");			
		}
		
		@Override
		public void onComplete(Bundle values) {
			// 人人登陆后不返回ID		
			try {				
				String token = values.getString("access_token");
				String expires_in = values.getString("expires_in");
				long exp = System.currentTimeMillis() + Long.parseLong(expires_in)
						* 1000;
				
				SharedPreferences pref = AccountActivity.this.getSharedPreferences(
						AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
				Editor editor = pref.edit();
				editor.putString("Renren_Token", token);
				editor.putLong("Renren_ExpirationDate", exp);
				editor.commit();

				AsyncRenren asyncRenren = new AsyncRenren(App.getRenren());	
		
				UsersGetInfoRequestParam param = new UsersGetInfoRequestParam(null, "uid,name,sex,birthday,headurl"); 
				asyncRenren.getUsersInfo(param, mRenrenShowRequestListener);
				showProgress(true);
			} catch (Exception e) {
				ToastHelper.show( "授权过程中发生未知错误，请确保网络通畅");
				PreferenceHelper.removeRenrenPreference();
			}
		}
		
		@Override
		public void onCancelLogin() {
		}
		
		@Override
		public void onCancelAuth(Bundle values) {				
		}
	};
	
	private AbstractRequestListener<UsersGetInfoResponseBean> mRenrenShowRequestListener = new AbstractRequestListener<UsersGetInfoResponseBean>() {
		public void onComplete(UsersGetInfoResponseBean bean) {
			try {
				ArrayList<UserInfo> listUsers = bean.getUsersInfo();
				
				UserInfo user = listUsers.get(0);
				SharedPreferences pref = AccountActivity.this
						.getSharedPreferences(AppConstants.PREFERENCES_NAME,
								Context.MODE_APPEND);
				Editor editor = pref.edit();
				editor.putString("Renren_ID", String.valueOf(user.getUid()));
				editor.putString("Renren_NickName", user.getName());
				editor.putString("Renren_Avatar", user.getHeadurl());
				editor.commit();
				renrenListView.post(new Runnable() {
					
					@Override
					public void run() {
						initRenren();						
					}
				});
				showProgress(false);
				
			} catch (Exception e) {
				showProgress(false);
				ToastHelper.show( "授权过程中发生未知错误，请确保网络通畅");	
				PreferenceHelper.removeRenrenPreference();
				App.getRenren().logout(getApplicationContext());
			}
		}

		public void onRenrenError(RenrenError renrenError) {
			showProgress(false);
			ToastHelper.show( "授权过程中发生未知错误，请确保网络通畅");	
			PreferenceHelper.removeRenrenPreference();
			App.getRenren().logout(getApplicationContext());
		}

		public void onFault(Throwable fault) {
			showProgress(false);
			ToastHelper.show( "授权过程中发生未知错误，请确保网络通畅");	
			PreferenceHelper.removeRenrenPreference();
			App.getRenren().logout(getApplicationContext());
		}
	};
	
	private DoubanAuthListener mDoubanAuthListener = new DoubanAuthListener() {
		
		@Override
		public void onError(String e) {
			ToastHelper.show( "授权过程中发生未知错误，请确保网络通畅");	
			PreferenceHelper.removeDoubanPreference();
		}
		
		@Override
		public void onComplete(AccessToken values) {
			try {
    			String id = values.getDoubanUserId();
    			final String token = values.getAccessToken();
    			Integer expires_in = values.getExpiresIn();
    			String refresh_token = values.getRefreshToken();
    			long exp = System.currentTimeMillis() + expires_in * 1000;    			

    			SharedPreferences pref = AccountActivity.this.getSharedPreferences(
    					AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
    			Editor editor = pref.edit();			
    			editor.putString("Douban_ID", id);
    			editor.putString("Douban_Token", token);
    			editor.putString("Douban_RefreshToken", refresh_token);
    			editor.putLong("Douban_ExpirationDate", exp);    			
    			editor.commit();

    			showProgress(true);
        		new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							HttpManager httpManager = new HttpManager(token);
							String result = httpManager.getResponseString(DefaultConfigs.API_URL_PREFIX + "/v2/user/~me", null, true);
							JSONObject object = new JSONObject(result);
														
							String name = object.optString("name");
							String avatar = object.optString("avatar");

							SharedPreferences pref = AccountActivity.this
									.getSharedPreferences(AppConstants.PREFERENCES_NAME,
											Context.MODE_APPEND);
							Editor editor = pref.edit();
							editor.putString("Douban_NickName", name);
							editor.putString("Douban_Avatar", avatar);
							editor.commit();
							
							doubanListView.post(new Runnable() {					
								@Override
								public void run() {						
									initDouban();
								}
							});		
							showProgress(false);
						} catch (Exception e) {
							ToastHelper.show( "授权过程中发生未知错误，请确保网络通畅");  
							PreferenceHelper.removeDoubanPreference();
							showProgress(false);
							e.printStackTrace();
						} 
					}
				}).start();
        		
			} catch (Exception e) {
				ToastHelper.show( "授权过程中发生未知错误，请确保网络通畅");  
				PreferenceHelper.removeDoubanPreference();
			}
		}
		
		@Override
		public void onCancel() {
		}
	};
		
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /**
         * 下面两个注释掉的代码，仅当sdk支持sso时有效，
         */
        if (msinaWeiboSsoHandler != null) {
            msinaWeiboSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_account, menu);
		return false;
	}

	
	 class AccountGroupAdapter extends BaseAdapter {

			private List<SimpleTableModel> listModel = new ArrayList();;
			private LayoutInflater mInflater;
			
			public AccountGroupAdapter(Context context) {
				super();
				mInflater = LayoutInflater.from(context);
			}
			
			public void addItem(SimpleTableModel model) {
				listModel.add(model);
				notifyDataSetChanged();
			}

			@Override
			public int getCount() {
				return listModel.size();
			}

			@Override
			public Object getItem(int position) {
				try {
					return listModel.get(position);
				} catch (Exception e) {
					return null;
				}
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			/**
			 * 因为表格中有两种格式的，所以这里不做convert了，每次都建一个新view，不然极易崩溃
			 */
			public View getView(int position, View convertView, ViewGroup parent) {	
				ViewHolder holder = null;
//				if(convertView == null)
//				{
					holder = new ViewHolder();
					if(position < listModel.size() - 1)
					{
						convertView = mInflater.inflate(R.layout.listview_item_account, null);
						holder.textViewPrefix = (TextView) convertView.findViewById(R.id.account_list_item_prefix);	
						holder.textViewValue = (TextView) convertView.findViewById(R.id.account_list_item_value);	
						convertView.setTag(holder);						
					}
					else 
					{
						convertView = mInflater.inflate(R.layout.listview_item_account_logout, null);
						SimpleTableModel md = listModel.get(position);
						if(!StringTool.isNullOrEmpty(md.value))
						{
							TextView txt = (TextView) convertView.findViewById(R.id.setting_list_item_logout_text);
							txt.setText(md.value);
						}
					}						
//				}
//				else
//				{
//					if(position < 2)
//					{
//						holder = (ViewHolder)convertView.getTag();
//					}
//				}
				
				if(position < listModel.size() - 1)
				{
					TextView test = holder.textViewPrefix;
					SimpleTableModel md = listModel.get(position);
					String test2 = md.prefix;
					holder.textViewPrefix.setText(listModel.get(position).prefix);					
					holder.textViewValue.setText(listModel.get(position).value);
				}
				
				
				return convertView;
			}
			
			public class ViewHolder {
		        public TextView textViewPrefix;
		        public TextView textViewValue;
		        public int tag;
		    }

		}
}
