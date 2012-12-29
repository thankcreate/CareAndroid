package com.thankcreate.care.status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.buuuk.android.gallery.ImageViewFlipper;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.thankcreate.care.App;
import com.thankcreate.care.AppConstants;
import com.thankcreate.care.R;
import com.thankcreate.care.R.drawable;
import com.thankcreate.care.R.layout;
import com.thankcreate.care.R.menu;
import com.thankcreate.care.account.AccountActivity;
import com.thankcreate.care.account.AccountSelectFreindActivity;
import com.thankcreate.care.tool.misc.DateTool;
import com.thankcreate.care.tool.misc.FirstCharactorComparator;
import com.thankcreate.care.tool.misc.StringTool;
import com.thankcreate.care.tool.ui.DrawableManager;
import com.thankcreate.care.tool.ui.RefreshViewerHelper;
import com.thankcreate.care.tool.ui.RefreshViewerHelper.OnRefreshCompleteListener;
import com.thankcreate.care.viewmodel.EntryType;
import com.thankcreate.care.viewmodel.FriendViewModel;
import com.thankcreate.care.viewmodel.ItemViewModel;
import com.thankcreate.care.viewmodel.SimpleTableModel;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Rect;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView.ScaleType;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class StatusTimelineActivity extends Activity implements OnRefreshCompleteListener {
	private ActionBar actionBar;
	private RefreshViewerHelper refreshViewerHelper;
	private DrawableManager drawableManager = App.drawableManager;
	private StatusTimelineAdapter adapter;
	private ListView listViewTimeline;
	private PullToRefreshListView pullToRefreshListView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_status_timeline);
		initControl();
		initActionBar();
	}

	private void initControl() {
		pullToRefreshListView =  (PullToRefreshListView) findViewById(R.id.listView1);	
		listViewTimeline = pullToRefreshListView.getRefreshableView();	
		pullToRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

				refresh();
			}
		});
		pullToRefreshListView.setOnItemClickListener(mOnItemClickListener);		
	}

	private void initActionBar() {
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("我只在乎你");
		actionBar.addActionLeft(new Action() {
			
			@Override
			public void performAction(View view) {				
				
			}
			
			@Override
			public int getDrawable() {				
				return drawable.thumb_write_new;
			}
		});

		actionBar.addActionRight(new Action() {

			@Override
			public void performAction(View view) {
				refresh();
			}

			@Override
			public int getDrawable() {
				return drawable.thumb_refresh;
			}
		});
	}
	
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener()
	{

		@Override
		public void onItemClick(AdapterView<?> arg0,  View view, int position,
				long id) {
			// 这里的position是从1开始算起的，真奇怪
			--position;
			if(position < 0 || position >= adapter.listModel.size())
				return;
			ItemViewModel item = adapter.listModel.get(position);
			Intent intent = new Intent();
			intent.setClass(StatusTimelineActivity.this, StatusDetailActivity.class);			
			intent.putExtra("itemViewModel", item);
			startActivity(intent); 
		}			
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_status_timeline, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(App.mainViewModel.isChanged)
		{
			refresh();
		}
	}
	
	private void refresh() {
		
		actionBar.setProgressBarVisibility(View.VISIBLE);
		if(refreshViewerHelper == null)
		{
			refreshViewerHelper = RefreshViewerHelper.getInstance();
			refreshViewerHelper.addListenter(this);
		}
		refreshViewerHelper.refreshMainViewModel();
	}

	@Override
	public void onRefreshComplete() {		
		
		adapter = new StatusTimelineAdapter(this);
		adapter.setListModel(App.mainViewModel.items);
		
		listViewTimeline.post(new Runnable() {					
			@Override
			public void run() {		
				actionBar.setProgressBarVisibility(View.GONE);
				pullToRefreshListView.onRefreshComplete();
				listViewTimeline.setAdapter(adapter);
			}
		});	
	}
	
	
	 class StatusTimelineAdapter extends BaseAdapter {

			public List<ItemViewModel> listModel = new ArrayList();;
			private LayoutInflater mInflater;
			
			public StatusTimelineAdapter(Context context) {
				super();
				mInflater = LayoutInflater.from(context);
			}
			
			public void addItem(ItemViewModel model) {
				listModel.add(model);
				notifyDataSetChanged();
			}
			
			public void setListModel(List<ItemViewModel> input){
				listModel = input;
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
				if(convertView == null)
				{
					holder = new ViewHolder();
					
					convertView = mInflater.inflate(R.layout.listview_item_status_timeline, null);
					holder.imageAvatar = (ImageView) convertView.findViewById(R.id.status_list_item_avatar);
					holder.textTitle = (TextView) convertView.findViewById(R.id.status_list_item_title);	
					holder.textContent = (TextView) convertView.findViewById(R.id.status_list_item_content);
					holder.imageThumb = (ImageView) convertView.findViewById(R.id.status_list_item_thumb);
					
					holder.layoutForward = (LinearLayout) convertView.findViewById(R.id.status_list_item_forward);
					holder.textForwardContent = (TextView) convertView.findViewById(R.id.status_list_item_forward_content);
					holder.imageForwardThumb = (ImageView) convertView.findViewById(R.id.status_list_item_forward_thumb);
					holder.textTime = (TextView) convertView.findViewById(R.id.status_list_item_time);
					holder.textCommentCount = (TextView) convertView.findViewById(R.id.status_list_item_comment_count);
					convertView.setTag(holder);
				}
				else
				{
					holder = (ViewHolder)convertView.getTag();				
				}
			
				final ItemViewModel item = listModel.get(position);

				if(item == null)
					return null;
				holder.imageAvatar.setTag(item.iconURL);
				drawableManager.fetchDrawableOnThread(item.iconURL, holder.imageAvatar);
				holder.textTitle.setText(item.title);
				holder.textContent.setText(item.content);
				if(StringTool.isNullOrEmpty(item.imageURL))
				{
					holder.imageThumb.setVisibility(View.GONE);
				}
				else
				{
					holder.imageThumb.setVisibility(View.VISIBLE);
					holder.imageThumb.setTag(item.imageURL);
					holder.imageThumb.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent();
							intent.setClass(StatusTimelineActivity.this, ImageViewFlipper.class);
							intent.putExtra("src", item.fullImageURL);			
							startActivity(intent); 
						}
					});
					
					// 这里必须提前做大小的缓存，因为如果是从下往上滑的话，由于size是异步返回的
					// 会有一次明显的卡顿
					Rect cacheSize = drawableManager.adjustSizeMap.get(item.imageURL);
					if(cacheSize != null)
					{
						LayoutParams params = holder.imageThumb.getLayoutParams();
						params.height = cacheSize.height();
						params.width = cacheSize.width();
						holder.imageThumb.setLayoutParams(params);
					}
					DisplayMetrics metric = new DisplayMetrics();
					getWindowManager().getDefaultDisplay().getMetrics(metric);
					int maxSize = (int) (80 * metric.density);
					drawableManager.fetchDrawableOnThread(item.imageURL, holder.imageThumb, maxSize, true, this);					
				}
				
				// 转发部分
				if(item.forwardItem == null)
				{
					holder.layoutForward.setVisibility(View.GONE);
				}
				else
				{
					holder.layoutForward.setVisibility(View.VISIBLE);
					holder.textForwardContent.setText(item.forwardItem.getContentWithTitle());
					
					
					if(StringTool.isNullOrEmpty(item.forwardItem.imageURL))
					{
						holder.imageForwardThumb.setVisibility(View.GONE);
					}
					else
					{
						holder.imageForwardThumb.setVisibility(View.VISIBLE);
						holder.imageForwardThumb.setTag(item.forwardItem.imageURL);
						holder.imageForwardThumb.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent intent = new Intent();
								intent.setClass(StatusTimelineActivity.this, ImageViewFlipper.class);
								intent.putExtra("src", item.forwardItem.fullImageURL);			
								startActivity(intent); 
							}
						});					
						Rect cacheSize = drawableManager.adjustSizeMap.get(item.forwardItem.imageURL);
						if(cacheSize != null)
						{
							LayoutParams params = holder.imageForwardThumb.getLayoutParams();
							params.height = cacheSize.height();
							params.width = cacheSize.width();
							holder.imageForwardThumb.setLayoutParams(params);
						}
						DisplayMetrics metric = new DisplayMetrics();
				        getWindowManager().getDefaultDisplay().getMetrics(metric);
				        int maxSize =(int)( 80 * metric.density);
						drawableManager.fetchDrawableOnThread(item.forwardItem.imageURL, holder.imageForwardThumb, maxSize, true,this);
					}
				}
				holder.textTime.setText(DateTool.convertDateToStringInShow(item.time));
				holder.textCommentCount.setText(item.getCommentCount());				
				return convertView;
			}
			
			public class ViewHolder {
		        public ImageView imageAvatar;
		        public TextView textTitle;
		        public TextView textContent;
		        public ImageView imageThumb;
		        public LinearLayout layoutForward;		        
		        public TextView textForwardContent;
		        public ImageView imageForwardThumb;
		        public TextView textTime;
		        public TextView textCommentCount;
		        public int tag;
		    }

		}
	
	

}
