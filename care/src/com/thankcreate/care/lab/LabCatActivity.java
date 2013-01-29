package com.thankcreate.care.lab;

import java.util.Stack;

import org.achartengine.model.CategorySeries;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.thankcreate.care.BaseActivity;
import com.thankcreate.care.R;
import com.thankcreate.care.R.layout;
import com.thankcreate.care.R.menu;
import com.thankcreate.care.tool.ui.ToastHelper;
import com.umeng.analytics.MobclickAgent;
import com.umeng.common.net.r;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class LabCatActivity extends BaseActivity {

	private final int UP = 1;
	private final int DOWN = 2;
	private final int LEFT = 3;
	private final int RIGHT = 4;
	private final int INVALID = 5;
	
	/**
	 * 当Egg被触发后，经过REGRET_TIME会使喵喵复原
	 * 嗯，喵喵~
	 */
	private final int REGRET_TIME = 3000;
	private TextView textMiao;
	
	protected ActionBar actionBar;
	private GestureDetector myGestureDetector;
	
	private float density = 1;
	
	private int correctArray[] = {UP, UP, DOWN, DOWN, LEFT, RIGHT, LEFT, RIGHT};
	private Stack<Integer> inputStack = new Stack<Integer>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lab_cat);
		initActionBar();
		initWhatEver();
		initControl();
		initGesture();
		MobclickAgent.onEvent(this, "LabCatActivity");
	}

	private void initControl() {
		textMiao = (TextView) findViewById(R.id.lab_cat_miao);
	}

	private void initWhatEver() {
		DisplayMetrics metric = new DisplayMetrics();		
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		density = metric.density;
	}

	private void initGesture() {
		myGestureDetector = new GestureDetector(this, new CatGestureListener());
	}	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return myGestureDetector.onTouchEvent(event);
	}
	
	protected void initActionBar() {	
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("奇怪的地方");
		addActionBarBackButton(actionBar);
		actionBar.addActionRight(new Action() {
			@Override
			public void performAction(View view) {
				new AlertDialog.Builder(LabCatActivity.this)
		        .setIcon(R.drawable.thumb_help)
		        .setTitle(">_<")
		        .setMessage("亲，玩过魂斗罗没？")
		        .setPositiveButton("谁要跟你亲！", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int whichButton) {
		            }
		        })        
		        .setPositiveButton("关你蛋事喵~", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int whichButton) {
		            }
		        })   
		        .create().show();
			}
			
			@Override
			public int getDrawable() {
				return R.drawable.thumb_help;
			}
		});
	}
	
	private void dispatchDicrection(int type)
	{
		Log.i("---dirction---", String.valueOf(type));
		inputStack.push(type);
		judge();
	}
	
	private void judge()
	{
		if(inputStack.size() == 0)
			return;
		for(int i = 0; i < inputStack.size(); i++)
		{
			if(correctArray[i] != inputStack.get(i))
			{
				inputStack.clear();
				return;
			}	
		}
		
		// 如果任何一个单字都没有发生不匹配，而且已经到了正确长度
		// 则说明已经完全匹配，开始出效果
		if(inputStack.size() == correctArray.length)
		{
			showEgg();
			inputStack.clear();
		}
	}
	
	private void showEgg()
	{
		MediaPlayer player = MediaPlayer.create(this, R.raw.cat);
        player.start();
        textMiao.setText("喵喵~");
        new Handler().postDelayed(new Runnable() {
			public void run() {
				textMiao.setText("喵~");
			}
		}, REGRET_TIME);
	}
	
	
	class CatGestureListener implements OnGestureListener{

		@Override
		public boolean onDown(MotionEvent e) {
			return false;
		}

		
		@Override
		/**
		 * e1是起始点，e2是终结点，后面那两个蛋蛋是加速度
		 */
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			float xDelta = e2.getX() - e1.getX();
			float yDelta = e2.getY() - e1.getY();
			if(Math.abs(xDelta) > Math.abs(yDelta))
			{
				if(xDelta > 0)					
					dispatchDicrection(RIGHT);
				else 
					dispatchDicrection(LEFT);				
			}
			else if(Math.abs(yDelta) > Math.abs(xDelta))
			{
				if(yDelta > 0)					
					dispatchDicrection(DOWN);
				else 
					dispatchDicrection(UP);				
			}
			else
			{
				dispatchDicrection(INVALID);
			}
				
			return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
			
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return false;
		}
		
	}
	

}
