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

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.IntentAction;
import com.thankcreate.care.App;
import com.thankcreate.care.AppConstants;
import com.thankcreate.care.MainActivity;
import com.thankcreate.care.R;
import com.thankcreate.care.R.drawable;
import com.thankcreate.care.R.id;
import com.thankcreate.care.R.layout;
import com.thankcreate.care.R.menu;
import com.thankcreate.care.tool.misc.MiscTool;
import com.thankcreate.care.tool.misc.PreferenceHelper;
import com.thankcreate.care.tool.ui.ToastHelper;
import com.thankcreate.care.viewmodel.EntryType;
import com.thankcreate.care.viewmodel.SimpleTableModel;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;


import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.api.UsersAPI;
import com.weibo.sdk.android.api.WeiboAPI.FEATURE;
import com.weibo.sdk.android.net.RequestListener;
import com.weibo.sdk.android.sso.SsoHandler;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class AccountActivity extends Activity {

	private ActionBar actionBar;
	private ListView sinaWeiboListView;
	private AccountGroupAdapter sinaWeiboAdapter = null;
	private SsoHandler mSsoHandler;
	
	
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
	}



	private void initControlBind()
	{
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		sinaWeiboListView = (ListView) findViewById(R.id.listView1);
	}
	
	private void initActionBar()
	{		
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
		sinaWeiboListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long id) {
				// 登陆
				if(position == 0) {
					try {
						Class sso = Class.forName("com.weibo.sdk.android.sso.SsoHandler");
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					Activity activity = AccountActivity.this;
					mSsoHandler = new SsoHandler(AccountActivity.this, App
							.sinaWeibo);
					mSsoHandler.authorize(weiboAuthListener);
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
					PreferenceHelper.removeSinaWeiboPreference();
					initSinaWeibo();
					App.mainViewModel.isChanged = true;
				}
			}
			
		});
	}
	
	private WeiboAuthListener weiboAuthListener = new WeiboAuthListener (){
        @Override
        public void onComplete(Bundle values) {
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
    		usersAPI.show(lID, sinaWeiboShowRequestListener);
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
    
    RequestListener sinaWeiboShowRequestListener = new RequestListener()
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
			} catch (JSONException e) {
				e.printStackTrace();
			}			
		}

		@Override
		public void onError(WeiboException arg0) {

			
		}

		@Override
		public void onIOException(IOException arg0) {

			
		}	
	};
		
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /**
         * 下面两个注释掉的代码，仅当sdk支持sso时有效，
         */
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_account, menu);
		return true;
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
				return 3;
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
			public View getView(int position, View convertView, ViewGroup parent) {	
				ViewHolder holder = null;
				if(convertView == null)
				{
					holder = new ViewHolder();
					if(position < 2)
					{
						convertView = mInflater.inflate(R.layout.listview_item_account, null);
						holder.textViewPrefix = (TextView) convertView.findViewById(R.id.account_list_item_prefix);	
						holder.textViewValue = (TextView) convertView.findViewById(R.id.account_list_item_value);	
						convertView.setTag(holder);
					}
					else 
					{
						convertView = mInflater.inflate(R.layout.listview_item_account_logout, null);
					}						
				}
				else
				{
					if(position < 2)
					{
						holder = (ViewHolder)convertView.getTag();
					}
				}
				
				if(position < 2)
				{
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
