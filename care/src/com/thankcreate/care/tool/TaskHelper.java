package com.thankcreate.care.tool;

import java.util.concurrent.locks.Lock;

public class TaskHelper {
	
	private int taskInProcess = 0;
	
	private OnTaskCompleteListener onTaskCompleteListener;

	public TaskHelper(OnTaskCompleteListener onTaskCompleteListener) {
		super();
		this.onTaskCompleteListener = onTaskCompleteListener;
	}
	
	public void clear()
	{
		taskInProcess = 0;
	}

	public void pushTask(String name)
	{
		pushTask();
	}
	
	public void popTask(String name)
	{
		popTask();
	}
	
	public synchronized void pushTask()
	{	   
		taskInProcess++;		
	}
	
	public synchronized  void popTask()
	{
		
		if(taskInProcess == 0)
		{		
			return;
		}			
		
		--taskInProcess;
		if(taskInProcess == 0)
		{
			if(onTaskCompleteListener != null)
				onTaskCompleteListener.onTaskComplete();
		}		
	}


	public interface OnTaskCompleteListener
	{
		public void onTaskComplete();
	}
	
	

}
