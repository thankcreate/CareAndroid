package com.thankcreate.care.picture;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import com.buuuk.android.gallery.ImageViewFlipper;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.thankcreate.care.App;
import com.thankcreate.care.AppConstants;
import com.thankcreate.care.BaseActivity;
import com.thankcreate.care.R;
import com.thankcreate.care.R.drawable;
import com.thankcreate.care.R.layout;
import com.thankcreate.care.R.menu;
import com.thankcreate.care.status.StatusAddCommentActivity;
import com.thankcreate.care.status.StatusDetailActivity;
import com.thankcreate.care.tool.misc.DateTool;
import com.thankcreate.care.tool.ui.DrawableManager;
import com.thankcreate.care.tool.ui.ListViewTool;
import com.thankcreate.care.tool.ui.RefreshViewerHelper;
import com.thankcreate.care.tool.ui.RefreshViewerHelper.OnRefreshCompleteListener;
import com.thankcreate.care.viewmodel.CommentViewModel;
import com.thankcreate.care.viewmodel.EntryType;
import com.thankcreate.care.viewmodel.ItemViewModel;
import com.thankcreate.care.viewmodel.PictureItemViewModel;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.service.dreams.DreamService;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class PictureWallActivity extends BaseActivity implements OnRefreshCompleteListener{
	
	private ActionBar actionBar;
	private PullToRefreshGridView pullToRefreshGridView;
	private GridView gridViewPictureWall;
	private DrawableManager drawableManager = App.getDrawableManager();
	private PictureWallAdapter adapter;
	private RefreshViewerHelper refreshViewerHelper;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture_picture_wall);		 
		initActionBar();
		initControl();
	}

	
	@Override
	protected void onResume() {
		super.onResume();
		if (refreshViewerHelper == null) {
			refreshViewerHelper = RefreshViewerHelper.getAppInstance();
			refreshViewerHelper.addListenter(this);
			// 因为在Splash页面时就已经在做预加载，所以到这里时，可能还在loading，也可能已经做完了loading
			if (refreshViewerHelper.isLoading)
				actionBar.setProgressBarVisibility(View.VISIBLE);

			// 如果预加载已经做完，则使用mainViewModel用的数据做刷新
			if (refreshViewerHelper.isComplete)
				onRefreshComplete();

			// 如果预加载还没做完，则使用本地缓存做第一次加载
			else {
				refreshFromCache();
			}
		}

		if (App.mainViewModel.isChanged) {
			refresh();
		} else {
			if (App.memoryCleaned) {
				App.memoryCleaned = false;
				onRefreshComplete();
			}
		}		
	}
	
	private void refresh() {
		actionBar.setProgressBarVisibility(View.VISIBLE);
		refreshViewerHelper.refreshMainViewModel();
	}
	
	public synchronized void refreshFromCache() {
		// 读缓存
		try {
			File myDir = App.getAppContext().getFilesDir();
			File cacheFile = new File(myDir, AppConstants.CACHE_PIC_ITEM);
			FileInputStream fis = new FileInputStream(cacheFile);
			ObjectInputStream ois = new ObjectInputStream(fis);
			ArrayList<PictureItemViewModel> cacheItems = (ArrayList<PictureItemViewModel>) ois
					.readObject();
			loadPicture(cacheItems);
			ois.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	
	private void initActionBar() {
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("图片");
		actionBar.SetTitleLogo(R.drawable.tab_picture);
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

	private void initControl() {
		pullToRefreshGridView = (PullToRefreshGridView) findViewById(R.id.picture_wall_grid_view);
		gridViewPictureWall = pullToRefreshGridView.getRefreshableView();		
		pullToRefreshGridView
				.setOnRefreshListener(new OnRefreshListener<GridView>() {
					@Override
					public void onRefresh(
							PullToRefreshBase<GridView> refreshView) {
						refresh();
					}
				});
		SharedPreferences pref = App.getAppContext().getSharedPreferences(
				AppConstants.PREFERENCES_NAME, Context.MODE_APPEND);
		Long lastTime = pref.getLong("Global_LastUpdateTime", 0);
		String label = "";
		if (lastTime == 0) {
			label = "从未更新";
		} else {
			label = "上次更新:  "
					+ DateUtils.formatDateTime(getApplicationContext(),
							System.currentTimeMillis(),
							DateUtils.FORMAT_SHOW_TIME
									| DateUtils.FORMAT_SHOW_DATE
									| DateUtils.FORMAT_ABBREV_ALL);
		}

		pullToRefreshGridView.getLoadingLayoutProxy()
				.setLastUpdatedLabel(label);
		pullToRefreshGridView.setOnItemClickListener(mOnItemClickListener);
		// gridview的下面一句好像不启作用，所以我把这个滑动到底下载的过程放到了adapter的getView的时候
		//pullToRefreshGridView.setOnLastItemVisibleListener(mOnLastItemVisibleListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_picture_wall, menu);
		return false;
	}
	
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long id) {			
			if(position < 0 || position >= adapter.listModel.size())
				return;
			
			//CommentViewModel commentViewModel = adapter.listModel.get(position);
			Intent intent = new Intent();
			intent.setClass(PictureWallActivity.this, ImageViewFlipper.class);
			intent.putExtra("index", position);			
			startActivity(intent); 
		}
	};
	
	private OnLastItemVisibleListener mOnLastItemVisibleListener = new OnLastItemVisibleListener() {

		@Override
		public void onLastItemVisible() {
			adapter.showNext();
		}
	};


	private void loadPicture(ArrayList<PictureItemViewModel> pictureItems) 
	{
		adapter = new PictureWallAdapter(PictureWallActivity.this);
		adapter.setListModel(pictureItems);
		
		gridViewPictureWall.post(new Runnable() {					
			@Override
			public void run() {
				gridViewPictureWall.setAdapter(adapter);				
			}
		});
	}
	

	 class PictureWallAdapter extends BaseAdapter {

			private final int SHOW_FIRST_TIME = 30;
			private final int SHOW_EACH_TIME = 15;
			private int currentShowCount;
			
			public List<PictureItemViewModel> listModel = new ArrayList();;
			private LayoutInflater mInflater;
			
			public PictureWallAdapter(Context context) {
				super();
				mInflater = LayoutInflater.from(context);
				currentShowCount = SHOW_FIRST_TIME;
			}
			
			public void addItem(PictureItemViewModel model) {
				listModel.add(model);
				notifyDataSetChanged();
			}
			
			public void setListModel(List<PictureItemViewModel> input){
				listModel = input;
				notifyDataSetChanged();
			}

			@Override
			public int getCount() {
				if(listModel.size() < currentShowCount)
					return listModel.size();
				return currentShowCount;
			}
			
			public boolean showNext() {
				if (currentShowCount < listModel.size()) {
					currentShowCount += SHOW_EACH_TIME;
					notifyDataSetChanged();
					return true;
				}
				else
					return false;
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
				// 滑动到底，加载更多
				if(position == currentShowCount - 1)
					showNext();
				ViewHolder holder = null;
				if(convertView == null)
				{
					holder = new ViewHolder();					
					convertView = mInflater.inflate(R.layout.gridview_item_picture_wall, null);
					holder.imageThumb = (ImageView) convertView.findViewById(R.id.picture_wall_item_thumb_image);			
					convertView.setTag(holder);
				}
				else
				{
					holder = (ViewHolder)convertView.getTag();				
				}
			
				PictureItemViewModel pic = listModel.get(position);
				if(pic == null)
					return null;
				holder.imageThumb.setTag(pic.smallURL);	
				holder.imageThumb.setImageResource(R.drawable.thumb_default_thumb1);
				drawableManager.fetchDrawableOnThread(pic.smallURL, holder.imageThumb);
				return convertView;
			}
			
			public class ViewHolder {
		        public ImageView imageThumb;
		        public int tag;
		    }

		}


	@Override
	public void onRefreshComplete() {
		actionBar.post(new Runnable() {
			@Override
			public void run() {
				pullToRefreshGridView.onRefreshComplete();
				actionBar.setProgressBarVisibility(View.GONE);	
			}
		});
		
		loadPicture(App.mainViewModel.pictureItems);
			
		
		Long current = System.currentTimeMillis();
		final String label = "上次更新:  "
				+ DateUtils.formatDateTime(getApplicationContext(),
						current, DateUtils.FORMAT_SHOW_TIME
								| DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL);
		pullToRefreshGridView.post(new Runnable() {
			
			@Override
			public void run() {
				pullToRefreshGridView.getLoadingLayoutProxy()
				.setLastUpdatedLabel(label);
				
			}
		});
		SharedPreferences pref = App.getAppContext()
				.getSharedPreferences(AppConstants.PREFERENCES_NAME,
						Context.MODE_APPEND);
		Editor editor = pref.edit();
		editor.putLong("Global_LastUpdateTime", current);
		editor.commit();
	}

}
