package com.thankcreate.care.tool.ui;

import com.thankcreate.care.tool.TaskHelper;
import com.thankcreate.care.tool.TaskHelper.OnTaskCompleteListener;

public class RefreshViewerHelper implements OnTaskCompleteListener{
	
	public TaskHelper taskHelper;
	private OnRefreshCompleteListener onRefreshCompleteListener;
	
	
	
	public RefreshViewerHelper(
			OnRefreshCompleteListener onRefreshCompleteListener) {
		super();
		taskHelper = new TaskHelper(this);
		this.onRefreshCompleteListener = onRefreshCompleteListener;
	}


	public void RefreshMainViewModel()
	{
		taskHelper.clear();
		taskHelper.pushTask("SinaWeibo");
		
        // 1.Weibo
        RefreshModelSinaWeibo();
        // 2.Rss
        RefreshModelRssFeed();
        // 3.Renren
        RefreshModelRenren();
        // 4.Douban 
        RefreshModelDouban();
	}


	private void RefreshModelSinaWeibo() {
		
	}


	private void RefreshModelRssFeed() {
		// TODO Auto-generated method stub
		
	}
	
	private void RefreshModelRenren() {
		// TODO Auto-generated method stub
		
	}

	private void RefreshModelDouban() {
		// TODO Auto-generated method stub
		
	}





	@Override
	public void onTaskComplete() {
		if(onRefreshCompleteListener != null)
			onRefreshCompleteListener.onRefreshComplete();
	}
	
	public interface OnRefreshCompleteListener
	{
		public void onRefreshComplete();
	}


}
