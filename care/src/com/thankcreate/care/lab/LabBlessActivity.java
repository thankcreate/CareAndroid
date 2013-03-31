package com.thankcreate.care.lab;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.thankcreate.care.App;
import com.thankcreate.care.AppConstants;
import com.thankcreate.care.BaseActivity;
import com.thankcreate.care.R;
import com.thankcreate.care.R.layout;
import com.thankcreate.care.R.menu;
import com.thankcreate.care.account.AccountActivity;
import com.thankcreate.care.lab.LabSmartChatActivity.SmartChatListAdapter.ViewHolder;
import com.thankcreate.care.status.StatusAddCommentActivity;
import com.thankcreate.care.status.StatusDetailActivity;
import com.thankcreate.care.tool.misc.BlessHelper;
import com.thankcreate.care.tool.misc.StringTool;
import com.thankcreate.care.tool.misc.BlessHelper.FetchBlessItemListener;
import com.thankcreate.care.tool.misc.DateTool;
import com.thankcreate.care.viewmodel.BlessItemViewModel;
import com.thankcreate.care.viewmodel.ChatItemViewModel;
import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class LabBlessActivity extends BaseActivity {

	private ActionBar actionBar;
	private BlessHelper blessHelper;
	private ListView listView;
	private BlessListAdapter adapter;
	private ProgressDialog mSpinner;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lab_bless);
		initActionBar();
		initControl();		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		initLoad();
	}

	private void initActionBar() {
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("祝福墙");
		actionBar.addActionRight(new Action() {			
			@Override
			public void performAction(View view) {				
				Intent intent = new Intent();
				intent.setClass(LabBlessActivity.this, LabBlessPostActivity.class);							
				startActivity(intent);
			}
			
			@Override
			public int getDrawable() {
				return R.drawable.thumb_message_add;				
			}
		});
		addActionBarBackButton(actionBar);
	}
	
	private void initControl() {
		listView = (ListView) findViewById(R.id.lab_bless_list_view);
		mSpinner = new ProgressDialog(this);
		mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mSpinner.setMessage("Loading...");
	}

	private void initLoad() {
		if(blessHelper == null)
			blessHelper = new BlessHelper();
		mSpinner.show();
		blessHelper.fetchBlessItem(25, false, new FetchBlessItemListener() {
			@Override
			public void fetchComplete(List<BlessItemViewModel> resList) {				
				refreshBlessList(resList);
			}
		});
	}
	
	private void refreshBlessList(final List<BlessItemViewModel> modelList)
	{
		// 要放在ui线程里
		actionBar.post(new Runnable() {
			
			@Override
			public void run() {
				mSpinner.dismiss();
				adapter = new BlessListAdapter(LabBlessActivity.this);
				if(modelList != null)
				{
					// 刷新
					adapter.setListModel(modelList);
					listView.setAdapter(adapter);
					
					// 弹提示
					SharedPreferences pref = App.getAppContext().getSharedPreferences(
							AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
					String firstComeHere = pref.getString("Global_FirstLoadBlessList", "");
					if(StringTool.isNullOrEmpty(firstComeHere))
					{
						Editor editor = pref.edit();
						editor.putString("Global_FirstLoadBlessList", "whatever");
						editor.commit();
						
						Builder alertDialog = new  AlertDialog.Builder(LabBlessActivity.this);
						alertDialog.setTitle("^_^");
						alertDialog.setMessage("发表在祝福墙上的内容，写得比较好的会显示在软件启动页上哦~");
						alertDialog.setPositiveButton("寡人喻矣" ,  null);
						alertDialog.show();
					}
				}
			}
		});
	}

	
	
	class BlessListAdapter extends BaseAdapter {

		public List<BlessItemViewModel> listModel = new ArrayList();;
		private LayoutInflater mInflater;

		public BlessListAdapter(Context context) {
			super();
			mInflater = LayoutInflater.from(context);
		}

		public void refresh() {
			notifyDataSetChanged();
		}

		public void addItem(BlessItemViewModel model) {
			listModel.add(model);
			notifyDataSetChanged();
		}

		public void setListModel(List<BlessItemViewModel> input) {
			listModel = input;
			notifyDataSetChanged();
		}

		public void clear() {
			listModel.clear();
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
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			BlessItemViewModel model = listModel.get(position);
			if (model == null)
				return null;

			int remainDer = position % 2;
			if (convertView != null
					&& ((ViewHolder) convertView.getTag()).type == remainDer) {
				holder = (ViewHolder) convertView.getTag();
			} else {
				holder = new ViewHolder();
				
				if (remainDer == 0)
					convertView = mInflater.inflate(
							R.layout.listview_item_lab_bless_left, null);
				else
					convertView = mInflater.inflate(
							R.layout.listview_item_lab_bless_right, null);

				holder.textTitle = (TextView) convertView
						.findViewById(R.id.lab_bless_item_title);
				holder.textContent = (TextView) convertView
						.findViewById(R.id.lab_bless_item_content);
				holder.textTime = (TextView) convertView
						.findViewById(R.id.lab_bless_item_time);
				holder.type = remainDer;
				convertView.setTag(holder);
			}
			holder.textTitle.setText(model.title);
			holder.textContent.setText(model.content);
			holder.textTime.setText(DateTool
					.convertDateToStringInShow(model.time));
			return convertView;
		}

		public class ViewHolder {			
			public TextView textTitle;
			public TextView textContent;
			public TextView textTime;
			public int type;
		}
	}
	

}
