package com.thankcreate.care.picture;

import java.util.ArrayList;
import java.util.List;

import com.buuuk.android.gallery.ImageViewFlipper;
import com.markupartist.android.widget.ActionBar;
import com.thankcreate.care.App;
import com.thankcreate.care.BaseActivity;
import com.thankcreate.care.R;
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
import com.thankcreate.care.viewmodel.ItemViewModel;
import com.thankcreate.care.viewmodel.PictureItemViewModel;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.service.dreams.DreamService;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class PictureWallActivity extends BaseActivity implements OnRefreshCompleteListener{
	
	private ActionBar actionBar;
	private GridView gridViewPictureWall;
	private DrawableManager drawableManager = App.drawableManager;
	private PictureWallAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture_picture_wall);
		RefreshViewerHelper.getInstance().addListenter(this); 
		initActionBar();
		initControl();
		loadPicture();
	}


	@Override
	protected void onResume() {
		super.onResume();
		if(App.mainViewModel.isChanged)
		{
			RefreshViewerHelper.getInstance().refreshMainViewModel();
		}
	}
	
	
	private void initActionBar() {
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("图片");
		actionBar.SetTitleLogo(R.drawable.tab_picture);
	}
	
	private void initControl() {
		gridViewPictureWall = (GridView) findViewById(R.id.picture_wall_grid_view);
		gridViewPictureWall.setOnItemClickListener(mOnItemClickListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_picture_wall, menu);
		return true;
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

	private void loadPicture() 
	{
		adapter = new PictureWallAdapter(PictureWallActivity.this);
		adapter.setListModel(App.mainViewModel.pictureItems);
		
		gridViewPictureWall.post(new Runnable() {					
			@Override
			public void run() {
				gridViewPictureWall.setAdapter(adapter);				
			}
		});
	}
	

	 class PictureWallAdapter extends BaseAdapter {

			public List<PictureItemViewModel> listModel = new ArrayList();;
			private LayoutInflater mInflater;
			
			public PictureWallAdapter(Context context) {
				super();
				mInflater = LayoutInflater.from(context);
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
		loadPicture();
	}

}
