package com.thankcreate.care.status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.buuuk.android.gallery.ImageViewFlipper;
import com.dongxuexidu.douban4j.constants.DefaultConfigs;
import com.dongxuexidu.douban4j.utils.HttpManager;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.renren.api.connect.android.AsyncRenren;
import com.renren.api.connect.android.exception.RenrenError;
import com.thankcreate.care.App;
import com.thankcreate.care.BaseActivity;
import com.thankcreate.care.R;
import com.thankcreate.care.R.layout;
import com.thankcreate.care.R.menu;
import com.thankcreate.care.picture.PictureWallActivity;
import com.thankcreate.care.status.StatusTimelineActivity.StatusTimelineAdapter;
import com.thankcreate.care.status.StatusTimelineActivity.StatusTimelineAdapter.ViewHolder;
import com.thankcreate.care.tool.converter.DoubanConverter;
import com.thankcreate.care.tool.converter.RenrenConverter;
import com.thankcreate.care.tool.converter.SinaWeiboConverter;
import com.thankcreate.care.tool.misc.DateTool;
import com.thankcreate.care.tool.misc.MiscTool;
import com.thankcreate.care.tool.misc.PreferenceHelper;
import com.thankcreate.care.tool.misc.StringTool;
import com.thankcreate.care.tool.ui.DrawableManager;
import com.thankcreate.care.tool.ui.ListViewTool;
import com.thankcreate.care.tool.ui.ToastHelper;
import com.thankcreate.care.viewmodel.CommentViewModel;
import com.thankcreate.care.viewmodel.EntryType;
import com.thankcreate.care.viewmodel.ItemViewModel;
import com.thankcreate.care.viewmodel.RenrenType;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.CommentsAPI;
import com.weibo.sdk.android.api.WeiboAPI.AUTHOR_FILTER;
import com.weibo.sdk.android.net.RequestListener;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class StatusDetailActivity extends BaseActivity {

	private ActionBar actionBar;
	
	
	private LinearLayout layoutStatus;	
	public ImageView imageAvatar;
    public TextView textTitle;
    public TextView textContent;
    public ImageView imageThumb;
    public LinearLayout layoutForward;		        
    public TextView textForwardContent;
    public ImageView imageForwardThumb;
    public TextView textTime;
    public TextView textCommentCount;
	
    private DrawableManager drawableManager = App.getDrawableManager();
    private ItemViewModel itemViewModel;
    
    private List<CommentViewModel> listComments = new ArrayList<CommentViewModel>();
    
    private ListView listViewComments;
    private CommentsAdapter adapter;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_status_detail);		
		initActionBar();
		initControl();
		parseIntent();
		initControlContent();		
	}

	

	@Override
	protected void onResume() {
		super.onResume();
		loadComments();
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_status_detail, menu);
		return false;
	}
	
	private void initActionBar() {
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("详情");
		actionBar.addActionRight(new Action() {			
			@Override
			public void performAction(View view) {				
				Intent intent = new Intent();
				intent.setClass(StatusDetailActivity.this, StatusAddCommentActivity.class);
				intent.putExtra("itemViewModel", itemViewModel);				
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
		
		imageAvatar = (ImageView) findViewById(R.id.status_detail_avatar_image);
		textTitle = (TextView) findViewById(R.id.status_detail_title);
		
		layoutStatus = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.activity_status_detail_header, null);
		textContent = (TextView) layoutStatus.findViewById(R.id.status_detail_content);
		imageThumb = (ImageView) layoutStatus.findViewById(R.id.status_detail_thumb_image);
		layoutForward = (LinearLayout) layoutStatus.findViewById(R.id.status_detail_forward);
		textForwardContent = (TextView) layoutStatus.findViewById(R.id.status_detail_forward_content);
		imageForwardThumb = (ImageView) layoutStatus.findViewById(R.id.status_detail_forward_thumb_image);
		textTime = (TextView) layoutStatus.findViewById(R.id.status_detail_time);
		textCommentCount = (TextView) layoutStatus.findViewById(R.id.status_detail_comment_count);
		listViewComments = (ListView) findViewById(R.id.status_detail_listViewComments);	
		listViewComments.addHeaderView(layoutStatus);
		listViewComments.setOnItemClickListener(mOnItemClickListener);
		adapter = new CommentsAdapter(this);
		listViewComments.setAdapter(adapter);
	}
	
	private void parseIntent()
	{
		Intent it= this.getIntent();
		
		itemViewModel =(ItemViewModel) it.getSerializableExtra("itemViewModel");
		if(itemViewModel == null)
			finish();
	}
	
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener()
	{

		@Override
		public void onItemClick(AdapterView<?> arg0,  View view, int position,
				long id) {
			// position是从1开始算起的?
			position -= 1;
			if(position < 0 || position >= adapter.listModel.size())
				return;
			CommentViewModel commentViewModel = adapter.listModel.get(position);
			Intent intent = new Intent();
			intent.setClass(StatusDetailActivity.this, StatusAddCommentActivity.class);
			intent.putExtra("itemViewModel", itemViewModel);
			intent.putExtra("commentViewModel", commentViewModel);
			startActivity(intent);
		}			
	};
	
	private void initControlContent() {
		if(itemViewModel == null)
			return;
		drawableManager.fetchDrawableOnThread(itemViewModel.largeIconURL, imageAvatar);
		textTitle.setText(itemViewModel.title);
		textContent.setText(itemViewModel.content);
		
		if(StringTool.isNullOrEmpty(itemViewModel.imageURL))
		{
			imageThumb.setVisibility(View.GONE);
		}
		else
		{
			imageThumb.setVisibility(View.VISIBLE);			
			drawableManager.fetchDrawableOnThread(itemViewModel.imageURL, imageThumb);		
			imageThumb.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
					intent.setClass(StatusDetailActivity.this, ImageViewFlipper.class);
					intent.putExtra("src", itemViewModel.fullImageURL);			
					startActivity(intent); 
				}
			});
		}
		
		// 转发部分
		if(itemViewModel.forwardItem == null)
		{
			layoutForward.setVisibility(View.GONE);
		}
		else
		{
			layoutForward.setVisibility(View.VISIBLE);
			textForwardContent.setText(itemViewModel.forwardItem.getContentWithTitle());
			
			
			if(StringTool.isNullOrEmpty(itemViewModel.forwardItem.imageURL))
			{
				imageForwardThumb.setVisibility(View.GONE);
			}
			else
			{
				imageForwardThumb.setVisibility(View.VISIBLE);							
				drawableManager.fetchDrawableOnThread(itemViewModel.forwardItem.imageURL, imageForwardThumb);
				imageForwardThumb.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent();
						intent.setClass(StatusDetailActivity.this, ImageViewFlipper.class);
						intent.putExtra("src", itemViewModel.forwardItem.fullImageURL);			
						startActivity(intent); 
					}
				});
			}
		}
		textTime.setText(DateTool.convertDateToStringInShow(itemViewModel.time));
		textCommentCount.setText(itemViewModel.getCommentCount());
	}
	

	private void loadComments() {
		
		if(itemViewModel == null)
			return;
		if(itemViewModel.type == EntryType.SinaWeibo)
		{
			loadCommentsSinaWeibo();
		}
		else if(itemViewModel.type == EntryType.Renren)
		{
			loadCommentsRenren();
		}
		else if(itemViewModel.type == EntryType.Douban)
		{
			loadCommentsDouban();
		}
	}



	private void loadCommentsSinaWeibo() {
		actionBar.setProgressBarVisibility(View.VISIBLE);
		Oauth2AccessToken oa = MiscTool.getOauth2AccessToken();
		if(oa == null)
			return;

		CommentsAPI commentsAPI = new CommentsAPI(oa);
		commentsAPI.show(Long.valueOf(itemViewModel.ID), 0, 0, 50, 1, AUTHOR_FILTER.ALL , mSinaWeiboCommentsShowRequestListener);
	}
	
	
	private RequestListener mSinaWeiboCommentsShowRequestListener = new RequestListener(){

		@Override
		public void onComplete(String arg0) {
			listComments.clear();
			actionBar.post(new Runnable() {				
				@Override
				public void run() {
					actionBar.setProgressBarVisibility(View.GONE);					
				}
			});
			
			try {
				JSONObject root = new JSONObject(arg0);
				final JSONArray comments = root.getJSONArray("comments");
				if(comments == null)
					return;		
				
				textCommentCount.post(new Runnable() {
					@Override
					public void run() {
						textCommentCount.setText(String.valueOf(comments.length()));						
					}
				});
				
				for (int i = 0; i < comments.length(); i++) {
					JSONObject comment = comments.getJSONObject(i);
					CommentViewModel model = SinaWeiboConverter.convertCommentToCommon(comment);
					if(model != null)
						listComments.add(model);
				}
				refreshCommmentList();
			} catch (Exception e) {
				e.printStackTrace();
				ToastHelper.show("获取评论失败，请确保网络通畅");
			}
		}

		@Override
		public void onError(WeiboException arg0) {
			actionBar.post(new Runnable() {				
				@Override
				public void run() {
					actionBar.setProgressBarVisibility(View.GONE);					
				}
			});
			ToastHelper.show("获取评论失败，请确保网络通畅");
		}

		@Override
		public void onIOException(IOException arg0) {
			actionBar.post(new Runnable() {				
				@Override
				public void run() {
					actionBar.setProgressBarVisibility(View.GONE);					
				}
			});
			ToastHelper.show( "获取评论失败，请确保网络通畅");
		}
		
	};

	private void loadCommentsRenren() {
		AsyncRenren asyncRenren = new AsyncRenren(App.getRenren());
		Bundle bd = new Bundle();	
		int renrenType = itemViewModel.renrenFeedType;
		if(renrenType == RenrenType.TextStatus)
		{
			bd.putString("method", "status.getComment");
			bd.putString("status_id", itemViewModel.ID);
			bd.putString("owner_id", itemViewModel.ownerID);			
		}
		else if(renrenType == RenrenType.UploadPhoto)
		{
			bd.putString("method", "photos.getComments");
			bd.putString("pid", itemViewModel.ID);
			bd.putString("uid", itemViewModel.ownerID);
		}
		else if(renrenType == RenrenType.SharePhoto)
		{
			bd.putString("method", "share.getComments");
			bd.putString("share_id", itemViewModel.ID);
			bd.putString("user_id", itemViewModel.ownerID);
		}
		bd.putString("count", "100");
		asyncRenren.requestJSON(bd, mRenrenCommentsShowRequestListener);
		
	}
	
	private com.renren.api.connect.android.RequestListener mRenrenCommentsShowRequestListener = new com.renren.api.connect.android.RequestListener() {
				
		@Override
		public void onRenrenError(RenrenError renrenError) {
			actionBar.post(new Runnable() {				
				@Override
				public void run() {
					actionBar.setProgressBarVisibility(View.GONE);					
				}
			});
			ToastHelper.show( "获取评论失败，请确保网络通畅");
		}
		
		@Override
		public void onFault(Throwable fault) {
			actionBar.post(new Runnable() {				
				@Override
				public void run() {
					actionBar.setProgressBarVisibility(View.GONE);					
				}
			});
			ToastHelper.show( "获取评论失败，请确保网络通畅");	
		}
		
		@Override
		public void onComplete(String response) {
			try {	
				listComments.clear();
				// 这里很恶心，对于分享的评论是存在comments结点下的，其它的直接存在root结点下
				JSONArray comments = null;
				if(itemViewModel.renrenFeedType == RenrenType.SharePhoto)
		        {
					JSONObject root = new JSONObject(response);
		            comments = root.optJSONArray("comments");
		        }
		        else
		        {
		        	comments = new JSONArray(response);
		        }		
				final int length = comments.length();
				textCommentCount.post(new Runnable() {
					@Override
					public void run() {
						textCommentCount.setText(String.valueOf(length));						
					}
				});
				if(comments != null)
				{
					for(int i = 0; i < comments.length(); i++)
					{
						JSONObject comment = comments.getJSONObject(i);
						CommentViewModel model = RenrenConverter.convertCommentToCommon(comment, itemViewModel.renrenFeedType);
						if(model != null)
							listComments.add(model);
					}
					refreshCommmentList();
				}
			} catch (Exception e) {
				actionBar.post(new Runnable() {				
					@Override
					public void run() {
						actionBar.setProgressBarVisibility(View.GONE);					
					}
				});
				ToastHelper.show( "获取评论失败，请确保网络通畅");
			}
			
		}
	};


	
	/**
	 * 豆瓣比较特殊，转发的评论其实就是原始广播的评论
	 */
	private void loadCommentsDouban() {
		final String token = PreferenceHelper.getString("Douban_Token");
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					listComments.clear();
					HttpManager httpManager = new HttpManager(token);
					String itemID = itemViewModel.ID;
					if(itemViewModel.forwardItem != null)
						itemID = itemViewModel.forwardItem.ID;
					String url = String.format("%s/shuo/v2/statuses/%s/comments", 
							DefaultConfigs.API_URL_PREFIX, itemID);		
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("count", "100")); 
					String result = httpManager.getResponseString(url, params, true);					
					JSONArray comments = new JSONArray(result);
					if(comments != null)
					{
						for(int i = 0; i < comments.length(); i++)
						{
							JSONObject ob = comments.getJSONObject(i);
							CommentViewModel model = DoubanConverter.convertCommentToCommon(ob);
							if(model != null)
							{
								listComments.add(model);
							}
						}
					}
					final int length = comments.length();
					textCommentCount.post(new Runnable() {
						@Override
						public void run() {
							textCommentCount.setText(String.valueOf(length));						
						}
					});
					refreshCommmentList();
					actionBar.post(new Runnable() {				
						@Override
						public void run() {
							actionBar.setProgressBarVisibility(View.GONE);					
						}
					});
				} catch (Exception e) {
					actionBar.post(new Runnable() {				
						@Override
						public void run() {
							actionBar.setProgressBarVisibility(View.GONE);					
						}
					});
					ToastHelper.show( "获取评论失败，请确保网络通畅");
				}				
			}
		}).start();
	}


	
	
	
	private void refreshCommmentList() {
		
		adapter = new CommentsAdapter(this);
		adapter.setListModel(listComments);
		
		// 更新一下评论数，因为有可能是自己又发了一条评论，但是mainViewModel这个时候还没更新
		final int length = listComments.size();
		itemViewModel.commentCount = String.valueOf(length);
		textCommentCount.post(new Runnable() {
			@Override
			public void run() {
				textCommentCount.setText(String.valueOf(length));						
			}
		});
		for (ItemViewModel item : App.mainViewModel.items) {
			if (item != null && !StringTool.isNullOrEmpty(item.ID)
					&& item.ID.equalsIgnoreCase(itemViewModel.ID)) {
				item.commentCount = String.valueOf(length);
				break;
			}
		}
		
		Collections.sort(listComments, new Comparator<CommentViewModel>() {
			@Override
			public int compare(CommentViewModel lhs, CommentViewModel rhs) {
				try {
					int result = lhs.time.compareTo(rhs.time);	
					return -result;
				} catch (Exception e) {
					return 0;
				}
			}
		});
		
		listViewComments.post(new Runnable() {					
			@Override
			public void run() {
				listViewComments.setAdapter(adapter);
			}
		});	
	}
	

	 class CommentsAdapter extends BaseAdapter {

			public List<CommentViewModel> listModel = new ArrayList();;
			private LayoutInflater mInflater;
			
			public CommentsAdapter(Context context) {
				super();
				mInflater = LayoutInflater.from(context);
			}
			
			public void addItem(CommentViewModel model) {
				listModel.add(model);
				notifyDataSetChanged();
			}
			
			public void setListModel(List<CommentViewModel> input){
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
					convertView = mInflater.inflate(R.layout.listview_item_status_comment, null);
					holder.imageAvatar = (ImageView) convertView.findViewById(R.id.comment_list_item_avatar);
					holder.textTitle = (TextView) convertView.findViewById(R.id.comment_list_item_title);	
					holder.textContent = (TextView) convertView.findViewById(R.id.comment_list_item_content);					
					holder.textTime = (TextView) convertView.findViewById(R.id.comment_list_item_time);					
					convertView.setTag(holder);
				}
				else
				{
					holder = (ViewHolder)convertView.getTag();				
				}
			
				CommentViewModel comment = listModel.get(position);
				if(comment == null)
					return null;
				holder.imageAvatar.setTag(comment.iconURL);
				drawableManager.fetchDrawableOnThread(comment.iconURL, holder.imageAvatar);
				holder.textTitle.setText(comment.title);
				holder.textContent.setText(comment.content);				
				holder.textTime.setText(DateTool.convertDateToStringInShow(comment.time));	
				
				return convertView;
			}
			
			public class ViewHolder {
		        public ImageView imageAvatar;
		        public TextView textTitle;
		        public TextView textContent;		        
		        public TextView textTime;		        
		        public int tag;
		    }

		}
}
