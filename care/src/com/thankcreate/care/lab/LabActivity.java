package com.thankcreate.care.lab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.buuuk.android.gallery.ImageViewFlipper;
import com.markupartist.android.widget.ActionBar;
import com.thankcreate.care.App;
import com.thankcreate.care.BaseActivity;
import com.thankcreate.care.R;
import com.thankcreate.care.R.layout;
import com.thankcreate.care.R.menu;
import com.thankcreate.care.picture.PictureWallActivity;

import android.os.Bundle;
import android.app.Activity;
import android.app.LauncherActivity.ListItem;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class LabActivity extends BaseActivity {
	private ActionBar actionBar;
	private GridView gridView;
	private LabItemAdapter adapter;
	private Class[] activities;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lab_lab);
		initActionBar();
		initControl();
		loadGridView();
	}
	
	private void initActionBar()
	{
		actionBar  = (ActionBar) findViewById(R.id.actionbar);     
        actionBar.setTitle("奇怪的地方");    
        actionBar.SetTitleLogo(R.drawable.tab_microscope);
	}
	
	private void initControl()
	{
		activities = new Class[]{
				LabTimelineActivity.class,
				LabCharactorAnalysisActivity.class,
				LabPercentageActivity.class,
				LabEnemyActivity.class,
				LabCatActivity.class
			};
		gridView = (GridView) findViewById(R.id.lab_grid_view);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int index,
					long id) {
				if(index < 0 || index >= adapter.listModel.size())
					return;
				
				Intent intent = new Intent();
				intent.setClass(LabActivity.this, activities[index]);					
				startActivity(intent); 
			}
		});
	}
	
	private void loadGridView()
	{

		
		adapter = new LabItemAdapter(LabActivity.this);
		Integer[]  arrayPicID = new Integer[]{
				R.drawable.lab_1, 
				R.drawable.lab_2,
				R.drawable.lab_3,
				R.drawable.lab_4,
				R.drawable.lab_5};
		List<Integer> listPicID  = Arrays.asList(arrayPicID);
		adapter.setListModel(listPicID);
		gridView.setAdapter(adapter);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_lab, menu);
		return true;
	}
	
	 class LabItemAdapter extends BaseAdapter {

			public List<Integer> listModel = new ArrayList();;
			private LayoutInflater mInflater;
			
			public LabItemAdapter(Context context) {
				super();
				mInflater = LayoutInflater.from(context);
			}
			
			public void addItem(int model) {
				listModel.add(model);
				notifyDataSetChanged();
			}
			
			public void setListModel(List<Integer> input){
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
					convertView = mInflater.inflate(R.layout.gridview_item_lab, null);
					holder.imageThumb = (ImageView) convertView.findViewById(R.id.lab_item_thumb_image);
					LayoutParams params = holder.imageThumb.getLayoutParams();
					params.height = params.width;
					holder.imageThumb.setLayoutParams(params);
					convertView.setTag(holder);
				}
				else
				{
					holder = (ViewHolder)convertView.getTag();				
				}
			
				Integer picID = listModel.get(position);
				if(picID == null)
					return null;
					
				holder.imageThumb.setImageResource(picID);
				return convertView;
			}
			
			public class ViewHolder {
		        public ImageView imageThumb;
		        public int tag;
		    }

		}
}
