package com.thankcreate.care.tool.fetcher;

import java.util.List;

public abstract class BaseFetcher {
	
	public final int MAX_FETCHE_COUNT = 30;
	
	public class CommentMan
	{
		public String id;
		public String name;
	}
	
	public interface FetchCompleteListener
	{
		/**
		 * 
		 * @param  注意，此处返回list可能为null
		 */
		public void fetchComplete(List<CommentMan> list);
	}
	
	public abstract void fetch(FetchCompleteListener listener);
}
