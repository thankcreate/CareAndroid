package com.thankcreate.care.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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
import com.thankcreate.care.BaseActivity;
import com.thankcreate.care.R;
import com.thankcreate.care.R.drawable;
import com.thankcreate.care.R.layout;
import com.thankcreate.care.R.menu;
import com.thankcreate.care.account.AccountActivity;
import com.thankcreate.care.account.AccountSelectFreindActivity;
import com.thankcreate.care.rss.RssDetailActivity;
import com.thankcreate.care.tool.misc.DateTool;
import com.thankcreate.care.tool.misc.FirstCharactorComparator;
import com.thankcreate.care.tool.misc.MiscTool;
import com.thankcreate.care.tool.misc.StringTool;
import com.thankcreate.care.tool.ui.DrawableManager;
import com.thankcreate.care.tool.ui.RefreshViewerHelper;
import com.thankcreate.care.tool.ui.ToastHelper;
import com.thankcreate.care.tool.ui.RefreshViewerHelper.OnRefreshCompleteListener;
import com.thankcreate.care.viewmodel.EntryType;
import com.thankcreate.care.viewmodel.FriendViewModel;
import com.thankcreate.care.viewmodel.ItemViewModel;
import com.thankcreate.care.viewmodel.SimpleTableModel;

import android.os.Bundle;
import android.os.DropBoxManager.Entry;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

public class StatusTimelineActivity extends BaseActivity implements OnRefreshCompleteListener {
	private ActionBar actionBar;
	private RefreshViewerHelper refreshViewerHelper;
	private DrawableManager drawableManager = App.drawableManager;
	private StatusTimelineAdapter adapter;
	private ListView listViewTimeline;
	private PullToRefreshListView pullToRefreshListView;
	
	private int sourceSelected = 0;
	private int[] typeArray = {EntryType.SinaWeibo, EntryType.Renren, EntryType.Douban};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_status_timeline);
		initControl();
		initActionBar();
		initSourceSelected();
		checkNetWork();

	}
	
	private void checkNetWork() {
		if(!MiscTool.isOnline())
		{
			ToastHelper.show("当前网络连接不可用",true);
		}
	}

	private void initControl() {
		pullToRefreshListView =  (PullToRefreshListView) findViewById(R.id.listViewTimeline);	
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
				postStatusClicked();
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
	
	private void initSourceSelected() {
		int tp = MiscTool.getFirstFoundLoginType();
		for (int i = 0; i < typeArray.length; i++) {
			if (typeArray[i] == tp)
				sourceSelected = i;
		}
	}
	
	private void postStatusClicked()
	{		
		 new AlertDialog.Builder(this)
	     .setIcon(R.drawable.thumb_share)
	     .setTitle("选择发布源")
	     .setSingleChoiceItems(R.array.array_post_source_choose, sourceSelected, new DialogInterface.OnClickListener() {
	         public void onClick(DialogInterface dialog, int whichButton) {
	        	 sourceSelected = whichButton;
	         }
	     })
	     .setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
	         public void onClick(DialogInterface dialog, int whichButton) {            	 
	        	 if(sourceSelected == 0)
	        		 postStatusSinaWeibo();
	        	 else if(sourceSelected == 1)
	        		 postStatusRenren();
	        	 else if(sourceSelected == 2)
	        		 postStatusDouban();
	         }
	     })
	     .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
	         public void onClick(DialogInterface dialog, int whichButton) {
	        	 Log.i("btnIndex", String.valueOf(whichButton));
	         }
	     })
	    .create().show();
	}
	
	private void postStatusSinaWeibo()
	{
		if(!MiscTool.isAuthValid(EntryType.SinaWeibo))
		{
			ToastHelper.show("新浪微博尚未登陆，或者登陆已过期~");
			return;
		}
		gotoStatusPostPage(EntryType.SinaWeibo);
	}
	
	private void postStatusRenren()
	{
		if(!MiscTool.isAuthValid(EntryType.Renren))
		{
			ToastHelper.show("人人帐号尚未登陆，或者登陆已过期~");
			return;
		}
		gotoStatusPostPage(EntryType.Renren);
	}
	
	private void postStatusDouban()
	{
		if(!MiscTool.isAuthValid(EntryType.Douban))
		{
			ToastHelper.show("豆瓣帐号尚未登陆，或者登陆已过期~");
			return;
		}
		gotoStatusPostPage(EntryType.Douban);
	}
	
	private void gotoStatusPostPage(int type)
	{
		Intent intent = new Intent();
		intent.setClass(this, StatusPostActivity.class);
		intent.putExtra("type", type);				
		startActivity(intent); 
	}
	
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener()
	{

		@Override
		public void onItemClick(AdapterView<?> arg0,  View view, int position,
				long id) {
			// 这里的position是从1开始算起的，真奇怪，应该是pull to refresh的header占住0了
			--position;
			if(position < 0 || position >= adapter.listModel.size())
				return;
			ItemViewModel item = adapter.listModel.get(position);
			Intent intent = new Intent();
			if(item.type == EntryType.Rss)
			{
				intent.setClass(StatusTimelineActivity.this, RssDetailActivity.class);
				intent.putExtra("itemViewModel", item);			
				startActivity(intent); 
			}
			else if(item.type == EntryType.SinaWeibo
					|| item.type == EntryType.Renren
					|| item.type == EntryType.Douban)
			{
				intent.setClass(StatusTimelineActivity.this, StatusDetailActivity.class);	
				intent.putExtra("itemViewModel", item);			
				startActivity(intent); 
			}
		}			
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_status_timeline, menu);
		return true;
	}

	
	
	private boolean firstLoadTimeline = true;
	/**
	 * Android版的“我只在乎你”的缓存机制与其它3个版本不一样
	 * 其它三个版本都是把缓存直接加载到mainViewModel的item里，然后再把网络得到的数据加载到同样的位置
	 * 有一个明显的先后顺序，缓存的加载必然比从网络得到的快
	 * 但是因为安卓的Splash是个伪启动画面，它就是一个普通的Activity加上延时跳转
	 * 为了不浪费那个延时，在延时阶段就开始做预加载
	 * 所以导致当Timeline页面开始显示时，有可能缓存和网络数据都在写item
	 * 为了解决这个问题，把缓存数据直接存在这StatusTimeline这个Activity里
	 * 如果满足firstLoadTimeline == true 并且 refreshViewerHelper.isComplete = false
	 * 说明是第一次加载，而且这个时候网络返回还没做完，那么就开始加载缓存
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if(refreshViewerHelper == null)
		{
			refreshViewerHelper = RefreshViewerHelper.getInstance();
			refreshViewerHelper.addListenter(this);		
			// 因为在Splash页面时就已经在做预加载，所以到这里时，可能还在loading，也可能已经做完了loading
			if(refreshViewerHelper.isLoading)
				actionBar.setProgressBarVisibility(View.VISIBLE);
			
			// 如果预加载已经做完，则使用mainViewModel用的数据做刷新
			if(refreshViewerHelper.isComplete)
				onRefreshComplete();
			
			// 如果预加载还没做完，并且这是第一次加载timeline,则使用本地缓存做第一次加载
			else if(firstLoadTimeline)
			{
				refreshFromCache();
			}
		}		
		
		if(App.mainViewModel.isChanged)
		{
			refresh();
		}
		else
		{
			if(App.memoryCleaned)
			{
				App.memoryCleaned = false;
				onRefreshComplete();
			}
		}
	}
	
	private void refresh() {		
		actionBar.setProgressBarVisibility(View.VISIBLE);
		refreshViewerHelper.refreshMainViewModel();
	}

	/**
	 * onRefreshComplete 和 refreshFromCache要加锁
	 * 理论上大部分时候应该是先refreshFromCache再会onRefreshComplete
	 * 但是可能同时进入，所以加个锁保险点
	 */
	@Override
	public synchronized void onRefreshComplete() {		
		
		adapter = new StatusTimelineAdapter(this);
		final List<ItemViewModel> input;
		if(App.mainViewModel.items.size() == 0)
		{
			input = new ArrayList<ItemViewModel>();
			ItemViewModel model = generateNoteItemViewModel();
			model.type = EntryType.NotSet;
			input.add(model);
		}
		else {
			input = App.mainViewModel.items;
		}
		adapter.setListModel(input);
		
		listViewTimeline.post(new Runnable() {					
			@Override
			public void run() {		
				actionBar.setProgressBarVisibility(View.GONE);
				pullToRefreshListView.onRefreshComplete();
				listViewTimeline.setAdapter(adapter);
			}
		});	
	}
	
	public synchronized void refreshFromCache()
	{
		// 读缓存
		try {
			File myDir = App.getAppContext().getCacheDir();		
			File cacheFile = new File(myDir , AppConstants.CACHE_ITEM);
			FileInputStream fis = new FileInputStream(cacheFile);
			ObjectInputStream ois = new ObjectInputStream(fis);
			ArrayList<ItemViewModel> cacheItems = (ArrayList<ItemViewModel>) ois.readObject();			
			adapter = new StatusTimelineAdapter(this);
			if(cacheItems.size() == 0)
			{
				ItemViewModel model = generateNoteItemViewModel();
				model.type = EntryType.NotSet;
				cacheItems.add(model);
			}
			adapter.setListModel(cacheItems);
			listViewTimeline.post(new Runnable() {					
				@Override
				public void run() {					
					listViewTimeline.setAdapter(adapter);
				}
			});	
			ois.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private ItemViewModel generateNoteItemViewModel()
	{
		ItemViewModel model = new ItemViewModel();
		model.type = EntryType.NotSet;
		model.title = ">_<";
		model.content = "未得到任何信息，请先设置好关注人，并确保网络畅通~";
		return model;
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
				if(item.type == EntryType.Rss)
				{
					holder.imageAvatar.setImageResource(R.drawable.thumb_rss);
				}
				else if(item.type == EntryType.NotSet)
				{
					holder.imageAvatar.setImageResource(R.drawable.thumb_cry);
				}
				else
				{
					drawableManager.fetchDrawableOnThread(item.iconURL, holder.imageAvatar);
				}
				holder.textTitle.setText(item.title);
				
				holder.textContent.setText(item.content);
				if(StringTool.isNullOrEmpty(item.content))
				{
					holder.textContent.setVisibility(View.GONE);
				}
				else 
				{
					holder.textContent.setVisibility(View.VISIBLE);
				}
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
					if(StringTool.isNullOrEmpty(item.forwardItem.getContentWithTitle()))
					{
						holder.textForwardContent.setVisibility(View.GONE);
					}
					else 
					{
						holder.textForwardContent.setVisibility(View.VISIBLE);
					}
					
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
